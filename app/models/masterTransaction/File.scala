package models.masterTransaction

import java.sql.Date

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{Json, OWrites, Reads}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class File(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], context:Option[String], status: Option[Boolean])

object FileTypeContext {
  case class OBL(billOfLadingId: String, portOfLoading: String, shipperName: String, shipperAddress: String, notifyPartyName: String, notifyPartyAddress: String, dateOfShipping: Date, deliveryTerm: String, weightOfConsignment: Int, declaredAssetValue: Int)

  implicit val oblReads: Reads[OBL] = Json.reads[OBL]
  implicit val oblWrites: OWrites[OBL] = Json.writes[OBL]
}

@Singleton
class Files @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ACCOUNT_FILE

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val fileTable = TableQuery[FileTable]

  private def add(file: File): Future[String] = db.run((fileTable returning fileTable.map(_.id) += file).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(file: File): Future[Int] = db.run(fileTable.insertOrUpdate(file).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def findByIdDocumentType(id: String, documentType: String): Future[File] = db.run(fileTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
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

  private def getAllDocumentsById(id: String): Future[Seq[File]] = db.run(fileTable.filter(_.id === id).result)

  private def deleteById(id: String) = db.run(fileTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def checkByIdAndDocumentType(id: String, documentType: String): Future[Boolean] = db.run(fileTable.filter(_.id === id).filter(_.documentType === documentType).exists.result)

  private[models] class FileTable(tag: Tag) extends Table[File](tag, "File") {

    def * = (id, documentType, fileName, file.?, context.?,status.?) <> (File.tupled, File.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("type", O.PrimaryKey)

    def fileName = column[String]("fileName")

    def file = column[Array[Byte]]("file")

    def context = column[String]("context")

    def status = column[Boolean]("status")
  }

  object Service {

    def create(id: String, documentType: String, fileName: String,file: Option[Array[Byte]], context: Option[String], status: Option[Boolean]): String = Await.result(add(File(id = id, documentType = documentType, fileName = fileName, file = file, context = context,status = status)), Duration.Inf)

    def updateOldDocument(id: String, documentType: String, fileName: String, context: Option[String], file: Option[Array[Byte]], status: Option[Boolean]): Int = Await.result(upsert(File(id = id, documentType = documentType, fileName = fileName, file = file, context = context, status = status)), Duration.Inf)

    def get(id: String, documentType: String): File = Await.result(findByIdDocumentType(id = id, documentType = documentType), Duration.Inf)

    def getAllDocuments(id: String): Seq[File] = Await.result(getAllDocumentsById(id = id), Duration.Inf)

    def deleteAllDocuments(id: String): Int = Await.result(deleteById(id = id), Duration.Inf)

    def checkFileExists(id: String, documentType: String): Boolean = Await.result(checkByIdAndDocumentType(id = id, documentType = documentType), Duration.Inf)

    def getProfilePicture(id: String): Array[Byte] = Await.result(getFileByIdDocumentType(id = id, documentType = constants.File.PROFILE_PICTURE), Duration.Inf)
  }

}