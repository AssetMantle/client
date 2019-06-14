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

case class AccountKYC(id: String, documentType: String, status: Option[Boolean], fileName: String, file: Option[Array[Byte]])

@Singleton
class AccountKYCs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ACCOUNT_KYC

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val accountKYCTable = TableQuery[AccountKYCTable]

  private def add(accountKYC: AccountKYC): Future[String] = db.run((accountKYCTable returning accountKYCTable.map(_.id) += accountKYC).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(accountKYC: AccountKYC): Future[Int] = db.run(accountKYCTable.insertOrUpdate(accountKYC).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e:Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message,e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def findByIdDocumentType(id: String, documentType: String): Future[AccountKYC] = db.run(accountKYCTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusById(id: String, status: Option[Boolean]): Future[Int] = db.run(accountKYCTable.filter(_.id === id).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusByIdAndDocumentType(id: String, documentType: String, status: Option[Boolean]): Future[Int] = db.run(accountKYCTable.filter(_.id === id).filter(_.documentType === documentType).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAllDocumentsById(id: String): Future[Seq[AccountKYC]] = db.run(accountKYCTable.filter(_.id === id).result)

  private def deleteById(id: String) = db.run(accountKYCTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }


  private[models] class AccountKYCTable(tag: Tag) extends Table[AccountKYC](tag, "AccountKYC") {

    def * = (id, documentType, status.?, fileName, file.?) <> (AccountKYC.tupled, AccountKYC.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def status = column[Boolean]("status")

    def fileName = column[String]("fileName")

    def file = column[Array[Byte]]("file")

  }

  object Service {

    def create(id: String, documentType: String, fileName: String, file: Option[Array[Byte]]): String = Await.result(add(AccountKYC(id = id, documentType = documentType, status = null, fileName = fileName, file = file)), Duration.Inf)

    def updateOldDocument(id: String, documentType: String, fileName: String, file: Option[Array[Byte]]): Int = Await.result(upsert(AccountKYC(id = id, documentType = documentType, status = null, fileName = fileName, file = file)), Duration.Inf)

    def get(id: String, documentType: String): AccountKYC = Await.result(findByIdDocumentType(id = id, documentType = documentType), Duration.Inf)

    def getAllDocuments(id: String): Seq[AccountKYC] = Await.result(getAllDocumentsById(id = id), Duration.Inf)

    def verifyAll(id: String): Int = Await.result(updateStatusById(id = id, status = Option(true)), Duration.Inf)

    def verify(id: String, documentType: String): Int = Await.result(updateStatusByIdAndDocumentType(id = id, documentType = documentType, status = Option(true)), Duration.Inf)

    def deleteAllDocuments(id: String): Int = Await.result(deleteById(id = id), Duration.Inf)

  }

}