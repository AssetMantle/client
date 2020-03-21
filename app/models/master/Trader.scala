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

case class Trader(id: String, zoneID: String, organizationID: String, accountID: String, name: String, completionStatus: Boolean = false, verificationStatus: Option[Boolean] = None)

@Singleton
class Traders @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRADER

  import databaseConfig.profile.api._

  private[models] val traderTable = TableQuery[TraderTable]

  private def add(trader: Trader): Future[String] = db.run((traderTable returning traderTable.map(_.id) += trader).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(trader: Trader): Future[Int] = db.run(traderTable.insertOrUpdate(trader).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[Trader] = db.run(traderTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByAccountId(accountID: String): Future[Trader] = db.run(traderTable.filter(_.accountID === accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findOrganizationIDByAccountId(accountID: String): Future[String] = db.run(traderTable.filter(_.accountID === accountID).map(_.organizationID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAccountIdById(id: String): Future[String] = db.run(traderTable.filter(_.id === id).map(_.accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getIDByAccountID(accountID: String): Future[Option[String]] = db.run(traderTable.filter(_.accountID === accountID).map(_.id.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        None
    }
  }

  private def getZoneIDOnAccountID(accountID: String): Future[String] = db.run(traderTable.filter(_.accountID === accountID).map(_.zoneID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getZoneIDByID(id: String): Future[String] = db.run(traderTable.filter(_.id === id).map(_.zoneID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getOrganizationIDByID(id: String): Future[String] = db.run(traderTable.filter(_.id === id).map(_.organizationID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTraderNameByID(id: String): Future[String] = db.run(traderTable.filter(_.id === id).map(_.name).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTraderOrNoneByAccountID(accountID: String): Future[Option[Trader]] = db.run(traderTable.filter(_.accountID === accountID).result.headOption)

  private def getVerificationStatusById(id: String): Future[Option[Boolean]] = db.run(traderTable.filter(_.id === id).map(_.verificationStatus.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findVerificationStatusByAccountID(accountID: String): Future[Option[Boolean]] = db.run(traderTable.filter(_.accountID === accountID).map(_.verificationStatus.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String) = db.run(traderTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTradersByCompletionStatusVerificationStatusAndZoneID(zoneID: String, completionStatus: Boolean, verificationStatus: Option[Boolean]): Future[Seq[Trader]] = db.run(traderTable.filter(_.zoneID === zoneID).filter(_.completionStatus === completionStatus).filter(_.verificationStatus.? === verificationStatus).result)

  private def getTradersByCompletedStatusVerificationStatusByOrganizationID(organizationID: String, completionStatus: Boolean, verificationStatus: Option[Boolean]): Future[Seq[Trader]] = db.run(traderTable.filter(_.organizationID === organizationID).filter(_.completionStatus === completionStatus).filter(_.verificationStatus.? === verificationStatus).result)

  private def getVerifiedTradersByOrganizationID(organizationID: String): Future[Seq[Trader]] = db.run(traderTable.filter(_.organizationID === organizationID).filter(_.verificationStatus === true).result)

  private def getTradersByOrganizationID(organizationID: String): Future[Seq[Trader]] = db.run(traderTable.filter(_.organizationID === organizationID).result)

  private def getTradersByTraderIDs(traderIDs:Seq[String]): Future[Seq[Trader]] = db.run(traderTable.filter(_.id inSet traderIDs).result)

  private def checkOrganizationIDTraderIDExists(traderID: String, organizationID: String): Future[Boolean] = db.run(traderTable.filter(_.id === traderID).filter(_.organizationID === organizationID).exists.result)

  private def updateVerificationStatusOnID(id: String, verificationStatus: Option[Boolean]): Future[Int] = db.run(traderTable.filter(_.id === id).map(_.verificationStatus.?).update(verificationStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateCompletionStatusOnID(id: String, completionStatus: Boolean) = db.run(traderTable.filter(_.id === id).map(_.completionStatus).update(completionStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateVerificationStatusOnAccountID(accountID: String, verificationStatus: Option[Boolean]) = db.run(traderTable.filter(_.accountID === accountID).map(_.verificationStatus.?).update(verificationStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class TraderTable(tag: Tag) extends Table[Trader](tag, "Trader") {

    def * = (id, zoneID, organizationID, accountID, name, completionStatus, verificationStatus.?) <> (Trader.tupled, Trader.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def zoneID = column[String]("zoneID")

    def organizationID = column[String]("organizationID")

    def accountID = column[String]("accountID")

    def name = column[String]("name")

    def completionStatus = column[Boolean]("completionStatus")

    def verificationStatus = column[Boolean]("verificationStatus")

  }

  object Service {

    def create(zoneID: String, organizationID: String, accountID: String, name: String): Future[String] = add(Trader(utilities.IDGenerator.hexadecimal, zoneID, organizationID, accountID, name))

    def insertOrUpdate(zoneID: String, organizationID: String, accountID: String, name: String): Future[String] = {

      val id = getIDByAccountID(accountID)

      def upsertTrader(id: String): Future[Int] = upsert(Trader(id = id, zoneID = zoneID, organizationID = organizationID, accountID = accountID, name = name, completionStatus = true))

      for {
        id <- id
        _ <- upsertTrader(id.getOrElse(utilities.IDGenerator.hexadecimal))
      } yield id.getOrElse(utilities.IDGenerator.hexadecimal)
    }

    def getID(accountID: String): Future[String] = getIDByAccountID(accountID).map { id => id.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)) }

    def get(id: String): Future[Trader] = findById(id)

    def getByAccountID(accountID: String): Future[Trader] = findByAccountId(accountID)

    def getZoneID(id: String): Future[String] = getZoneIDByID(id)

    def getOrganizationID(id: String): Future[String] = getOrganizationIDByID(id)

    def getTraderName(id: String): Future[String] = getTraderNameByID(id)

    def getZoneIDByAccountID(accountID: String): Future[String] = getZoneIDOnAccountID(accountID)

    def getOrganizationIDByAccountID(accountID: String): Future[String] = findOrganizationIDByAccountId(accountID)

    def rejectTrader(id: String): Future[Int] = updateVerificationStatusOnID(id = id, verificationStatus = Option(false))

    def verifyTrader(id: String): Future[Int] = updateVerificationStatusOnID(id = id, verificationStatus = Option(true))

    def rejectTraderByAccountID(accountID: String): Future[Int] = updateVerificationStatusOnAccountID(accountID = accountID, verificationStatus = Option(false))

    def verifyTraderByAccountID(accountID: String): Future[Int] = updateVerificationStatusOnAccountID(accountID = accountID, verificationStatus = Option(true))

    def getAccountId(id: String): Future[String] = getAccountIdById(id)

    def getVerifyTraderRequestsForZone(zoneID: String): Future[Seq[Trader]] = getTradersByCompletionStatusVerificationStatusAndZoneID(zoneID = zoneID, completionStatus = true, verificationStatus = null)

    def getVerifyTraderRequestsForOrganization(organizationID: String): Future[Seq[Trader]] = getTradersByCompletedStatusVerificationStatusByOrganizationID(organizationID = organizationID, completionStatus = true, verificationStatus = null)

    def getVerifiedTradersForOrganization(organizationID: String): Future[Seq[Trader]] = getVerifiedTradersByOrganizationID(organizationID)

    def getVerificationStatus(id: String): Future[Boolean] = getVerificationStatusById(id).map(_.getOrElse(false))

    def getVerificationStatusByAccountID(accountID: String): Future[Boolean] = findVerificationStatusByAccountID(accountID).map(_.getOrElse(false))

    def markTraderFormCompleted(id: String): Future[Int] = updateCompletionStatusOnID(id = id, completionStatus = true)

    def getTradersListInOrganization(organizationID: String): Future[Seq[Trader]] = getTradersByOrganizationID(organizationID)

    def verifyOrganizationTrader(traderID: String, organizationID: String): Future[Boolean] = checkOrganizationIDTraderIDExists(traderID = traderID, organizationID = organizationID)

    def getOrNoneByAccountID(accountID: String): Future[Option[Trader]] = getTraderOrNoneByAccountID(accountID)

    def getTraders(traderIDs:Seq[String])=getTradersByTraderIDs(traderIDs)
  }

}