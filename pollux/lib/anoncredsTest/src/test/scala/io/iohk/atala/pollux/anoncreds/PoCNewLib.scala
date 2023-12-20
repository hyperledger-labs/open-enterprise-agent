package io.iohk.atala.pollux.anoncreds

import org.scalatest.flatspec.AnyFlatSpec

import scala.jdk.CollectionConverters.*

/** polluxAnoncredsTest/Test/testOnly io.iohk.atala.pollux.anoncreds.PoCNewLib
  */
class PoCNewLib extends AnyFlatSpec {

  val SCHEMA_ID = "mock:uri2"
  val CRED_DEF_ID = "mock:uri3"
  val ISSUER_DID = "mock:issuer_id/path&q=bar"

  "LinkSecret" should "be able to parse back to the anoncreds lib" in {
    import scala.language.implicitConversions

    val ls1 = LinkSecret("65965334953670062552662719679603258895632947953618378932199361160021795698890")
    val ls1p = ls1: uniffi.anoncreds.LinkSecret
    assert(ls1p.getValue() == "65965334953670062552662719679603258895632947953618378932199361160021795698890")

    val ls0 = LinkSecret()
    val ls0p = ls0: uniffi.anoncreds.LinkSecret
    val ls0_ = ls0p: LinkSecret
    assert(ls0.data == ls0_.data)
  }

  "The POC New Lib script" should "run to completion" in {
    script()
  }

  def script(): Unit = {
    println(s"Version of anoncreds library")

    // ##############
    // ### ISSUER ###
    // ##############
    println("*** issuer " + ("*" * 100))
    // ############################################################################################
    val schema = AnoncredLib.createSchema(
      SCHEMA_ID,
      "0.1.0",
      Set("name", "sex", "age"),
      ISSUER_DID
    )

    // ############################################################################################
    val credentialDefinition = AnoncredLib.createCredDefinition(ISSUER_DID, schema, "tag", supportRevocation = false)

    // // ############################################################################################
    val credentialOffer = AnoncredLib.createOffer(credentialDefinition, CRED_DEF_ID)

    println("credentialOffer.schemaId: " + credentialOffer.schemaId)
    println("credentialOffer.credDefId: " + credentialOffer.credDefId)

    // ##############
    // ### HOLDER ###
    // ##############
    println("*** holder " + ("*" * 100))

    val ls1 = LinkSecret("65965334953670062552662719679603258895632947953618378932199361160021795698890")
    val linkSecret = LinkSecretWithId("ID_of_some_secret_1", ls1)

    val credentialRequest = AnoncredLib.createCredentialRequest(linkSecret, credentialDefinition.cd, credentialOffer)
    println("*" * 100)

    val credential = AnoncredLib.createCredential(
      credentialDefinition.cd,
      credentialDefinition.cdPrivate,
      credentialOffer,
      credentialRequest.request,
      Seq(
        ("name", "Miguel"),
        ("sex", "M"),
        ("age", "31"),
      )
    )

    println(credential)

    // ##############
    // ### PROVER ###
    // ##############

    // TODO READ about PresentationRequest https://hyperledger.github.io/anoncreds-spec/#create-presentation-request
    val presentationRequest = PresentationRequest(
      s"""{
        "nonce": "1103253414365527824079144",
        "name":"proof_req_1",
        "version":"0.1",
        "requested_attributes": {
            "sex":{"name":"sex", "restrictions":{"attr::sex::value":"M","cred_def_id":"$CRED_DEF_ID"}}
        },
        "requested_predicates":{
          "age":{"name":"age", "p_type":">=", "p_value":18}
        }
       }""".stripMargin

        // {"name":"proof_req_1","nonce":"1103253414365527824079144","requested_attributes":{"attr1_referent":{"name":"name","restrictions":{"attr::name::value":"Alex","cred_def_id":"creddef:government"}},"attr2_referent":{"name":"role","restrictions":{"cred_def_id":"creddef:employee"}},"attr3_referent":{"name":"name","restrictions":{"cred_def_id":"creddef:employee"}},"attr4_referent":{"name":"height","restrictions":{"attr::height::value":"175","cred_def_id":"creddef:government"}}},"requested_predicates":{"predicate1_referent":{"name":"age","p_type":">=","p_value":18,"restrictions":{"attr::height::value":"175","attr::name::value":"Alex","cred_def_id":"creddef:government"}}},"version":"0.1"}
    )

    val presentation = AnoncredLib.createPresentation(
      presentationRequest, // : PresentationRequest,
      Seq(
        CredentialAndRequestedAttributesPredicates(credential, Seq("sex"), Seq("age"))
      ), // credentials: Seq[Credential],
      Map(), // selfAttested: Map[String, String],
      linkSecret.secret, // linkSecret: LinkSecret,
      Map(credentialOffer.schemaId -> schema), // schemas: Map[SchemaId, SchemaDef],
      Map(
        credentialOffer.credDefId -> credentialDefinition.cd
      ), // credentialDefinitions: Map[CredentialDefinitionId, CredentialDefinition],
    )

    println("*** PROVER " + ("*" * 100) + " presentation")
    println(presentation)

    val verifyPresentation = AnoncredLib.verifyPresentation(
      presentation, // : Presentation,
      presentationRequest, // : PresentationRequest,
      Map(credentialOffer.schemaId -> schema), // schemas: Map[SchemaId, SchemaDef],
      Map(
        credentialOffer.credDefId -> credentialDefinition.cd
      ), // credentialDefinitions: Map[CredentialDefinitionId, CredentialDefinition],
    )

    println("*** PROVER " + ("*" * 100) + " verifyPresentation")
    println(verifyPresentation)

  }

}
