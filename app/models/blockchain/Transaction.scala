package models.blockchain

import exceptions.BaseException
import models.Trait.Logged
import models.common.Serializable.Fee
import models.common.TransactionMessages.StdMsg
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import utilities.Date.RFC3339

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.collection.immutable.ListMap
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Transaction(hash: String, height: Int, code: Int, rawLog: String, gasWanted: String, gasUsed: String, messages: Seq[StdMsg], fee: Fee, memo: String, timestamp: RFC3339, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {

  def status: Boolean = code == 0

  // Since Seq in scala is by default immutable and ordering is maintained, we can use these methods directly
  def getSigners: Seq[String] = messages.flatMap(_.getSigners).distinct

  def getFeePayer: String = if (fee.payer != "") fee.payer else getSigners.headOption.getOrElse("")

  def getFeeGranter: String = fee.granter

}

@Singleton
class Transactions @Inject()(
                              protected val databaseConfigProvider: DatabaseConfigProvider,
                              configuration: Configuration,
                              utilitiesOperations: utilities.Operations,
                            )(implicit executionContext: ExecutionContext) {

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

  case class TransactionSerialized(hash: String, height: Int, code: Int, rawLog: String, gasWanted: String, gasUsed: String, messages: String, fee: String, memo: String, timestamp: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Transaction = Transaction(hash = hash, height = height, code = code, rawLog = rawLog, gasWanted = gasWanted, gasUsed = gasUsed, messages = utilities.JSON.convertJsonStringToObject[Seq[StdMsg]](messages), fee = utilities.JSON.convertJsonStringToObject[Fee](fee), memo = memo, timestamp = RFC3339(timestamp), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(transaction: Transaction): TransactionSerialized = TransactionSerialized(hash = transaction.hash, height = transaction.height, code = transaction.code, rawLog = transaction.rawLog, gasWanted = transaction.gasWanted, gasUsed = transaction.gasUsed, messages = Json.toJson(transaction.messages).toString, fee = Json.toJson(transaction.fee).toString, memo = transaction.memo, timestamp = transaction.timestamp.toString, createdBy = transaction.createdBy, createdOn = transaction.createdOn, createdOnTimeZone = transaction.createdOnTimeZone, updatedBy = transaction.updatedBy, updatedOn = transaction.updatedOn, updatedOnTimeZone = transaction.updatedOnTimeZone)

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

  private def tryGetCodeByHash(hash: String): Future[Int] = db.run(transactionTable.filter(_.hash === hash).map(_.code).result.head.asTry).map {
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

    def * = (hash, height, code, rawLog, gasWanted, gasUsed, messages, fee, memo, timestamp, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (TransactionSerialized.tupled, TransactionSerialized.unapply)

    def hash = column[String]("hash", O.PrimaryKey)

    def height = column[Int]("height")

    def code = column[Int]("code")

    def rawLog = column[String]("rawLog")

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

    def create(hash: String, height: String, code: Int, rawLog: String, gasWanted: String, gasUsed: String, messages: Seq[StdMsg], fee: Fee, memo: String, timestamp: RFC3339): Future[Int] = add(Transaction(hash = hash, height = height.toInt, code = code, rawLog = rawLog, gasWanted = gasWanted, gasUsed = gasUsed, messages = messages, fee = fee, memo = memo, timestamp = timestamp))

    def insertMultiple(transactions: Seq[Transaction]): Future[Seq[Int]] = addMultiple(transactions)

    def insertOrUpdate(hash: String, height: String, code: Int, rawLog: String, gasWanted: String, gasUsed: String, messages: Seq[StdMsg], fee: Fee, memo: String, timestamp: RFC3339): Future[Int] = upsert(Transaction(hash = hash, height = height.toInt, code = code, rawLog = rawLog, gasWanted = gasWanted, gasUsed = gasUsed, messages = messages, fee = fee, memo = memo, timestamp = timestamp))

    def tryGet(hash: String): Future[Transaction] = tryGetTransactionByHash(hash).map(_.deserialize)

    def tryGetMessages(hash: String): Future[Seq[StdMsg]] = tryGetMessagesByHash(hash).map(x => utilities.JSON.convertJsonStringToObject[Seq[StdMsg]](x))

    def tryGetStatus(hash: String): Future[Boolean] = tryGetCodeByHash(hash).map(x => x == 0)

    def tryGetHeight(hash: String): Future[Int] = tryGetHeightByHash(hash)

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
        } yield s"""${s / transactionsStatisticsBinWidth} - ${e / transactionsStatisticsBinWidth}""" -> numTxs
      }
      for {
        result <- result
      } yield ListMap(result.toList: _*)
    }
  }
}