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
import queries.blockchain.GetAccount
import queries.responses.blockchain.AccountResponse.{Response => AccountResponse}
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Account(address: String, username: String, coins: Seq[Coin], publicKey: String, accountNumber: String, sequence: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Accounts @Inject()(
                          protected val databaseConfigProvider: DatabaseConfigProvider,
                          getAccount: GetAccount,
                          configuration: Configuration,
                          masterAccounts: master.Accounts,
                          utilitiesOperations: utilities.Operations
                        )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ACCOUNT

  import databaseConfig.profile.api._

  private[models] val accountTable = TableQuery[AccountTable]

  case class AccountSerialized(address: String, username: String, coins: String, publicKey: String, accountNumber: String, sequence: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Account = Account(address = address, username = username, coins = utilities.JSON.convertJsonStringToObject[Seq[Coin]](coins), publicKey = publicKey, accountNumber = accountNumber, sequence = sequence, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(account: Account): AccountSerialized = AccountSerialized(address = account.address, username = account.username, coins = Json.toJson(account.coins).toString, publicKey = account.publicKey, accountNumber = account.accountNumber, sequence = account.sequence, createdBy = account.createdBy, createdOn = account.createdOn, createdOnTimeZone = account.createdOnTimeZone, updatedBy = account.updatedBy, updatedOn = account.updatedOn, updatedOnTimeZone = account.updatedOnTimeZone)

  private def add(account: Account): Future[String] = db.run((accountTable returning accountTable.map(_.address) += serialize(account)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.WALLET_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(account: Account): Future[Int] = db.run(accountTable.insertOrUpdate(serialize(account)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.WALLET_UPSERT_FAILED, psqlException)
    }
  }

  private def tryGetByAddress(address: String): Future[AccountSerialized] = db.run(accountTable.filter(_.address === address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.WALLET_NOT_FOUND, noSuchElementException)
    }
  }

  private def getByAddress(address: String): Future[Option[AccountSerialized]] = db.run(accountTable.filter(_.address === address).result.headOption)

  private def getListByAddress(addresses: Seq[String]): Future[Seq[AccountSerialized]] = db.run(accountTable.filter(_.address.inSet(addresses)).result)

  private def findByUsername(username: String): Future[AccountSerialized] = db.run(accountTable.filter(_.username === username).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findUsernameByAddress(address: String): Future[String] = db.run(accountTable.filter(_.address === address).map(_.username).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getUsernameByAddress(address: String): Future[Option[String]] = db.run(accountTable.filter(_.address === address).map(_.username).result.headOption)

  private def findAddressByID(username: String): Future[String] = db.run(accountTable.filter(_.username === username).map(_.address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByAddress(address: String): Future[Int] = db.run(accountTable.filter(_.address === address).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.WALLET_NOT_FOUND, noSuchElementException)
    }
  }

  private def checkAccountExistsByUsername(username: String): Future[Boolean] = db.run(accountTable.filter(_.username === username).exists.result)

  private[models] class AccountTable(tag: Tag) extends Table[AccountSerialized](tag, "Account_BC") {

    def * = (address, username, coins, publicKey, accountNumber, sequence, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (AccountSerialized.tupled, AccountSerialized.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def username = column[String]("username")

    def coins = column[String]("coins")

    def publicKey = column[String]("publicKey")

    def accountNumber = column[String]("accountNumber")

    def sequence = column[String]("sequence")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(address: String, username: String, publicKey: String): Future[String] = add(Account(address = address, username = username, coins = Seq(), publicKey = publicKey, accountNumber = "", sequence = ""))

    def tryGet(address: String): Future[Account] = tryGetByAddress(address).map(_.deserialize)

    def insertOrUpdate(account: Account): Future[Int] = upsert(account)

    def get(address: String): Future[Option[Account]] = getByAddress(address).map(_.map(_.deserialize))

    def getList(addresses: Seq[String]): Future[Seq[Account]] = getListByAddress(addresses).map(_.map(_.deserialize))

    def tryGetByUsername(username: String): Future[Account] = findByUsername(username).map(_.deserialize)

    def tryGetUsername(address: String): Future[String] = findUsernameByAddress(address)

    def getUsername(address: String): Future[Option[String]] = getUsernameByAddress(address)

    def tryGetAddress(username: String): Future[String] = findAddressByID(username)

    def checkAccountExists(username: String): Future[Boolean] = checkAccountExistsByUsername(username)

  }

  object Utility {

    def onSendCoin(sendCoin: SendCoin): Future[Unit] = {
      val fromAccount = insertOrUpdateAccountBalance(sendCoin.fromAddress)
      val toAccount = insertOrUpdateAccountBalance(sendCoin.toAddress)

      (for {
        fromAccount <- fromAccount
        toAccount <- toAccount
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onMultiSend(multiSend: MultiSend): Future[Unit] = {
      val inputAccounts = utilitiesOperations.traverse(multiSend.inputs)(input => insertOrUpdateAccountBalance(input.address))
      val outputAccounts = utilitiesOperations.traverse(multiSend.outputs)(output => insertOrUpdateAccountBalance(output.address))

      (for {
        inputAccounts <- inputAccounts
        outputAccounts <- outputAccounts
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    private def subtractCoinsFromAccount(fromAccount: Account, subtractCoins: Seq[Coin]) = {
      val updatedCoins = fromAccount.coins.map(accountCoin => subtractCoins.find(_.denom == accountCoin.denom).fold(accountCoin)(subtractCoin => Coin(denom = accountCoin.denom, amount = accountCoin.amount - subtractCoin.amount)))
      for {
        _ <- Service.insertOrUpdate(fromAccount.copy(coins = updatedCoins))
      } yield ()
    }

    private def addCoinsToAccount(toAddress: String, toAccount: Option[Account], addCoins: Seq[Coin]) = {
      toAccount.fold {
        val accountResponse = getAccount.Service.get(toAddress)

        def insert(accountResponse: AccountResponse) = Service.insertOrUpdate(Account(address = toAddress, username = toAddress, coins = addCoins, publicKey = accountResponse.result.value.publicKeyValue, sequence = accountResponse.result.value.sequence, accountNumber = accountResponse.result.value.accountNumber))

        for {
          accountResponse <- accountResponse
          _ <- insert(accountResponse)
        } yield ()
      } { account => {
        val updatedCoins = if (account.coins.nonEmpty) {
          val updatedAccountOldCoins = account.coins.map(accountCoin => addCoins.find(_.denom == accountCoin.denom).fold(accountCoin)(addCoin => Coin(denom = addCoin.denom, amount = accountCoin.amount + addCoin.amount)))
          updatedAccountOldCoins ++ addCoins.filter(x => !updatedAccountOldCoins.map(_.denom).contains(x.denom))
        } else addCoins
        for {
          _ <- Service.insertOrUpdate(account.copy(coins = updatedCoins))
        } yield ()
      }
      }
    }

    def insertOrUpdateAccountBalance(address: String): Future[Unit] = {
      val accountResponse = getAccount.Service.get(address)
      val bcAccount = Service.get(address)

      def upsert(accountResponse: AccountResponse, bcAccount: Option[Account]) = Service.insertOrUpdate(Account(address = address, username = bcAccount.fold(address)(_.username), coins = accountResponse.result.value.coins.map(_.toCoin), publicKey = accountResponse.result.value.publicKeyValue, sequence = accountResponse.result.value.sequence, accountNumber = accountResponse.result.value.accountNumber))

      (for {
        accountResponse <- accountResponse
        bcAccount <- bcAccount
        _ <- upsert(accountResponse, bcAccount)
      } yield ()).recover {
        case _: BaseException => Future()
      }
    }
  }

}