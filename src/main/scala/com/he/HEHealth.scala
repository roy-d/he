package com.he

import argonaut.Argonaut._
import argonaut.CodecJson

case class HEHealth(upMinutes: Long, rootDir: String, htmlFileCount: Int)

object HEHealth {
  implicit val healthCodec: CodecJson[HEHealth] = casecodec3(HEHealth.apply, HEHealth.unapply)("upMinutes", "rootDir", "htmlFileCount")
}
