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

case class TraderBackgroundCheck(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], status: Option[Boolean] = None) extends Document[TraderBackgroundCheck] {

  def updateFile(newFile: Option[Array[Byte]]): TraderBackgroundCheck = TraderBackgroundCheck(id = id, documentType = documentType, fileName = fileName, file = newFile, status = status)

  def updateFileName(newFileName: String): TraderBackgroundCheck = TraderBackgroundCheck(id = id, documentType = documentType, fileName = newFileName, file = file, status = status)

}

@Singleton
class TraderBackgroundChecks @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ORGANIZATION_KYC

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

  private def upsert(traderBackgroundCheck: TraderBackgroundCheck): Future[Int] = db.run(traderBackgroundCheckTable.insertOrUpdate(traderBackgroundCheck).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def findByIdDocumentType(id: String, documentType: String): Future[TraderBackgroundCheck] = db.run(traderBackgroundCheckTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
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

  private def updateStatusById(id: String, status: Option[Boolean]): Future[Int] = db.run(traderBackgroundCheckTable.filter(_.id === id).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusByIdAndDocumentType(id: String, documentType: String, status: Option[Boolean]): Future[Int] = db.run(traderBackgroundCheckTable.filter(_.id === id).filter(_.documentType === documentType).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAllDocumentsById(id: String): Future[Seq[TraderBackgroundCheck]] = db.run(traderBackgroundCheckTable.filter(_.id === id).result)

  private def getAllDocumentTypesByIDAndDocumentSet(id: String, documentTypes: Seq[String]): Future[Seq[String]] = db.run(traderBackgroundCheckTable.filter(_.id === id).filter(_.documentType.inSet(documentTypes)).map(_.documentType).result)

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

  private def checkByIdAndDocumentType(id: String, documentType: String): Future[Boolean] = db.run(traderBackgroundCheckTable.filter(_.id === id).filter(_.documentType === documentType).exists.result)

  private def checkByIdAndFileName(id: String, fileName: String): Future[Boolean] = db.run(traderBackgroundCheckTable.filter(_.id === id).filter(_.fileName === fileName).exists.result)

  private[models] class TraderBackgroundCheckTable(tag: Tag) extends Table[TraderBackgroundCheck](tag, "TraderBackgroundCheck") {

    def * = (id, documentType, fileName, file.?, status.?) <> (TraderBackgroundCheck.tupled, TraderBackgroundCheck.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def status = column[Boolean]("status")

    def fileName = column[String]("fileName", O.Unique)

    def file = column[Array[Byte]]("file")

  }

  object Service {

    def create(traderBackgroundCheck: TraderBackgroundCheck): Future[String] = add(TraderBackgroundCheck(id = traderBackgroundCheck.id, documentType = traderBackgroundCheck.documentType, fileName = traderBackgroundCheck.fileName, file = traderBackgroundCheck.file, status = Option(true)))

    def updateOldDocument(traderBackgroundCheck: TraderBackgroundCheck): Future[Int] = upsert(TraderBackgroundCheck(id = traderBackgroundCheck.id, documentType = traderBackgroundCheck.documentType, fileName = traderBackgroundCheck.fileName, file = traderBackgroundCheck.file, status = Option(true)))

    def get(id: String, documentType: String): Future[TraderBackgroundCheck] = findByIdDocumentType(id = id, documentType = documentType)

    def getFileName(id: String, documentType: String): Future[String] = getFileNameByIdDocumentType(id = id, documentType = documentType)

    def getAllDocuments(id: String): Future[Seq[TraderBackgroundCheck]] = getAllDocumentsById(id = id)

    def verifyAll(id: String): Future[Int] = updateStatusById(id = id, status = Option(true))

    def verify(id: String, documentType: String): Future[Int] = updateStatusByIdAndDocumentType(id = id, documentType = documentType, status = Option(true))

    def reject(id: String, documentType: String): Future[Int] = updateStatusByIdAndDocumentType(id = id, documentType = documentType, status = Option(false))

    def rejectAll(id: String): Future[Int] = updateStatusById(id = id, status = Option(false))

    def deleteAllDocuments(id: String): Future[Int] = deleteById(id = id)

    def checkFileExists(id: String, documentType: String): Future[Boolean] = checkByIdAndDocumentType(id = id, documentType = documentType)

    def checkFileNameExists(id: String, fileName: String): Future[Boolean] = checkByIdAndFileName(id = id, fileName = fileName)

    def checkAllBackgroundFilesVerified(id: String): Future[Boolean] = getAllDocumentTypesByIDStatusAndDocumentSet(id = id, documentTypes = constants.File.TRADER_BACKGROUND_CHECK_DOCUMENT_TYPES, status = true).map {
      constants.File.TRADER_BACKGROUND_CHECK_DOCUMENT_TYPES.diff(_).isEmpty
    }
  }

}