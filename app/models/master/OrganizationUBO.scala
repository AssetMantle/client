package models.master

import java.sql.Timestamp
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class OrganizationUBO(id: String, organizationID: String, firstName: String, lastName: String, sharePercentage: Double, relationship: String, title: String, status: Option[Boolean], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class OrganizationUBOs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, configuration: Configuration) {
  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db
  private[models] val organizationUBOTable = TableQuery[OrganizationUBOTable]


  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ORGANIZATION_UBO

  import databaseConfig.profile.api._

  private def add(organizationUBO: OrganizationUBO): Future[String] = db.run((organizationUBOTable returning organizationUBOTable.map(_.id) += organizationUBO).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(organizationUBO: OrganizationUBO): Future[Int] = db.run(organizationUBOTable.insertOrUpdate(organizationUBO).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findById(id: String): Future[OrganizationUBO] = db.run(organizationUBOTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findByOrganizationID(organizationID: String): Future[Seq[OrganizationUBO]] = db.run(organizationUBOTable.filter(_.organizationID === organizationID).result)

  private def checkOrganizationIDAndStatus(organizationID: String, status: Boolean): Future[Boolean] = db.run(organizationUBOTable.filter(x => x.organizationID === organizationID && x.status === status).exists.result)

  private def updateStatus(id: String, status: Boolean): Future[Int] = db.run(organizationUBOTable.filter(_.id === id).map(_.status).update(status).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }


  private def deleteById(id: String, organizationID: String): Future[Int] = db.run(organizationUBOTable.filter(x => x.id === id && x.organizationID === organizationID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }


  private[models] class OrganizationUBOTable(tag: Tag) extends Table[OrganizationUBO](tag, "OrganizationUBO") {

    def * = (id, organizationID, firstName, lastName, sharePercentage, relationship, title, status.? ,createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (OrganizationUBO.tupled, OrganizationUBO.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def organizationID = column[String]("organizationID")

    def firstName = column[String]("firstName")

    def lastName = column[String]("lastName")

    def sharePercentage = column[Double]("sharePercentage")

    def relationship = column[String]("relationship")

    def title = column[String]("title")

    def status = column[Boolean]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {
    def create(organizationID: String, firstName: String, lastName: String, sharePercentage: Double, relationship: String, title: String): Future[String] = add(OrganizationUBO(utilities.IDGenerator.requestID(), organizationID, firstName, lastName,sharePercentage, relationship, title, status = None))

    def tryGet(id: String): Future[OrganizationUBO] = findById(id)

    def getUBOs(organizationID: String): Future[Seq[OrganizationUBO]] = findByOrganizationID(organizationID)

    def checkScanStatus(organizationID: String) : Future[Boolean] = checkOrganizationIDAndStatus(organizationID, status = true)

    def markVerified(id: String, status: Boolean): Future[Int] = updateStatus(id, status)

    def delete(id:String, organizationID: String): Future [Int] = deleteById(id, organizationID)
  }

}