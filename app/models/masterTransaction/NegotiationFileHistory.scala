package models.masterTransaction

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.NegotiationDocumentContent
import models.Trait.{Document, HistoryLogged}
import models.common.Serializable._
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class NegotiationFileHistory(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], documentContent: Option[NegotiationDocumentContent] = None, status: Option[Boolean] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None, deletedBy: String, deletedOn: Timestamp, deletedOnTimeZone: String) extends Document[NegotiationFileHistory] with HistoryLogged {

  def updateFileName(newFileName: String): NegotiationFileHistory = copy(fileName = newFileName)

  def updateFile(newFile: Option[Array[Byte]]): NegotiationFileHistory = copy(file = newFile)

}

@Singleton
class NegotiationFileHistories @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_NEGOTIATION_FILE_HISTORY

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  case class NegotiationFileHistorySerialized(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], documentContentJson: Option[String], status: Option[Boolean], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String], deletedBy: String, deletedOn: Timestamp, deletedOnTimeZone: String) {
    def deserialize: NegotiationFileHistory = NegotiationFileHistory(id = id, documentType = documentType, fileName = fileName, file = file, documentContent = documentContentJson.map(content => utilities.JSON.convertJsonStringToObject[NegotiationDocumentContent](content)), status = status, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone, deletedBy = deletedBy, deletedOn = deletedOn, deletedOnTimeZone = deletedOnTimeZone)
  }

  private def serialize(negotiationFileHistory: NegotiationFileHistory): NegotiationFileHistorySerialized = NegotiationFileHistorySerialized(id = negotiationFileHistory.id, documentType = negotiationFileHistory.documentType, fileName = negotiationFileHistory.fileName, file = negotiationFileHistory.file, documentContentJson = negotiationFileHistory.documentContent.map(content => Json.toJson(content).toString), status = negotiationFileHistory.status, createdBy = negotiationFileHistory.createdBy, createdOn = negotiationFileHistory.createdOn, createdOnTimeZone = negotiationFileHistory.createdOnTimeZone, updatedBy = negotiationFileHistory.updatedBy, updatedOn = negotiationFileHistory.updatedOn, updatedOnTimeZone = negotiationFileHistory.updatedOnTimeZone, deletedBy = negotiationFileHistory.deletedBy, deletedOn = negotiationFileHistory.deletedOn, deletedOnTimeZone = negotiationFileHistory.deletedOnTimeZone)

  private[models] val negotiationFileHistoryTable = TableQuery[NegotiationFileHistoryTable]

  private def tryGetByIDAndDocumentType(id: String, documentType: String): Future[NegotiationFileHistorySerialized] = db.run(negotiationFileHistoryTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getByIDAndDocumentType(id: String, documentType: String): Future[Option[NegotiationFileHistorySerialized]] = db.run(negotiationFileHistoryTable.filter(_.id === id).filter(_.documentType === documentType).result.headOption)

  private def tryGetFileNameByIdDocumentType(id: String, documentType: String): Future[String] = db.run(negotiationFileHistoryTable.filter(_.id === id).filter(_.documentType === documentType).map(_.fileName).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getAllDocumentsById(id: String): Future[Seq[NegotiationFileHistorySerialized]] = db.run(negotiationFileHistoryTable.filter(_.id === id).result)

  private def getByIDAndDocumentTypes(id: String, documentTypes: Seq[String]): Future[Seq[NegotiationFileHistorySerialized]] = db.run(negotiationFileHistoryTable.filter(_.id === id).filter(_.documentType inSet documentTypes).result)

  private def getIDAndDocumentType(id: String, documentType: String): Future[Boolean] = db.run(negotiationFileHistoryTable.filter(_.id === id).filter(_.documentType === documentType).exists.result)

  private def checkByIdAndFileName(id: String, fileName: String): Future[Boolean] = db.run(negotiationFileHistoryTable.filter(_.id === id).filter(_.fileName === fileName).exists.result)

  private[models] class NegotiationFileHistoryTable(tag: Tag) extends Table[NegotiationFileHistorySerialized](tag, "NegotiationFile_History") {

    def * = (id, documentType, fileName, file.?, documentContentJson.?, status.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?, deletedBy, deletedOn, deletedOnTimeZone) <> (NegotiationFileHistorySerialized.tupled, NegotiationFileHistorySerialized.unapply)

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

    def deletedBy = column[String]("deletedBy")

    def deletedOn = column[Timestamp]("deletedOn")

    def deletedOnTimeZone = column[String]("deletedOnTimeZone")

  }

  object Service {

    def tryGet(id: String, documentType: String): Future[NegotiationFileHistory] = tryGetByIDAndDocumentType(id = id, documentType = documentType).map(_.deserialize)

    def get(id: String, documentType: String): Future[Option[NegotiationFileHistory]] = getByIDAndDocumentType(id = id, documentType = documentType).map(_.map(_.deserialize))

    def tryGetFileName(id: String, documentType: String): Future[String] = tryGetFileNameByIdDocumentType(id = id, documentType = documentType)

    def getAllDocuments(id: String): Future[Seq[NegotiationFileHistory]] = getAllDocumentsById(id = id).map(_.map(_.deserialize))

    def getDocuments(id: String, documentTypes: String*): Future[Seq[NegotiationFileHistory]] = getByIDAndDocumentTypes(id = id, documentTypes = documentTypes).map(_.map(_.deserialize))

    def checkFileExists(id: String, documentType: String): Future[Boolean] = getIDAndDocumentType(id, documentType)

    def checkFileNameExists(id: String, fileName: String): Future[Boolean] = checkByIdAndFileName(id = id, fileName = fileName)

    def getDocumentContent(id: String, documentType: String): Future[Option[NegotiationDocumentContent]] = tryGetByIDAndDocumentType(id = id, documentType = documentType).map(_.deserialize).map(_.documentContent)
  }

}