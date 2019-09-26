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

case class ZoneKYC(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], status: Option[Boolean] = None) extends Document[ZoneKYC] {

  def updateFile(newFile: Option[Array[Byte]]): ZoneKYC = ZoneKYC(id = id, documentType = documentType, fileName = fileName, file = newFile, status = status)

  def updateFileName(newFileName: String): ZoneKYC = ZoneKYC(id = id, documentType = documentType, fileName = newFileName, file = file, status = status)
}

@Singleton
class ZoneKYCs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ZONE_KYC

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val zoneKYCTable = TableQuery[ZoneKYCTable]

  private def add(zoneKYC: ZoneKYC): Future[String] = db.run((zoneKYCTable returning zoneKYCTable.map(_.id) += zoneKYC).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(zoneKYC: ZoneKYC): Future[Int] = db.run(zoneKYCTable.insertOrUpdate(zoneKYC).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def findByIdDocumentType(id: String, documentType: String): Future[ZoneKYC] = db.run(zoneKYCTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getFileNameByIdDocumentType(id: String, documentType: String): Future[String] = db.run(zoneKYCTable.filter(_.id === id).filter(_.documentType === documentType).map(_.fileName).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusById(id: String, status: Option[Boolean]): Future[Int] = db.run(zoneKYCTable.filter(_.id === id).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusByIdAndDocumentType(id: String, documentType: String, status: Option[Boolean]): Future[Int] = db.run(zoneKYCTable.filter(_.id === id).filter(_.documentType === documentType).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAllDocumentsById(id: String): Future[Seq[ZoneKYC]] = db.run(zoneKYCTable.filter(_.id === id).result)

  private def getAllDocumentTypesByIDAndDocumentSet(id: String, documentTypes: Seq[String]): Future[Seq[String]] = db.run(zoneKYCTable.filter(_.id === id).filter(_.documentType.inSet(documentTypes)).map(_.documentType).result)

  private def getAllDocumentTypesByIDStatusAndDocumentSet(id: String, documentTypes: Seq[String], status: Boolean): Future[Seq[String]] = db.run(zoneKYCTable.filter(_.id === id).filter(_.documentType.inSet(documentTypes)).filter(_.status === status).map(_.documentType).result)

  private def deleteById(id: String) = db.run(zoneKYCTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def checkByIdAndDocumentType(id: String, documentType: String): Future[Boolean] = db.run(zoneKYCTable.filter(_.id === id).filter(_.documentType === documentType).exists.result)

  private def checkByIdAndFileName(id: String, fileName: String): Future[Boolean] = db.run(zoneKYCTable.filter(_.id === id).filter(_.fileName === fileName).exists.result)

  private[models] class ZoneKYCTable(tag: Tag) extends Table[ZoneKYC](tag, "ZoneKYC") {

    def * = (id, documentType, fileName, file.?, status.?) <> (ZoneKYC.tupled, ZoneKYC.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def status = column[Boolean]("status")

    def fileName = column[String]("fileName", O.Unique)

    def file = column[Array[Byte]]("file")

  }

  object Service {

    def create(zoneKYC: ZoneKYC): String = Await.result(add(ZoneKYC(id = zoneKYC.id, documentType = zoneKYC.documentType, fileName = zoneKYC.fileName, file = zoneKYC.file)), Duration.Inf)

    def updateOldDocument(zoneKYC: ZoneKYC): Int = Await.result(upsert(ZoneKYC(id = zoneKYC.id, documentType = zoneKYC.documentType, fileName = zoneKYC.fileName, file = zoneKYC.file)), Duration.Inf)

    def get(id: String, documentType: String): ZoneKYC = Await.result(findByIdDocumentType(id = id, documentType = documentType), Duration.Inf)

    def getFileName(id: String, documentType: String): String = Await.result(getFileNameByIdDocumentType(id = id, documentType = documentType), Duration.Inf)

    def getAllDocuments(id: String): Seq[ZoneKYC] = Await.result(getAllDocumentsById(id = id), Duration.Inf)

    def verifyAll(id: String): Int = Await.result(updateStatusById(id = id, status = Option(true)), Duration.Inf)

    def verify(id: String, documentType: String): Int = Await.result(updateStatusByIdAndDocumentType(id = id, documentType = documentType, status = Option(true)), Duration.Inf)

    def reject(id: String, documentType: String): Int = Await.result(updateStatusByIdAndDocumentType(id = id, documentType = documentType, status = Option(false)), Duration.Inf)

    def rejectAll(id: String): Int = Await.result(updateStatusById(id = id, status = Option(false)), Duration.Inf)

    def deleteAllDocuments(id: String): Int = Await.result(deleteById(id = id), Duration.Inf)

    def checkFileExists(id: String, documentType: String): Boolean = Await.result(checkByIdAndDocumentType(id = id, documentType = documentType), Duration.Inf)

    def checkFileNameExists(id: String, fileName: String): Boolean = Await.result(checkByIdAndFileName(id = id, fileName = fileName), Duration.Inf)

    def checkAllKYCFileTypesExists(id: String): Boolean = constants.File.ZONE_KYC_DOCUMENT_TYPES.diff(Await.result(getAllDocumentTypesByIDAndDocumentSet(id = id, documentTypes = constants.File.ZONE_KYC_DOCUMENT_TYPES), Duration.Inf)).isEmpty

    def checkAllKYCFilesVerified(id: String): Boolean = constants.File.ZONE_KYC_DOCUMENT_TYPES.diff(Await.result(getAllDocumentTypesByIDStatusAndDocumentSet(id = id, documentTypes = constants.File.ZONE_KYC_DOCUMENT_TYPES, status = true), Duration.Inf)).isEmpty

  }

}