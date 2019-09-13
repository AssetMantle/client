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

case class OrganizationKYC(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], status: Option[Boolean]) extends Document[OrganizationKYC]{

  def updateFile(newFile: Option[Array[Byte]]): OrganizationKYC = OrganizationKYC(id = id, documentType = documentType, fileName = fileName, file = newFile, status = status)

  def updateFileName(newFileName: String): OrganizationKYC = OrganizationKYC(id = id, documentType = documentType, fileName = newFileName, file = file, status = status)

}

@Singleton
class OrganizationKYCs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ORGANIZATION_KYC

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val organizationKYCTable = TableQuery[OrganizationKYCTable]

  private def add(organizationKYC: OrganizationKYC): Future[String] = db.run((organizationKYCTable returning organizationKYCTable.map(_.id) += organizationKYC).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(organizationKYC: OrganizationKYC): Future[Int] = db.run(organizationKYCTable.insertOrUpdate(organizationKYC).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def findByIdDocumentType(id: String, documentType: String): Future[OrganizationKYC] = db.run(organizationKYCTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getFileNameByIdDocumentType(id: String, documentType: String): Future[String] = db.run(organizationKYCTable.filter(_.id === id).filter(_.documentType === documentType).map(_.fileName).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusById(id: String, status: Option[Boolean]): Future[Int] = db.run(organizationKYCTable.filter(_.id === id).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusByIdAndDocumentType(id: String, documentType: String, status: Option[Boolean]): Future[Int] = db.run(organizationKYCTable.filter(_.id === id).filter(_.documentType === documentType).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAllDocumentsById(id: String): Future[Seq[OrganizationKYC]] = db.run(organizationKYCTable.filter(_.id === id).result)

  private def deleteById(id: String) = db.run(organizationKYCTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def checkByIdAndDocumentType(id: String, documentType: String): Future[Boolean] = db.run(organizationKYCTable.filter(_.id === id).filter(_.documentType === documentType).exists.result)

  private def checkByIdAndFileName(id: String, fileName: String): Future[Boolean] = db.run(organizationKYCTable.filter(_.id === id).filter(_.fileName === fileName).exists.result)

  private[models] class OrganizationKYCTable(tag: Tag) extends Table[OrganizationKYC](tag, "OrganizationKYC") {

    def * = (id, documentType, fileName, file.?, status.?) <> (OrganizationKYC.tupled, OrganizationKYC.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def status = column[Boolean]("status")

    def fileName = column[String]("fileName", O.Unique)

    def file = column[Array[Byte]]("file")

  }

  object Service {

    def create(organizationKYC: OrganizationKYC): String = Await.result(add(OrganizationKYC(id = organizationKYC.id, documentType = organizationKYC.documentType, fileName = organizationKYC.fileName, file = organizationKYC.file, status = None)), Duration.Inf)

    def updateOldDocument(organizationKYC: OrganizationKYC): Int = Await.result(upsert(OrganizationKYC(id = organizationKYC.id, documentType = organizationKYC.documentType, fileName = organizationKYC.fileName, file = organizationKYC.file, status = None)), Duration.Inf)

    def get(id: String, documentType: String): OrganizationKYC = Await.result(findByIdDocumentType(id = id, documentType = documentType), Duration.Inf)

    def getFileName(id: String, documentType: String): String = Await.result(getFileNameByIdDocumentType(id = id, documentType = documentType), Duration.Inf)

    def getAllDocuments(id: String): Seq[OrganizationKYC] = Await.result(getAllDocumentsById(id = id), Duration.Inf)

    def verifyAll(id: String): Int = Await.result(updateStatusById(id = id, status = Option(true)), Duration.Inf)

    def verify(id: String, documentType: String): Int = Await.result(updateStatusByIdAndDocumentType(id = id, documentType = documentType, status = Option(true)), Duration.Inf)

    def reject(id: String, documentType: String): Int = Await.result(updateStatusByIdAndDocumentType(id = id, documentType = documentType, status = Option(false)), Duration.Inf)

    def rejectAll(id: String): Int = Await.result(updateStatusById(id = id, status = Option(false)), Duration.Inf)

    def deleteAllDocuments(id: String): Int = Await.result(deleteById(id = id), Duration.Inf)

    def checkFileExists(id: String, documentType: String): Boolean = Await.result(checkByIdAndDocumentType(id = id, documentType = documentType), Duration.Inf)

    def checkFileNameExists(id: String, fileName: String): Boolean = Await.result(checkByIdAndFileName(id = id, fileName = fileName), Duration.Inf)

  }

}