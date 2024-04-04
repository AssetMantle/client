package models.archive

import com.cosmos.crypto.{secp256k1, secp256r1}
import com.cosmos.tx.v1beta1.Tx
import com.google.protobuf.{Any => protoAny}
import exceptions.BaseException
import models.blockchain
import models.common.Serializable.{Coin, Fee}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success}

case class Transaction(hash: String, height: Int, code: Int, gasWanted: String, gasUsed: String, txBytes: Array[Byte], log: Option[String]) {

  def toTx: blockchain.Transaction = blockchain.Transaction(hash = hash, height = height, code = code, gasWanted = gasWanted, gasUsed = gasUsed, txBytes = txBytes, log = log, processed = true)

  lazy val parsedTx: Tx = Tx.parseFrom(txBytes)

  def status: Boolean = code == 0

  // Since Seq in scala is by default immutable and ordering is maintained, we can use these methods directly
  def getSigners: Seq[String] = parsedTx.getAuthInfo.getSignerInfosList.asScala.toSeq.map { signerInfo =>
    signerInfo.getPublicKey.getTypeUrl match {
      case schema.constants.PublicKey.SINGLE_SECP256K1 => utilities.Crypto.convertAccountPublicKeyToAccountAddress(secp256k1.PubKey.parseFrom(signerInfo.getPublicKey.getValue).getKey.toByteArray)
      case schema.constants.PublicKey.SINGLE_SECP256R1 => utilities.Crypto.convertAccountPublicKeyToAccountAddress(secp256r1.PubKey.parseFrom(signerInfo.getPublicKey.getValue).getKey.toByteArray)
      case schema.constants.PublicKey.MULTI_SIG => "mantle1glk8f6xhs0u440xwfwqgn68aps6l85v9sn4efz"
    }
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

  private implicit val module: String = constants.Module.ARCHIVE_TRANSACTION

  private val transactionsPerPage = configuration.get[Int]("blockchain.transactions.perPage")

  import databaseConfig.profile.api._

  private[models] val transactionTable = TableQuery[TransactionTable]

  private def addMultiple(transactions: Seq[Transaction]): Future[Seq[Int]] = db.run((transactionTable returning transactionTable.map(_.height) ++= transactions).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.TRANSACTION_INSERT_FAILED, psqlException)
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

  private def getNumberOfTransactionsByHeight(height: Int): Future[Int] = db.run(transactionTable.filter(_.height === height).length.result)

  private def getTransactionsByHeight(height: Int): Future[Seq[Transaction]] = db.run(transactionTable.filter(_.height === height).result)

  private def getTransactionsByHashes(hashes: Seq[String]): Future[Seq[Transaction]] = db.run(transactionTable.filter(_.hash.inSet(hashes)).result)

  private def getTransactionsForPageNumber(offset: Int, limit: Int): Future[Seq[Transaction]] = db.run(transactionTable.sortBy(_.height.desc).drop(offset).take(limit).result)

  private[models] class TransactionTable(tag: Tag) extends Table[Transaction](tag, Option("archive"), "Transaction") {

    def * = (hash, height, code, gasWanted, gasUsed, txBytes, log.?) <> (Transaction.tupled, Transaction.unapply)

    def hash = column[String]("hash", O.PrimaryKey)

    def height = column[Int]("height")

    def code = column[Int]("code")

    def gasWanted = column[String]("gasWanted")

    def gasUsed = column[String]("gasUsed")

    def txBytes = column[Array[Byte]]("txBytes")

    def log = column[String]("log")

  }

  object Service {
    private var lastArchiveHeight = 0

    def getLastArchiveHeight: Int = lastArchiveHeight

    def create(transactions: Seq[Transaction], endHeight: Int): Future[Seq[Int]] = {
      lastArchiveHeight = endHeight
      addMultiple(transactions)
    }

    def setLastArchiveHeight(value: Int): Unit = {
      lastArchiveHeight = value
    }

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

  }
}