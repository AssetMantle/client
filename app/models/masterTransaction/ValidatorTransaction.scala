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

case class ValidatorTransaction(validatorAddress: String, txHash: String, height: Int, createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging

@Singleton
class ValidatorTransactions @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_VALIDATOR_TRANSACTION

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private val transactionsPerPage = configuration.get[Int]("blockchain.transactions.perPage")

  private[models] val validatorTransactionTable = TableQuery[ValidatorTransactionTable]

  private def create(validatorTransaction: ValidatorTransaction): Future[String] = db.run((validatorTransactionTable returning validatorTransactionTable.map(_.txHash) += validatorTransaction).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def create(validatorTransactions: Seq[ValidatorTransaction]) = db.run((validatorTransactionTable ++= validatorTransactions).asTry).map {
    case Success(result) => ()
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findValidatorTransactions(validatorAddress: String, offset: Int, limit: Int): Future[Seq[ValidatorTransaction]] = db.run(validatorTransactionTable.filter(_.validatorAddress === validatorAddress).sortBy(_.height.desc).drop(offset).take(limit).result)

  private[models] class ValidatorTransactionTable(tag: Tag) extends Table[ValidatorTransaction](tag, "ValidatorTransaction") {

    def * = (validatorAddress, txHash, height, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (ValidatorTransaction.tupled, ValidatorTransaction.unapply)

    def validatorAddress = column[String]("validatorAddress", O.PrimaryKey)

    def txHash = column[String]("txHash", O.PrimaryKey)

    def height = column[Int]("height")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")

  }

  object Service {

    def add(validatorTransactions: Seq[ValidatorTransaction]): Future[Unit] = create(validatorTransactions)

    def getTransactions(validatorAddress: String, pageNumber: Int): Future[Seq[ValidatorTransaction]] = findValidatorTransactions(validatorAddress = validatorAddress, offset = (pageNumber - 1) * transactionsPerPage, limit = transactionsPerPage)

  }

}