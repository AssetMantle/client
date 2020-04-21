package models.masterTransaction

import java.sql.Timestamp
import models.common.Serializable._
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.NegotiationDocumentContent
import models.Trait.{Document, Logged}
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class NegotiationFile(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], documentContent: Option[NegotiationDocumentContent] = None, status: Option[Boolean] = None, createdBy: String, createdOn: Timestamp, createdOnTimezone: String, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Document with Logged

@Singleton
class NegotiationFiles @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_NEGOTIATION_FILE

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private val nodeID = configuration.get[String]("node.id")

  private val nodeTimezone = configuration.get[String]("node.timezone")

  case class NegotiationFileSerialized(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], documentContentJson: Option[String] = None, status: Option[Boolean], createdBy: String, createdOn: Timestamp, createdOnTimezone: String, updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: NegotiationFile =
      documentContentJson match {
        case Some(content) => NegotiationFile(id = id, documentType = documentType, fileName = fileName, file = file, documentContent = Option(utilities.JSON.convertJsonStringToObject[NegotiationDocumentContent](content)), status = status, createdBy = createdBy, createdOn = createdOn, createdOnTimezone = createdOnTimezone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
        case None => NegotiationFile(id = id, documentType = documentType, fileName = fileName, file = file, documentContent = None, status = status, createdBy = createdBy, createdOn = createdOn, createdOnTimezone = createdOnTimezone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
      }

  }

  private def serialize(negotiationFile: NegotiationFile): NegotiationFileSerialized = {
    negotiationFile.documentContent match {
      case Some(content) => NegotiationFileSerialized(id = negotiationFile.id, documentType = negotiationFile.documentType, fileName = negotiationFile.fileName, file = negotiationFile.file, documentContentJson = Option(Json.toJson(content).toString), status = negotiationFile.status, createdBy = negotiationFile.createdBy, createdOn = negotiationFile.createdOn, createdOnTimezone = negotiationFile.createdOnTimezone, updatedBy = negotiationFile.updatedBy, updatedOn = negotiationFile.updatedOn, updatedOnTimeZone = negotiationFile.updatedOnTimeZone)
      case None => NegotiationFileSerialized(id = negotiationFile.id, documentType = negotiationFile.documentType, fileName = negotiationFile.fileName, file = negotiationFile.file, documentContentJson = None, negotiationFile.status, createdBy = negotiationFile.createdBy, createdOn = negotiationFile.createdOn, createdOnTimezone = negotiationFile.createdOnTimezone, updatedBy = negotiationFile.updatedBy, updatedOn = negotiationFile.updatedOn, updatedOnTimeZone = negotiationFile.updatedOnTimeZone)
    }
  }

  private[models] val negotiationFileTable = TableQuery[NegotiationFileTable]

  private def add(id: String, documentType: String, fileName: String, file: Option[Array[Byte]]): Future[String] = db.run((negotiationFileTable returning negotiationFileTable.map(_.id) += serialize(NegotiationFile(id = id, documentType = documentType, fileName = fileName, file = file, createdBy = nodeID, createdOn = new Timestamp(System.currentTimeMillis()), createdOnTimezone = nodeTimezone))).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def update(id: String, documentType: String, fileName: String, file: Option[Array[Byte]]): Future[Int] = db.run(negotiationFileTable.filter(_.id === id).filter(_.documentType === documentType).map(x => (x.fileName, x.file.?, x.updatedBy, x.updatedOn, x.updatedOnTimezone)).update((fileName, file, nodeID, new Timestamp(System.currentTimeMillis()), nodeTimezone)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateDocumentContentByIDAndDocumentType(id: String, documentType: String, documentContentJson: Option[String]): Future[Int] = db.run(negotiationFileTable.map(x => (x.id, x.documentType, x.documentContentJson.?, x.updatedBy, x.updatedOn, x.updatedOnTimezone)).update((id, documentType, documentContentJson, nodeID, new Timestamp(System.currentTimeMillis()), nodeTimezone)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusByIDAndDocumentType(id: String, documentType: String, status: Option[Boolean]): Future[Int] = db.run(negotiationFileTable.filter(_.id === id).filter(_.documentType === documentType).map(x => (x.status.?, x.updatedBy, x.updatedOn, x.updatedOnTimezone)).update((status, nodeID, new Timestamp(System.currentTimeMillis()), nodeTimezone)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def tryGetByIDAndDocumentType(id: String, documentType: String): Future[NegotiationFileSerialized] = db.run(negotiationFileTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getByIDAndDocumentType(id: String, documentType: String): Future[Option[NegotiationFileSerialized]] = db.run(negotiationFileTable.filter(_.id === id).filter(_.documentType === documentType).result.headOption)

  private def getFileNameByIdDocumentType(id: String, documentType: String): Future[String] = db.run(negotiationFileTable.filter(_.id === id).filter(_.documentType === documentType).map(_.fileName).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAllDocumentsById(id: String): Future[Seq[NegotiationFileSerialized]] = db.run(negotiationFileTable.filter(_.id === id).result)

  private def getByIDAndDocumentTypes(id: String, documentTypes: Seq[String]): Future[Seq[NegotiationFileSerialized]] = db.run(negotiationFileTable.filter(_.id === id).filter(_.documentType inSet documentTypes).result)

  private def deleteById(id: String) = db.run(negotiationFileTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getIDAndDocumentType(id: String, documentType: String): Future[Boolean] = db.run(negotiationFileTable.filter(_.id === id).filter(_.documentType === documentType).exists.result)

  private def checkByIdAndFileName(id: String, fileName: String): Future[Boolean] = db.run(negotiationFileTable.filter(_.id === id).filter(_.fileName === fileName).exists.result)

  private[models] class NegotiationFileTable(tag: Tag) extends Table[NegotiationFileSerialized](tag, "NegotiationFile") {

    def * = (id, documentType, fileName, file.?, documentContentJson.?, status.?, createdBy, createdOn, createdOnTimezone, updatedBy.?, updatedOn.?, updatedOnTimezone.?) <> (NegotiationFileSerialized.tupled, NegotiationFileSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def fileName = column[String]("fileName", O.Unique)

    def file = column[Array[Byte]]("file")

    def documentContentJson = column[String]("documentContentJson")

    def status = column[Boolean]("status")

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

    def tryGet(id: String, documentType: String): Future[NegotiationFile] = tryGetByIDAndDocumentType(id = id, documentType = documentType).map(_.deserialize)

    def get(id: String, documentType: String): Future[Option[NegotiationFile]] = getByIDAndDocumentType(id = id, documentType = documentType).map(_.map(_.deserialize))

    def getConfirmBidDocuments(id: String): Future[Seq[NegotiationFile]] = getByIDAndDocumentTypes(id = id, documentTypes = Seq(constants.File.BUYER_CONTRACT, constants.File.SELLER_CONTRACT)).map(_.map(_.deserialize))

    def updateDocumentContent(id: String, documentType: String, documentContent: NegotiationDocumentContent): Future[Int] = updateDocumentContentByIDAndDocumentType(id = id, documentType = documentType, documentContentJson = Option(Json.toJson(documentContent).toString))

    def accept(id: String, documentType: String): Future[Int] = updateStatusByIDAndDocumentType(id = id, documentType = documentType, status = Option(true))

    def reject(id: String, documentType: String): Future[Int] = updateStatusByIDAndDocumentType(id = id, documentType = documentType, status = Option(false))

    def getFileName(id: String, documentType: String): Future[String] = getFileNameByIdDocumentType(id = id, documentType = documentType)

    def getAllDocuments(id: String): Future[Seq[NegotiationFile]] = getAllDocumentsById(id = id).map(_.map(_.deserialize))

    def getDocuments(id: String, documentTypes: Seq[String]): Future[Seq[NegotiationFile]] = getByIDAndDocumentTypes(id = id, documentTypes = documentTypes).map(_.map(_.deserialize))

    def deleteAllDocuments(id: String): Future[Int] = deleteById(id = id)

    def checkFileExists(id: String, documentType: String): Future[Boolean] = getIDAndDocumentType(id, documentType)

    def checkFileNameExists(id: String, fileName: String): Future[Boolean] = checkByIdAndFileName(id = id, fileName = fileName)
  }

}