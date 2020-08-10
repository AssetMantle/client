package models.masterTransaction

import java.sql.Timestamp
import java.util.TimeZone

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.blockchain
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class TokenPrice(serial: Int = 0, symbol: String, price: Double, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class TokenPrices @Inject()(
                             actorSystem: ActorSystem,
                             protected val databaseConfigProvider: DatabaseConfigProvider,
                             configuration: Configuration,
                             blockchainTokens: blockchain.Tokens,
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

  private def getPrices(symbol: String, n: Int): Future[Seq[TokenPrice]] = db.run(tokenPriceTable.filter(_.symbol === symbol).sortBy(_.serial.desc).take(n).result)

  private def getLatestPrices(n: Int, totalTokens: Int): Future[Seq[TokenPrice]] = db.run(tokenPriceTable.sortBy(_.serial.desc).take(n * totalTokens).result)

  private[models] class TokenPriceTable(tag: Tag) extends Table[TokenPrice](tag, "TokenPrice") {

    def * = (serial, symbol, price, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (TokenPrice.tupled, TokenPrice.unapply)

    def serial = column[Int]("serial", O.PrimaryKey, O.AutoInc)

    def symbol = column[String]("symbol")

    def price = column[Double]("price")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(symbol: String, price: Double): Future[Int] = add(TokenPrice(symbol = symbol, price = price))

    def get(symbol: String, from: Timestamp, to: Timestamp, timeZone: TimeZone = TimeZone.getTimeZone("UTC")): Future[Seq[TokenPrice]] = getPrices(symbol, 5)

    def get(symbol: String, n: Int): Future[Seq[TokenPrice]] = getPrices(symbol, n)

    def getLatest(n: Int, totalTokens: Int): Future[Seq[TokenPrice]] = getLatestPrices(n = n, totalTokens = totalTokens)

  }

  object Utility {
    def insertPrice(): Future[Unit] = {
      val r = new Random(System.currentTimeMillis())
      val symbols = blockchainTokens.Service.getAllSymbols

      def update(symbols: Seq[String]) = {
        Future.traverse(symbols) { symbol =>
          val price = Future(2.5 + 5 * r.nextDouble())
          for {
            price <- price
            _ <- Service.create(symbol = symbol, price = price)
          } yield ()
        }
      }

      (for {
        symbols <- symbols
        _ <- update(symbols)
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