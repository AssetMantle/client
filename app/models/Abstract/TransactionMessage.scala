package models.Abstract

abstract class TransactionMessage {
  def getSigners: Seq[String]
}
