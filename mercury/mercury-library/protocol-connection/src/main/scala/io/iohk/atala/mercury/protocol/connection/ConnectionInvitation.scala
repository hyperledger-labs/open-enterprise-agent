package io.iohk.atala.mercury.protocol.connection

import io.iohk.atala.mercury.model.{AttachmentDescriptor, DidId}
import io.iohk.atala.mercury.protocol.invitation.v2.Invitation
import io.iohk.atala.mercury.protocol.invitation.v2.Invitation.Body

object ConnectionInvitation {

  /** Make a invitation to establish a trust connection between two peers */
  def makeConnectionInvitation(from: DidId): Invitation = {
    makeConnectionInvitation(
      from = from,
      goalCode = Some("io.atalaprism.connect"),
      goal = Some(s"Establish a trust connection between two peers using the protocol '${ConnectionRequest.`type`}'")
    )
  }

  def makeConnectionInvitation(from: DidId, goalCode: Option[String], goal: Option[String]): Invitation = {
    Invitation(
      from = from,
      body = Invitation.Body(
        goal_code = goalCode,
        goal = goal,
        Nil
      )
    )
  }

}
