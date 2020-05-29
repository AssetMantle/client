package models.masterTransaction

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class TraderInvitation(id: String, organizationID: String, inviteeEmailAddress: String, status: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class TraderInvitations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_TRADER_INVITATION

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val traderInvitationTable = TableQuery[TraderInvitationTable]

  private def add(traderInvitation: TraderInvitation): Future[String] = db.run((traderInvitationTable returning traderInvitationTable.map(_.id) += traderInvitation).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByID(id: String): Future[TraderInvitation] = db.run(traderInvitationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusByOrganizationIdAndInviteeEmail(organizationID: String, inviteeEmailAddress: String, status: String): Future[Int] = db.run(traderInvitationTable.filter(_.organizationID === organizationID).filter(_.inviteeEmailAddress === inviteeEmailAddress).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        0
    }
  }

  private[models] class TraderInvitationTable(tag: Tag) extends Table[TraderInvitation](tag, "TraderInvitation") {

    def * = (id, organizationID, inviteeEmailAddress, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (TraderInvitation.tupled, TraderInvitation.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def organizationID = column[String]("organizationID")

    def inviteeEmailAddress = column[String]("inviteeEmailAddress")

    def status = column[String]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(organizationID: String, inviteeEmailAddress: String): Future[String] = add(TraderInvitation(id = utilities.IDGenerator.requestID(), organizationID = organizationID, inviteeEmailAddress = inviteeEmailAddress, status = constants.Status.TraderInvitation.NO_CONTACT))

    def get(id: String): Future[TraderInvitation] = findByID(id)

    def updateStatusByEmailAddress(organizationID: String, emailAddress: String, status: String): Future[Int] = updateStatusByOrganizationIdAndInviteeEmail(organizationID = organizationID, inviteeEmailAddress = emailAddress, status = status)
  }

}
