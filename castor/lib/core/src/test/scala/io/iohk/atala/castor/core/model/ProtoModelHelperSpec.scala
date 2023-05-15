package io.iohk.atala.castor.core.model

import com.google.protobuf.timestamp.Timestamp
import io.iohk.atala.castor.core.model.did.ServiceType
import io.iohk.atala.castor.core.util.GenUtils
import io.iohk.atala.prism.protos.common_models.Ledger
import zio.*
import zio.test.*
import zio.test.Assertion.*
import io.iohk.atala.prism.protos.node_models

import java.time.Instant
import io.iohk.atala.castor.core.model.did.ServiceEndpoint
import io.circe.Json

object ProtoModelHelperSpec extends ZIOSpecDefault {

  import ProtoModelHelper.*

  given Conversion[String, ServiceType.Name] = ServiceType.Name.fromStringUnsafe
  given Conversion[String, ServiceEndpoint.URI] = s => ServiceEndpoint.URI.fromString(s).toOption.get

  private def makePublicKey(id: String, revokedOn: Option[node_models.LedgerData] = None): node_models.PublicKey =
    node_models.PublicKey(
      id = id,
      usage = node_models.KeyUsage.AUTHENTICATION_KEY,
      addedOn = None,
      revokedOn = revokedOn,
      keyData = node_models.PublicKey.KeyData.CompressedEcKeyData(
        node_models.CompressedECKeyData("secp256k1", Array.emptyByteArray.toProto)
      )
    )

  private def makeService(
      id: String,
      serviceType: String = "LinkedDomains",
      serviceEndpoint: String = "[]",
      deletedOn: Option[node_models.LedgerData] = None
  ): node_models.Service =
    node_models.Service(
      id = id,
      `type` = serviceType,
      serviceEndpoint = serviceEndpoint,
      addedOn = None,
      deletedOn = deletedOn
    )

  extension (i: Instant) {
    def toLedgerData: node_models.LedgerData = {
      val timestamp = Timestamp.of(i.getEpochSecond, i.getNano)
      val timestampInfo = node_models.TimestampInfo(
        blockSequenceNumber = 0,
        operationSequenceNumber = 0,
        blockTimestamp = Some(timestamp)
      )
      node_models.LedgerData(
        transactionId = "",
        ledger = Ledger.IN_MEMORY,
        timestampInfo = Some(timestampInfo)
      )
    }
  }

  override def spec =
    suite("ProtoModelHelper")(conversionSpec, didDataFilterSpec, parseServiceType, parseServiceEndpoint)

  private val conversionSpec = suite("round trip model conversion does not change data of models")(
    test("PublicKeyData") {
      check(GenUtils.publicKeyData) { pkd =>
        val result = pkd.toProto.toDomain
        assert(result)(isRight(equalTo(pkd)))
      }
    },
    test("PublicKey") {
      check(GenUtils.publicKey) { pk =>
        val result = pk.toProto.toDomain
        assert(result)(isRight(equalTo(pk)))
      }
    },
    test("InternalPublicKey") {
      check(GenUtils.internalPublicKey) { pk =>
        val result = pk.toProto.toDomain
        assert(result)(isRight(equalTo(pk)))
      }
    },
    test("Service") {
      check(GenUtils.service) { service =>
        val result = service.toProto.toDomain
        assert(result)(isRight(equalTo(service)))
      }
    }
  )

