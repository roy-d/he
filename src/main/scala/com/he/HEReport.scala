package com.he

import argonaut.Argonaut._
import argonaut.CodecJson

case class HEReport(timeTakenSecs: Long, filesCount: Long, totalTableCount: Long, leafTableCount: Long, tablesWithImages: Long, tablesWithWideSpaces: Long, tablesWithInvalidChars: Long, tablesWithEmptyCells: Long, tablesWithInvalidXML: Long, visualTypes: List[ReportTypeInfo], contentTypes: List[ReportTypeInfo])

object HEReport {
  implicit val reportCodec: CodecJson[HEReport] = casecodec11(HEReport.apply, HEReport.unapply)("timeTakenSecs", "filesCount", "totalTableCount", "leafTableCount", "tablesWithImages", "tablesWithWideSpaces", "tablesWithInvalidChars", "tablesWithEmptyCells", "tablesWithInvalidXML", "visualTypes", "contentTypes")
}

case class ReportTypeInfo(name: String, totalTableCount: Long, leafTableCount: Long, tablesWithImages: Long, tablesWithWideSpaces: Long, tablesWithInvalidChars: Long, tablesWithEmptyCells: Long, tablesWithInvalidXML: Long, formCount: Option[Long] = None, simpleCount: Option[Long] = None, complexCount: Option[Long] = None)

object ReportTypeInfo {
  implicit val reportTypeCodec: CodecJson[ReportTypeInfo] = casecodec11(ReportTypeInfo.apply, ReportTypeInfo.unapply)("name", "totalTableCount", "leafTableCount", "tablesWithImages", "tablesWithWideSpaces", "tablesWithInvalidChars", "tablesWithEmptyCells", "tablesWithInvalidXML", "formCount", "simpleCount", "complexCount")
}
