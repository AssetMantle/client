package models.blockchain

import models.blockchain.Transactions.{CreateTransaction, GetNumberOfBlockTransactions, GetNumberOfTransactions, GetTransactions, GetTransactionsByAddress, GetTransactionsPerPage, GetTransactionsPerPageByAddress, InsertMultipleTransaction, InsertOrUpdateTransaction, TransactionActor, TryGetHeight, TryGetMessages, TryGetStatus, TryGetTransaction}
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import constants.Actor.{NUMBER_OF_ENTITIES, NUMBER_OF_SHARDS}
import exceptions.BaseException
import models.Trait.Logged
import models.common.Serializable.{Fee, StdMsg}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import models.Abstract.ShardedActorRegion

import java.sql.Timestamp
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.collection.immutable.ListMap
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Transaction(hash: String, height: Int, code: Int, rawLog: String, status: Boolean, gasWanted: String, gasUsed: String, messages: Seq[StdMsg], fee: Fee, memo: String, timestamp: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {

  def getSigners: Seq[String] = {
    var seen: Map[String, Boolean] = Map()
    var signers: Seq[String] = Seq()
    messages.foreach(message => message.getSigners.foreach(signer => {
      if (!seen.getOrElse(signer, false)) {
        signers = signers :+ signer
        seen = seen + (signer -> true)
      }
    }))
    signers
  }

  def getFeePayer: String = {
    val signers = getSigners
    if (signers.nonEmpty) signers.head else ""
  }

}

@Singleton
class Transactions @Inject()(
                              protected val databaseConfigProvider: DatabaseConfigProvider,
                              configuration: Configuration,
                              utilitiesOperations: utilities.Operations,
                            )(implicit executionContext: ExecutionContext) extends ShardedActorRegion {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION

  private val transactionsPerPage = configuration.get[Int]("blockchain.transactions.perPage")

  private val transactionsStatisticsBinWidth = configuration.get[Int]("statistics.transactions.binWidth")

  private val transactionsStatisticsTotalBins = configuration.get[Int]("statistics.transactions.totalBins")

  private val blockchainStartHeight = configuration.get[Int]("blockchain.startHeight")

  private val accountTransactionsPerPage = configuration.get[Int]("blockchain.account.transactions.perPage")

  import databaseConfig.profile.api._

  private[models] val transactionTable = TableQuery[TransactionTable]

  private val uniqueId: String = UUID.randomUUID().toString

  private implicit val timeout = Timeout(constants.Actor.ACTOR_ASK_TIMEOUT)

  override def idExtractor: ShardRegion.ExtractEntityId = {
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

  override def shardResolver: ShardRegion.ExtractShardId = {
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

  override def regionName: String = "transactionRegion"

  override def props: Props = Transactions.props(Transactions.this)

  case class TransactionSerialized(hash: String, height: Int, code: Int, rawLog: String, status: Boolean, gasWanted: String, gasUsed: String, messages: String, fee: String, memo: String, timestamp: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Transaction = Transaction(hash = hash, height = height, code = code, rawLog = rawLog, status = status, gasWanted = gasWanted, gasUsed = gasUsed, messages = utilities.JSON.convertJsonStringToObject[Seq[StdMsg]](messages), fee = utilities.JSON.convertJsonStringToObject[Fee](fee), memo = memo, timestamp = timestamp, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(transaction: Transaction): TransactionSerialized = TransactionSerialized(hash = transaction.hash, height = transaction.height, code = transaction.code, rawLog = transaction.rawLog, status = transaction.status, gasWanted = transaction.gasWanted, gasUsed = transaction.gasUsed, messages = Json.toJson(transaction.messages).toString, fee = Json.toJson(transaction.fee).toString, memo = transaction.memo, timestamp = transaction.timestamp, createdBy = transaction.createdBy, createdOn = transaction.createdOn, createdOnTimeZone = transaction.createdOnTimeZone, updatedBy = transaction.updatedBy, updatedOn = transaction.updatedOn, updatedOnTimeZone = transaction.updatedOnTimeZone)

  private def add(transaction: Transaction): Future[Int] = db.run((transactionTable returning transactionTable.map(_.height) += serialize(transaction)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.TRANSACTION_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(transactions: Seq[Transaction]): Future[Seq[Int]] = db.run((transactionTable returning transactionTable.map(_.height) ++= transactions.map(x => serialize(x))).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.TRANSACTION_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(transaction: Transaction): Future[Int] = db.run(transactionTable.insertOrUpdate(serialize(transaction)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.TRANSACTION_UPSERT_FAILED, psqlException)
    }
  }

  private def getTotalTransactionsNumber: Future[Int] = db.run(transactionTable.length.result)

  private def findTransactionsForAddress(address: String): Future[Seq[TransactionSerialized]] = db.run(transactionTable.filter(_.messages.like(s"""%$address%""")).sortBy(_.height.desc).result)

  private def findTransactionsPerPageForAddress(address: String, offset: Int, limit: Int): Future[Seq[TransactionSerialized]] = db.run(transactionTable.filter(_.messages.like(s"""%$address%""")).sortBy(_.height.desc).drop(offset).take(limit).result)

  private def tryGetTransactionByHash(hash: String): Future[TransactionSerialized] = db.run(transactionTable.filter(_.hash === hash).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.TRANSACTION_NOT_FOUND, noSuchElementException)
    }
  }

  private def getTransactionsByHeightList(heights: Seq[Int]): Future[Seq[TransactionSerialized]] = db.run(transactionTable.filter(_.height.inSet(heights)).result)

  private def getTransactionsNumberByHeightRange(start: Int, end: Int): Future[Int] = db.run(transactionTable.filter(x => x.height >= start && x.height <= end).length.result)

  private def getNumberOfTransactionsByHeight(height: Int): Future[Int] = db.run(transactionTable.filter(_.height === height).length.result)

  private def tryGetMessagesByHash(hash: String): Future[String] = db.run(transactionTable.filter(_.hash === hash).map(_.messages).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.TRANSACTION_NOT_FOUND, noSuchElementException)
    }
  }

  private def tryGetStatusByHash(hash: String): Future[Boolean] = db.run(transactionTable.filter(_.hash === hash).map(_.status).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.TRANSACTION_NOT_FOUND, noSuchElementException)
    }
  }

  private def tryGetHeightByHash(hash: String): Future[Int] = db.run(transactionTable.filter(_.hash === hash).map(_.height).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.TRANSACTION_NOT_FOUND, noSuchElementException)
    }
  }

  private def getTransactionsByHeight(height: Int): Future[Seq[TransactionSerialized]] = db.run(transactionTable.filter(_.height === height).result)

  private def getTransactionsForPageNumber(offset: Int, limit: Int): Future[Seq[TransactionSerialized]] = db.run(transactionTable.sortBy(_.height.desc).drop(offset).take(limit).result)

  private[models] class TransactionTable(tag: Tag) extends Table[TransactionSerialized](tag, "Transaction") {

    def * = (hash, height, code, rawLog, status, gasWanted, gasUsed, messages, fee, memo, timestamp, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (TransactionSerialized.tupled, TransactionSerialized.unapply)

    def hash = column[String]("hash", O.PrimaryKey)

    def height = column[Int]("height")

    def code = column[Int]("code")

    def rawLog = column[String]("rawLog")

    def status = column[Boolean]("status")

    def gasWanted = column[String]("gasWanted")

    def gasUsed = column[String]("gasUsed")

    def messages = column[String]("messages")

    def fee = column[String]("fee")

    def memo = column[String]("memo")

    def timestamp = column[String]("timestamp")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def createTransactionWithActor(hash: String, height: String, code: Int, rawLog: String, status: Boolean, gasWanted: String, gasUsed: String, messages: Seq[StdMsg], fee: Fee, memo: String, timestamp: String): Future[Int] = (actorRegion ? CreateTransaction(uniqueId, hash, height, code, rawLog, status, gasWanted, gasUsed, messages, fee, memo, timestamp)).mapTo[Int]

    def create(hash: String, height: String, code: Int, rawLog: String, status: Boolean, gasWanted: String, gasUsed: String, messages: Seq[StdMsg], fee: Fee, memo: String, timestamp: String): Future[Int] = add(Transaction(hash = hash, height = height.toInt, code = code, rawLog = rawLog, status = status, gasWanted = gasWanted, gasUsed = gasUsed, messages = messages, fee = fee, memo = memo, timestamp = timestamp))

    def insertMultipleTransactionWithActor(transactions: Seq[Transaction]): Future[Seq[Int]] = (actorRegion ? InsertMultipleTransaction(uniqueId, transactions)).mapTo[Seq[Int]]

    def insertMultiple(transactions: Seq[Transaction]): Future[Seq[Int]] = addMultiple(transactions)

    def insertOrUpdateTransactionWithActor(hash: String, height: String, code: Int, rawLog: String, status: Boolean, gasWanted: String, gasUsed: String, messages: Seq[StdMsg], fee: Fee, memo: String, timestamp: String): Future[Int] = (actorRegion ? InsertOrUpdateTransaction(uniqueId, hash, height, code, rawLog, status, gasWanted, gasUsed, messages, fee, memo, timestamp)).mapTo[Int]

    def insertOrUpdate(hash: String, height: String, code: Int, rawLog: String, status: Boolean, gasWanted: String, gasUsed: String, messages: Seq[StdMsg], fee: Fee, memo: String, timestamp: String): Future[Int] = upsert(Transaction(hash = hash, height = height.toInt, code = code, rawLog = rawLog, status = status, gasWanted = gasWanted, gasUsed = gasUsed, messages = messages, fee = fee, memo = memo, timestamp = timestamp))

    def tryGetTransactionWithActor(hash: String): Future[Transaction] = (actorRegion ? TryGetTransaction(uniqueId, hash)).mapTo[Transaction]

    def tryGet(hash: String): Future[Transaction] = tryGetTransactionByHash(hash).map(_.deserialize)

    def tryGetMessagesTransactionWithActor(hash: String): Future[Seq[StdMsg]] = (actorRegion ? TryGetMessages(uniqueId, hash)).mapTo[Seq[StdMsg]]

    def tryGetMessages(hash: String): Future[Seq[StdMsg]] = tryGetMessagesByHash(hash).map(x => utilities.JSON.convertJsonStringToObject[Seq[StdMsg]](x))

    def tryGetStatusTransactionWithActor(hash: String): Future[Transaction] = (actorRegion ? TryGetStatus(uniqueId, hash)).mapTo[Transaction]

    def tryGetStatus(hash: String): Future[Boolean] = tryGetStatusByHash(hash)

    def tryGetHeightTransactionWithActor(hash: String): Future[Transaction] = (actorRegion ? TryGetHeight(uniqueId, hash)).mapTo[Transaction]

    def tryGetHeight(hash: String): Future[Int] = tryGetHeightByHash(hash)

    def getTransactionsWithActor(height: Int): Future[Seq[Transaction]] = (actorRegion ? GetTransactions(uniqueId, height)).mapTo[Seq[Transaction]]

    def getTransactions(height: Int): Future[Seq[Transaction]] = getTransactionsByHeight(height).map(x => x.map(_.deserialize))

    def getTransactionsByAddressWithActor(address: String): Future[Seq[Transaction]] = (actorRegion ? GetTransactionsByAddress(uniqueId, address)).mapTo[Seq[Transaction]]

    def getTransactionsByAddress(address: String): Future[Seq[Transaction]] = findTransactionsForAddress(address).map(x => x.map(_.deserialize))

    def getTransactionsPerPageByAddressWithActor(address: String, pageNumber: Int): Future[Seq[Transaction]] = (actorRegion ? GetTransactionsPerPageByAddress(uniqueId, address, pageNumber)).mapTo[Seq[Transaction]]

    def getTransactionsPerPageByAddress(address: String, pageNumber: Int): Future[Seq[Transaction]] = findTransactionsPerPageForAddress(address = address, offset = (pageNumber - 1) * accountTransactionsPerPage, limit = accountTransactionsPerPage).map(x => x.map(_.deserialize))

    def getNumberOfTransactionsWithActor(height: Int): Future[Int] = (actorRegion ? GetNumberOfTransactions(uniqueId, height)).mapTo[Int]

    def getNumberOfTransactions(height: Int): Future[Int] = getNumberOfTransactionsByHeight(height)

    def getNumberOfBlockTransactionsWithActor(blockHeights: Seq[Int]): Future[Map[Int, Int]] = (actorRegion ? GetNumberOfBlockTransactions(uniqueId, blockHeights)).mapTo[Map[Int, Int]]

    def getNumberOfTransactions(blockHeights: Seq[Int]): Future[Map[Int, Int]] = {
      val transactions = getTransactionsByHeightList(blockHeights).map(_.map(_.deserialize))

      for {
        transactions <- transactions
      } yield blockHeights.map(height => height -> transactions.count(_.height == height)).toMap
    }

    def getTransactionsPerPageWithActor(pageNumber: Int): Future[Seq[Transaction]] = (actorRegion ? GetTransactionsPerPage(uniqueId, pageNumber)).mapTo[Seq[Transaction]]

    def getTransactionsPerPage(pageNumber: Int): Future[Seq[Transaction]] = getTransactionsForPageNumber(offset = (pageNumber - 1) * transactionsPerPage, limit = transactionsPerPage).map(_.map(_.deserialize))

    def getTotalTransactions: Future[Int] = getTotalTransactionsNumber

    def getTransactionStatisticsData(latestHeight: Int): Future[ListMap[String, Int]] = {
      val end = if (latestHeight % transactionsStatisticsBinWidth == 0) latestHeight else ((latestHeight / transactionsStatisticsBinWidth) + 1) * transactionsStatisticsBinWidth
      val start = if (end - transactionsStatisticsTotalBins * transactionsStatisticsBinWidth > blockchainStartHeight) end - transactionsStatisticsTotalBins * transactionsStatisticsBinWidth else blockchainStartHeight - 1
      val maxBins = (end - start + 1) / transactionsStatisticsBinWidth
      val totalBins = if (maxBins == transactionsStatisticsTotalBins) transactionsStatisticsTotalBins else maxBins
      val result = utilitiesOperations.traverse(0 until totalBins) { index =>
        val s = start + index * transactionsStatisticsBinWidth + 1
        val e = start + (index + 1) * transactionsStatisticsBinWidth
        val numTxs = getTransactionsNumberByHeightRange(s, e)
        for {
          numTxs <- numTxs
        } yield s"""${s/transactionsStatisticsBinWidth} - ${e/transactionsStatisticsBinWidth}""" -> numTxs
      }
      for {
        result <- result
      } yield ListMap(result.toList: _*)
    }
  }
}

object Transactions {
  def props(blockchainTransactions: models.blockchain.Transactions) (implicit executionContext: ExecutionContext) = Props(new TransactionActor(blockchainTransactions))

  @Singleton
  class TransactionActor @Inject()(
                                    blockchainTransactions: models.blockchain.Transactions
                                  ) (implicit executionContext: ExecutionContext) extends Actor with ActorLogging {
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
}