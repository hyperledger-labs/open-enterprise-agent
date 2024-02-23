package io.iohk.atala.mercury.protocol.presentproof

import io.iohk.atala.mercury.model.{AttachmentDescriptor, DidId}
import io.iohk.atala.mercury.protocol.invitation.v2.Invitation
import io.iohk.atala.mercury.protocol.invitation.v2.Invitation.Body

object PresentProofInvitation {
  def makeInvitation(
      from: DidId,
      goalCode: Option[String],
      goal: Option[String],
      invitationId: String,
      requestPresentation: RequestPresentation
  ): Invitation = {
    val attachmentDescriptor = AttachmentDescriptor.buildJsonAttachment(payload = requestPresentation)
    Invitation(
      id = invitationId,
      from = from,
      body = Invitation.Body(
        goal_code = goalCode,
        goal = goal,
        Nil
      ),
      attachments = Some(Seq(attachmentDescriptor))
    )
  }

}
