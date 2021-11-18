package models.blockchain

import akka.pattern.ask
import akka.util.Timeout
import actors.models.{ BlockActor, ClassificationActor, CreateDelegation, DelegationActor, DeleteDelegation, GetAllDelegationForDelegator, GetAllDelegationForValidator, GetDelegation, InsertMultipleDelegation, InsertOrUpdateDelegation}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}

import java.sql.Timestamp
import exceptions.BaseException

import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries._
import queries.blockchain.GetValidatorDelegatorDelegation
import slick.jdbc.JdbcProfile

import java.util.UUID
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Delegation(delegatorAddress: String, validatorAddress: String, shares: BigDecimal, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Delegations @Inject()(
                             protected val databaseConfigProvider: DatabaseConfigProvider,
                             configuration: Configuration,
                             getValidatorDelegatorDelegation: GetValidatorDelegatorDelegation,
                           )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_DELEGATION

  private val uniqueId: String = UUID.randomUUID().toString

  import databaseConfig.profile.api._

  private[models] val delegationTable = TableQuery[DelegationTable]

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

    implicit val timeout = Timeout(5 seconds) // needed for `?` below

    private val delegationActorRegion = {
      ClusterSharding(actors.models.Service.actorSystem).start(
        typeName = "delegationRegion",
        entityProps = DelegationActor.props(Delegations.this),
        settings = ClusterShardingSettings(actors.models.Service.actorSystem),
        extractEntityId = DelegationActor.idExtractor,
        extractShardId = DelegationActor.shardResolver
      )
    }

    def createDelegationWithActor(delegation: Delegation): Future[String] = (delegationActorRegion ? CreateDelegation(uniqueId, delegation)).mapTo[String]

    def create(delegation: Delegation): Future[String] = add(delegation)

    def insertMultipleDelegationWithActor(delegations: Seq[Delegation]): Future[String] = (delegationActorRegion ? InsertMultipleDelegation(uniqueId, delegations)).mapTo[String]

    def insertMultiple(delegations: Seq[Delegation]): Future[Seq[String]] = addMultiple(delegations)

    def insertOrUpdateDelegationWithActor(delegation: Delegation): Future[String] = (delegationActorRegion ? InsertOrUpdateDelegation(uniqueId, delegation)).mapTo[String]

    def insertOrUpdate(delegation: Delegation): Future[Int] = upsert(delegation)

    def getAllDelegationForDelegatorWithActor(address: String): Future[Seq[Delegation]] = (delegationActorRegion ? GetAllDelegationForDelegator(uniqueId, address)).mapTo[Seq[Delegation]]

    def getAllForDelegator(address: String): Future[Seq[Delegation]] = getAllByDelegatorAddress(address)

    def getAllDelegationForValidatorWithActor(address: String): Future[Seq[Delegation]] = (delegationActorRegion ? GetAllDelegationForValidator(uniqueId, address)).mapTo[Seq[Delegation]]

    def getAllForValidator(operatorAddress: String): Future[Seq[Delegation]] = getAllByValidatorAddress(operatorAddress)

    def getDelegationWithActor(delegatorAddress: String, operatorAddress: String): Future[Option[Delegation]] = (delegationActorRegion ? GetDelegation(uniqueId, delegatorAddress, operatorAddress)).mapTo[Option[Delegation]]

    def get(delegatorAddress: String, operatorAddress: String): Future[Option[Delegation]] = getByDelegatorAndValidatorAddress(delegatorAddress = delegatorAddress, validatorAddress = operatorAddress)

    def deleteDelegationWithActor(delegatorAddress: String, operatorAddress: String): Future[Option[Delegation]] = (delegationActorRegion ? DeleteDelegation(uniqueId, delegatorAddress, operatorAddress)).mapTo[Option[Delegation]]

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