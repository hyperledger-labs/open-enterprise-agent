package org.hyperledger.identus.pollux.core.service

import org.hyperledger.identus.agent.walletapi.storage.GenericSecretStorage
import org.hyperledger.identus.mercury.model.{AttachmentDescriptor, Base64, DidId}
import org.hyperledger.identus.mercury.protocol.issuecredential.{IssueCredential, IssueCredentialIssuedFormat}
import org.hyperledger.identus.mercury.protocol.presentproof.*
import org.hyperledger.identus.pollux.anoncreds.*
import org.hyperledger.identus.pollux.core.model.*
import org.hyperledger.identus.pollux.core.model.error.PresentationError.*
import org.hyperledger.identus.pollux.core.model.presentation.Options
import org.hyperledger.identus.pollux.core.model.schema.CredentialDefinition.Input
import org.hyperledger.identus.pollux.core.model.secret.CredentialDefinitionSecret
import org.hyperledger.identus.pollux.core.repository.{CredentialRepository, PresentationRepository}
import org.hyperledger.identus.pollux.core.service.serdes.{
  AnoncredCredentialProofV1,
  AnoncredCredentialProofsV1,
  AnoncredPresentationRequestV1,
  AnoncredPresentationV1
}
import org.hyperledger.identus.shared.models.{WalletAccessContext, WalletId}
import zio.*
import zio.json.{DecoderOps, EncoderOps}
import zio.test.*
import zio.test.Assertion.*

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import java.time.{Instant, OffsetDateTime}
import java.util.{Base64 as JBase64, UUID}

object PresentationServiceSpec extends ZIOSpecDefault with PresentationServiceSpecHelper {

  override def spec: Spec[Any, Any] =
    suite("PresentationService")(singleWalletSpec, multiWalletSpec).provide(
      presentationServiceLayer ++ genericSecretStorageLayer
    )

