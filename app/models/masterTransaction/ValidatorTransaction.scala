package models.masterTransaction

import com.cosmos.distribution.{v1beta1 => distributionTx}
import com.cosmos.slashing.{v1beta1 => slashingTx}
import com.cosmos.staking.{v1beta1 => stakingTx}
import exceptions.BaseException
import models.traits.Logging
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ValidatorTransaction(address: String, txHash: String, height: Int, createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging

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

  private def findValidatorTransactions(address: String, offset: Int, limit: Int): Future[Seq[ValidatorTransaction]] = db.run(validatorTransactionTable.filter(_.address === address).sortBy(_.height.desc).drop(offset).take(limit).result)

  private[models] class ValidatorTransactionTable(tag: Tag) extends Table[ValidatorTransaction](tag, "ValidatorTransaction") {

    def * = (address, txHash, height, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (ValidatorTransaction.tupled, ValidatorTransaction.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def txHash = column[String]("txHash", O.PrimaryKey)

    def height = column[Int]("height")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")

  }

  object Service {

    def add(validatorTransactions: Seq[ValidatorTransaction]): Future[Unit] = create(validatorTransactions)

    def getTransactions(address: String, pageNumber: Int): Future[Seq[ValidatorTransaction]] = findValidatorTransactions(address = address, offset = (pageNumber - 1) * transactionsPerPage, limit = transactionsPerPage)

  }

  object Utility {

    def addForTransactions(txs: Seq[models.blockchain.Transaction], height: Int): Future[Unit] = {
      val validatorTransactions = txs.map { tx =>
        val txAddresses = tx.getMessages.flatMap { stdMsg =>
          stdMsg.getTypeUrl match {
            case constants.Blockchain.TransactionMessage.WITHDRAW_VALIDATOR_COMMISSION => Option(distributionTx.MsgWithdrawValidatorCommission.parseFrom(stdMsg.getValue).getValidatorAddress)
            case constants.Blockchain.TransactionMessage.UNJAIL => Option(slashingTx.MsgUnjail.parseFrom(stdMsg.getValue).getValidatorAddr)
            case constants.Blockchain.TransactionMessage.CREATE_VALIDATOR => Option(stakingTx.MsgCreateValidator.parseFrom(stdMsg.getValue).getValidatorAddress)
            case constants.Blockchain.TransactionMessage.EDIT_VALIDATOR => Option(stakingTx.MsgEditValidator.parseFrom(stdMsg.getValue).getValidatorAddress)
            case _ => None
          }
        }
        txAddresses.distinct.map(x => ValidatorTransaction(address = x, txHash = tx.hash, height = height))
      }

      Service.add(validatorTransactions.flatten)
    }

  }
}