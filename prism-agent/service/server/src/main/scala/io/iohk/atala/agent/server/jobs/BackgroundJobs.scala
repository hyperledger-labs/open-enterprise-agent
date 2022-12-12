package io.iohk.atala.agent.server.jobs

import scala.jdk.CollectionConverters.*
import zio.*
import io.iohk.atala.pollux.core.service.CredentialService
import io.iohk.atala.pollux.core.model.IssueCredentialRecord
import io.iohk.atala.pollux.core.model.error.CredentialServiceError
import io.iohk.atala.pollux.core.service.CredentialService
import io.iohk.atala.pollux.vc.jwt.W3cCredentialPayload
import zio.*

import java.time.Instant

import io.iohk.atala.mercury.DidComm
import io.iohk.atala.mercury.MediaTypes
import io.iohk.atala.mercury.model._
import io.iohk.atala.mercury.model.error._
import io.iohk.atala.mercury.protocol.issuecredential._
import io.iohk.atala.resolvers.UniversalDidResolver
import io.iohk.atala.agent.server.jobs.MercuryUtils.sendMessage
import java.io.IOException

import zhttp.service._
import zhttp.http._
import io.iohk.atala.pollux.vc.jwt.JwtCredential
import io.iohk.atala.pollux.core.model.PresentationRecord
import io.iohk.atala.mercury.protocol.presentproof.RequestPresentation
import io.iohk.atala.pollux.core.service.PresentationService
import io.iohk.atala.pollux.core.model.error.PresentationError
import io.iohk.atala.agent.server.http.model.{InvalidState, NotImplemented}

object BackgroundJobs {

  val didCommExchanges = {
    for {
      credentialService <- ZIO.service[CredentialService]
      records <- credentialService
        .getIssueCredentialRecords()
        .mapError(err => Throwable(s"Error occured while getting issue credential records: $err"))
      _ <- ZIO.foreach(records)(performExchange)
    } yield ()
  }
  val presentProofExchanges = {
    for {
      presentationService <- ZIO.service[PresentationService]
      records <- presentationService
        .getPresentationRecords()
        .mapError(err => Throwable(s"Error occured while getting issue credential records: $err"))
      _ <- ZIO.foreach(records)(performPresentation)
    } yield ()
  }

