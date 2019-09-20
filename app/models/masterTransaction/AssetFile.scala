package models.masterTransaction

import java.util.Date

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Document
import models.Trait.DocumentContent
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import models.common.Serializable._
import play.api.libs.json.{JsValue, Json, OWrites, Reads, Writes}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class AssetFile(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], documentContent: Option[DocumentContent], status: Option[Boolean]) extends Document[AssetFile] {
  def updateFile(newFile: Option[Array[Byte]]): AssetFile = AssetFile(id = id, documentType = documentType, fileName = fileName, file = newFile, documentContent = documentContent, status = status)

  def updateFileName(newFileName: String): AssetFile = AssetFile(id = id, documentType = documentType, fileName = newFileName, file = file, documentContent = documentContent, status = status)
}

@Singleton
class AssetFiles @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_ASSET_FILE

  case class AssetFileSerialized(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], documentContent: Option[String], status: Option[Boolean]) {
    def deserialize: AssetFile =
        documentContent match {
          case Some(content) => AssetFile(id, documentType, fileName, file, Option(utilities.JSON.convertJsonStringToObject[DocumentContent](content.toString)), status)
          case None =>AssetFile(id, documentType, fileName, file, None, status)
        }

  }

  private def serialize(assetFile: AssetFile): AssetFileSerialized = {
    assetFile.documentContent match {
      case Some(content) =>  AssetFileSerialized(assetFile.id, assetFile.documentType, assetFile.fileName, assetFile.file, Option (Json.toJson(content).toString), assetFile.status)
      case None => AssetFileSerialized(assetFile.id, assetFile.documentType, assetFile.fileName, assetFile.file, None, assetFile.status)
    }
  }

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val assetFileTable = TableQuery[AssetFileTable]

  private def add(file: AssetFileSerialized): Future[String] = db.run((assetFileTable returning assetFileTable.map(_.id) += file).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(file: AssetFileSerialized): Future[Int] = db.run(assetFileTable.insertOrUpdate(file).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def upsertFile(file: AssetFile): Future[Int] = db.run(assetFileTable.map(x => (x.id, x.documentType, x.fileName, x.file.?)).insertOrUpdate(file.id, file.documentType, file.fileName, file.file).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def upsertContext(file: AssetFileSerialized): Future[Int] = db.run(assetFileTable.map(x => (x.id, x.documentType, x.documentContent.?)).insertOrUpdate(file.id, file.documentType, file.documentContent).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def updateAllFilesStatus(id: String, status: Boolean): Future[Int] = db.run(assetFileTable.map(x => (x.id, status)).update(id, status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def updateDocument(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], status: Option[Boolean]): Future[Int] = db.run(assetFileTable.filter(_.id === id).filter(_.documentType === documentType).map(x => (x.fileName, x.file.?, x.status.?)).update((fileName, file, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def updateStatus(id: String, documentType: String, status: Boolean): Future[Int] = db.run(assetFileTable.filter(_.id === id).filter(_.documentType === documentType).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def findByIdDocumentType(id: String, documentType: String): Future[Option[AssetFileSerialized]] = db.run(assetFileTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => Option(result)
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        None
    }
  }

  private def getFileByIdDocumentType(id: String, documentType: String): Future[Array[Byte]] = db.run(assetFileTable.filter(_.id === id).filter(_.documentType === documentType).map(_.file).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => documentType match {
        case constants.File.PROFILE_PICTURE => Array[Byte]()
        case _ => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      }
    }
  }

  private def getFileNameByIdDocumentType(id: String, documentType: String): Future[String] = db.run(assetFileTable.filter(_.id === id).filter(_.documentType === documentType).map(_.fileName).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getStatusForAllDocumentsById(id: String): Future[Seq[Option[Boolean]]] = db.run(assetFileTable.filter(_.id === id).map(_.status.?).result)

  private def getAllDocumentsById(id: String): Future[Seq[AssetFileSerialized]] = db.run(assetFileTable.filter(_.id === id).result)

  private def getDocumentsByID(id: String, documents: Seq[String]): Future[Seq[AssetFileSerialized]] = db.run(assetFileTable.filter(_.id === id).filter(_.documentType inSet documents).result)

  private def deleteById(id: String) = db.run(assetFileTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getIDAndDocumentType(id: String, documentType: String): Future[Boolean] = db.run(assetFileTable.filter(_.id === id).filter(_.documentType === documentType).exists.result)

  private def checkByIdAndFileName(id: String, fileName: String): Future[Boolean] = db.run(assetFileTable.filter(_.id === id).filter(_.fileName === fileName).exists.result)

  private[models] class AssetFileTable(tag: Tag) extends Table[AssetFileSerialized](tag, "AssetFile") {

    def * = (id, documentType, fileName, file.?, documentContent.?, status.?) <> (AssetFileSerialized.tupled, AssetFileSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def fileName = column[String]("fileName", O.Unique)

    def file = column[Array[Byte]]("file")

    def documentContent = column[String]("documentContent")

    def status = column[Boolean]("status")
  }

  object Service {

    def create(file: AssetFile): String = Await.result(add(serialize(AssetFile(id = file.id, documentType = file.documentType, fileName = file.fileName, file = file.file, documentContent = None, status = None))), Duration.Inf)

    def getOrEmpty(id: String, documentType: String): AssetFile = Await.result(findByIdDocumentType(id = id, documentType = documentType), Duration.Inf).getOrElse(AssetFileSerialized("","","",None,None,None)).deserialize

    def getOrNone(id: String, documentType: String): Option[AssetFile] = {

      Await.result(findByIdDocumentType(id = id, documentType = documentType), Duration.Inf) match {
        case Some(assetFile) => Option(assetFile.deserialize)
        case None => None
      }
    }

    def get(id: String, documentType: String): AssetFile = Await.result(findByIdDocumentType(id = id, documentType = documentType), Duration.Inf).getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)).deserialize

    def insertOrUpdateOldDocument(file: AssetFile): Int = Await.result(upsertFile(AssetFile(id = file.id, documentType = file.documentType, fileName = file.fileName, file = file.file, documentContent = None, status = None)), Duration.Inf)

    def insertOrUpdateContext(file: AssetFile): Int = Await.result(upsertContext(serialize(file)), Duration.Inf)

    def accept(id: String, documentType: String): Int = Await.result(updateStatus(id, documentType, status = true), Duration.Inf)

    def reject(id: String, documentType: String): Int = Await.result(updateStatus(id, documentType, status = false), Duration.Inf)

    def getFileName(id: String, documentType: String): String = Await.result(getFileNameByIdDocumentType(id = id, documentType = documentType), Duration.Inf)

    def getAllDocuments(id: String): Seq[AssetFile] = Await.result(getAllDocumentsById(id = id), Duration.Inf).map(_.deserialize)

    def getDocuments(id: String, documents: Seq[String]): Seq[AssetFile] = Await.result(getDocumentsByID(id, documents), Duration.Inf).map(_.deserialize)

    def deleteAllDocuments(id: String): Int = Await.result(deleteById(id = id), Duration.Inf)

    def checkFileExists(id: String, documentType: String): Boolean = Await.result(getIDAndDocumentType(id, documentType), Duration.Inf)

    def checkFileNameExists(id: String, fileName: String): Boolean = Await.result(checkByIdAndFileName(id = id, fileName = fileName), Duration.Inf)

    def checkAllAssetFilesVerified(id: String): Boolean = {
      val documentStatuses = Await.result(getStatusForAllDocumentsById(id), Duration.Inf)
      if (documentStatuses.nonEmpty) documentStatuses.forall(status => status.getOrElse(false)) else false
    }
  }

}