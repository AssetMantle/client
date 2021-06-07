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

case class UserKYC(
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
) extends Document[UserKYC]
    with Logged {

  def updateFileName(newFileName: String): UserKYC =
    copy(fileName = newFileName)

  def updateFile(newFile: Option[Array[Byte]]): UserKYC =
    copy(file = newFile)

  def updateStatus(status: Option[Boolean]): UserKYC =
    copy(status = status)
}

@Singleton
class UserKYCs @Inject()(
    protected val databaseConfigProvider: DatabaseConfigProvider,
    configuration: Configuration
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.WALLEX_DOCUMENT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val userKYCTable = TableQuery[UserKYCTable]

  private def add(userKYC: UserKYC): Future[String] =
    db.run(
        (userKYCTable returning userKYCTable
          .map(_.id) += userKYC).asTry
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
  ): Future[Option[UserKYC]] =
    db.run(
      userKYCTable
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
      userKYCTable
        .filter(_.id === id)
        .filter(_.fileName === fileName)
        .exists
        .result
    )

  private def tryGetIDAndDocumentType(
      id: String,
      documentType: String
  ): Future[UserKYC] =
    db.run(
        userKYCTable
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

  private def update(userKYC: UserKYC): Future[Int] =
    db.run(
        userKYCTable
          .filter(_.id === userKYC.id)
          .filter(_.documentType === userKYC.documentType)
          .update(userKYC)
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

  private def getAllDocumentsById(id: String): Future[Seq[UserKYC]] = db.run(userKYCTable.filter(_.id === id).result)


  private[models] class UserKYCTable(tag: Tag)
      extends Table[UserKYC](tag, "UserKYC") {

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
      ) <> (UserKYC.tupled, UserKYC.unapply)

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

    def create(wallexDocument: UserKYC): Future[String] =
      add(wallexDocument)

    def get(id: String, documentType: String): Future[Option[UserKYC]] =
      getByIDAndDocumentType(id = id, documentType = documentType)

    def checkFileNameExists(id: String, fileName: String): Future[Boolean] =
      checkByIdAndFileName(id = id, fileName = fileName)

    def tryGet(id: String, documentType: String): Future[UserKYC] =
      tryGetIDAndDocumentType(id = id, documentType = documentType)

    def updateOldDocument(wallexDocument: UserKYC): Future[Int] =
      update(wallexDocument)

    def getAllDocuments(id: String): Future[Seq[UserKYC]] = getAllDocumentsById(id = id)

  }
}