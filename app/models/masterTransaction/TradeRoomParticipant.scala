package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class TradeRoomParticipant(accountID: String, tradeRoomID: String)

@Singleton
class TradeRoomParticipants @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_TRADE_ROOM_PARTICIPANT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val tradeRoomParticipantTable = TableQuery[TradeRoomParticipantTable]

  private def add(tradeRoomParticipant: TradeRoomParticipant): Future[String] = db.run((tradeRoomParticipantTable returning tradeRoomParticipantTable.map(_.tradeRoomID) += tradeRoomParticipant).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(tradeRoomParticipant: TradeRoomParticipant): Future[Int] = db.run(tradeRoomParticipantTable.insertOrUpdate(tradeRoomParticipant).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private[models] class TradeRoomParticipantTable(tag: Tag) extends Table[TradeRoomParticipant](tag, "TradeRoomParticipant") {

    def * = (accountID, tradeRoomID) <> (TradeRoomParticipant.tupled, TradeRoomParticipant.unapply)

    def accountID = column[String]("accountID", O.PrimaryKey)

    def tradeRoomID = column[String]("tradeRoomID", O.PrimaryKey)
  }

  object Service {
    def create(accountID: String, tradeRoomID: String): Future[String] = {add(TradeRoomParticipant(accountID = accountID, tradeRoomID = tradeRoomID))}

  //    def get()
  }

}
