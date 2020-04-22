package models.masterTransaction

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.AssetDocumentContent
import models.Trait.{Document, Logged}
import models.common.Node
import models.common.Serializable._
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class AssetFile(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], documentContent: Option[AssetDocumentContent] = None, status: Option[Boolean] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Document[AssetFile] with Logged[AssetFile] {

  def updateFileName(newFileName: String): AssetFile = copy(fileName = newFileName)

  def updateFile(newFile: Option[Array[Byte]]): AssetFile = copy(file = newFile)

  def createLog()(implicit node: Node): AssetFile = copy(createdBy = Option(node.id), createdOn = Option(new Timestamp(System.currentTimeMillis())), createdOnTimeZone = Option(node.timeZone))

  def updateLog()(implicit node: Node): AssetFile = copy(updatedBy = Option(node.id), updatedOn = Option(new Timestamp(System.currentTimeMillis())), updatedOnTimeZone = Option(node.timeZone))

}

@Singleton
class AssetFiles @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_ASSET_FILE

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val node: Node = Node(id = configuration.get[String]("node.id"), timeZone = configuration.get[String]("node.timeZone"))

  private[models] val assetFileTable = TableQuery[AssetFileTable]

  case class AssetFileSerialized(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], documentContentJson: Option[String] = None, status: Option[Boolean], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: AssetFile =
      documentContentJson match {
        case Some(content) => AssetFile(id = id, documentType = documentType, fileName = fileName, file = file, documentContent = Option(utilities.JSON.convertJsonStringToObject[AssetDocumentContent](content)), status = status, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
        case None => AssetFile(id = id, documentType = documentType, fileName = fileName, file = file, documentContent = None, status = status, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
      }

  }

  private def serialize(assetFile: AssetFile): AssetFileSerialized = {
    assetFile.documentContent match {
      case Some(content) => AssetFileSerialized(id = assetFile.id, documentType = assetFile.documentType, fileName = assetFile.fileName, file = assetFile.file, documentContentJson = Option(Json.toJson(content).toString), status = assetFile.status, createdBy = assetFile.createdBy, createdOn = assetFile.createdOn, createdOnTimeZone = assetFile.createdOnTimeZone, updatedBy = assetFile.updatedBy, updatedOn = assetFile.updatedOn, updatedOnTimeZone = assetFile.updatedOnTimeZone)
      case None => AssetFileSerialized(id = assetFile.id, documentType = assetFile.documentType, fileName = assetFile.fileName, file = assetFile.file, documentContentJson = None, assetFile.status, createdBy = assetFile.createdBy, createdOn = assetFile.createdOn, createdOnTimeZone = assetFile.createdOnTimeZone, updatedBy = assetFile.updatedBy, updatedOn = assetFile.updatedOn, updatedOnTimeZone = assetFile.updatedOnTimeZone)
    }
  }

  import databaseConfig.profile.api._

  private def add(assetFile: AssetFile): Future[String] = db.run((assetFileTable returning assetFileTable.map(_.id) += serialize(assetFile.createLog())).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def update(assetFile: AssetFile): Future[Int] = db.run(assetFileTable.filter(_.id === assetFile.id).filter(_.documentType === assetFile.documentType).update(serialize(assetFile.updateLog())).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateDocumentContentByIDAndDocumentType(id: String, documentType: String, documentContentJson: Option[String]): Future[Int] = db.run(assetFileTable.filter(_.id === id).filter(_.documentType === documentType).map(x => (x.documentContentJson.?, x.updatedBy, x.updatedOn, x.updatedOnTimeZone)).update(documentContentJson, node.id, new Timestamp(System.currentTimeMillis()), node.timeZone).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusByIDAndDocumentType(id: String, documentType: String, status: Option[Boolean]): Future[Int] = db.run(assetFileTable.filter(_.id === id).filter(_.documentType === documentType).map(x => (x.status.?, x.updatedBy, x.updatedOn, x.updatedOnTimeZone)).update((status, node.id, new Timestamp(System.currentTimeMillis()), node.timeZone)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def tryGetByIDAndDocumentType(id: String, documentType: String): Future[AssetFileSerialized] = db.run(assetFileTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getByIDAndDocumentType(id: String, documentType: String): Future[Option[AssetFileSerialized]] = db.run(assetFileTable.filter(_.id === id).filter(_.documentType === documentType).result.headOption)

  private def tryGetFileNameByIDAndDocumentType(id: String, documentType: String): Future[String] = db.run(assetFileTable.filter(_.id === id).filter(_.documentType === documentType).map(_.fileName).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getStatusForAllDocumentsById(id: String): Future[Seq[Option[Boolean]]] = db.run(assetFileTable.filter(_.id === id).map(_.status.?).result)

  private def getAllDocumentsById(id: String): Future[Seq[AssetFileSerialized]] = db.run(assetFileTable.filter(_.id === id).result)

  private def getAllDocumentsByIds(ids: Seq[String]): Future[Seq[AssetFileSerialized]] = db.run(assetFileTable.filter(_.id inSet ids).result)

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

    def * = (id, documentType, fileName, file.?, documentContentJson.?, status.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (AssetFileSerialized.tupled, AssetFileSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def fileName = column[String]("fileName", O.Unique)

    def file = column[Array[Byte]]("file")

    def documentContentJson = column[String]("documentContentJson")

    def status = column[Boolean]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(assetFile: AssetFile): Future[String] = add(assetFile)

    def updateOldDocument(assetFile: AssetFile): Future[Int] = update(assetFile)

    def get(id: String, documentType: String): Future[Option[AssetFile]] = getByIDAndDocumentType(id = id, documentType = documentType).map(_.map(_.deserialize))

    def tryGet(id: String, documentType: String): Future[AssetFile] = tryGetByIDAndDocumentType(id = id, documentType = documentType).map(_.deserialize)

    def updateDocumentContent(id: String, documentType: String, documentContent: AssetDocumentContent): Future[Int] = updateDocumentContentByIDAndDocumentType(id = id, documentType = documentType, documentContentJson = Option(Json.toJson(documentContent).toString))

    def accept(id: String, documentType: String): Future[Int] = updateStatusByIDAndDocumentType(id = id, documentType = documentType, status = Option(true))

    def reject(id: String, documentType: String): Future[Int] = updateStatusByIDAndDocumentType(id = id, documentType = documentType, status = Option(false))

    def tryGetFileName(id: String, documentType: String): Future[String] = tryGetFileNameByIDAndDocumentType(id = id, documentType = documentType)

    def getAllDocuments(id: String): Future[Seq[AssetFile]] = getAllDocumentsById(id = id).map {
      _.map(_.deserialize)
    }

    def getAllDocumentsForAllAssets(ids: Seq[String]): Future[Seq[AssetFile]] = getAllDocumentsByIds(ids = ids).map(_.map(_.deserialize))

    def getDocuments(id: String, documents: Seq[String]): Future[Seq[AssetFile]] = getDocumentsByID(id, documents).map { documents => documents.map(_.deserialize) }

    def deleteAllDocuments(id: String): Future[Int] = deleteById(id = id)

    def checkFileExists(id: String, documentType: String): Future[Boolean] = getIDAndDocumentType(id, documentType)

    def checkFileNameExists(id: String, fileName: String): Future[Boolean] = checkByIdAndFileName(id = id, fileName = fileName)

    def checkAllAssetFilesVerified(id: String): Future[Boolean] = {
      getStatusForAllDocumentsById(id).map { documentStatuses => if (documentStatuses.nonEmpty) documentStatuses.forall(status => status.getOrElse(false)) else false }
    }
  }

}