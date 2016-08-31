package com.he

import _root_.argonaut._
import argonaut.Argonaut._
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import io.finch._
import io.finch.argonaut._
import io.finch.items._

object Endpoint {

  val startTime = System.currentTimeMillis()

  implicit val encodeException: EncodeJson[Exception] = EncodeJson {
    case Error.NotPresent(ParamItem(p)) => Json.obj(
      "error" -> jString("param_not_present"), "param" -> jString(p)
    )
    case Error.NotPresent(BodyItem) => Json.obj(
      "error" -> jString("body_not_present")
    )
    case Error.NotParsed(ParamItem(p), _, _) => Json.obj(
      "error" -> jString("param_not_parsed"), "param" -> jString(p)
    )
    case Error.NotParsed(BodyItem, _, _) => Json.obj(
      "error" -> jString("body_not_parsed")
    )
    case Error.NotValid(ParamItem(p), rule) => Json.obj(
      "error" -> jString("param_not_valid"), "param" -> jString(p), "rule" -> jString(rule)
    )
    // Domain errors
    case error: HEError => Json.obj(
      "error" -> jString(error.message)
    )
  }

  def makeService(): Service[Request, Response] = (hc() :+: query())
    .handle({
      case e: HEError => NotFound(e)
    }).toService

  def hc(): Endpoint[Health] =
    get("hc") { () => Ok(Health(s"started: $startTime")) }

  def query(): Endpoint[HEResponse] =
    put("he" :: body.as[Query]) {
      query: Query =>
        Ok(HEResponse("ok", Some("response"))) }

}