  private val singleWalletSpec =
    suite("singleWalletSpec")(
      test("createPresentationRecord creates a valid JWT PresentationRecord") {
        val didGen = for {
          suffix <- Gen.stringN(10)(Gen.alphaNumericChar)
        } yield DidId("did:peer:" + suffix)

        val proofTypeGen = for {
          schemaId <- Gen.stringN(10)(Gen.alphaChar)
          requiredFields <- Gen.listOfBounded(1, 5)(Gen.stringN(10)(Gen.alphaChar)).map(Some(_))
          trustIssuers <- Gen.listOfBounded(1, 5)(didGen).map(Some(_))
        } yield ProofType(schemaId, requiredFields, trustIssuers)

        val optionsGen = for {
          challenge <- Gen.stringN(10)(Gen.alphaNumericChar)
          domain <- Gen.stringN(10)(Gen.alphaNumericChar)
        } yield Options(challenge, domain)

        check(
          Gen.uuid.map(e => DidCommID(e.toString)),
          Gen.string,
          Gen.listOfBounded(1, 5)(proofTypeGen),
          Gen.option(optionsGen)
        ) { (thid, connectionId, proofTypes, options) =>
          for {
            svc <- ZIO.service[PresentationService]
            pairwiseVerifierDid = DidId("did:peer:Verifier")
            pairwiseProverDid = DidId("did:peer:Prover")
            record <- svc.createJwtPresentationRecord(
              pairwiseVerifierDid,
              Some(pairwiseProverDid),
              thid,
              Some(connectionId),
              proofTypes,
              options,
              PresentCredentialRequestFormat.JWT,
              None,
              None,
              None,
            )
          } yield {
            assertTrue(record.thid == thid) &&
            assertTrue(record.updatedAt.isEmpty) &&
            assertTrue(record.connectionId.contains(connectionId)) &&
            assertTrue(record.role == PresentationRecord.Role.Verifier) &&
            assertTrue(record.protocolState == PresentationRecord.ProtocolState.RequestPending) &&
            assertTrue(record.requestPresentationData.isDefined) &&
            assertTrue(record.requestPresentationData.get.to.contains(pairwiseProverDid)) &&
            assertTrue(record.requestPresentationData.get.thid.contains(thid.toString)) &&
            assertTrue(record.requestPresentationData.get.body.goal_code.contains("Request Proof Presentation")) &&
            assertTrue(record.requestPresentationData.get.body.proof_types == proofTypes) &&
            assertTrue(
              if (record.requestPresentationData.get.attachments.nonEmpty) {
                val maybePresentationOptions =
                  record.requestPresentationData.get.attachments.headOption
                    .map(attachment =>
                      attachment.data.toJson
                        .fromJson[org.hyperledger.identus.mercury.model.JsonData]
                        .flatMap(data =>
                          org.hyperledger.identus.pollux.core.model.presentation.PresentationAttachment.given_JsonDecoder_PresentationAttachment
                            .decodeJson(data.json.toJson)
                            .map(_.options)
                        )
                    )
                    .get
                maybePresentationOptions
                  .map(
                    _ == options
                  )
                  .getOrElse(false)
              } else true
            ) &&
            assertTrue(record.proposePresentationData.isEmpty) &&
            assertTrue(record.presentationData.isEmpty) &&
            assertTrue(record.credentialsToUse.isEmpty)
          }
        }
      },
      test("createPresentationRecord creates a valid Anoncred PresentationRecord") {
        check(
          Gen.uuid.map(e => DidCommID(e.toString)),
          Gen.string,
          Gen.string,
          Gen.string,
          Gen.string
        ) { (thid, connectionId, name, nonce, version) =>
          for {
            svc <- ZIO.service[PresentationService]
            pairwiseVerifierDid = DidId("did:peer:Verifier")
            pairwiseProverDid = DidId("did:peer:Prover")
            anoncredPresentationRequestV1 = AnoncredPresentationRequestV1(
              Map.empty,
              Map.empty,
              name,
              nonce,
              version,
              None
            )
            record <-
              svc.createAnoncredPresentationRecord(
                pairwiseVerifierDid,
                Some(pairwiseProverDid),
                thid,
                Some(connectionId),
                anoncredPresentationRequestV1,
                PresentCredentialRequestFormat.Anoncred,
                None,
                None,
                None
              )
          } yield {
            assertTrue(record.thid == thid) &&
            assertTrue(record.updatedAt.isEmpty) &&
            assertTrue(record.connectionId.contains(connectionId)) &&
            assertTrue(record.role == PresentationRecord.Role.Verifier) &&
            assertTrue(record.protocolState == PresentationRecord.ProtocolState.RequestPending) &&
            assertTrue(record.requestPresentationData.isDefined) &&
            assertTrue(record.requestPresentationData.get.to.contains(pairwiseProverDid)) &&
            assertTrue(record.requestPresentationData.get.thid.contains(thid.toString)) &&
            assertTrue(record.requestPresentationData.get.body.goal_code.contains("Request Proof Presentation")) &&
            assertTrue(
              record.requestPresentationData.get.attachments.map(_.media_type) == Seq(Some("application/json"))
            ) &&
            assertTrue(
              record.requestPresentationData.get.attachments.map(_.format) == Seq(
                Some(PresentCredentialRequestFormat.Anoncred.name)
              )
            ) &&
            assertTrue(
              record.requestPresentationData.get.attachments.map(_.data) ==
                Seq(
                  Base64(
                    JBase64.getUrlEncoder.encodeToString(
                      AnoncredPresentationRequestV1.schemaSerDes
                        .serializeToJsonString(anoncredPresentationRequestV1)
                        .getBytes()
                    )
                  )
                )
            ) &&
            assertTrue(record.proposePresentationData.isEmpty) &&
            assertTrue(record.presentationData.isEmpty) &&
            assertTrue(record.credentialsToUse.isEmpty)
          }
        }
      },
      test("getPresentationRecords returns created PresentationRecord") {
        for {
          svc <- ZIO.service[PresentationService]
          _ <- svc.createJwtRecord()
          _ <- svc.createJwtRecord()
          records <- svc.getPresentationRecords(false)
        } yield {
          assertTrue(records.size == 2)
        }
      },
      test("getPresentationRecordsByStates returns the correct records") {
        for {
          svc <- ZIO.service[PresentationService]
          aRecord <- svc.createJwtRecord()
          records <- svc.getPresentationRecordsByStates(
            ignoreWithZeroRetries = true,
            limit = 10,
            PresentationRecord.ProtocolState.RequestPending
          )
          onePending = assertTrue(records.size == 1) && assertTrue(records.contains(aRecord))
          records <- svc.getPresentationRecordsByStates(
            ignoreWithZeroRetries = true,
            limit = 10,
            PresentationRecord.ProtocolState.RequestSent
          )
          zeroSent = assertTrue(records.isEmpty)
        } yield onePending && zeroSent
      },
      test("getPresentationRecord returns the correct record") {
        for {
          svc <- ZIO.service[PresentationService]
          _ <- svc.createJwtRecord()
          bRecord <- svc.createJwtRecord()
          record <- svc.findPresentationRecord(bRecord.id)
        } yield assertTrue(record.contains(bRecord))
      },
      test("getPresentationRecord returns nothing for an unknown 'recordId'") {
        for {
          svc <- ZIO.service[PresentationService]
          _ <- svc.createJwtRecord()
          _ <- svc.createJwtRecord()
          record <- svc.findPresentationRecord(DidCommID())
        } yield assertTrue(record.isEmpty)
      },
      test("createJwtPresentationPayloadFromRecord returns jwt presentation payload") {
        for {
          repo <- ZIO.service[CredentialRepository]
          aIssueCredentialRecord = issueCredentialRecord(CredentialFormat.JWT)
          _ <- repo.create(aIssueCredentialRecord)
          rawCredentialData =
            """{"base64":"ZXlKaGJHY2lPaUpGVXpJMU5rc2lMQ0owZVhBaU9pSktWMVFpZlEuZXlKcFlYUWlPakUyTnprek1qYzROaklzSW1GMVpDSTZJbVJ2YldGcGJpSXNJbTV2Ym1ObElqb2lZMlk1T1RJMk56Z3RPREV3TmkwME1EZzVMV0UxWXprdE5tTmhObU0wWkRBMU1HVTBJaXdpZG5BaU9uc2lRR052Ym5SbGVIUWlPbHNpYUhSMGNITTZMeTkzZDNjdWR6TXViM0puTHpJd01UZ3ZjSEpsYzJWdWRHRjBhVzl1Y3k5Mk1TSmRMQ0owZVhCbElqcGJJbFpsY21sbWFXRmliR1ZRY21WelpXNTBZWFJwYjI0aVhYMHNJbWx6Y3lJNkltUnBaRHB3Y21semJUcGhaR0psT1RJNE9XUXdZelZtWWpVMlptWmhOVEF6T0Rka01UZ3dOR0ZpTkdFeE5UYzJOVEkzWXprME5tRTFNalV5T0RFM1ptRTRaVGhoTW1OalpXUXdPa056YzBKRGMyZENSVzFKUzBSWE1XaGpNMUpzWTJsb2NHSnRVbXhsUTJ0UlFWVktVRU5uYkZSYVYwNTNUV3BWTW1GNlJWTkpSUzFNYVVkTU0xRklaRlZ1VG10d1dXSkthSE5VYTIxWVVGaEpVM0ZXZWpjMll6RlZPWGhvVURseWNFZHBSSEZXTlRselJYcEtWbEpEYWxJMGEwMHdaMGg0YkhWUU5tVk5Ta2wwZHpJMk4yWllWbEpoTUhoRE5XaEthVU5uTVhSWldFNHdXbGhKYjJGWE5XdGFXR2R3UlVGU1ExUjNiMHBWTWxacVkwUkpNVTV0YzNoRmFVSlFhVFJvYVRrd1FqTldTbnBhUzFkSGVWbGlSVFZLYkhveGVVVnhiR010TFc1T1ZsQmpXVlJmWVRaU2IyYzJiR1ZtWWtKTmVWWlZVVzh3WlVwRVRrbENPRnBpYWkxdWFrTlRUR05PZFhVek1URlZWM1JOVVhWWkluMC5CcmFpbEVXa2VlSXhWbjY3dnpkVHZGTXpBMV9oNzFoaDZsODBHRFBpbkRaVVB4ajAxSC0tUC1QZDIxTk9wRDd3am51SDkxdUNBOFZMUW9fS2FnVjlnQQo="}"""
          issueCredential <- ZIO.fromEither(
            IssueCredential.makeIssueCredentialFromRequestCredential(requestCredential.makeMessage)
          )
          _ <- repo.updateWithIssuedRawCredential(
            aIssueCredentialRecord.id,
            issueCredential,
            rawCredentialData,
            None,
            None,
            IssueCredentialRecord.ProtocolState.CredentialReceived
          )
          svc <- ZIO.service[PresentationService]
          aRecord <- svc.createJwtRecord()
          repo <- ZIO.service[PresentationRepository]
          _ <- repo.updatePresentationWithCredentialsToUse(
            aRecord.id,
            Some(Seq(aIssueCredentialRecord.id.value)),
            PresentationRecord.ProtocolState.RequestPending
          )
          issuer = createIssuer("did:prism:issuer")
          aPresentationPayload <- svc.createJwtPresentationPayloadFromRecord(aRecord.id, issuer, Instant.now())
        } yield {
          assertTrue(aPresentationPayload.toJwtPresentationPayload.iss == "did:prism:issuer")
        }
      },
      test("createAnoncredPresentationPayloadFromRecord returns Anoncred presentation payload") {
        for {
          presentationWithRecord <- createAnoncredPresentation
          (presentation, _) = presentationWithRecord
          serializedPresentation <- presentation.attachments.head.data match {
            case Base64(data) => ZIO.succeed(AnoncredPresentation(new String(JBase64.getUrlDecoder.decode(data))))
            case _            => ZIO.fail(InvalidAnoncredPresentation("Expecting Base64-encoded data"))
          }
          validation <- AnoncredPresentationV1.schemaSerDes.validate(serializedPresentation.data)
          presentation <- AnoncredPresentationV1.schemaSerDes.deserialize(serializedPresentation.data)
        } yield {
          assert(validation)(isUnit)
          assert(
            presentation.proof.proofs.headOption.flatMap(_.primary_proof.eq_proof.revealed_attrs.headOption.map(_._1))
          )(isSome(equalTo("sex")))
        }
      },
      test("verify anoncred presentation") {
        for {
          presentationWithRecord <- createAnoncredPresentation
          (presentation, aRecord) = presentationWithRecord
          svc <- ZIO.service[PresentationService]
          _ <- svc.receivePresentation(presentation)
          validateRecord <-
            svc.verifyAnoncredPresentation(
              presentation,
              aRecord.requestPresentationData.get,
              aRecord.id
            )
        } yield {
          assert(validateRecord.protocolState)(equalTo(PresentationRecord.ProtocolState.PresentationVerified))
        }
      },
      test("markRequestPresentationSent returns updated PresentationRecord") {
        for {
          svc <- ZIO.service[PresentationService]
          record <- svc.createJwtRecord()
          record <- svc.markRequestPresentationSent(record.id)

        } yield {
          assertTrue(record.protocolState == PresentationRecord.ProtocolState.RequestSent)
        }
      },
      test("markRequestPresentationRejected returns updated PresentationRecord") {
        for {
          svc <- ZIO.service[PresentationService]
          record <- svc.createJwtRecord()
          repo <- ZIO.service[PresentationRepository]
          _ <- repo.updatePresentationRecordProtocolState(
            record.id,
            PresentationRecord.ProtocolState.RequestPending,
            PresentationRecord.ProtocolState.RequestReceived
          )
          record <- svc.markRequestPresentationRejected(record.id)

        } yield {
          assertTrue(record.protocolState == PresentationRecord.ProtocolState.RequestRejected)
        }
      },
      test("receiveRequestPresentation with a MissingCredentialFormat") {
        for {
          svc <- ZIO.service[PresentationService]
          connectionId = Some("connectionId")
          body = RequestPresentation.Body(goal_code = Some("Presentation Request"))
          presentationAttachmentAsJson = """{
                "challenge": "1f44d55f-f161-4938-a659-f8026467f126",
                "domain": "us.gov/DriverLicense",
                "credential_manifest": {}
            }"""
          prover = Some(DidId("did:peer:Prover"))
          verifier = Some(DidId("did:peer:Verifier"))

          attachmentDescriptor = AttachmentDescriptor.buildJsonAttachment(
            payload = presentationAttachmentAsJson,
          )
          requestPresentation = RequestPresentation(
            body = body,
            attachments = Seq(attachmentDescriptor),
            to = prover,
            from = verifier,
          )
          result <- svc.receiveRequestPresentation(connectionId, requestPresentation).exit

        } yield assert(result)(
          fails(equalTo(MissingCredentialFormat))
        )
      },
      test("receiveRequestPresentation with a UnsupportedCredentialFormat") {
        for {
          svc <- ZIO.service[PresentationService]
          connectionId = Some("connectionId")
          body = RequestPresentation.Body(goal_code = Some("Presentation Request"))
          presentationAttachmentAsJson = """{
                "challenge": "1f44d55f-f161-4938-a659-f8026467f126",
                "domain": "us.gov/DriverLicense",
                "credential_manifest": {}
            }"""
          prover = Some(DidId("did:peer:Prover"))
          verifier = Some(DidId("did:peer:Verifier"))

          attachmentDescriptor = AttachmentDescriptor.buildJsonAttachment(
            payload = presentationAttachmentAsJson,
            format = Some("Some/UnsupportedCredentialFormat")
          )
          requestPresentation = RequestPresentation(
            body = body,
            attachments = Seq(attachmentDescriptor),
            to = prover,
            from = verifier,
          )
          result <- svc.receiveRequestPresentation(connectionId, requestPresentation).exit

        } yield assert(result)(
          fails(equalTo(UnsupportedCredentialFormat(vcFormat = "Some/UnsupportedCredentialFormat")))
        )
      },
      test("receiveRequestPresentation JWT updates the RequestPresentation in PresentationRecord") {
        for {
          svc <- ZIO.service[PresentationService]
          connectionId = Some("connectionId")
          body = RequestPresentation.Body(goal_code = Some("Presentation Request"))
          presentationAttachmentAsJson = """{
                "challenge": "1f44d55f-f161-4938-a659-f8026467f126",
                "domain": "us.gov/DriverLicense",
                "credential_manifest": {}
            }"""
          prover = Some(DidId("did:peer:Prover"))
          verifier = Some(DidId("did:peer:Verifier"))

          attachmentDescriptor = AttachmentDescriptor.buildJsonAttachment(
            payload = presentationAttachmentAsJson,
            format = Some(PresentCredentialProposeFormat.JWT.name)
          )
          requestPresentation = RequestPresentation(
            body = body,
            attachments = Seq(attachmentDescriptor),
            to = prover,
            from = verifier,
          )
          aRecord <- svc.receiveRequestPresentation(connectionId, requestPresentation)

        } yield {
          assertTrue(aRecord.connectionId == connectionId) &&
          assertTrue(aRecord.protocolState == PresentationRecord.ProtocolState.RequestReceived) &&
          assertTrue(aRecord.requestPresentationData.contains(requestPresentation))
        }
      },
      test("receiveRequestPresentation Anoncred updates the RequestPresentation in PresentationRecord") {
        val anoncredPresentationRequestV1 = AnoncredPresentationRequestV1(
          Map.empty,
          Map.empty,
          "name",
          "nonce",
          "version",
          None
        )
        val attachmentDescriptor = AttachmentDescriptor.buildBase64Attachment(
          mediaType = Some("application/json"),
          format = Some(PresentCredentialRequestFormat.Anoncred.name),
          payload =
            AnoncredPresentationRequestV1.schemaSerDes.serializeToJsonString(anoncredPresentationRequestV1).getBytes()
        )
        val connectionId = Some("connectionId")
        for {
          svc <- ZIO.service[PresentationService]
          requestPresentationWithRecord <- receiveRequestPresentationTest(attachmentDescriptor)
          (_, requestPresentation) = requestPresentationWithRecord
          aRecord <- svc.receiveRequestPresentation(connectionId, requestPresentation)

        } yield {
          assertTrue(aRecord.connectionId == connectionId) &&
          assertTrue(aRecord.protocolState == PresentationRecord.ProtocolState.RequestReceived) &&
          assertTrue(aRecord.requestPresentationData.contains(requestPresentation))
        }
      },
      test("receiveRequestPresentation Anoncred should fail given invalid attachment") {

        val presentationAttachmentAsJson =
          """{
              "challenge": "1f44d55f-f161-4938-a659-f8026467f126",
              "domain": "us.gov/DriverLicense",
              "credential_manifest": {}
          }"""

        val attachmentDescriptor = AttachmentDescriptor.buildJsonAttachment(
          payload = presentationAttachmentAsJson,
          format = Some(PresentCredentialProposeFormat.Anoncred.name)
        )
        for {
          requestPresentation <- receiveRequestPresentationTest(attachmentDescriptor).exit
        } yield assert(requestPresentation)(
          fails(isSubtype[InvalidAnoncredPresentationRequest](anything))
        )
      },
      test("receiveRequestPresentation Anoncred should fail given invalid anoncred format") {
        val presentationAttachmentAsJson =
          """{
              "challenge": "1f44d55f-f161-4938-a659-f8026467f126",
              "domain": "us.gov/DriverLicense",
              "credential_manifest": {}
          }"""
        val attachmentDescriptor = AttachmentDescriptor.buildBase64Attachment(
          mediaType = Some("application/json"),
          format = Some(PresentCredentialRequestFormat.Anoncred.name),
          payload = presentationAttachmentAsJson.getBytes()
        )
        for {
          requestPresentation <- receiveRequestPresentationTest(attachmentDescriptor).exit
        } yield assert(requestPresentation)(
          fails(isSubtype[InvalidAnoncredPresentationRequest](anything))
        )
      },
      test("acceptRequestPresentation updates the PresentationRecord JWT") {
        for {
          repo <- ZIO.service[CredentialRepository]
          aIssueCredentialRecord = issueCredentialRecord(CredentialFormat.JWT)
          _ <- repo.create(aIssueCredentialRecord)
          rawCredentialData =
            """{"base64":"ZXlKaGJHY2lPaUpGVXpJMU5rc2lMQ0owZVhBaU9pSktWMVFpZlEuZXlKcFlYUWlPakUyTnprek1qYzROaklzSW1GMVpDSTZJbVJ2YldGcGJpSXNJbTV2Ym1ObElqb2lZMlk1T1RJMk56Z3RPREV3TmkwME1EZzVMV0UxWXprdE5tTmhObU0wWkRBMU1HVTBJaXdpZG5BaU9uc2lRR052Ym5SbGVIUWlPbHNpYUhSMGNITTZMeTkzZDNjdWR6TXViM0puTHpJd01UZ3ZjSEpsYzJWdWRHRjBhVzl1Y3k5Mk1TSmRMQ0owZVhCbElqcGJJbFpsY21sbWFXRmliR1ZRY21WelpXNTBZWFJwYjI0aVhYMHNJbWx6Y3lJNkltUnBaRHB3Y21semJUcGhaR0psT1RJNE9XUXdZelZtWWpVMlptWmhOVEF6T0Rka01UZ3dOR0ZpTkdFeE5UYzJOVEkzWXprME5tRTFNalV5T0RFM1ptRTRaVGhoTW1OalpXUXdPa056YzBKRGMyZENSVzFKUzBSWE1XaGpNMUpzWTJsb2NHSnRVbXhsUTJ0UlFWVktVRU5uYkZSYVYwNTNUV3BWTW1GNlJWTkpSUzFNYVVkTU0xRklaRlZ1VG10d1dXSkthSE5VYTIxWVVGaEpVM0ZXZWpjMll6RlZPWGhvVURseWNFZHBSSEZXTlRselJYcEtWbEpEYWxJMGEwMHdaMGg0YkhWUU5tVk5Ta2wwZHpJMk4yWllWbEpoTUhoRE5XaEthVU5uTVhSWldFNHdXbGhKYjJGWE5XdGFXR2R3UlVGU1ExUjNiMHBWTWxacVkwUkpNVTV0YzNoRmFVSlFhVFJvYVRrd1FqTldTbnBhUzFkSGVWbGlSVFZLYkhveGVVVnhiR010TFc1T1ZsQmpXVlJmWVRaU2IyYzJiR1ZtWWtKTmVWWlZVVzh3WlVwRVRrbENPRnBpYWkxdWFrTlRUR05PZFhVek1URlZWM1JOVVhWWkluMC5CcmFpbEVXa2VlSXhWbjY3dnpkVHZGTXpBMV9oNzFoaDZsODBHRFBpbkRaVVB4ajAxSC0tUC1QZDIxTk9wRDd3am51SDkxdUNBOFZMUW9fS2FnVjlnQQo="}"""
          issueCredential <- ZIO.fromEither(
            IssueCredential.makeIssueCredentialFromRequestCredential(requestCredential.makeMessage)
          )
          _ <- repo.updateWithIssuedRawCredential(
            aIssueCredentialRecord.id,
            issueCredential,
            rawCredentialData,
            None,
            None,
            IssueCredentialRecord.ProtocolState.CredentialReceived
          )
          svc <- ZIO.service[PresentationService]
          connectionId = Some("connectionId")

          aRecord <- svc.receiveRequestPresentation(
            connectionId,
            requestPresentation(PresentCredentialRequestFormat.JWT)
          )
          credentialsToUse = Seq(aIssueCredentialRecord.id.value)
          updateRecord <- svc.acceptRequestPresentation(aRecord.id, credentialsToUse)

        } yield {
          assertTrue(updateRecord.connectionId == connectionId) &&
          // assertTrue(updateRecord.requestPresentationData == Some(requestPresentation)) && // FIXME: enabling them make the test fail.
          assertTrue(updateRecord.credentialsToUse.contains(credentialsToUse))
        }
      },
      test("acceptRequestPresentation updates the PresentationRecord AnonCreds") {
        for {
          repo <- ZIO.service[CredentialRepository]
          aIssueCredentialRecord = issueCredentialRecord(CredentialFormat.AnonCreds)
          _ <- repo.create(aIssueCredentialRecord)
          rawCredentialData =
            """{"base64":"ZXlKaGJHY2lPaUpGVXpJMU5rc2lMQ0owZVhBaU9pSktWMVFpZlEuZXlKcFlYUWlPakUyTnprek1qYzROaklzSW1GMVpDSTZJbVJ2YldGcGJpSXNJbTV2Ym1ObElqb2lZMlk1T1RJMk56Z3RPREV3TmkwME1EZzVMV0UxWXprdE5tTmhObU0wWkRBMU1HVTBJaXdpZG5BaU9uc2lRR052Ym5SbGVIUWlPbHNpYUhSMGNITTZMeTkzZDNjdWR6TXViM0puTHpJd01UZ3ZjSEpsYzJWdWRHRjBhVzl1Y3k5Mk1TSmRMQ0owZVhCbElqcGJJbFpsY21sbWFXRmliR1ZRY21WelpXNTBZWFJwYjI0aVhYMHNJbWx6Y3lJNkltUnBaRHB3Y21semJUcGhaR0psT1RJNE9XUXdZelZtWWpVMlptWmhOVEF6T0Rka01UZ3dOR0ZpTkdFeE5UYzJOVEkzWXprME5tRTFNalV5T0RFM1ptRTRaVGhoTW1OalpXUXdPa056YzBKRGMyZENSVzFKUzBSWE1XaGpNMUpzWTJsb2NHSnRVbXhsUTJ0UlFWVktVRU5uYkZSYVYwNTNUV3BWTW1GNlJWTkpSUzFNYVVkTU0xRklaRlZ1VG10d1dXSkthSE5VYTIxWVVGaEpVM0ZXZWpjMll6RlZPWGhvVURseWNFZHBSSEZXTlRselJYcEtWbEpEYWxJMGEwMHdaMGg0YkhWUU5tVk5Ta2wwZHpJMk4yWllWbEpoTUhoRE5XaEthVU5uTVhSWldFNHdXbGhKYjJGWE5XdGFXR2R3UlVGU1ExUjNiMHBWTWxacVkwUkpNVTV0YzNoRmFVSlFhVFJvYVRrd1FqTldTbnBhUzFkSGVWbGlSVFZLYkhveGVVVnhiR010TFc1T1ZsQmpXVlJmWVRaU2IyYzJiR1ZtWWtKTmVWWlZVVzh3WlVwRVRrbENPRnBpYWkxdWFrTlRUR05PZFhVek1URlZWM1JOVVhWWkluMC5CcmFpbEVXa2VlSXhWbjY3dnpkVHZGTXpBMV9oNzFoaDZsODBHRFBpbkRaVVB4ajAxSC0tUC1QZDIxTk9wRDd3am51SDkxdUNBOFZMUW9fS2FnVjlnQQo="}"""
          issueCredential <- ZIO.fromEither(
            IssueCredential.makeIssueCredentialFromRequestCredential(requestCredential.makeMessage)
          )
          _ <- repo.updateWithIssuedRawCredential(
            aIssueCredentialRecord.id,
            issueCredential,
            rawCredentialData,
            Some(List("SchemaId")),
            Some("CredDefId"),
            IssueCredentialRecord.ProtocolState.CredentialReceived
          )
          svc <- ZIO.service[PresentationService]
          connectionId = Some("connectionId")
          anoncredPresentationRequestV1 = AnoncredPresentationRequestV1(
            Map.empty,
            Map.empty,
            "name",
            "nonce",
            "version",
            None
          )
          attachmentDescriptor = AttachmentDescriptor.buildBase64Attachment(
            mediaType = Some("application/json"),
            format = Some(PresentCredentialRequestFormat.Anoncred.name),
            payload =
              AnoncredPresentationRequestV1.schemaSerDes.serializeToJsonString(anoncredPresentationRequestV1).getBytes()
          )
          requestPresentation = RequestPresentation(
            body = RequestPresentation.Body(goal_code = Some("Presentation Request")),
            attachments = Seq(attachmentDescriptor),
            to = Some(DidId("did:peer:Prover")),
            from = Some(DidId("did:peer:Verifier")),
          )
          aRecord <- svc.receiveRequestPresentation(connectionId, requestPresentation)
          credentialsToUse =
            AnoncredCredentialProofsV1(
              List(
                AnoncredCredentialProofV1(
                  aIssueCredentialRecord.id.value,
                  Seq("requestedAttribute"),
                  Seq("requestedPredicate")
                )
              )
            )
          anoncredCredentialProofsJson <- ZIO.fromEither(
            AnoncredCredentialProofsV1.schemaSerDes.serialize(credentialsToUse)
          )
          updateRecord <- svc.acceptAnoncredRequestPresentation(aRecord.id, credentialsToUse)

        } yield {
          assertTrue(updateRecord.connectionId == connectionId) &&
          assertTrue(updateRecord.anoncredCredentialsToUse.contains(anoncredCredentialProofsJson)) &&
          assertTrue(updateRecord.anoncredCredentialsToUseJsonSchemaId.contains(AnoncredCredentialProofsV1.version))
        }
      },
      test("acceptRequestPresentation should fail given unmatching format") {
        for {
          repo <- ZIO.service[CredentialRepository]
          aIssueCredentialRecord = issueCredentialRecord(CredentialFormat.JWT)
          _ <- repo.create(aIssueCredentialRecord)
          rawCredentialData =
            """{"base64":"ZXlKaGJHY2lPaUpGVXpJMU5rc2lMQ0owZVhBaU9pSktWMVFpZlEuZXlKcFlYUWlPakUyTnprek1qYzROaklzSW1GMVpDSTZJbVJ2YldGcGJpSXNJbTV2Ym1ObElqb2lZMlk1T1RJMk56Z3RPREV3TmkwME1EZzVMV0UxWXprdE5tTmhObU0wWkRBMU1HVTBJaXdpZG5BaU9uc2lRR052Ym5SbGVIUWlPbHNpYUhSMGNITTZMeTkzZDNjdWR6TXViM0puTHpJd01UZ3ZjSEpsYzJWdWRHRjBhVzl1Y3k5Mk1TSmRMQ0owZVhCbElqcGJJbFpsY21sbWFXRmliR1ZRY21WelpXNTBZWFJwYjI0aVhYMHNJbWx6Y3lJNkltUnBaRHB3Y21semJUcGhaR0psT1RJNE9XUXdZelZtWWpVMlptWmhOVEF6T0Rka01UZ3dOR0ZpTkdFeE5UYzJOVEkzWXprME5tRTFNalV5T0RFM1ptRTRaVGhoTW1OalpXUXdPa056YzBKRGMyZENSVzFKUzBSWE1XaGpNMUpzWTJsb2NHSnRVbXhsUTJ0UlFWVktVRU5uYkZSYVYwNTNUV3BWTW1GNlJWTkpSUzFNYVVkTU0xRklaRlZ1VG10d1dXSkthSE5VYTIxWVVGaEpVM0ZXZWpjMll6RlZPWGhvVURseWNFZHBSSEZXTlRselJYcEtWbEpEYWxJMGEwMHdaMGg0YkhWUU5tVk5Ta2wwZHpJMk4yWllWbEpoTUhoRE5XaEthVU5uTVhSWldFNHdXbGhKYjJGWE5XdGFXR2R3UlVGU1ExUjNiMHBWTWxacVkwUkpNVTV0YzNoRmFVSlFhVFJvYVRrd1FqTldTbnBhUzFkSGVWbGlSVFZLYkhveGVVVnhiR010TFc1T1ZsQmpXVlJmWVRaU2IyYzJiR1ZtWWtKTmVWWlZVVzh3WlVwRVRrbENPRnBpYWkxdWFrTlRUR05PZFhVek1URlZWM1JOVVhWWkluMC5CcmFpbEVXa2VlSXhWbjY3dnpkVHZGTXpBMV9oNzFoaDZsODBHRFBpbkRaVVB4ajAxSC0tUC1QZDIxTk9wRDd3am51SDkxdUNBOFZMUW9fS2FnVjlnQQo="}"""
          issueCredential <- ZIO.fromEither(
            IssueCredential.makeIssueCredentialFromRequestCredential(requestCredential.makeMessage)
          )
          _ <- repo.updateWithIssuedRawCredential(
            aIssueCredentialRecord.id,
            issueCredential,
            rawCredentialData,
            None,
            None,
            IssueCredentialRecord.ProtocolState.CredentialReceived
          )
          svc <- ZIO.service[PresentationService]
          connectionId = Some("connectionId")
          anoncredPresentationRequestV1 = AnoncredPresentationRequestV1(
            Map.empty,
            Map.empty,
            "name",
            "nonce",
            "version",
            None
          )
          attachmentDescriptor = AttachmentDescriptor.buildBase64Attachment(
            mediaType = Some("application/json"),
            format = Some(PresentCredentialRequestFormat.Anoncred.name),
            payload =
              AnoncredPresentationRequestV1.schemaSerDes.serializeToJsonString(anoncredPresentationRequestV1).getBytes()
          )
          requestPresentation = RequestPresentation(
            body = RequestPresentation.Body(goal_code = Some("Presentation Request")),
            attachments = Seq(attachmentDescriptor),
            to = Some(DidId("did:peer:Prover")),
            from = Some(DidId("did:peer:Verifier")),
          )
          aRecord <- svc.receiveRequestPresentation(connectionId, requestPresentation)
          credentialsToUse = Seq(aIssueCredentialRecord.id.value)
          result <- svc.acceptRequestPresentation(aRecord.id, credentialsToUse).exit

        } yield assert(result)(
          fails(isSubtype[NotMatchingPresentationCredentialFormat](anything))
        )
      },
      test("rejectRequestPresentation updates the PresentationRecord") {
        for {
          svc <- ZIO.service[PresentationService]
          connectionId = Some("connectionId")
          aRecord <- svc.receiveRequestPresentation(
            connectionId,
            requestPresentation(PresentCredentialRequestFormat.JWT)
          )
          updateRecord <- svc.rejectRequestPresentation(aRecord.id)

        } yield {
          assertTrue(updateRecord.connectionId == connectionId) &&
          // assertTrue(updateRecord.requestPresentationData == Some(requestPresentation)) && // FIXME: enabling them make the test fail.
          assertTrue(updateRecord.protocolState == PresentationRecord.ProtocolState.RequestRejected)
        }
      },
      test("markPresentationSent returns updated PresentationRecord") {
        for {
          svc <- ZIO.service[PresentationService]
          _ = DidId("did:peer:Prover")
          record <- svc.createJwtRecord()
          repo <- ZIO.service[PresentationRepository]
          _ <- repo.updatePresentationRecordProtocolState(
            record.id,
            PresentationRecord.ProtocolState.RequestPending,
            PresentationRecord.ProtocolState.PresentationGenerated
          )
          record <- svc.markPresentationSent(record.id)

        } yield {
          assertTrue(record.protocolState == PresentationRecord.ProtocolState.PresentationSent)
        }
      },
      test("receivePresentation updates the PresentationRecord") {
        for {
          svc <- ZIO.service[PresentationService]
          aRecord <- svc.createJwtRecord()
          p = presentation(aRecord.thid.value)
          aRecordReceived <- svc.receivePresentation(p)

        } yield {
          assertTrue(aRecordReceived.id == aRecord.id) &&
          assertTrue(aRecordReceived.presentationData.contains(p))
        }
      },
      test("acceptPresentation updates the PresentationRecord") {
        for {
          svc <- ZIO.service[PresentationService]
          aRecord <- svc.createJwtRecord()
          p = presentation(aRecord.thid.value)
          aRecordReceived <- svc.receivePresentation(p)
          repo <- ZIO.service[PresentationRepository]
          _ <- repo.updatePresentationRecordProtocolState(
            aRecord.id,
            PresentationRecord.ProtocolState.PresentationReceived,
            PresentationRecord.ProtocolState.PresentationVerified
          )
          _ <- svc.acceptPresentation(aRecord.id)
        } yield {
          assertTrue(aRecordReceived.id == aRecord.id) &&
          assertTrue(aRecordReceived.presentationData.contains(p))
        }
      },
      test("markPresentationRejected updates the PresentationRecord") {
        for {
          svc <- ZIO.service[PresentationService]
          aRecord <- svc.createJwtRecord()
          p = presentation(aRecord.thid.value)
          _ <- svc.receivePresentation(p)
          repo <- ZIO.service[PresentationRepository]
          _ <- repo.updatePresentationRecordProtocolState(
            aRecord.id,
            PresentationRecord.ProtocolState.PresentationReceived,
            PresentationRecord.ProtocolState.PresentationVerified
          )
          aRecordReject <- svc.markPresentationRejected(aRecord.id)
        } yield {
          assertTrue(aRecordReject.id == aRecord.id) &&
          assertTrue(aRecordReject.presentationData.contains(p)) &&
          assertTrue(aRecordReject.protocolState == PresentationRecord.ProtocolState.PresentationRejected)
        }
      },
      test("rejectPresentation updates the PresentationRecord") {
        for {
          svc <- ZIO.service[PresentationService]
          aRecord <- svc.createJwtRecord()
          p = presentation(aRecord.thid.value)
          _ <- svc.receivePresentation(p)
          repo <- ZIO.service[PresentationRepository]
          _ <- repo.updatePresentationRecordProtocolState(
            aRecord.id,
            PresentationRecord.ProtocolState.PresentationReceived,
            PresentationRecord.ProtocolState.PresentationVerified
          )
          aRecordReject <- svc.rejectPresentation(aRecord.id)
        } yield {
          assertTrue(aRecordReject.id == aRecord.id) &&
          assertTrue(aRecordReject.presentationData.contains(p)) &&
          assertTrue(aRecordReject.protocolState == PresentationRecord.ProtocolState.PresentationRejected)
        }
      },
      test("markPresentationGenerated returns updated PresentationRecord") {
        for {
          svc <- ZIO.service[PresentationService]
          record <- svc.createJwtRecord()
          p = presentation(record.thid.value)
          repo <- ZIO.service[PresentationRepository]
          _ <- repo.updatePresentationRecordProtocolState(
            record.id,
            PresentationRecord.ProtocolState.RequestPending,
            PresentationRecord.ProtocolState.PresentationPending
          )
          record <- svc.markPresentationGenerated(record.id, p)
        } yield {
          assertTrue(record.protocolState == PresentationRecord.ProtocolState.PresentationGenerated)
        }
      },
      test("markProposePresentationSent returns updated PresentationRecord") {
        for {
          svc <- ZIO.service[PresentationService]
          record <- svc.createJwtRecord()
          repo <- ZIO.service[PresentationRepository]
          _ <- repo.updatePresentationRecordProtocolState(
            record.id,
            PresentationRecord.ProtocolState.RequestPending,
            PresentationRecord.ProtocolState.ProposalPending
          )
          record <- svc.markProposePresentationSent(record.id)
        } yield {
          assertTrue(record.protocolState == PresentationRecord.ProtocolState.ProposalSent)
        }
      },
      test("receiveProposePresentation updates the PresentationRecord") {
        for {
          svc <- ZIO.service[PresentationService]
          aRecord <- svc.createJwtRecord()
          p = proposePresentation(aRecord.thid.value)
          aRecordReceived <- svc.receiveProposePresentation(p)
        } yield {
          assertTrue(aRecordReceived.id == aRecord.id) &&
          assertTrue(aRecordReceived.proposePresentationData.contains(p))
        }
      },
      test("acceptProposePresentation updates the PresentationRecord") {
        for {
          svc <- ZIO.service[PresentationService]
          aRecord <- svc.createJwtRecord()
          p = proposePresentation(aRecord.thid.value)
          aRecordReceived <- svc.receiveProposePresentation(p)
          repo <- ZIO.service[PresentationRepository]
          _ <- svc.acceptProposePresentation(aRecord.id)
        } yield {
          assertTrue(aRecordReceived.id == aRecord.id) &&
          assertTrue(aRecordReceived.proposePresentationData.contains(p))
        }
      },
    ).provideSomeLayer(ZLayer.succeed(WalletAccessContext(WalletId.random)))

