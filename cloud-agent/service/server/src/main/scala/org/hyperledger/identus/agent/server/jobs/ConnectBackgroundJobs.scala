package org.hyperledger.identus.agent.server.jobs

import org.hyperledger.identus.agent.server.config.AppConfig
import org.hyperledger.identus.agent.server.jobs.BackgroundJobError.ErrorResponseReceivedFromPeerAgent
import org.hyperledger.identus.agent.walletapi.model.error.DIDSecretStorageError
import org.hyperledger.identus.agent.walletapi.model.error.DIDSecretStorageError.{KeyNotFoundError, WalletNotFoundError}
import org.hyperledger.identus.agent.walletapi.service.ManagedDIDService
import org.hyperledger.identus.agent.walletapi.storage.DIDNonSecretStorage
import org.hyperledger.identus.connect.core.model.error.ConnectionServiceError.InvalidStateForOperation
import org.hyperledger.identus.connect.core.model.error.ConnectionServiceError.RecordIdNotFound
import org.hyperledger.identus.connect.core.model.ConnectionRecord
import org.hyperledger.identus.connect.core.model.ConnectionRecord.*
import org.hyperledger.identus.connect.core.service.ConnectionService
import org.hyperledger.identus.mercury.*
import org.hyperledger.identus.mercury.error.SendMessageError
import org.hyperledger.identus.resolvers.DIDResolver
import org.hyperledger.identus.shared.models.WalletAccessContext
import org.hyperledger.identus.shared.utils.aspects.CustomMetricsAspect
import org.hyperledger.identus.shared.utils.DurationOps.toMetricsSeconds
import zio.*
import zio.metrics.*

object ConnectBackgroundJobs extends BackgroundJobsHelper {

  // val didCommExchanges = {
  //   for {
  //     connectionService <- ZIO.service[ConnectionService]
  //     config <- ZIO.service[AppConfig]
  //     records <- connectionService
  //       .findRecordsByStatesForAllWallets(
  //         ignoreWithZeroRetries = true,
  //         limit = config.connect.connectBgJobRecordsLimit,
  //         ConnectionRecord.ProtocolState.ConnectionRequestPending,
  //         ConnectionRecord.ProtocolState.ConnectionResponsePending
  //       )
  //     _ <- ZIO.foreachPar(records)(performExchange).withParallelism(config.connect.connectBgJobProcessingParallelism)
  //   } yield ()
  // }

  // private def performExchange(
  //     record: ConnectionRecord
  // ): URIO[
  //   DidOps & DIDResolver & HttpClient & ConnectionService & ManagedDIDService & DIDNonSecretStorage & AppConfig,
  //   Unit
  // ] = {
  //   import ProtocolState.*
  //   import Role.*

  //   def counterMetric(key: String) = Metric
  //     .counterInt(key)
  //     .fromConst(1)

  //   val InviteeConnectionRequestMsgFailed = counterMetric(
  //     "connection_flow_invitee_connection_request_msg_failed_counter"
  //   )
  //   val InviteeConnectionRequestMsgSuccess = counterMetric(
  //     "connection_flow_invitee_connection_request_msg_success_counter"
  //   )
  //   val InviterConnectionResponseMsgFailed = counterMetric(
  //     "connection_flow_inviter_connection_response_msg_failed_counter"
  //   )
  //   val InviterConnectionResponseMsgSuccess = counterMetric(
  //     "connection_flow_inviter_connection_response_msg_success_counter"
  //   )
  //   val InviteeProcessConnectionRecordPendingSuccess = counterMetric(
  //     "connection_flow_invitee_process_connection_record_success_counter"
  //   )
  //   val InviteeProcessConnectionRecordPendingFailed = counterMetric(
  //     "connection_flow_invitee_process_connection_record_failed_counter"
  //   )
  //   val InviteeProcessConnectionRecordPendingTotal = counterMetric(
  //     "connection_flow_invitee_process_connection_record_all_counter"
  //   )
  //   val InviterProcessConnectionRecordPendingSuccess = counterMetric(
  //     "connection_flow_inviter_process_connection_record_success_counter"
  //   )
  //   val InviterProcessConnectionRecordPendingFailed = counterMetric(
  //     "connection_flow_inviter_process_connection_record_failed_counter"
  //   )
  //   val InviterProcessConnectionRecordPendingTotal = counterMetric(
  //     "connection_flow_inviter_process_connection_record_all_counter"
  //   )

  //   val exchange = record match {
  //     case ConnectionRecord(
  //           id,
  //           _,
  //           _,
  //           _,
  //           _,
  //           _,
  //           _,
  //           Invitee,
  //           ConnectionRequestPending,
  //           _,
  //           Some(request),
  //           _,
  //           metaRetries,
  //           _,
  //           _
  //         ) if metaRetries > 0 =>
  //       val inviteeProcessFlow = for {
  //         walletAccessContext <- buildWalletAccessContextLayer(request.from)
  //         result <- (for {
  //           didCommAgent <- buildDIDCommAgent(request.from).provideSomeLayer(ZLayer.succeed(walletAccessContext))
  //           resp <- MessagingService.send(request.makeMessage).provideSomeLayer(didCommAgent) @@ Metric
  //             .gauge("connection_flow_invitee_send_connection_request_ms_gauge")
  //             .trackDurationWith(_.toMetricsSeconds)
  //           connectionService <- ZIO.service[ConnectionService]
  //           _ <- {
  //             if (resp.status >= 200 && resp.status < 300)
  //               connectionService.markConnectionRequestSent(id).provideSomeLayer(ZLayer.succeed(walletAccessContext))
  //                 @@ InviteeConnectionRequestMsgSuccess
  //                 @@ CustomMetricsAspect.endRecordingTime(
  //                   s"${record.id}_invitee_pending_to_req_sent",
  //                   "connection_flow_invitee_pending_to_req_sent_ms_gauge"
  //                 )
  //             else ZIO.fail(ErrorResponseReceivedFromPeerAgent(resp)) @@ InviteeConnectionRequestMsgFailed
  //           }
  //         } yield ()).mapError(e => (walletAccessContext, e))
  //       } yield result

