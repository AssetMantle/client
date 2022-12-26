package models.blockchain

import exceptions.BaseException
import models.Abstract.PublicKey
import models.Trait.Logging
import models.common.PublicKeys.SinglePublicKey
import models.common.Vesting.VestingParameters
import models.common.TransactionMessages.CreateVestingAccount
import models.master
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.blockchain.GetAccount
import queries.responses.blockchain.AccountResponse.{Response => AccountResponse}
import queries.responses.common.Header
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Account(address: String, username: String, accountType: Option[String], publicKey: Option[PublicKey], accountNumber: Int, sequence: Int, vestingParameters: Option[VestingParameters], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging

@Singleton
class Accounts @Inject()(
                          protected val databaseConfigProvider: DatabaseConfigProvider,
                          getAccount: GetAccount,
                          configuration: Configuration,
                          masterAccounts: master.Accounts,
                          utilitiesOperations: utilities.Operations,
                          blockchainBalances: Balances,
                        )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ACCOUNT

  import databaseConfig.profile.api._

  private[models] val accountTable = TableQuery[AccountTable]

  case class AccountSerialized(address: String, username: String, accountType: Option[String], publicKey: Option[String], accountNumber: Int, sequence: Int, vestingParameters: Option[String], createdBy: Option[String], createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) {
    def deserialize: Account = Account(address = address, username = username, accountType = accountType, publicKey = publicKey.fold[Option[PublicKey]](None)(x => Option(utilities.JSON.convertJsonStringToObject[PublicKey](x))), accountNumber = accountNumber, sequence = sequence, vestingParameters = vestingParameters.fold[Option[VestingParameters]](None)(x => Option(utilities.JSON.convertJsonStringToObject[VestingParameters](x))), createdBy = createdBy, createdOnMillisEpoch = createdOnMillisEpoch, updatedBy = updatedBy, updatedOnMillisEpoch = updatedOnMillisEpoch)
  }

  def serialize(account: Account): AccountSerialized = AccountSerialized(address = account.address, username = account.username, accountType = account.accountType, publicKey = account.publicKey.fold[Option[String]](None)(x => Option(Json.toJson(x).toString)), accountNumber = account.accountNumber, sequence = account.sequence, vestingParameters = account.vestingParameters.fold[Option[String]](None)(x => Option(Json.toJson(x).toString)), createdBy = account.createdBy, createdOnMillisEpoch = account.createdOnMillisEpoch, updatedBy = account.updatedBy, updatedOnMillisEpoch = account.updatedOnMillisEpoch)

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

    def * = (address, username, accountType.?, publicKey.?, accountNumber, sequence, vestingParameters.?, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (AccountSerialized.tupled, AccountSerialized.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def username = column[String]("username")

    def accountType = column[String]("accountType")

    def publicKey = column[String]("publicKey")

    def accountNumber = column[Int]("accountNumber")

    def sequence = column[Int]("sequence")

    def vestingParameters = column[String]("vestingParameters")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")
  }

  object Service {

    def create(address: String, username: String, accountType: String, publicKey: Option[PublicKey]): Future[String] = add(Account(address = address, username = username, accountType = Option(accountType), publicKey = publicKey, accountNumber = -1, sequence = 0, vestingParameters = None))

    def tryGet(address: String): Future[Account] = tryGetByAddress(address).map(_.deserialize)

    def insertOrUpdate(account: Account): Future[Int] = upsert(account)

    def get(address: String): Future[Option[Account]] = getByAddress(address).map(_.map(_.deserialize))

    def getList(addresses: Seq[String]): Future[Seq[Account]] = getListByAddress(addresses).map(_.map(_.deserialize))

    def tryGetByUsername(username: String): Future[Account] = findByUsername(username).map(_.deserialize)

    def tryGetUsername(address: String): Future[String] = findUsernameByAddress(address)

    def getUsername(address: String): Future[Option[String]] = getUsernameByAddress(address)

    def tryGetAddress(username: String): Future[String] = findAddressByID(username)

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

    def onKeplrSignUp(address: String, username: String, publicKey: String): Future[Unit] = {
      val optionalAccountResponse = {
        val accountResponse = getAccount.Service.get(address)
        (for {
          accountResponse <- accountResponse
        } yield Option(accountResponse)).recover {
          case baseException: BaseException => if (baseException.failure.logMessage == s"LOG.rpc error: code = NotFound desc = account ${address} not found: key not found") {
            None
          } else throw baseException
        }
      }

      def upsert(optionalAccountResponse: Option[AccountResponse]) = optionalAccountResponse.fold {
        Service.insertOrUpdate(Account(address = address, username = username, accountType = None, publicKey = Option(SinglePublicKey(publicKeyType = constants.Blockchain.PublicKey.SINGLE, key = publicKey)), accountNumber = -1, sequence = 0, vestingParameters = None))
      } { accountResponse => Service.insertOrUpdate(accountResponse.account.toSerializableAccount(username)) }

      (for {
        optionalAccountResponse <- optionalAccountResponse
        _ <- upsert(optionalAccountResponse)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }
  }
}