package io.iohk.atala.agent.server.jobs

import io.iohk.atala.agent.server.config.AppConfig
import io.iohk.atala.agent.server.jobs.BackgroundJobError.ErrorResponseReceivedFromPeerAgent
import io.iohk.atala.agent.walletapi.model.error.DIDSecretStorageError
import io.iohk.atala.agent.walletapi.model.error.DIDSecretStorageError.KeyNotFoundError
import io.iohk.atala.agent.walletapi.service.ManagedDIDService
import io.iohk.atala.connect.core.model.ConnectionRecord
import io.iohk.atala.connect.core.model.ConnectionRecord.*
import io.iohk.atala.connect.core.model.error.ConnectionServiceError
import io.iohk.atala.connect.core.service.ConnectionService
import io.iohk.atala.mercury.*
import io.iohk.atala.mercury.model.*
import io.iohk.atala.mercury.model.error.*
import io.iohk.atala.resolvers.DIDResolver
import zio.*
object ConnectBackgroundJobs {

  val didCommExchanges = {
    for {
      connectionService <- ZIO.service[ConnectionService]
      config <- ZIO.service[AppConfig]
      records <- connectionService
        .getConnectionRecordsByStates(
          ignoreWithZeroRetries = true,
          limit = config.connect.connectBgJobRecordsLimit,
          ConnectionRecord.ProtocolState.ConnectionRequestPending,
          ConnectionRecord.ProtocolState.ConnectionResponsePending
        )
        .mapError(err => Throwable(s"Error occurred while getting connection records: $err"))
      _ <- ZIO.foreachPar(records)(performExchange).withParallelism(config.connect.connectBgJobProcessingParallelism)
    } yield ()
  }

  private[this] def performExchange(
      record: ConnectionRecord
  ): URIO[DidOps & DIDResolver & HttpClient & ConnectionService & ManagedDIDService, Unit] = {
    import ProtocolState.*
    import Role.*
    val exchange = record match {
      case ConnectionRecord(
            id,
            _,
            _,
            _,
            _,
            Invitee,
            ConnectionRequestPending,
            _,
            Some(request),
            _,
            metaRetries,
            _,
            _
          ) if metaRetries > 0 =>
        val aux = for {

          didCommAgent <- buildDIDCommAgent(request.from)
          resp <- MessagingService.send(request.makeMessage).provideSomeLayer(didCommAgent)
          connectionService <- ZIO.service[ConnectionService]
          _ <- {
            if (resp.status >= 200 && resp.status < 300) connectionService.markConnectionRequestSent(id)
            else ZIO.fail(ErrorResponseReceivedFromPeerAgent(resp))
          }
        } yield ()

        // aux // TODO decrete metaRetries if it has a error
        aux

      case ConnectionRecord(
            id,
            _,
            _,
            _,
            _,
            Inviter,
            ConnectionResponsePending,
            _,
            _,
            Some(response),
            metaRetries,
            _,
            _
          ) if metaRetries > 0 =>
        for {
          didCommAgent <- buildDIDCommAgent(response.from)
          resp <- MessagingService.send(response.makeMessage).provideSomeLayer(didCommAgent)
          connectionService <- ZIO.service[ConnectionService]
          _ <- {
            if (resp.status >= 200 && resp.status < 300) connectionService.markConnectionResponseSent(id)
            else ZIO.fail(ErrorResponseReceivedFromPeerAgent(resp))
          }
        } yield ()

      case _ => ZIO.unit
    }

    exchange
      .tapError(e =>
        for {
          connectService <- ZIO.service[ConnectionService]
          _ <- connectService
            .reportProcessingFailure(record.id, Some(e.toString))
            .tapError(err =>
              ZIO.logErrorCause(
                s"Connect - failed to report processing failure: ${record.id}",
                Cause.fail(err)
              )
            )
        } yield ()
      )
      .catchAll(e => ZIO.logErrorCause(s"Connect - Error processing record: ${record.id} ", Cause.fail(e)))
      .catchAllDefect(d => ZIO.logErrorCause(s"Connect - Defect processing record: ${record.id}", Cause.fail(d)))
  }

  private[this] def buildDIDCommAgent(
      myDid: DidId
  ): ZIO[ManagedDIDService, KeyNotFoundError, ZLayer[Any, Nothing, DidAgent]] = {
    for {
      managedDidService <- ZIO.service[ManagedDIDService]
      peerDID <- managedDidService.getPeerDID(myDid)
      agent = AgentPeerService.makeLayer(peerDID)
    } yield agent
  }

}
