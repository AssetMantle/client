package models.blockchain

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Serializable._
import models.common.TransactionMessages.MaintainerDeputize
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Maintainer(id: String, maintainedTraits: Properties, addMaintainer: Boolean, removeMaintainer: Boolean, mutateMaintainer: Boolean, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {
  def getClassificationID: String = id.split(constants.RegularExpression.BLOCKCHAIN_ID_SEPARATOR)(0)

  def getFromID: String = id.split(constants.RegularExpression.BLOCKCHAIN_ID_SEPARATOR)(1)
}

@Singleton
class Maintainers @Inject()(
                             protected val databaseConfigProvider: DatabaseConfigProvider,
                             configuration: Configuration,
                           )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_MAINTAINER

  import databaseConfig.profile.api._

  case class MaintainerSerialized(id: String, maintainedTraits: String, addMaintainer: Boolean, removeMaintainer: Boolean, mutateMaintainer: Boolean, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Maintainer = Maintainer(id = id, maintainedTraits = utilities.JSON.convertJsonStringToObject[Properties](maintainedTraits), addMaintainer = addMaintainer, removeMaintainer = removeMaintainer, mutateMaintainer = mutateMaintainer, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
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

    def create(maintainer: Maintainer): Future[String] = add(maintainer)

    def tryGet(id: String): Future[Maintainer] = tryGetByID(id).map(_.deserialize)

    def get(id: String): Future[Option[Maintainer]] = getByID(id).map(_.map(_.deserialize))

    def getAll: Future[Seq[Maintainer]] = getAllMaintainers.map(_.map(_.deserialize))

    def insertMultiple(maintainers: Seq[Maintainer]): Future[Seq[String]] = addMultiple(maintainers)

    def insertOrUpdate(maintainer: Maintainer): Future[Int] = upsert(maintainer)

    def delete(id: String): Future[Int] = deleteByID(id)
  }

  object Utility {

    private val chainID = configuration.get[String]("blockchain.main.chainID")

    def onDeputize(maintainerDeputize: MaintainerDeputize): Future[Unit] = {
      val upsert = Service.insertOrUpdate(Maintainer(id = getID(classificationID = maintainerDeputize.classificationID, fromID = maintainerDeputize.fromID), maintainedTraits = maintainerDeputize.maintainedTraits, addMaintainer = maintainerDeputize.addMaintainer, removeMaintainer = maintainerDeputize.removeMaintainer, mutateMaintainer = maintainerDeputize.mutateMaintainer))

      (for {
        _ <- upsert
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    private def getID(classificationID: String, fromID: String) = Seq(classificationID, fromID).mkString(constants.Blockchain.IDSeparator)

  }

}