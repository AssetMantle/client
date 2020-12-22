package models.blockchain

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Serializable.{Fee, StdMsg}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Transaction(hash: String, height: Int, code: Option[Int], rawLog: String, status: Boolean, gasWanted: String, gasUsed: String, messages: Seq[StdMsg], fee: Fee, timestamp: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Transactions @Inject()(
                              protected val databaseConfigProvider: DatabaseConfigProvider,
                              configuration: Configuration
                            )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION

  private val transactionsPerPage = configuration.get[Int]("blockchain.transactions.perPage")

  private val accountTransactionsPerPage = configuration.get[Int]("blockchain.account.transactions.perPage")

  import databaseConfig.profile.api._

  private[models] val transactionTable = TableQuery[TransactionTable]

  case class TransactionSerialized(hash: String, height: Int, code: Option[Int], rawLog: String, status: Boolean, gasWanted: String, gasUsed: String, messages: String, fee: String, timestamp: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Transaction = Transaction(hash = hash, height = height, code = code, rawLog = rawLog, status = status, gasWanted = gasWanted, gasUsed = gasUsed, messages = utilities.JSON.convertJsonStringToObject[Seq[StdMsg]](messages), fee = utilities.JSON.convertJsonStringToObject[Fee](fee), timestamp = timestamp, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(transaction: Transaction): TransactionSerialized = TransactionSerialized(hash = transaction.hash, height = transaction.height, code = transaction.code, rawLog = transaction.rawLog, status = transaction.status, gasWanted = transaction.gasWanted, gasUsed = transaction.gasUsed, messages = Json.toJson(transaction.messages).toString, fee = Json.toJson(transaction.fee).toString, timestamp = transaction.timestamp, createdBy = transaction.createdBy, createdOn = transaction.createdOn, createdOnTimeZone = transaction.createdOnTimeZone, updatedBy = transaction.updatedBy, updatedOn = transaction.updatedOn, updatedOnTimeZone = transaction.updatedOnTimeZone)

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

  //TODO messages.like
  private def findTransactionsForAddress(address: String): Future[Seq[TransactionSerialized]] = db.run(transactionTable.filter(_.messages.like(s"""%$address%""")).sortBy(_.height.desc).result)

  //TODO messages.like
  private def findTransactionsPerPageForAddress(address: String, offset: Int, limit: Int): Future[Seq[TransactionSerialized]] = db.run(transactionTable.filter(_.messages.like(s"""%$address%""")).sortBy(_.height.desc).drop(offset).take(limit).result)

  private def tryGetTransactionByHash(hash: String): Future[TransactionSerialized] = db.run(transactionTable.filter(_.hash === hash).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.TRANSACTION_NOT_FOUND, noSuchElementException)
    }
  }

  private def getTransactionsByHeightList(heights: Seq[Int]): Future[Seq[TransactionSerialized]] = db.run(transactionTable.filter(_.height.inSet(heights)).result)

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

  private def getTransactionsByHeight(height: Int): Future[Seq[TransactionSerialized]] = db.run(transactionTable.filter(_.height === height).result)

  private def getTransactionsForPageNumber(offset: Int, limit: Int): Future[Seq[TransactionSerialized]] = db.run(transactionTable.sortBy(_.height.desc).drop(offset).take(limit).result)

  private[models] class TransactionTable(tag: Tag) extends Table[TransactionSerialized](tag, "Transaction") {

    def * = (hash, height, code.?, rawLog, status, gasWanted, gasUsed, messages, fee, timestamp, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (TransactionSerialized.tupled, TransactionSerialized.unapply)

    def hash = column[String]("hash", O.PrimaryKey)

    def height = column[Int]("height")

    def code = column[Int]("code")

    def rawLog = column[String]("rawLog")

    def status = column[Boolean]("status")

    def gasWanted = column[String]("gasWanted")

    def gasUsed = column[String]("gasUsed")

    def messages = column[String]("messages")

    def fee = column[String]("fee")

    def timestamp = column[String]("timestamp")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(hash: String, height: String, code: Option[Int], rawLog: String, status: Boolean, gasWanted: String, gasUsed: String, messages: Seq[StdMsg], fee: Fee, timestamp: String): Future[Int] = add(Transaction(hash = hash, height = height.toInt, code = code, rawLog = rawLog, status = status, gasWanted = gasWanted, gasUsed = gasUsed, messages = messages, fee = fee, timestamp = timestamp))

    def insertMultiple(transactions: Seq[Transaction]): Future[Seq[Int]] = addMultiple(transactions)

    def insertOrUpdate(hash: String, height: String, code: Option[Int], rawLog: String, status: Boolean, gasWanted: String, gasUsed: String, messages: Seq[StdMsg], fee: Fee, timestamp: String): Future[Int] = upsert(Transaction(hash = hash, height = height.toInt, code = code, rawLog = rawLog, status = status, gasWanted = gasWanted, gasUsed = gasUsed, messages = messages, fee = fee, timestamp = timestamp))

    def tryGet(hash: String): Future[Transaction] = tryGetTransactionByHash(hash).map(_.deserialize)

    def tryGetMessages(hash: String): Future[Seq[StdMsg]] = tryGetMessagesByHash(hash).map(x => utilities.JSON.convertJsonStringToObject[Seq[StdMsg]](x))

    def tryGetStatus(hash: String): Future[Boolean] = tryGetStatusByHash(hash)

    def getTransactions(height: Int): Future[Seq[Transaction]] = getTransactionsByHeight(height).map(x => x.map(_.deserialize))

    def getTransactionsByAddress(address: String): Future[Seq[Transaction]] = findTransactionsForAddress(address).map(x => x.map(_.deserialize))

    def getTransactionsPerPageByAddress(address: String, pageNumber: Int): Future[Seq[Transaction]] = findTransactionsPerPageForAddress(address = address, offset = (pageNumber - 1) * accountTransactionsPerPage, limit = accountTransactionsPerPage).map(x => x.map(_.deserialize))

    def getNumberOfTransactions(height: Int): Future[Int] = getNumberOfTransactionsByHeight(height)

    def getNumberOfTransactions(blockHeights: Seq[Int]): Future[Map[Int, Int]] = {
      val transactions = getTransactionsByHeightList(blockHeights).map(_.map(_.deserialize))

      for {
        transactions <- transactions
      } yield blockHeights.map(height => height -> transactions.count(_.height == height)).toMap
    }

    def getTransactionsPerPage(pageNumber: Int): Future[Seq[Transaction]] = getTransactionsForPageNumber(offset = (pageNumber - 1) * transactionsPerPage, limit = transactionsPerPage).map(_.map(_.deserialize))
  }

}