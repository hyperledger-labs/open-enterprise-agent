package io.iohk.atala.pollux.core.service.serdes

import zio.*
import zio.test.*
import zio.test.Assertion.*

object AnoncredPresentationRequestSchemaSerDesSpec extends ZIOSpecDefault {
  val json =
    """
      |{
      |  "requested_attributes": {
      |    "attribute1": {
      |      "name": "Attribute 1",
      |      "restrictions": [
      |        {
      |          "cred_def_id": "credential_definition_id_of_attribute1",
      |          "non_revoked": {
      |            "from": 1635734400,
      |            "to": 1735734400
      |          }
      |        }
      |      ]
      |    }
      |  },
      |  "requested_predicates": {
      |    "predicate1": {
      |      "name": "Predicate 1",
      |      "p_type": ">=",
      |      "p_value": 18,
      |      "restrictions": [
      |        {
      |          "schema_id": "schema_id_of_predicate1",
      |          "non_revoked": {
      |            "from": 1635734400
      |          }
      |        }
      |      ]
      |    }
      |  },
      |  "name": "Example Presentation Request",
      |  "nonce": "1234567890",
      |  "version": "1.0"
      |}
      |""".stripMargin

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("AnoncredPresentationRequestSerDes")(
    test("should validate a correct schema") {
      assertZIO(AnoncredPresentationRequestSchemaSerDesV1.schemaSerDes.validate(json))(isTrue)
    },
    test("should deserialize correctly") {
      val expectedPresentationRequest =
        AnoncredPresentationRequestSchemaSerDesV1(
          requested_attributes = Map(
            "attribute1" -> AnoncredRequestedAttribute(
              "Attribute 1",
              List(
                AnoncredAttributeRestriction(
                  None,
                  Some("credential_definition_id_of_attribute1"),
                  Some(
                    AnoncredNonRevokedInterval(
                      Some(1635734400),
                      Some(1735734400)
                    )
                  )
                )
              )
            )
          ),
          requested_predicates = Map(
            "predicate1" ->
              AnoncredRequestedPredicate(
                "Predicate 1",
                ">=",
                18,
                List(
                  AnoncredPredicateRestriction(
                    Some("schema_id_of_predicate1"),
                    None,
                    Some(
                      AnoncredNonRevokedInterval(
                        Some(1635734400),
                        None
                      )
                    )
                  )
                )
              )
          ),
          name = "Example Presentation Request",
          nonce = "1234567890",
          version = "1.0",
          non_revoked = None
        )

      assertZIO(AnoncredPresentationRequestSchemaSerDesV1.schemaSerDes.deserialize(json))(
        Assertion.equalTo(expectedPresentationRequest)
      )
    }
  )
}
