package com

package object he {

  implicit class CSVWrapper(val table: TableInfo) extends AnyVal {
    def toCSV() = table.productIterator.map {
      case Some(value) => value
      case None => ""
      case x: Set[Char] => x.mkString("|")
      case tc: TableContentTypeInfo => tc.toCSV()
      case rest => rest
    }.mkString(",")
  }

  implicit class TCCSVWrapper(val table: TableContentTypeInfo) extends AnyVal {
    def toCSV() = table.productIterator.map {
      case Some(value) => value
      case x: Set[String] => x.mkString("|")
      case None => ""
      case rest => rest
    }.mkString(",")
  }

}