  private[this] def performExchange(
      record: IssueCredentialRecord
  ): URIO[DidComm & CredentialService, Unit] = {
    import IssueCredentialRecord._
    import IssueCredentialRecord.ProtocolState._
    import IssueCredentialRecord.PublicationState._
    val aux = for {
      _ <- ZIO.logDebug(s"Running action with records => $record")
      _ <- record match {
        // Offer should be sent from Issuer to Holder
        case IssueCredentialRecord(id, _, _, _, _, Role.Issuer, _, _, _, _, OfferPending, _, Some(offer), _, _, _) =>
          (for {
            _ <- ZIO.log(s"IssueCredentialRecord: OfferPending (START)")
            didComm <- ZIO.service[DidComm]
            _ <- sendMessage(offer.makeMessage)
            credentialService <- ZIO.service[CredentialService]
            _ <- credentialService.markOfferSent(id)
          } yield ()): ZIO[DidComm & CredentialService, CredentialServiceError | MercuryException, Unit]

        // Request should be sent from Holder to Issuer
        case IssueCredentialRecord(
              id,
              _,
              _,
              _,
              _,
              Role.Holder,
              _,
              _,
              _,
              _,
              RequestPending,
              _,
              _,
              Some(request),
              _,
              _,
            ) =>
          (for {
            _ <- sendMessage(request.makeMessage)
            credentialService <- ZIO.service[CredentialService]
            _ <- credentialService.markRequestSent(id)
          } yield ()): ZIO[DidComm & CredentialService, CredentialServiceError | MercuryException, Unit]

        // 'automaticIssuance' is TRUE. Issuer automatically accepts the Request
        case IssueCredentialRecord(id, _, _, _, _, Role.Issuer, _, _, Some(true), _, RequestReceived, _, _, _, _, _) =>
          for {
            credentialService <- ZIO.service[CredentialService]
            _ <- credentialService.acceptCredentialRequest(id)
          } yield ()

        // Credential is pending, can be generated by Issuer and optionally published on-chain
        case IssueCredentialRecord(
              id,
              _,
              _,
              _,
              _,
              Role.Issuer,
              _,
              _,
              _,
              Some(awaitConfirmation),
              CredentialPending,
              _,
              _,
              _,
              Some(issue),
              _,
            ) =>
          // Generate the JWT Credential and store it in DB as an attacment to IssueCredentialData
          // Set ProtocolState to CredentialGenerated
          // Set PublicationState to PublicationPending
          for {
            credentialService <- ZIO.service[CredentialService]
            issuer = credentialService.createIssuer
            w3Credential <- credentialService.createCredentialPayloadFromRecord(
              record,
              issuer,
              Instant.now()
            )
            signedJwtCredential = JwtCredential.toEncodedJwt(w3Credential, issuer)
            issueCredential = IssueCredential.build(
              fromDID = issue.from,
              toDID = issue.to,
              thid = issue.thid,
              credentials = Map("prims/jwt" -> signedJwtCredential.value)
            )
            _ <- credentialService.markCredentialGenerated(id, issueCredential)

          } yield ()

        // Credential has been generated and can be sent directly to the Holder
        case IssueCredentialRecord(
              id,
              _,
              _,
              _,
              _,
              Role.Issuer,
              _,
              _,
              _,
              Some(false),
              CredentialGenerated,
              None,
              _,
              _,
              Some(issue),
              _,
            ) =>
          (for {
            _ <- sendMessage(issue.makeMessage)
            credentialService <- ZIO.service[CredentialService]
            _ <- credentialService.markCredentialSent(id)
          } yield ()): ZIO[DidComm & CredentialService, CredentialServiceError | MercuryException, Unit]

        // Credential has been generated, published, and can now be sent to the Holder
        case IssueCredentialRecord(
              id,
              _,
              _,
              _,
              _,
              Role.Issuer,
              _,
              _,
              _,
              Some(true),
              CredentialGenerated,
              Some(Published),
              _,
              _,
              Some(issue),
              _,
            ) =>
          (for {
            _ <- sendMessage(issue.makeMessage)
            credentialService <- ZIO.service[CredentialService]
            _ <- credentialService.markCredentialSent(id)
          } yield ()): ZIO[DidComm & CredentialService, CredentialServiceError | MercuryException, Unit]

        case IssueCredentialRecord(id, _, _, _, _, _, _, _, _, _, ProblemReportPending, _, _, _, _, _) => ???
        case IssueCredentialRecord(id, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _)                    => ZIO.unit
      }
    } yield ()

    aux
      .catchAll {
        case ex: MercuryException =>
          ZIO.logErrorCause(s"DIDComm communication error processing record: ${record.id}", Cause.fail(ex))
        case ex: CredentialServiceError =>
          ZIO.logErrorCause(s"Credential service error processing record: ${record.id} ", Cause.fail(ex))
      }
      .catchAllDefect { case throwable =>
        ZIO.logErrorCause(s"Issue Credential protocol defect processing record: ${record.id}", Cause.fail(throwable))
      }
  }

