package io.iohk.atala.pollux.sql.repository

import cats.data.NonEmptyList
import cats.instances.seq
import doobie.*
import doobie.implicits.*
import doobie.postgres._
import doobie.postgres.implicits._
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import io.iohk.atala.mercury.protocol.presentproof._
import io.iohk.atala.pollux.core.model.PresentationRecord.ProtocolState
import io.iohk.atala.pollux.core.model.*
import io.iohk.atala.pollux.core.repository.PresentationRepository
import io.iohk.atala.pollux.sql.model.JWTCredentialRow
import io.iohk.atala.prism.crypto.MerkleInclusionProof
import io.iohk.atala.shared.utils.BytesOps
import zio.*
import zio.interop.catz.*

import java.time.Instant
import java.util.UUID
import java.{util => ju}

// TODO: replace with actual implementation
class JdbcPresentationRepository(
    xa: Transactor[Task],
    maxRetries: Int
) extends PresentationRepository[Task] {
  // serializes into hex string

  override def updatePresentationWithCredentialsToUse(
      recordId: DidCommID,
      credentialsToUse: Option[Seq[String]],
      protocolState: ProtocolState
  ): Task[Int] = {
    val cxnIO = sql"""
        | UPDATE public.presentation_records
        | SET
        |   credentials_to_use = ${credentialsToUse.map(_.toList)},
        |   protocol_state = $protocolState,
        |   updated_at = ${Instant.now},
        |   meta_retries = $maxRetries,
        |   meta_next_retry = ${Instant.now},
        |   meta_last_failure = null
        | WHERE
        |   id = $recordId
        """.stripMargin.update

    cxnIO.run
      .transact(xa)
  }

  private def serializeInclusionProof(proof: MerkleInclusionProof): String = BytesOps.bytesToHex(proof.encode.getBytes)

  // deserializes from the hex string
  private def deserializeInclusionProof(proof: String): MerkleInclusionProof =
    MerkleInclusionProof.decode(
      String(
        BytesOps.hexToBytes(proof)
      )
    )

  // Uncomment to have Doobie LogHandler in scope and automatically output SQL statements in logs
  // given logHandler: LogHandler = LogHandler.jdkLogHandler

  import PresentationRecord._

  given didCommIDGet: Get[DidCommID] = Get[String].map(DidCommID(_))
  given didCommIDPut: Put[DidCommID] = Put[String].contramap(_.value)

  import doobie.postgres.implicits.JavaTimeInstantMeta

  given protocolStateGet: Get[ProtocolState] = Get[String].map(ProtocolState.valueOf)
  given protocolStatePut: Put[ProtocolState] = Put[String].contramap(_.toString)

  given roleGet: Get[Role] = Get[String].map(Role.valueOf)
  given rolePut: Put[Role] = Put[String].contramap(_.toString)

  given presentationGet: Get[Presentation] = Get[String].map(decode[Presentation](_).getOrElse(???))
  given presentationPut: Put[Presentation] = Put[String].contramap(_.asJson.toString)

  given requestPresentationGet: Get[RequestPresentation] =
    Get[String].map(decode[RequestPresentation](_).getOrElse(???))
  given requestPresentationPut: Put[RequestPresentation] = Put[String].contramap(_.asJson.toString)

  given proposePresentationGet: Get[ProposePresentation] =
    Get[String].map(decode[ProposePresentation](_).getOrElse(???))
  given proposePresentationPut: Put[ProposePresentation] = Put[String].contramap(_.asJson.toString)

  given inclusionProofGet: Get[MerkleInclusionProof] = Get[String].map(deserializeInclusionProof)

  override def createPresentationRecord(record: PresentationRecord): Task[Int] = {
    val cxnIO = sql"""
        | INSERT INTO public.presentation_records(
        |   id,
        |   created_at,
        |   updated_at,
        |   thid,
        |   connection_id,
        |   schema_id,
        |   role,
        |   subject_id,
        |   protocol_state,
        |   request_presentation_data,
        |   credentials_to_use,
        |   meta_retries,
        |   meta_next_retry,
        |   meta_last_failure
        | ) values (
        |   ${record.id},
        |   ${record.createdAt},
        |   ${record.updatedAt},
        |   ${record.thid},
        |   ${record.connectionId},
        |   ${record.schemaId},
        |   ${record.role},
        |   ${record.subjectId},
        |   ${record.protocolState},
        |   ${record.requestPresentationData},
        |   ${record.credentialsToUse.map(_.toList)},
        |   ${record.metaRetries},
        |   ${record.metaNextRetry},
        |   ${record.metaLastFailure}
        | )
        """.stripMargin.update

    cxnIO.run
      .transact(xa)
  }

  override def getPresentationRecords(): Task[Seq[PresentationRecord]] = {
    val cxnIO = sql"""
        | SELECT
        |   id,
        |   created_at,
        |   updated_at,
        |   thid,
        |   schema_id,
        |   connection_id,
        |   role,
        |   subject_id,
        |   protocol_state,
        |   request_presentation_data,
        |   propose_presentation_data,
        |   presentation_data,
        |   credentials_to_use,
        |   meta_retries,
        |   meta_next_retry,
        |   meta_last_failure
        | FROM public.presentation_records
        """.stripMargin
      .query[PresentationRecord]
      .to[Seq]

    cxnIO
      .transact(xa)
  }

  override def getPresentationRecordsByStates(
      states: PresentationRecord.ProtocolState*
  ): Task[Seq[PresentationRecord]] = {
    states match
      case Nil =>
        ZIO.succeed(Nil)
      case head +: tail =>
        val nel = NonEmptyList.of(head, tail: _*)
        val inClauseFragment = Fragments.in(fr"protocol_state", nel)
        val cxnIO = sql"""
            | SELECT
            |   id,
            |   created_at,
            |   updated_at,
            |   thid,
            |   connection_id,
            |   schema_id,
            |   role,
            |   subject_id,
            |   protocol_state,
            |   request_presentation_data,
            |   propose_presentation_data,
            |   presentation_data,
            |   credentials_to_use,
            |   meta_retries,
            |   meta_next_retry,
            |   meta_last_failure
            | FROM public.presentation_records
            | WHERE $inClauseFragment
            """.stripMargin
          .query[PresentationRecord]
          .to[Seq]

        cxnIO
          .transact(xa)
  }

  override def getPresentationRecord(recordId: DidCommID): Task[Option[PresentationRecord]] = {
    val cxnIO = sql"""
        | SELECT
        |   id,
        |   created_at,
        |   updated_at,
        |   thid,
        |   connection_id,
        |   schema_id,
        |   role,
        |   subject_id,
        |   protocol_state,
        |   request_presentation_data,
        |   propose_presentation_data,
        |   presentation_data,
        |   credentials_to_use,
        |   meta_retries,
        |   meta_next_retry,
        |   meta_last_failure
        | FROM public.presentation_records
        | WHERE id = $recordId
        """.stripMargin
      .query[PresentationRecord]
      .option

    cxnIO
      .transact(xa)
  }

  override def getPresentationRecordByThreadId(thid: DidCommID): Task[Option[PresentationRecord]] = {
    val cxnIO = sql"""
        | SELECT
        |   id,
        |   created_at,
        |   updated_at,
        |   thid,
        |   connection_id,
        |   schema_id,
        |   role,
        |   subject_id,
        |   protocol_state,
        |   request_presentation_data,
        |   propose_presentation_data,
        |   presentation_data,
        |   credentials_to_use,
        |   meta_retries,
        |   meta_next_retry,
        |   meta_last_failure
        | FROM public.presentation_records
        | WHERE thid = $thid
        """.stripMargin
      .query[PresentationRecord]
      .option

    cxnIO
      .transact(xa)
  }

  override def updatePresentationRecordProtocolState(
      recordId: DidCommID,
      from: PresentationRecord.ProtocolState,
      to: PresentationRecord.ProtocolState
  ): Task[Int] = {
    val cxnIO = sql"""
        | UPDATE public.presentation_records
        | SET
        |   protocol_state = $to,
        |   updated_at = ${Instant.now},
        |   meta_retries = $maxRetries,
        |   meta_next_retry = ${Instant.now},
        |   meta_last_failure = null
        | WHERE
        |   id = $recordId
        |   AND protocol_state = $from
        """.stripMargin.update

    cxnIO.run
      .transact(xa)
  }

  override def updateWithRequestPresentation(
      recordId: DidCommID,
      request: RequestPresentation,
      protocolState: ProtocolState
  ): Task[Int] = {
    val cxnIO = sql"""
        | UPDATE public.presentation_records
        | SET
        |   request_presentation_data = $request,
        |   protocol_state = $protocolState,
        |   updated_at = ${Instant.now},
        |   meta_retries = $maxRetries,
        |   meta_next_retry = ${Instant.now},
        |   meta_last_failure = null
        | WHERE
        |   id = $recordId
        """.stripMargin.update

    cxnIO.run
      .transact(xa)
  }

  override def updateWithProposePresentation(
      recordId: DidCommID,
      propose: ProposePresentation,
      protocolState: ProtocolState
  ): Task[Int] = {
    val cxnIO = sql"""
        | UPDATE public.presentation_records
        | SET
        |   propose_presentation_data = $propose,
        |   protocol_state = $protocolState,
        |   updated_at = ${Instant.now},
        |   meta_retries = $maxRetries,
        |   meta_next_retry = ${Instant.now},
        |   meta_last_failure = null
        | WHERE
        |   id = $recordId
        """.stripMargin.update

    cxnIO.run
      .transact(xa)
  }

  override def updateWithPresentation(
      recordId: DidCommID,
      presentation: Presentation,
      protocolState: ProtocolState
  ): Task[Int] = {
    val cxnIO = sql"""
        | UPDATE public.presentation_records
        | SET
        |   presentation_data = $presentation,
        |   protocol_state = $protocolState,
        |   updated_at = ${Instant.now},
        |   meta_retries = $maxRetries,
        |   meta_next_retry = ${Instant.now},
        |   meta_last_failure = null
        | WHERE
        |   id = $recordId
        """.stripMargin.update

    cxnIO.run
      .transact(xa)
  }

  def updateAfterFail(
      recordId: DidCommID,
      failReason: Option[String]
  ): Task[Int] = {
    val cxnIO = sql"""
        | UPDATE public.issue_credential_records
        | SET
        |   meta_retries = CASE WHEN (meta_retries > 1) THEN meta_retries - 1 ELSE 0 END,
        |   meta_next_retry = CASE WHEN (meta_retries > 1) THEN ${Instant.now().plusSeconds(60)} ELSE null END,
        |   meta_last_failure = ${failReason}
        | WHERE
        |   id = $recordId
        """.stripMargin.update
    cxnIO.run.transact(xa)
  }

}

object JdbcPresentationRepository {
  val maxRetries = 5 // TODO Move to config
  val layer: URLayer[Transactor[Task], PresentationRepository[Task]] =
    ZLayer.fromFunction(new JdbcPresentationRepository(_, maxRetries))
}
