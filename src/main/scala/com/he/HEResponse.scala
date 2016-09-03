package com.he

import argonaut.Argonaut._
import argonaut.CodecJson

case class HEResponse(fileCount: Int, files: List[String], tableCount: Int, tables: List[TableInfo], errors: List[ErrorInfo], responseMSec: Long)

object HEResponse {
  implicit val responseCodec: CodecJson[HEResponse] = casecodec6(HEResponse.apply, HEResponse.unapply)("fileCount", "files", "tableCount", "tables", "errors", "responseMSec")
}

case class TableInfo(file: String, rows: Int, cols: Int, emptyCols: Int, images: Int, hasWideSpaces: Boolean, invalidChars: Set[Char], emptyCells: Int, border: String, validHTML: Boolean, hasChild: Boolean, colSpan: String, rowSpan: String, contentType: TableContentTypeInfo, visualType: String)

object TableInfo {
  implicit val tableCodec: CodecJson[TableInfo] = casecodec15(TableInfo.apply, TableInfo.unapply)("file", "rows", "cols", "emptyCols", "images", "wideSpaces", "invalidChars", "emptyCells", "border", "validHTML", "hasChild", "colSpan", "rowSpan", "contentType", "visualType")
}

case class TableContentTypeInfo(name: String, matchedKeywords: Set[String])

object TableContentTypeInfo {
  implicit val tableTypeCodec: CodecJson[TableContentTypeInfo] = casecodec2(TableContentTypeInfo.apply, TableContentTypeInfo.unapply)("name", "matchedKeywords")
}

case class ErrorInfo(file: String, errorCharRatio: Double, invalidChars: Set[Char])

object ErrorInfo {
  implicit val tableCodec: CodecJson[ErrorInfo] = casecodec3(ErrorInfo.apply, ErrorInfo.unapply)("file", "errorCharRatio", "invalidChars")
}

case class LSInfo(fileNames: List[String])

object LSInfo {
  implicit val lsCodec: CodecJson[LSInfo] = casecodec1(LSInfo.apply, LSInfo.unapply)("fileNames")
}
