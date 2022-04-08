package models.master

import exceptions.BaseException
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.Lang
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import utilities.Wallet

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Account(id: String, partialMnemonic: Seq[String], encryptedPrivateKeys: Array[Byte], secretHash: String, salt: Array[Byte], iterations: Int, language: Lang, userType: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Accounts @Inject()(
                          configuration: Configuration,
                          protected val databaseConfigProvider: DatabaseConfigProvider
                        )(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_ACCOUNT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val accountTable = TableQuery[AccountTable]

  private val pepper: String = configuration.get[String]("webApp.pepper")

  case class AccountSerialized(id: String, partialMnemonic: String, encryptedPrivateKeys: Array[Byte], secretHash: String, salt: Array[Byte], iterations: Int, language: String, userType: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Account = Account(id = id, partialMnemonic = utilities.JSON.convertJsonStringToObject[Seq[String]](partialMnemonic), encryptedPrivateKeys = encryptedPrivateKeys, secretHash = secretHash, salt = salt, iterations = iterations, language = Lang(language), userType = userType, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(account: Account): AccountSerialized = AccountSerialized(id = account.id, partialMnemonic = Json.toJson(account.partialMnemonic).toString, encryptedPrivateKeys = account.encryptedPrivateKeys, secretHash = account.secretHash, salt = account.salt, iterations = account.iterations, language = account.language.toString.stripPrefix("Lang(").stripSuffix(")").trim.split("_")(0), userType = account.userType, createdBy = account.createdBy, createdOn = account.createdOn, createdOnTimeZone = account.createdOnTimeZone, updatedBy = account.updatedBy, updatedOn = account.updatedOn, updatedOnTimeZone = account.updatedOnTimeZone)

  private def add(account: Account): Future[String] = db.run((accountTable returning accountTable.map(_.userType) += serialize(account)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ACCOUNT_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(account: Account): Future[Int] = db.run(accountTable.insertOrUpdate(serialize(account)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ACCOUNT_UPSERT_FAILED, psqlException)
    }
  }

  private def tryGetById(id: String): Future[AccountSerialized] = db.run(accountTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ACCOUNT_NOT_FOUND, noSuchElementException)
    }
  }

  private def getByID(id: String): Future[Option[AccountSerialized]] = db.run(accountTable.filter(_.id === id).result.headOption)

  private def tryGetLanguageById(id: String): Future[String] = db.run(accountTable.filter(_.id === id).map(_.language).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ACCOUNT_NOT_FOUND, noSuchElementException)
    }
  }

  private def getUserTypeById(id: String): Future[String] = db.run(accountTable.filter(_.id === id).map(_.userType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ACCOUNT_NOT_FOUND, noSuchElementException)
    }
  }

  private def updateUserTypeById(id: String, userType: String): Future[Int] = db.run(accountTable.filter(_.id === id).map(_.userType).update(userType).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.ACCOUNT_NOT_FOUND)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ACCOUNT_NOT_FOUND, psqlException)
    }
  }

  private def updateWalletKeysByID(id: String, partialMnemonic: String, encryptedPrivateKeys: Array[Byte]): Future[Int] = db.run(accountTable.filter(_.id === id).map(x => (x.partialMnemonic, x.encryptedPrivateKeys)).update((partialMnemonic, encryptedPrivateKeys)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.ACCOUNT_NOT_FOUND)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ACCOUNT_NOT_FOUND, psqlException)
    }
  }

  private def updatePasswordAndPrivateKeysByID(id: String, secretHash: String, encryptedPrivateKeys: Array[Byte]): Future[Int] = db.run(accountTable.filter(_.id === id).map(x => (x.encryptedPrivateKeys, x.secretHash)).update((encryptedPrivateKeys, secretHash)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.ACCOUNT_NOT_FOUND)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ACCOUNT_NOT_FOUND, psqlException)
    }
  }

  private def checkById(id: String): Future[Boolean] = db.run(accountTable.filter(_.id === id).exists.result)

  private def deleteById(id: String) = db.run(accountTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ACCOUNT_NOT_FOUND, noSuchElementException)
    }
  }

  private[models] class AccountTable(tag: Tag) extends Table[AccountSerialized](tag, "Account") {

    def * = (id, partialMnemonic, encryptedPrivateKeys, secretHash, salt, iterations, language, userType, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (AccountSerialized.tupled, AccountSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def partialMnemonic = column[String]("partialMnemonic")

    def encryptedPrivateKeys = column[Array[Byte]]("encryptedPrivateKeys")

    def secretHash = column[String]("secretHash")

    def salt = column[Array[Byte]]("salt")

    def iterations = column[Int]("iterations")

    def language = column[String]("language")

    def userType = column[String]("userType")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(username: String, password: String, language: Lang, userType: String): Future[Wallet] = {
      val wallet = utilities.WalletGenerator.getRandomWallet
      val salt = utilities.Secrets.getNewSalt
      val account = Account(
        id = username,
        partialMnemonic = wallet.mnemonics.take(wallet.mnemonics.length - constants.Blockchain.MnemonicShown),
        encryptedPrivateKeys = utilities.Secrets.encryptData(wallet.privateKey, password),
        secretHash = utilities.Secrets.hashPassword(password = password, salt = salt, pepper = pepper.getBytes, iterations = constants.Security.DefaultIterations),
        salt = salt,
        iterations = constants.Security.DefaultIterations,
        language = language,
        userType = userType)
      for {
        _ <- add(account)
      } yield wallet
    }

    def validateUsernamePasswordAndGetAccount(username: String, password: String): Future[(Boolean, Account)] = {
      val account = tryGet(username)
      for {
        account <- account
      } yield (utilities.Secrets.verifyPassword(password = password, passwordHash = account.secretHash, salt = account.salt, pepper = pepper.getBytes, iterations = account.iterations), account)
    }


    def validateAndUpdateWithNewWallet(username: String, password: String): Future[Wallet] = {
      val account = tryGet(username)
      val wallet = utilities.WalletGenerator.getRandomWallet

      def verifyAndUpdate(account: Account) = if (utilities.Secrets.verifyPassword(password = password, passwordHash = account.secretHash, salt = account.salt, pepper = pepper.getBytes, iterations = account.iterations)) {
        updateWalletKeysByID(id = username, partialMnemonic = Json.toJson(wallet.mnemonics.take(wallet.mnemonics.length - constants.Blockchain.MnemonicShown)).toString, encryptedPrivateKeys = utilities.Secrets.encryptData(wallet.privateKey, password))
      } else Future(throw new BaseException(constants.Response.INVALID_USERNAME_OR_PASSWORD))

      for {
        account <- account
        _ <- verifyAndUpdate(account)
      } yield wallet
    }

    def validateAndUpdatePassword(username: String, oldPassword: String, newPassword: String): Future[Unit] = {
      val account = tryGet(username)

      def verifyAndUpdate(account: Account) = if (utilities.Secrets.verifyPassword(password = oldPassword, passwordHash = account.secretHash, salt = account.salt, pepper = pepper.getBytes, iterations = account.iterations)) {
        val decryptedPrivateKeys = utilities.Secrets.decryptData(account.encryptedPrivateKeys, oldPassword)
        updatePasswordAndPrivateKeysByID(id = username, secretHash = utilities.Secrets.hashPassword(password = newPassword, salt = account.salt, pepper = pepper.getBytes, iterations = constants.Security.DefaultIterations), encryptedPrivateKeys = utilities.Secrets.encryptData(decryptedPrivateKeys, newPassword))
      } else Future(throw new BaseException(constants.Response.INVALID_USERNAME_OR_PASSWORD))

      for {
        account <- account
        _ <- verifyAndUpdate(account)
      } yield ()
    }

    def updateOnForgotPassword(account: Account, newPassword: String, wallet: Wallet): Future[Int] = updatePasswordAndPrivateKeysByID(id = account.id, secretHash = utilities.Secrets.hashPassword(password = newPassword, salt = account.salt, pepper = pepper.getBytes, iterations = constants.Security.DefaultIterations), encryptedPrivateKeys = utilities.Secrets.encryptData(wallet.privateKey, newPassword))

    def checkUsernameAvailable(username: String): Future[Boolean] = checkById(username).map(!_)

    def tryGet(username: String): Future[Account] = tryGetById(username).map(_.deserialize)

    def get(username: String): Future[Option[Account]] = getByID(username).map(_.map(_.deserialize))

    def tryGetLanguage(id: String): Future[String] = tryGetLanguageById(id)

    def markUserTypeUser(id: String): Future[Int] = updateUserTypeById(id, constants.User.USER)

    def getUserType(id: String): Future[String] = getUserTypeById(id)

    def tryVerifyingUserType(id: String, userType: String): Future[Boolean] = {
      getUserTypeById(id).map { userTypeResult =>
        if (userTypeResult == userType) true else throw new BaseException(constants.Response.UNAUTHORIZED)
      }
    }

    def checkAccountExists(username: String): Future[Boolean] = checkById(username)

  }

}