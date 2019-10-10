package models.master

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Document
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class AccountKYC(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], status: Option[Boolean] = None) extends Document[AccountKYC] {

  def updateFile(newFile: Option[Array[Byte]]): AccountKYC = AccountKYC(id = id, documentType = documentType, fileName = fileName, file = newFile, status = status)

  def updateFileName(newFileName: String): AccountKYC = AccountKYC(id = id, documentType = documentType, fileName = newFileName, file = file, status = status)
}

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
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
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

  private def getFileNameByIdDocumentType(id: String, documentType: String): Future[String] = db.run(accountKYCTable.filter(_.id === id).filter(_.documentType === documentType).map(_.fileName).result.head.asTry).map {
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

  private def checkFileExistsByIdAndDocumentType(id: String, documentType: String): Future[Boolean] = db.run(accountKYCTable.filter(_.id === id).filter(_.documentType === documentType).exists.result)

  private def checkByIdAndFileName(id: String, fileName: String): Future[Boolean] = db.run(accountKYCTable.filter(_.id === id).filter(_.fileName === fileName).exists.result)

  private[models] class AccountKYCTable(tag: Tag) extends Table[AccountKYC](tag, "AccountKYC") {

    def * = (id, documentType, fileName, file.?, status.?) <> (AccountKYC.tupled, AccountKYC.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def status = column[Boolean]("status")

    def fileName = column[String]("fileName", O.Unique)

    def file = column[Array[Byte]]("file")

  }

  object Service {

    def create(accountKYC: AccountKYC): String = Await.result(add(AccountKYC(id = accountKYC.id, documentType = accountKYC.documentType, fileName = accountKYC.fileName, file = accountKYC.file)), Duration.Inf)

    def updateOldDocument(accountKYC: AccountKYC): Int = Await.result(upsert(AccountKYC(id = accountKYC.id, documentType = accountKYC.documentType, fileName = accountKYC.fileName, file = accountKYC.file)), Duration.Inf)

    def get(id: String, documentType: String): AccountKYC = Await.result(findByIdDocumentType(id = id, documentType = documentType), Duration.Inf)

    def getFileName(id: String, documentType: String): String = Await.result(getFileNameByIdDocumentType(id = id, documentType = documentType), Duration.Inf)

    def getAllDocuments(id: String): Future[Seq[AccountKYC]] = getAllDocumentsById(id = id)

    def verifyAll(id: String): Int = Await.result(updateStatusById(id = id, status = Option(true)), Duration.Inf)

    def verify(id: String, documentType: String): Int = Await.result(updateStatusByIdAndDocumentType(id = id, documentType = documentType, status = Option(true)), Duration.Inf)

    def deleteAllDocuments(id: String): Int = Await.result(deleteById(id = id), Duration.Inf)

    def checkFileExists(id: String, documentType: String): Boolean = Await.result(checkFileExistsByIdAndDocumentType(id = id, documentType = documentType), Duration.Inf)

    def checkFileNameExists(id: String, fileName: String): Boolean = Await.result(checkByIdAndFileName(id = id, fileName = fileName), Duration.Inf)
  }

}