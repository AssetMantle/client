package models.blockchain

import akka.util.Timeout
import akka.cluster.sharding.ShardRegion

import java.sql.Timestamp
import exceptions.BaseException
import akka.pattern.{ask, pipe}

import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.blockchain.Delegations.{CreateDelegation, DeleteDelegation, GetAllDelegationForDelegator, GetAllDelegationForValidator, GetDelegation, InsertMultipleDelegation, InsertOrUpdateDelegation}
import akka.actor.{Actor, ActorLogging, Props}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.blockchain.GetValidatorDelegatorDelegation
import slick.jdbc.JdbcProfile

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import constants.Actor.{NUMBER_OF_ENTITIES, NUMBER_OF_SHARDS}
import models.Abstract.ShardedActorRegion


case class Delegation(delegatorAddress: String, validatorAddress: String, shares: BigDecimal, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Delegations @Inject()(
                             protected val databaseConfigProvider: DatabaseConfigProvider,
                             configuration: Configuration,
                             getValidatorDelegatorDelegation: GetValidatorDelegatorDelegation,
                           )(implicit executionContext: ExecutionContext) extends ShardedActorRegion {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_DELEGATION

  private val uniqueId: String = UUID.randomUUID().toString

  import databaseConfig.profile.api._

  private[models] val delegationTable = TableQuery[DelegationTable]

  private implicit val timeout = Timeout(constants.Actor.ACTOR_ASK_TIMEOUT)

  override def idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateDelegation(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertMultipleDelegation(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateDelegation(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllDelegationForDelegator(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllDelegationForValidator(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetDelegation(uid, _, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@DeleteDelegation(uid, _, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  override def shardResolver: ShardRegion.ExtractShardId = {
    case CreateDelegation(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertMultipleDelegation(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateDelegation(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllDelegationForDelegator(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllDelegationForValidator(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetDelegation(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case DeleteDelegation(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }

  override def regionName: String = "delegationRegion"

  override def props: Props = Delegations.props(Delegations.this)
  
  private val responseErrorDelegationNotFound: String = constants.Response.PREFIX + constants.Response.FAILURE_PREFIX + configuration.get[String]("blockchain.response.error.delegationNotFound")

  private def add(delegation: Delegation): Future[String] = db.run((delegationTable returning delegationTable.map(_.delegatorAddress) += delegation).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.DELEGATION_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(delegations: Seq[Delegation]): Future[Seq[String]] = db.run((delegationTable returning delegationTable.map(_.delegatorAddress) ++= delegations).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.DELEGATION_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(delegation: Delegation): Future[Int] = db.run(delegationTable.insertOrUpdate(delegation).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.DELEGATION_UPSERT_FAILED, psqlException)
    }
  }

  private def getAllByDelegatorAddress(delegatorAddress: String): Future[Seq[Delegation]] = db.run(delegationTable.filter(_.delegatorAddress === delegatorAddress).result)

  private def getAllByValidatorAddress(validatorAddress: String): Future[Seq[Delegation]] = db.run(delegationTable.filter(_.validatorAddress === validatorAddress).result)

  private def getByDelegatorAndValidatorAddress(delegatorAddress: String, validatorAddress: String): Future[Option[Delegation]] = db.run(delegationTable.filter(x => x.delegatorAddress === delegatorAddress && x.validatorAddress === validatorAddress).result.headOption)

  private def deleteByAddress(delegatorAddress: String, validatorAddress: String): Future[Int] = db.run(delegationTable.filter(x => x.delegatorAddress === delegatorAddress && x.validatorAddress === validatorAddress).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.DELEGATION_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.DELEGATION_DELETE_FAILED, noSuchElementException)
    }
  }

  private[models] class DelegationTable(tag: Tag) extends Table[Delegation](tag, "Delegation") {

    def * = (delegatorAddress, validatorAddress, shares, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Delegation.tupled, Delegation.unapply)

    def delegatorAddress = column[String]("delegatorAddress", O.PrimaryKey)

    def validatorAddress = column[String]("validatorAddress", O.PrimaryKey)

    def shares = column[BigDecimal]("shares")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def createDelegationWithActor(delegation: Delegation): Future[String] = (actorRegion ? CreateDelegation(uniqueId, delegation)).mapTo[String]

    def create(delegation: Delegation): Future[String] = add(delegation)

    def insertMultipleDelegationWithActor(delegations: Seq[Delegation]): Future[String] = (actorRegion ? InsertMultipleDelegation(uniqueId, delegations)).mapTo[String]

    def insertMultiple(delegations: Seq[Delegation]): Future[Seq[String]] = addMultiple(delegations)

    def insertOrUpdateDelegationWithActor(delegation: Delegation): Future[String] = (actorRegion ? InsertOrUpdateDelegation(uniqueId, delegation)).mapTo[String]

    def insertOrUpdate(delegation: Delegation): Future[Int] = upsert(delegation)

    def getAllDelegationForDelegatorWithActor(address: String): Future[Seq[Delegation]] = (actorRegion ? GetAllDelegationForDelegator(uniqueId, address)).mapTo[Seq[Delegation]]

    def getAllForDelegator(address: String): Future[Seq[Delegation]] = getAllByDelegatorAddress(address)

    def getAllDelegationForValidatorWithActor(address: String): Future[Seq[Delegation]] = (actorRegion ? GetAllDelegationForValidator(uniqueId, address)).mapTo[Seq[Delegation]]

    def getAllForValidator(operatorAddress: String): Future[Seq[Delegation]] = getAllByValidatorAddress(operatorAddress)

    def getDelegationWithActor(delegatorAddress: String, operatorAddress: String): Future[Option[Delegation]] = (actorRegion ? GetDelegation(uniqueId, delegatorAddress, operatorAddress)).mapTo[Option[Delegation]]

    def get(delegatorAddress: String, operatorAddress: String): Future[Option[Delegation]] = getByDelegatorAndValidatorAddress(delegatorAddress = delegatorAddress, validatorAddress = operatorAddress)

    def deleteDelegationWithActor(delegatorAddress: String, operatorAddress: String): Future[Option[Delegation]] = (actorRegion ? DeleteDelegation(uniqueId, delegatorAddress, operatorAddress)).mapTo[Option[Delegation]]

    def delete(delegatorAddress: String, validatorAddress: String): Future[Int] = deleteByAddress(delegatorAddress = delegatorAddress, validatorAddress = validatorAddress)

  }

  object Utility {

    //onDelegation moved to blockchain/Validators due to import cycle issues

    def insertOrUpdate(delegatorAddress: String, validatorAddress: String): Future[Unit] = {
      val delegationResponse = getValidatorDelegatorDelegation.Service.get(delegatorAddress = delegatorAddress, validatorAddress = validatorAddress)

      def insertDelegation(delegation: Delegation) = Service.insertOrUpdate(delegation)

      (for {
        delegationResponse <- delegationResponse
        _ <- insertDelegation(delegationResponse.delegation_response.delegation.toDelegation)
      } yield ()).recover {
        // It's fine if responseErrorDelegationNotFound exception comes, happens when syncing from block 1
        case baseException: BaseException => if (!baseException.failure.message.matches(responseErrorDelegationNotFound)) throw baseException else logger.info(baseException.failure.logMessage)
      }
    }

    def updateOrDelete(delegatorAddress: String, validatorAddress: String): Future[Unit] = {
      val delegationResponse = getValidatorDelegatorDelegation.Service.get(delegatorAddress = delegatorAddress, validatorAddress = validatorAddress)

      def insertDelegation(delegation: Delegation) = Service.insertOrUpdate(delegation)

      (for {
        delegationResponse <- delegationResponse
        _ <- insertDelegation(delegationResponse.delegation_response.delegation.toDelegation)
      } yield ()).recover {
        case baseException: BaseException => if (baseException.failure.message.matches(responseErrorDelegationNotFound)) {
          val delete = Service.delete(delegatorAddress = delegatorAddress, validatorAddress = validatorAddress)
          (for {
            _ <- delete
          } yield ()
            ).recover {
            case baseException: BaseException => logger.info(baseException.failure.logMessage)
          }
        } else throw baseException
      }
    }
  }

}

object  Delegations {
  def props(blockchainDelegations: models.blockchain.Delegations) (implicit executionContext: ExecutionContext) = Props(new DelegationActor(blockchainDelegations))
 
  @Singleton
  class DelegationActor @Inject()(
                                   blockchainDelegations: models.blockchain.Delegations
                                 ) (implicit executionContext: ExecutionContext) extends Actor with ActorLogging {
    private implicit val logger: Logger = Logger(this.getClass)

    override def receive: Receive = {
      case CreateDelegation(_, delegation) => {
        blockchainDelegations.Service.create(delegation) pipeTo sender()
      }
      case InsertMultipleDelegation(_, delegations) => {
        blockchainDelegations.Service.insertMultiple(delegations) pipeTo sender()
      }
      case InsertOrUpdateDelegation(_, delegation) => {
        blockchainDelegations.Service.insertOrUpdate(delegation) pipeTo sender()
      }
      case GetDelegation(_, delegatorAddress, operatorAddress) => {
        blockchainDelegations.Service.get(delegatorAddress, operatorAddress) pipeTo sender()
      }
      case GetAllDelegationForDelegator(_, address) => {
        blockchainDelegations.Service.getAllForDelegator(address) pipeTo sender()
      }
      case GetAllDelegationForValidator(_, operatorAddress) => {
        blockchainDelegations.Service.getAllForValidator(operatorAddress) pipeTo sender()
      }
      case DeleteDelegation(_, delegatorAddress, operatorAddress) => {
        blockchainDelegations.Service.delete(delegatorAddress, operatorAddress) pipeTo sender()
      }
    }
  }

  case class CreateDelegation(id: String, delegation: Delegation)
  case class InsertMultipleDelegation(id: String, delegation: Seq[Delegation])
  case class InsertOrUpdateDelegation(id: String, delegation: Delegation)
  case class GetAllDelegationForDelegator(id: String, address: String)
  case class GetAllDelegationForValidator(id: String, operatorAddress: String)
  case class GetDelegation(id: String, delegatorAddress: String, operatorAddress: String)
  case class DeleteDelegation(id: String, delegatorAddress: String, operatorAddress: String)
}