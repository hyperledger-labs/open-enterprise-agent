package org.hyperledger.identus.pollux.sql.repository

import cats.data.NonEmptyList
import doobie.*
import doobie.free.connection
import doobie.implicits.*
import doobie.postgres.implicits.*
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import org.hyperledger.identus.castor.core.model.did.*
import org.hyperledger.identus.mercury.protocol.issuecredential.{IssueCredential, OfferCredential, RequestCredential}
import org.hyperledger.identus.pollux.anoncreds.AnoncredCredentialRequestMetadata
import org.hyperledger.identus.pollux.core.model.*
import org.hyperledger.identus.pollux.core.repository.CredentialRepository
import org.hyperledger.identus.shared.db.ContextAwareTask
import org.hyperledger.identus.shared.db.Implicits.*
import org.hyperledger.identus.shared.models.*
import zio.*
import zio.interop.catz.*
import zio.json.*

import java.time.Instant
import java.util.UUID

class JdbcCredentialRepository(xa: Transactor[ContextAwareTask], xb: Transactor[Task], maxRetries: Int)
    extends CredentialRepository {

  // Uncomment to have Doobie LogHandler in scope and automatically output SQL statements in logs
  // given logHandler: LogHandler = LogHandler.jdkLogHandler

  import IssueCredentialRecord.*

  given credentialFormatGet: Get[CredentialFormat] = Get[String].map(CredentialFormat.valueOf)
  given credentialFormatPut: Put[CredentialFormat] = Put[String].contramap(_.toString)

  given protocolStateGet: Get[ProtocolState] = Get[String].map(ProtocolState.valueOf)
  given protocolStatePut: Put[ProtocolState] = Put[String].contramap(_.toString)

  given roleGet: Get[Role] = Get[String].map(Role.valueOf)
  given rolePut: Put[Role] = Put[String].contramap(_.toString)

  given offerCredentialGet: Get[OfferCredential] = Get[String].map(decode[OfferCredential](_).getOrElse(???))
  given offerCredentialPut: Put[OfferCredential] = Put[String].contramap(_.asJson.toString)

  given requestCredentialGet: Get[RequestCredential] = Get[String].map(decode[RequestCredential](_).getOrElse(???))
  given requestCredentialPut: Put[RequestCredential] = Put[String].contramap(_.asJson.toString)

  given acRequestMetadataGet: Get[AnoncredCredentialRequestMetadata] =
    Get[String].map(_.fromJson[AnoncredCredentialRequestMetadata].getOrElse(???))
  given acRequestMetadataPut: Put[AnoncredCredentialRequestMetadata] = Put[String].contramap(_.toJson)

  given issueCredentialGet: Get[IssueCredential] = Get[String].map(decode[IssueCredential](_).getOrElse(???))
  given issueCredentialPut: Put[IssueCredential] = Put[String].contramap(_.asJson.toString)

  given keyIdGet: Get[KeyId] = Get[String].map(KeyId(_))
  given keyIdPut: Put[KeyId] = Put[String].contramap(_.value)

  given failureGet: Get[Failure] = Get[String].temap(_.fromJson[FailureInfo])
  given failurePut: Put[Failure] = Put[String].contramap(_.asFailureInfo.toJson)

  override def create(record: IssueCredentialRecord): URIO[WalletAccessContext, Unit] = {
    val cxnIO = sql"""
        | INSERT INTO public.issue_credential_records(
        |   id,
        |   created_at,
        |   updated_at,
        |   thid,
        |   schema_uri,
        |   credential_definition_id,
        |   credential_definition_uri,
        |   credential_format,
        |   role,
        |   subject_id,
        |   key_id,
        |   validity_period,
        |   automatic_issuance,
        |   protocol_state,
        |   offer_credential_data,
        |   request_credential_data,
        |   ac_request_credential_metadata,
        |   issue_credential_data,
        |   issued_credential_raw,
        |   issuing_did,
        |   meta_retries,
        |   meta_next_retry,
        |   meta_last_failure,
        |   wallet_id
        | ) values (
        |   ${record.id},
        |   ${record.createdAt},
        |   ${record.updatedAt},
        |   ${record.thid},
        |   ${record.schemaUri},
        |   ${record.credentialDefinitionId},
        |   ${record.credentialDefinitionUri},
        |   ${record.credentialFormat},
        |   ${record.role},
        |   ${record.subjectId},
        |   ${record.keyId},
        |   ${record.validityPeriod},
        |   ${record.automaticIssuance},
        |   ${record.protocolState},
        |   ${record.offerCredentialData},
        |   ${record.requestCredentialData},
        |   ${record.anonCredsRequestMetadata},
        |   ${record.issueCredentialData},
        |   ${record.issuedCredentialRaw},
        |   ${record.issuingDID},
        |   ${record.metaRetries},
        |   ${record.metaNextRetry},
        |   ${record.metaLastFailure},
        |   current_setting('app.current_wallet_id')::UUID
        | )
        """.stripMargin.update

    cxnIO.run
      .transactWallet(xa)
      .ensureOneAffectedRowOrDie
  }

  override def findAll(
      ignoreWithZeroRetries: Boolean,
      offset: Option[Int],
      limit: Option[Int]
  ): URIO[WalletAccessContext, (Seq[IssueCredentialRecord], Int)] = {
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
           |   schema_uri,
           |   credential_definition_id,
           |   credential_definition_uri,
           |   credential_format,
           |   role,
           |   subject_id,
           |   key_id,
           |   validity_period,
           |   automatic_issuance,
           |   protocol_state,
           |   offer_credential_data,
           |   request_credential_data,
           |   ac_request_credential_metadata,
           |   issue_credential_data,
           |   issued_credential_raw,
           |   issuing_did,
           |   meta_retries,
           |   meta_next_retry,
           |   meta_last_failure
           | FROM public.issue_credential_records
           | $conditionFragment
           | ORDER BY created_at
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

    effect
      .transactWallet(xa)
      .orDie
  }

  private def getRecordsByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: IssueCredentialRecord.ProtocolState*
  ): ConnectionIO[Seq[IssueCredentialRecord]] = {
    states match
      case Nil =>
        connection.pure(Nil)
      case head +: tail =>
        val conditionFragment = {
          val nel = NonEmptyList.of(head, tail*)
          val inClauseFragment = Fragments.in(fr"protocol_state", nel)
          if (!ignoreWithZeroRetries) inClauseFragment
          else Fragments.and(inClauseFragment, fr"meta_retries > 0")
        }
        val cxnIO = sql"""
            | SELECT
            |   id,
            |   created_at,
            |   updated_at,
            |   thid,
            |   schema_uri,
            |   credential_definition_id,
            |   credential_definition_uri,
            |   credential_format,
            |   role,
            |   subject_id,
            |   key_id,
            |   validity_period,
            |   automatic_issuance,
            |   protocol_state,
            |   offer_credential_data,
            |   request_credential_data,
            |   ac_request_credential_metadata,
            |   issue_credential_data,
            |   issued_credential_raw,
            |   issuing_did,
            |   meta_retries,
            |   meta_next_retry,
            |   meta_last_failure
            | FROM public.issue_credential_records
            | WHERE $conditionFragment
            | ORDER BY created_at
            | LIMIT $limit
            """.stripMargin
          .query[IssueCredentialRecord]
          .to[Seq]
        cxnIO
  }
  override def findByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: IssueCredentialRecord.ProtocolState*
  ): URIO[WalletAccessContext, Seq[IssueCredentialRecord]] = {
    getRecordsByStates(ignoreWithZeroRetries, limit, states*)
      .transactWallet(xa)
      .orDie
  }

  override def findByStatesForAllWallets(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: IssueCredentialRecord.ProtocolState*
  ): UIO[Seq[IssueCredentialRecord]] = {
    getRecordsByStates(ignoreWithZeroRetries, limit, states*)
      .transact(xb)
      .orDie
  }

  override def getById(recordId: DidCommID): URIO[WalletAccessContext, IssueCredentialRecord] =
    for {
      maybeRecord <- findById(recordId)
      record <- ZIO.fromOption(maybeRecord).orDieWith(_ => RuntimeException(s"Record not found: $recordId"))
    } yield record

  override def findById(
      recordId: DidCommID
  ): URIO[WalletAccessContext, Option[IssueCredentialRecord]] = {
    val cxnIO = sql"""
        | SELECT
        |   id,
        |   created_at,
        |   updated_at,
        |   thid,
        |   schema_uri,
        |   credential_definition_id,
        |   credential_definition_uri,
        |   credential_format,
        |   role,
        |   subject_id,
        |   key_id,
        |   validity_period,
        |   automatic_issuance,
        |   protocol_state,
        |   offer_credential_data,
        |   request_credential_data,
        |   ac_request_credential_metadata,
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
      .transactWallet(xa)
      .orDie
  }

  override def findByThreadId(
      thid: DidCommID,
      ignoreWithZeroRetries: Boolean,
  ): URIO[WalletAccessContext, Option[IssueCredentialRecord]] = {
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
        |   schema_uri,
        |   credential_definition_id,
        |   credential_definition_uri,
        |   credential_format,
        |   role,
        |   subject_id,
        |   key_id,
        |   validity_period,
        |   automatic_issuance,
        |   protocol_state,
        |   offer_credential_data,
        |   request_credential_data,
        |   ac_request_credential_metadata,
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
      .transactWallet(xa)
      .orDie
  }

  override def updateProtocolState(
      recordId: DidCommID,
      from: IssueCredentialRecord.ProtocolState,
      to: IssueCredentialRecord.ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
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
      .transactWallet(xa)
      .ensureOneAffectedRowOrDie
  }

  def updateWithSubjectId(
      recordId: DidCommID,
      subjectId: String,
      keyId: Option[KeyId] = None,
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
    val cxnIO = sql"""
        | UPDATE public.issue_credential_records
        | SET
        |   protocol_state = $protocolState,
        |   subject_id = ${Some(subjectId)},
        |   key_id = ${keyId},
        |   updated_at = ${Instant.now}
        | WHERE
        |   id = $recordId
        """.stripMargin.update

    cxnIO.run
      .transactWallet(xa)
      .ensureOneAffectedRowOrDie
  }

  override def updateWithJWTRequestCredential(
      recordId: DidCommID,
      request: RequestCredential,
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
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
      .transactWallet(xa)
      .ensureOneAffectedRowOrDie
  }

  override def updateWithAnonCredsRequestCredential(
      recordId: DidCommID,
      request: RequestCredential,
      metadata: AnoncredCredentialRequestMetadata,
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
    val cxnIO =
      sql"""
           | UPDATE public.issue_credential_records
           | SET
           |   request_credential_data = $request,
           |   ac_request_credential_metadata = $metadata,
           |   protocol_state = $protocolState,
           |   updated_at = ${Instant.now}
           | WHERE
           |   id = $recordId
        """.stripMargin.update

    cxnIO.run
      .transactWallet(xa)
      .ensureOneAffectedRowOrDie
  }

  override def updateWithIssueCredential(
      recordId: DidCommID,
      issue: IssueCredential,
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
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
      .transactWallet(xa)
      .ensureOneAffectedRowOrDie
  }

  override def findValidIssuedCredentials(
      recordIds: Seq[DidCommID]
  ): URIO[WalletAccessContext, Seq[ValidIssuedCredentialRecord]] = {
    val idAsStrings = recordIds.map(_.toString)
    val nel = NonEmptyList.of(idAsStrings.head, idAsStrings.tail*)
    val inClauseFragment = Fragments.in(fr"id", nel)

    val cxnIO = sql"""
        | SELECT
        |   id,
        |   issued_credential_raw,
        |   credential_format,
        |   subject_id,
        |   key_id
        | FROM public.issue_credential_records
        | WHERE
        |   issued_credential_raw IS NOT NULL
        |   AND $inClauseFragment
        """.stripMargin
      .query[ValidIssuedCredentialRecord]
      .to[Seq]

    cxnIO
      .transactWallet(xa)
      .orDie

  }

  override def findValidAnonCredsIssuedCredentials(
      recordIds: Seq[DidCommID]
  ): URIO[WalletAccessContext, Seq[ValidFullIssuedCredentialRecord]] = {
    val idAsStrings = recordIds.map(_.toString)
    val nel = NonEmptyList.of(idAsStrings.head, idAsStrings.tail*)
    val inClauseFragment = Fragments.in(fr"id", nel)

    val cxnIO = sql"""
                     | SELECT
                     |   id,
                     |   issue_credential_data,
                     |   credential_format,
                     |   schema_uri,
                     |   credential_definition_uri,
                     |   subject_id
                     | FROM public.issue_credential_records
                     | WHERE 1=1
                     |   AND issue_credential_data IS NOT NULL
                     |   AND schema_uri IS NOT NULL
                     |   AND credential_definition_uri IS NOT NULL
                     |   AND credential_format = 'AnonCreds'
                     |   AND $inClauseFragment
        """.stripMargin
      .query[ValidFullIssuedCredentialRecord]
      .to[Seq]

    cxnIO
      .transactWallet(xa)
      .orDie

  }

  override def deleteById(recordId: DidCommID): URIO[WalletAccessContext, Unit] = {
    val cxnIO = sql"""
      | DELETE
      | FROM public.issue_credential_records
      | WHERE id = $recordId
      """.stripMargin.update

    cxnIO.run
      .transactWallet(xa)
      .ensureOneAffectedRowOrDie
  }

  override def updateWithIssuedRawCredential(
      recordId: DidCommID,
      issue: IssueCredential,
      issuedRawCredential: String,
      schemaUri: Option[String],
      credentialDefinitionUri: Option[String],
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
    val cxnIO = sql"""
        | UPDATE public.issue_credential_records
        | SET
        |   schema_uri = $schemaUri,
        |   credential_definition_uri = $credentialDefinitionUri,
        |   issue_credential_data = $issue,
        |   issued_credential_raw = $issuedRawCredential,
        |   protocol_state = $protocolState,
        |   updated_at = ${Instant.now}
        | WHERE
        |   id = $recordId
        """.stripMargin.update

    cxnIO.run
      .transactWallet(xa)
      .ensureOneAffectedRowOrDie
  }

  override def updateAfterFail(
      recordId: DidCommID,
      failReason: Option[Failure]
  ): URIO[WalletAccessContext, Unit] = {
    val cxnIO = sql"""
        | UPDATE public.issue_credential_records
        | SET
        |   meta_retries = CASE WHEN (meta_retries > 1) THEN meta_retries - 1 ELSE 0 END,
        |   meta_next_retry = CASE WHEN (meta_retries > 1) THEN ${Instant.now().plusSeconds(60)} ELSE null END,
        |   meta_last_failure = ${failReason}
        | WHERE
        |   id = $recordId
        """.stripMargin.update
    cxnIO.run
      .transactWallet(xa)
      .ensureOneAffectedRowOrDie
  }
}

object JdbcCredentialRepository {
  val maxRetries = 5 // TODO Move to config
  val layer: URLayer[Transactor[ContextAwareTask] & Transactor[Task], CredentialRepository] =
    ZLayer.fromFunction(new JdbcCredentialRepository(_, _, maxRetries))
}
