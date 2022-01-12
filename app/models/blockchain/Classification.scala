package models.blockchain

import exceptions.BaseException
import models.Trait.Logged
import models.common.Serializable._
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
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
                               )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_CLASSIFICATION

  import databaseConfig.profile.api._

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

    def create(classification: Classification): Future[String] = add(classification)

    def tryGet(id: String): Future[Classification] = tryGetByID(id).map(_.deserialize)

    def get(id: String): Future[Option[Classification]] = getByID(id).map(_.map(_.deserialize))

    def getAll: Future[Seq[Classification]] = getAllClassifications.map(_.map(_.deserialize))

    def insertMultiple(classifications: Seq[Classification]): Future[Seq[String]] = addMultiple(classifications)

    def insertOrUpdate(classification: Classification): Future[Int] = upsert(classification)

    def delete(id: String): Future[Int] = deleteByID(id)

    def checkExists(id: String): Future[Boolean] = checkExistsByID(id)
  }

  object Utility {

    //Insert in master.Classification happens in respective txs
    def auxiliaryDefine(immutables: Immutables, mutables: Mutables): Future[String] = {
      val classificationID = utilities.IDGenerator.getClassificationID(chainID = constants.Blockchain.ChainID, immutables = immutables, mutables = mutables)
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