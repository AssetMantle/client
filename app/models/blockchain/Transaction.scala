package models.blockchain

import com.google.protobuf.{Any => protoAny}
import com.cosmos.crypto.secp256k1
import com.cosmos.tx.v1beta1.Tx
import exceptions.BaseException
import models.traits.Logging
import models.common.Serializable.{Coin, Fee}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.Configuration
import play.api.Logger
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.collection.immutable.ListMap
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success}

case class Transaction(hash: String, height: Int, code: Int, gasWanted: String, gasUsed: String, txBytes: Array[Byte], log: String, createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging {

  lazy val parsedTx: Tx = Tx.parseFrom(txBytes)

  def status: Boolean = code == 0

  // Since Seq in scala is by default immutable and ordering is maintained, we can use these methods directly
  def getSigners: Seq[String] = parsedTx.getAuthInfo.getSignerInfosList.asScala.toSeq.map { signerInfo =>
    utilities.Crypto.convertAccountAddressBytesToBech32Address(secp256k1.PubKey.parseFrom(signerInfo.getPublicKey.getValue).getKey.toByteArray)
  }

  def getFeePayer: String = if (parsedTx.getAuthInfo.getFee.getPayer != "") parsedTx.getAuthInfo.getFee.getPayer else getSigners.headOption.getOrElse("")

  def getFeeGranter: String = parsedTx.getAuthInfo.getFee.getGranter

  def getMessages: Seq[protoAny] = parsedTx.getBody.getMessagesList.asScala.toSeq

  def getMessagesTypeURL: Seq[String] = this.getMessages.map(_.getTypeUrl)

  def getFee: Fee = {
    val fee = parsedTx.getAuthInfo.getFee
    Fee(amount = fee.getAmountList.asScala.toSeq.map(x => Coin(x)), gasLimit = fee.getGasLimit.toString, payer = fee.getPayer, granter = fee.getGranter)
  }

  def getMessageCounters: Map[String, Int] = parsedTx.getBody.getMessagesList.asScala.toSeq.map(stdMsg => constants.View.TxMessagesMap.getOrElse(stdMsg.getTypeUrl, stdMsg.getTypeUrl)).groupBy(identity).view.mapValues(_.size).toMap

  def getMemo: String = parsedTx.getBody.getMemo
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

  private def add(transaction: Transaction): Future[Int] = db.run((transactionTable returning transactionTable.map(_.height) += transaction).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.TRANSACTION_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(transactions: Seq[Transaction]): Future[Seq[Int]] = db.run((transactionTable returning transactionTable.map(_.height) ++= transactions).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.TRANSACTION_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(transaction: Transaction): Future[Int] = db.run(transactionTable.insertOrUpdate(transaction).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.TRANSACTION_UPSERT_FAILED, psqlException)
    }
  }

  private def getTotalTransactionsNumber: Future[Int] = db.run(transactionTable.length.result)

  private def tryGetTransactionByHash(hash: String): Future[Transaction] = db.run(transactionTable.filter(_.hash === hash).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.TRANSACTION_NOT_FOUND, noSuchElementException)
    }
  }

  private def getTransactionsByHeightList(heights: Seq[Int]): Future[Seq[Transaction]] = db.run(transactionTable.filter(_.height.inSet(heights)).result)

  private def getTransactionsNumberByHeightRange(start: Int, end: Int): Future[Int] = db.run(transactionTable.filter(x => x.height >= start && x.height <= end).length.result)

  private def getNumberOfTransactionsByHeight(height: Int): Future[Int] = db.run(transactionTable.filter(_.height === height).length.result)

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

  private def getTransactionsByHeight(height: Int): Future[Seq[Transaction]] = db.run(transactionTable.filter(_.height === height).result)

  private def getTransactionsByHashes(hashes: Seq[String]): Future[Seq[Transaction]] = db.run(transactionTable.filter(_.hash.inSet(hashes)).result)

  private def getTransactionsForPageNumber(offset: Int, limit: Int): Future[Seq[Transaction]] = db.run(transactionTable.sortBy(_.height.desc).drop(offset).take(limit).result)

  private[models] class TransactionTable(tag: Tag) extends Table[Transaction](tag, "Transaction") {

    def * = (hash, height, code, gasWanted, gasUsed, txBytes, log, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (Transaction.tupled, Transaction.unapply)

    def hash = column[String]("hash", O.PrimaryKey)

    def height = column[Int]("height")

    def code = column[Int]("code")

    def gasWanted = column[String]("gasWanted")

    def gasUsed = column[String]("gasUsed")

    def txBytes = column[Array[Byte]]("txBytes")

    def log = column[String]("log")


    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")
  }

  object Service {

    def create(hash: String, height: String, code: Int, log: String, gasWanted: String, gasUsed: String, txBytes: Array[Byte]): Future[Int] = add(Transaction(hash = hash, height = height.toInt, code = code, log = log, gasWanted = gasWanted, gasUsed = gasUsed, txBytes = txBytes))

    def insertMultiple(transactions: Seq[Transaction]): Future[Seq[Int]] = addMultiple(transactions)

    def insertOrUpdate(hash: String, height: String, code: Int, log: String, gasWanted: String, gasUsed: String, txBytes: Array[Byte]): Future[Int] = upsert(Transaction(hash = hash, height = height.toInt, code = code, log = log, gasWanted = gasWanted, gasUsed = gasUsed, txBytes = txBytes))

    def tryGet(hash: String): Future[Transaction] = tryGetTransactionByHash(hash)

    def getTransactions(height: Int): Future[Seq[Transaction]] = getTransactionsByHeight(height)

    def get(hashes: Seq[String]): Future[Seq[Transaction]] = getTransactionsByHashes(hashes)

    def getNumberOfTransactions(height: Int): Future[Int] = getNumberOfTransactionsByHeight(height)

    def getNumberOfTransactions(blockHeights: Seq[Int]): Future[Map[Int, Int]] = {
      val transactions = getTransactionsByHeightList(blockHeights)

      for {
        transactions <- transactions
      } yield blockHeights.map(height => height -> transactions.count(_.height == height)).toMap
    }

    def getTransactionsPerPage(pageNumber: Int): Future[Seq[Transaction]] = getTransactionsForPageNumber(offset = (pageNumber - 1) * transactionsPerPage, limit = transactionsPerPage)

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