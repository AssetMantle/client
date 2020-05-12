package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Mobile(id: String, mobileNumber: String, status: Boolean = false, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Mobiles @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_MOBILE_NUMBER

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private val logger: Logger = Logger(this.getClass)

  private[models] val mobileTable = TableQuery[MobileTable]

  private def add(mobile: Mobile): Future[String] = db.run((mobileTable returning mobileTable.map(_.id) += mobile).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getByID(id: String): Future[Option[Mobile]] = db.run(mobileTable.filter(_.id === id).result.headOption)

  private def tryGetByID(id: String): Future[Mobile] = db.run(mobileTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def tryGetMobileNumberByIDAndStatus(id: String, status: Boolean): Future[String] = db.run(mobileTable.filter(_.id === id).filter(_.status === status).map(_.mobileNumber).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String) = db.run(mobileTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def upsert(mobile: Mobile): Future[Int] = db.run(mobileTable.insertOrUpdate(mobile).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateMobileNumberVerificationStatusOnId(id: String, verificationStatus: Boolean): Future[Int] = db.run(mobileTable.filter(_.id === id).map(_.status).update(verificationStatus).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateMobileNumberVerificationStatusOnMobileNumber(mobileNumber: String, status: Boolean): Future[Int] = db.run(mobileTable.filter(_.mobileNumber === mobileNumber).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateMobileNumberAndStatusByID(id: String, mobileNumber: String, status: Boolean): Future[Int] = db.run(mobileTable.filter(_.id === id).map(x => (x.mobileNumber, x.status)).update((mobileNumber, status)).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private[models] class MobileTable(tag: Tag) extends Table[Mobile](tag, "Mobile") {

    def * = (id, mobileNumber, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Mobile.tupled, Mobile.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def mobileNumber = column[String]("mobileNumber")

    def status = column[Boolean]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def get(id: String): Future[Option[Mobile]] = getByID(id)

    def tryGet(id: String): Future[Mobile] = tryGetByID(id)

    def create(id: String, mobileNumber: String): Future[String] = add(Mobile(id = id, mobileNumber = mobileNumber))

    def unVerifyOldMobileNumbers(mobileNumber: String): Future[Int] = updateMobileNumberVerificationStatusOnMobileNumber(mobileNumber, status = false)

    def verifyMobileNumber(id: String): Future[Int] = {
      def updateNumber(): Future[Int] = updateMobileNumberVerificationStatusOnId(id, verificationStatus = true)

      for {
        mobile <- tryGet(id)
        _ <- unVerifyOldMobileNumbers(mobile.mobileNumber)
        updateNumber <- updateNumber()
      } yield updateNumber
    }

    def tryGetVerifiedMobileNumber(id: String): Future[String] = tryGetMobileNumberByIDAndStatus(id, status = true)

    def tryGetUnverifiedMobileNumber(id: String): Future[String] = tryGetMobileNumberByIDAndStatus(id, status = false)

    def updateMobileNumber(id: String, mobileNumber: String): Future[Int] = updateMobileNumberAndStatusByID(id = id, mobileNumber = mobileNumber, status = false)
  }

}

