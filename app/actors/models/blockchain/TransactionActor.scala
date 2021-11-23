package actors.models.blockchain

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.blockchain.Transaction
import models.common.Serializable.{Fee, StdMsg}
import play.api.Logger
import javax.inject.{Inject, Singleton}
import constants.Actor.{NUMBER_OF_SHARDS, NUMBER_OF_ENTITIES}

object TransactionActor {
  def props(blockchainTransactions: models.blockchain.Transactions) = Props(new TransactionActor(blockchainTransactions))

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateTransaction(id, _, _, _, _, _, _, _, _, _, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertMultipleTransaction(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateTransaction(id, _, _, _, _, _, _, _, _, _, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetTransaction(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetMessages(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetStatus(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetHeight(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetTransactions(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetTransactionsByAddress(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetTransactionsPerPageByAddress(id, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetNumberOfTransactions(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetNumberOfBlockTransactions(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetTransactionsPerPage(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateTransaction(id, _, _, _, _, _, _, _, _, _, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertMultipleTransaction(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateTransaction(id, _, _, _, _, _, _, _, _, _, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetTransaction(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetMessages(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetStatus(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetHeight(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetTransactions(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetTransactionsByAddress(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetTransactionsPerPageByAddress(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetNumberOfTransactions(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetNumberOfBlockTransactions(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetTransactionsPerPage(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }
}

@Singleton
class TransactionActor @Inject()(
                               blockchainTransactions: models.blockchain.Transactions
                             )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {

    case CreateTransaction(_, hash, height, code, rawLog, status, gasWanted, gasUsed, messages, fee, memo, timestamp) => {
      blockchainTransactions.Service.create(hash, height, code, rawLog, status, gasWanted, gasUsed, messages, fee, memo, timestamp) pipeTo sender()
    }
    case InsertMultipleTransaction(_, transactions) => {
      blockchainTransactions.Service.insertMultiple(transactions) pipeTo sender()
    }
    case InsertOrUpdateTransaction(_, hash, height, code, rawLog, status, gasWanted, gasUsed, messages, fee, memo, timestamp) => {
      blockchainTransactions.Service.insertOrUpdate(hash, height, code, rawLog, status, gasWanted, gasUsed, messages, fee, memo, timestamp) pipeTo sender()
    }
    case TryGetTransaction(_, hash) => {
      blockchainTransactions.Service.tryGet(hash) pipeTo sender()
    }
    case TryGetMessages(_, hash) => {
      blockchainTransactions.Service.tryGetMessages(hash) pipeTo sender()
    }
    case TryGetStatus(_, hash) => {
      blockchainTransactions.Service.tryGetStatus(hash) pipeTo sender()
    }
    case TryGetHeight(_, hash) => {
      blockchainTransactions.Service.tryGetHeight(hash) pipeTo sender()
    }
    case GetTransactions(_, height) => {
      blockchainTransactions.Service.getTransactions(height) pipeTo sender()
    }
    case GetTransactionsByAddress(_, address) => {
      blockchainTransactions.Service.getTransactionsByAddress(address) pipeTo sender()
    }
    case GetTransactionsPerPageByAddress(_, address, pageNumber) => {
      blockchainTransactions.Service.getTransactionsPerPageByAddress(address, pageNumber) pipeTo sender()
    }
    case GetNumberOfTransactions(_, height) => {
      blockchainTransactions.Service.getNumberOfTransactions(height) pipeTo sender()
    }
    case GetNumberOfBlockTransactions(_, blockHeights) => {
      blockchainTransactions.Service.getNumberOfTransactions(blockHeights) pipeTo sender()
    }
    case GetTransactionsPerPage(_, pageNumber) => {
      blockchainTransactions.Service.getTransactionsPerPage(pageNumber) pipeTo sender()
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
