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

case class TraderBackgroundCheck(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], status: Option[Boolean] = Option(true), createdBy: String, createdOn: Timestamp, createdOnTimezone: String, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Document with Logged

@Singleton
class TraderBackgroundChecks @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRADER_BACKGROUND_CHECK

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val nodeID = configuration.get[String]("node.id")

  private val nodeTimezone = configuration.get[String]("node.timezone")

  import databaseConfig.profile.api._

  private[models] val traderBackgroundCheckTable = TableQuery[TraderBackgroundCheckTable]

  private def add(id: String, documentType: String, fileName: String, file: Option[Array[Byte]]): Future[String] = db.run((traderBackgroundCheckTable returning traderBackgroundCheckTable.map(_.id) += TraderBackgroundCheck(id = id, documentType = documentType, fileName = fileName, file = file, createdBy = nodeID, createdOn = new Timestamp(System.currentTimeMillis()), createdOnTimezone = nodeTimezone)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def update(id: String, documentType: String, fileName: String, file: Option[Array[Byte]]): Future[Int] = db.run(traderBackgroundCheckTable.filter(_.id === id).filter(_.documentType === documentType).map(x => (x.fileName, x.file.?, x.updatedBy, x.updatedOn, x.updatedOnTimezone)).update((fileName, file, nodeID, new Timestamp(System.currentTimeMillis()), nodeTimezone)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getFileNameByIdDocumentType(id: String, documentType: String): Future[String] = db.run(traderBackgroundCheckTable.filter(_.id === id).filter(_.documentType === documentType).map(_.fileName).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAllDocumentsById(id: String): Future[Seq[TraderBackgroundCheck]] = db.run(traderBackgroundCheckTable.filter(_.id === id).result)

  private def getAllDocumentTypesByIDStatusAndDocumentSet(id: String, documentTypes: Seq[String], status: Boolean): Future[Seq[String]] = db.run(traderBackgroundCheckTable.filter(_.id === id).filter(_.documentType.inSet(documentTypes)).filter(_.status === status).map(_.documentType).result)

  private def deleteById(id: String) = db.run(traderBackgroundCheckTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private[models] class TraderBackgroundCheckTable(tag: Tag) extends Table[TraderBackgroundCheck](tag, "TraderBackgroundCheck") {

    def * = (id, documentType, fileName, file.?, status.?, createdBy, createdOn, createdOnTimezone, updatedBy.?, updatedOn.?, updatedOnTimezone.?) <> (TraderBackgroundCheck.tupled, TraderBackgroundCheck.unapply)

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

    def create(id: String, documentType: String, fileName: String, file: Option[Array[Byte]]): Future[String] = add(id = id, documentType = documentType, fileName = fileName, file = file)

    def updateOldDocument(id: String, documentType: String, fileName: String, file: Option[Array[Byte]]): Future[Int] = update(id = id, documentType = documentType, fileName = fileName, file = file)

    def getFileName(id: String, documentType: String): Future[String] = getFileNameByIdDocumentType(id = id, documentType = documentType)

    def getAllDocuments(id: String): Future[Seq[TraderBackgroundCheck]] = getAllDocumentsById(id = id)

    def checkAllBackgroundFilesVerified(id: String): Future[Boolean] = getAllDocumentTypesByIDStatusAndDocumentSet(id = id, documentTypes = constants.File.TRADER_BACKGROUND_CHECK_DOCUMENT_TYPES, status = true).map {
      constants.File.TRADER_BACKGROUND_CHECK_DOCUMENT_TYPES.diff(_).isEmpty
    }
  }

}