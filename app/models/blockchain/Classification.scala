package models.blockchain

import akka.pattern.{ask, pipe}
import akka.util.Timeout
import models.blockchain.Classifications.{CheckExistsClassification, CreateClassification, DeleteClassification, GetAllClassification, GetClassification, InsertMultipleClassification, InsertOrUpdateClassification, TryGetClassification}
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.{ShardRegion}
import constants.Actor.{NUMBER_OF_ENTITIES, NUMBER_OF_SHARDS}
import exceptions.BaseException
import models.Abstract.ShardedActorRegion
import models.Trait.Logged
import models.common.Serializable._
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Classification(id: String, immutableTraits: Immutables, mutableTraits: Mutables, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {
  def getChainID: String = id.split(constants.RegularExpression.BLOCKCHAIN_ID_SEPARATOR)(0)

  def getHashID: String = id.split(constants.RegularExpression.BLOCKCHAIN_ID_SEPARATOR)(1)
}

@Singleton
class Classifications @Inject()(
                                 protected val databaseConfigProvider: DatabaseConfigProvider,
                                 configuration: Configuration,
                               )(implicit executionContext: ExecutionContext) extends ShardedActorRegion {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_CLASSIFICATION

  private val uniqueId: String = UUID.randomUUID().toString

  import databaseConfig.profile.api._

  private implicit val timeout = Timeout(constants.Actor.ACTOR_ASK_TIMEOUT)

  override def idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateClassification(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetClassification(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetClassification(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllClassification(uid) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertMultipleClassification(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateClassification(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@DeleteClassification(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@CheckExistsClassification(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

 override def shardResolver: ShardRegion.ExtractShardId = {
    case CreateClassification(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetClassification(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetClassification(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllClassification(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertMultipleClassification(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateClassification(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case DeleteClassification(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case CheckExistsClassification(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }

  override def regionName: String = "classificationRegion"

  override def props: Props = Classifications.props(Classifications.this)
  
  case class ClassificationSerialized(id: String, immutableTraits: String, mutableTraits: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Classification = Classification(id = id, immutableTraits = utilities.JSON.convertJsonStringToObject[Immutables](immutableTraits), mutableTraits = utilities.JSON.convertJsonStringToObject[Mutables](mutableTraits), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(classification: Classification): ClassificationSerialized = ClassificationSerialized(id = classification.id, immutableTraits = Json.toJson(classification.immutableTraits).toString, mutableTraits = Json.toJson(classification.mutableTraits).toString, createdBy = classification.createdBy, createdOn = classification.createdOn, createdOnTimeZone = classification.createdOnTimeZone, updatedBy = classification.updatedBy, updatedOn = classification.updatedOn, updatedOnTimeZone = classification.updatedOnTimeZone)

  private[models] val classificationTable = TableQuery[ClassificationTable]

  private def add(classification: Classification): Future[String] = db.run((classificationTable returning classificationTable.map(_.id) += serialize(classification)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ORDER_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(classifications: Seq[Classification]): Future[Seq[String]] = db.run((classificationTable returning classificationTable.map(_.id) ++= classifications.map(x => serialize(x))).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ORDER_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(classification: Classification): Future[Int] = db.run(classificationTable.insertOrUpdate(serialize(classification)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ORDER_UPSERT_FAILED, psqlException)
    }
  }

  private def tryGetByID(id: String) = db.run(classificationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ORDER_NOT_FOUND, noSuchElementException)
    }
  }

  private def getByID(id: String) = db.run(classificationTable.filter(_.id === id).result.headOption)

  private def checkExistsByID(id: String) = db.run(classificationTable.filter(_.id === id).exists.result)

  private def getAllClassifications = db.run(classificationTable.result)

  private def deleteByID(id: String): Future[Int] = db.run(classificationTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ORDER_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ORDER_DELETE_FAILED, noSuchElementException)
    }
  }

  private[models] class ClassificationTable(tag: Tag) extends Table[ClassificationSerialized](tag, "Classification_BC") {

    def * = (id, immutableTraits, mutableTraits, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (ClassificationSerialized.tupled, ClassificationSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def immutableTraits = column[String]("immutableTraits")

    def mutableTraits = column[String]("mutableTraits")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def createClassificationWithActor(classification: Classification): Future[String] = (actorRegion ? CreateClassification(uniqueId, classification)).mapTo[String]

    def create(classification: Classification): Future[String] = add(classification)

    def tryGetClassificationWithActor(id: String): Future[Classification] = (actorRegion ? TryGetClassification(uniqueId, id)).mapTo[Classification]

    def tryGet(id: String): Future[Classification] = tryGetByID(id).map(_.deserialize)

    def getClassificationWithActor(id: String): Future[Option[Classification]] = (actorRegion ? GetClassification(uniqueId, id)).mapTo[Option[Classification]]

    def get(id: String): Future[Option[Classification]] = getByID(id).map(_.map(_.deserialize))

    def getAllClassificationWithActor: Future[Seq[Classification]] = (actorRegion ? GetAllClassification(uniqueId)).mapTo[Seq[Classification]]

    def getAll: Future[Seq[Classification]] = getAllClassifications.map(_.map(_.deserialize))

    def insertMultipleClassificationWithActor(classifications: Seq[Classification]): Future[Seq[String]] = (actorRegion ? InsertMultipleClassification(uniqueId, classifications)).mapTo[Seq[String]]

    def insertMultiple(classifications: Seq[Classification]): Future[Seq[String]] = addMultiple(classifications)

    def insertOrUpdateClassificationWithActor(classification: Classification): Future[Int] = (actorRegion ? InsertOrUpdateClassification(uniqueId, classification)).mapTo[Int]

    def insertOrUpdate(classification: Classification): Future[Int] = upsert(classification)

    def deleteClassificationWithActor(id: String): Future[Int] = (actorRegion ? DeleteClassification(uniqueId, id)).mapTo[Int]

    def delete(id: String): Future[Int] = deleteByID(id)

    def checkExistsClassificationWithActor(id: String): Future[Boolean] = (actorRegion ? CheckExistsClassification(uniqueId, id)).mapTo[Boolean]

    def checkExists(id: String): Future[Boolean] = checkExistsByID(id)
  }

  object Utility {

    private val chainID = configuration.get[String]("blockchain.chainID")

    //Insert in master.Classification happens in respective txs
    def auxiliaryDefine(immutables: Immutables, mutables: Mutables): Future[String] = {
      val classificationID = utilities.IDGenerator.getClassificationID(chainID = chainID, immutables = immutables, mutables = mutables)
      val upsert = Service.insertOrUpdate(Classification(id = classificationID, immutableTraits = immutables, mutableTraits = mutables))

      (for {
        _ <- upsert
      } yield classificationID
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }
  }

}

object Classifications {
  def props(blockchainClassifications: models.blockchain.Classifications) (implicit executionContext: ExecutionContext) = Props(new ClassificationActor(blockchainClassifications))

  @Singleton
  class ClassificationActor @Inject()(
                                       blockchainClassifications: models.blockchain.Classifications
                                     ) (implicit executionContext: ExecutionContext) extends Actor with ActorLogging {
    private implicit val logger: Logger = Logger(this.getClass)

    override def receive: Receive = {
      case CreateClassification(_, classification) => {
        blockchainClassifications.Service.create(classification) pipeTo sender()
      }
      case TryGetClassification(_, id) => {
        blockchainClassifications.Service.tryGet(id) pipeTo sender()
      }
      case GetClassification(_, id) => {
        blockchainClassifications.Service.get(id) pipeTo sender()
      }
      case GetAllClassification(_) => {
        blockchainClassifications.Service.getAll pipeTo sender()
      }
      case InsertMultipleClassification(_, classifications) => {
        blockchainClassifications.Service.insertMultiple(classifications) pipeTo sender()
      }
      case InsertOrUpdateClassification(_, classification) => {
        blockchainClassifications.Service.insertOrUpdate(classification) pipeTo sender()
      }
      case DeleteClassification(_, id) => {
        blockchainClassifications.Service.delete(id) pipeTo sender()
      }
      case CheckExistsClassification(_, id) => {
        blockchainClassifications.Service.checkExists(id) pipeTo sender()
      }
    }
  }

  case class CreateClassification(uid: String, classification: Classification)
  case class TryGetClassification(uid: String, id: String)
  case class GetClassification(uid: String, id: String)
  case class GetAllClassification(uid: String)
  case class InsertMultipleClassification(uid: String, classifications: Seq[Classification])
  case class InsertOrUpdateClassification(uid: String, classification: Classification)
  case class DeleteClassification(uid: String, id: String)
  case class CheckExistsClassification(uid: String, id: String)
}