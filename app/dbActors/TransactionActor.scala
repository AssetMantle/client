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
    }
    case InsertMultipleTransaction(transactions) => {
      blockchainTransaction.Service.insertMultiple(transactions) pipeTo sender()
    }
    case InsertOrUpdateTransaction(hash, height, code, rawLog, status, gasWanted, gasUsed, messages, fee, memo, timestamp) => {
      blockchainTransaction.Service.insertOrUpdate(hash, height, code, rawLog, status, gasWanted, gasUsed, messages, fee, memo, timestamp) pipeTo sender()
    }
    case TryGetTransaction(hash) => {
      blockchainTransaction.Service.tryGet(hash) pipeTo sender()
    }
    case TryGetMessages(hash) => {
      blockchainTransaction.Service.tryGetMessages(hash) pipeTo sender()
    }
    case TryGetStatus(hash) => {
      blockchainTransaction.Service.tryGetStatus(hash) pipeTo sender()
    }
    case TryGetHeight(hash) => {
      blockchainTransaction.Service.tryGetHeight(hash) pipeTo sender()
    }
    case GetTransactions(height) => {
      blockchainTransaction.Service.getTransactions(height) pipeTo sender()
    }
    case GetTransactionsByAddress(address) => {
      blockchainTransaction.Service.getTransactionsByAddress(address) pipeTo sender()
    }
    case GetTransactionsPerPageByAddress(address, pageNumber) => {
      blockchainTransaction.Service.getTransactionsPerPageByAddress(address, pageNumber) pipeTo sender()
    }
    case GetNumberOfTransactions(height) => {
      blockchainTransaction.Service.getNumberOfTransactions(height) pipeTo sender()
    }
    case GetNumberOfBlockTransactions(blockHeights) => {
      blockchainTransaction.Service.getNumberOfTransactions(blockHeights) pipeTo sender()
    }
    case GetTransactionsPerPage(pageNumber) => {
      blockchainTransaction.Service.getTransactionsPerPage(pageNumber) pipeTo sender()
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
