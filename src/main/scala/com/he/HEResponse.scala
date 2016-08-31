package com.he

import argonaut.Argonaut._
import argonaut.CodecJson

case class HEResponse(param1: String, param2: Option[String])

object HEResponse {
  implicit val responseCodec: CodecJson[HEResponse] = casecodec2(HEResponse.apply, HEResponse.unapply)("param1", "param2")
}
