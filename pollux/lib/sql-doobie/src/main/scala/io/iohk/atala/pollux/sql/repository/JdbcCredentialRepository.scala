package io.iohk.atala.pollux.sql.repository

import cats.data.NonEmptyList
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import io.iohk.atala.castor.core.model.did.*
import io.iohk.atala.mercury.protocol.issuecredential.{IssueCredential, OfferCredential, RequestCredential}
import io.iohk.atala.pollux.core.model.*
import io.iohk.atala.pollux.core.model.error.CredentialRepositoryError
import io.iohk.atala.pollux.core.model.error.CredentialRepositoryError.*
import io.iohk.atala.pollux.core.repository.CredentialRepository
import io.iohk.atala.prism.crypto.MerkleInclusionProof
import io.iohk.atala.shared.utils.BytesOps
import org.postgresql.util.PSQLException
import zio.*
import zio.interop.catz.*

import java.time.Instant

// TODO: replace with actual implementation
class JdbcCredentialRepository(xa: Transactor[Task], maxRetries: Int) extends CredentialRepository[Task] {
  // serializes into hex string

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

  import IssueCredentialRecord.*

  given didCommIDGet: Get[DidCommID] = Get[String].map(DidCommID(_))
  given didCommIDPut: Put[DidCommID] = Put[String].contramap(_.value)

  given protocolStateGet: Get[ProtocolState] = Get[String].map(ProtocolState.valueOf)
  given protocolStatePut: Put[ProtocolState] = Put[String].contramap(_.toString)

  given publicationStateGet: Get[PublicationState] = Get[String].map(PublicationState.valueOf)
  given publicationStatePut: Put[PublicationState] = Put[String].contramap(_.toString)

  given roleGet: Get[Role] = Get[String].map(Role.valueOf)
  given rolePut: Put[Role] = Put[String].contramap(_.toString)

  given offerCredentialGet: Get[OfferCredential] = Get[String].map(decode[OfferCredential](_).getOrElse(???))
  given offerCredentialPut: Put[OfferCredential] = Put[String].contramap(_.asJson.toString)

  given requestCredentialGet: Get[RequestCredential] = Get[String].map(decode[RequestCredential](_).getOrElse(???))
  given requestCredentialPut: Put[RequestCredential] = Put[String].contramap(_.asJson.toString)

  given issueCredentialGet: Get[IssueCredential] = Get[String].map(decode[IssueCredential](_).getOrElse(???))
  given issueCredentialPut: Put[IssueCredential] = Put[String].contramap(_.asJson.toString)

  given inclusionProofGet: Get[MerkleInclusionProof] = Get[String].map(deserializeInclusionProof)

  given prismDIDGet: Get[CanonicalPrismDID] =
    Get[String].map(s => PrismDID.fromString(s).fold(e => throw RuntimeException(e), _.asCanonical))
  given prismDIDPut: Put[CanonicalPrismDID] = Put[String].contramap(_.toString)

  override def createIssueCredentialRecord(record: IssueCredentialRecord): Task[Int] = {
    val cxnIO = sql"""
        | INSERT INTO public.issue_credential_records(
        |   id,
        |   created_at,
        |   updated_at,
        |   thid,
        |   schema_id,
        |   role,
        |   subject_id,
        |   validity_period,
        |   automatic_issuance,
        |   await_confirmation,
        |   protocol_state,
        |   publication_state,
        |   offer_credential_data,
        |   request_credential_data,
        |   issue_credential_data,
        |   issued_credential_raw,
        |   issuing_did,
        |   meta_retries,
        |   meta_next_retry,
        |   meta_last_failure
        | ) values (
        |   ${record.id},
        |   ${record.createdAt},
        |   ${record.updatedAt},
        |   ${record.thid},
        |   ${record.schemaId},
        |   ${record.role},
        |   ${record.subjectId},
        |   ${record.validityPeriod},
        |   ${record.automaticIssuance},
        |   ${record.awaitConfirmation},
        |   ${record.protocolState},
        |   ${record.publicationState},
        |   ${record.offerCredentialData},
        |   ${record.requestCredentialData},
        |   ${record.issueCredentialData},
        |   ${record.issuedCredentialRaw},
        |   ${record.issuingDID},
        |   ${record.metaRetries},
        |   ${record.metaNextRetry},
        |   ${record.metaLastFailure}
        | )
        """.stripMargin.update

    cxnIO.run
      .transact(xa)
      .mapError {
        case e: PSQLException => CredentialRepositoryError.fromPSQLException(e.getSQLState, e.getMessage)
        case e                => e
      }
  }

