package org.hyperledger.identus.pollux.sql.repository

import cats.data.NonEmptyList
import doobie.*
import doobie.free.connection
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.circe.json.implicits.*
import doobie.postgres.implicits.*
import io.circe
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import org.hyperledger.identus.mercury.protocol.presentproof.*
import org.hyperledger.identus.pollux.core.model.*
import org.hyperledger.identus.pollux.core.repository.PresentationRepository
import org.hyperledger.identus.shared.db.ContextAwareTask
import org.hyperledger.identus.shared.db.Implicits.*
import org.hyperledger.identus.shared.models.*
import zio.*
import zio.interop.catz.*
import zio.json.*
import zio.json.ast.Json
import zio.json.ast.Json.*

import java.time.Instant
// TODO: replace with actual implementation
class JdbcPresentationRepository(
    xa: Transactor[ContextAwareTask],
    xb: Transactor[Task],
    maxRetries: Int
) extends PresentationRepository {
  // serializes into hex string

  override def updatePresentationWithCredentialsToUse(
      recordId: DidCommID,
      credentialsToUse: Option[Seq[String]],
      protocolState: PresentationRecord.ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
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
      .transactWallet(xa)
      .orDie
      .ensureOneAffectedRowOrDie
  }

  override def updateSDJWTPresentationWithCredentialsToUse(
      recordId: DidCommID,
      credentialsToUse: Option[Seq[String]],
      sdJwtClaimsToDisclose: Option[SdJwtCredentialToDisclose],
      protocolState: PresentationRecord.ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
    val cxnIO =
      sql"""
           | UPDATE public.presentation_records
           | SET
           |   credentials_to_use = ${credentialsToUse.map(_.toList)},
           |   sd_jwt_claims_to_disclose = $sdJwtClaimsToDisclose,
           |   protocol_state = $protocolState,
           |   updated_at = ${Instant.now},
           |   meta_retries = $maxRetries,
           |   meta_next_retry = ${Instant.now},
           |   meta_last_failure = null
           | WHERE
           |   id = $recordId
        """.stripMargin.update

    cxnIO.run
      .transactWallet(xa)
      .orDie
      .ensureOneAffectedRowOrDie
  }

  def updateAnoncredPresentationWithCredentialsToUse(
      recordId: DidCommID,
      anoncredCredentialsToUseJsonSchemaId: Option[String],
      anoncredCredentialsToUse: Option[AnoncredCredentialProofs],
      protocolState: PresentationRecord.ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
    val cxnIO =
      sql"""
           | UPDATE public.presentation_records
           | SET
           |   anoncred_credentials_to_use_json_schema_id = ${anoncredCredentialsToUseJsonSchemaId},
           |   anoncred_credentials_to_use = ${anoncredCredentialsToUse},
           |   protocol_state = $protocolState,
           |   updated_at = ${Instant.now},
           |   meta_retries = $maxRetries,
           |   meta_next_retry = ${Instant.now},
           |   meta_last_failure = null
           | WHERE
           |   id = $recordId
        """.stripMargin.update

    cxnIO.run
      .transactWallet(xa)
      .orDie
      .ensureOneAffectedRowOrDie
  }

  // Uncomment to have Doobie LogHandler in scope and automatically output SQL statements in logs
  // given logHandler: LogHandler = LogHandler.jdkLogHandler

  import PresentationRecord.*

  def zioJsonToCirceJson(zioJson: Json): circe.Json = {
    parse(zioJson.toString).getOrElse(circe.Json.Null)
  }

  def circeJsonToZioJson(circeJson: circe.Json): Json = {
    circeJson.noSpaces.fromJson[Json].getOrElse(Json.Null)
  }

  given jsonGet: Get[AnoncredCredentialProofs] = Get[circe.Json].map { jsonString => circeJsonToZioJson(jsonString) }
  given jsonPut: Put[AnoncredCredentialProofs] = Put[circe.Json].contramap(zioJsonToCirceJson(_))

  def zioJsonToCirceJsonObj(zioJson: Json.Obj): circe.Json =
    parse(zioJson.toString).getOrElse(circe.Json.Null)
  def circeJsonToZioJsonObj(circeJson: circe.Json): Json.Obj =
    circeJson.noSpaces.fromJson[Json.Obj].getOrElse(Json.Obj())
  given Get[SdJwtCredentialToDisclose] = Get[circe.Json].map { jsonString => circeJsonToZioJsonObj(jsonString) }
  given Put[SdJwtCredentialToDisclose] = Put[circe.Json].contramap(zioJsonToCirceJsonObj(_))

  given didCommIDGet: Get[DidCommID] = Get[String].map(DidCommID(_))
  given didCommIDPut: Put[DidCommID] = Put[String].contramap(_.value)

  given protocolStateGet: Get[ProtocolState] = Get[String].map(ProtocolState.valueOf)
  given protocolStatePut: Put[ProtocolState] = Put[String].contramap(_.toString)

  given Get[CredentialFormat] = Get[String].temap(str =>
    CredentialFormat.fromString(str) match {
      case None      => Left(s"Fail to parce CredentialFormat from '$str'")
      case Some(obj) => Right(obj)
    }
  )
  given Put[CredentialFormat] = Put[String].contramap(_.toString)

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

  given failureGet: Get[Failure] = Get[String].temap(_.fromJson[FailureInfo])
  given failurePut: Put[Failure] = Put[String].contramap(_.asFailureInfo.toJson)

  override def createPresentationRecord(record: PresentationRecord): URIO[WalletAccessContext, Unit] = {
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
        |   credential_format,
        |   request_presentation_data,
        |   credentials_to_use,
        |   anoncred_credentials_to_use_json_schema_id,
        |   anoncred_credentials_to_use,
        |   sd_jwt_claims_to_use_json_schema_id,
        |   sd_jwt_claims_to_disclose,
        |   meta_retries,
        |   meta_next_retry,
        |   meta_last_failure,
        |   wallet_id
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
        |   ${record.credentialFormat},
        |   ${record.requestPresentationData},
        |   ${record.credentialsToUse.map(_.toList)},
        |   ${record.anoncredCredentialsToUseJsonSchemaId},
        |   ${record.anoncredCredentialsToUse},
        |   ${record.sdJwtClaimsToUseJsonSchemaId},
        |   ${record.sdJwtClaimsToDisclose},
        |   ${record.metaRetries},
        |   ${record.metaNextRetry},
        |   ${record.metaLastFailure},
        |   current_setting('app.current_wallet_id')::UUID
        | )
        """.stripMargin.update

    cxnIO.run
      .transactWallet(xa)
      .orDie
      .ensureOneAffectedRowOrDie
  }

  override def getPresentationRecords(
      ignoreWithZeroRetries: Boolean
  ): URIO[WalletAccessContext, Seq[PresentationRecord]] = {
    val conditionFragment = Fragments.whereAndOpt(
      Option.when(ignoreWithZeroRetries)(fr"meta_retries > 0")
    )
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
        |   credential_format,
        |   request_presentation_data,
        |   propose_presentation_data,
        |   presentation_data,
        |   credentials_to_use,
        |   anoncred_credentials_to_use_json_schema_id,
        |   anoncred_credentials_to_use,
        |   sd_jwt_claims_to_use_json_schema_id,
        |   sd_jwt_claims_to_disclose,
        |   meta_retries,
        |   meta_next_retry,
        |   meta_last_failure
        | FROM
        |   public.presentation_records
        | $conditionFragment
        | ORDER BY created_at
        """.stripMargin
      .query[PresentationRecord]
      .to[Seq]

    cxnIO
      .transactWallet(xa)
      .orDie
  }

  private def getRecordsByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: PresentationRecord.ProtocolState*
  ): ConnectionIO[Seq[PresentationRecord]] = {
    states match
      case Nil =>
        connection.pure(Nil)
      case head +: tail =>
        val nel = NonEmptyList.of(head, tail*)
        val inClauseFragment = Fragments.in(fr"protocol_state", nel)
        val conditionFragment = Fragments.whereAndOpt(
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
            |   connection_id,
            |   role,
            |   subject_id,
            |   protocol_state,
            |   credential_format,
            |   request_presentation_data,
            |   propose_presentation_data,
            |   presentation_data,
            |   credentials_to_use,
            |   anoncred_credentials_to_use_json_schema_id,
            |   anoncred_credentials_to_use,
            |   sd_jwt_claims_to_use_json_schema_id,
            |   sd_jwt_claims_to_disclose,
            |   meta_retries,
            |   meta_next_retry,
            |   meta_last_failure
            | FROM public.presentation_records
            | $conditionFragment
            | ORDER BY created_at
            | LIMIT $limit
            """.stripMargin
          .query[PresentationRecord]
          .to[Seq]
        cxnIO
  }
  override def getPresentationRecordsByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: PresentationRecord.ProtocolState*
  ): URIO[WalletAccessContext, Seq[PresentationRecord]] = {
    getRecordsByStates(ignoreWithZeroRetries, limit, states*).transactWallet(xa).orDie
  }
  override def getPresentationRecordsByStatesForAllWallets(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: PresentationRecord.ProtocolState*
  ): UIO[Seq[PresentationRecord]] = {
    getRecordsByStates(ignoreWithZeroRetries, limit, states*).transact(xb).orDie
  }

  override def findPresentationRecord(recordId: DidCommID): URIO[WalletAccessContext, Option[PresentationRecord]] = {
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
        |   credential_format,
        |   request_presentation_data,
        |   propose_presentation_data,
        |   presentation_data,
        |   credentials_to_use,
        |   anoncred_credentials_to_use_json_schema_id,
        |   anoncred_credentials_to_use,
        |   sd_jwt_claims_to_use_json_schema_id,
        |   sd_jwt_claims_to_disclose,
        |   meta_retries,
        |   meta_next_retry,
        |   meta_last_failure
        | FROM public.presentation_records
        | WHERE id = $recordId
        """.stripMargin
      .query[PresentationRecord]
      .option

    cxnIO
      .transactWallet(xa)
      .orDie
  }

  override def findPresentationRecordByThreadId(
      thid: DidCommID
  ): URIO[WalletAccessContext, Option[PresentationRecord]] = {
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
        |   credential_format,
        |   request_presentation_data,
        |   propose_presentation_data,
        |   presentation_data,
        |   credentials_to_use,
        |   anoncred_credentials_to_use_json_schema_id,
        |   anoncred_credentials_to_use,
        |   sd_jwt_claims_to_use_json_schema_id,
        |   sd_jwt_claims_to_disclose,
        |   meta_retries,
        |   meta_next_retry,
        |   meta_last_failure
        | FROM public.presentation_records
        | WHERE thid = $thid
        """.stripMargin
      .query[PresentationRecord]
      .option

    cxnIO
      .transactWallet(xa)
      .orDie
  }

  override def updatePresentationRecordProtocolState(
      recordId: DidCommID,
      from: PresentationRecord.ProtocolState,
      to: PresentationRecord.ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
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
      .transactWallet(xa)
      .orDie
      .ensureOneAffectedRowOrDie
  }

  override def updateWithRequestPresentation(
      recordId: DidCommID,
      request: RequestPresentation,
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
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
      .transactWallet(xa)
      .orDie
      .ensureOneAffectedRowOrDie
  }

  override def updateWithProposePresentation(
      recordId: DidCommID,
      propose: ProposePresentation,
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
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
      .transactWallet(xa)
      .orDie
      .ensureOneAffectedRowOrDie
  }

  override def updateWithPresentation(
      recordId: DidCommID,
      presentation: Presentation,
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
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
      .transactWallet(xa)
      .orDie
      .ensureOneAffectedRowOrDie
  }

  override def updateAfterFail(
      recordId: DidCommID,
      failReason: Option[Failure]
  ): UIO[Unit] = {
    val cxnIO = sql"""
        | UPDATE public.presentation_records
        | SET
        |   meta_retries = CASE WHEN (meta_retries > 1) THEN meta_retries - 1 ELSE 0 END,
        |   meta_next_retry = CASE WHEN (meta_retries > 1) THEN ${Instant.now().plusSeconds(60)} ELSE null END,
        |   meta_last_failure = ${failReason}
        | WHERE
        |   id = $recordId
        """.stripMargin.update
    cxnIO.run
      .transactWithoutContext(xa)
      .orDie
      .ensureOneAffectedRowOrDie
  }

}

object JdbcPresentationRepository {
  val maxRetries = 5 // TODO Move to config
  val layer: URLayer[Transactor[ContextAwareTask] & Transactor[Task], PresentationRepository] =
    ZLayer.fromFunction(new JdbcPresentationRepository(_, _, maxRetries))
}
