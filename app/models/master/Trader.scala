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

case class Trader(id: String, zoneID: String, organizationID: String, accountID: String, name: String, status: Option[Boolean] = None, comment: Option[String] = None)

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

  private def getIDByAccountID(accountID: String): Future[Option[String]] = db.run(traderTable.filter(_.accountID === accountID).map(_.id).result.headOption)

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

  private def getZoneIDsByIDs(ids: Seq[String]): Future[Seq[String]] = db.run(traderTable.filter(_.id inSet ids).map(_.zoneID).result)

  private def getOrganizationIDsByIDs(ids: Seq[String]): Future[Seq[String]] = db.run(traderTable.filter(_.id inSet ids).map(_.organizationID).result)

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

  private def getByAccountID(accountID: String): Future[Option[Trader]] = db.run(traderTable.filter(_.accountID === accountID).result.headOption)

  private def tryGetStatusById(id: String): Future[Option[Boolean]] = db.run(traderTable.filter(_.id === id).map(_.status.?).result.head.asTry).map {
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

  private def getTradersByStatusAndZoneID(zoneID: String, status: Option[Boolean]): Future[Seq[Trader]] = db.run(traderTable.filter(_.zoneID === zoneID).filter(_.status.? === status).result)

  private def getTradersByStatusByOrganizationID(organizationID: String, status: Option[Boolean]): Future[Seq[Trader]] = db.run(traderTable.filter(_.organizationID === organizationID).filter(_.status.? === status).result)

  private def getTradersByTraderIDs(traderIDs: Seq[String]): Future[Seq[Trader]] = db.run(traderTable.filter(_.id inSet traderIDs).result)

  private def findTradersByZoneID(zoneID: String): Future[Seq[Trader]] = db.run(traderTable.filter(_.zoneID === zoneID).result)

  private def findTradersByOrganizationID(organizationID: String): Future[Seq[Trader]] = db.run(traderTable.filter(_.organizationID === organizationID).result)

  private def findTraderIDsByZoneID(zoneID: String): Future[Seq[String]] = db.run(traderTable.filter(_.zoneID === zoneID).map(_.id).result)

  private def findTraderIDsByOrganizationID(organizationID: String): Future[Seq[String]] = db.run(traderTable.filter(_.organizationID === organizationID).map(_.id).result)

  private def checkOrganizationIDTraderIDExists(traderID: String, organizationID: String): Future[Boolean] = db.run(traderTable.filter(_.id === traderID).filter(_.organizationID === organizationID).exists.result)

  private def updateStatusAndCommentByID(id: String, status: Option[Boolean], comment: Option[String]): Future[Int] = db.run(traderTable.filter(_.id === id).map(x => (x.status.?, x.comment.?)).update((status, comment)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class TraderTable(tag: Tag) extends Table[Trader](tag, "Trader") {

    def * = (id, zoneID, organizationID, accountID, name, status.?, comment.?) <> (Trader.tupled, Trader.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def zoneID = column[String]("zoneID")

    def organizationID = column[String]("organizationID")

    def accountID = column[String]("accountID")

    def name = column[String]("name")

    def status = column[Boolean]("status")

    def comment = column[String]("comment")

  }

  object Service {

    def create(zoneID: String, organizationID: String, accountID: String, name: String): Future[String] = add(Trader(utilities.IDGenerator.hexadecimal, zoneID, organizationID, accountID, name))

    def insertOrUpdate(zoneID: String, organizationID: String, accountID: String, name: String): Future[String] = {

      val id = getIDByAccountID(accountID)

      def upsertTrader(id: String): Future[Int] = upsert(Trader(id = id, zoneID = zoneID, organizationID = organizationID, accountID = accountID, name = name))

      for {
        id <- id
        _ <- upsertTrader(id.getOrElse(utilities.IDGenerator.hexadecimal))
      } yield id.getOrElse(utilities.IDGenerator.hexadecimal)
    }

    def tryGetID(accountID: String): Future[String] = getIDByAccountID(accountID).map { id => id.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)) }

    def getID(accountID: String): Future[Option[String]] = getIDByAccountID(accountID)

    def tryGet(id: String): Future[Trader] = findById(id)

    def tryGetByAccountID(accountID: String): Future[Trader] = findByAccountId(accountID)

    def tryGetZoneID(id: String): Future[String] = getZoneIDByID(id)

    def tryGetZoneIDs(ids: Seq[String]): Future[Seq[String]] = getZoneIDsByIDs(ids)

    def tryGetOrganizationIDs(ids: Seq[String]): Future[Seq[String]] = getOrganizationIDsByIDs(ids)

    def tryGetOrganizationID(id: String): Future[String] = getOrganizationIDByID(id)

    def tryGetTraderName(id: String): Future[String] = getTraderNameByID(id)

    def tryGetZoneIDByAccountID(accountID: String): Future[String] = getZoneIDOnAccountID(accountID)

    def getOrganizationIDByAccountID(accountID: String): Future[String] = findOrganizationIDByAccountId(accountID)

    def markAccepted(id: String): Future[Int] = updateStatusAndCommentByID(id = id, status = Option(true), comment = None)

    def markRejected(id: String, comment: Option[String]): Future[Int] = updateStatusAndCommentByID(id = id, status = Option(false), comment = comment)

    def tryGetAccountId(id: String): Future[String] = getAccountIdById(id)

    def tryGetStatus(id: String): Future[Boolean] = tryGetStatusById(id).map(_.getOrElse(false))

    def getOrganizationAcceptedTraderList(organizationID: String): Future[Seq[Trader]] = getTradersByStatusByOrganizationID(organizationID = organizationID, status = Option(true))

    def getOrganizationPendingTraderRequestList(organizationID: String): Future[Seq[Trader]] = getTradersByStatusByOrganizationID(organizationID = organizationID, status = null)

    def getOrganizationRejectedTraderRequestList(organizationID: String): Future[Seq[Trader]] = getTradersByStatusByOrganizationID(organizationID = organizationID, status = Option(false))

    def getZoneAcceptedTraderList(zoneID: String): Future[Seq[Trader]] = getTradersByStatusAndZoneID(zoneID = zoneID, status = Option(true))

    def getZonePendingTraderRequestList(zoneID: String): Future[Seq[Trader]] = getTradersByStatusAndZoneID(zoneID = zoneID, status = null)

    def getZoneRejectedTraderRequestList(zoneID: String): Future[Seq[Trader]] = getTradersByStatusAndZoneID(zoneID = zoneID, status = Option(false))

    def verifyOrganizationTrader(traderID: String, organizationID: String): Future[Boolean] = checkOrganizationIDTraderIDExists(traderID = traderID, organizationID = organizationID)

    def getOrNoneByAccountID(accountID: String): Future[Option[Trader]] = getByAccountID(accountID)

    def getTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = getTradersByTraderIDs(traderIDs)

    def getTradersByZoneID(zoneID: String): Future[Seq[Trader]] = findTradersByZoneID(zoneID)

    def getTraderIDsByZoneID(zoneID: String): Future[Seq[String]] = findTraderIDsByZoneID(zoneID)

    def getTraderIDsByOrganizationID(organizationID: String): Future[Seq[String]] = findTraderIDsByOrganizationID(organizationID)
  }

}