  private def receiveRequestPresentationTest(attachment: AttachmentDescriptor) = {
    for {
      svc <- ZIO.service[PresentationService]
      connectionId = Some("connectionId")
      body = RequestPresentation.Body(goal_code = Some("Presentation Request"))
      prover = Some(DidId("did:peer:Prover"))
      verifier = Some(DidId("did:peer:Verifier"))
      attachmentDescriptor = attachment
      requestPresentation = RequestPresentation(
        body = body,
        attachments = Seq(attachmentDescriptor),
        to = prover,
        from = verifier,
      )
      requestPresentationRecord <- svc.receiveRequestPresentation(connectionId, requestPresentation)
    } yield (requestPresentationRecord, requestPresentation)
  }

  private def createAnoncredPresentation = {
    for {
      credentialDefinitionService <- ZIO.service[CredentialDefinitionService]
      issuerId = "did:prism:issuer"
      holderID = "did:prism:holder"
      schemaId = "resource:///anoncred-presentation-schema-example.json"
      credentialDefinitionDb <- credentialDefinitionService.create(
        Input(
          name = "Credential Definition Name",
          description = "Credential Definition Description",
          version = "1.2",
          signatureType = "CL",
          tag = "tag",
          author = issuerId,
          authored = Some(OffsetDateTime.parse("2022-03-10T12:00:00Z")),
          schemaId = schemaId,
          supportRevocation = false
        )
      )
      repo <- ZIO.service[CredentialRepository]
      linkSecretService <- ZIO.service[LinkSecretService]
      linkSecret <- linkSecretService.fetchOrCreate()
      genericSecretStorage <- ZIO.service[GenericSecretStorage]
      maybeCredentialDefintionPrivate <-
        genericSecretStorage
          .get[UUID, CredentialDefinitionSecret](credentialDefinitionDb.guid)
      credentialDefinition = AnoncredCreateCredentialDefinition(
        AnoncredCredentialDefinition(credentialDefinitionDb.definition.toString()),
        AnoncredCredentialDefinitionPrivate(maybeCredentialDefintionPrivate.get.json.toString()),
        AnoncredCredentialKeyCorrectnessProof(credentialDefinitionDb.keyCorrectnessProof.toString())
      )
      file = createTempJsonFile(credentialDefinition.cd.data, "anoncred-presentation-credential-definition-example")
      credentialDefinitionId = "resource:///" + file.getFileName
      credentialOffer = AnoncredLib.createOffer(credentialDefinition, credentialDefinitionId)
      credentialRequest = AnoncredLib.createCredentialRequest(linkSecret, credentialDefinition.cd, credentialOffer)
      processedCredential =
        AnoncredLib.processCredential(
          AnoncredLib
            .createCredential(
              credentialDefinition.cd,
              credentialDefinition.cdPrivate,
              credentialOffer,
              credentialRequest.request,
              Seq(
                ("name", "Miguel"),
                ("sex", "M"),
                ("age", "31"),
              )
            ),
          credentialRequest.metadata,
          linkSecret,
          credentialDefinition.cd
        )
      issueCredential =
        IssueCredential(
          from = DidId(issuerId),
          to = DidId(holderID),
          body = IssueCredential.Body(),
          attachments = Seq(
            AttachmentDescriptor.buildBase64Attachment(
              mediaType = Some("application/json"),
              format = Some(IssueCredentialIssuedFormat.Anoncred.name),
              payload = processedCredential.data.getBytes()
            )
          )
        )
      aIssueCredentialRecord =
        IssueCredentialRecord(
          id = DidCommID(),
          createdAt = Instant.now,
          updatedAt = None,
          thid = DidCommID(),
          schemaUris = Some(List(schemaId)),
          credentialDefinitionId = Some(credentialDefinitionDb.guid),
          credentialDefinitionUri = Some(credentialDefinitionId),
          credentialFormat = CredentialFormat.AnonCreds,
          invitation = None,
          role = IssueCredentialRecord.Role.Issuer,
          subjectId = None,
          keyId = None,
          validityPeriod = None,
          automaticIssuance = None,
          protocolState = IssueCredentialRecord.ProtocolState.CredentialReceived,
          offerCredentialData = None,
          requestCredentialData = None,
          anonCredsRequestMetadata = None,
          issueCredentialData = Some(issueCredential),
          issuedCredentialRaw = Some(issueCredential.attachments.map(_.data.toJson).headOption.getOrElse("???")),
          issuingDID = None,
          metaRetries = 5,
          metaNextRetry = Some(Instant.now()),
          metaLastFailure = None,
        )
      _ <- repo.create(aIssueCredentialRecord)
      svc <- ZIO.service[PresentationService]
      aRecord <- svc.createAnoncredRecord(
        credentialDefinitionId = credentialDefinitionId
      )
      repo <- ZIO.service[PresentationRepository]
      credentialsToUse =
        AnoncredCredentialProofsV1(
          List(
            AnoncredCredentialProofV1(
              aIssueCredentialRecord.id.value,
              Seq("sex"),
              Seq("age")
            )
          )
        )
      credentialsToUseJson <- ZIO.fromEither(
        AnoncredCredentialProofsV1.schemaSerDes.serialize(credentialsToUse)
      )
      _ <-
        repo.updateAnoncredPresentationWithCredentialsToUse(
          aRecord.id,
          Some(AnoncredPresentationV1.version),
          Some(credentialsToUseJson),
          PresentationRecord.ProtocolState.RequestPending
        )
      presentation <- svc.createAnoncredPresentation(
        aRecord.requestPresentationData.get,
        aRecord.id,
        credentialsToUse,
        Instant.now()
      )
    } yield (presentation, aRecord)
  }

