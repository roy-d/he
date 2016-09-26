package com.he

import java.io.File
import java.nio.charset.Charset

import _root_.argonaut._
import argonaut.Argonaut._
import com.he.DocumentUtils._
import com.he.HTMLUtils._
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.typesafe.config._
import io.finch._
import io.finch.argonaut._
import io.finch.items._

import scala.collection.JavaConversions._

object Endpoint {

  val startTime = System.currentTimeMillis()

  val conf = ConfigFactory.load()

  val rootDir = conf.getString("he.root-dir")

  val charsetName = conf.getString("he.charset")

  val resultLimit = conf.getInt("he.resultLimit")

  val charset = Charset.forName(charsetName)

  val htmlFiles = filterHTML(ls(rootDir))

  val htmlFileNames = htmlFiles.map(_.getName)

  val keywords = conf.getStringList("he.keywords").toList

  val tableTypes: List[TableType] = keywords.map(new TableType(_))

  var dump = true

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

  def makeService(): Service[Request, Response] = (hc() :+: query() :+: lsInfo() :+: report())
    .handle({
      case e: HEError => NotFound(e)
    }).toService

  def hc(): Endpoint[HEHealth] =
    get("hc") {
      () =>
        val upMinutes = (System.currentTimeMillis() - startTime) / 60000
        Ok(HEHealth(upMinutes, rootDir, htmlFiles.size))
    }

  def lsInfo(): Endpoint[LSInfo] =
    get("ls") {
      () =>
        Ok(LSInfo(htmlFileNames))
    }

  def query(): Endpoint[HEResponse] =
    put("he" :: body.as[HEQuery]) {
      query: HEQuery =>
        val start = System.currentTimeMillis()
        val files = filterPattern(htmlFiles, query.namePattern)

        val contentNames = files.take(resultLimit).map(file => (getContent(file, charset), file.getName))

        val contents = query.contentPattern match {
          case None =>
            contentNames
          case Some(cPattern) =>
            val cRegex = s"(?i)$cPattern".r
            contentNames.filter { case (content, name) => cRegex.findFirstIn(content).isDefined }
        }
        val errors = contents
          .par
          .map { case (content, name) =>
            println(s"detecting errors: $name")
            getErrors(content, name)
          }.toList
        val tables = contents
          .map { case (content, name) => (getJSD(content, charsetName), name) }
          .par
          .map { case (jsd, name) =>
            println(s"detecting tables: $name")
            getTables(jsd, name, tableTypes)
          }
          .toList
          .flatten
        println("done !!")
        Ok(HEResponse(contents.size, contents.map(_._2), tables.size, tables, errors, System.currentTimeMillis() - start))
    }

