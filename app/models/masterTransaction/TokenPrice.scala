package models.masterTransaction

import akka.actor.ActorSystem
import exceptions.BaseException
import models.Trait.Logged
import models.blockchain
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.ascendex.GetTicker
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class TokenPrice(serial: Int = 0, denom: String, price: Double, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class TokenPrices @Inject()(
                             actorSystem: ActorSystem,
                             protected val databaseConfigProvider: DatabaseConfigProvider,
                             configuration: Configuration,
                             getAscendexTicker: GetTicker,
                             blockchainTokens: blockchain.Tokens,
                             utilitiesOperations: utilities.Operations
                           )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_TOKEN_PRICE

  import databaseConfig.profile.api._

  private[models] val tokenPriceTable = TableQuery[TokenPriceTable]

  private val stakingDenom = configuration.get[String]("blockchain.stakingDenom")

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  private val tokenPriceInitialDelay = configuration.get[Int]("blockchain.token.priceInitialDelay")

  private val tokenPriceUpdateRate = configuration.get[Int]("blockchain.token.priceUpdateRate")

  private implicit val tokenTickers: Seq[utilities.TokenTicker] = configuration.get[Seq[Configuration]]("blockchain.token.tickers").map { tokenTicker =>
    utilities.TokenTicker(denom = tokenTicker.get[String]("denom"), normalizedDenom = tokenTicker.get[String]("normalizedDenom"), ticker = tokenTicker.get[String]("ticker"))
  }

  private def add(tokenPrice: TokenPrice): Future[Int] = db.run((tokenPriceTable returning tokenPriceTable.map(_.serial) += tokenPrice).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.CRYPTO_TOKEN_INSERT_FAILED, psqlException)
    }
  }

  private def getLatestSerial(denom: String): Future[Int] = db.run(tokenPriceTable.filter(_.denom === denom).map(_.serial).max.result.asTry).map {
    case Success(result) => result.getOrElse(0)
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.CRYPTO_TOKEN_NOT_FOUND, noSuchElementException)
    }
  }

  private def getLatestTokens(denom: String, n: Int): Future[Seq[TokenPrice]] = db.run(tokenPriceTable.filter(_.denom === denom).sortBy(_.serial.desc).take(n).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.CRYPTO_TOKEN_NOT_FOUND, noSuchElementException)
    }
  }

  private def getLatestSerial: Future[Int] = db.run(tokenPriceTable.map(_.serial).max.result.asTry).map {
    case Success(result) => result.getOrElse(0)
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.CRYPTO_TOKEN_NOT_FOUND, noSuchElementException)
    }
  }

  private def getAllTokensBySerial(startSerial: Int, endSerial: Int): Future[Seq[TokenPrice]] = db.run(tokenPriceTable.filter(x => x.serial >= startSerial && x.serial <= endSerial).result)

  private[models] class TokenPriceTable(tag: Tag) extends Table[TokenPrice](tag, "TokenPrice") {

    def * = (serial, denom, price, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (TokenPrice.tupled, TokenPrice.unapply)

    def serial = column[Int]("serial", O.PrimaryKey, O.AutoInc)

    def denom = column[String]("denom")

    def price = column[Double]("price")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(denom: String, price: Double): Future[Int] = add(TokenPrice(denom = denom, price = price))

    def getLatestByToken(denom: String, n: Int): Future[Seq[TokenPrice]] = getLatestTokens(denom = denom, n = n)

    def getLatestForAllTokens(n: Int, totalTokens: Int): Future[Seq[TokenPrice]] = {
      (for {
        latestSerial <- getLatestSerial
        tokenPrices <- getAllTokensBySerial(startSerial = latestSerial - (n * totalTokens), endSerial = latestSerial)
      } yield tokenPrices).recover {
        case baseException: BaseException => throw baseException
      }
    }

  }

  object Utility {
    def insertPrice(): Future[Unit] = {
      val tokenTicker = tokenTickers.find(_.denom == stakingDenom)
      if (tokenTicker.isDefined) {
        val price = getAscendexTicker.Service.get(tokenTicker.get.ticker).map(_.data.close.toDouble)
        (for {
          price <- price
          _ <- Service.create(denom = stakingDenom, price = price)
        } yield ()).recover {
          case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        }
      } else {
        logger.error(constants.Response.CRYPTO_TOKEN_TICKER_NOT_FOUND.logMessage)
        Future()
      }
    }
  }

  private val runnable = new Runnable {
    def run(): Unit = Utility.insertPrice()
  }

  actorSystem.scheduler.scheduleAtFixedRate(tokenPriceInitialDelay.milliseconds, ((24 * 60 * 60) / tokenPriceUpdateRate).seconds)(runnable)(schedulerExecutionContext)

}