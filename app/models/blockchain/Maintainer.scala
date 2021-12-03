package models.blockchain

import akka.pattern.{ask, pipe}
import akka.util.Timeout
import models.blockchain.Maintainers.{CreateMaintainer, DeleteMaintainer, GetAllMaintainer, GetMaintainer, InsertMultipleMaintainer, InsertOrUpdateMaintainer, MaintainerActor, TryGetMaintainer}
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.{ShardRegion}
import constants.Actor.{NUMBER_OF_ENTITIES, NUMBER_OF_SHARDS}
import exceptions.BaseException
import models.Abstract.ShardedActorRegion
import models.Trait.Logged
import models.common.Serializable._
import models.common.TransactionMessages.MaintainerDeputize
import models.master
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.responses.common.Header
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Maintainer(id: String, maintainedTraits: Mutables, addMaintainer: Boolean, removeMaintainer: Boolean, mutateMaintainer: Boolean, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {
  def getClassificationID: String = id.split(constants.RegularExpression.BLOCKCHAIN_FIRST_ORDER_COMPOSITE_ID_SEPARATOR)(0)

  def getIdentityID: String = id.split(constants.RegularExpression.BLOCKCHAIN_FIRST_ORDER_COMPOSITE_ID_SEPARATOR)(1)
}

@Singleton
class Maintainers @Inject()(
                             protected val databaseConfigProvider: DatabaseConfigProvider,
                             configuration: Configuration,
                             masterClassifications: master.Classifications
                           )(implicit executionContext: ExecutionContext) extends ShardedActorRegion {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_MAINTAINER

  private val uniqueId:String = UUID.randomUUID().toString

  import databaseConfig.profile.api._

  private implicit val timeout = Timeout(constants.Actor.ACTOR_ASK_TIMEOUT)

  override def idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateMaintainer(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetMaintainer(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertMultipleMaintainer(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateMaintainer(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllMaintainer(uid) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetMaintainer(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@DeleteMaintainer(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  override def shardResolver: ShardRegion.ExtractShardId = {
    case CreateMaintainer(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetMaintainer(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertMultipleMaintainer(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateMaintainer(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllMaintainer(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetMaintainer(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case DeleteMaintainer(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }

  override def regionName: String = "maintainerRegion"

  override def props: Props = Maintainers.props(Maintainers.this)

  case class MaintainerSerialized(id: String, maintainedTraits: String, addMaintainer: Boolean, removeMaintainer: Boolean, mutateMaintainer: Boolean, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Maintainer = Maintainer(id = id, maintainedTraits = utilities.JSON.convertJsonStringToObject[Mutables](maintainedTraits), addMaintainer = addMaintainer, removeMaintainer = removeMaintainer, mutateMaintainer = mutateMaintainer, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(maintainer: Maintainer): MaintainerSerialized = MaintainerSerialized(id = maintainer.id, maintainedTraits = Json.toJson(maintainer.maintainedTraits).toString, addMaintainer = maintainer.addMaintainer, removeMaintainer = maintainer.removeMaintainer, mutateMaintainer = maintainer.mutateMaintainer, createdBy = maintainer.createdBy, createdOn = maintainer.createdOn, createdOnTimeZone = maintainer.createdOnTimeZone, updatedBy = maintainer.updatedBy, updatedOn = maintainer.updatedOn, updatedOnTimeZone = maintainer.updatedOnTimeZone)

  private[models] val maintainerTable = TableQuery[MaintainerTable]

  private def add(maintainer: Maintainer): Future[String] = db.run((maintainerTable returning maintainerTable.map(_.id) += serialize(maintainer)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ORDER_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(maintainers: Seq[Maintainer]): Future[Seq[String]] = db.run((maintainerTable returning maintainerTable.map(_.id) ++= maintainers.map(x => serialize(x))).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ORDER_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(maintainer: Maintainer): Future[Int] = db.run(maintainerTable.insertOrUpdate(serialize(maintainer)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ORDER_UPSERT_FAILED, psqlException)
    }
  }

  private def tryGetByID(id: String) = db.run(maintainerTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ORDER_NOT_FOUND, noSuchElementException)
    }
  }

  private def getByID(id: String) = db.run(maintainerTable.filter(_.id === id).result.headOption)

  private def getAllMaintainers = db.run(maintainerTable.result)

  private def deleteByID(id: String): Future[Int] = db.run(maintainerTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ORDER_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ORDER_DELETE_FAILED, noSuchElementException)
    }
  }

  private[models] class MaintainerTable(tag: Tag) extends Table[MaintainerSerialized](tag, "Maintainer_BC") {

    def * = (id, maintainedTraits, addMaintainer, removeMaintainer, mutateMaintainer, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (MaintainerSerialized.tupled, MaintainerSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def maintainedTraits = column[String]("maintainedTraits")

    def addMaintainer = column[Boolean]("addMaintainer")

    def removeMaintainer = column[Boolean]("removeMaintainer")

    def mutateMaintainer = column[Boolean]("mutateMaintainer")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def createMaintainerWithActor(maintainer: Maintainer): Future[String] = (actorRegion ? CreateMaintainer(uniqueId, maintainer)).mapTo[String]

    def create(maintainer: Maintainer): Future[String] = add(maintainer)

    def tryGetWithActor(id: String): Future[Maintainer] = (actorRegion ? TryGetMaintainer(uniqueId, id)).mapTo[Maintainer]

    def tryGet(id: String): Future[Maintainer] = tryGetByID(id).map(_.deserialize)

    def getMaintainerWithActor(id: String): Future[Option[Maintainer]] = (actorRegion ? GetMaintainer(uniqueId, id)).mapTo[Option[Maintainer]]

    def get(id: String): Future[Option[Maintainer]] = getByID(id).map(_.map(_.deserialize))

    def getAllMaintainerWithActor: Future[Seq[Maintainer]] = (actorRegion ? GetAllMaintainer(uniqueId)).mapTo[Seq[Maintainer]]

    def getAll: Future[Seq[Maintainer]] = getAllMaintainers.map(_.map(_.deserialize))

    def insertMultipleMaintainersWithActor(maintainers: Seq[Maintainer]): Future[Seq[String]] = (actorRegion ? InsertMultipleMaintainer(uniqueId, maintainers)).mapTo[Seq[String]]

    def insertMultiple(maintainers: Seq[Maintainer]): Future[Seq[String]] = addMultiple(maintainers)

    def insertOrUpdateMaintainerWithActor(maintainer: Maintainer): Future[Seq[String]] = (actorRegion ? InsertOrUpdateMaintainer(uniqueId, maintainer)).mapTo[Seq[String]]

    def insertOrUpdate(maintainer: Maintainer): Future[Int] = upsert(maintainer)

    def deleteMaintainer(id: String): Future[Int] = (actorRegion ? DeleteMaintainer(uniqueId, id)).mapTo[Int]

    def delete(id: String): Future[Int] = deleteByID(id)
  }

  object Utility {

    private val chainID = configuration.get[String]("blockchain.chainID")

    def onDeputize(maintainerDeputize: MaintainerDeputize)(implicit header: Header): Future[Unit] = {
      val maintainerID = utilities.IDGenerator.getMaintainerID(classificationID = maintainerDeputize.classificationID, identityID = maintainerDeputize.toID)
      val upsert = Service.insertOrUpdate(Maintainer(id = maintainerID, maintainedTraits = Mutables(maintainerDeputize.maintainedTraits), addMaintainer = maintainerDeputize.addMaintainer, removeMaintainer = maintainerDeputize.removeMaintainer, mutateMaintainer = maintainerDeputize.mutateMaintainer))

      val masterOperations = {
        val entityType = masterClassifications.Service.tryGetEntityType(id = maintainerDeputize.classificationID, maintainerID = maintainerDeputize.fromID)

        def insertClassification(entityType: String) = masterClassifications.Service.insertOrUpdate(id = maintainerDeputize.classificationID, entityType = entityType, maintainerID = maintainerDeputize.toID, status = Option(true))

        for {
          entityType <- entityType
          _ <- insertClassification(entityType)
        } yield ()
      }

      (for {
        _ <- upsert
        _ <- masterOperations
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.MAINTAINER_DEPUTIZE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def auxiliarySuper(classificationID: String, identityID: String, mutableTraits: Mutables): Future[Unit] = {
      val upsert = Service.insertOrUpdate(Maintainer(id = utilities.IDGenerator.getMaintainerID(classificationID = classificationID, identityID = identityID), maintainedTraits = mutableTraits, addMaintainer = true, removeMaintainer = true, mutateMaintainer = true))

      (for {
        _ <- upsert
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }
  }

}

object Maintainers {
  def props(blockchainMaintainers: models.blockchain.Maintainers) (implicit executionContext: ExecutionContext) = Props(new MaintainerActor(blockchainMaintainers))
  
  @Singleton
  class MaintainerActor @Inject()(
                                   blockchainMaintainers: models.blockchain.Maintainers
                                 ) (implicit executionContext: ExecutionContext) extends Actor with ActorLogging {
    private implicit val logger: Logger = Logger(this.getClass)

    override def receive: Receive = {
      case CreateMaintainer(_, maintainer) => {
        blockchainMaintainers.Service.create(maintainer) pipeTo sender()
      }
      case InsertMultipleMaintainer(_, maintainers) => {
        blockchainMaintainers.Service.insertMultiple(maintainers) pipeTo sender()
      }
      case InsertOrUpdateMaintainer(_, maintainer) => {
        blockchainMaintainers.Service.insertOrUpdate(maintainer) pipeTo sender()
      }
      case TryGetMaintainer(_, id) => {
        blockchainMaintainers.Service.tryGet(id) pipeTo sender()
      }
      case GetMaintainer(_, id) => {
        blockchainMaintainers.Service.get(id) pipeTo sender()
      }
      case GetAllMaintainer(_) => {
        blockchainMaintainers.Service.getAll pipeTo sender()
      }
      case DeleteMaintainer(_, id) => {
        blockchainMaintainers.Service.delete(id) pipeTo sender()
      }
    }
  }

  case class CreateMaintainer(uid: String, maintainer: Maintainer)
  case class TryGetMaintainer(uid: String, id: String)
  case class InsertMultipleMaintainer(uid: String, maintainer: Seq[Maintainer])
  case class InsertOrUpdateMaintainer(uid: String, maintainer: Maintainer)
  case class GetAllMaintainer(uid: String)
  case class GetMaintainer(uid: String, id: String)
  case class DeleteMaintainer(uid: String, id: String)
}