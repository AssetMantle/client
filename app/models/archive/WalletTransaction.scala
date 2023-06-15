package models.archive

import exceptions.BaseException
import models.masterTransaction
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class WalletTransaction(address: String, txHash: String, height: Int) {
  def toWalletTx: masterTransaction.WalletTransaction = masterTransaction.WalletTransaction(address = address, txHash = txHash, height = height)
}

@Singleton
class WalletTransactions @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.ARCHIVE_WALLET_TRANSACTION

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private val transactionsPerPage = configuration.get[Int]("blockchain.transactions.perPage")

  private[models] val walletTransactionTable = TableQuery[WalletTransactionTable]

  private def create(walletTransactions: Seq[WalletTransaction]): Future[Seq[Int]] = db.run((walletTransactionTable returning walletTransactionTable.map(_.height) ++= walletTransactions).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findWalletTransactions(address: String, offset: Int, limit: Int): Future[Seq[WalletTransaction]] = db.run(walletTransactionTable.filter(_.address === address).sortBy(_.height.desc).drop(offset).take(limit).result)

  private[models] class WalletTransactionTable(tag: Tag) extends Table[WalletTransaction](tag, Option("archive"), "WalletTransaction") {

    def * = (address, txHash, height) <> (WalletTransaction.tupled, WalletTransaction.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def txHash = column[String]("txHash", O.PrimaryKey)

    def height = column[Int]("height")
  }

  object Service {

    def add(walletTransactions: Seq[WalletTransaction]): Future[Seq[Int]] = create(walletTransactions)

    def getTransactions(address: String, offset: Int, limit: Int): Future[Seq[WalletTransaction]] = findWalletTransactions(address = address, offset = offset, limit = limit)

  }

  object Utility {

  }
}