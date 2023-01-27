package models.masterTransaction

import akka.actor.ActorSystem
import exceptions.BaseException
import models.traits.Logging
import models.blockchain
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.Configuration
import play.api.Logger
import queries.coingecko.GetTicker
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class TokenPrice(serial: Int = 0, denom: String, price: Double, createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging

@Singleton
class TokenPrices @Inject()(
                             actorSystem: ActorSystem,
                             protected val databaseConfigProvider: DatabaseConfigProvider,
                             configuration: Configuration,
                             getCoingeckoTicker: GetTicker,
                             blockchainTokens: blockchain.Tokens,
                             utilitiesOperations: utilities.Operations
                           )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_TOKEN_PRICE

  import databaseConfig.profile.api._

  private[models] val tokenPriceTable = TableQuery[TokenPriceTable]

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  private val tokenPriceInitialDelay = configuration.get[Int]("blockchain.token.priceInitialDelay")

  private val tokenPriceUpdateRate = configuration.get[Int]("blockchain.token.priceUpdateRate")

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

  private def tryGetLatestTokenPriceBySerial(denom: String): Future[TokenPrice] = db.run(tokenPriceTable.filter(_.denom === denom).sortBy(_.serial.desc).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.CRYPTO_TOKEN_NOT_FOUND, noSuchElementException)
    }
  }

  private def getLatestTokenPriceBySerial(denom: String): Future[Option[TokenPrice]] = db.run(tokenPriceTable.filter(_.denom === denom).sortBy(_.serial.desc).result.headOption)

  private def getLatestSerial: Future[Int] = db.run(tokenPriceTable.map(_.serial).max.result.asTry).map {
    case Success(result) => result.getOrElse(0)
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.CRYPTO_TOKEN_NOT_FOUND, noSuchElementException)
    }
  }

  private def getAllTokensBySerial(startSerial: Int, endSerial: Int): Future[Seq[TokenPrice]] = db.run(tokenPriceTable.filter(x => x.serial >= startSerial && x.serial <= endSerial).result)

  private[models] class TokenPriceTable(tag: Tag) extends Table[TokenPrice](tag, "TokenPrice") {

    def * = (serial, denom, price, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (TokenPrice.tupled, TokenPrice.unapply)

    def serial = column[Int]("serial", O.PrimaryKey, O.AutoInc)

    def denom = column[String]("denom")

    def price = column[Double]("price")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")
  }

  object Service {

    def create(denom: String, price: Double): Future[Int] = add(TokenPrice(denom = denom, price = price))

    def getLatestByToken(denom: String, n: Int): Future[Seq[TokenPrice]] = getLatestTokens(denom = denom, n = n)

    def tryGetLatestTokenPrice(denom: String): Future[TokenPrice] = tryGetLatestTokenPriceBySerial(denom = denom)

    def getLatestTokenPrice(denom: String): Future[TokenPrice] = getLatestTokenPriceBySerial(denom = denom).map(_.getOrElse(TokenPrice(denom = denom, price = 0.0)))

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
      val tokenTicker = constants.AppConfig.tokenTickers.find(_.denom == constants.Blockchain.StakingDenom)
      if (tokenTicker.isDefined) {
        val price = getCoingeckoTicker.Service.get().map(_.assetmantle.usd)
        (for {
          price <- price
          _ <- Service.create(denom = constants.Blockchain.StakingDenom, price = price)
        } yield ()).recover {
          case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        }
      } else {
        logger.warn(constants.Response.CRYPTO_TOKEN_TICKER_NOT_FOUND.logMessage)
        Future()
      }
    }
  }

  private val runnable = new Runnable {
    def run(): Unit = Utility.insertPrice()
  }

  actorSystem.scheduler.scheduleAtFixedRate(tokenPriceInitialDelay.milliseconds, ((24 * 60 * 60) / tokenPriceUpdateRate).seconds)(runnable)(schedulerExecutionContext)

}