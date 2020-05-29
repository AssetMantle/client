package models.memberCheck

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class CorporateScan(id: String, companyName: String, scanID: Int, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class CorporateScans @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.MEMBER_CHECK_MEMBER_SCAN

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val corporateScanTable = TableQuery[CorporateScanTable]

  private def add(corporateScan: CorporateScan): Future[String] = db.run((corporateScanTable returning corporateScanTable.map(_.id) += corporateScan).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(corporateScan: CorporateScan): Future[Int] = db.run(corporateScanTable.insertOrUpdate(corporateScan).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findById(id: String): Future[Option[CorporateScan]] = db.run(corporateScanTable.filter(_.id === id).result.headOption)

  private def findByScanID(scanID: Int): Future[CorporateScan] = db.run(corporateScanTable.filter(_.scanID === scanID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findScanIDByCompanyName(companyName: String): Future[Option[Int]] = db.run(corporateScanTable.filter(_.companyName === companyName).map(_.scanID).result.headOption)

  private def deleteById(id: String): Future[Int] = db.run(corporateScanTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class CorporateScanTable(tag: Tag) extends Table[CorporateScan](tag, "CorporateScan") {

    def * = (id, companyName, scanID, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (CorporateScan.tupled, CorporateScan.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def companyName = column[String]("companyName", O.Unique)

    def scanID = column[Int]("scanID", O.Unique)

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(id: String, companyName: String, scanID: Int): Future[String] = add(CorporateScan(id, companyName, scanID))

    def get(id: String): Future[Option[CorporateScan]] = findById(id)

    def tryGetByScanID(scanID: Int): Future[CorporateScan] = findByScanID(scanID)

    def getScanID(companyName: String): Future[Option[Int]] = findScanIDByCompanyName(companyName)
  }

}
