package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Node
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.Lang
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Account(id: String, secretHash: String, language: Lang, userType: String, partialMnemonic: Seq[String], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) {
  def createLog()(implicit node: Node): Account = copy(createdBy = Option(node.id), createdOn = Option(new Timestamp(System.currentTimeMillis())), createdOnTimeZone = Option(node.timeZone))

  def updateLog()(implicit node: Node): Account = copy(updatedBy = Option(node.id), updatedOn = Option(new Timestamp(System.currentTimeMillis())), updatedOnTimeZone = Option(node.timeZone))

}

@Singleton
class Accounts @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_ACCOUNT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private implicit val node: Node = Node(id = configuration.get[String]("node.id"), timeZone = configuration.get[String]("node.timeZone"))

  private[models] val accountTable = TableQuery[AccountTable]

  case class AccountSerialized(id: String, secretHash: String, language: String, userType: String, partialMnemonic: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Account = Account(id = id, secretHash = secretHash, language = Lang(language), userType = userType, partialMnemonic = utilities.JSON.convertJsonStringToObject[Seq[String]](partialMnemonic), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(account: Account): AccountSerialized = AccountSerialized(id = account.id, secretHash = account.secretHash, language = account.language.toString.stripPrefix("Lang(").stripSuffix(")").trim.split("_")(0), userType = account.userType, partialMnemonic = Json.toJson(account.partialMnemonic).toString, createdBy = account.createdBy, createdOn = account.createdOn, createdOnTimeZone = account.createdOnTimeZone, updatedBy = account.updatedBy, updatedOn = account.updatedOn, updatedOnTimeZone = account.updatedOnTimeZone)

  private def add(account: Account): Future[String] = db.run((accountTable returning accountTable.map(_.id) += serialize(account.createLog())).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[AccountSerialized] = db.run(accountTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }
  
  private def tryGetLanguageById(id: String): Future[String] = db.run(accountTable.filter(_.id === id).map(_.language).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def tryGetPartialMnemonicById(id: String): Future[String] = db.run(accountTable.filter(_.id === id).map(_.partialMnemonic).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getUserTypeById(id: String): Future[String] = db.run(accountTable.filter(_.id === id).map(_.userType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def validateLoginByIDAndSecretHash(id: String, secretHash: String): Future[Boolean] = db.run(accountTable.filter(_.id === id).filter(_.secretHash === secretHash).exists.result)

  private def updatePartialMnemonicById(id: String, partialMnemonic: String): Future[Int] = db.run(accountTable.filter(_.id === id).map(_.partialMnemonic).update(partialMnemonic).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }


  private def updateUserTypeById(id: String, userType: String): Future[Int] = db.run(accountTable.filter(_.id === id).map(_.userType).update(userType).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updatePasswordByID(id: String, secretHash: String): Future[Int] = db.run(accountTable.filter(_.id === id).map(_.secretHash).update(secretHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def checkById(id: String): Future[Boolean] = db.run(accountTable.filter(_.id === id).exists.result)

  private def deleteById(id: String) = db.run(accountTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class AccountTable(tag: Tag) extends Table[AccountSerialized](tag, "Account") {

    def * = (id, secretHash, language, userType, partialMnemonic, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (AccountSerialized.tupled, AccountSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def secretHash = column[String]("secretHash")

    def language = column[String]("language")

    def userType = column[String]("userType")

    def partialMnemonic = column[String]("partialMnemonic")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def validateUsernamePassword(username: String, password: String): Future[Boolean] = validateLoginByIDAndSecretHash(id = username, secretHash = util.hashing.MurmurHash3.stringHash(password).toString)

    def updatePassword(username: String, newPassword: String): Future[Int] = updatePasswordByID(id = username, secretHash = util.hashing.MurmurHash3.stringHash(newPassword).toString)

    def checkUsernameAvailable(username: String): Future[Boolean] = checkById(username).map(!_)

    def addLogin(username: String, password: String, language: Lang, mnemonics: Seq[String]): Future[String] = add(Account(id = username, secretHash = util.hashing.MurmurHash3.stringHash(password).toString, partialMnemonic = mnemonics, language = language, userType = constants.User.WITHOUT_LOGIN))

    def tryGet(username: String): Future[Account] = findById(username).map(_.deserialize)

    def tryGetLanguage(id: String): Future[String] = tryGetLanguageById(id)

    def tryGetPartialMnemonic(id: String): Future[Seq[String]] = tryGetPartialMnemonicById(id).map(x => utilities.JSON.convertJsonStringToObject[Seq[String]](x))

    def updatePartialMnemonic(id: String, partialMnemonic: Seq[String]): Future[Int] = updatePartialMnemonicById(id = id, partialMnemonic = Json.toJson(partialMnemonic).toString)

    def markUserTypeTrader(id: String): Future[Int] = updateUserTypeById(id, constants.User.TRADER)

    def markUserTypeZone(id: String): Future[Int] = updateUserTypeById(id, constants.User.ZONE)

    def markUserTypeOrganization(id: String): Future[Int] = updateUserTypeById(id, constants.User.ORGANIZATION)

    def markUserTypeUser(id: String): Future[Int] = updateUserTypeById(id, constants.User.USER)

    def getUserType(id: String): Future[String] = getUserTypeById(id)

    def tryVerifyingUserType(id: String, userType: String): Future[Boolean] = {
      getUserTypeById(id).map { userTypeResult =>
        if (userTypeResult == userType) true else throw new BaseException(constants.Response.UNAUTHORIZED)
      }
    }

  }

}