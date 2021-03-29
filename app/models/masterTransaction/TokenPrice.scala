package models.masterTransaction

import java.sql.Timestamp
import java.util.TimeZone

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.blockchain
import queries.ascendex.GetTicker
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

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

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  private val tokenPriceIntialDelay = configuration.get[Int]("blockchain.token.priceInitialDelay")

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
      val denoms = blockchainTokens.Service.getAllDenoms

      def update(denoms: Seq[String]) = utilitiesOperations.traverse(denoms) { denom =>
        val denomTicker = constants.Blockchain.TokenTickers.get(denom)
        if (denomTicker.isDefined) {
          val price = getAscendexTicker.Service.get(denomTicker.get).map(_.data.close.toDouble)
          for {
            price <- price
            _ <- Service.create(denom = denom, price = price)
          } yield ()
        } else Future()
      }

      (for {
        denoms <- denoms
        _ <- update(denoms)
      } yield ()
        ).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  private val runnable = new Runnable {
    def run(): Unit = Utility.insertPrice()
  }

  actorSystem.scheduler.scheduleAtFixedRate(tokenPriceIntialDelay.milliseconds, ((24 * 60 * 60) / tokenPriceUpdateRate).seconds)(runnable)(schedulerExecutionContext)

}