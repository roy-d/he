package com.he

import com.he.DocumentUtils._
import com.he.HTMLUtils._
import com.he.RegexUtils._
import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConversions._

class UtilsSpec extends FlatSpec with Matchers {

  val conf = ConfigFactory.load()

  "Regex Utils" should "match a pattern" in {
    val nameReg = "abc.*".r
    nameReg matches "abcdefg" shouldBe true
    nameReg matches "abxcdefg" shouldBe false
  }

  "Document Utils" should "list all files" in {
    val rootDir = conf.getString("he.root-dir")
    val files = ls(rootDir)
    files.size should be > 0
    val htmlFiles = filterHTML(files)
    htmlFiles.size should be > 0
    val noteFiles = filterPattern(htmlFiles, ".*Note.*")
    noteFiles.size should be > 0
  }

  "HTML Utils" should "have no errors for valid HTML" in {
    val error = getErrors("<foo><bar>baz</bar></foo>", "test1.html")
    error.file shouldBe "test1.html"
    error.invalidChars.size shouldBe 0
  }

  "HTML Utils" should "have errors for invalid HTML" in {
    val error = getErrors("<foo><bar>βaz</foo>", "test2.html")
    error.file shouldBe "test2.html"
    error.invalidChars.size shouldBe 1
  }

  it should "extract table info" in {
    val keywords = conf.getStringList("he.keywords").toList
    val tableTypes: List[TableType] = keywords.map(new TableType(_))
    val jsd = getJSD("<html><body><table><tr><td>11</td><td>12</td></tr><tr><td>11</td><td>12</td></tr></table></body></html>", "WINDOWS-1252")
    val tables = getTables(jsd, "test3.html", tableTypes)
    tables.size shouldBe 1
    val table = tables.head
    table.file shouldBe "test3.html"
    table.cols shouldBe 2
    table.rows shouldBe 2
    table.emptyCells shouldBe 0
    table.images shouldBe 0
  }

  it should "extract table info with images and spaces" in {
    val keywords = conf.getStringList("he.keywords").toList
    val tableTypes: List[TableType] = keywords.map(new TableType(_))
    val jsd = getJSD("<html><body><table><tr><td>11</td><td>12</td></tr><tr><td><img src=\"test.png\"></td><td style=\" width:0.00pt; height:0.00pt;\"/></tr><tr></tr></table></body></html>", "WINDOWS-1252")
    val tables = getTables(jsd, "test4.html", tableTypes)
    tables.size shouldBe 1
    val table = tables.head
    table.file shouldBe "test4.html"
    table.cols shouldBe 2
    table.rows shouldBe 3
    table.emptyCells shouldBe 1
    table.images shouldBe 1
  }

  it should "extract multiple table info" in {
    val keywords = conf.getStringList("he.keywords").toList
    val tableTypes: List[TableType] = keywords.map(new TableType(_))
    val jsd = getJSD("<html><body><table><tr><td>11</td><td>12</td></tr><tr/></table><table><tr><td>11</td><td>12</td></tr><tr/></table></body></html>", "WINDOWS-1252")
    val tables = getTables(jsd, "test5.html", tableTypes)
    tables.size shouldBe 2
  }

  it should "extract nested table info" in {
    val keywords = conf.getStringList("he.keywords").toList
    val tableTypes: List[TableType] = keywords.map(new TableType(_))
    val jsd = getJSD("<html><body><table><tr><td>11</td><td><table><tr><td>11</td><td>12</td></tr><tr/></table></td></tr><tr/></table></body></html>", "WINDOWS-1252")
    val tables = getTables(jsd, "test6.html", tableTypes)
    tables.size shouldBe 2
  }
}
