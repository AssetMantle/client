package models.master

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Account(id: String, secretHash: String, accountAddress: String, language: String, userType: String)

@Singleton
class Accounts @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_ACCOUNT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val accountTable = TableQuery[AccountTable]

  private def add(account: Account): Future[String] = db.run((accountTable returning accountTable.map(_.id) += account).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[Account] = db.run(accountTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByAddress(address: String): Future[Account] = db.run(accountTable.filter(_.accountAddress === address).result.head.asTry).map {
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

  private def tryGetAddressById(id: String): Future[String] = db.run(accountTable.filter(_.id === id).map(_.accountAddress).result.head.asTry).map {
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

  private def getIDsByAddresses(addresses: Seq[String]): Future[Seq[String]] = db.run(accountTable.filter(_.accountAddress.inSet(addresses)).map(_.id).result)

  private def getAddressByIds(ids: Seq[String]): Future[Seq[String]] = db.run(accountTable.filter(_.id.inSet(ids)).map(_.accountAddress).result)

  private def getUserTypeByAddress(address: String): Future[String] = db.run(accountTable.filter(_.accountAddress === address).map(_.userType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getIdByAddress(accountAddress: String): Future[String] = db.run(accountTable.filter(_.accountAddress === accountAddress).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
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

  private def updateUserTypeByAddress(address: String, userType: String): Future[Int] = db.run(accountTable.filter(_.accountAddress === address).map(_.userType).update(userType).asTry).map {
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

  private[models] class AccountTable(tag: Tag) extends Table[Account](tag, "Account") {

    def * = (id, secretHash, accountAddress, language, userType) <> (Account.tupled, Account.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def secretHash = column[String]("secretHash")

    def accountAddress = column[String]("accountAddress")

    def language = column[String]("language")

    def userType = column[String]("userType")

  }

  object Service {

    def validateUsernamePassword(username: String, password: String): Future[Boolean] = validateLoginByIDAndSecretHash(id = username, secretHash = util.hashing.MurmurHash3.stringHash(password).toString)

    def updatePassword(username: String, newPassword: String): Future[Int] = updatePasswordByID(id = username, secretHash = util.hashing.MurmurHash3.stringHash(newPassword).toString)

    def checkUsernameAvailable(username: String): Future[Boolean] = checkById(username).map {
      !_
    }

    def addLogin(username: String, password: String, accountAddress: String, language: String): Future[String] = add(Account(username, util.hashing.MurmurHash3.stringHash(password).toString, accountAddress, language, constants.User.WITHOUT_LOGIN)).map { _ => accountAddress }

    def tryGet(username: String): Future[Account] = findById(username)

    def tryGetLanguage(id: String): Future[String] = tryGetLanguageById(id)

    def getId(accountAddress: String): Future[String] = getIdByAddress(accountAddress)

    def getAccountByAddress(accountAddress: String): Future[Account] = findByAddress(accountAddress)

    def tryGetAddress(id: String): Future[String] = tryGetAddressById(id)

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

    def getUserTypeOnAddress(address: String): Future[String] = getUserTypeByAddress(address)

    def getIDsForAddresses(addresses: Seq[String]): Future[Seq[String]] = getIDsByAddresses(addresses)

    def getAddresses(ids: Seq[String]): Future[Seq[String]] = getAddressByIds(ids)

  }

}