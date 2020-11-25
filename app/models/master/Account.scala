package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.Lang
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Account(id: String, secretHash: Option[String], language: Option[Lang], userType: String, partialMnemonic: Option[Seq[String]], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Accounts @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration, utilitiesLog: utilities.Log)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_ACCOUNT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val accountTable = TableQuery[AccountTable]

  case class AccountSerialized(id: String, secretHash: Option[String], language: Option[String], userType: String, partialMnemonic: Option[String], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Account = Account(id = id, secretHash = secretHash, language = language.fold[Option[Lang]](None)(x => Option(Lang(x))), userType = userType, partialMnemonic = partialMnemonic.fold[Option[Seq[String]]](None)(x => Option(utilities.JSON.convertJsonStringToObject[Seq[String]](x))), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(account: Account): AccountSerialized = AccountSerialized(id = account.id, secretHash = account.secretHash, language = account.language.fold[Option[String]](None)(x => Option(x.toString.stripPrefix("Lang(").stripSuffix(")").trim.split("_")(0))), userType = account.userType, partialMnemonic = account.partialMnemonic.fold[Option[String]](None)(x => Option(Json.toJson(x).toString)), createdBy = account.createdBy, createdOn = account.createdOn, createdOnTimeZone = account.createdOnTimeZone, updatedBy = account.updatedBy, updatedOn = account.updatedOn, updatedOnTimeZone = account.updatedOnTimeZone)

  private def add(account: Account): Future[String] = db.run((accountTable returning accountTable.map(_.id) += serialize(account)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def tryGetById(id: String): Future[AccountSerialized] = db.run(accountTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getByID(id: String): Future[Option[AccountSerialized]] = db.run(accountTable.filter(_.id === id).result.headOption)

  private def tryGetLanguageById(id: String): Future[String] = db.run(accountTable.filter(_.id === id).map(_.language).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def tryGetPartialMnemonicById(id: String): Future[String] = db.run(accountTable.filter(_.id === id).map(_.partialMnemonic).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getUserTypeById(id: String): Future[String] = db.run(accountTable.filter(_.id === id).map(_.userType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def validateLoginByIDAndSecretHash(id: String, secretHash: String): Future[Boolean] = db.run(accountTable.filter(_.id === id).filter(_.secretHash === secretHash).exists.result)

  private def updatePartialMnemonicById(id: String, partialMnemonic: String): Future[Int] = db.run(accountTable.filter(_.id === id).map(_.partialMnemonic).update(partialMnemonic).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateUserTypeById(id: String, userType: String): Future[Int] = db.run(accountTable.filter(_.id === id).map(_.userType).update(userType).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updatePasswordByID(id: String, secretHash: String): Future[Int] = db.run(accountTable.filter(_.id === id).map(_.secretHash).update(secretHash).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def checkById(id: String): Future[Boolean] = db.run(accountTable.filter(_.id === id).exists.result)

  private def deleteById(id: String) = db.run(accountTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class AccountTable(tag: Tag) extends Table[AccountSerialized](tag, "Account") {

    def * = (id, secretHash.?, language.?, userType, partialMnemonic.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (AccountSerialized.tupled, AccountSerialized.unapply)

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

    def addLogin(username: String, password: String, language: Lang, mnemonics: Seq[String]): Future[String] = add(Account(id = username, secretHash = Option(util.hashing.MurmurHash3.stringHash(password).toString), partialMnemonic = Option(mnemonics), language = Option(language), userType = constants.User.WITHOUT_LOGIN))

    def addWithoutSignUp(username: String): Future[String] = add(Account(id = username, secretHash = None, partialMnemonic = None, language = None, userType = constants.User.WITHOUT_LOGIN))

    def tryGet(username: String): Future[Account] = tryGetById(username).map(_.deserialize)

    def get(username: String): Future[Option[Account]] = getByID(username).map(_.map(_.deserialize))

    def tryGetLanguage(id: String): Future[String] = tryGetLanguageById(id)

    def tryGetPartialMnemonic(id: String): Future[Seq[String]] = tryGetPartialMnemonicById(id).map(x => utilities.JSON.convertJsonStringToObject[Seq[String]](x))

    def updatePartialMnemonic(id: String, partialMnemonic: Seq[String]): Future[Int] = updatePartialMnemonicById(id = id, partialMnemonic = Json.toJson(partialMnemonic).toString)

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