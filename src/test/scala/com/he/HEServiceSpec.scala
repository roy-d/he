package com.he

import com.twitter.finagle.Service
import com.twitter.finagle.http.{FileElement, Request, RequestBuilder, Response}
import com.twitter.io.{Buf, Reader}
import com.twitter.util.{Duration, Await}
import io.finch.test.ServiceSuite
import org.scalatest.Matchers
import org.scalatest.fixture.FlatSpec

trait HEServiceSuite {
  this: FlatSpec with ServiceSuite with Matchers =>

  def createService(): Service[Request, Response] = {
    Endpoint.makeService()
  }

  "HTML Explorer" should "return health" in { f =>
    val request = Request("/hc")
    val result: Response = f(request)
    result.statusCode shouldBe 200
  }

  it should "return lsInfo" in { f =>
    val request = Request("/ls")
    val result: Response = f(request)
    result.statusCode shouldBe 200
  }

  it should "respond to queries" in { f =>
    val request: Request = RequestBuilder()
      .url("http://localhost:8080/he").buildPut(
      Buf.Utf8(
        s"""
           |  {
           |    "namePattern": "XXX1055.*Invoice_001.*"
           |  }
           """.stripMargin)
    )
    val result: Response = f(request)
    result.statusCode shouldBe 200
  }

}

class HEServiceSpec extends FlatSpec with ServiceSuite with HEServiceSuite with Matchers
