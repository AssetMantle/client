package models.master

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class TraderNew(id: String, zoneID: String, organizationID: String, accountID: String, name: String, completionStatus: Boolean = false, verificationStatus: Option[Boolean] = None)

@Singleton
class TraderNews @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRADER

  import databaseConfig.profile.api._

  private[models] val traderNewTable = TableQuery[TraderNewTable]

  private def add(traderNew: TraderNew): Future[String] = db.run((traderNewTable returning traderNewTable.map(_.id) += traderNew).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(traderNew: TraderNew): Future[Int] = db.run(traderNewTable.insertOrUpdate(traderNew).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[TraderNew] = db.run(traderNewTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByAccountId(accountID: String): Future[TraderNew] = db.run(traderNewTable.filter(_.accountID === accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findOrganizationIDByAccountId(accountID: String): Future[String] = db.run(traderNewTable.filter(_.accountID === accountID).map(_.organizationID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAccountIdById(id: String): Future[String] = db.run(traderNewTable.filter(_.id === id).map(_.accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getIDByAccountID(accountID: String): Future[Option[String]] = db.run(traderNewTable.filter(_.accountID === accountID).map(_.id.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        None
    }
  }

  private def getZoneIDOnAccountID(accountID: String): Future[String] = db.run(traderNewTable.filter(_.accountID === accountID).map(_.zoneID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getZoneIDByID(id: String): Future[String] = db.run(traderNewTable.filter(_.id === id).map(_.zoneID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getOrganizationIDByID(id: String): Future[String] = db.run(traderNewTable.filter(_.id === id).map(_.organizationID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getVerificationStatusById(id: String): Future[Option[Boolean]] = db.run(traderNewTable.filter(_.id === id).map(_.verificationStatus.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String) = db.run(traderNewTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTraderNewsByCompletionStatusVerificationStatusAndZoneID(zoneID: String, completionStatus: Boolean, verificationStatus: Option[Boolean]): Future[Seq[TraderNew]] = db.run(traderNewTable.filter(_.zoneID === zoneID).filter(_.completionStatus === completionStatus).filter(_.verificationStatus.? === verificationStatus).result)

  private def getTraderNewsByCompletedStatusVerificationStatusByOrganizationID(organizationID: String, completionStatus: Boolean, verificationStatus: Option[Boolean]): Future[Seq[TraderNew]] = db.run(traderNewTable.filter(_.organizationID === organizationID).filter(_.completionStatus === completionStatus).filter(_.verificationStatus.? === verificationStatus).result)

  private def getVerifiedTraderNewsByOrganizationID(organizationID: String): Future[Seq[TraderNew]] = db.run(traderNewTable.filter(_.organizationID === organizationID).filter(_.verificationStatus === true).result)

  private def getTraderNewsByOrganizationID(organizationID: String): Future[Seq[TraderNew]] = db.run(traderNewTable.filter(_.organizationID === organizationID).result)

  private def checkOrganizationIDTraderNewIDExists(traderNewID: String, organizationID: String): Future[Boolean] = db.run(traderNewTable.filter(_.id === traderNewID).filter(_.organizationID === organizationID).exists.result)

  private def updateVerificationStatusOnID(id: String, verificationStatus: Option[Boolean]): Future[Int] = db.run(traderNewTable.filter(_.id === id).map(_.verificationStatus.?).update(verificationStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateCompletionStatusOnID(id: String, completionStatus: Boolean) = db.run(traderNewTable.filter(_.id === id).map(_.completionStatus).update(completionStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateVerificationStatusOnAccountID(accountID: String, verificationStatus: Option[Boolean]) = db.run(traderNewTable.filter(_.accountID === accountID).map(_.verificationStatus.?).update(verificationStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class TraderNewTable(tag: Tag) extends Table[TraderNew](tag, "TraderNew") {

    def * = (id, zoneID, organizationID, accountID, name, completionStatus, verificationStatus.?) <> (TraderNew.tupled, TraderNew.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def zoneID = column[String]("zoneID")

    def organizationID = column[String]("organizationID")

    def accountID = column[String]("accountID")

    def name = column[String]("name")

    def completionStatus = column[Boolean]("completionStatus")

    def verificationStatus = column[Boolean]("verificationStatus")

  }

  object Service {

    def create(zoneID: String, organizationID: String, accountID: String, name: String): Future[String] = add(TraderNew(utilities.IDGenerator.hexadecimal, zoneID, organizationID, accountID, name))

    def insertOrUpdate(zoneID: String, organizationID: String, accountID: String, name: String): Future[String] = {

      val id = getIDByAccountID(accountID)

      def upsertTraderNew(id: String): Future[Int] = upsert(TraderNew(id = id, zoneID = zoneID, organizationID = organizationID, accountID = accountID, name = name))

      for {
        id <- id
        _ <- upsertTraderNew(id.getOrElse(utilities.IDGenerator.hexadecimal))
      } yield id.getOrElse(utilities.IDGenerator.hexadecimal)
    }

    def getID(accountID: String): Future[String] = getIDByAccountID(accountID).map { id => id.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)) }

    def get(id: String): Future[TraderNew] = findById(id)

    def getByAccountID(accountID: String): Future[TraderNew] = findByAccountId(accountID)

    def getZoneID(id: String): Future[String] = getZoneIDByID(id)

    def getOrganizationID(id: String): Future[String] = getOrganizationIDByID(id)

    def getZoneIDByAccountID(accountID: String): Future[String] = getZoneIDOnAccountID(accountID)

    def getOrganizationIDByAccountID(accountID: String): Future[String] = findOrganizationIDByAccountId(accountID)

    def rejectTraderNew(id: String): Future[Int] = updateVerificationStatusOnID(id = id, verificationStatus = Option(false))

    def verifyTraderNew(id: String): Future[Int] = updateVerificationStatusOnID(id = id, verificationStatus = Option(true))

    def rejectTraderNewByAccountID(accountID: String): Future[Int] = updateVerificationStatusOnAccountID(accountID = accountID, verificationStatus = Option(false))

    def verifyTraderNewByAccountID(accountID: String): Future[Int] = updateVerificationStatusOnAccountID(accountID = accountID, verificationStatus = Option(true))

    def getAccountId(id: String): Future[String] = getAccountIdById(id)

    def getVerifyTraderNewRequestsForZone(zoneID: String): Future[Seq[TraderNew]] = getTraderNewsByCompletionStatusVerificationStatusAndZoneID(zoneID = zoneID, completionStatus = true, verificationStatus = null)

    def getVerifyTraderNewRequestsForOrganization(organizationID: String): Future[Seq[TraderNew]] = getTraderNewsByCompletedStatusVerificationStatusByOrganizationID(organizationID = organizationID, completionStatus = true, verificationStatus = null)

    def getVerifiedTraderNewsForOrganization(organizationID: String): Future[Seq[TraderNew]] = getVerifiedTraderNewsByOrganizationID(organizationID)

    def getVerificationStatus(id: String): Future[Boolean] = getVerificationStatusById(id).map(_.getOrElse(false))

    def markTraderNewFormCompleted(id: String): Future[Int] = updateCompletionStatusOnID(id = id, completionStatus = true)

    def getTraderNewsListInOrganization(organizationID: String): Future[Seq[TraderNew]] = getTraderNewsByOrganizationID(organizationID)

    def verifyOrganizationTraderNew(traderNewID: String, organizationID: String): Future[Boolean] = checkOrganizationIDTraderNewIDExists(traderNewID = traderNewID, organizationID = organizationID)
  }

}