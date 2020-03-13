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

case class TraderInvitation(invitationID: String, organizationID: String, inviteeEmail: String, status: String)

@Singleton
class TraderInvitations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_TRADER_INVITATION

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val traderInvitationTable = TableQuery[TraderInvitationTable]

  private def add(traderInvitation: TraderInvitation): Future[String] = db.run((traderInvitationTable returning traderInvitationTable.map(_.invitationID) += traderInvitation).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByInvitationID(invitationID: String): Future[TraderInvitation] = db.run(traderInvitationTable.filter(_.invitationID === invitationID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByInviter(organizationID: String): Future[Seq[TraderInvitation]] = db.run(traderInvitationTable.filter(_.organizationID === organizationID).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByInviteeEmail(inviteeEmail: String): Future[TraderInvitation] = db.run(traderInvitationTable.filter(_.inviteeEmail === inviteeEmail).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusByInviteeEmail(inviteeEmail: String, status: String): Future[Int] = db.run(traderInvitationTable.filter(_.inviteeEmail === inviteeEmail).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        0
    }
  }

  private[models] class TraderInvitationTable(tag: Tag) extends Table[TraderInvitation](tag, "TraderInvitation") {

    def * = (invitationID, organizationID, inviteeEmail, status) <> (TraderInvitation.tupled, TraderInvitation.unapply)

    def invitationID = column[String]("invitationID", O.PrimaryKey)
    
    def organizationID = column[String]("organizationID")

    def inviteeEmail = column[String]("inviteeEmail")

    def status = column[String]("status")

  }

  object Service {

    def create(organizationID: String, inviteeEmail: String): Future[String] = add(TraderInvitation(invitationID = utilities.IDGenerator.requestID, organizationID = organizationID, inviteeEmail = inviteeEmail, status = constants.Status.TraderInvitation.NO_CONTACT))

    def get(invitationID: String): Future[TraderInvitation] = findByInvitationID(invitationID)

    def getByInviter(invitationID: String): Future[Seq[TraderInvitation]] = findByInviter(invitationID)

    def getByInvitee(inviteeEmail: String): Future[TraderInvitation] = findByInviteeEmail(inviteeEmail)

    def updateStatusByEmail(inviteeEmail: String, status: String): Future[Int] = updateStatusByInviteeEmail(inviteeEmail = inviteeEmail, status = status)
  }

}