  def report(): Endpoint[HEReport] =
    get("report") {
      () =>
        val reportStart = System.currentTimeMillis()

        var fileCount = 0
        var tableCount = 0
        var leafTableCount = 0
        var imageTableCount = 0
        var wideSpaceTableCount = 0
        var invalidCharTableCount = 0
        var emptyCellTableCount = 0
        var tablesWithInvalidXML = 0
        val contentTypeMap = scala.collection.mutable.Map[String, ReportTypeInfo]()
        val visualTypeMap = scala.collection.mutable.Map("simple" -> ReportTypeInfo("simple", 0, 0, 0, 0, 0, 0, 0), "complex" -> ReportTypeInfo("complex", 0, 0, 0, 0, 0, 0, 0), "form" -> ReportTypeInfo("form", 0, 0, 0, 0, 0, 0, 0))

        import java.io._

        val pw = new PrintWriter(new File("cmla_table_report.csv" ))

        htmlFiles.foreach { case file: File =>
          val name = file.getName
          if (fileCount % 50 == 0) println(s"reporting file # $fileCount")
          fileCount = fileCount + 1
          val content = getContent(file, charset)
          val jsd = getJSD(content, charsetName)
          val tables = getTables(jsd, name, tableTypes)
          tables.foreach { case table: TableInfo =>
            if(dump){
              pw.write(table.toCSV())
              pw.write('\n')
            }
            tableCount = tableCount + 1
            val hasChild = table.hasChild
            if (hasChild.unary_!) {
              leafTableCount = leafTableCount + 1
            }
            if (table.images > 0) {
              imageTableCount = imageTableCount + 1
            }
            if (table.hasWideSpaces) {
              wideSpaceTableCount = wideSpaceTableCount + 1
            }
            if (table.invalidChars.nonEmpty) {
              invalidCharTableCount = invalidCharTableCount + 1
            }
            if (table.emptyCols > 0) {
              emptyCellTableCount = emptyCellTableCount + 1
            }
            if (table.validHTML.unary_!) {
              tablesWithInvalidXML = tablesWithInvalidXML + 1
            }
            val tti = table.contentType
            val contentTypeName = tti.name
            if (!contentTypeMap.contains(contentTypeName)) {
              val reportType = ReportTypeInfo(contentTypeName, 0, 0, 0, 0, 0, 0, 0)
              contentTypeMap.put(contentTypeName, reportType)
            }

            val reportType = contentTypeMap(contentTypeName)

            var formCount = reportType.formCount.getOrElse(0L)
            var simpleCount = reportType.simpleCount.getOrElse(0L)
            var complexCount = reportType.complexCount.getOrElse(0L)

            table.visualType match {
              case "form" => formCount = formCount + 1
              case "simple" => simpleCount = simpleCount + 1
              case "complex" => complexCount = complexCount + 1
            }
            val newContentLeafCount = if (table.hasChild) reportType.leafTableCount else reportType.leafTableCount + 1
            val contentImageCount = if (table.images > 0) reportType.tablesWithImages + 1 else reportType.tablesWithImages
            val contentWideSpaceCount = if (table.hasWideSpaces) reportType.tablesWithWideSpaces + 1 else reportType.tablesWithWideSpaces
            val contentInvalidCharCount = if (table.invalidChars.nonEmpty) reportType.tablesWithInvalidChars + 1 else reportType.tablesWithInvalidChars
            val contentEmptyCellCount = if (table.emptyCols > 0) reportType.tablesWithEmptyCells + 1 else reportType.tablesWithEmptyCells
            val contentinvalidXMLCount = if (table.validHTML) reportType.tablesWithInvalidXML else reportType.tablesWithInvalidXML + 1
            val newReportType = ReportTypeInfo(contentTypeName, reportType.totalTableCount + 1, newContentLeafCount, contentImageCount, contentWideSpaceCount, contentInvalidCharCount, contentEmptyCellCount, contentinvalidXMLCount, Some(formCount), Some(simpleCount), Some(complexCount))
            contentTypeMap.put(contentTypeName, newReportType)


            val visualReport = visualTypeMap(table.visualType)
            val newVisualLeafCount = if (table.hasChild) visualReport.leafTableCount else visualReport.leafTableCount + 1
            val visualImageCount = if (table.images > 0) visualReport.tablesWithImages + 1 else visualReport.tablesWithImages
            val visualWideSpaceCount = if (table.hasWideSpaces) visualReport.tablesWithWideSpaces + 1 else visualReport.tablesWithWideSpaces
            val visualInvalidCharCount = if (table.invalidChars.nonEmpty) visualReport.tablesWithInvalidChars + 1 else visualReport.tablesWithInvalidChars
            val visualEmptyCellCount = if (table.emptyCols > 0) visualReport.tablesWithEmptyCells + 1 else visualReport.tablesWithEmptyCells
            val visualinvalidXMLCount = if (table.validHTML) visualReport.tablesWithInvalidXML else visualReport.tablesWithInvalidXML + 1
            val newVisualReport = ReportTypeInfo(table.visualType, visualReport.totalTableCount + 1, newVisualLeafCount, visualImageCount, visualWideSpaceCount, visualInvalidCharCount, visualEmptyCellCount, visualinvalidXMLCount)
            visualTypeMap.put(table.visualType, newVisualReport)

          }
        }

        pw.close

        Ok(HEReport(
          (System.currentTimeMillis() - reportStart) / 1000,
          fileCount,
          tableCount,
          leafTableCount,
          imageTableCount,
          wideSpaceTableCount,
          invalidCharTableCount,
          emptyCellTableCount,
          tablesWithInvalidXML,
          visualTypeMap.values.toList,
          contentTypeMap.values.toList
        ))
    }

}
