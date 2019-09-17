package models.masterTransaction

import java.util.Date

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Document
import models.Trait.Context
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import models.common.Serializable
import play.api.libs.json.{JsValue, Json, OWrites, Reads, Writes}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

//case class AssetFile[T](id: String, documentType: String, fileName: String, file: Option[Array[Byte]], context: Option[T] , status: Option[Boolean]) extends Document[AssetFile[T]] {
//
//  def updateFile(newFile: Option[Array[Byte]]): AssetFile[T] = AssetFile[T](id = id, documentType = documentType, fileName = fileName, file = newFile, context = context, status = status)
//
//  def updateFileName(newFileName: String): AssetFile[T] = AssetFile[T](id = id, documentType = documentType, fileName = newFileName, file = file, context = context, status = status)
//}

case class AssetFile(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], context: Option[models.Trait.Context], status: Option[Boolean]) extends Document[AssetFile] {
  def updateFile(newFile: Option[Array[Byte]]): AssetFile = AssetFile(id = id, documentType = documentType, fileName = fileName, file = newFile, context = context, status = status)

  def updateFileName(newFileName: String): AssetFile = AssetFile(id = id, documentType = documentType, fileName = newFileName, file = file, context = context, status = status)
}


//case class AssetFile(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], context: Option[String], status: Option[Boolean]) extends Document[AssetFile] {
//  def updateFile(newFile: Option[Array[Byte]]): AssetFile = AssetFile(id = id, documentType = documentType, fileName = fileName, file = newFile, context = context, status = status)
//
//  def updateFileName(newFileName: String): AssetFile = AssetFile(id = id, documentType = documentType, fileName = newFileName, file = file, context = context, status = status)
//}


@Singleton
class AssetFiles @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_ASSET_FILE

  case class AssetFileSerialized(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], context: Option[String], status: Option[Boolean]) {
    def deserialize: AssetFile = documentType match {
      case constants.File.OBL => AssetFile(id, documentType, fileName, file, Option(utilities.JSON.convertJsonStringToObject[Serializable.OBL](context.getOrElse(Json.toJson(Serializable.OBL("", "", "", "", "", "", new Date, "", 0, 0)).toString))), status)
      case constants.File.INVOICE => AssetFile(id, documentType, fileName, file, Option(utilities.JSON.convertJsonStringToObject[Serializable.Invoice](context.getOrElse(Json.toJson(Serializable.Invoice("", new Date)).toString))), status)
      case _ => AssetFile(id, documentType, fileName, file, None, status)
    }
  }

  implicit val modelWrites = new Writes[models.Trait.Context] {
    override def writes(o: models.Trait.Context): JsValue = o match {
      case u: Serializable.OBL => Json.toJson(u)
      case cl: Serializable.Invoice => Json.toJson(cl)
    }
  }
  private def serialize(assetFileTrait: AssetFile): AssetFileSerialized = AssetFileSerialized(assetFileTrait.id, assetFileTrait.documentType, assetFileTrait.fileName, assetFileTrait.file, if(assetFileTrait.context.isDefined) Option (helper(assetFileTrait.context.get).toString) else None, assetFileTrait.status)

  def helper(models: models.Trait.Context): Either[JsValue, String] =  Left(Json.toJson(models))

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

  private def upsertContext(file: AssetFileSerialized): Future[Int] = db.run(assetFileTable.map(x => (x.id, x.documentType, x.context.?)).insertOrUpdate(file.id, file.documentType, file.context).asTry).map {
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

    def * = (id, documentType, fileName, file.?, context.?, status.?) <> (AssetFileSerialized.tupled, AssetFileSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def fileName = column[String]("fileName", O.Unique)

    def file = column[Array[Byte]]("file")

    def context = column[String]("context")

    def status = column[Boolean]("status")
  }

  object Service {

    def create(file: AssetFile): String = Await.result(add(serialize(AssetFile(id = file.id, documentType = file.documentType, fileName = file.fileName, file = file.file, context = None, status = None))), Duration.Inf)

    def getOrEmpty(id: String, documentType: String): AssetFile = Await.result(findByIdDocumentType(id = id, documentType = documentType), Duration.Inf).getOrElse(AssetFileSerialized("","","",None,None,None)).deserialize

    def getOrNone(id: String, documentType: String): Option[AssetFile] = {
        val assetFile = Await.result(findByIdDocumentType(id = id, documentType = documentType), Duration.Inf)
        if(assetFile.isDefined) Option(assetFile.get.deserialize) else None
    }

    def get(id: String, documentType: String): AssetFile = Await.result(findByIdDocumentType(id = id, documentType = documentType), Duration.Inf).getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)).deserialize

    def insertOrUpdateOldDocument(file: AssetFile): Int = Await.result(upsertFile(AssetFile(id = file.id, documentType = file.documentType, fileName = file.fileName, file = file.file, context = None, status = None)), Duration.Inf)

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