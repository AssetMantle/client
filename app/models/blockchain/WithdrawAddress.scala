package models.blockchain

import akka.pattern.{ask, pipe}
import akka.util.Timeout
import models.blockchain.WithdrawAddresses.{CreateWithdrawAddress, GetAllWithdrawAddresses, GetWithdrawAddress, InsertMultipleWithdrawAddress, InsertOrUpdateWithdrawAddress, WithdrawAddressActor}
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.{ShardRegion}
import constants.Actor.{NUMBER_OF_ENTITIES, NUMBER_OF_SHARDS}

import java.sql.Timestamp
import exceptions.BaseException
import models.Abstract.ShardedActorRegion

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
                                 )(implicit executionContext: ExecutionContext) extends ShardedActorRegion {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_WITHDRAW_ADDRESS

  import databaseConfig.profile.api._

  private[models] val withdrawAddressTable = TableQuery[WithdrawAddressTable]

  private val uniqueId: String = UUID.randomUUID().toString

  private implicit val timeout = Timeout(constants.Actor.ACTOR_ASK_TIMEOUT)

  override def idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateWithdrawAddress(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertMultipleWithdrawAddress(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateWithdrawAddress(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllWithdrawAddresses(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetWithdrawAddress(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  override def shardResolver: ShardRegion.ExtractShardId = {
    case CreateWithdrawAddress(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertMultipleWithdrawAddress(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateWithdrawAddress(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllWithdrawAddresses(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetWithdrawAddress(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }

  override def regionName: String = "withdrawAddressRegion"

  override def props: Props = WithdrawAddresses.props(WithdrawAddresses.this)

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

    def createWithdrawAddressWithActor(withdrawAddress: WithdrawAddress): Future[String] = (actorRegion ? CreateWithdrawAddress(uniqueId, withdrawAddress)).mapTo[String]

    def create(withdrawAddress: WithdrawAddress): Future[String] = add(withdrawAddress)

    def insertMultipleWithdrawAddressWithActor(withdrawAddress: Seq[WithdrawAddress]): Future[Seq[String]] = (actorRegion ? InsertMultipleWithdrawAddress(uniqueId, withdrawAddress)).mapTo[Seq[String]]

    def insertMultiple(withdrawAddress: Seq[WithdrawAddress]): Future[Seq[String]] = addMultiple(withdrawAddress)

    def insertOrUpdateWithdrawAddressWithActor(withdrawAddress: WithdrawAddress): Future[Int] = (actorRegion ? InsertOrUpdateWithdrawAddress(uniqueId, withdrawAddress)).mapTo[Int]

    def insertOrUpdate(withdrawAddress: WithdrawAddress): Future[Int] = upsert(withdrawAddress)

    def getAllWithdrawAddressesWithActor: Future[Seq[WithdrawAddress]] = (actorRegion ? GetAllWithdrawAddresses(uniqueId)).mapTo[Seq[WithdrawAddress]]

    def getAll: Future[Seq[WithdrawAddress]] = findAll

    def getWithdrawAddressWithActor(delegatorAddress: String): Future[String] = (actorRegion ? GetWithdrawAddress(uniqueId, delegatorAddress)).mapTo[String]

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

object WithdrawAddresses {
  def props(blockchainWithdrawAddresses: models.blockchain.WithdrawAddresses) (implicit executionContext: ExecutionContext) = Props(new WithdrawAddressActor(blockchainWithdrawAddresses))

  @Singleton
  class WithdrawAddressActor @Inject()(
                                        blockchainWithdrawAddresses: models.blockchain.WithdrawAddresses
                                      ) (implicit executionContext: ExecutionContext) extends Actor with ActorLogging {
    private implicit val logger: Logger = Logger(this.getClass)

    override def receive: Receive = {
      case CreateWithdrawAddress(_, withdrawAddress) => {
        blockchainWithdrawAddresses.Service.create(withdrawAddress) pipeTo sender()
      }
      case InsertMultipleWithdrawAddress(_, withdrawAddresses) => {
        blockchainWithdrawAddresses.Service.insertMultiple(withdrawAddresses) pipeTo sender()
      }
      case InsertOrUpdateWithdrawAddress(_, withdrawAddress) => {
        blockchainWithdrawAddresses.Service.insertOrUpdate(withdrawAddress) pipeTo sender()
      }
      case GetAllWithdrawAddresses(_) => {
        blockchainWithdrawAddresses.Service.getAll pipeTo sender()
      }
      case GetWithdrawAddress(_, delegatorAddress) => {
        blockchainWithdrawAddresses.Service.get(delegatorAddress) pipeTo sender()
      }
    }
  }

  case class CreateWithdrawAddress(uid: String, withdrawAddress: WithdrawAddress)
  case class InsertMultipleWithdrawAddress(uid: String, withdrawAddresses: Seq[WithdrawAddress])
  case class InsertOrUpdateWithdrawAddress(uid: String, withdrawAddress: WithdrawAddress)
  case class GetAllWithdrawAddresses(uid: String)
  case class GetWithdrawAddress(uid: String, delegatorAddress: String)
}