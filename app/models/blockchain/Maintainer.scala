package models.blockchain

import exceptions.BaseException
import models.Trait.Logged
import models.common.ID.{ClassificationID, IdentityID, MaintainerID}
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
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Maintainer(id: MaintainerID, maintainedTraits: Mutables, addMaintainer: Boolean, removeMaintainer: Boolean, mutateMaintainer: Boolean, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Maintainers @Inject()(
                             protected val databaseConfigProvider: DatabaseConfigProvider,
                             configuration: Configuration,
                             masterClassifications: master.Classifications
                           )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_MAINTAINER

  import databaseConfig.profile.api._

  case class MaintainerSerialized(classificationID: String, identityID: String, maintainedTraits: String, addMaintainer: Boolean, removeMaintainer: Boolean, mutateMaintainer: Boolean, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Maintainer = Maintainer(id = MaintainerID(classificationID = classificationID, identityID = identityID), maintainedTraits = utilities.JSON.convertJsonStringToObject[Mutables](maintainedTraits), addMaintainer = addMaintainer, removeMaintainer = removeMaintainer, mutateMaintainer = mutateMaintainer, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(maintainer: Maintainer): MaintainerSerialized = MaintainerSerialized(classificationID = maintainer.id.classificationID.asString, identityID = maintainer.id.identityID.asString, maintainedTraits = Json.toJson(maintainer.maintainedTraits).toString, addMaintainer = maintainer.addMaintainer, removeMaintainer = maintainer.removeMaintainer, mutateMaintainer = maintainer.mutateMaintainer, createdBy = maintainer.createdBy, createdOn = maintainer.createdOn, createdOnTimeZone = maintainer.createdOnTimeZone, updatedBy = maintainer.updatedBy, updatedOn = maintainer.updatedOn, updatedOnTimeZone = maintainer.updatedOnTimeZone)

  private[models] val maintainerTable = TableQuery[MaintainerTable]

  private def add(maintainer: Maintainer): Future[String] = db.run((maintainerTable returning maintainerTable.map(_.identityID) += serialize(maintainer)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ORDER_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(maintainers: Seq[Maintainer]): Future[Seq[String]] = db.run((maintainerTable returning maintainerTable.map(_.identityID) ++= maintainers.map(x => serialize(x))).asTry).map {
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

  private def tryGetByID(classificationID: String, identityID: String) = db.run(maintainerTable.filter(x => x.classificationID === classificationID && x.identityID === identityID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ORDER_NOT_FOUND, noSuchElementException)
    }
  }

  private def getByID(classificationID: String, identityID: String) = db.run(maintainerTable.filter(x => x.classificationID === classificationID && x.identityID === identityID).result.headOption)

  private def getAllByClassificationID(classificationID: String) = db.run(maintainerTable.filter(_.classificationID === classificationID).result)

  private def getAllMaintainers = db.run(maintainerTable.result)

  private def deleteByID(classificationID: String, identityID: String): Future[Int] = db.run(maintainerTable.filter(x => x.classificationID === classificationID && x.identityID === identityID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ORDER_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ORDER_DELETE_FAILED, noSuchElementException)
    }
  }

  private[models] class MaintainerTable(tag: Tag) extends Table[MaintainerSerialized](tag, "Maintainer_BC") {

    def * = (classificationID, identityID, maintainedTraits, addMaintainer, removeMaintainer, mutateMaintainer, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (MaintainerSerialized.tupled, MaintainerSerialized.unapply)

    def classificationID = column[String]("classificationID", O.PrimaryKey)

    def identityID = column[String]("identityID", O.PrimaryKey)

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

    def create(maintainer: Maintainer): Future[String] = add(maintainer)

    def tryGet(id: MaintainerID): Future[Maintainer] = tryGetByID(classificationID = id.classificationID.asString, identityID = id.identityID.asString).map(_.deserialize)

    def get(id: MaintainerID): Future[Option[Maintainer]] = getByID(classificationID = id.classificationID.asString, identityID = id.identityID.asString).map(_.map(_.deserialize))

    def getAll: Future[Seq[Maintainer]] = getAllMaintainers.map(_.map(_.deserialize))

    def insertMultiple(maintainers: Seq[Maintainer]): Future[Seq[String]] = addMultiple(maintainers)

    def insertOrUpdate(maintainer: Maintainer): Future[Int] = upsert(maintainer)

    def getByClassificationID(id: ClassificationID): Future[Seq[Maintainer]] = getAllByClassificationID(classificationID = id.asString).map(_.map(_.deserialize))

    def delete(id: MaintainerID): Future[Int] = deleteByID(classificationID = id.classificationID.asString, identityID = id.identityID.asString)
  }

  object Utility {

    def onDeputize(maintainerDeputize: MaintainerDeputize)(implicit header: Header): Future[Unit] = {
      val maintainerID = MaintainerID(classificationID = maintainerDeputize.classificationID, identityID = maintainerDeputize.toID)
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

    def auxiliarySuper(classificationID: ClassificationID, identityID: IdentityID, mutableTraits: Mutables): Future[Unit] = {
      val upsert = Service.insertOrUpdate(Maintainer(id = MaintainerID(classificationID = classificationID, identityID = identityID), maintainedTraits = mutableTraits, addMaintainer = true, removeMaintainer = true, mutateMaintainer = true))

      (for {
        _ <- upsert
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }
  }

}