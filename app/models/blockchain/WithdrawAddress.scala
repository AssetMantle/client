package models.blockchain

import akka.pattern.ask
import akka.util.Timeout
import actors.models.blockchain
import actors.models.blockchain.{CreateWithdrawAddress, GetAllWithdrawAddresses, GetWithdrawAddress, InsertMultipleWithdrawAddress, InsertOrUpdateWithdrawAddress, WithdrawAddressActor}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}

import java.sql.Timestamp
import exceptions.BaseException

import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.TransactionMessages.SetWithdrawAddress
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.responses.common.Header
import slick.jdbc.JdbcProfile

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class WithdrawAddress(delegatorAddress: String, withdrawAddress: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

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

  private val uniqueId: String = UUID.randomUUID().toString

  private implicit val timeout = Timeout(constants.Actor.ACTOR_ASK_TIMEOUT)

  private val withdrawAddressActorRegion = {
    ClusterSharding(blockchain.Service.actorSystem).start(
      typeName = "withdrawAddressRegion",
      entityProps = WithdrawAddressActor.props(WithdrawAddresses.this),
      settings = ClusterShardingSettings(blockchain.Service.actorSystem),
      extractEntityId = WithdrawAddressActor.idExtractor,
      extractShardId = WithdrawAddressActor.shardResolver
    )
  }

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

    def * = (delegatorAddress, withdrawAddress, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (WithdrawAddress.tupled, WithdrawAddress.unapply)

    def delegatorAddress = column[String]("delegatorAddress", O.PrimaryKey)

    def withdrawAddress = column[String]("withdrawAddress")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def createWithdrawAddressWithActor(withdrawAddress: WithdrawAddress): Future[String] = (withdrawAddressActorRegion ? CreateWithdrawAddress(uniqueId, withdrawAddress)).mapTo[String]

    def create(withdrawAddress: WithdrawAddress): Future[String] = add(withdrawAddress)

    def insertMultipleWithdrawAddressWithActor(withdrawAddress: Seq[WithdrawAddress]): Future[Seq[String]] = (withdrawAddressActorRegion ? InsertMultipleWithdrawAddress(uniqueId, withdrawAddress)).mapTo[Seq[String]]

    def insertMultiple(withdrawAddress: Seq[WithdrawAddress]): Future[Seq[String]] = addMultiple(withdrawAddress)

    def insertOrUpdateWithdrawAddressWithActor(withdrawAddress: WithdrawAddress): Future[Int] = (withdrawAddressActorRegion ? InsertOrUpdateWithdrawAddress(uniqueId, withdrawAddress)).mapTo[Int]

    def insertOrUpdate(withdrawAddress: WithdrawAddress): Future[Int] = upsert(withdrawAddress)

    def getAllWithdrawAddressesWithActor: Future[Seq[WithdrawAddress]] = (withdrawAddressActorRegion ? GetAllWithdrawAddresses(uniqueId)).mapTo[Seq[WithdrawAddress]]

    def getAll: Future[Seq[WithdrawAddress]] = findAll

    def getWithdrawAddressWithActor(delegatorAddress: String): Future[String] = (withdrawAddressActorRegion ? GetWithdrawAddress(uniqueId, delegatorAddress)).mapTo[String]

    def get(delegatorAddress: String): Future[String] = getByDelegatorAddress(delegatorAddress).map(_.getOrElse(delegatorAddress))

  }

  object Utility {
    def onSetWithdrawAddress(setWithdrawAddress: SetWithdrawAddress)(implicit header: Header): Future[Unit] = {
      val insert = Service.insertOrUpdate(WithdrawAddress(delegatorAddress = setWithdrawAddress.delegatorAddress, withdrawAddress = setWithdrawAddress.withdrawAddress))

      (for {
        _ <- insert
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.SET_WITHDRAW_ADDRESS + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
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