package com.he

import argonaut.Argonaut._
import argonaut.CodecJson

case class Query(param1: String, param2: Option[String])

object Query {
  implicit val queryCodec: CodecJson[Query] = casecodec2(Query.apply, Query.unapply)("param1", "param2")
}
