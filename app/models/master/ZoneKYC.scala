package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.{Document, Logged}
import models.common.Node
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ZoneKYC(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], status: Option[Boolean] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimezone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Document[ZoneKYC] with Logged[ZoneKYC] {

  def updateFileName(newFileName: String): ZoneKYC = copy(fileName = newFileName)

  def updateFile(newFile: Option[Array[Byte]]): ZoneKYC = copy(file = newFile)

  def createLog()(implicit node: Node): ZoneKYC = copy(createdBy = Option(node.id), createdOn = Option(new Timestamp(System.currentTimeMillis())), createdOnTimezone = Option(node.timeZone))

  def updateLog()(implicit node: Node): ZoneKYC = copy(updatedBy = Option(node.id), updatedOn = Option(new Timestamp(System.currentTimeMillis())), updatedOnTimeZone = Option(node.timeZone))

}

@Singleton
class ZoneKYCs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ZONE_KYC

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val node: Node = Node(id = configuration.get[String]("node.id"), timeZone = configuration.get[String]("node.timeZone"))

  import databaseConfig.profile.api._

  private[models] val zoneKYCTable = TableQuery[ZoneKYCTable]

  private def add(zoneKYC: ZoneKYC): Future[String] = db.run((zoneKYCTable returning zoneKYCTable.map(_.id) += zoneKYC.createLog()).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def update(zoneKYC: ZoneKYC): Future[Int] = db.run(zoneKYCTable.filter(_.id === zoneKYC.id).filter(_.documentType === zoneKYC.documentType).update(zoneKYC.updateLog()).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
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

  private def updateStatusByIdAndDocumentType(id: String, documentType: String, status: Option[Boolean]): Future[Int] = db.run(zoneKYCTable.filter(_.id === id).filter(_.documentType === documentType).map(x => (x.status.?, x.updatedBy, x.updatedOn, x.updatedOnTimezone)).update((status, node.id, new Timestamp(System.currentTimeMillis()), node.timeZone)).asTry).map {
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

    def * = (id, documentType, fileName, file.?, status.?, createdBy.?, createdOn.?, createdOnTimezone.?, updatedBy.?, updatedOn.?, updatedOnTimezone.?) <> (ZoneKYC.tupled, ZoneKYC.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def status = column[Boolean]("status")

    def fileName = column[String]("fileName", O.Unique)

    def file = column[Array[Byte]]("file")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimezone = column[String]("createdOnTimezone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimezone = column[String]("updatedOnTimezone")

  }

  object Service {

    def create(zoneKYC: ZoneKYC): Future[String] = add(zoneKYC)

    def updateOldDocument(zoneKYC: ZoneKYC): Future[Int] = update(zoneKYC)

    def get(id: String, documentType: String): Future[ZoneKYC] = findByIdDocumentType(id = id, documentType = documentType)

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