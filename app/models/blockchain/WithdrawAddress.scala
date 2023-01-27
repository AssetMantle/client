package models.blockchain

import java.sql.Timestamp
import exceptions.BaseException

import javax.inject.{Inject, Singleton}
import models.traits.Logging
import com.cosmos.distribution.{v1beta1 => distributionTx}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.Configuration
import play.api.Logger
import queries.responses.common.Header
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class WithdrawAddress(delegatorAddress: String, withdrawAddress: String, createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging

@Singleton
class WithdrawAddresses @Inject()(
                                   protected val databaseConfigProvider: DatabaseConfigProvider,
                                   configuration: Configuration,
                                   blockchainBalances: Balances
                                 )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_WITHDRAW_ADDRESS

  import databaseConfig.profile.api._

  private[models] val withdrawAddressTable = TableQuery[WithdrawAddressTable]

  private def add(withdrawAddress: WithdrawAddress): Future[String] = db.run((withdrawAddressTable returning withdrawAddressTable.map(_.withdrawAddress) += withdrawAddress).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.SIGNING_INFO_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(withdrawAddress: Seq[WithdrawAddress]): Future[Seq[String]] = db.run((withdrawAddressTable returning withdrawAddressTable.map(_.withdrawAddress) ++= withdrawAddress).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.SIGNING_INFO_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(withdrawAddress: WithdrawAddress): Future[Int] = db.run(withdrawAddressTable.insertOrUpdate(withdrawAddress).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.SIGNING_INFO_UPSERT_FAILED, psqlException)
    }
  }

  private def findAll: Future[Seq[WithdrawAddress]] = db.run(withdrawAddressTable.result)

  private def tryGetByDelegatorAddress(delegatorAddress: String): Future[WithdrawAddress] = db.run(withdrawAddressTable.filter(_.delegatorAddress === delegatorAddress).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.SIGNING_INFO_NOT_FOUND, noSuchElementException)
    }
  }

  private def getByDelegatorAddress(delegatorAddress: String): Future[Option[String]] = db.run(withdrawAddressTable.filter(_.delegatorAddress === delegatorAddress).map(_.withdrawAddress).result.headOption)

  private[models] class WithdrawAddressTable(tag: Tag) extends Table[WithdrawAddress](tag, "WithdrawAddress") {

    def * = (delegatorAddress, withdrawAddress, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (WithdrawAddress.tupled, WithdrawAddress.unapply)

    def delegatorAddress = column[String]("delegatorAddress", O.PrimaryKey)

    def withdrawAddress = column[String]("withdrawAddress")

   def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")
  }

  object Service {

    def create(withdrawAddress: WithdrawAddress): Future[String] = add(withdrawAddress)

    def insertMultiple(withdrawAddress: Seq[WithdrawAddress]): Future[Seq[String]] = addMultiple(withdrawAddress)

    def insertOrUpdate(withdrawAddress: WithdrawAddress): Future[Int] = upsert(withdrawAddress)

    def getAll: Future[Seq[WithdrawAddress]] = findAll

    def get(delegatorAddress: String): Future[String] = getByDelegatorAddress(delegatorAddress).map(_.getOrElse(delegatorAddress))

  }

  object Utility {
    def onSetWithdrawAddress(setWithdrawAddress: distributionTx.MsgSetWithdrawAddress)(implicit header: Header): Future[String] = {
      val insert = Service.insertOrUpdate(WithdrawAddress(delegatorAddress = setWithdrawAddress.getDelegatorAddress, withdrawAddress = setWithdrawAddress.getWithdrawAddress))

      (for {
        _ <- insert
      } yield setWithdrawAddress.getDelegatorAddress).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.SET_WITHDRAW_ADDRESS + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          setWithdrawAddress.getDelegatorAddress
      }
    }

    def withdrawRewards(address: String): Future[Unit] = {
      val withdrawAddress = Service.get(address)

      def updateBalance(withdrawAddress: String) = blockchainBalances.Utility.insertOrUpdateBalance(withdrawAddress)

      (for {
        withdrawAddress <- withdrawAddress
        _ <- updateBalance(withdrawAddress)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }
  }

}