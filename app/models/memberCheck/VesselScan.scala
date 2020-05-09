package models.memberCheck

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Node
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class VesselScan(id: String, vesselName: String, scanID: Int, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged[VesselScan] {

  def createLog()(implicit node: Node): VesselScan = copy(createdBy = Option(node.id), createdOn = Option(new Timestamp(System.currentTimeMillis())), createdOnTimeZone = Option(node.timeZone))

  def updateLog()(implicit node: Node): VesselScan = copy(updatedBy = Option(node.id), updatedOn = Option(new Timestamp(System.currentTimeMillis())), updatedOnTimeZone = Option(node.timeZone))

}

@Singleton
class VesselScans @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.MEMBER_CHECK_MEMBER_SCAN

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private implicit val node: Node = Node(id = configuration.get[String]("node.id"), timeZone = configuration.get[String]("node.timeZone"))

  private[models] val vesselScanTable = TableQuery[VesselScanTable]

  private def add(vesselScan: VesselScan): Future[String] = db.run((vesselScanTable returning vesselScanTable.map(_.id) += vesselScan.createLog()).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(vesselScan: VesselScan): Future[Int] = db.run(vesselScanTable.insertOrUpdate(vesselScan.updateLog()).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[Option[VesselScan]] = db.run(vesselScanTable.filter(_.id === id).result.headOption)

  private def findByScanID(scanID: Int): Future[VesselScan] = db.run(vesselScanTable.filter(_.scanID === scanID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findScanIDByVesselName(vesselName: String): Future[Option[Int]] = db.run(vesselScanTable.filter(_.vesselName === vesselName).map(_.scanID).result.headOption)

  private def deleteById(id: String): Future[Int] = db.run(vesselScanTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class VesselScanTable(tag: Tag) extends Table[VesselScan](tag, "VesselScan") {

    def * = (id, vesselName, scanID, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (VesselScan.tupled, VesselScan.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def vesselName = column[String]("vesselName", O.Unique)

    def scanID = column[Int]("scanID", O.Unique)

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(id: String, vesselName: String, scanID: Int): Future[String] = add(VesselScan(id, vesselName, scanID))

    def get(id: String): Future[Option[VesselScan]] = findById(id)

    def tryGetByScanID(scanID: Int): Future[VesselScan] = findByScanID(scanID)

    def getScanID(vesselName: String): Future[Option[Int]] = findScanIDByVesselName(vesselName)
  }

}