  private val multiWalletSpec =
    suite("multi-wallet spec")(
      test("createPresentation for different wallet and isolate records") {
        val walletId1 = WalletId.random
        val walletId2 = WalletId.random
        val wallet1 = ZLayer.succeed(WalletAccessContext(walletId1))
        val wallet2 = ZLayer.succeed(WalletAccessContext(walletId2))
        for {
          svc <- ZIO.service[PresentationService]
          record1 <- svc.createJwtRecord().provide(wallet1)
          record2 <- svc.createJwtRecord().provide(wallet2)
          ownRecord1 <- svc.findPresentationRecord(record1.id).provide(wallet1)
          ownRecord2 <- svc.findPresentationRecord(record2.id).provide(wallet2)
          crossRecord1 <- svc.findPresentationRecord(record1.id).provide(wallet2)
          crossRecord2 <- svc.findPresentationRecord(record2.id).provide(wallet1)
        } yield assert(ownRecord1)(isSome(equalTo(record1))) &&
          assert(ownRecord2)(isSome(equalTo(record2))) &&
          assert(crossRecord1)(isNone) &&
          assert(crossRecord2)(isNone)
      }
    )

  def createTempJsonFile(jsonContent: String, fileName: String): Path = {
    // NOTE: under sbt 2.0.8 `getResource("/")` resolves to sbt's own worker jar, so
    // the test-classpath root is derived from this class file's location instead.
    val classFile =
      Path.of(this.getClass.getResource(this.getClass.getSimpleName + ".class").toURI)
    var root = classFile.getParent
    while (root != null && root.getFileName.toString != "test-classes") root = root.getParent
    if root == null then throw new IllegalStateException("could not locate test-classes directory")

    // A unique name per call avoids races between tests writing to the same
    // file when tests run concurrently (parallel test execution).
    val uniqueFileName = s"$fileName-${UUID.randomUUID}"
    val filePath = root.resolve(uniqueFileName + ".json")

    Files.write(filePath, jsonContent.getBytes(StandardCharsets.UTF_8))

    filePath.toFile.deleteOnExit()
    filePath
  }

}
