package actors.models

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.blockchain.Transaction
import models.common.Serializable.{Fee, StdMsg}
import play.api.Logger

import javax.inject.{Inject, Singleton}

object TransactionActor {
  def props(blockchainTransaction: models.blockchain.Transactions) = Props(new TransactionActor(blockchainTransaction))

  val  numberOfShards = 10
  val numberOfEntities = 100

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateTransaction(id, _, _, _, _, _, _, _, _, _, _, _) => (id, attempt)
    case attempt@InsertMultipleTransaction(id, _) => (id, attempt)
    case attempt@InsertOrUpdateTransaction(id, _, _, _, _, _, _, _, _, _, _, _) => (id, attempt)
    case attempt@TryGetTransaction(id, _) => (id, attempt)
    case attempt@TryGetMessages(id, _) => (id, attempt)
    case attempt@TryGetStatus(id, _) => (id, attempt)
    case attempt@TryGetHeight(id, _) => (id, attempt)
    case attempt@GetTransactions(id, _) => (id, attempt)
    case attempt@GetTransactionsByAddress(id, _) => (id, attempt)
    case attempt@GetTransactionsPerPageByAddress(id, _, _) => (id, attempt)
    case attempt@GetNumberOfTransactions(id, _) => (id, attempt)
    case attempt@GetNumberOfBlockTransactions(id, _) => (id, attempt)
    case attempt@GetTransactionsPerPage(id, _) => (id, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateTransaction(id, _, _, _, _, _, _, _, _, _, _, _) => (id.hashCode % numberOfShards).toString
    case InsertMultipleTransaction(id, _) => (id.hashCode % numberOfShards).toString
    case InsertOrUpdateTransaction(id, _, _, _, _, _, _, _, _, _, _, _) => (id.hashCode % numberOfShards).toString
    case TryGetTransaction(id, _) => (id.hashCode % numberOfShards).toString
    case TryGetMessages(id, _) => (id.hashCode % numberOfShards).toString
    case TryGetStatus(id, _) => (id.hashCode % numberOfShards).toString
    case TryGetHeight(id, _) => (id.hashCode % numberOfShards).toString
    case GetTransactions(id, _) => (id.hashCode % numberOfShards).toString
    case GetTransactionsByAddress(id, _) => (id.hashCode % numberOfShards).toString
    case GetTransactionsPerPageByAddress(id, _, _) => (id.hashCode % numberOfShards).toString
    case GetNumberOfTransactions(id, _) => (id.hashCode % numberOfShards).toString
    case GetNumberOfBlockTransactions(id, _) => (id.hashCode % numberOfShards).toString
    case GetTransactionsPerPage(id, _) => (id.hashCode % numberOfShards).toString
  }
}

@Singleton
class TransactionActor @Inject()(
                               blockchainTransaction: models.blockchain.Transactions
                             )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {

    case CreateTransaction(_, hash, height, code, rawLog, status, gasWanted, gasUsed, messages, fee, memo, timestamp) => {
      blockchainTransaction.Service.create(hash, height, code, rawLog, status, gasWanted, gasUsed, messages, fee, memo, timestamp) pipeTo sender()
    }
    case InsertMultipleTransaction(_, transactions) => {
      blockchainTransaction.Service.insertMultiple(transactions) pipeTo sender()
    }
    case InsertOrUpdateTransaction(_, hash, height, code, rawLog, status, gasWanted, gasUsed, messages, fee, memo, timestamp) => {
      blockchainTransaction.Service.insertOrUpdate(hash, height, code, rawLog, status, gasWanted, gasUsed, messages, fee, memo, timestamp) pipeTo sender()
    }
    case TryGetTransaction(_, hash) => {
      blockchainTransaction.Service.tryGet(hash) pipeTo sender()
    }
    case TryGetMessages(_, hash) => {
      blockchainTransaction.Service.tryGetMessages(hash) pipeTo sender()
    }
    case TryGetStatus(_, hash) => {
      blockchainTransaction.Service.tryGetStatus(hash) pipeTo sender()
    }
    case TryGetHeight(_, hash) => {
      blockchainTransaction.Service.tryGetHeight(hash) pipeTo sender()
    }
    case GetTransactions(_, height) => {
      blockchainTransaction.Service.getTransactions(height) pipeTo sender()
    }
    case GetTransactionsByAddress(_, address) => {
      blockchainTransaction.Service.getTransactionsByAddress(address) pipeTo sender()
    }
    case GetTransactionsPerPageByAddress(_, address, pageNumber) => {
      blockchainTransaction.Service.getTransactionsPerPageByAddress(address, pageNumber) pipeTo sender()
    }
    case GetNumberOfTransactions(_, height) => {
      blockchainTransaction.Service.getNumberOfTransactions(height) pipeTo sender()
    }
    case GetNumberOfBlockTransactions(_, blockHeights) => {
      blockchainTransaction.Service.getNumberOfTransactions(blockHeights) pipeTo sender()
    }
    case GetTransactionsPerPage(_, pageNumber) => {
      blockchainTransaction.Service.getTransactionsPerPage(pageNumber) pipeTo sender()
    }
  }
}

case class CreateTransaction(uid: String, hash: String, height: String, code: Int, rawLog: String, status: Boolean, gasWanted: String, gasUsed: String, messages: Seq[StdMsg], fee: Fee, memo: String, timestamp: String)
case class InsertMultipleTransaction(uid: String, Transactions: Seq[Transaction])
case class InsertOrUpdateTransaction(uid: String, hash: String, height: String, code: Int, rawLog: String, status: Boolean, gasWanted: String, gasUsed: String, messages: Seq[StdMsg], fee: Fee, memo: String, timestamp: String)
case class TryGetTransaction(uid: String, hash: String)
case class TryGetMessages(uid: String, hash: String)
case class TryGetStatus(uid: String, hash: String)
case class TryGetHeight(uid: String, hash: String)
case class GetTransactions(uid: String, height: Int)
case class GetTransactionsByAddress(uid: String, address: String)
case class GetTransactionsPerPageByAddress(uid: String, address: String, pageNumber: Int)
case class GetNumberOfTransactions(uid: String, height: Int)
case class GetNumberOfBlockTransactions(uid: String, blockHeights: Seq[Int])
case class GetTransactionsPerPage(uid: String, pageNumber: Int)
