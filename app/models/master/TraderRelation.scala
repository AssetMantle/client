package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class TraderRelation(id: String, fromID: String, toID: String, status: Option[Boolean] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class TraderRelations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private[models] val traderRelationTable = TableQuery[TraderRelationTable]

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRADER_RELATION

  import databaseConfig.profile.api._

  private def add(traderRelation: TraderRelation): Future[String] = db.run((traderRelationTable returning traderRelationTable.map(_.id) += traderRelation).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(ftraderRelation: TraderRelation): Future[Int] = db.run(traderRelationTable.insertOrUpdate(ftraderRelation).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateStatusByID(id: String, status: Option[Boolean]): Future[Int] = db.run(traderRelationTable.filter(_.id === id).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def deleteByID(id: String): Future[Int] = db.run(traderRelationTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getByID(id: String): Future[Option[TraderRelation]] = db.run(traderRelationTable.filter(_.id === id).result.headOption)

  private def tryGetByID(id: String): Future[TraderRelation] = db.run(traderRelationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def checkByFromIDToIDAndStatus(id: String, status: Option[Boolean]): Future[Boolean] = db.run(traderRelationTable.filter(_.id === id).filter(_.status.? === status).exists.result)

  private def findAllByFromIDOrToIDAndStatus(id: String, status: Option[Boolean]): Future[Seq[TraderRelation]] = db.run(traderRelationTable.filter(relation => relation.fromID === id || relation.toID === id).filter(_.status.? === status).result)

  private def findAllByFromIDAndStatus(fromID: String, status: Option[Boolean]): Future[Seq[TraderRelation]] = db.run(traderRelationTable.filter(_.fromID === fromID).filter(_.status.? === status).result)

  private def findAllByToIDAndStatus(toID: String, status: Option[Boolean]): Future[Seq[TraderRelation]] = db.run(traderRelationTable.filter(_.toID === toID).filter(_.status.? === status).result)

  private def getTraderRelationIDByFromIDAndToID(fromID: String, toID: String): String = Seq(fromID, toID).sorted.mkString("")

  private[models] class TraderRelationTable(tag: Tag) extends Table[TraderRelation](tag, "TraderRelation") {

    def * = (id, fromID, toID, status.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (TraderRelation.tupled, TraderRelation.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def fromID = column[String]("fromID")

    def toID = column[String]("toID")

    def status = column[Boolean]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def insertOrUpdate(fromID: String, toID: String): Future[Int] = if (fromID == toID) {
      throw new BaseException(constants.Response.COUNTERPARTY_CANNOT_BE_SELF)
    } else {
      upsert(TraderRelation(id = getTraderRelationIDByFromIDAndToID(fromID = fromID, toID = toID), fromID = fromID, toID = toID))
    }

    def markAccepted(fromID: String, toID: String): Future[Int] = updateStatusByID(id = getTraderRelationIDByFromIDAndToID(fromID = fromID, toID = toID), status = Option(true))

    def getAllAcceptedTraderRelation(id: String): Future[Seq[TraderRelation]] = findAllByFromIDOrToIDAndStatus(id = id, status = Option(true))

    def getAllReceivedPendingTraderRelation(toID: String): Future[Seq[TraderRelation]] = findAllByToIDAndStatus(toID = toID, status = null)

    def getAllSentPendingTraderRelation(fromID: String): Future[Seq[TraderRelation]] = findAllByFromIDAndStatus(fromID = fromID, status = null)

    def tryGet(fromID: String, toID: String): Future[TraderRelation] = tryGetByID(getTraderRelationIDByFromIDAndToID(fromID = fromID, toID = toID))

    def get(fromID: String, toID: String): Future[Option[TraderRelation]] = getByID(getTraderRelationIDByFromIDAndToID(fromID = fromID, toID = toID))

    def markRejected(fromID: String, toID: String): Future[Int] = updateStatusByID(id = getTraderRelationIDByFromIDAndToID(fromID = fromID, toID = toID), status = Option(false))

    def getAllCounterParties(id: String): Future[Seq[String]] = findAllByFromIDOrToIDAndStatus(id = id, status = Option(true)).map(_.map(tradeRelation => if (tradeRelation.fromID == id) tradeRelation.toID else tradeRelation.fromID))

    def checkRelationExists(fromID: String, toID: String): Future[Boolean] = checkByFromIDToIDAndStatus(id = getTraderRelationIDByFromIDAndToID(fromID = fromID, toID = toID), status = Option(true))
  }

}
