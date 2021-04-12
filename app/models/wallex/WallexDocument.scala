package models.wallex

import exceptions.BaseException
import models.Trait.{Document, Logged}
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class WallexDocument(
    id: String,
    documentType: String,
    fileName: String,
    file: Option[Array[Byte]],
    status: Option[Boolean] = None,
    createdBy: Option[String] = None,
    createdOn: Option[Timestamp] = None,
    createdOnTimeZone: Option[String] = None,
    updatedBy: Option[String] = None,
    updatedOn: Option[Timestamp] = None,
    updatedOnTimeZone: Option[String] = None
) extends Document[WallexDocument]
    with Logged {

  def updateFileName(newFileName: String): WallexDocument =
    copy(fileName = newFileName)

  def updateFile(newFile: Option[Array[Byte]]): WallexDocument =
    copy(file = newFile)

  def updateStatus(status: Option[Boolean]): WallexDocument =
    copy(status = status)
}

@Singleton
class WallexDocuments @Inject() (
    protected val databaseConfigProvider: DatabaseConfigProvider,
    configuration: Configuration
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.WALLEX_DOCUMENT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val wallexDocumentTable = TableQuery[WallexDocumentTable]

  private def add(wallexDocument: WallexDocument): Future[String] =
    db.run(
        (wallexDocumentTable returning wallexDocumentTable
          .map(_.id) += wallexDocument).asTry
      )
      .map {
        case Success(result) => result
        case Failure(exception) =>
          exception match {
            case psqlException: PSQLException =>
              throw new BaseException(
                constants.Response.PSQL_EXCEPTION,
                psqlException
              )
          }
      }

  private def getByIDAndDocumentType(
      id: String,
      documentType: String
  ): Future[Option[WallexDocument]] =
    db.run(
      wallexDocumentTable
        .filter(_.id === id)
        .filter(_.documentType === documentType)
        .result
        .headOption
    )

  private def checkByIdAndFileName(
      id: String,
      fileName: String
  ): Future[Boolean] =
    db.run(
      wallexDocumentTable
        .filter(_.id === id)
        .filter(_.fileName === fileName)
        .exists
        .result
    )

  private def tryGetIDAndDocumentType(
      id: String,
      documentType: String
  ): Future[WallexDocument] =
    db.run(
        wallexDocumentTable
          .filter(_.id === id)
          .filter(_.documentType === documentType)
          .result
          .head
          .asTry
      )
      .map {
        case Success(result) => result
        case Failure(exception) =>
          exception match {
            case noSuchElementException: NoSuchElementException =>
              throw new BaseException(
                constants.Response.NO_SUCH_ELEMENT_EXCEPTION,
                noSuchElementException
              )
          }
      }

  private def update(wallexDocument: WallexDocument): Future[Int] =
    db.run(
        wallexDocumentTable
          .filter(_.id === wallexDocument.id)
          .filter(_.documentType === wallexDocument.documentType)
          .update(wallexDocument)
          .asTry
      )
      .map {
        case Success(result) => result
        case Failure(exception) =>
          exception match {
            case psqlException: PSQLException =>
              throw new BaseException(
                constants.Response.PSQL_EXCEPTION,
                psqlException
              )
            case noSuchElementException: NoSuchElementException =>
              throw new BaseException(
                constants.Response.NO_SUCH_ELEMENT_EXCEPTION,
                noSuchElementException
              )
          }
      }

  private def getAllDocumentsById(id: String): Future[Seq[WallexDocument]] = db.run(wallexDocumentTable.filter(_.id === id).result)


  private[models] class WallexDocumentTable(tag: Tag)
      extends Table[WallexDocument](tag, "WallexDocument") {

    override def * =
      (
        id,
        documentType,
        fileName,
        file.?,
        status.?,
        createdBy.?,
        createdOn.?,
        createdOnTimeZone.?,
        updatedBy.?,
        updatedOn.?,
        updatedOnTimeZone.?
      ) <> (WallexDocument.tupled, WallexDocument.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def fileName = column[String]("fileName", O.Unique)

    def file = column[Array[Byte]]("file")

    def status = column[Boolean]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(wallexDocument: WallexDocument): Future[String] =
      add(wallexDocument)

    def get(id: String, documentType: String): Future[Option[WallexDocument]] =
      getByIDAndDocumentType(id = id, documentType = documentType)

    def checkFileNameExists(id: String, fileName: String): Future[Boolean] =
      checkByIdAndFileName(id = id, fileName = fileName)

    def tryGet(id: String, documentType: String): Future[WallexDocument] =
      tryGetIDAndDocumentType(id = id, documentType = documentType)

    def updateOldDocument(wallexDocument: WallexDocument): Future[Int] =
      update(wallexDocument)

    def getAllDocuments(id: String): Future[Seq[WallexDocument]] = getAllDocumentsById(id = id)

  }
}
