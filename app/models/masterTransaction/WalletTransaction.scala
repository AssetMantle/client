package models.masterTransaction

import exceptions.BaseException
import models.Trait.Logging
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.collection.Seq
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class WalletTransaction(walletAddress: String, txHash: String, height: Int, createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging

@Singleton
class WalletTransactions @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_WALLET_TRANSACTION

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private val transactionsPerPage = configuration.get[Int]("blockchain.transactions.perPage")

  private[models] val walletTransactionTable = TableQuery[WalletTransactionTable]

  private def create(walletTransaction: WalletTransaction): Future[String] = db.run((walletTransactionTable returning walletTransactionTable.map(_.txHash) += walletTransaction).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def create(walletTransactions: Seq[WalletTransaction]) = db.run((walletTransactionTable ++= walletTransactions).asTry).map {
    case Success(result) => ()
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findWalletTransactions(walletAddress: String, offset: Int, limit: Int): Future[Seq[WalletTransaction]] = db.run(walletTransactionTable.filter(_.walletAddress === walletAddress).sortBy(_.height.desc).drop(offset).take(limit).result)

  private[models] class WalletTransactionTable(tag: Tag) extends Table[WalletTransaction](tag, "WalletTransaction") {

    def * = (walletAddress, txHash, height, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (WalletTransaction.tupled, WalletTransaction.unapply)

    def walletAddress = column[String]("walletAddress", O.PrimaryKey)

    def txHash = column[String]("txHash", O.PrimaryKey)

    def height = column[Int]("height")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")

  }

  object Service {

    def add(walletTransactions: Seq[WalletTransaction]): Future[Unit] = create(walletTransactions)

    def getTransactions(walletAddress: String, pageNumber: Int): Future[Seq[WalletTransaction]] = findWalletTransactions(walletAddress = walletAddress, offset = (pageNumber - 1) * transactionsPerPage, limit = transactionsPerPage)

  }

}