package models.master

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

case class Trader(id: String, zoneID: String, organizationID: String, accountID: String, status: Option[Boolean] = None, comment: Option[String] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

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
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(trader: Trader): Future[Int] = db.run(traderTable.insertOrUpdate(trader).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findById(id: String): Future[Trader] = db.run(traderTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findByAccountId(accountID: String): Future[Trader] = db.run(traderTable.filter(_.accountID === accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findOrganizationIDByAccountId(accountID: String): Future[String] = db.run(traderTable.filter(_.accountID === accountID).map(_.organizationID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getAccountIdById(id: String): Future[String] = db.run(traderTable.filter(_.id === id).map(_.accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getIDByAccountID(accountID: String): Future[Option[String]] = db.run(traderTable.filter(_.accountID === accountID).map(_.id).result.headOption)

  private def getZoneIDOnAccountID(accountID: String): Future[String] = db.run(traderTable.filter(_.accountID === accountID).map(_.zoneID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getZoneIDByID(id: String): Future[String] = db.run(traderTable.filter(_.id === id).map(_.zoneID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getZoneIDsByIDs(ids: Seq[String]): Future[Seq[String]] = db.run(traderTable.filter(_.id inSet ids).map(_.zoneID).result)

  private def getOrganizationIDsByIDs(ids: Seq[String]): Future[Seq[String]] = db.run(traderTable.filter(_.id inSet ids).map(_.organizationID).result)

  private def getOrganizationIDByID(id: String): Future[String] = db.run(traderTable.filter(_.id === id).map(_.organizationID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getTraderByAccountID(accountID: String): Future[Option[Trader]] = db.run(traderTable.filter(_.accountID === accountID).result.headOption)

  private def tryGetStatusById(id: String): Future[Option[Boolean]] = db.run(traderTable.filter(_.id === id).map(_.status.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteById(id: String) = db.run(traderTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getTradersByStatusAndZoneID(zoneID: String, status: Option[Boolean]): Future[Seq[Trader]] = db.run(traderTable.filter(_.zoneID === zoneID).filter(_.status.? === status).sortBy(x => x.updatedOn.ifNull(x.createdOn).desc).result)

  private def getTradersByStatusByOrganizationID(organizationID: String, status: Option[Boolean]): Future[Seq[Trader]] = db.run(traderTable.filter(_.organizationID === organizationID).filter(_.status.? === status).sortBy(x => x.updatedOn.ifNull(x.createdOn).desc).result)

  private def getTraderIDsByStatusByOrganizationID(organizationID: String, status: Option[Boolean]): Future[Seq[String]] = db.run(traderTable.filter(_.organizationID === organizationID).filter(_.status.? === status).map(_.id).result)

  private def getTraderIDsByStatusByZoneID(zoneID: String, status: Option[Boolean]): Future[Seq[String]] = db.run(traderTable.filter(_.zoneID === zoneID).filter(_.status.? === status).map(_.id).result)

  private def getTradersByTraderIDs(traderIDs: Seq[String]): Future[Seq[Trader]] = db.run(traderTable.filter(_.id inSet traderIDs).result)

  private def findTradersByZoneID(zoneID: String): Future[Seq[Trader]] = db.run(traderTable.filter(_.zoneID === zoneID).result)

  private def findTradersByOrganizationID(organizationID: String): Future[Seq[Trader]] = db.run(traderTable.filter(_.organizationID === organizationID).result)

  private def findTraderIDsByZoneID(zoneID: String): Future[Seq[String]] = db.run(traderTable.filter(_.zoneID === zoneID).map(_.id).result)

  private def findTraderIDsByOrganizationID(organizationID: String): Future[Seq[String]] = db.run(traderTable.filter(_.organizationID === organizationID).map(_.id).result)

  private def checkOrganizationIDTraderIDExists(traderID: String, organizationID: String): Future[Boolean] = db.run(traderTable.filter(_.id === traderID).filter(_.organizationID === organizationID).exists.result)

  private def updateStatusAndCommentByID(id: String, status: Option[Boolean], comment: Option[String]): Future[Int] = db.run(traderTable.filter(_.id === id).map(x => (x.status.?, x.comment.?)).update((status, comment)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class TraderTable(tag: Tag) extends Table[Trader](tag, "Trader") {

    def * = (id, zoneID, organizationID, accountID, status.?, comment.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Trader.tupled, Trader.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def zoneID = column[String]("zoneID")

    def organizationID = column[String]("organizationID")

    def accountID = column[String]("accountID")

    def status = column[Boolean]("status")

    def comment = column[String]("comment")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(zoneID: String, organizationID: String, accountID: String, name: String): Future[String] = add(Trader(utilities.IDGenerator.hexadecimal, zoneID, organizationID, accountID))

    def insertOrUpdate(zoneID: String, organizationID: String, accountID: String): Future[String] = {
      val id = getIDByAccountID(accountID).map(_.getOrElse(utilities.IDGenerator.hexadecimal))

      def upsertTrader(id: String): Future[Int] = upsert(Trader(id = id, zoneID = zoneID, organizationID = organizationID, accountID = accountID))

      for {
        id <- id
        _ <- upsertTrader(id)
      } yield id
    }

    def tryGetID(accountID: String): Future[String] = getIDByAccountID(accountID).map { id => id.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)) }

    def getID(accountID: String): Future[Option[String]] = getIDByAccountID(accountID)

    def tryGet(id: String): Future[Trader] = findById(id)

    def tryGetByAccountID(accountID: String): Future[Trader] = findByAccountId(accountID)

    def tryGetZoneID(id: String): Future[String] = getZoneIDByID(id)

    def tryGetZoneIDs(ids: Seq[String]): Future[Seq[String]] = getZoneIDsByIDs(ids)

    def tryGetOrganizationIDs(ids: Seq[String]): Future[Seq[String]] = getOrganizationIDsByIDs(ids)

    def tryGetOrganizationID(id: String): Future[String] = getOrganizationIDByID(id)

    def tryGetZoneIDByAccountID(accountID: String): Future[String] = getZoneIDOnAccountID(accountID)

    def getOrganizationIDByAccountID(accountID: String): Future[String] = findOrganizationIDByAccountId(accountID)

    def markAccepted(id: String): Future[Int] = updateStatusAndCommentByID(id = id, status = Option(true), comment = None)

    def markRejected(id: String, comment: Option[String]): Future[Int] = updateStatusAndCommentByID(id = id, status = Option(false), comment = comment)

    def tryGetAccountId(id: String): Future[String] = getAccountIdById(id)

    def tryGetStatus(id: String): Future[Boolean] = tryGetStatusById(id).map(_.getOrElse(false))

    def getOrganizationAcceptedTraderList(organizationID: String): Future[Seq[Trader]] = getTradersByStatusByOrganizationID(organizationID = organizationID, status = Option(true))

    def getVerifiedTraderIDsByOrganizationID(organizationID: String): Future[Seq[String]] = getTraderIDsByStatusByOrganizationID(organizationID = organizationID, status = Option(true))

    def getVerifiedTraderIDsByZoneID(zoneID: String): Future[Seq[String]] = getTraderIDsByStatusByZoneID(zoneID = zoneID, status = Option(true))

    def getOrganizationPendingTraderRequestList(organizationID: String): Future[Seq[Trader]] = getTradersByStatusByOrganizationID(organizationID = organizationID, status = null)

    def getOrganizationRejectedTraderRequestList(organizationID: String): Future[Seq[Trader]] = getTradersByStatusByOrganizationID(organizationID = organizationID, status = Option(false))

    def getZoneAcceptedTraderList(zoneID: String): Future[Seq[Trader]] = getTradersByStatusAndZoneID(zoneID = zoneID, status = Option(true))

    def getZonePendingTraderRequestList(zoneID: String): Future[Seq[Trader]] = getTradersByStatusAndZoneID(zoneID = zoneID, status = null)

    def getZoneRejectedTraderRequestList(zoneID: String): Future[Seq[Trader]] = getTradersByStatusAndZoneID(zoneID = zoneID, status = Option(false))

    def verifyOrganizationTrader(traderID: String, organizationID: String): Future[Boolean] = checkOrganizationIDTraderIDExists(traderID = traderID, organizationID = organizationID)

    def getByAccountID(accountID: String): Future[Option[Trader]] = getTraderByAccountID(accountID)

    def getTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = getTradersByTraderIDs(traderIDs)

    def getTradersByZoneID(zoneID: String): Future[Seq[Trader]] = findTradersByZoneID(zoneID)

    def getTraderIDsByZoneID(zoneID: String): Future[Seq[String]] = findTraderIDsByZoneID(zoneID)

    def getTraderIDsByOrganizationID(organizationID: String): Future[Seq[String]] = findTraderIDsByOrganizationID(organizationID)
  }

}