package com.he

import java.io.File

import scala.annotation.tailrec

object DocumentUtils {

  def ls(rootDir: String): List[File] = {
    @tailrec
    def helper(parents: List[File], accum: List[File]): List[File] = parents match {
      case Nil => accum
      case head :: rest =>
        val (cDirs, cFiles) = head.listFiles.toList.partition(_.isDirectory)
        helper(cDirs ++: rest, accum ++: cFiles)
    }

    val root = new File(rootDir)
    if (root.isDirectory) helper(List(root), List.empty) else List.empty
  }

  def filterHTML(allFiles: List[File]): List[File] =
    allFiles.filter {
      case file: File =>
        val name = file.getName.toLowerCase
        name.endsWith(".html") || name.endsWith(".htm")
    }

  def filterPattern(allFiles: List[File], namePattern: String): List[File] = {
    import RegexUtils._
    val nameReg = namePattern.r
    allFiles.filter(nameReg matches _.getName)
  }

}
