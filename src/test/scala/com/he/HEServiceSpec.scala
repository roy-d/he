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
    println(result)
    result.statusCode shouldBe 200
  }

  it should "respond to queries" in { f =>
    val request: Request = RequestBuilder()
      .url("http://localhost:8080/he").buildPost(
      Buf.Utf8(
        s"""
           |  {
           |    "param1": "One"
           |  }
           """.stripMargin)
    )
    val result: Response = f(request)
    println(result)
    result.statusCode shouldBe 200
  }

}

class HEServiceSpec extends FlatSpec with ServiceSuite with HEServiceSuite with Matchers
