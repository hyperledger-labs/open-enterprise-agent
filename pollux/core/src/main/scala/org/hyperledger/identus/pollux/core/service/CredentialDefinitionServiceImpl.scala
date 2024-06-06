package org.hyperledger.identus.pollux.core.service

import org.hyperledger.identus.agent.walletapi.storage
import org.hyperledger.identus.agent.walletapi.storage.GenericSecretStorage
import org.hyperledger.identus.pollux.anoncreds.{AnoncredLib, AnoncredSchemaDef}
import org.hyperledger.identus.pollux.core.model.error.CredentialSchemaError
import org.hyperledger.identus.pollux.core.model.error.CredentialSchemaError.{CredentialSchemaParsingError, InvalidURI}
import org.hyperledger.identus.pollux.core.model.schema.`type`.anoncred.AnoncredSchemaSerDesV1
import org.hyperledger.identus.pollux.core.model.schema.validator.JsonSchemaError
import org.hyperledger.identus.pollux.core.model.schema.CredentialDefinition
import org.hyperledger.identus.pollux.core.model.schema.CredentialDefinition.{Filter, FilteredEntries}
import org.hyperledger.identus.pollux.core.model.secret.CredentialDefinitionSecret
import org.hyperledger.identus.pollux.core.repository.CredentialDefinitionRepository
import org.hyperledger.identus.pollux.core.repository.Repository.SearchQuery
import org.hyperledger.identus.pollux.core.service.serdes.{
  PrivateCredentialDefinitionSchemaSerDesV1,
  ProofKeyCredentialDefinitionSchemaSerDesV1,
  PublicCredentialDefinitionSerDesV1
}
import org.hyperledger.identus.pollux.core.service.CredentialDefinitionService.Error.*
import zio.*
import zio.ZIO.getOrFailWith

import java.net.URI
import java.util.UUID
import scala.util.Try

class CredentialDefinitionServiceImpl(
    genericSecretStorage: GenericSecretStorage,
    credentialDefinitionRepository: CredentialDefinitionRepository,
    uriDereferencer: URIDereferencer
) extends CredentialDefinitionService {

  override def create(in: CredentialDefinition.Input): Result[CredentialDefinition] = {
    for {
      uri <- ZIO.attempt(new URI(in.schemaId)).mapError(error => InvalidURI(in.schemaId)).orDieAsUnmanagedFailure
      content <- uriDereferencer.dereference(uri).orDieAsUnmanagedFailure
      anoncredSchema <- AnoncredSchemaSerDesV1.schemaSerDes
        .deserialize(content)
        .mapError(error => CredentialSchemaParsingError(error.error))
        .orDieAsUnmanagedFailure
      anoncredLibSchema =
        AnoncredSchemaDef(
          in.schemaId,
          anoncredSchema.version,
          anoncredSchema.attrNames,
          anoncredSchema.issuerId
        )
      anoncredLibCredentialDefinition <-
        ZIO
          .fromEither(
            Try(
              AnoncredLib.createCredDefinition(
                in.author,
                anoncredLibSchema,
                in.tag,
                in.supportRevocation
              )
            ).toEither
          )
          .mapError(t => CredentialDefinitionCreationError(t.getMessage))
      publicCredentialDefinitionJson <-
        PublicCredentialDefinitionSerDesV1.schemaSerDes.deserializeAsJson(
          anoncredLibCredentialDefinition.cd.data
        )
      privateCredentialDefinitionJson <-
        PrivateCredentialDefinitionSchemaSerDesV1.schemaSerDes.deserializeAsJson(
          anoncredLibCredentialDefinition.cdPrivate.data
        )
      proofKeyCredentialDefinitionJson <-
        ProofKeyCredentialDefinitionSchemaSerDesV1.schemaSerDes.deserializeAsJson(
          anoncredLibCredentialDefinition.proofKey.data
        )
      cd <-
        CredentialDefinition.make(
          in,
          PublicCredentialDefinitionSerDesV1.version,
          publicCredentialDefinitionJson,
          ProofKeyCredentialDefinitionSchemaSerDesV1.version,
          proofKeyCredentialDefinitionJson
        )
      createdCredentialDefinition <- credentialDefinitionRepository
        .create(cd)
        .mapError(t => CredentialDefinitionCreationError(t.getMessage))
      _ <- genericSecretStorage
        .set(
          createdCredentialDefinition.guid,
          CredentialDefinitionSecret(privateCredentialDefinitionJson)
        )
        .mapError(t =>
          CredentialDefinitionCreationError(s"An error occurred while storing the CredDef secret: ${t.getMessage}")
        )
    } yield createdCredentialDefinition
  }.mapError {
    case e: JsonSchemaError                   => CredentialDefinitionParsingError(e.error)
    case e: CredentialDefinitionService.Error => e
  }

  override def delete(guid: UUID): Result[CredentialDefinition] =
    for {
      deleted_row_opt <- credentialDefinitionRepository
        .delete(guid)
        .mapError(RepositoryError.apply)
      deleted_row <- deleted_row_opt match
        case None        => ZIO.fail(GuidNotFoundError(guid))
        case Some(value) => ZIO.succeed(value)
    } yield deleted_row

  override def lookup(filter: CredentialDefinition.Filter, skip: Int, limit: Int): Result[FilteredEntries] = {
    credentialDefinitionRepository
      .search(SearchQuery(filter, skip, limit))
      .map(sr => FilteredEntries(sr.entries, sr.count.toInt, sr.totalCount.toInt))
  }

  override def getByGUID(guid: UUID): IO[CredentialDefinitionService.Error, CredentialDefinition] = {
    credentialDefinitionRepository
      .getByGuid(guid)
      .mapError[CredentialDefinitionService.Error](t => RepositoryError(t))
      .flatMap {
        case None        => ZIO.fail(GuidNotFoundError(guid))
        case Some(value) => ZIO.succeed(value)
      }
  }
}

object CredentialDefinitionServiceImpl {
  val layer: URLayer[
    GenericSecretStorage & CredentialDefinitionRepository & URIDereferencer,
    CredentialDefinitionService
  ] =
    ZLayer.fromFunction(CredentialDefinitionServiceImpl(_, _, _))
}