  private val didDataFilterSpec = suite("filterRevokedKeysAndServices")(
    test("not filter keys if revokedOn is empty") {
      val didData = node_models.DIDData(
        id = "123",
        publicKeys = Seq(
          makePublicKey(id = "key1"),
          makePublicKey(id = "key2"),
          makePublicKey(id = "key3")
        ),
        services = Seq(),
        context = Seq()
      )
      assertZIO(didData.filterRevokedKeysAndServices.map(_.publicKeys.map(_.id)))(
        hasSameElements(Seq("key1", "key2", "key3"))
      )
    },
    test("not filter keys if revokedOn timestamp has not passed") {
      for {
        now <- Clock.instant
        revokeTime = now.plusSeconds(5)
        ledgerData = revokeTime.toLedgerData
        didData = node_models.DIDData(
          id = "123",
          publicKeys = Seq(
            makePublicKey("key1"),
            makePublicKey("key2", revokedOn = Some(ledgerData)),
            makePublicKey("key3", revokedOn = Some(ledgerData))
          ),
          services = Seq(),
          context = Seq()
        )
        validKeysId <- didData.filterRevokedKeysAndServices.map(_.publicKeys.map(_.id))
      } yield assert(validKeysId)(hasSameElements(Seq("key1", "key2", "key3")))
    },
    test("filter keys if revokedOn timestamp has passed") {
      for {
        now <- Clock.instant
        revokeTime = now.minusSeconds(5)
        ledgerData = revokeTime.toLedgerData
        didData = node_models.DIDData(
          id = "123",
          publicKeys = Seq(
            makePublicKey("key1"),
            makePublicKey("key2", revokedOn = Some(ledgerData)),
            makePublicKey("key3", revokedOn = Some(ledgerData))
          ),
          services = Seq(),
          context = Seq()
        )
        validKeysId <- didData.filterRevokedKeysAndServices.map(_.publicKeys.map(_.id))
      } yield assert(validKeysId)(hasSameElements(Seq("key1")))
    },
    test("filter keys if revokedOn timestamp is exactly now") {
      for {
        now <- Clock.instant
        ledgerData = now.toLedgerData
        didData = node_models.DIDData(
          id = "123",
          publicKeys = Seq(makePublicKey(id = "key1", revokedOn = Some(ledgerData))),
          services = Seq(),
          context = Seq()
        )
        validKeysId <- didData.filterRevokedKeysAndServices.map(_.publicKeys.map(_.id))
      } yield assert(validKeysId)(isEmpty)
    },
    test("not filter services if deletedOn is empty") {
      val didData = node_models.DIDData(
        id = "123",
        publicKeys = Seq(),
        services = Seq(
          makeService("service1"),
          makeService("service2"),
          makeService("service3")
        ),
        context = Seq()
      )
      assertZIO(didData.filterRevokedKeysAndServices.map(_.services.map(_.id)))(
        hasSameElements(Seq("service1", "service2", "service3"))
      )
    },
    test("not filter services if deletedOn timestamp has not passed") {
      for {
        now <- Clock.instant
        revokeTime = now.plusSeconds(5)
        ledgerData = revokeTime.toLedgerData
        didData = node_models.DIDData(
          id = "123",
          publicKeys = Seq(),
          services = Seq(
            makeService(id = "key1"),
            makeService(id = "key2", deletedOn = Some(ledgerData)),
            makeService(id = "key3", deletedOn = Some(ledgerData))
          ),
          context = Seq()
        )
        validKeysId <- didData.filterRevokedKeysAndServices.map(_.services.map(_.id))
      } yield assert(validKeysId)(hasSameElements(Seq("key1", "key2", "key3")))
    },
    test("filter services if deletedOn timestamp has passed") {
      for {
        now <- Clock.instant
        revokeTime = now.minusSeconds(5)
        ledgerData = revokeTime.toLedgerData
        didData = node_models.DIDData(
          id = "123",
          publicKeys = Seq(),
          services = Seq(
            makeService(id = "key1"),
            makeService(id = "key2", deletedOn = Some(ledgerData)),
            makeService(id = "key3", deletedOn = Some(ledgerData))
          ),
          context = Seq()
        )
        validKeysId <- didData.filterRevokedKeysAndServices.map(_.services.map(_.id))
      } yield assert(validKeysId)(hasSameElements(Seq("key1")))
    },
    test("filter services if deletedOn timestamp is exactly now") {
      for {
        now <- Clock.instant
        ledgerData = now.toLedgerData
        didData = node_models.DIDData(
          id = "123",
          publicKeys = Seq(),
          services = Seq(makeService(id = "key1", deletedOn = Some(ledgerData))),
          context = Seq()
        )
        validKeysId <- didData.filterRevokedKeysAndServices.map(_.services.map(_.id))
      } yield assert(validKeysId)(isEmpty)
    }
  )

  private val parseServiceType = suite("parseServiceType")(
    test("parse valid single service type") {
      val serviceType = "LinkedDomains"
      val result = ProtoModelHelper.parseServiceType(serviceType)
      assert(result)(isRight(equalTo(ServiceType.Single("LinkedDomains"))))
    },
    test("parse valid multiple service type") {
      val serviceType = """["LinkedDomains", "IdentityHub"]"""
      val result = ProtoModelHelper.parseServiceType(serviceType)
      assert(result)(isRight(equalTo(ServiceType.Multiple("LinkedDomains", List("IdentityHub")))))
    },
    test("parse valid multiple service type with one item") {
      val serviceType = """["LinkedDomains"]"""
      val result = ProtoModelHelper.parseServiceType(serviceType)
      assert(result)(isRight(equalTo(ServiceType.Multiple("LinkedDomains", List()))))
    },
    test("parse multiple service type containing item that is not a string") {
      val serviceType = """["LinkedDomains", 1]"""
      val result = ProtoModelHelper.parseServiceType(serviceType)
      assert(result)(isLeft(containsString("service type is not a JSON array of strings")))
    },
    test("parse empty multiple service type") {
      val serviceType = """[]"""
      val result = ProtoModelHelper.parseServiceType(serviceType)
      assert(result)(isLeft(containsString("service type cannot be an empty JSON array")))
    },
    test("parse empty string") {
      val serviceType = ""
      val result = ProtoModelHelper.parseServiceType(serviceType)
      assert(result)(isLeft(containsString("service type name must have at least a non whitespace character")))
    },
    test("parse single service type starting with a white space character") {
      val serviceType = " LinkedDomains"
      val result = ProtoModelHelper.parseServiceType(serviceType)
      assert(result)(isLeft(containsString("service type name cannot start nor end with whitespaces")))
    },
    test("parse single service type ending with a white space character") {
      val serviceType = "LinkedDomains "
      val result = ProtoModelHelper.parseServiceType(serviceType)
      assert(result)(isLeft(containsString("service type name cannot start nor end with whitespaces")))
    },
    test("parse multiple service type starting with a white space character") {
      val serviceType = """["LinkedDomains", " IdentityHub"]"""
      val result = ProtoModelHelper.parseServiceType(serviceType)
      assert(result)(isLeft(containsString("service type name cannot start nor end with whitespaces")))
    },
    test("parse multiple service type ending with a white space character") {
      val serviceType = """["LinkedDomains", "IdentityHub "]"""
      val result = ProtoModelHelper.parseServiceType(serviceType)
      assert(result)(isLeft(containsString("service type name cannot start nor end with whitespaces")))
    },
    test("parse multiple service type that contain item with empty string") {
      val serviceType = """["LinkedDomains", ""]"""
      val result = ProtoModelHelper.parseServiceType(serviceType)
      assert(result)(isLeft(containsString("service type name must have at least a non whitespace character")))
    },
    test("parse multiple service type with extra whitespace between items") {
      val serviceType = """[   "LinkedDomains" ,      "IdentityHub"    ]"""
      val result = ProtoModelHelper.parseServiceType(serviceType)
      assert(result)(isRight(equalTo(ServiceType.Multiple("LinkedDomains", List("IdentityHub")))))
    },
  )

