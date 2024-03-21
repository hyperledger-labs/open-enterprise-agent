package io.iohk.atala.api.http

import io.iohk.atala.api.http.ErrorResponse.annotations
import sttp.model.StatusCode
import sttp.tapir.Schema
import sttp.tapir.Schema.annotations.{description, encodedExample}
import sttp.tapir.generic.auto.*
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

import java.util.UUID

private val INSTANCE_URI_PREFIX = "error:instance:"

case class ErrorResponse(
    @description(annotations.status.description)
    @encodedExample(annotations.status.example)
    status: Int,
    @description(annotations.`type`.description)
    @encodedExample(annotations.`type`.example)
    `type`: String,
    @description(annotations.title.description)
    @encodedExample(annotations.title.example)
    title: String,
    @description(annotations.detail.description)
    @encodedExample(annotations.detail.example)
    detail: Option[String] = None,
    @description(annotations.instance.description)
    @encodedExample(annotations.instance.example)
    instance: String = INSTANCE_URI_PREFIX + UUID.randomUUID().toString
)

object ErrorResponse {
  given encoder: zio.json.JsonEncoder[ErrorResponse] = DeriveJsonEncoder.gen[ErrorResponse]

  given decoder: zio.json.JsonDecoder[ErrorResponse] = DeriveJsonDecoder.gen[ErrorResponse]

  given schema: Schema[ErrorResponse] = Schema.derived

  object annotations {
    object status
        extends Annotation[Int](
          description = "The HTTP status code for this occurrence of the problem.",
          example = 200
        )
    object `type`
        extends Annotation[String](
          description = "A URI reference that identifies the problem type.",
          example = "https://example.org/doc/#model-MalformedEmail/"
        )

    object title
        extends Annotation[String](
          description =
            "A short, human-readable summary of the problem type. It does not change from occurrence to occurrence of the problem.",
          example = "Malformed email"
        )

    object detail
        extends Annotation[Option[String]](
          description = "A human-readable explanation specific to this occurrence of the problem.",
          example = Option("The received '{}à!è@!.b}' email does not conform to the email format")
        )

    object instance
        extends Annotation[String](
          description =
            "A URI reference that identifies the specific occurrence of the problem. It may or may not yield further information if dereferenced.",
          example = "The received '{}à!è@!.b}' email does not conform to the email format"
        )
  }

  def notFound(title: String = "NotFound", detail: Option[String] = None) =
    ErrorResponse(StatusCode.NotFound.code, `type` = "NotFound", title = title, detail = detail)

  def internalServerError(title: String = "InternalServerError", detail: Option[String] = None) =
    ErrorResponse(
      StatusCode.InternalServerError.code,
      `type` = "InternalServerError",
      title = title,
      detail = detail
    )

  def badRequest(title: String = "BadRequest", detail: Option[String] = None) =
    ErrorResponse(
      StatusCode.BadRequest.code,
      `type` = title,
      title = title,
      detail = detail
    )

  def unprocessableEntity(title: String = "UnprocessableEntity", detail: Option[String] = None) =
    ErrorResponse(
      StatusCode.UnprocessableEntity.code,
      `type` = title,
      title = title,
      detail = detail
    )

  def conflict(title: String = "Conflict", detail: Option[String] = None) =
    ErrorResponse(
      StatusCode.Conflict.code,
      `type` = title,
      title = title,
      detail = detail
    )
}
