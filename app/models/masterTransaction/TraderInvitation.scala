package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import java.sql.Date

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class TraderInvitation(id: String, organizationID: String, inviteeEmailAddress: String, status: String)

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
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByID(id: String): Future[TraderInvitation] = db.run(traderInvitationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusByOrganizationIdAndInviteeEmail(organizationID: String, inviteeEmailAddress: String, status: String): Future[Int] = db.run(traderInvitationTable.filter(_.organizationID === organizationID).filter(_.inviteeEmailAddress === inviteeEmailAddress).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        0
    }
  }

  private[models] class TraderInvitationTable(tag: Tag) extends Table[TraderInvitation](tag, "TraderInvitation") {

    def * = (id, organizationID, inviteeEmailAddress, status) <> (TraderInvitation.tupled, TraderInvitation.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def organizationID = column[String]("organizationID")

    def inviteeEmailAddress = column[String]("inviteeEmailAddress")

    def status = column[String]("status")

  }

  object Service {

    def create(organizationID: String, inviteeEmailAddress: String): Future[String] = add(TraderInvitation(id = utilities.IDGenerator.requestID(), organizationID = organizationID, inviteeEmailAddress = inviteeEmailAddress, status = constants.Status.TraderInvitation.NO_CONTACT))

    def get(id: String): Future[TraderInvitation] = findByID(id)

    def updateStatusByEmailAddress(organizationID: String, emailAddress: String, status: String): Future[Int] = updateStatusByOrganizationIdAndInviteeEmail(organizationID = organizationID, inviteeEmailAddress = emailAddress, status = status)
  }

}
