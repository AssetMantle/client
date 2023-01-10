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

case class MemberScan(id: String, firstName: String, lastName: String, scanID: Int, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class MemberScans @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.MEMBER_CHECK_MEMBER_SCAN

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val memberScanTable = TableQuery[MemberScanTable]

  private def add(memberScan: MemberScan): Future[String] = db.run((memberScanTable returning memberScanTable.map(_.id) += memberScan).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(memberScan: MemberScan): Future[Int] = db.run(memberScanTable.insertOrUpdate(memberScan).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findById(id: String): Future[Option[MemberScan]] = db.run(memberScanTable.filter(_.id === id).result.headOption)

  private def findByScanID(scanID: Int): Future[MemberScan] = db.run(memberScanTable.filter(_.scanID === scanID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findScanIDByFirstNameAndLastName(firstName: String, lastName: String): Future[Option[Int]] = db.run(memberScanTable.filter(x => x.firstName === firstName && x.lastName === lastName).map(_.scanID).result.headOption)

  private def deleteById(id: String): Future[Int] = db.run(memberScanTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class MemberScanTable(tag: Tag) extends Table[MemberScan](tag, "MemberScan") {

    def * = (id, firstName, lastName, scanID, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (MemberScan.tupled, MemberScan.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def firstName = column[String]("firstName")

    def lastName = column[String]("lastName")

    def scanID = column[Int]("scanID", O.Unique)

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(id: String, firstName: String, lastName: String, scanID: Int): Future[String] = add(MemberScan(id, firstName, lastName, scanID))

    def get(id: String): Future[Option[MemberScan]] = findById(id)

    def tryGetByScanID(scanID: Int): Future[MemberScan] = findByScanID(scanID)

    def getScanID(firstName: String, lastName: String): Future[Option[Int]] = findScanIDByFirstNameAndLastName(firstName, lastName)
  }

}
