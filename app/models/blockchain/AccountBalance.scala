package models.blockchain

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Serializable.Coin
import models.common.TransactionMessages.SendCoin
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.GetAccountBalance
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class AccountBalance(address: String, coins: Seq[Coin], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class AccountBalances @Inject()(
                                 protected val databaseConfigProvider: DatabaseConfigProvider,
                                 getAccountBalance: GetAccountBalance,
                                 configuration: Configuration
                               )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ACCOUNT_BALANCE

  import databaseConfig.profile.api._

  private[models] val accountBalanceTable = TableQuery[AccountBalanceTable]

  case class AccountBalanceSerialized(address: String, coins: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: AccountBalance = AccountBalance(address = address, coins = utilities.JSON.convertJsonStringToObject[Seq[Coin]](coins), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(accountBalance: AccountBalance): AccountBalanceSerialized = AccountBalanceSerialized(address = accountBalance.address, coins = Json.toJson(accountBalance.coins).toString, createdBy = accountBalance.createdBy, createdOn = accountBalance.createdOn, createdOnTimeZone = accountBalance.createdOnTimeZone, updatedBy = accountBalance.updatedBy, updatedOn = accountBalance.updatedOn, updatedOnTimeZone = accountBalance.updatedOnTimeZone)

  private def add(accountBalance: AccountBalance): Future[String] = db.run((accountBalanceTable returning accountBalanceTable.map(_.address) += serialize(accountBalance)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(accountBalance: AccountBalance): Future[Int] = db.run(accountBalanceTable.insertOrUpdate(serialize(accountBalance)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByAddress(address: String): Future[AccountBalanceSerialized] = db.run(accountBalanceTable.filter(_.address === address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getCoinsByAddress(address: String): Future[String] = db.run(accountBalanceTable.filter(_.address === address).map(_.coins).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def checkExistsByAddress(address: String): Future[Boolean] = db.run(accountBalanceTable.filter(_.address === address).exists.result)

  private[models] class AccountBalanceTable(tag: Tag) extends Table[AccountBalanceSerialized](tag, "AccountBalance") {

    def * = (address, coins, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (AccountBalanceSerialized.tupled, AccountBalanceSerialized.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def coins = column[String]("coins")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(address: String, coins: Seq[Coin]): Future[String] = add(AccountBalance(address = address, coins = coins))

    def insertOrUpdate(address: String, coins: Seq[Coin]): Future[Int] = upsert(AccountBalance(address = address, coins = coins))

    def get(address: String): Future[AccountBalance] = findByAddress(address).map(_.deserialize)

    def getCoins(address: String): Future[Seq[Coin]] = getCoinsByAddress(address).map(x => utilities.JSON.convertJsonStringToObject[Seq[Coin]](x))

    def checkExists(address: String): Future[Boolean] = checkExistsByAddress(address)

  }

  object Utility {

    def onSendCoin(sendCoin: SendCoin): Future[Unit] = {
      val updateFromAccount = insertOrUpdateAccountBalance(sendCoin.fromAddress)
      val updateToAccount = insertOrUpdateAccountBalance(sendCoin.toAddress)
      (for {
        _ <- updateFromAccount
        _ <- updateToAccount
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def insertOrUpdateAccountBalance(address: String): Future[Unit] = {
      val accountBalanceResponse = getAccountBalance.Service.get(address)

      (for {
        accountBalanceResponse <- accountBalanceResponse
        _ <- Service.insertOrUpdate(address = address, accountBalanceResponse.result.map(_.toCoin))
      } yield ()).recover {
        case baseException: BaseException => logger.error(baseException.failure.message)
          throw baseException
      }
    }
  }

}