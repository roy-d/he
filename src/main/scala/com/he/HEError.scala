package com.he

sealed abstract class HEError(msg: String) extends Exception(msg) {
  def message: String
}

case class InvalidInput(message: String) extends HEError(message)
