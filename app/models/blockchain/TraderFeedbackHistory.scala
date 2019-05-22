package models.blockchain

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class TraderFeedbackHistory(address: String, buyerAddress: String, sellerAddress: String, pegHash: String, rating: String)

@Singleton
class TraderFeedbackHistories @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, actorSystem: ActorSystem)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRADER_FEEDBACK_HISTORY

  import databaseConfig.profile.api._

  private[models] val traderFeedbackHistoryTable = TableQuery[TraderFeedbackHistoryTable]

  private def add(traderFeedbackHistory: TraderFeedbackHistory)(implicit executionContext: ExecutionContext): Future[String] = db.run((traderFeedbackHistoryTable returning traderFeedbackHistoryTable.map(_.address) += traderFeedbackHistory).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def update(traderFeedbackHistory: TraderFeedbackHistory): Future[Int] = db.run(traderFeedbackHistoryTable.filter(_.address === traderFeedbackHistory.address).filter(_.buyerAddress === traderFeedbackHistory.buyerAddress).filter(_.sellerAddress === traderFeedbackHistory.sellerAddress).filter(_.pegHash === traderFeedbackHistory.pegHash).update(traderFeedbackHistory).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findById(address: String)(implicit executionContext: ExecutionContext): Future[Seq[TraderFeedbackHistory]] = db.run(traderFeedbackHistoryTable.filter(_.address === address).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(traderFeedbackHistory: TraderFeedbackHistory)(implicit executionContext: ExecutionContext) = db.run(traderFeedbackHistoryTable.filter(_.address === traderFeedbackHistory.address).filter(_.buyerAddress === traderFeedbackHistory.buyerAddress).filter(_.sellerAddress === traderFeedbackHistory.sellerAddress).filter(_.pegHash === traderFeedbackHistory.pegHash).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class TraderFeedbackHistoryTable(tag: Tag) extends Table[TraderFeedbackHistory](tag, "TraderFeedbackHistory_BC") {

    def * = (address, buyerAddress, sellerAddress, pegHash, rating) <> (TraderFeedbackHistory.tupled, TraderFeedbackHistory.unapply)

    def ? = (address.?, buyerAddress.?, sellerAddress.?, pegHash.?, rating.?).shaped.<>({ r => import r._; _1.map(_ => TraderFeedbackHistory.tupled((_1.get, _2.get, _3.get, _4.get, _5.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    def address = column[String]("address", O.PrimaryKey)

    def buyerAddress = column[String]("buyerAddress", O.PrimaryKey)

    def sellerAddress = column[String]("sellerAddress", O.PrimaryKey)

    def pegHash = column[String]("pegHash", O.PrimaryKey)

    def rating = column[String]("rating", O.PrimaryKey)

  }

  object Service {

    def addTraderFeedbackHistory(address: String, buyerAddress: String, sellerAddress: String, pegHash: String, rating: String): String = Await.result(add(TraderFeedbackHistory(address, buyerAddress, sellerAddress, pegHash, rating)), Duration.Inf)

    def getTraderFeedbackHistory(address: String): Seq[TraderFeedbackHistory] = Await.result(findById(address), Duration.Inf)
  }

}