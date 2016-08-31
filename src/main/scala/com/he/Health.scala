package com.he

import argonaut.Argonaut._
import argonaut.CodecJson

case class Health(message: String)

object Health {
  implicit val healthCodec: CodecJson[Health] = casecodec1(Health.apply, Health.unapply)("message")
}
