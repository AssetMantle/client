package models.blockchain

import exceptions.BaseException
import javax.inject.Inject
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Negotiation(id: String, buyerAddress: String, sellerAddress: String, assetPegHash: String, bid: Int, time: Int, buyerSignature: Option[String], sellerSignature: Option[String])

class Negotiations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_NEGOTIATION

  private[models] val negotiationTable = TableQuery[NegotiationTable]

  private def add(negotiation: Negotiation)(implicit executionContext: ExecutionContext): Future[String] = db.run((negotiationTable returning negotiationTable.map(_.id) += negotiation).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def insertOrUpdate(negotiation: Negotiation)(implicit executionContext: ExecutionContext): Future[Int] = db.run(negotiationTable.insertOrUpdate(negotiation).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String)(implicit executionContext: ExecutionContext): Future[Negotiation] = db.run(negotiationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(negotiationTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateBuyerSignatureById(id: String, buyerSignature: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.buyerSignature).update(buyerSignature).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateSellerSignatureById(id: String, sellerSignature: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.sellerSignature).update(sellerSignature).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateBidAndTimeById(id: String, bid: Int, time: Int)(implicit executionContext: ExecutionContext): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(x => (x.bid, x.time)).update((bid, time)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class NegotiationTable(tag: Tag) extends Table[Negotiation](tag, "Negotiation_BC") {

    def * = (id, buyerAddress, sellerAddress, assetPegHash, bid, time, buyerSignature.?, sellerSignature.?) <> (Negotiation.tupled, Negotiation.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def buyerAddress = column[String]("buyerAddress")

    def sellerAddress = column[String]("sellerAddress")

    def assetPegHash = column[String]("assetPegHash")

    def bid = column[Int]("bid")

    def time = column[Int]("time")

    def buyerSignature = column[String]("buyerSignature")

    def sellerSignature = column[String]("sellerSignature")

  }

  object Service {

    def addNegotiation(id: String, buyerAddress: String, sellerAddress: String, assetPegHash: String, bid: Int, time: Int, buyerSignature: Option[String], sellerSignature: Option[String])(implicit executionContext: ExecutionContext): String = Await.result(add(Negotiation(id = id, buyerAddress = buyerAddress, sellerAddress = sellerAddress, assetPegHash = assetPegHash, bid = bid, time = time, buyerSignature = buyerSignature, sellerSignature = sellerSignature)),Duration.Inf)

    def insertOrUpdateNegotiation(id: String, buyerAddress: String, sellerAddress: String, assetPegHash: String, bid: Int, time: Int, buyerSignature: Option[String], sellerSignature: Option[String])(implicit executionContext: ExecutionContext): Int = Await.result(insertOrUpdate(Negotiation(id = id, buyerAddress = buyerAddress, sellerAddress = sellerAddress, assetPegHash = assetPegHash, bid = bid, time = time, buyerSignature = buyerSignature, sellerSignature = sellerSignature)),Duration.Inf)

    def updateBuyerSignature(id: String, buyerSignature: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateBuyerSignatureById(id, buyerSignature),Duration.Inf)

    def updateSellerSignature(id: String, sellerSignature: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateSellerSignatureById(id, sellerSignature),Duration.Inf)

    def updateBidAndTime(id: String, bid: Int, time: Int)(implicit executionContext: ExecutionContext): Int = Await.result(updateBidAndTimeById(id, bid = bid, time = time),Duration.Inf)
  }

}