package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.{Document, Logged}
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class TraderBackgroundCheck(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], status: Option[Boolean] = Option(true), createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Document[TraderBackgroundCheck] with Logged {

  def updateFileName(newFileName: String): TraderBackgroundCheck = copy(fileName = newFileName)

  def updateFile(newFile: Option[Array[Byte]]): TraderBackgroundCheck = copy(file = newFile)

}

@Singleton
class TraderBackgroundChecks @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRADER_BACKGROUND_CHECK

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val traderBackgroundCheckTable = TableQuery[TraderBackgroundCheckTable]

  private def add(traderBackgroundCheck: TraderBackgroundCheck): Future[String] = db.run((traderBackgroundCheckTable returning traderBackgroundCheckTable.map(_.id) += traderBackgroundCheck).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def update(traderBackgroundCheck: TraderBackgroundCheck): Future[Int] = db.run(traderBackgroundCheckTable.filter(_.id === traderBackgroundCheck.id).filter(_.documentType === traderBackgroundCheck.documentType).update(traderBackgroundCheck).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def tryGetByIDAndDocumentType(id: String, documentType: String): Future[TraderBackgroundCheck] = db.run(traderBackgroundCheckTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def tryGetFileNameByIdDocumentType(id: String, documentType: String): Future[String] = db.run(traderBackgroundCheckTable.filter(_.id === id).filter(_.documentType === documentType).map(_.fileName).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAllDocumentsById(id: String): Future[Seq[TraderBackgroundCheck]] = db.run(traderBackgroundCheckTable.filter(_.id === id).result)

  private def getAllDocumentTypesByIDStatusAndDocumentSet(id: String, documentTypes: Seq[String], status: Boolean): Future[Seq[String]] = db.run(traderBackgroundCheckTable.filter(_.id === id).filter(_.documentType.inSet(documentTypes)).filter(_.status === status).map(_.documentType).result)

  private def deleteById(id: String): Future[Int] = db.run(traderBackgroundCheckTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private[models] class TraderBackgroundCheckTable(tag: Tag) extends Table[TraderBackgroundCheck](tag, "TraderBackgroundCheck") {

    def * = (id, documentType, fileName, file.?, status.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (TraderBackgroundCheck.tupled, TraderBackgroundCheck.unapply)

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

    def create(traderBackgroundCheck: TraderBackgroundCheck): Future[String] = add(traderBackgroundCheck)

    def updateOldDocument(traderBackgroundCheck: TraderBackgroundCheck): Future[Int] = update(traderBackgroundCheck)

    def tryGet(id: String, documentType: String): Future[TraderBackgroundCheck] = tryGetByIDAndDocumentType(id = id, documentType = documentType)

    def tryGetFileName(id: String, documentType: String): Future[String] = tryGetFileNameByIdDocumentType(id = id, documentType = documentType)

    def getAllDocuments(id: String): Future[Seq[TraderBackgroundCheck]] = getAllDocumentsById(id = id)

    def checkAllBackgroundFilesVerified(id: String): Future[Boolean] = getAllDocumentTypesByIDStatusAndDocumentSet(id = id, documentTypes = constants.File.TRADER_BACKGROUND_CHECK_DOCUMENT_TYPES, status = true).map {
      constants.File.TRADER_BACKGROUND_CHECK_DOCUMENT_TYPES.diff(_).isEmpty
    }
  }

}