  //       // inviteeProcessFlow // TODO decrease metaRetries if it has a error

  //       inviteeProcessFlow
  //     // @@ InviteeProcessConnectionRecordPendingSuccess.trackSuccess
  //     // @@ InviteeProcessConnectionRecordPendingFailed.trackError
  //     // @@ InviteeProcessConnectionRecordPendingTotal
  //     // @@ Metric
  //     //   .gauge("connection_flow_invitee_process_connection_record_ms_gauge")
  //     //   .trackDurationWith(_.toMetricsSeconds)

  //     case ConnectionRecord(
  //           id,
  //           _,
  //           _,
  //           _,
  //           _,
  //           _,
  //           _,
  //           Inviter,
  //           ConnectionResponsePending,
  //           _,
  //           _,
  //           Some(response),
  //           metaRetries,
  //           _,
  //           _
  //         ) if metaRetries > 0 =>
  //       val inviterProcessFlow = for {
  //         walletAccessContext <- buildWalletAccessContextLayer(response.from)
  //         result <- (for {
  //           didCommAgent <- buildDIDCommAgent(response.from).provideSomeLayer(ZLayer.succeed(walletAccessContext))
  //           resp <- MessagingService.send(response.makeMessage).provideSomeLayer(didCommAgent) @@ Metric
  //             .gauge("connection_flow_inviter_send_connection_response_ms_gauge")
  //             .trackDurationWith(_.toMetricsSeconds)
  //           connectionService <- ZIO.service[ConnectionService]
  //           _ <- {
  //             if (resp.status >= 200 && resp.status < 300)
  //               connectionService.markConnectionResponseSent(id).provideSomeLayer(ZLayer.succeed(walletAccessContext))
  //                 @@ InviterConnectionResponseMsgSuccess
  //                 @@ CustomMetricsAspect.endRecordingTime(
  //                   s"${record.id}_inviter_pending_to_res_sent",
  //                   "connection_flow_inviter_pending_to_res_sent_ms_gauge"
  //                 )
  //             else ZIO.fail(ErrorResponseReceivedFromPeerAgent(resp)) @@ InviterConnectionResponseMsgFailed
  //           }
  //         } yield ()).mapError(e => (walletAccessContext, e))
  //       } yield result

  //       inviterProcessFlow
  //         @@ InviterProcessConnectionRecordPendingSuccess.trackSuccess
  //         @@ InviterProcessConnectionRecordPendingFailed.trackError
  //         @@ InviterProcessConnectionRecordPendingTotal
  //         @@ Metric
  //           .gauge("connection_flow_inviter_process_connection_record_ms_gauge")
  //           .trackDurationWith(_.toMetricsSeconds)
  //     case _ => ZIO.unit
  //   }

  //   val tmp: ZIO[
  //     HttpClient & DidOps & DIDResolver & (DIDNonSecretStorage & ManagedDIDService & ConnectionService),
  //     WalletNotFoundError |
  //       (WalletAccessContext, KeyNotFoundError | SendMessageError | (RecordIdNotFound | InvalidStateForOperation)),
  //     Unit
  //   ] = exchange

  //   tmp
  //     .tapError({
  //       case walletNotFound: WalletNotFoundError =>
  //         ZIO.logErrorCause(
  //           s"Connect - Error processing record: ${record.id}",
  //           Cause.fail(walletNotFound)
  //         )
  //       case ((walletAccessContext, e)) =>
  //         for {
  //           connectService <- ZIO.service[ConnectionService]
  //           errorResponse: org.hyperledger.identus.shared.models.Failure = e match
  //             case ex @ KeyNotFoundError(didId, keyId)               => ex
  //             case ex @ ErrorResponseReceivedFromPeerAgent(response) => ex
  //             case ex @ SendMessageError(cause, mData)               => ex: Int
  //             case ex @ RecordIdNotFound(recordId)                   => ex
  //             case ex @ InvalidStateForOperation(state)              => ex: Int
  //             // e: KeyNotFoundError | SendMessageError | (RecordIdNotFound | InvalidStateForOperation | ErrorResponseReceivedFromPeerAgent)
  //           _ <- connectService
  //             .reportProcessingFailure(record.id, errorResponse)
  //             .provideSomeLayer(ZLayer.succeed(walletAccessContext))
  //         } yield ()
  //     })
  //     .catchAll(e => ZIO.logErrorCause(s"Connect - Error processing record: ${record.id} ", Cause.fail(e)))
  //     .catchAllDefect(d => ZIO.logErrorCause(s"Connect - Defect processing record: ${record.id}", Cause.fail(d)))
  // }

}
