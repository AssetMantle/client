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

case class ZoneInvitation(id: String, emailAddress: String, accountID: Option[String] = None, status: Option[Boolean] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class ZoneInvitations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_ZONE_INVITATION

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val zoneInvitationTable = TableQuery[ZoneInvitationTable]

  private def add(zoneInvitation: ZoneInvitation): Future[String] = db.run((zoneInvitationTable returning zoneInvitationTable.map(_.id) += zoneInvitation).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByID(id: String): Future[ZoneInvitation] = db.run(zoneInvitationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getStatusByAccountID(accountID: String): Future[Option[Boolean]] = db.run(zoneInvitationTable.filter(_.accountID === accountID).map(_.status.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusAndAccountIDByID(id: String, accountID: Option[String], status: Option[Boolean]): Future[Int] = db.run(zoneInvitationTable.filter(_.id === id).map(x => (x.accountID.?, x.status.?)).update((accountID, status)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.ZONE_INVITATION_NOT_FOUND)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private[models] class ZoneInvitationTable(tag: Tag) extends Table[ZoneInvitation](tag, "ZoneInvitation") {

    def * = (id, emailAddress, accountID.?, status.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (ZoneInvitation.tupled, ZoneInvitation.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def emailAddress = column[String]("emailAddress")

    def accountID = column[String]("accountID")

    def status = column[Boolean]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(emailAddress: String): Future[String] = add(ZoneInvitation(id = utilities.IDGenerator.requestID(), emailAddress = emailAddress))

    def tryGet(id: String): Future[ZoneInvitation] = findByID(id)

    def getStatus(accountID: String): Future[Option[Boolean]] = getStatusByAccountID(accountID)

    def markInvitationAccepted(id: String, accountID: String): Future[Int] = updateStatusAndAccountIDByID(id = id, accountID = Option(accountID), status = Option(true))

  }

}
