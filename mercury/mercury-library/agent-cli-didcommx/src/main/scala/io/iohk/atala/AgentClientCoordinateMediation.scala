package io.iohk.atala

import io.iohk.atala.mercury.*
import zio.*

@main def AgentClientGetInvitation() = {
  val app =
    InvitationPrograms
      .getInvitationProgram("http://localhost:8000/oob_url")
      .provide(ZioHttpClient.layer, zio.http.Client.default)

  Unsafe.unsafe { implicit u => Runtime.default.unsafe.run(app).getOrThrowFiberFailure() }

}

// val env = zio.http.Client.default ++ zio.Scope.default

// @main def AgentClientCoordinateMediationWithRootsId() = {
//   val env = ChannelFactory.auto ++ EventLoopGroup.auto()
//   val mediatorURL = "http://localhost:8000"
//   val app = CoordinateMediationPrograms
//     .senderMediationRequestProgram(mediatorURL)
//     .provide(AgentService.charlie, HttpClientZhttp.layer)
//   Unsafe.unsafe { implicit u => Runtime.default.unsafe.run(app).getOrThrowFiberFailure() }
// }

// @main def AgentClientCoordinateMediation() = {
//   val env = ChannelFactory.auto ++ EventLoopGroup.auto()
//   val mediatorURL = "http://localhost:8080"
//   val app = CoordinateMediationPrograms
//     .senderMediationRequestProgram(mediatorURL)
//     .provide(AgentService.charlie, HttpClientZhttp.layer)
//   Unsafe.unsafe { implicit u => Runtime.default.unsafe.run(app).getOrThrowFiberFailure() }
// }
