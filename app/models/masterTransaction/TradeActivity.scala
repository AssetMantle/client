package models.masterTransaction

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Database
import models.common.Serializable.TradeActivityMessage
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class TradeActivity(id: String, negotiationID: String, title: String, message: TradeActivityMessage, createdOn: Timestamp, createdBy: String, updatedOn: Option[Timestamp] = None, updatedBy: Option[String] = None, timezone: String) extends Database

@Singleton
class TradeActivities @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_TRADE_ACTIVITY

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private val nodeID = configuration.get[String]("node.id")

  private val nodeTimezone = configuration.get[String]("node.timezone")

  private val notificationsPerPageLimit = configuration.get[Int]("notification.notificationsPerPage")

  case class TradeActivitySerializable(id: String, negotiationID: String, title: String, message: String, createdOn: Timestamp, createdBy: String, updatedOn: Option[Timestamp], updatedBy: Option[String], timezone: String) {
    def deserialize(): TradeActivity = TradeActivity(id = id, negotiationID = negotiationID, title = title, message = utilities.JSON.convertJsonStringToObject[TradeActivityMessage](message), createdOn = createdOn, createdBy = createdBy, updatedBy = updatedBy, updatedOn = updatedOn, timezone = timezone)
  }

  def serialize(tradeActivity: TradeActivity): TradeActivitySerializable = TradeActivitySerializable(id = tradeActivity.id, negotiationID = tradeActivity.negotiationID, title = tradeActivity.title, message = Json.toJson(tradeActivity.message).toString(), createdOn = tradeActivity.createdOn, createdBy = tradeActivity.createdBy, updatedBy = tradeActivity.updatedBy, updatedOn = tradeActivity.updatedOn, timezone = tradeActivity.timezone)

  private[models] val tradeActivityTable = TableQuery[TradeActivityTable]

  private def add(tradeActivitySerializable: TradeActivitySerializable): Future[String] = db.run((tradeActivityTable returning tradeActivityTable.map(_.id) += tradeActivitySerializable).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findAllByNegotiationID(negotiationID: String, offset: Int, limit: Int): Future[Seq[TradeActivitySerializable]] = db.run(tradeActivityTable.filter(_.negotiationID === negotiationID).sortBy(_.createdOn.desc).drop(offset).take(limit).result)

  private def deleteById(negotiationID: String): Future[Int] = db.run(tradeActivityTable.filter(_.negotiationID === negotiationID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class TradeActivityTable(tag: Tag) extends Table[TradeActivitySerializable](tag, "TradeActivity") {

    def * = (id, negotiationID, title, message, createdOn, createdBy, updatedOn.?, updatedBy.?, timezone) <> (TradeActivitySerializable.tupled, TradeActivitySerializable.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def negotiationID = column[String]("negotiationID")

    def title = column[String]("title")

    def message = column[String]("message")

    def createdOn = column[Timestamp]("createdOn")

    def createdBy = column[String]("createdBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedBy = column[String]("updatedBy")

    def timezone = column[String]("timezone")

  }

  object Service {
    def insert(negotiationID: String, tradeActivity: constants.TradeActivity, parameters: String*): Future[String] = add(serialize(TradeActivity(id = utilities.IDGenerator.hexadecimal, negotiationID = negotiationID, title = tradeActivity.title, message = TradeActivityMessage(header = tradeActivity.message, parameters = parameters), createdOn = new Timestamp(System.currentTimeMillis()), createdBy = nodeID, timezone = nodeTimezone)))

    def getAllTradeActivities(negotiationID: String, pageNumber: Int): Future[Seq[TradeActivity]] = findAllByNegotiationID(negotiationID = negotiationID, offset = pageNumber * notificationsPerPageLimit, limit = notificationsPerPageLimit).map(serializedTradeActivities => serializedTradeActivities.map(_.deserialize()))
  }

}
