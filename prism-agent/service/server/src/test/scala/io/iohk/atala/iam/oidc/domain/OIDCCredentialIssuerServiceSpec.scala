package io.iohk.atala.iam.oidc.domain

import io.iohk.atala.agent.walletapi.memory.GenericSecretStorageInMemory
import io.iohk.atala.agent.walletapi.service.{ManagedDIDService, MockManagedDIDService}
import io.iohk.atala.agent.walletapi.storage.{DIDNonSecretStorage, MockDIDNonSecretStorage}
import io.iohk.atala.castor.core.model.did.VerificationRelationship
import io.iohk.atala.castor.core.service.{DIDService, MockDIDService}
import io.iohk.atala.iam.oidc.http.{ClaimDescriptor, CredentialDefinition, Localization}
import io.iohk.atala.pollux.core.repository.{CredentialRepository, CredentialRepositoryInMemory}
import io.iohk.atala.pollux.core.service.*
import io.iohk.atala.pollux.vc.jwt.PrismDidResolver
import io.iohk.atala.shared.models.{WalletAccessContext, WalletId}
import zio.mock.MockSpecDefault
import zio.test.*
import zio.test.Assertion.*
import zio.{URLayer, ZIO, ZLayer}

object OIDCCredentialIssuerServiceSpec extends MockSpecDefault with CredentialServiceSpecHelper {

  val layers: URLayer[
    DIDService & ManagedDIDService & DIDNonSecretStorage,
    CredentialService & CredentialDefinitionService & OIDCCredentialIssuerService
  ] =
    ZLayer.makeSome[
      DIDService & ManagedDIDService & DIDNonSecretStorage,
      CredentialService & CredentialDefinitionService & OIDCCredentialIssuerService
    ](
      CredentialRepositoryInMemory.layer,
      PrismDidResolver.layer,
      credentialDefinitionServiceLayer,
      GenericSecretStorageInMemory.layer,
      LinkSecretServiceImpl.layer,
      CredentialServiceImpl.layer,
      OIDCCredentialIssuerServiceImpl.layer
    )

  override def spec = suite("CredentialServiceImpl")(
    OIDCCredentialIssuerServiceSpec
  )

  private val (issuerOp, issuerKp, issuerDidMetadata, issuerDidData) =
    MockDIDService.createDID(VerificationRelationship.AssertionMethod)

//  private val (holderOp, holderKp, holderDidMetadata, holderDidData) =
//    MockDIDService.createDID(VerificationRelationship.Authentication)
//
//  private val holderDidServiceExpectations =
//    MockDIDService.resolveDIDExpectation(holderDidMetadata, holderDidData)

  private val issuerDidServiceExpectations =
    MockDIDService.resolveDIDExpectation(issuerDidMetadata, issuerDidData)

  private val issuerManagedDIDServiceExpectations =
    MockManagedDIDService.javaKeyPairWithDIDExpectation(issuerKp)

  private val getIssuerPrismDidWalletIdExpectation =
    MockDIDNonSecretStorage.getPrismDidWalletIdExpectation(issuerDidData.id, WalletId.default)

  private val OIDCCredentialIssuerServiceSpec =
    suite("Simple JWT credential issuance")(
      test("should issue a JWT credential") {
        for {
          oidcCredentialIssuerService <- ZIO.service[OIDCCredentialIssuerService]
          credentialDefinition = CredentialDefinition(
            `@context` = Seq("https://www.w3.org/2018/credentials/v1"),
            `type` = Seq("VerifiableCredential", "CertificateOfBirth"),
            credentialSubject = Some(
              Map(
                "name" ->
                  ClaimDescriptor(mandatory = Some(true), valueType = Some("string"), display = Seq.empty[Localization])
              )
            )
          )
          jwt <- oidcCredentialIssuerService.issueJwtCredential(issuerDidData.id, None, credentialDefinition)
          _ <- zio.Console.printLine(jwt)
        } yield assert(jwt.toString)(isNonEmptyString)
      }.provideSomeLayer(
        issuerDidServiceExpectations.toLayer ++
          issuerManagedDIDServiceExpectations.toLayer ++
          getIssuerPrismDidWalletIdExpectation.toLayer >+> layers
      )
    )
}
