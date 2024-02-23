package io.iohk.atala.presentproof.controller.http

import io.iohk.atala.api.http.Annotation
import io.iohk.atala.presentproof.controller.http.OOBPresentation.annotations
import io.iohk.atala.presentproof
import sttp.model.Uri
import sttp.tapir.Schema.annotations.{description, encodedExample, validate}
import sttp.tapir.{Schema, Validator}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
import io.iohk.atala.pollux.core.model.PresentationRecord
import io.iohk.atala.pollux.core.model.PresentationRecord.Role
import java.time.{OffsetDateTime, ZoneOffset}
import java.util.UUID

case class OOBPresentation(
    @description(annotations.presentationId.description)
    @encodedExample(annotations.presentationId.example)
    presentationId: UUID,
    @description(annotations.thid.description)
    @encodedExample(annotations.thid.example)
    thid: String,
    @description(annotations.goalcode.description)
    @encodedExample(annotations.goalcode.example)
    goalCode: Option[String] = None,
    @description(annotations.goal.description)
    @encodedExample(annotations.goal.example)
    goal: Option[String] = None,
    @description(annotations.myDid.description)
    @encodedExample(annotations.myDid.example)
    myDid: Option[String] = None,
    @description(annotations.role.description)
    @encodedExample(annotations.role.example)
    @validate(annotations.role.validator)
    role: String,
    @description(annotations.state.description)
    @encodedExample(annotations.state.example)
    @validate(annotations.state.validator)
    state: String,
    @description(annotations.invitation.description)
    // @encodedExample(annotations.invitation.example) // FIXME: tapir incorrectly render this example
    invitation: OOBPresentationInvitation,
    @description(annotations.createdAt.description)
    @encodedExample(annotations.createdAt.example)
    createdAt: OffsetDateTime,
    @description(annotations.updatedAt.description)
    @encodedExample(annotations.updatedAt.example)
    updatedAt: Option[OffsetDateTime] = None,
    @description(annotations.metaRetries.description)
    @encodedExample(annotations.metaRetries.example)
    metaRetries: Int,
    @description(annotations.self.description)
    @encodedExample(annotations.self.example)
    self: String = "",
    @description(annotations.kind.description)
    @encodedExample(annotations.kind.example)
    kind: String = "Presentation"
) {
  def withBaseUri(base: Uri): OOBPresentation = withSelf(base.addPath(presentationId.toString).toString)
  def withSelf(self: String): OOBPresentation = copy(self = self)
}

object OOBPresentation {

  def fromDomain(domain: PresentationRecord): OOBPresentation =
    OOBPresentation(
      presentationId = UUID.fromString(domain.id.value),
      thid = domain.thid.value,
      goalCode = domain.invitation.flatMap(_.body.goal_code),
      goal = domain.invitation.flatMap(_.body.goal),
      myDid = domain.role match
        case Role.Verifier =>
          domain.requestPresentationData.flatMap(_.from.map(_.value))
        case Role.Prover => // TODO This in not required/ possible
          domain.presentationData.map(_.from).map(_.value)
      ,
      role = domain.role.toString,
      state = domain.protocolState.toString,
      invitation = OOBPresentationInvitation.fromDomain(domain.invitation),
      createdAt = domain.createdAt.atOffset(ZoneOffset.UTC),
      updatedAt = domain.updatedAt.map(_.atOffset(ZoneOffset.UTC)),
      metaRetries = domain.metaRetries,
      self = domain.id.value,
      kind = "Presentation",
    )

  given Conversion[PresentationRecord, OOBPresentation] = fromDomain

  object annotations {
    object presentationId
        extends Annotation[UUID](
          description = "The unique identifier of the presentation.",
          example = UUID.fromString("0527aea1-d131-3948-a34d-03af39aba8b4")
        )

    object thid
        extends Annotation[String](
          description = "The unique identifier of the thread this connection record belongs to. " +
            "The value will identical on both sides of the connection (inviter and invitee)",
          example = "0527aea1-d131-3948-a34d-03af39aba8b4"
        )

    object goalcode
        extends Annotation[String](
          description =
            "A self-attested code the receiver may want to display to the user or use in automatically deciding what to do with the out-of-band message.",
          example = "issue-vc"
        )
    object goal
        extends Annotation[String](
          description =
            "A self-attested string that the receiver may want to display to the user about the context-specific goal of the out-of-band message.",
          example = "To issue a Peter College Graduate credential"
        )
    object myDid
        extends Annotation[String](
          description = "The DID representing me as the inviter or invitee in this specific connection.",
          example = "did:peer:12345"
        )

    object role
        extends Annotation[String](
          description = "The role played by the Prism agent in the Verification flow.",
          example = "Verifier",
          validator = Validator.enumeration(
            List(
              "Verifier",
              "Prover"
            )
          )
        )

    object state
        extends Annotation[String](
          description = "The current state of the presentproof protocol execution.",
          example = "InvitationGenerated",
          validator = Validator.enumeration(
            List(
              "InvitationGenerated",
              "InvitationReceived",
              "ConnectionRequestPending",
              "ConnectionRequestSent",
              "ConnectionRequestReceived",
              "ConnectionResponsePending",
              "ConnectionResponseSent",
              "ConnectionResponseReceived",
              "ProblemReportPending",
              "ProblemReportSent",
              "ProblemReportReceived"
            )
          )
        )

    object invitation
        extends Annotation[OOBPresentationInvitation](
          description = "The invitation for this Request Presentation",
          example = OOBPresentationInvitation.Example
        )

    object createdAt
        extends Annotation[OffsetDateTime](
          description = "The date and time the Presentation record was created.",
          example = OffsetDateTime.parse("2022-03-10T12:00:00Z")
        )

    object updatedAt
        extends Annotation[OffsetDateTime](
          description = "The date and time the Presentation record was last updated.",
          example = OffsetDateTime.parse("2022-03-10T12:00:00Z")
        )

    object metaRetries
        extends Annotation[Int](
          description = "The maximum background processing attempts remaining for this record",
          example = 5
        )

    object self
        extends Annotation[String](
          description = "The reference to the Presentation resource.",
          example = "https://atala-prism-products.io/connections/ABCD-1234"
        )

    object kind
        extends Annotation[String](
          description = "The type of object returned. In this case a `Presentation`.",
          example = "Presentation"
        )
  }

  given encoder: JsonEncoder[OOBPresentation] =
    DeriveJsonEncoder.gen[OOBPresentation]

  given decoder: JsonDecoder[OOBPresentation] =
    DeriveJsonDecoder.gen[OOBPresentation]

  given schema: Schema[OOBPresentation] = Schema.derived

}
