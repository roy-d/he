package com.he

import argonaut.Argonaut._
import argonaut.CodecJson

case class HEQuery(namePattern: String, contentPattern: Option[String])

object HEQuery {
  implicit val queryCodec: CodecJson[HEQuery] = casecodec2(HEQuery.apply, HEQuery.unapply)("namePattern", "contentPattern")
}
