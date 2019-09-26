package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Document
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{Json, OWrites, Reads}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class NegotiationFile(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], documentContent: Option[String], status: Option[Boolean]) extends Document[NegotiationFile] {
  def updateFile(newFile: Option[Array[Byte]]): NegotiationFile = NegotiationFile(id = id, documentType = documentType, fileName = fileName, file = newFile, documentContent = documentContent, status = status)

  def updateFileName(newFileName: String): NegotiationFile = NegotiationFile(id = id, documentType = documentType, fileName = newFileName, file = file, documentContent = documentContent, status = status)
}

object NegotiationFileTypeContext {

}

@Singleton
class NegotiationFiles @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_NEGOTIATION_FILE

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val fileTable = TableQuery[NegotiationFileTable]

  private def add(file: NegotiationFile): Future[String] = db.run((fileTable returning fileTable.map(_.id) += file).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(file: NegotiationFile): Future[Int] = db.run(fileTable.insertOrUpdate(file).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def upsertFile(file: NegotiationFile): Future[Int] = db.run(fileTable.map(x => (x.id, x.documentType, x.fileName, x.file.?)).insertOrUpdate(file.id, file.documentType, file.fileName, file.file).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def upsertContext(file: NegotiationFile): Future[Int] = db.run(fileTable.map(x => (x.id, x.documentType, x.fileName, x.documentContent.?)).insertOrUpdate(file.id, file.documentType, file.fileName, file.documentContent).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def updateAllFilesStatus(id: String, status: Boolean) = db.run(fileTable.map(x => (x.id, status)).update(id, status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def updateDocument(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], status: Option[Boolean]): Future[Int] = db.run(fileTable.filter(_.id === id).filter(_.documentType === documentType).map(x => (x.fileName, x.file.?, x.status.?)).update((fileName, file, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def updateStatus(id: String, documentType: String, status: Boolean): Future[Int] = db.run(fileTable.filter(_.id === id).filter(_.documentType === documentType).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def findByIdDocumentType(id: String, documentType: String): Future[NegotiationFile] = db.run(fileTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByIdDocumentTypeOrEmpty(id: String, documentType: String): Future[NegotiationFile] = db.run(fileTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case _: NoSuchElementException => NegotiationFile("", "", "", None, None, None)
    }
  }

  private def getFileByIdDocumentType(id: String, documentType: String): Future[Array[Byte]] = db.run(fileTable.filter(_.id === id).filter(_.documentType === documentType).map(_.file).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => documentType match {
        case constants.File.PROFILE_PICTURE => Array[Byte]()
        case _ => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      }
    }
  }

  private def getFileNameByIdDocumentType(id: String, documentType: String): Future[String] = db.run(fileTable.filter(_.id === id).filter(_.documentType === documentType).map(_.fileName).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAllDocumentsById(id: String): Future[Seq[NegotiationFile]] = db.run(fileTable.filter(_.id === id).result)

  private def deleteById(id: String) = db.run(fileTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getIDAndDocumentType(id: String, documentType: String): Future[Boolean] = db.run(fileTable.filter(_.id === id).filter(_.documentType === documentType).exists.result)

  private def checkByIdAndFileName(id: String, fileName: String): Future[Boolean] = db.run(fileTable.filter(_.id === id).filter(_.fileName === fileName).exists.result)

  private[models] class NegotiationFileTable(tag: Tag) extends Table[NegotiationFile](tag, "NegotiationFile") {

    def * = (id, documentType, fileName, file.?, documentContent.?, status.?) <> (NegotiationFile.tupled, NegotiationFile.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def fileName = column[String]("fileName", O.Unique)

    def file = column[Array[Byte]]("file")

    def documentContent = column[String]("documentContent")

    def status = column[Boolean]("status")
  }

  object Service {

    def create(file: NegotiationFile): String = Await.result(add(NegotiationFile(id = file.id, documentType = file.documentType, fileName = file.fileName, file = file.file, documentContent = None, status = None)), Duration.Inf)

    def getOrEmpty(id: String, documentType: String): NegotiationFile = Await.result(findByIdDocumentTypeOrEmpty(id = id, documentType = documentType), Duration.Inf)

    def get(id: String, documentType: String): NegotiationFile = Await.result(findByIdDocumentType(id = id, documentType = documentType), Duration.Inf)

    def insertOrUpdateOldDocument(file: NegotiationFile): Int = Await.result(upsertFile(NegotiationFile(id = file.id, documentType = file.documentType, fileName = file.fileName, file = file.file, documentContent = None, status = None)), Duration.Inf)

    def insertOrUpdateContext(file: NegotiationFile): String = Await.result(upsertContext(file), Duration.Inf).toString

    def updateFileStatus(id: String, documentType: String, status: Boolean) = Await.result(updateStatus(id, documentType, status), Duration.Inf)

    def getFileName(id: String, documentType: String): String = Await.result(getFileNameByIdDocumentType(id = id, documentType = documentType), Duration.Inf)

    def getAllDocuments(id: String): Seq[NegotiationFile] = Await.result(getAllDocumentsById(id = id), Duration.Inf)

    def deleteAllDocuments(id: String): Int = Await.result(deleteById(id = id), Duration.Inf)

    def checkFileExists(id: String, documentType: String): Boolean =  Await.result(getIDAndDocumentType(id, documentType), Duration.Inf)

    def checkFileNameExists(id: String, fileName: String): Boolean = Await.result(checkByIdAndFileName(id = id, fileName = fileName), Duration.Inf)
  }

}