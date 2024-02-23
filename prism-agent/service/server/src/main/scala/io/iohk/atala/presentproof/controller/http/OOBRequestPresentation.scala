package io.iohk.atala.presentproof.controller.http

import io.iohk.atala.api.http.Annotation
import io.iohk.atala.presentproof.controller.http.OOBRequestPresentation.annotations
import sttp.tapir.Schema.annotations.{description, encodedExample}
import sttp.tapir.{Schema, Validator}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import java.util.UUID

final case class OOBRequestPresentation(
    @description(annotations.label.description)
    @encodedExample(annotations.label.example)
    label: Option[String] = None,
    @description(annotations.goalcode.description)
    @encodedExample(annotations.goalcode.example)
    goalCode: Option[String] = None,
    @description(annotations.goal.description)
    @encodedExample(annotations.goal.example)
    goal: Option[String] = None,
    @description(annotations.options.description)
    @encodedExample(annotations.options.example)
    options: Option[Options] = None,
    @description(annotations.proofs.description)
    @encodedExample(annotations.proofs.example)
    proofs: Seq[ProofRequestAux],
    @description(annotations.credentialFormat.description)
    @encodedExample(annotations.credentialFormat.example)
    credentialFormat: Option[String],
)

object OOBRequestPresentation {
  object annotations {
    object label
        extends Annotation[String](
          description = "A human readable alias for the connection.",
          example = "Peter"
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

    object connectionId
        extends Annotation[UUID](
          description = "The unique identifier of an established connection between the verifier and the prover.",
          example = UUID.fromString("bc528dc8-69f1-4c5a-a508-5f8019047900")
        )
    object options
        extends Annotation[Option[Options]](
          description = "The options to use when creating the proof presentation request (e.g., domain, challenge).",
          example = None
        )
    object proofs
        extends Annotation[Seq[ProofRequestAux]](
          description =
            "The type of proofs requested in the context of this proof presentation request (e.g., VC schema, trusted issuers, etc.)",
          example = Seq.empty
        )

    object credentialFormat
        extends Annotation[Option[String]](
          description = "The credential format (default to 'JWT')",
          example = Some("JWT"),
          validator = Validator.enumeration(
            List(
              Some("JWT"),
              Some("AnonCreds")
            )
          )
        )
  }

  given encoder: JsonEncoder[OOBRequestPresentation] =
    DeriveJsonEncoder.gen[OOBRequestPresentation]

  given decoder: JsonDecoder[OOBRequestPresentation] =
    DeriveJsonDecoder.gen[OOBRequestPresentation]

  given schema: Schema[OOBRequestPresentation] = Schema.derived
}
