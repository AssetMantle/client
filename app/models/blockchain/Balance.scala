package models.blockchain

import exceptions.BaseException
import models.Trait.Logged
import models.common.Serializable.Coin
import models.common.TransactionMessages.{MultiSend, SendCoin}
import models.master
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.blockchain.GetBalance
import queries.responses.blockchain.BalanceResponse.{Response => BalanceResponse}
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Balance(address: String, coins: Seq[Coin], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Balances @Inject()(
                          protected val databaseConfigProvider: DatabaseConfigProvider,
                          getBalance: GetBalance,
                          configuration: Configuration,
                          masterAccounts: master.Accounts,
                          utilitiesOperations: utilities.Operations
                        )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_BALANCE

  import databaseConfig.profile.api._

  private[models] val balanceTable = TableQuery[BalanceTable]

  case class BalanceSerialized(address: String, coins: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Balance = Balance(address = address, coins = utilities.JSON.convertJsonStringToObject[Seq[Coin]](coins), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(balance: Balance): BalanceSerialized = BalanceSerialized(address = balance.address, coins = Json.toJson(balance.coins).toString, createdBy = balance.createdBy, createdOn = balance.createdOn, createdOnTimeZone = balance.createdOnTimeZone, updatedBy = balance.updatedBy, updatedOn = balance.updatedOn, updatedOnTimeZone = balance.updatedOnTimeZone)

  private def add(balance: Balance): Future[String] = db.run((balanceTable returning balanceTable.map(_.address) += serialize(balance)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.WALLET_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(balance: Balance): Future[Int] = db.run(balanceTable.insertOrUpdate(serialize(balance)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.WALLET_UPSERT_FAILED, psqlException)
    }
  }

  private def tryGetByAddress(address: String): Future[BalanceSerialized] = db.run(balanceTable.filter(_.address === address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.WALLET_NOT_FOUND, noSuchElementException)
    }
  }

  private def getByAddress(address: String): Future[Option[BalanceSerialized]] = db.run(balanceTable.filter(_.address === address).result.headOption)

  private def getListByAddress(addresses: Seq[String]): Future[Seq[BalanceSerialized]] = db.run(balanceTable.filter(_.address.inSet(addresses)).result)

  private[models] class BalanceTable(tag: Tag) extends Table[BalanceSerialized](tag, "Balance_BC") {

    def * = (address, coins, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (BalanceSerialized.tupled, BalanceSerialized.unapply)

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

    def create(address: String, coins: Seq[Coin]): Future[String] = add(Balance(address = address, coins = coins))

    def tryGet(address: String): Future[Balance] = tryGetByAddress(address).map(_.deserialize)

    def insertOrUpdate(balance: Balance): Future[Int] = upsert(balance)

    def get(address: String): Future[Option[Balance]] = getByAddress(address).map(_.map(_.deserialize))

    def getList(addresses: Seq[String]): Future[Seq[Balance]] = getListByAddress(addresses).map(_.map(_.deserialize))

  }

  object Utility {

    def onSendCoin(sendCoin: SendCoin): Future[Unit] = {
      val fromAccount = subtractCoinsFromAccount(sendCoin.fromAddress, sendCoin.amounts)
      val toAccount = addCoinsToAccount(sendCoin.toAddress, sendCoin.amounts)

      (for {
        _ <- fromAccount
        _ <- toAccount
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onMultiSend(multiSend: MultiSend): Future[Unit] = {
      val inputAccounts = utilitiesOperations.traverse(multiSend.inputs)(input => subtractCoinsFromAccount(input.address, input.coins))
      val outputAccounts = utilitiesOperations.traverse(multiSend.outputs)(output => addCoinsToAccount(output.address, output.coins))

      (for {
        _ <- inputAccounts
        _ <- outputAccounts
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def subtractCoinsFromAccount(fromAddress: String, subtractCoins: Seq[Coin]): Future[Unit] = {
      val fromBalance = Service.tryGet(fromAddress)

      def updatedCoins(fromBalance: Balance) = fromBalance.coins.map(oldCoin => subtractCoins.find(_.denom == oldCoin.denom).fold(oldCoin)(subtractCoin => Coin(denom = oldCoin.denom, amount = oldCoin.amount - subtractCoin.amount)))

      for {
        fromBalance <- fromBalance
        _ <- Service.insertOrUpdate(Balance(address = fromAddress, coins = updatedCoins(fromBalance)))
      } yield ()
    }

    def addCoinsToAccount(toAddress: String, addCoins: Seq[Coin]): Future[Unit] = {
      val oldBalance = Service.get(toAddress)

      def upsert(oldBalance: Option[Balance]) = oldBalance.fold(Service.insertOrUpdate(Balance(address = toAddress, coins = addCoins)))(old => {
        val updatedCoins = if (old.coins.nonEmpty) {
          val newCoins = old.coins.map(oldCoin => addCoins.find(_.denom == oldCoin.denom).fold(oldCoin)(addCoin => Coin(denom = addCoin.denom, amount = oldCoin.amount + addCoin.amount)))
          newCoins ++ addCoins.filter(x => !newCoins.map(_.denom).contains(x.denom))
        } else addCoins
        Service.insertOrUpdate(Balance(address = toAddress, coins = updatedCoins))
      })

      (for {
        oldBalance <- oldBalance
        _ <- upsert(oldBalance)
      } yield ()).recover {
        case _: BaseException => Future()
      }
    }

    def insertOrUpdateBalance(address: String): Future[Unit] = {
      val balanceResponse = getBalance.Service.get(address)

      def upsert(balanceResponse: BalanceResponse) = Service.insertOrUpdate(Balance(address = address, coins = balanceResponse.balances.map(_.toCoin)))

      (for {
        balanceResponse <- balanceResponse
        _ <- upsert(balanceResponse)
      } yield ()).recover {
        case _: BaseException => Future()
      }
    }
  }

}