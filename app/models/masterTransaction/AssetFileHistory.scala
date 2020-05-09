package models.masterTransaction

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.AssetDocumentContent
import models.Trait.{Document, Logged}
import models.common.Serializable._
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class AssetFileHistory(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], documentContent: Option[AssetDocumentContent] = None, status: Option[Boolean] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Document[AssetFileHistory] with Logged {

  def updateFileName(newFileName: String): AssetFileHistory = copy(fileName = newFileName)

  def updateFile(newFile: Option[Array[Byte]]): AssetFileHistory = copy(file = newFile)
}

@Singleton
class AssetFileHistories @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_ASSET_FILE_HISTORY

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private[models] val assetFileHistoryTable = TableQuery[AssetFileHistoryTable]

  case class AssetFileHistorySerialized(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], documentContentJson: Option[String] = None, status: Option[Boolean], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: AssetFileHistory = AssetFileHistory(id = id, documentType = documentType, fileName = fileName, file = file, documentContent = documentContentJson.map(content => utilities.JSON.convertJsonStringToObject[AssetDocumentContent](content)), status = status, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  private def serialize(assetFile: AssetFileHistory): AssetFileHistorySerialized = AssetFileHistorySerialized(id = assetFile.id, documentType = assetFile.documentType, fileName = assetFile.fileName, file = assetFile.file, assetFile.documentContent.map(content => Json.toJson(content).toString), status = assetFile.status, createdBy = assetFile.createdBy, createdOn = assetFile.createdOn, createdOnTimeZone = assetFile.createdOnTimeZone, updatedBy = assetFile.updatedBy, updatedOn = assetFile.updatedOn, updatedOnTimeZone = assetFile.updatedOnTimeZone)

  import databaseConfig.profile.api._

  private def tryGetByIDAndDocumentType(id: String, documentType: String): Future[AssetFileHistorySerialized] = db.run(assetFileHistoryTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getByIDAndDocumentType(id: String, documentType: String): Future[Option[AssetFileHistorySerialized]] = db.run(assetFileHistoryTable.filter(_.id === id).filter(_.documentType === documentType).result.headOption)

  private def tryGetFileNameByIDAndDocumentType(id: String, documentType: String): Future[String] = db.run(assetFileHistoryTable.filter(_.id === id).filter(_.documentType === documentType).map(_.fileName).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAllDocumentsById(id: String): Future[Seq[AssetFileHistorySerialized]] = db.run(assetFileHistoryTable.filter(_.id === id).result)

  private[models] class AssetFileHistoryTable(tag: Tag) extends Table[AssetFileHistorySerialized](tag, "AssetFile_History") {

    def * = (id, documentType, fileName, file.?, documentContentJson.?, status.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (AssetFileHistorySerialized.tupled, AssetFileHistorySerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def fileName = column[String]("fileName")

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

    def get(id: String, documentType: String): Future[Option[AssetFileHistory]] = getByIDAndDocumentType(id = id, documentType = documentType).map(_.map(_.deserialize))

    def tryGet(id: String, documentType: String): Future[AssetFileHistory] = tryGetByIDAndDocumentType(id = id, documentType = documentType).map(_.deserialize)

    def tryGetFileName(id: String, documentType: String): Future[String] = tryGetFileNameByIDAndDocumentType(id = id, documentType = documentType)

    def getAllDocuments(id: String): Future[Seq[AssetFileHistory]] = getAllDocumentsById(id = id).map(_.map(_.deserialize))

    def getDocumentContent(id: String, documentType: String): Future[Option[AssetDocumentContent]] = tryGetByIDAndDocumentType(id = id, documentType = documentType).map(_.deserialize).map(_.documentContent)
  }

}