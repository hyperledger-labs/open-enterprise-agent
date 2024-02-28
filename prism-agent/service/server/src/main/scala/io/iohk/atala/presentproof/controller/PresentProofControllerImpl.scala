package io.iohk.atala.presentproof.controller

import io.iohk.atala.agent.server.ControllerHelper
import io.iohk.atala.api.http.model.PaginationInput
import io.iohk.atala.api.http.{ErrorResponse, RequestContext}
import io.iohk.atala.connect.controller.ConnectionController
import io.iohk.atala.connect.core.model.error.ConnectionServiceError
import io.iohk.atala.connect.core.service.ConnectionService
import io.iohk.atala.mercury.model.DidId
import io.iohk.atala.mercury.protocol.presentproof.ProofType
import io.iohk.atala.pollux.core.model.error.PresentationError
import io.iohk.atala.pollux.core.model.presentation.Options
import io.iohk.atala.pollux.core.model.{CredentialFormat, DidCommID, PresentationRecord}
import io.iohk.atala.pollux.core.service.{PresentationService}
import io.iohk.atala.presentproof.controller.PresentProofController.toDidCommID
import io.iohk.atala.presentproof.controller.http.*
import io.iohk.atala.shared.models.WalletAccessContext
import zio.{URLayer, ZIO, ZLayer}

import java.util.UUID

class PresentProofControllerImpl(
    presentationService: PresentationService,
    connectionService: ConnectionService
) extends PresentProofController
    with ControllerHelper {
  override def requestPresentation(request: RequestPresentationInput)(implicit
      rc: RequestContext
  ): ZIO[WalletAccessContext, ErrorResponse, PresentationStatus] = {
    val result: ZIO[WalletAccessContext, ConnectionServiceError | PresentationError, PresentationStatus] = for {
      didIdPair <- getPairwiseDIDs(request.connectionId).provideSomeLayer(ZLayer.succeed(connectionService))
      credentialFormat = request.credentialFormat.map(CredentialFormat.valueOf).getOrElse(CredentialFormat.JWT)
      record <-
        credentialFormat match {
          case CredentialFormat.JWT =>
            presentationService
              .createJwtPresentationRecord(
                pairwiseVerifierDID = didIdPair.myDID,
                pairwiseProverDID = didIdPair.theirDid,
                thid = DidCommID(),
                connectionId = Some(request.connectionId.toString),
                proofTypes = request.proofs.map { e =>
                  ProofType(
                    schema = e.schemaId,
                    requiredFields = None,
                    trustIssuers = Some(e.trustIssuers.map(DidId(_)))
                  )
                },
                options = request.options.map(x => Options(x.challenge, x.domain))
              )
          case CredentialFormat.AnonCreds =>
            request.anoncredPresentationRequest match {
              case Some(presentationRequest) =>
                presentationService
                  .createAnoncredPresentationRecord(
                    pairwiseVerifierDID = didIdPair.myDID,
                    pairwiseProverDID = didIdPair.theirDid,
                    thid = DidCommID(),
                    connectionId = Some(request.connectionId.toString),
                    presentationRequest = presentationRequest
                  )
              case None =>
                ZIO.fail(
                  PresentationError.MissingAnoncredPresentationRequest("Anoncred presentation request is missing")
                )
            }
        }
    } yield PresentationStatus.fromDomain(record)

    result.mapError {
      case e: ConnectionServiceError => ConnectionController.toHttpError(e)
      case e: PresentationError      => PresentProofController.toHttpError(e)
    }
  }

  override def getPresentations(paginationInput: PaginationInput, thid: Option[String])(implicit
      rc: RequestContext
  ): ZIO[WalletAccessContext, ErrorResponse, PresentationStatusPage] = {
    val result = for {
      records <- thid match
        case None       => presentationService.getPresentationRecords(ignoreWithZeroRetries = false)
        case Some(thid) => presentationService.getPresentationRecordByThreadId(DidCommID(thid)).map(_.toSeq)
    } yield PresentationStatusPage(
      records.map(PresentationStatus.fromDomain)
    )

    result.mapError(PresentProofController.toHttpError)
  }

  override def getPresentation(
      id: UUID
  )(implicit rc: RequestContext): ZIO[WalletAccessContext, ErrorResponse, PresentationStatus] = {
    val result: ZIO[WalletAccessContext, ErrorResponse | PresentationError, PresentationStatus] = for {
      presentationId <- toDidCommID(id.toString)
      maybeRecord <- presentationService.getPresentationRecord(presentationId)
      record <- ZIO
        .fromOption(maybeRecord)
        .mapError(_ => ErrorResponse.notFound(detail = Some(s"Presentation record not found: $id")))
    } yield PresentationStatus.fromDomain(record)

    result.mapError {
      case e: ErrorResponse     => e
      case e: PresentationError => PresentProofController.toHttpError(e)
    }
  }

  override def updatePresentation(id: UUID, requestPresentationAction: RequestPresentationAction)(implicit
      rc: RequestContext
  ): ZIO[WalletAccessContext, ErrorResponse, PresentationStatus] = {
    val result: ZIO[WalletAccessContext, ErrorResponse | PresentationError, PresentationStatus] = for {
      didCommId <- ZIO.succeed(DidCommID(id.toString))
      record <- requestPresentationAction.action match {
        case "request-accept" =>
          (requestPresentationAction.proofId, requestPresentationAction.anoncredPresentationRequest) match
            case (Some(proofs), None) =>
              presentationService.acceptRequestPresentation(recordId = didCommId, credentialsToUse = proofs)
            case (None, Some(proofs)) =>
              presentationService.acceptAnoncredRequestPresentation(
                recordId = didCommId,
                credentialsToUse = proofs
              )
            case _ => presentationService.acceptRequestPresentation(recordId = didCommId, credentialsToUse = Seq())

        case "request-reject"      => presentationService.rejectRequestPresentation(didCommId)
        case "presentation-accept" => presentationService.acceptPresentation(didCommId)
        case "presentation-reject" => presentationService.rejectPresentation(didCommId)
        case a =>
          ZIO.fail(
            ErrorResponse.badRequest(
              detail = Some(
                s"presentation action must be 'request-accept','request-reject', 'presentation-accept', or 'presentation-reject' but is $a"
              )
            )
          )
      }
    } yield PresentationStatus.fromDomain(record)

    result.mapError {
      case e: ErrorResponse     => e
      case e: PresentationError => PresentProofController.toHttpError(e)
    }
  }
}

object PresentProofControllerImpl {
  val layer: URLayer[PresentationService & ConnectionService, PresentProofController] =
    ZLayer.fromFunction(PresentProofControllerImpl(_, _))
}
