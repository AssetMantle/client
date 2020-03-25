package models.blockchain

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class TraderFeedbackHistory(address: String, buyerAddress: String, sellerAddress: String, pegHash: String, rating: String)

@Singleton
class TraderFeedbackHistories @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRADER_FEEDBACK_HISTORY

  import databaseConfig.profile.api._

  private[models] val traderFeedbackHistoryTable = TableQuery[TraderFeedbackHistoryTable]

  private def add(traderFeedbackHistory: TraderFeedbackHistory): Future[String] = db.run((traderFeedbackHistoryTable returning traderFeedbackHistoryTable.map(_.address) += traderFeedbackHistory).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(traderFeedbackHistory: TraderFeedbackHistory): Future[Int] = db.run(traderFeedbackHistoryTable.insertOrUpdate(traderFeedbackHistory).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateTraderFeedbackHistory(traderFeedbackHistory: TraderFeedbackHistory): Future[Int] = db.run(traderFeedbackHistoryTable.filter(_.address === traderFeedbackHistory.address).filter(_.buyerAddress === traderFeedbackHistory.buyerAddress).filter(_.sellerAddress === traderFeedbackHistory.sellerAddress).filter(_.pegHash === traderFeedbackHistory.pegHash).update(traderFeedbackHistory).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findBuyersByNullRating(address: String): Future[Seq[TraderFeedbackHistory]] = db.run(traderFeedbackHistoryTable.filter(_.buyerAddress === address).filter(_.rating === "").filterNot(_.address === address).result)

  private def findSellersByNullRating(address: String): Future[Seq[TraderFeedbackHistory]] = db.run(traderFeedbackHistoryTable.filter(_.sellerAddress === address).filter(_.rating === "").filterNot(_.address === address).result)

  private def findById(address: String): Future[Seq[TraderFeedbackHistory]] = db.run(traderFeedbackHistoryTable.filter(_.address === address).result)

  private def deleteById(traderFeedbackHistory: TraderFeedbackHistory) = db.run(traderFeedbackHistoryTable.filter(_.address === traderFeedbackHistory.address).filter(_.buyerAddress === traderFeedbackHistory.buyerAddress).filter(_.sellerAddress === traderFeedbackHistory.sellerAddress).filter(_.pegHash === traderFeedbackHistory.pegHash).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class TraderFeedbackHistoryTable(tag: Tag) extends Table[TraderFeedbackHistory](tag, "TraderFeedbackHistory_BC") {

    def * = (address, buyerAddress, sellerAddress, pegHash, rating) <> (TraderFeedbackHistory.tupled, TraderFeedbackHistory.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def buyerAddress = column[String]("buyerAddress", O.PrimaryKey)

    def sellerAddress = column[String]("sellerAddress", O.PrimaryKey)

    def pegHash = column[String]("pegHash", O.PrimaryKey)

    def rating = column[String]("rating")

  }

  object Service {

    def create(address: String, buyerAddress: String, sellerAddress: String, pegHash: String, rating: String): Future[String] = add(TraderFeedbackHistory(address, buyerAddress, sellerAddress, pegHash, rating))

    def insertOrUpdate(address: String, buyerAddress: String, sellerAddress: String, pegHash: String, rating: String): Future[Int] = upsert(TraderFeedbackHistory(address, buyerAddress, sellerAddress, pegHash, rating))

    def update(address: String, buyerAddress: String, sellerAddress: String, pegHash: String, rating: String): Future[Int] = updateTraderFeedbackHistory(TraderFeedbackHistory(address, buyerAddress, sellerAddress, pegHash, rating))

    def get(address: String): Future[Seq[TraderFeedbackHistory]] = findById(address)

    def getNullRatingsForBuyerFeedback(buyerAddress: String): Future[Seq[TraderFeedbackHistory]] = findBuyersByNullRating(buyerAddress)

    def getNullRatingsForSellerFeedback(sellerAddress: String): Future[Seq[TraderFeedbackHistory]] = findSellersByNullRating(sellerAddress)
  }

}