  private val parseServiceEndpoint = suite("parseServiceEndpoint")(
    test("parse valid uri string") {
      val serviceEndpoint = "https://example.com"
      val result = ProtoModelHelper.parseServiceEndpoint(serviceEndpoint)
      val expected: ServiceEndpoint.URI = "https://example.com"
      assert(result)(isRight(equalTo(expected)))
    },
    test("parse invalid uri string") {
      val serviceEndpoint = "example"
      val result = ProtoModelHelper.parseServiceEndpoint(serviceEndpoint)
      assert(result)(isLeft(containsString("unable to parse service endpoint URI")))
    },
    test("parse empty uri string") {
      val serviceEndpoint = ""
      val result = ProtoModelHelper.parseServiceEndpoint(serviceEndpoint)
      assert(result)(isLeft(containsString("unable to parse service endpoint URI")))
    },
    test("parse valid json object") {
      val serviceEndpoint = """{"uri": "https://example.com"}"""
      val result = ProtoModelHelper.parseServiceEndpoint(serviceEndpoint)
      val expected = ServiceEndpoint.Json(Json.obj("uri" -> Json.fromString("https://example.com")).asObject.get)
      assert(result)(isRight(equalTo(expected)))
    },
    test("parse invalid endpoint that is not a string or object") {
      val serviceEndpoint = "123"
      val result = ProtoModelHelper.parseServiceEndpoint(serviceEndpoint)
      assert(result)(isLeft(containsString("unable to parse service endpoint URI")))
    },
    test("parse empty json object") {
      val serviceEndpoint = "{}"
      val result = ProtoModelHelper.parseServiceEndpoint(serviceEndpoint)
      val expected = ServiceEndpoint.Json(Json.obj().asObject.get)
      assert(result)(isRight(equalTo(expected)))
    },
    test("parse empty json array") {
      val serviceEndpoint = "[]"
      val result = ProtoModelHelper.parseServiceEndpoint(serviceEndpoint)
      assert(result)(isLeft(containsString("the service endpoint cannot be an empty JSON array")))
    },
    test("parse json array of invalid items") {
      val serviceEndpoint = """[123]"""
      val result = ProtoModelHelper.parseServiceEndpoint(serviceEndpoint)
      assert(result)(isLeft(containsString("the service endpoint is not a JSON array of URIs and/or JSON objects")))
    },
    test("parse json array of uris") {
      val serviceEndpoint = """["https://example.com", "https://example2.com"]"""
      val result = ProtoModelHelper.parseServiceEndpoint(serviceEndpoint)
      val expected =
        ServiceEndpoint.EndpointList(
          "https://example.com",
          Seq("https://example2.com")
        )
      assert(result)(isRight(equalTo(expected)))
    },
    test("parse json array of objects") {
      val serviceEndpoint = """[{"uri": "https://example.com"}, {"uri": "https://example2.com"}]"""
      val result = ProtoModelHelper.parseServiceEndpoint(serviceEndpoint)
      val expected = ServiceEndpoint.EndpointList(
        ServiceEndpoint.Json(Json.obj("uri" -> Json.fromString("https://example.com")).asObject.get),
        Seq(
          ServiceEndpoint.Json(Json.obj("uri" -> Json.fromString("https://example2.com")).asObject.get)
        )
      )
      assert(result)(isRight(equalTo(expected)))
    },
    test("parse json array of mixed types") {
      val serviceEndpoint = """[{"uri": "https://example.com"}, "https://example2.com"]"""
      val result = ProtoModelHelper.parseServiceEndpoint(serviceEndpoint)
      val expected = ServiceEndpoint.EndpointList(
        ServiceEndpoint.Json(Json.obj("uri" -> Json.fromString("https://example.com")).asObject.get),
        Seq("https://example2.com")
      )
      assert(result)(isRight(equalTo(expected)))
    },
  )

}
