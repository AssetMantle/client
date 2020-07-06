package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.{Document, Logged}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ZoneKYC(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], status: Option[Boolean] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Document[ZoneKYC] with Logged {

  def updateFileName(newFileName: String): ZoneKYC = copy(fileName = newFileName)

  def updateFile(newFile: Option[Array[Byte]]): ZoneKYC = copy(file = newFile)

  def updateStatus(status: Option[Boolean]): ZoneKYC = copy(status = status)

}

@Singleton
class ZoneKYCs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ZONE_KYC

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val zoneKYCTable = TableQuery[ZoneKYCTable]

  private def add(zoneKYC: ZoneKYC): Future[String] = db.run((zoneKYCTable returning zoneKYCTable.map(_.id) += zoneKYC).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def update(zoneKYC: ZoneKYC): Future[Int] = db.run(zoneKYCTable.filter(_.id === zoneKYC.id).filter(_.documentType === zoneKYC.documentType).update(zoneKYC).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def tryGetByIDAndDocumentType(id: String, documentType: String): Future[ZoneKYC] = db.run(zoneKYCTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getFileNameByIdDocumentType(id: String, documentType: String): Future[String] = db.run(zoneKYCTable.filter(_.id === id).filter(_.documentType === documentType).map(_.fileName).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusByIdAndDocumentType(id: String, documentType: String, status: Option[Boolean]): Future[Int] = db.run(zoneKYCTable.filter(_.id === id).filter(_.documentType === documentType).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getAllDocumentsById(id: String): Future[Seq[ZoneKYC]] = db.run(zoneKYCTable.filter(_.id === id).result)

  private def getAllDocumentTypesByIDAndDocumentSet(id: String, documentTypes: Seq[String]): Future[Seq[String]] = db.run(zoneKYCTable.filter(_.id === id).filter(_.documentType.inSet(documentTypes)).map(_.documentType).result)

  private def getAllDocumentTypesByIDStatusAndDocumentSet(id: String, documentTypes: Seq[String], status: Boolean): Future[Seq[String]] = db.run(zoneKYCTable.filter(_.id === id).filter(_.documentType.inSet(documentTypes)).filter(_.status === status).map(_.documentType).result)

  private def deleteById(id: String) = db.run(zoneKYCTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def checkByIdAndDocumentType(id: String, documentType: String): Future[Boolean] = db.run(zoneKYCTable.filter(_.id === id).filter(_.documentType === documentType).exists.result)

  private def checkByIdAndFileName(id: String, fileName: String): Future[Boolean] = db.run(zoneKYCTable.filter(_.id === id).filter(_.fileName === fileName).exists.result)

  private[models] class ZoneKYCTable(tag: Tag) extends Table[ZoneKYC](tag, "ZoneKYC") {

    def * = (id, documentType, fileName, file.?, status.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (ZoneKYC.tupled, ZoneKYC.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def status = column[Boolean]("status")

    def fileName = column[String]("fileName", O.Unique)

    def file = column[Array[Byte]]("file")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(zoneKYC: ZoneKYC): Future[String] = add(zoneKYC)

    def updateOldDocument(zoneKYC: ZoneKYC): Future[Int] = update(zoneKYC)

    def tryGet(id: String, documentType: String): Future[ZoneKYC] = tryGetByIDAndDocumentType(id = id, documentType = documentType)

    def getFileName(id: String, documentType: String): Future[String] = getFileNameByIdDocumentType(id = id, documentType = documentType)

    def getAllDocuments(id: String): Future[Seq[ZoneKYC]] = getAllDocumentsById(id = id)

    def verify(id: String, documentType: String): Future[Int] = updateStatusByIdAndDocumentType(id = id, documentType = documentType, status = Option(true))

    def reject(id: String, documentType: String): Future[Int] = updateStatusByIdAndDocumentType(id = id, documentType = documentType, status = Option(false))

    def deleteAllDocuments(id: String): Future[Int] = deleteById(id = id)

    def checkFileExists(id: String, documentType: String): Future[Boolean] = checkByIdAndDocumentType(id = id, documentType = documentType)

    def checkFileNameExists(id: String, fileName: String): Future[Boolean] = checkByIdAndFileName(id = id, fileName = fileName)

    def checkAllKYCFileTypesExists(id: String): Future[Boolean] = getAllDocumentTypesByIDAndDocumentSet(id = id, documentTypes = constants.File.ZONE_KYC_DOCUMENT_TYPES).map(constants.File.ZONE_KYC_DOCUMENT_TYPES.diff(_).isEmpty)

    def checkAllKYCFilesVerified(id: String): Future[Boolean] = getAllDocumentTypesByIDStatusAndDocumentSet(id = id, documentTypes = constants.File.ZONE_KYC_DOCUMENT_TYPES, status = true).map(constants.File.ZONE_KYC_DOCUMENT_TYPES.diff(_).isEmpty)

  }

}