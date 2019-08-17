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

case class Account(id: String, secretHash: String, accountAddress: String, language: String, userType: String, status: String)

@Singleton
class Accounts @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_ACCOUNT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val accountTable = TableQuery[AccountTable]

  private def add(account: Account)(implicit executionContext: ExecutionContext): Future[String] = db.run((accountTable returning accountTable.map(_.id) += account).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String)(implicit executionContext: ExecutionContext): Future[Account] = db.run(accountTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByAddress(address: String)(implicit executionContext: ExecutionContext): Future[Account] = db.run(accountTable.filter(_.accountAddress === address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }


  private def getLanguageById(id: String)(implicit executionContext: ExecutionContext): Future[String] = db.run(accountTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result.language
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAddressById(id: String)(implicit executionContext: ExecutionContext): Future[String] = db.run(accountTable.filter(_.id === id).map(_.accountAddress).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getStatusByIDAndSecretHash(id: String, secretHash: String)(implicit executionContext: ExecutionContext): Future[String] = db.run(accountTable.filter(_.id === id).filter(_.secretHash === secretHash).map(_.status).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getSecretHashById(id: String)(implicit executionContext: ExecutionContext): Future[String] = db.run(accountTable.filter(_.id === id).map(_.secretHash).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getUserTypeById(id: String): Future[String] = db.run(accountTable.filter(_.id === id).map(_.userType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getIDsByAddresses(addresses: Seq[String]): Future[Seq[String]] = db.run(accountTable.filter(_.accountAddress.inSet(addresses)).map(_.id).result)

  private def getAddressByIds(ids: Seq[String]): Future[Seq[String]] = db.run(accountTable.filter(_.id.inSet(ids)).map(_.accountAddress).result)

  private def filterIdsOnUserType(ids: Seq[String], userType: String): Future[Seq[String]] = db.run(accountTable.filter(_.id.inSet(ids)).filter(_.userType === userType).map(_.id).result)

  private def updateStatusById(id: String, status: String): Future[Int] = db.run(accountTable.filter(_.id === id).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getUserTypeByAddress(address: String)(implicit executionContext: ExecutionContext): Future[String] = db.run(accountTable.filter(_.accountAddress === address).map(_.userType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getIdByAddress(accountAddress: String)(implicit executionContext: ExecutionContext): Future[String] = db.run(accountTable.filter(_.accountAddress === accountAddress).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateUserTypeById(id: String, userType: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(accountTable.filter(_.id === id).map(_.userType).update(userType).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateUserTypeByAddress(address: String, userType: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(accountTable.filter(_.accountAddress === address).map(_.userType).update(userType).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def checkById(id: String): Future[Boolean] = db.run(accountTable.filter(_.id === id).exists.result)

  private def deleteById(id: String)(implicit executionContext: ExecutionContext) = db.run(accountTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class AccountTable(tag: Tag) extends Table[Account](tag, "Account") {

    def * = (id, secretHash, accountAddress, language, userType, status) <> (Account.tupled, Account.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def secretHash = column[String]("secretHash")

    def accountAddress = column[String]("accountAddress")

    def language = column[String]("language")

    def userType = column[String]("userType")

    def status = column[String]("status")

  }

  object Service {

    def validateLoginAndGetStatus(username: String, password: String)(implicit executionContext: ExecutionContext): String = Await.result(getStatusByIDAndSecretHash(username, util.hashing.MurmurHash3.stringHash(password).toString), Duration.Inf)

    def checkUsernameAvailable(username: String): Boolean = !Await.result(checkById(username), Duration.Inf)

    def addLogin(username: String, password: String, accountAddress: String, language: String)(implicit executionContext: ExecutionContext): String = {
      Await.result(add(Account(username, util.hashing.MurmurHash3.stringHash(password).toString, accountAddress, language, constants.User.WITHOUT_LOGIN, constants.Status.Account.NO_CONTACT)), Duration.Inf)
      accountAddress
    }

    def getAccount(username: String)(implicit executionContext: ExecutionContext): Account = Await.result(findById(username), Duration.Inf)

    def getLanguage(id: String)(implicit executionContext: ExecutionContext): String = Await.result(getLanguageById(id), Duration.Inf)

    def getId(accountAddress: String)(implicit executionContext: ExecutionContext): String = Await.result(getIdByAddress(accountAddress), Duration.Inf)

    def getAccountByAddress(accountAddress: String)(implicit executionContext: ExecutionContext): Account = Await.result(findByAddress(accountAddress), Duration.Inf)

    def getAddress(id: String)(implicit executionContext: ExecutionContext): String = Await.result(getAddressById(id), Duration.Inf)

    def updateUserType(id: String, userType: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateUserTypeById(id, userType), Duration.Inf)

    def updateUserTypeOnAddress(address: String, userType: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateUserTypeByAddress(address, userType), Duration.Inf)

    def getUserType(id: String)(implicit executionContext: ExecutionContext): String = Await.result(getUserTypeById(id), Duration.Inf)

    def tryVerifyingUserType(id: String, userType: String)(implicit executionContext: ExecutionContext): Boolean = {
      if (Await.result(getUserTypeById(id), Duration.Inf) == userType) true
      else throw new BaseException(constants.Response.UNAUTHORIZED)
    }

    def getUserTypeOnAddress(address: String)(implicit executionContext: ExecutionContext): String = Await.result(getUserTypeByAddress(address), Duration.Inf)

    def getIDsForAddresses(addresses: Seq[String]): Seq[String] = Await.result(getIDsByAddresses(addresses), Duration.Inf)

    def getAddresses(ids: Seq[String]): Seq[String] = Await.result(getAddressByIds(ids), Duration.Inf)

    def filterTraderIds(ids: Seq[String]): Seq[String] = Await.result(filterIdsOnUserType(ids, constants.User.TRADER), Duration.Inf)

    def updateStatusUnverifiedContact(id: String): Int = Await.result(updateStatusById(id, constants.Status.Account.CONTACT_UNVERIFIED), Duration.Inf)

    def updateStatusUnverifiedMobile(id: String): Int = Await.result(updateStatusById(id, constants.Status.Account.MOBILE_NUMBER_UNVERIFIED), Duration.Inf)

    def updateStatusUnverifiedEmail(id: String): Int = Await.result(updateStatusById(id, constants.Status.Account.EMAIL_ADDRESS_UNVERIFIED), Duration.Inf)

    def updateStatusComplete(id: String): Int = Await.result(updateStatusById(id, constants.Status.Account.COMPLETE), Duration.Inf)
  }

}