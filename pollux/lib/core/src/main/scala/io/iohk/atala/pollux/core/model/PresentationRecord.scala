package io.iohk.atala.pollux.core.model

import io.iohk.atala.mercury.protocol.presentproof.ProposePresentation
import io.iohk.atala.mercury.protocol.presentproof.RequestPresentation
import io.iohk.atala.mercury.protocol.presentproof.Presentation
import io.iohk.atala.mercury.model.DidId
import io.iohk.atala.mercury.protocol.invitation.v2.Invitation

import java.time.Instant
import java.time.temporal.ChronoUnit

final case class PresentationRecord(
    id: DidCommID,
    createdAt: Instant,
    updatedAt: Option[Instant],
    thid: DidCommID,
    schemaId: Option[String],
    connectionId: Option[String],
    role: PresentationRecord.Role,
    subjectId: DidId,
    protocolState: PresentationRecord.ProtocolState,
    credentialFormat: CredentialFormat,
    invitation: Option[Invitation],
    requestPresentationData: Option[RequestPresentation],
    proposePresentationData: Option[ProposePresentation],
    presentationData: Option[Presentation],
    credentialsToUse: Option[List[String]],
    metaRetries: Int,
    metaNextRetry: Option[Instant],
    metaLastFailure: Option[String],
) {
  def withTruncatedTimestamp(unit: ChronoUnit = ChronoUnit.MICROS): PresentationRecord =
    copy(
      createdAt = createdAt.truncatedTo(unit),
      updatedAt = updatedAt.map(_.truncatedTo(unit)),
      metaNextRetry = metaNextRetry.map(_.truncatedTo(unit)),
    )
}

object PresentationRecord {

  enum Role:
    case Verifier extends Role
    case Prover extends Role

  enum ProtocolState:
    // Prover has created a Proposal in a database, but it has not been sent yet (in Prover DB)
    case ProposalPending extends ProtocolState
    // Prover has sent an proposal to a Verifier (in Prover DB)
    case ProposalSent extends ProtocolState
    // Verifier has received a proposal (In Verifier DB)
    case ProposalReceived extends ProtocolState
    // Verifier has received a proposal and has rejected (In Verifier DB)
    case ProposalRejected extends ProtocolState // TODO start propose presentation

    // Verifier has created a OOB Presentation request  (in Verifier DB)
    case InvitationGenerated extends ProtocolState
    // Verifier has created a OOB Presentation request  Expired (in Verifier DB)
    // case InvitationExpired extends ProtocolState // TODO
    case RequestPending extends ProtocolState
    // Verifier has sent a request to a an Prover (in Verifier DB)
    case RequestSent extends ProtocolState
    // Prover has received a request from the Verifier (In Verifier DB)
    case RequestReceived extends ProtocolState
    // Prover has rejected a presentation request from the Verifier (In prover DB)
    case RequestRejected extends ProtocolState // TODO start propose presentation

    // Prover/Verifier declined the Presentation request/ Proposed Presenation  by Verifier/Prover (DB)
    case ProblemReportPending extends ProtocolState
    // Prover/Verifier has sent problem report to Verifier/Prover (Verifier/Prover DB)
    case ProblemReportSent extends ProtocolState
    // Prover/Verifier has received problem resport from Verifier/Prover (DB)
    case ProblemReportReceived extends ProtocolState

    // Prover has "accepted" a Presentation request received from a Verifier (Prover DB)
    case PresentationPending extends ProtocolState
    // Prover has generated (signed) the VC  and is now ready to send it to the Verifier (Prover DB)
    case PresentationGenerated extends ProtocolState
    // Prover has sent the Presentation (Prover DB)
    case PresentationSent extends ProtocolState
    // Verifier has received the presentation (Verifier DB)
    case PresentationReceived extends ProtocolState
    // Verifier has verified the presentation (proof) (Verifier DB)
    case PresentationVerified extends ProtocolState
    // Verifier has updated Verification failed in the presentation (proof) (Verifier DB)
    case PresentationVerificationFailed extends ProtocolState
    // Verifier has accepted the verified presentation (proof) (Verifier DB)
    case PresentationAccepted extends ProtocolState
    // Verifier has rejected the presentation (proof) (Verifier DB)
    case PresentationRejected extends ProtocolState // TODO send problem report

}
