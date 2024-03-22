package io.iohk.atala.iam.oidc

import io.iohk.atala.LogUtils.*
import io.iohk.atala.agent.walletapi.model.BaseEntity
import io.iohk.atala.api.http.{ErrorResponse, RequestContext}
import io.iohk.atala.iam.authentication.{Authenticator, Authorizer, DefaultAuthenticator, SecurityLogic}
import io.iohk.atala.iam.oidc.controller.CredentialIssuerController
import io.iohk.atala.iam.oidc.http.{CredentialErrorResponse, CredentialRequest, NonceResponse}
import sttp.tapir.ztapir.*
import zio.*

case class CredentialIssuerServerEndpoints(
    credentialIssuerController: CredentialIssuerController,
    authenticator: Authenticator[BaseEntity],
    authorizer: Authorizer[BaseEntity],
) {
  val credentialServerEndpoint: ZServerEndpoint[Any, Any] =
    CredentialIssuerEndpoints.credentialEndpoint
      .zServerSecurityLogic(
        SecurityLogic // TODO: add OIDC client authenticator
          .authenticate(_)(authenticator)
          .mapError(Left[ErrorResponse, CredentialErrorResponse])
      )
      .serverLogic { wac =>
        { case (rc: RequestContext, didRef: String, request: CredentialRequest) =>
          ZIO.succeed(request).debug("credentialRequest") *>
            credentialIssuerController
              .issueCredential(rc, didRef, request)
              .logTrace(rc)
        }
      }

  val createCredentialOfferServerEndpoint: ZServerEndpoint[Any, Any] =
    CredentialIssuerEndpoints.createCredentialOfferEndpoint
      .zServerSecurityLogic(SecurityLogic.authorizeWalletAccessWith(_)(authenticator, authorizer))
      .serverLogic { wac =>
        { case (rc, didRef, request) =>
          credentialIssuerController
            .createCredentialOffer(rc, didRef, request)
            .provideSomeLayer(ZLayer.succeed(wac))
            .logTrace(rc)
        }
      }

  val nonceServerEndpoint: ZServerEndpoint[Any, Any] =
    CredentialIssuerEndpoints.nonceEndpoint
      .zServerSecurityLogic(
        // FIXME: how can authorization server authorize itself?
        SecurityLogic.authorizeWalletAccessWith(_)(authenticator, authorizer)
      )
      .serverLogic { wac =>
        { case (rc, didRef, request) =>
          credentialIssuerController
            .getNonce(rc, didRef, request)
            .provideSomeLayer(ZLayer.succeed(wac))
            .logTrace(rc)
        }
      }

  val issuerMetadataEndpoint: ZServerEndpoint[Any, Any] = CredentialIssuerEndpoints.issuerMetadataEndpoint
    .zServerLogic {
      { case (rc, didRef) => credentialIssuerController.getIssuerMetadata(rc, didRef).logTrace(rc) }
    }

  val all: List[ZServerEndpoint[Any, Any]] = List(
    credentialServerEndpoint,
    nonceServerEndpoint,
    createCredentialOfferServerEndpoint,
    issuerMetadataEndpoint
  )
}

object CredentialIssuerServerEndpoints {
  def all: URIO[DefaultAuthenticator & CredentialIssuerController, List[ZServerEndpoint[Any, Any]]] = {
    for {
      authenticator <- ZIO.service[DefaultAuthenticator]
      credentialIssuerController <- ZIO.service[CredentialIssuerController]
      oidcEndpoints = CredentialIssuerServerEndpoints(credentialIssuerController, authenticator, authenticator)
    } yield oidcEndpoints.all
  }
}
