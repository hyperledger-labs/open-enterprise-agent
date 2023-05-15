package io.iohk.atala.castor.core.model.did

import zio.*
import zio.test.*
import zio.test.Assertion.*

object ServiceEndpointSpec extends ZIOSpecDefault {

  override def spec = suite("ServiceEndpoint")(
    test("URI.fromString create an endpoint with a valid uri") {
      val uri = "https://example.com/login"
      val result = ServiceEndpoint.URI.fromString(uri)
      assert(result)(isRight(isSubtype[ServiceEndpoint.URI](hasField("uri", _.uri, equalTo(uri)))))
    },
    test("URI.fromString does not create an endpoint with invalid uri") {
      val uri = "this is not a uri"
      val result = ServiceEndpoint.URI.fromString(uri)
      assert(result)(isLeft(containsString(s"unable to parse service endpoint URI")))
    },
    test("URI.fromString does not change original uri if it is unnormalized") {
      val uri = "https://example.com/../login"
      val result = ServiceEndpoint.URI.fromString(uri)
      assert(result)(isRight(isSubtype[ServiceEndpoint.URI](hasField("uri", _.uri, equalTo(uri)))))
    },
  )

}
