package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ZoneInvitation(id: String, emailAddress: String, status: Option[Boolean])

@Singleton
class ZoneInvitations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_TRADER_INVITATION

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val zoneInvitationTable = TableQuery[ZoneInvitationTable]

  private def add(zoneInvitation: ZoneInvitation): Future[String] = db.run((zoneInvitationTable returning zoneInvitationTable.map(_.id) += zoneInvitation).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByID(id: String): Future[ZoneInvitation] = db.run(zoneInvitationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusByID(id: String, status: Option[Boolean]): Future[Int] = db.run(zoneInvitationTable.filter(_.id === id).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.ZONE_INVITATION_DOES_NOT_EXISTS.message, noSuchElementException)
        throw new BaseException(constants.Response.ZONE_INVITATION_DOES_NOT_EXISTS)
    }
  }

  private[models] class ZoneInvitationTable(tag: Tag) extends Table[ZoneInvitation](tag, "ZoneInvitation") {

    def * = (id, emailAddress, status.?) <> (ZoneInvitation.tupled, ZoneInvitation.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def emailAddress = column[String]("emailAddress")

    def status = column[Boolean]("status")

  }

  object Service {

    def create(emailAddress: String): Future[String] = add(ZoneInvitation(id = utilities.IDGenerator.requestID, emailAddress = emailAddress, status = null))

    def tryGet(id: String): Future[ZoneInvitation] = findByID(id)

    def markInviationAccepted(id: String): Future[Int] = updateStatusByID(id = id, status = Option(true))

  }

}
