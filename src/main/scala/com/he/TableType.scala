package com.he

class TableType(keywordString: String) {

  private val parts: Array[String] = keywordString.split(",")
  val name: String = parts.head
  val keywords: Set[String] = parts.map(_.toLowerCase).toSet

  def matches(tableContent: String): Set[String] = {
    val lowerContent = tableContent.toLowerCase
    val hits: Set[String] = keywords.filter(lowerContent.contains(_))
    if (hits.isEmpty) Set.empty else hits
  }
}
