package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.{Document, Logged}
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class TraderKYC(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], zoneStatus: Option[Boolean] = None, organizationStatus: Option[Boolean] = None, createdBy: String, createdOn: Timestamp, createdOnTimezone: String, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Document with Logged {

  val status: Option[Boolean] = Option(zoneStatus.getOrElse(false) && organizationStatus.getOrElse(false))

}

@Singleton
class TraderKYCs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRADER_KYC

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val nodeID = configuration.get[String]("node.id")

  private val nodeTimezone = configuration.get[String]("node.timezone")

  import databaseConfig.profile.api._

  private[models] val traderKYCTable = TableQuery[TraderKYCTable]

  private def add(id: String, documentType: String, fileName: String, file: Option[Array[Byte]]): Future[String] = db.run((traderKYCTable returning traderKYCTable.map(_.id) += TraderKYC(id = id, documentType = documentType, fileName = fileName, file = file, createdBy = nodeID, createdOn = new Timestamp(System.currentTimeMillis()), createdOnTimezone = nodeTimezone)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def update(id: String, documentType: String, fileName: String, file: Option[Array[Byte]]): Future[Int] = db.run(traderKYCTable.filter(_.id === id).filter(_.documentType === documentType).map(x => (x.fileName, x.file.?, x.updatedBy, x.updatedOn, x.updatedOnTimezone)).update((fileName, file, nodeID, new Timestamp(System.currentTimeMillis()), nodeTimezone)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByIdDocumentType(id: String, documentType: String): Future[TraderKYC] = db.run(traderKYCTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getFileNameByIdDocumentType(id: String, documentType: String): Future[String] = db.run(traderKYCTable.filter(_.id === id).filter(_.documentType === documentType).map(_.fileName).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def zoneUpdateStatusByIdAndDocumentType(id: String, documentType: String, zoneStatus: Option[Boolean]): Future[Int] = db.run(traderKYCTable.filter(_.id === id).filter(_.documentType === documentType).map(x => (x.zoneStatus.?, x.updatedBy, x.updatedOn, x.updatedOnTimezone)).update((zoneStatus, nodeID, new Timestamp(System.currentTimeMillis()), nodeTimezone)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def organizationUpdateStatusByIdAndDocumentType(id: String, documentType: String, organizationStatus: Option[Boolean]): Future[Int] = db.run(traderKYCTable.filter(_.id === id).filter(_.documentType === documentType).map(x => (x.organizationStatus.?, x.updatedBy, x.updatedOn, x.updatedOnTimezone)).update((organizationStatus, nodeID, new Timestamp(System.currentTimeMillis()), nodeTimezone)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAllDocumentsById(id: String): Future[Seq[TraderKYC]] = db.run(traderKYCTable.filter(_.id === id).result)

  private def getAllDocumentTypesByIDAndDocumentSet(id: String, documentTypes: Seq[String]): Future[Seq[String]] = db.run(traderKYCTable.filter(_.id === id).filter(_.documentType.inSet(documentTypes)).map(_.documentType).result)

  private def getAllDocumentTypesByIDStatusAndDocumentSet(id: String, documentTypes: Seq[String], status: Boolean): Future[Seq[String]] = db.run(traderKYCTable.filter(_.id === id).filter(_.documentType.inSet(documentTypes)).filter(traderKYC => traderKYC.organizationStatus && traderKYC.zoneStatus && status).map(_.documentType).result)

  private def deleteById(id: String) = db.run(traderKYCTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def checkByIdAndDocumentType(id: String, documentType: String): Future[Boolean] = db.run(traderKYCTable.filter(_.id === id).filter(_.documentType === documentType).exists.result)

  private def checkByIdAndFileName(id: String, fileName: String): Future[Boolean] = db.run(traderKYCTable.filter(_.id === id).filter(_.fileName === fileName).exists.result)

  private[models] class TraderKYCTable(tag: Tag) extends Table[TraderKYC](tag, "TraderKYC") {

    def * = (id, documentType, fileName, file.?, zoneStatus.?, organizationStatus.?, createdBy, createdOn, createdOnTimezone, updatedBy.?, updatedOn.?, updatedOnTimezone.?) <> (TraderKYC.tupled, TraderKYC.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def zoneStatus = column[Boolean]("zoneStatus")

    def organizationStatus = column[Boolean]("organizationStatus")

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

    def create(id: String, documentType: String, fileName: String, file: Option[Array[Byte]]): Future[String] = add(id = id, documentType = documentType, fileName = fileName, file = file)

    def updateOldDocument(id: String, documentType: String, fileName: String, file: Option[Array[Byte]]): Future[Int] = update(id = id, documentType = documentType, fileName = fileName, file = file)

    def get(id: String, documentType: String): Future[TraderKYC] = findByIdDocumentType(id = id, documentType = documentType)

    def getFileName(id: String, documentType: String): Future[String] = getFileNameByIdDocumentType(id = id, documentType = documentType)

    def getAllDocuments(id: String): Future[Seq[TraderKYC]] = getAllDocumentsById(id = id)

    def zoneVerify(id: String, documentType: String): Future[Int] = zoneUpdateStatusByIdAndDocumentType(id = id, documentType = documentType, zoneStatus = Option(true))

    def zoneReject(id: String, documentType: String): Future[Int] = zoneUpdateStatusByIdAndDocumentType(id = id, documentType = documentType, zoneStatus = Option(false))

    def organizationVerify(id: String, documentType: String): Future[Int] = organizationUpdateStatusByIdAndDocumentType(id = id, documentType = documentType, organizationStatus = Option(true))

    def organizationReject(id: String, documentType: String): Future[Int] = organizationUpdateStatusByIdAndDocumentType(id = id, documentType = documentType, organizationStatus = Option(false))

    def deleteAllDocuments(id: String): Future[Int] = deleteById(id = id)

    def checkFileExists(id: String, documentType: String): Future[Boolean] = checkByIdAndDocumentType(id = id, documentType = documentType)

    def checkFileNameExists(id: String, fileName: String): Future[Boolean] = checkByIdAndFileName(id = id, fileName = fileName)

    def checkAllKYCFileTypesExists(id: String): Future[Boolean] = getAllDocumentTypesByIDAndDocumentSet(id = id, documentTypes = constants.File.TRADER_KYC_DOCUMENT_TYPES).map {
      constants.File.TRADER_KYC_DOCUMENT_TYPES.diff(_).isEmpty
    }

    def checkAllKYCFilesVerified(id: String): Future[Boolean] = getAllDocumentTypesByIDStatusAndDocumentSet(id = id, documentTypes = constants.File.TRADER_KYC_DOCUMENT_TYPES, status = true).map {
      constants.File.TRADER_KYC_DOCUMENT_TYPES.diff(_).isEmpty
    }

  }

}