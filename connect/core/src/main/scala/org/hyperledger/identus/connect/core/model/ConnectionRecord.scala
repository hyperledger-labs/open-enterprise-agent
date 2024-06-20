package org.hyperledger.identus.connect.core.model

import org.hyperledger.identus.connect.core.model.ConnectionRecord.{ProtocolState, Role}
import org.hyperledger.identus.mercury.protocol.connection.{ConnectionRequest, ConnectionResponse}
import org.hyperledger.identus.mercury.protocol.invitation.v2.Invitation

import java.time.temporal.ChronoUnit
import java.time.Instant
import java.util.UUID

/** @param id
  * @param createdAt
  * @param updatedAt
  * @param thid
  * @param label
  * @param goalCode
  * @param goal
  * @param role
  * @param protocolState
  * @param invitation
  * @param connectionRequest
  * @param connectionResponse
  * @param metaRetries
  *   represents the number of tries that this state can be processed. If the retries is 0 mean there is no more tries
  *   available and the State MUST be considered unrecoverable.
  * @param metaLastFailure
  *   if present contains information about the last failure. TODO this information should be moved to some metric
  *   service.
  */
case class ConnectionRecord(
    id: UUID,
    createdAt: Instant,
    updatedAt: Option[Instant],
    thid: String,
    label: Option[String],
    goalCode: Option[String],
    goal: Option[String],
    role: Role,
    protocolState: ProtocolState,
    invitation: Invitation,
    connectionRequest: Option[ConnectionRequest],
    connectionResponse: Option[ConnectionResponse],
    metaRetries: Int,
    metaNextRetry: Option[Instant],
    metaLastFailure: Option[org.hyperledger.identus.shared.models.Failure]
) {
  def withTruncatedTimestamp(unit: ChronoUnit = ChronoUnit.MICROS): ConnectionRecord = copy(
    createdAt = createdAt.truncatedTo(unit),
    updatedAt = updatedAt.map(_.truncatedTo(unit)),
    metaNextRetry = metaNextRetry.map(_.truncatedTo(unit))
  )
}

object ConnectionRecord {
  enum Role:
    case Inviter extends Role
    case Invitee extends Role

  enum ProtocolState:
    // Inviter has created an Invitation in a database
    case InvitationGenerated extends ProtocolState

    // Invitee has received an offer (In Holder DB)
    case InvitationReceived extends ProtocolState

    // Inviter Invitation is expired in Invitee DB
    case InvitationExpired extends ProtocolState

    //  Invitee has created a Connection Request to Inviter
    case ConnectionRequestPending extends ProtocolState

    // Invitee has sent a request to Inviter
    case ConnectionRequestSent extends ProtocolState

    // Inviter has received a request from Invitee
    case ConnectionRequestReceived extends ProtocolState

    // Inviter has created a Connection response for Invitee
    case ConnectionResponsePending extends ProtocolState

    // Inviter has sent Connection response for Invitee
    case ConnectionResponseSent extends ProtocolState

    // Invitee has received a ConnectionResponse from Inviter
    case ConnectionResponseReceived extends ProtocolState

    // TODO Ack to the connection response to finalise connection

    // TODO DID Rotation later

    // Holder declined the offer sent by Issuer (Holder DB) or Issuer declined the proposal sent by Holder (Issuer DB)
    case ProblemReportPending extends ProtocolState
    // Holder has sent problem report to Issuer (Holder DB) or Issuer has sent problem report to Holder (Issuer DB)
    case ProblemReportSent extends ProtocolState
    // Holder has received problem resport from Issuer (Holder DB) or Issuer has received problem report from Holder (Issuer DB)
    case ProblemReportReceived extends ProtocolState

}
