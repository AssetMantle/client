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

  private def getIDByAccountID(accountID: String): Future[String] = db.run(traderTable.filter(_.accountID === accountID).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getVerificationStatusById(id: String): Future[Option[Boolean]] = db.run(traderTable.filter(_.id === id).map(_.verificationStatus.?).result.head.asTry).map {
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

  private def getTradersWithCompletedStatusNullVerificationStatusByZoneID(zoneID: String): Future[Seq[Trader]] = db.run(traderTable.filter(_.zoneID === zoneID).filter(_.completionStatus === true).filter(_.verificationStatus.?.isEmpty).result)

  private def getTradersWithCompletedStatusNullVerificationStatusByOrganizationID(organizationID: String): Future[Seq[Trader]] = db.run(traderTable.filter(_.organizationID === organizationID).filter(_.completionStatus === true).filter(_.verificationStatus.?.isEmpty).result)

  private def getVerifiedTradersByOrganizationID(organizationID: String): Future[Seq[Trader]] = db.run(traderTable.filter(_.organizationID === organizationID).filter(_.verificationStatus === true).result)

  private def getTradersByOrganizationID(organizationID: String): Future[Seq[Trader]] = db.run(traderTable.filter(_.organizationID === organizationID).result)

  private def checkOrganizationIDTraderIDExists(traderID: String, organizationID: String): Future[Boolean] = db.run(traderTable.filter(_.id === traderID).filter(_.organizationID === organizationID).exists.result)

  private def updateVerificationStatusOnID(id: String, verificationStatus: Option[Boolean]) = db.run(traderTable.filter(_.id === id).map(_.verificationStatus.?).update(verificationStatus).asTry).map {
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

    def create(zoneID: String, organizationID: String, accountID: String, name: String): String = Await.result(add(Trader(utilities.IDGenerator.hexadecimal, zoneID, organizationID, accountID, name)), Duration.Inf)

    def insertOrUpdateTraderDetails(zoneID: String, organizationID: String, accountID: String, name: String): String = {
      val id = try {
        getID(accountID)
      } catch {
        case baseException: BaseException => if (baseException.failure == constants.Response.NO_SUCH_ELEMENT_EXCEPTION) {
          utilities.IDGenerator.hexadecimal
        } else {
          throw new BaseException(baseException.failure)
        }
      }
      Await.result(upsert(Trader(id = id, zoneID = zoneID, organizationID = organizationID, accountID = accountID, name = name)), Duration.Inf)
      id
    }

    def getID(accountID: String): String = Await.result(getIDByAccountID(accountID), Duration.Inf)

    def get(id: String): Trader = Await.result(findById(id), Duration.Inf)

    def getByAccountID(accountID: String): Trader = Await.result(findByAccountId(accountID), Duration.Inf)

    def geOrganizationIDByAccountID(accountID: String): String = Await.result(findOrganizationIDByAccountId(accountID), Duration.Inf)

    def rejectTrader(id: String): Int = Await.result(updateVerificationStatusOnID(id = id, verificationStatus = Option(false)), Duration.Inf)

    def verifyTrader(id: String): Int = Await.result(updateVerificationStatusOnID(id = id, verificationStatus = Option(true)), Duration.Inf)

    def rejectTraderByAccountID(accountID: String): Int = Await.result(updateVerificationStatusOnAccountID(accountID = accountID, verificationStatus = Option(false)), Duration.Inf)

    def verifyTraderByAccountID(accountID: String): Int = Await.result(updateVerificationStatusOnAccountID(accountID = accountID, verificationStatus = Option(true)), Duration.Inf)

    def getAccountId(id: String): String = Await.result(getAccountIdById(id), Duration.Inf)

    def getVerifyTraderRequestsForZone(zoneID: String): Seq[Trader] = Await.result(getTradersWithCompletedStatusNullVerificationStatusByZoneID(zoneID), Duration.Inf)

    def getVerifyTraderRequestsForOrganization(organizationID: String): Seq[Trader] = Await.result(getTradersWithCompletedStatusNullVerificationStatusByOrganizationID(organizationID), Duration.Inf)

    def getVerifiedTradersForOrganization(organizationID: String): Seq[Trader] = Await.result(getVerifiedTradersByOrganizationID(organizationID), Duration.Inf)

    def getVerificationStatus(id: String): Boolean = Await.result(getVerificationStatusById(id), Duration.Inf).getOrElse(false)

    def markTraderFormCompleted(id: String): Int = Await.result(updateCompletionStatusOnID(id = id, completionStatus = true), Duration.Inf)

    def getTradersListInOrganization(organizationID: String): Seq[Trader] = Await.result(getTradersByOrganizationID(organizationID), Duration.Inf)

    def verifyOrganizationTrader(traderID: String, organizationID: String): Boolean = Await.result(checkOrganizationIDTraderIDExists(traderID = traderID, organizationID = organizationID), Duration.Inf)

  }

}