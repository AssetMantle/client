package dbActors

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import models.blockchain.{Transaction}
import models.common.Serializable.{Fee, StdMsg}
import play.api.Logger

import javax.inject.{Inject, Singleton}

object TransactionActor {
  def props(blockchainTransaction: models.blockchain.Transactions) = Props(new TransactionActor(blockchainTransaction))
}

@Singleton
class TransactionActor @Inject()(
                               blockchainTransaction: models.blockchain.Transactions
                             )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {

    case CreateTransaction(hash, height, code, rawLog, status, gasWanted, gasUsed, messages, fee, memo, timestamp) => {
      blockchainTransaction.Service.create(hash, height, code, rawLog, status, gasWanted, gasUsed, messages, fee, memo, timestamp) pipeTo sender()
      println(self.path)
    }
    case InsertMultipleTransaction(transactions) => {
      blockchainTransaction.Service.insertMultiple(transactions) pipeTo sender()
      println(self.path)
    }
    case InsertOrUpdateTransaction(hash, height, code, rawLog, status, gasWanted, gasUsed, messages, fee, memo, timestamp) => {
      blockchainTransaction.Service.insertOrUpdate(hash, height, code, rawLog, status, gasWanted, gasUsed, messages, fee, memo, timestamp) pipeTo sender()
      println(self.path)
    }
    case TryGetTransaction(hash) => {
      blockchainTransaction.Service.tryGet(hash) pipeTo sender()
      println(self.path)
    }
    case TryGetMessages(hash) => {
      blockchainTransaction.Service.tryGetMessages(hash) pipeTo sender()
      println(self.path)
    }

    case TryGetStatus(hash) => {
      blockchainTransaction.Service.tryGetStatus(hash) pipeTo sender()
      println(self.path)
    }
    case TryGetHeight(hash) => {
      blockchainTransaction.Service.tryGetHeight(hash) pipeTo sender()
      println(self.path)
    }
    case GetTransactions(height) => {
      blockchainTransaction.Service.getTransactions(height) pipeTo sender()
      println(self.path)
    }
    case GetTransactionsByAddress(address) => {
      blockchainTransaction.Service.getTransactionsByAddress(address) pipeTo sender()
      println(self.path)
    }
    case GetTransactionsPerPageByAddress(address, pageNumber) => {
      blockchainTransaction.Service.getTransactionsPerPageByAddress(address, pageNumber) pipeTo sender()
      println(self.path)
    }
    case GetNumberOfTransactions(height) => {
      blockchainTransaction.Service.getNumberOfTransactions(height) pipeTo sender()
      println(self.path)
    }
    case GetNumberOfBlockTransactions(blockHeights) => {
      blockchainTransaction.Service.getNumberOfTransactions(blockHeights) pipeTo sender()
      println(self.path)
    }
    case GetTransactionsPerPage(pageNumber) => {
      blockchainTransaction.Service.getTransactionsPerPage(pageNumber) pipeTo sender()
      println(self.path)
    }
  }

}

case class CreateTransaction(hash: String, height: String, code: Int, rawLog: String, status: Boolean, gasWanted: String, gasUsed: String, messages: Seq[StdMsg], fee: Fee, memo: String, timestamp: String)
case class InsertMultipleTransaction(Transactions: Seq[Transaction])
case class InsertOrUpdateTransaction(hash: String, height: String, code: Int, rawLog: String, status: Boolean, gasWanted: String, gasUsed: String, messages: Seq[StdMsg], fee: Fee, memo: String, timestamp: String)
case class TryGetTransaction(hash: String)
case class TryGetMessages(hash: String)
case class TryGetStatus(hash: String)
case class TryGetHeight(hash: String)
case class GetTransactions(height: Int)
case class GetTransactionsByAddress(address: String)
case class GetTransactionsPerPageByAddress(address: String, pageNumber: Int)
case class GetNumberOfTransactions(height: Int)
case class GetNumberOfBlockTransactions(blockHeights: Seq[Int])
case class GetTransactionsPerPage(pageNumber: Int)
