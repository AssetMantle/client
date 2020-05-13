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

case class CorporateScanDecision(id: String, scanID: Int, resultID: Option[Int], status: Boolean, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class CorporateScanDecisions @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.MEMBER_CHECK_MEMBER_SCAN_DECISION

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val corporateScanDecisionTable = TableQuery[CorporateScanDecisionTable]

  private def add(corporateScanDecision: CorporateScanDecision): Future[String] = db.run((corporateScanDecisionTable returning corporateScanDecisionTable.map(_.id) += corporateScanDecision).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(corporateScanDecision: CorporateScanDecision): Future[Int] = db.run(corporateScanDecisionTable.insertOrUpdate(corporateScanDecision).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[Option[CorporateScanDecision]] = db.run(corporateScanDecisionTable.filter(_.id === id).result.headOption)

  private def checkOrganizationAndStatus(id: String, status: Boolean): Future[Boolean] = db.run(corporateScanDecisionTable.filter(_.id === id).filter(_.status === status).exists.result)

  private def deleteById(id: String): Future[Int] = db.run(corporateScanDecisionTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class CorporateScanDecisionTable(tag: Tag) extends Table[CorporateScanDecision](tag, "CorporateScanDecision") {

    def * = (id, scanID, resultID.?, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (CorporateScanDecision.tupled, CorporateScanDecision.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def scanID = column[Int]("scanID")

    def resultID = column[Int]("resultID")

    def status = column[Boolean]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(organizationID: String, scanID: Int, resultID: Option[Int], status: Boolean): Future[String] = add(CorporateScanDecision(organizationID, scanID, resultID, status))

    def insertOrUpdate(organizationID: String, scanID: Int, resultID: Option[Int], status: Boolean): Future[Int] = upsert(CorporateScanDecision(organizationID, scanID, resultID, status))

    def checkOrganizationApproved(organizationID: String): Future[Boolean] = checkOrganizationAndStatus(organizationID, status = true)

    def get(organizationID: String): Future[Option[CorporateScanDecision]] = findById(organizationID)
  }

}