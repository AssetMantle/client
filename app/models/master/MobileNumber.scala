package models.master

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class MobileNumber(id: String, mobileNumber: String, status: Boolean = false)

@Singleton
class MobileNumbers @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_MOBILE_NUMBER

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private val logger: Logger = Logger(this.getClass)

  private[models] val mobileNumberTable = TableQuery[MobileNumberTable]

  private def add(mobileNumber: MobileNumber): Future[String] = db.run((mobileNumberTable returning mobileNumberTable.map(_.id) += mobileNumber).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getByID(id: String): Future[Option[MobileNumber]] = db.run(mobileNumberTable.filter(_.id === id).result.headOption)

  private def tryGet(id: String): Future[MobileNumber] = db.run(mobileNumberTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def tryGetMobileNumberByIDAndStatus(id: String, status: Boolean): Future[String] = db.run(mobileNumberTable.filter(_.id === id).filter(_.status === status).map(_.mobileNumber).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAccountIDByMobileNumber(mobileNumber: String): Future[Option[String]] = db.run(mobileNumberTable.filter(_.mobileNumber === mobileNumber).map(_.id).result.headOption)

  private def deleteById(id: String) = db.run(mobileNumberTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def upsert(mobileNumber: MobileNumber): Future[Int] = db.run(mobileNumberTable.insertOrUpdate(mobileNumber).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateMobileNumberVerificationStatusOnId(id: String, verificationStatus: Boolean): Future[Int] = db.run(mobileNumberTable.filter(_.id === id).map(_.status).update(verificationStatus).asTry).map {
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

  private def updateMobileNumberAndStatusByID(id: String, mobileNumber: String, status: Boolean): Future[Int] = db.run(mobileNumberTable.filter(_.id === id).map(x => (x.mobileNumber, x.status)).update((mobileNumber, status)).asTry).map {
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

  private[models] class MobileNumberTable(tag: Tag) extends Table[MobileNumber](tag, "MobileNumber") {

    def * = (id, mobileNumber, status) <> (MobileNumber.tupled, MobileNumber.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def mobileNumber = column[String]("mobileNumber")

    def status = column[Boolean]("status")

  }

  object Service {

    def get(id: String): Future[Option[MobileNumber]] = getByID(id)

    def tryGet(id: String): Future[MobileNumber] = tryGet(id)

    def create(id: String, mobileNumber: String): Future[String] = add(MobileNumber(id = id, mobileNumber = mobileNumber))
    
    def updateMobileNumberVerificationStatus(id: String, mobileNumberVerificationStatus: Boolean): Future[Int] = updateMobileNumberVerificationStatusOnId(id, mobileNumberVerificationStatus)

    def verifyMobileNumber(id: String): Future[Int] = updateMobileNumberVerificationStatusOnId(id, verificationStatus = true)

    def tryGetVerifiedMobileNumber(id: String): Future[String] = tryGetMobileNumberByIDAndStatus(id, status = true)

    def tryGetUnverifiedMobileNumber(id: String): Future[String] = tryGetMobileNumberByIDAndStatus(id, status = false)
    
    def getMobileNumberAccount(mobileNumber: String): Future[Option[String]] = getAccountIDByMobileNumber(mobileNumber)
    
    def updateMobileNumber(id: String, mobileNumber: String): Future[Int] = updateMobileNumberAndStatusByID(id = id, mobileNumber = mobileNumber, status = false)
  }

}

