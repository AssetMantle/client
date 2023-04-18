package models.blockchain

import com.cosmos.vesting.v1beta1._
import exceptions.BaseException
import models.common.Serializable.Vesting.VestingParameters
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import queries.blockchain.GetAccount
import queries.responses.blockchain.AccountResponse.{Response => AccountResponse}
import queries.responses.common.Header
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Account(address: String, accountType: Option[String], accountNumber: Int, sequence: Int, vestingParameters: Option[VestingParameters], publicKey: Option[Array[Byte]], publicKeyType: Option[String])

@Singleton
class Accounts @Inject()(
                          protected val databaseConfigProvider: DatabaseConfigProvider,
                          getAccount: GetAccount,
                          utilitiesOperations: utilities.Operations,
                          blockchainBalances: Balances,
                        )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ACCOUNT

  import databaseConfig.profile.api._

  private[models] val accountTable = TableQuery[AccountTable]

  case class AccountSerialized(address: String, accountType: Option[String], accountNumber: Int, sequence: Int, vestingParameters: Option[String], publicKey: Option[Array[Byte]], publicKeyType: Option[String]) {
    def deserialize: Account = Account(address = address, accountType = accountType, accountNumber = accountNumber, sequence = sequence, vestingParameters = vestingParameters.fold[Option[VestingParameters]](None)(x => Option(utilities.JSON.convertJsonStringToObject[VestingParameters](x))), publicKey = publicKey, publicKeyType = publicKeyType)
  }

  private def add(account: Account): Future[String] = db.run((accountTable returning accountTable.map(_.address) += serialize(account)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.WALLET_INSERT_FAILED, psqlException)
    }
  }

  def serialize(account: Account): AccountSerialized = AccountSerialized(address = account.address, accountType = account.accountType, accountNumber = account.accountNumber, sequence = account.sequence, vestingParameters = account.vestingParameters.fold[Option[String]](None)(x => Option(Json.toJson(x).toString)), publicKey = account.publicKey, publicKeyType = account.publicKeyType)

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

  private def fetchAllAddresses: Future[Seq[String]] = db.run(accountTable.map(_.address).result)

  private def getByAddress(address: String): Future[Option[AccountSerialized]] = db.run(accountTable.filter(_.address === address).result.headOption)

  private[models] class AccountTable(tag: Tag) extends Table[AccountSerialized](tag, "Account") {

    def * = (address, accountType.?, accountNumber, sequence, vestingParameters.?, publicKey.?, publicKeyType.?) <> (AccountSerialized.tupled, AccountSerialized.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def accountType = column[String]("accountType")

    def accountNumber = column[Int]("accountNumber")

    def sequence = column[Int]("sequence")

    def vestingParameters = column[String]("vestingParameters")

    def publicKey = column[Array[Byte]]("publicKey")

    def publicKeyType = column[String]("publicKeyType")

  }

  object Service {
    def tryGet(address: String): Future[Account] = tryGetByAddress(address).map(_.deserialize)

    def insertOrUpdate(account: Account): Future[Int] = upsert(account)

    def get(address: String): Future[Option[Account]] = getByAddress(address).map(_.map(_.deserialize))

    def getTotalAccounts: Future[Int] = getTotalAccountNumber

    def getAllAddressess: Future[Seq[String]] = fetchAllAddresses

  }

  object Utility {

    def onCreateVestingAccount(createVestingAccount: MsgCreateVestingAccount)(implicit header: Header): Future[String] = {
      val insert = insertOrUpdateAccountWithoutAnyTx(createVestingAccount.getToAddress)
      val insertBalance = blockchainBalances.Utility.insertOrUpdateBalance(createVestingAccount.getToAddress)

      (for {
        _ <- insert
        _ <- insertBalance
      } yield createVestingAccount.getFromAddress).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.CREATE_VESTING_ACCOUNT + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          createVestingAccount.getFromAddress
      }
    }

    def insertOrUpdateAccountWithoutAnyTx(address: String): Future[Unit] = {
      val accountResponse = getAccount.Service.get(address)
      val bcAccount = Service.get(address)

      def upsert(accountResponse: AccountResponse, bcAccount: Option[Account]) = Service.insertOrUpdate(accountResponse.account.toSerializableAccount.copy(sequence = 0))

      (for {
        accountResponse <- accountResponse
        bcAccount <- bcAccount
        _ <- upsert(accountResponse, bcAccount)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def incrementSequence(address: String): Future[Unit] = {
      val bcAccount = Service.get(address)

      def getUpdatedAccount(bcAccount: Option[Account]): Future[Account] = bcAccount.fold {
        val accountResponse = getAccount.Service.get(address)
        for {
          accountResponse <- accountResponse
        } yield accountResponse.account.toSerializableAccount.copy(sequence = 1)
      }(account => {
        if (account.accountNumber == -1) {
          val accountResponse = getAccount.Service.get(address)
          for {
            accountResponse <- accountResponse
          } yield accountResponse.account.toSerializableAccount
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
  }
}