  override def getIssueCredentialRecords(
      ignoreWithZeroRetries: Boolean = true,
      offset: Option[Int],
      limit: Option[Int]
  ): Task[(Seq[IssueCredentialRecord], Int)] = {
    val conditionFragment = Fragments.whereAndOpt(
      Option.when(ignoreWithZeroRetries)(fr"meta_retries > 0")
    )
    val baseFragment =
      sql"""
        | SELECT
        |   id,
        |   created_at,
        |   updated_at,
        |   thid,
        |   schema_id,
        |   role,
        |   subject_id,
        |   validity_period,
        |   automatic_issuance,
        |   await_confirmation,
        |   protocol_state,
        |   publication_state,
        |   offer_credential_data,
        |   request_credential_data,
        |   issue_credential_data,
        |   issued_credential_raw,
        |   issuing_did,
        |   meta_retries,
        |   meta_next_retry,
        |   meta_last_failure
        | FROM public.issue_credential_records
        | $conditionFragment
        """.stripMargin
    val withOffsetFragment = offset.fold(baseFragment)(offsetValue => baseFragment ++ fr"OFFSET $offsetValue")
    val withOffsetAndLimitFragment =
      limit.fold(withOffsetFragment)(limitValue => withOffsetFragment ++ fr"LIMIT $limitValue")

    val countCxnIO =
      sql"""
           | SELECT COUNT(*)
           | FROM public.issue_credential_records
           | $conditionFragment
           """.stripMargin
        .query[Int]
        .unique

    val cxnIO = withOffsetAndLimitFragment
      .query[IssueCredentialRecord]
      .to[Seq]

    val effect = for {
      totalCount <- countCxnIO
      records <- cxnIO
    } yield (records, totalCount)

    effect.transact(xa)
  }

  override def getIssueCredentialRecordsByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: IssueCredentialRecord.ProtocolState*
  ): Task[Seq[IssueCredentialRecord]] = {
    states match
      case Nil =>
        ZIO.succeed(Nil)
      case head +: tail =>
        val nel = NonEmptyList.of(head, tail: _*)
        val inClauseFragment = Fragments.in(fr"protocol_state", nel)
        val conditionFragment = Fragments.andOpt(
          Some(inClauseFragment),
          Option.when(ignoreWithZeroRetries)(fr"meta_retries > 0")
        )
        val cxnIO = sql"""
            | SELECT
            |   id,
            |   created_at,
            |   updated_at,
            |   thid,
            |   schema_id,
            |   role,
            |   subject_id,
            |   validity_period,
            |   automatic_issuance,
            |   await_confirmation,
            |   protocol_state,
            |   publication_state,
            |   offer_credential_data,
            |   request_credential_data,
            |   issue_credential_data,
            |   issued_credential_raw,
            |   issuing_did,
            |   meta_retries,
            |   meta_next_retry,
            |   meta_last_failure
            | FROM public.issue_credential_records
            | WHERE $conditionFragment
            | LIMIT $limit
            """.stripMargin
          .query[IssueCredentialRecord]
          .to[Seq]

        cxnIO
          .transact(xa)
  }

  override def getIssueCredentialRecord(recordId: DidCommID): Task[Option[IssueCredentialRecord]] = {
    val cxnIO = sql"""
        | SELECT
        |   id,
        |   created_at,
        |   updated_at,
        |   thid,
        |   schema_id,
        |   role,
        |   subject_id,
        |   validity_period,
        |   automatic_issuance,
        |   await_confirmation,
        |   protocol_state,
        |   publication_state,
        |   offer_credential_data,
        |   request_credential_data,
        |   issue_credential_data,
        |   issued_credential_raw,
        |   issuing_did,
        |   meta_retries,
        |   meta_next_retry,
        |   meta_last_failure
        | FROM public.issue_credential_records
        | WHERE id = $recordId
        """.stripMargin
      .query[IssueCredentialRecord]
      .option

    cxnIO
      .transact(xa)
  }

  override def getIssueCredentialRecordByThreadId(
      thid: DidCommID,
      ignoreWithZeroRetries: Boolean = true,
  ): Task[Option[IssueCredentialRecord]] = {
    val conditionFragment = Fragments.whereAndOpt(
      Some(fr"thid = $thid"),
      Option.when(ignoreWithZeroRetries)(fr"meta_retries > 0")
    )
    val cxnIO = sql"""
        | SELECT
        |   id,
        |   created_at,
        |   updated_at,
        |   thid,
        |   schema_id,
        |   role,
        |   subject_id,
        |   validity_period,
        |   automatic_issuance,
        |   await_confirmation,
        |   protocol_state,
        |   publication_state,
        |   offer_credential_data,
        |   request_credential_data,
        |   issue_credential_data,
        |   issued_credential_raw,
        |   issuing_did,
        |   meta_retries,
        |   meta_next_retry,
        |   meta_last_failure
        | FROM public.issue_credential_records
        | $conditionFragment
        """.stripMargin
      .query[IssueCredentialRecord]
      .option

    cxnIO
      .transact(xa)
  }

  override def updateCredentialRecordProtocolState(
      recordId: DidCommID,
      from: IssueCredentialRecord.ProtocolState,
      to: IssueCredentialRecord.ProtocolState
  ): Task[Int] = {
    val cxnIO = sql"""
        | UPDATE public.issue_credential_records
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

  // TODO: refactor to work with issueCredential form mercury
  override def updateCredentialRecordStateAndProofByCredentialIdBulk(
      idsStatesAndProofs: Seq[(DidCommID, IssueCredentialRecord.PublicationState, MerkleInclusionProof)]
  ): Task[Int] = {

    if (idsStatesAndProofs.isEmpty) ZIO.succeed(0)
    else
      val values = idsStatesAndProofs.map { idStateAndProof =>
        val (id, state, proof) = idStateAndProof
        s"(${id.toString}, '${state.toString}', '${serializeInclusionProof(proof)}')"
      }

      val cxnIO = sql"""
          | UPDATE public.issue_credential_records as icr
          | SET
          |   publication_state = idsStatesAndProofs.publication_state,
          |   merkle_inclusion_proof = idsStatesAndProofs.serializedProof
          | FROM (values ${values.mkString(",")}) as idsStatesAndProofs(id, publication_state, serializedProof)
          | WHERE icr.id = idsStatesAndProofs.id
          |""".stripMargin.update

      cxnIO.run
        .transact(xa)
  }

  override def updateCredentialRecordPublicationState(
      recordId: DidCommID,
      from: Option[IssueCredentialRecord.PublicationState],
      to: Option[IssueCredentialRecord.PublicationState]
  ): Task[Int] = {
    val pubStateFragment = from
      .map(state => fr"publication_state = $state")
      .getOrElse(fr"publication_state IS NULL")

    val cxnIO = sql"""
        | UPDATE public.issue_credential_records
        | SET
        |   publication_state = $to,
        |   updated_at = ${Instant.now}
        | WHERE
        |   id = $recordId
        |   AND $pubStateFragment
        """.stripMargin.update

    cxnIO.run
      .transact(xa)
  }

  def updateWithSubjectId(
      recordId: DidCommID,
      subjectId: String,
      protocolState: ProtocolState
  ): Task[Int] = {
    val cxnIO = sql"""
        | UPDATE public.issue_credential_records
        | SET
        |   protocol_state = $protocolState,
        |   subject_id = ${Some(subjectId)},
        |   updated_at = ${Instant.now}
        | WHERE
        |   id = $recordId
        """.stripMargin.update

    cxnIO.run
      .transact(xa)
  }

  override def updateWithRequestCredential(
      recordId: DidCommID,
      request: RequestCredential,
      protocolState: ProtocolState
  ): Task[Int] = {
    val cxnIO = sql"""
        | UPDATE public.issue_credential_records
        | SET
        |   request_credential_data = $request,
        |   protocol_state = $protocolState,
        |   updated_at = ${Instant.now}
        | WHERE
        |   id = $recordId
        """.stripMargin.update

    cxnIO.run
      .transact(xa)
  }

  override def updateWithIssueCredential(
      recordId: DidCommID,
      issue: IssueCredential,
      protocolState: ProtocolState
  ): Task[Int] = {
    val cxnIO = sql"""
        | UPDATE public.issue_credential_records
        | SET
        |   issue_credential_data = $issue,
        |   protocol_state = $protocolState,
        |   updated_at = ${Instant.now}
        | WHERE
        |   id = $recordId
        """.stripMargin.update

    cxnIO.run
      .transact(xa)
  }

  override def getValidIssuedCredentials(recordIds: Seq[DidCommID]): Task[Seq[ValidIssuedCredentialRecord]] = {
    val idAsStrings = recordIds.map(_.toString)
    val nel = NonEmptyList.of(idAsStrings.head, idAsStrings.tail: _*)
    val inClauseFragment = Fragments.in(fr"id", nel)

    val cxnIO = sql"""
        | SELECT
        |   id,
        |   issued_credential_raw,
        |   subject_id
        | FROM public.issue_credential_records
        | WHERE
        |   issued_credential_raw IS NOT NULL
        |   AND $inClauseFragment
        """.stripMargin
      .query[ValidIssuedCredentialRecord]
      .to[Seq]

    cxnIO
      .transact(xa)

  }

  override def deleteIssueCredentialRecord(recordId: DidCommID): Task[Int] = {
    val cxnIO = sql"""
      | DELETE
      | FROM public.issue_credential_records
      | WHERE id = $recordId
      """.stripMargin.update

    cxnIO.run
      .transact(xa)
  }

  override def updateWithIssuedRawCredential(
      recordId: DidCommID,
      issue: IssueCredential,
      issuedRawCredential: String,
      protocolState: ProtocolState
  ): Task[Int] = {
    val cxnIO = sql"""
        | UPDATE public.issue_credential_records
        | SET
        |   issue_credential_data = $issue,
        |   issued_credential_raw = $issuedRawCredential,
        |   protocol_state = $protocolState,
        |   updated_at = ${Instant.now}
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

object JdbcCredentialRepository {
  val maxRetries = 5 // TODO Move to config
  val layer: URLayer[Transactor[Task], CredentialRepository[Task]] =
    ZLayer.fromFunction(new JdbcCredentialRepository(_, maxRetries))
}