  private[this] def performPresentation(
      record: PresentationRecord
  ): ZIO[DidComm & PresentationService, Throwable, Unit] = {
    import io.iohk.atala.pollux.core.model.PresentationRecord.ProtocolState._

    val aux: ZIO[DidComm & PresentationService, MercuryException | InvalidState | NotImplemented.type, Unit] = for {
      _ <- ZIO.logDebug(s"Running action with records => $record")
      _ <- record match {
        // ##########################
        // ### PresentationRecord ###
        // ##########################
        case PresentationRecord(id, _, _, _, _, _, _, _, ProposalPending, _, _, _)  => ZIO.fail(NotImplemented)
        case PresentationRecord(id, _, _, _, _, _, _, _, ProposalSent, _, _, _)     => ZIO.fail(NotImplemented)
        case PresentationRecord(id, _, _, _, _, _, _, _, ProposalReceived, _, _, _) => ZIO.fail(NotImplemented)
        case PresentationRecord(id, _, _, _, _, _, _, _, ProposalRejected, _, _, _) => ZIO.fail(NotImplemented)

        case PresentationRecord(id, _, _, _, _, _, _, _, RequestPending, oRecord, _, _) => // Verifier
          oRecord match
            case None => ZIO.fail(InvalidState("PresentationRecord 'RequestPending' with no Record"))
            case Some(record) =>
              for {
                _ <- ZIO.log(s"PresentationRecord: RequestPending (Send Massage)")
                didComm <- ZIO.service[DidComm]
                _ <- sendMessage(record.makeMessage)
                service <- ZIO.service[PresentationService]
                _ <- service.markRequestPresentationSent(id).catchAll { case ex: PresentationError =>
                  ZIO.logError(s"Fail to mark the RequestPresentation '$id' as Verifier: $ex") *>
                    ZIO.unit
                }
              } yield ()

        case PresentationRecord(id, _, _, _, _, _, _, _, RequestSent, _, _, _) => // Verifier
          ZIO.logDebug("PresentationRecord: RequestSent") *> ZIO.unit
        case PresentationRecord(id, _, _, _, _, _, _, _, RequestReceived, _, _, _) => // Prover
          ZIO.logDebug("PresentationRecord: RequestReceived") *> ZIO.unit
        case PresentationRecord(id, _, _, _, _, _, _, _, RequestRejected, _, _, _) => // Prover
          ZIO.logDebug("PresentationRecord: RequestRejected") *> ZIO.unit
        case PresentationRecord(id, _, _, _, _, _, _, _, ProblemReportPending, _, _, _)  => ZIO.fail(NotImplemented)
        case PresentationRecord(id, _, _, _, _, _, _, _, ProblemReportSent, _, _, _)     => ZIO.fail(NotImplemented)
        case PresentationRecord(id, _, _, _, _, _, _, _, ProblemReportReceived, _, _, _) => ZIO.fail(NotImplemented)
        case PresentationRecord(id, _, _, _, _, _, _, _, PresentationPending, _, _, presentation) => // Prover
          presentation match
            case None => ZIO.fail(InvalidState("PresentationRecord in 'PresentationPending' with no Presentation"))
            case Some(p) =>
              for {
                _ <- ZIO.log(s"PresentationRecord: PresentationPending (Send Massage)")
                didComm <- ZIO.service[DidComm]
                _ <- sendMessage(p.makeMessage)
                service <- ZIO.service[PresentationService]
                _ <- service.markPresentationSent(id).catchAll { case ex: PresentationError =>
                  ZIO.logError(s"Fail to mark the PresentationSent '$id' as Prover: $ex") *>
                    ZIO.unit
                }
              } yield ()
        case PresentationRecord(id, _, _, _, _, _, _, _, PresentationSent, _, _, _) =>
          ZIO.logDebug("PresentationRecord: PresentationSent") *> ZIO.unit
        case PresentationRecord(id, _, _, _, _, _, _, _, PresentationReceived, _, _, _) =>
          ZIO.logDebug("PresentationRecord: PresentationReceived") *> ZIO.unit
          for {
            _ <- ZIO.log(s"PresentationRecord: PresentationPending (Send Massage)")
            // TODO Verify  https://input-output.atlassian.net/browse/ATL-2702
            service <- ZIO.service[PresentationService]
            _ <- service.markPresentationVerified(id).catchAll { case ex: PresentationError =>
              ZIO.logError(s"Fail to mark the PresentationVerified '$id' as Prover: $ex") *>
                ZIO.unit
            }
          } yield ()
        // TODO move the state to PresentationVerified
        case PresentationRecord(id, _, _, _, _, _, _, _, PresentationVerified, _, _, _) =>
          ZIO.logDebug("PresentationRecord: PresentationVerified") *> ZIO.unit
        case PresentationRecord(id, _, _, _, _, _, _, _, PresentationAccepted, _, _, _) =>
          ZIO.logDebug("PresentationRecord: PresentationVerifiedAccepted") *> ZIO.unit
        case PresentationRecord(id, _, _, _, _, _, _, _, PresentationRejected, _, _, _) =>
          ZIO.logDebug("PresentationRecord: PresentationRejected") *> ZIO.unit
      }
    } yield ()

    aux.catchAll {
      case ex: TransportError => // : io.iohk.atala.mercury.model.error.MercuryError | java.io.IOException =>
        ZIO.logError(ex.getMessage()) *>
          ZIO.fail(mercuryErrorAsThrowable(ex))
      case ex: IOException         => ZIO.fail(ex)
      case ex: InvalidState        => ZIO.fail(ex)
      case ex: NotImplemented.type => ZIO.fail(ex)
    }
  }

  val publishCredentialsToDlt = {
    for {
      credentialService <- ZIO.service[CredentialService]
      _ <- performPublishCredentialsToDlt(credentialService)
    } yield ()

  }

  private[this] def performPublishCredentialsToDlt(credentialService: CredentialService) = {
    val res: ZIO[Any, CredentialServiceError, Unit] = for {
      records <- credentialService.getCredentialRecordsByState(IssueCredentialRecord.ProtocolState.CredentialPending)
      // NOTE: the line below is a potentially slow operation, because <createCredentialPayloadFromRecord> makes a database SELECT call,
      // so calling this function n times will make n database SELECT calls, while it can be optimized to get
      // all data in one query, this function here has to be refactored as well. Consider doing this if this job is too slow
      credentials <- ZIO.foreach(records) { record =>
        credentialService.createCredentialPayloadFromRecord(record, credentialService.createIssuer, Instant.now())
      }
      // FIXME: issuer here should come from castor not from credential service, this needs to be done before going to prod
      publishedBatchData <- credentialService.publishCredentialBatch(credentials, credentialService.createIssuer)
      _ <- credentialService.markCredentialRecordsAsPublishQueued(publishedBatchData.credentialsAnsProofs)
      // publishedBatchData gives back irisOperationId, which should be persisted to track the status
    } yield ()

    ZIO.unit
  }

}
