package io.iohk.atala.presentproof.controller

import io.iohk.atala.LogUtils.*
import io.iohk.atala.agent.walletapi.model.BaseEntity
import io.iohk.atala.api.http.RequestContext
import io.iohk.atala.api.http.model.PaginationInput
import io.iohk.atala.iam.authentication.Authenticator
import io.iohk.atala.iam.authentication.Authorizer
import io.iohk.atala.iam.authentication.DefaultAuthenticator
import io.iohk.atala.iam.authentication.SecurityLogic
import io.iohk.atala.presentproof.controller.PresentProofEndpoints.{
  getAllPresentations,
  getPresentation,
  requestPresentation,
  oobRequestPresentation,
  acceptRequestPresentationInvitation,
  updatePresentation
}
import io.iohk.atala.presentproof.controller.http.{
  RequestPresentationAction,
  RequestPresentationInput,
  OOBRequestPresentation,
  AcceptRequestPresentationInvitationRequest
}
import io.iohk.atala.shared.models.WalletAccessContext
import sttp.tapir.ztapir.*
import zio.*

import java.util.UUID

class PresentProofServerEndpoints(
    presentProofController: PresentProofController,
    authenticator: Authenticator[BaseEntity],
    authorizer: Authorizer[BaseEntity]
) {
  private val requestPresentationEndpoint: ZServerEndpoint[Any, Any] =
    requestPresentation
      .zServerSecurityLogic(SecurityLogic.authorizeWalletAccessWith(_)(authenticator, authorizer))
      .serverLogic { wac =>
        { case (ctx: RequestContext, request: RequestPresentationInput) =>
          presentProofController
            .requestPresentation(request)(ctx)
            .provideSomeLayer(ZLayer.succeed(wac))
            .logTrace(ctx)
        }
      }
  private val oobRequestPresentationEndpoint: ZServerEndpoint[Any, Any] =
    oobRequestPresentation
      .zServerSecurityLogic(SecurityLogic.authorizeWalletAccessWith(_)(authenticator, authorizer))
      .serverLogic { wac =>
        { case (ctx: RequestContext, request: OOBRequestPresentation) =>
          presentProofController
            .createOOBRequestPresentation(request)(ctx)
            .provideSomeLayer(ZLayer.succeed(wac))
        }
      }
  private val acceptRequestPresentationInvitationEndpoint: ZServerEndpoint[Any, Any] =
    acceptRequestPresentationInvitation
      .zServerSecurityLogic(SecurityLogic.authorizeWalletAccessWith(_)(authenticator, authorizer))
      .serverLogic { wac =>
        { case (ctx: RequestContext, request: AcceptRequestPresentationInvitationRequest) =>
          presentProofController
            .acceptRequestPresentationInvitation(request)(ctx)
            .provideSomeLayer(ZLayer.succeed(wac))
        }
      }
  private val getAllPresentationsEndpoint: ZServerEndpoint[Any, Any] =
    getAllPresentations
      .zServerSecurityLogic(SecurityLogic.authorizeWalletAccessWith(_)(authenticator, authorizer))
      .serverLogic { wac =>
        { case (ctx: RequestContext, paginationInput: PaginationInput, thid: Option[String]) =>
          presentProofController
            .getPresentations(paginationInput, thid)(ctx)
            .provideSomeLayer(ZLayer.succeed(wac))
            .logTrace(ctx)
        }
      }

  private val getPresentationEndpoint: ZServerEndpoint[Any, Any] =
    getPresentation
      .zServerSecurityLogic(SecurityLogic.authorizeWalletAccessWith(_)(authenticator, authorizer))
      .serverLogic { wac =>
        { case (ctx: RequestContext, presentationId: UUID) =>
          presentProofController
            .getPresentation(presentationId)(ctx)
            .provideSomeLayer(ZLayer.succeed(wac))
            .logTrace(ctx)
        }
      }

  private val updatePresentationEndpoint: ZServerEndpoint[Any, Any] =
    updatePresentation
      .zServerSecurityLogic(SecurityLogic.authorizeWalletAccessWith(_)(authenticator, authorizer))
      .serverLogic { wac =>
        { case (ctx: RequestContext, presentationId: UUID, action: RequestPresentationAction) =>
          presentProofController
            .updatePresentation(presentationId, action)(ctx)
            .provideSomeLayer(ZLayer.succeed(wac))
            .logTrace(ctx)
        }
      }

  val all: List[ZServerEndpoint[Any, Any]] = List(
    requestPresentationEndpoint,
    getAllPresentationsEndpoint,
    getPresentationEndpoint,
    updatePresentationEndpoint,
    oobRequestPresentationEndpoint,
    acceptRequestPresentationInvitationEndpoint
  )
}

object PresentProofServerEndpoints {
  def all: URIO[PresentProofController & DefaultAuthenticator, List[ZServerEndpoint[Any, Any]]] = {
    for {
      authenticator <- ZIO.service[DefaultAuthenticator]
      presentProofController <- ZIO.service[PresentProofController]
      presentProofEndpoints = new PresentProofServerEndpoints(presentProofController, authenticator, authenticator)
    } yield presentProofEndpoints.all
  }
}
