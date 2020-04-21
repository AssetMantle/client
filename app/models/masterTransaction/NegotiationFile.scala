package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.{Document, DocumentContent}
import models.common.Serializable
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile
import models.common.Serializable._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class NegotiationFile(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], documentContent: Option[DocumentContent], status: Option[Boolean]) extends Document[NegotiationFile] {
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

  private def serialize(negotiationFile: NegotiationFile): NegotiationFileSerialized = NegotiationFileSerialized(negotiationFile.id, negotiationFile.documentType, negotiationFile.fileName, negotiationFile.file, negotiationFile.documentContent.map(Json.toJson(_).toString), negotiationFile.status)

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val negotiationFileTable = TableQuery[NegotiationFileTable]

  private def add(file: NegotiationFileSerialized): Future[String] = db.run((negotiationFileTable returning negotiationFileTable.map(_.id) += file).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(file: NegotiationFileSerialized): Future[Int] = db.run(negotiationFileTable.insertOrUpdate(file).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def upsertFile(file: NegotiationFile): Future[Int] = db.run(negotiationFileTable.map(x => (x.id, x.documentType, x.fileName, x.file.?)).insertOrUpdate(file.id, file.documentType, file.fileName, file.file).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def upsertContext(file: NegotiationFileSerialized): Future[Int] = db.run(negotiationFileTable.map(x => (x.id, x.documentType, x.fileName, x.documentContent.?)).insertOrUpdate(file.id, file.documentType, file.fileName, file.documentContent).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def updateAllFilesStatus(id: String, status: Boolean) = db.run(negotiationFileTable.map(x => (x.id, status)).update(id, status).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def updateDocument(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], status: Option[Boolean]): Future[Int] = db.run(negotiationFileTable.filter(_.id === id).filter(_.documentType === documentType).map(x => (x.fileName, x.file.?, x.status.?)).update((fileName, file, status)).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def updateStatus(id: String, documentType: String, status: Boolean): Future[Int] = db.run(negotiationFileTable.filter(_.id === id).filter(_.documentType === documentType).map(_.status).update(status).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def findByIdDocumentType(id: String, documentType: String): Future[NegotiationFileSerialized] = db.run(negotiationFileTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getByIdDocumentType(id: String, documentType: String): Future[Option[NegotiationFileSerialized]] = db.run(negotiationFileTable.filter(_.id === id).filter(_.documentType === documentType).result.headOption)

  private def getFileByIdDocumentType(id: String, documentType: String): Future[Array[Byte]] = db.run(negotiationFileTable.filter(_.id === id).filter(_.documentType === documentType).map(_.file).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => documentType match {
        case constants.File.PROFILE_PICTURE => Array[Byte]()
        case _ => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      }
    }
  }

  private def getFileNameByIdDocumentType(id: String, documentType: String): Future[String] = db.run(negotiationFileTable.filter(_.id === id).filter(_.documentType === documentType).map(_.fileName).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getDocumentContentByIDAndDocumentType(id: String, documentType: String): Future[Option[String]] = db.run(negotiationFileTable.filter(_.id === id).filter(_.documentType === documentType).map(_.documentContent.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAllDocumentsById(id: String): Future[Seq[NegotiationFileSerialized]] = db.run(negotiationFileTable.filter(_.id === id).result)

  private def getDocumentsByID(id: String, documents: Seq[String]): Future[Seq[NegotiationFileSerialized]] = db.run(negotiationFileTable.filter(_.id === id).filter(_.documentType inSet documents).result)

  private def deleteById(id: String) = db.run(negotiationFileTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateDocumentContentByID(id: String, documentType: String, documentContent: Option[String]): Future[Int] = db.run(negotiationFileTable.filter(_.id === id).filter(_.documentType === documentType).map(_.documentContent.?).update(documentContent).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def getIDAndDocumentType(id: String, documentType: String): Future[Boolean] = db.run(negotiationFileTable.filter(_.id === id).filter(_.documentType === documentType).exists.result)

  private def checkByIdAndFileName(id: String, fileName: String): Future[Boolean] = db.run(negotiationFileTable.filter(_.id === id).filter(_.fileName === fileName).exists.result)

  case class NegotiationFileSerialized(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], documentContent: Option[String], status: Option[Boolean]) {
    def deSerialize: NegotiationFile = NegotiationFile(id, documentType, fileName, file, documentContent.map(x => utilities.JSON.convertJsonStringToObject[DocumentContent](x)), status)
  }

  private[models] class NegotiationFileTable(tag: Tag) extends Table[NegotiationFileSerialized](tag, "NegotiationFile") {

    def * = (id, documentType, fileName, file.?, documentContent.?, status.?) <> (NegotiationFileSerialized.tupled, NegotiationFileSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def fileName = column[String]("fileName", O.Unique)

    def file = column[Array[Byte]]("file")

    def documentContent = column[String]("documentContent")

    def status = column[Boolean]("status")
  }

  object Service {

    def create(file: NegotiationFile): Future[String] = add(NegotiationFileSerialized(id = file.id, documentType = file.documentType, fileName = file.fileName, file = file.file, documentContent = None, status = None))

    def tryGet(id: String, documentType: String): Future[NegotiationFile] = findByIdDocumentType(id = id, documentType = documentType).map(_.deSerialize)

    def get(id: String, documentType: String): Future[Option[NegotiationFile]] = getByIdDocumentType(id = id, documentType = documentType).map(_.map(_.deSerialize))

    def getConfirmBidDocuments(id: String): Future[Seq[NegotiationFile]] = getDocumentsByID(id = id, documents = Seq(constants.File.BUYER_CONTRACT, constants.File.SELLER_CONTRACT)).map(_.map(_.deSerialize))

    def insertOrUpdateOldDocument(file: NegotiationFile): Future[Int] = upsertFile(NegotiationFile(id = file.id, documentType = file.documentType, fileName = file.fileName, file = file.file, documentContent = None, status = None))

    def updateFileStatus(id: String, documentType: String, status: Boolean): Future[Int] = updateStatus(id, documentType, status)

    def getFileName(id: String, documentType: String): Future[String] = getFileNameByIdDocumentType(id = id, documentType = documentType)

    def getAllDocuments(id: String): Future[Seq[NegotiationFile]] = getAllDocumentsById(id = id).map(_.map(_.deSerialize))

    def getDocuments(id: String, documents: Seq[String]): Future[Seq[NegotiationFile]] = getDocumentsByID(id, documents).map(_.map(_.deSerialize))

    def deleteAllDocuments(id: String): Future[Int] = deleteById(id = id)

    def checkFileExists(id: String, documentType: String): Future[Boolean] = getIDAndDocumentType(id, documentType)

    def checkFileNameExists(id: String, fileName: String): Future[Boolean] = checkByIdAndFileName(id = id, fileName = fileName)

    def updateDocumentContent(id: String, documentType: String, documentContent: DocumentContent): Future[Int] = updateDocumentContentByID(id, documentType, Some(Json.toJson(documentContent).toString))

    def getDocumentContent(id: String, documentType: String) = getDocumentContentByIDAndDocumentType(id = id, documentType = documentType).map(_.map(content => utilities.JSON.convertJsonStringToObject[DocumentContent](content)))
  }

}