package com.he

import java.io.File
import java.nio.charset.Charset

import net.sf.ehcache.config.PersistenceConfiguration.Strategy
import net.sf.ehcache.config.{CacheConfiguration, PersistenceConfiguration}
import net.sf.ehcache.store.MemoryStoreEvictionPolicy
import net.sf.ehcache.{Cache, CacheManager}
import org.apache.commons.io.FileUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}

object HTMLUtils {
  val manager = CacheManager.create()

  //Create a Cache specifying its configuration.
  val htmlCache = new Cache(
    new CacheConfiguration("testCache", 0)
      .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
      .eternal(true)
      .diskExpiryThreadIntervalSeconds(0)
      .persistence(new PersistenceConfiguration().strategy(Strategy.LOCALTEMPSWAP)))
  manager.addCache(htmlCache)

  sys.addShutdownHook(manager.shutdown())

  val validChars = ('!' to '}').toSet ++ Set('\t', '\n', '\r', ' ')

  def getContent(file: File, charset: Charset): String = FileUtils.readFileToString(file, charset)

  def getJSD(content: String, charsetName: String): Document = Jsoup.parse(content, charsetName)

  def getTables(jsd: Document, name: String, tableTypes: List[TableType]): List[TableInfo] = {
    val key = s"${name}_tables"
    if (htmlCache.get(key) == null) {
      val elements = jsd.select("table")
      val result = (0 until elements.size()).map(elements.get).map { case element: Element =>
        val border = element.attr("border")
        val rows = element.select("tr")
        val cols = rows.get(0).select("td")
        val mid = if(rows.size()>=2) (rows.size()/2)-1 else 0
        val emptyCols = rows.get(mid).select("td[style=\" width:0.00pt;\"]")
        val images = element.select("img[src$=.png]")
        val colspans = element.select("td[colspan]")
        val colspan = if (colspans.size() > 0) colspans.get(0).attr("colspan") else "0"
        val rowspans = element.select("td[rowspan]")
        val rowspan = if (rowspans.size() > 0) rowspans.get(0).attr("rowspan") else "0"
        var visualTypeInfo = if ((colspan.isEmpty || colspan == "0") && (rowspan.isEmpty || rowspan == "0")) "simple" else "complex"
        visualTypeInfo = if (visualTypeInfo == "simple" && (cols.size()-emptyCols.size()) == 2) "form" else visualTypeInfo
        val emptyCells = element.select("td[style=\" width:0.00pt; height:0.00pt;\"]").size()
        val content = element.toString
        var tableTypeInfo = tableTypes.par.map(tt => TableContentTypeInfo(tt.name, tt.matches(content))).toList.sortWith(_.matchedKeywords.size > _.matchedKeywords.size).head
        if(tableTypeInfo.matchedKeywords.isEmpty) tableTypeInfo = TableContentTypeInfo("Unknown",Set.empty)
        val hasChild = element.select("table table").size() > 0
        val hasWideSpaces = content.contains("&nbsp;")
        val invalidChars = content.filterNot(validChars.contains).toSet
        val validHTML = true
        TableInfo(name, rows.size(), cols.size(), emptyCols.size(), images.size(), hasWideSpaces, invalidChars, emptyCells, border, validHTML, hasChild, colspan, rowspan, tableTypeInfo, visualTypeInfo)
      }.toList

      val element = new net.sf.ehcache.Element(key, result)
      htmlCache.put(element)
    }

    htmlCache.get(key).getObjectValue.asInstanceOf[List[TableInfo]]
  }

  def getErrors(content: String, name: String): ErrorInfo = {
    val key = s"${name}_errors"
    if (htmlCache.get(key) == null) {
      val invalidChars = content.filterNot(validChars.contains)
      val errorRatio = if (content.length == 0) 0 else invalidChars.length.toDouble / content.length.toDouble
      val result = ErrorInfo(name, errorRatio, invalidChars.toSet)

      val element = new net.sf.ehcache.Element(key, result)
      htmlCache.put(element)
    }

    htmlCache.get(key).getObjectValue.asInstanceOf[ErrorInfo]
  }
}
