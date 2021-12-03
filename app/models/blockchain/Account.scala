package models.blockchain

import models.blockchain.Accounts.{CheckAccountExists, CreateAccount, GetAccountWithActor, GetListAccount, GetUsernameAccount, InsertOrUpdateAccount, TryGetAccount, TryGetAddressAccount, TryGetByUsernameAccount, TryGetUsernameAccount}
import akka.actor.{Actor, ActorLogging, Props}
import exceptions.BaseException
import models.Abstract.{PublicKey, ShardedActorRegion}
import models.Trait.Logged
import models.common.Serializable.Vesting.VestingParameters
import models.master
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.blockchain.GetAccount
import queries.responses.blockchain.AccountResponse.{Response => AccountResponse}
import slick.jdbc.JdbcProfile
import models.common.PublicKeys._
import models.common.TransactionMessages.CreateVestingAccount
import queries.responses.common.Header

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import akka.cluster.sharding.ShardRegion.{ExtractEntityId, ExtractShardId}
import constants.Actor.{NUMBER_OF_ENTITIES, NUMBER_OF_SHARDS}

import java.util.UUID

case class Account(address: String, username: String, accountType: String, publicKey: Option[PublicKey], accountNumber: Int, sequence: Int, vestingParameters: Option[VestingParameters], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Accounts @Inject()(
                          protected val databaseConfigProvider: DatabaseConfigProvider,
                          getAccount: GetAccount,
                          configuration: Configuration,
                          masterAccounts: master.Accounts,
                          utilitiesOperations: utilities.Operations,
                          blockchainBalances: Balances,
                        )(implicit executionContext: ExecutionContext) extends ShardedActorRegion {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ACCOUNT

  private val uniqueId: String = UUID.randomUUID().toString

  import databaseConfig.profile.api._

  private[models] val accountTable = TableQuery[AccountTable]

  private implicit val timeout = Timeout(constants.Actor.ACTOR_ASK_TIMEOUT)

  override def idExtractor: ExtractEntityId = {
    case attempt@GetAccountWithActor(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetAccount(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@CreateAccount(id, _, _, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateAccount(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetListAccount(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetByUsernameAccount(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetUsernameAccount(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetUsernameAccount(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetAddressAccount(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@CheckAccountExists(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  override def shardResolver: ExtractShardId = {
    case GetAccountWithActor(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetAccount(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case CreateAccount(id, _, _, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateAccount(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetListAccount(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetByUsernameAccount(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetUsernameAccount(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetUsernameAccount(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetAddressAccount(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case CheckAccountExists(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }

  override def regionName: String = "accountRegion"

  override val props: Props = Accounts.props(Accounts.this)

  case class AccountSerialized(address: String, username: String, accountType: String, publicKey: Option[String], accountNumber: Int, sequence: Int, vestingParameters: Option[String], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Account = Account(address = address, username = username, accountType = accountType, publicKey = publicKey.fold[Option[PublicKey]](None)(x => Option(utilities.JSON.convertJsonStringToObject[PublicKey](x))), accountNumber = accountNumber, sequence = sequence, vestingParameters = vestingParameters.fold[Option[VestingParameters]](None)(x => Option(utilities.JSON.convertJsonStringToObject[VestingParameters](x))), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(account: Account): AccountSerialized = AccountSerialized(address = account.address, username = account.username, accountType = account.accountType, publicKey = account.publicKey.fold[Option[String]](None)(x => Option(Json.toJson(x).toString)), accountNumber = account.accountNumber, sequence = account.sequence, vestingParameters = account.vestingParameters.fold[Option[String]](None)(x => Option(Json.toJson(x).toString)), createdBy = account.createdBy, createdOn = account.createdOn, createdOnTimeZone = account.createdOnTimeZone, updatedBy = account.updatedBy, updatedOn = account.updatedOn, updatedOnTimeZone = account.updatedOnTimeZone)

  private def add( account: Account): Future[String] = db.run((accountTable returning accountTable.map(_.address) += serialize(account)).asTry).map {
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

  private def getTotalAccountNumber: Future[Int] = db.run(accountTable.length.result)

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

    def * = (address, username, accountType, publicKey.?, accountNumber, sequence, vestingParameters.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (AccountSerialized.tupled, AccountSerialized.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def username = column[String]("username")

    def accountType = column[String]("accountType")

    def publicKey = column[String]("publicKey")

    def accountNumber = column[Int]("accountNumber")

    def sequence = column[Int]("sequence")

    def vestingParameters = column[String]("vestingParameters")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def createWithAccountActor(address: String, username: String, accountType: String, publicKey: Option[PublicKey]): Future[String] = (actorRegion ? CreateAccount(uniqueId, address, username, accountType, publicKey)).mapTo[String]

    def create(address: String, username: String, accountType: String, publicKey: Option[PublicKey]): Future[String] = add(Account(address = address, username = username, accountType = accountType, publicKey = publicKey, accountNumber = -1, sequence = 0, vestingParameters = None))

    def tryGetWithAccountActor(address: String): Future[Account] = (actorRegion ? TryGetAccount(uniqueId, address)).mapTo[Account]

    def tryGet(address: String): Future[Account] = tryGetByAddress(address).map(_.deserialize)

    def insertOrUpdateWithAccountActor(account: Account): Future[Int] = (actorRegion ? InsertOrUpdateAccount(uniqueId, account)).mapTo[Int]

    def insertOrUpdate(account: Account): Future[Int] = upsert(account)

    def getWithAccountActor(address: String): Future[Option[Account]] = (actorRegion ? GetAccountWithActor(uniqueId, address)).mapTo[Option[Account]]

    def get(address: String): Future[Option[Account]] = getByAddress(address).map(_.map(_.deserialize))

    def getListWithAccountActor(addresses: Seq[String]): Future[Seq[Account]] = (actorRegion ? GetListAccount(uniqueId, addresses)).mapTo[Seq[Account]]

    def getList(addresses: Seq[String]): Future[Seq[Account]] = getListByAddress(addresses).map(_.map(_.deserialize))

    def tryGetByUsernameWithAccountActor(username: String): Future[Account] = (actorRegion ? TryGetByUsernameAccount(uniqueId, username)).mapTo[Account]
1
    def tryGetByUsername(username: String): Future[Account] = findByUsername(username).map(_.deserialize)

    def tryGetUsernameWithAccountActor(address: String): Future[String] = (actorRegion ? TryGetUsernameAccount(uniqueId, address)).mapTo[String]

    def tryGetUsername(address: String): Future[String] = findUsernameByAddress(address)

    def getUsernameWithAccountActor(address: String): Future[Option[String]] = (actorRegion ? GetUsernameAccount(uniqueId, address)).mapTo[Option[String]]

    def getUsername(address: String): Future[Option[String]] = getUsernameByAddress(address)

    def tryGetAddressWithAccountActor(username: String): Future[String] = (actorRegion ? TryGetAddressAccount(uniqueId, username)).mapTo[String]

    def tryGetAddress(username: String): Future[String] = findAddressByID(username)

    def checkAccountExistsWithAccountActor(username: String): Future[Boolean] = (actorRegion ? CheckAccountExists(uniqueId, username)).mapTo[Boolean]

    def checkAccountExists(username: String): Future[Boolean] = checkAccountExistsByUsername(username)

    def getTotalAccounts: Future[Int] = getTotalAccountNumber

  }

  object Utility {

    def onCreateVestingAccount(createVestingAccount: CreateVestingAccount)(implicit header: Header): Future[Unit] = {
      val insert = insertOrUpdateAccountWithoutAnyTx(createVestingAccount.toAddress)
      val insertBalance = blockchainBalances.Utility.insertOrUpdateBalance(createVestingAccount.toAddress)

      (for {
        _ <- insert
        _ <- insertBalance
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.CREATE_VESTING_ACCOUNT + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def incrementSequence(address: String): Future[Unit] = {
      val bcAccount = Service.get(address)

      def getUpdatedAccount(bcAccount: Option[Account]): Future[Account] = bcAccount.fold {
        val accountResponse = getAccount.Service.get(address)
        for {
          accountResponse <- accountResponse
        } yield accountResponse.account.toSerializableAccount(address).copy(sequence = 1)
      }(account => {
        if (account.accountNumber == -1) {
          val accountResponse = getAccount.Service.get(address)
          for {
            accountResponse <- accountResponse
          } yield accountResponse.account.toSerializableAccount(account.username)
        } else Future(account.copy(sequence = account.sequence + 1))
      })

      def update(account: Account) = Service.insertOrUpdate(account)

      (for {
        bcAccount <- bcAccount
        updatedAccount <- getUpdatedAccount(bcAccount)
        _ <- update(updatedAccount)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def insertOrUpdateAccountWithoutAnyTx(address: String): Future[Unit] = {
      val accountResponse = getAccount.Service.get(address)
      val bcAccount = Service.get(address)

      def upsert(accountResponse: AccountResponse, bcAccount: Option[Account]) = Service.insertOrUpdate(accountResponse.account.toSerializableAccount(bcAccount.fold(address)(_.username)).copy(sequence = 0))

      (for {
        accountResponse <- accountResponse
        bcAccount <- bcAccount
        _ <- upsert(accountResponse, bcAccount)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }
  }


}

object Accounts {
  def props(blockchainAccounts: models.blockchain.Accounts) (implicit executionContext: ExecutionContext) = Props(new AccountActor(blockchainAccounts))

  @Singleton
  class AccountActor @Inject()(
                                blockchainAccounts: models.blockchain.Accounts
                              ) (implicit executionContext: ExecutionContext) extends Actor with ActorLogging {
    private implicit val logger: Logger = Logger(this.getClass)

    override def receive: Receive = {
      case GetAccountWithActor(_, address) => {
        blockchainAccounts.Service.get(address) pipeTo sender()
      }
      case CreateAccount(_, address, username, accountType, publicKey) => {
        blockchainAccounts.Service.create(address, username, accountType, publicKey) pipeTo sender()
      }
      case TryGetAccount(_, address) => {
        blockchainAccounts.Service.tryGet(address) pipeTo sender()
      }
      case InsertOrUpdateAccount(_, account) => {
        blockchainAccounts.Service.insertOrUpdate(account) pipeTo sender()
      }
      case GetListAccount(_, addresses) => {
        blockchainAccounts.Service.getList(addresses) pipeTo sender()
      }
      case TryGetByUsernameAccount(_, username) => {
        blockchainAccounts.Service.tryGetByUsername(username) pipeTo sender()
      }
      case TryGetUsernameAccount(_, username) => {
        blockchainAccounts.Service.tryGetUsername(username) pipeTo sender()
      }
      case GetUsernameAccount(_, address) => {
        blockchainAccounts.Service.getUsername(address) pipeTo sender()
      }
      case TryGetAddressAccount(_, address) => {
        blockchainAccounts.Service.tryGetAddress(address) pipeTo sender()
      }
      case CheckAccountExists(_, address) => {
        blockchainAccounts.Service.checkAccountExists(address) pipeTo sender()
      }
    }
  }

  case class GetAccountWithActor(id:String, address: String)
  case class TryGetAccount(id:String, address: String)
  case class CreateAccount(id:String, address: String, username: String, accountType: String, publicKey: Option[PublicKey])
  case class InsertOrUpdateAccount(id:String, account: Account)
  case class GetListAccount(id:String, addresses: Seq[String])
  case class TryGetByUsernameAccount(id:String, username: String)
  case class TryGetUsernameAccount(id:String, address: String)
  case class GetUsernameAccount(id:String, address: String)
  case class TryGetAddressAccount(id:String, username: String)
  case class CheckAccountExists(id:String, username: String)
}