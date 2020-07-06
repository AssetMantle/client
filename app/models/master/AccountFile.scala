package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.{Document, Logged}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class AccountFile(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Document[AccountFile] with Logged {

  val status: Option[Boolean] = Option(true)

  def updateFileName(newFileName: String): AccountFile = copy(fileName = newFileName)

  def updateFile(newFile: Option[Array[Byte]]): AccountFile = copy(file = newFile)

  def updateStatus(status: Option[Boolean]) = ???

}

@Singleton
class AccountFiles @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ACCOUNT_FILE

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val accountFileTable = TableQuery[AccountFileTable]

  private def add(accountFile: AccountFile): Future[String] = db.run((accountFileTable returning accountFileTable.map(_.id) += accountFile).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def update(accountFile: AccountFile): Future[Int] = db.run(accountFileTable.filter(_.id === accountFile.id).filter(_.documentType === accountFile.documentType).update(accountFile).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def tryGetByIDAndDocumentType(id: String, documentType: String): Future[AccountFile] = db.run(accountFileTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def tryGetFileNameByIDAndDocumentType(id: String, documentType: String): Future[String] = db.run(accountFileTable.filter(_.id === id).filter(_.documentType === documentType).map(_.fileName).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getFileByIdDocumentType(id: String, documentType: String): Future[Array[Byte]] = db.run(accountFileTable.filter(_.id === id).filter(_.documentType === documentType).map(_.file).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => documentType match {
        case constants.File.Account.PROFILE_PICTURE => Array[Byte]()
        case _ => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
      }
    }
  }

  private def getAllDocumentsById(id: String): Future[Seq[AccountFile]] = db.run(accountFileTable.filter(_.id === id).result)

  private def deleteById(id: String): Future[Int] = db.run(accountFileTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def checkByIdAndDocumentType(id: String, documentType: String): Future[Boolean] = db.run(accountFileTable.filter(_.id === id).filter(_.documentType === documentType).exists.result)

  private def checkByIdAndFileName(id: String, fileName: String): Future[Boolean] = db.run(accountFileTable.filter(_.id === id).filter(_.fileName === fileName).exists.result)

  private[models] class AccountFileTable(tag: Tag) extends Table[AccountFile](tag, "AccountFile") {

    def * = (id, documentType, fileName, file.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (AccountFile.tupled, AccountFile.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def fileName = column[String]("fileName")

    def file = column[Array[Byte]]("file")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(accountFile: AccountFile): Future[String] = add(accountFile)

    def updateOldDocument(accountFile: AccountFile): Future[Int] = update(accountFile)

    def tryGet(id: String, documentType: String): Future[AccountFile] = tryGetByIDAndDocumentType(id = id, documentType = documentType)

    def tryGetFileName(id: String, documentType: String): Future[String] = tryGetFileNameByIDAndDocumentType(id = id, documentType = documentType)

    def getAllDocuments(id: String): Future[Seq[AccountFile]] = getAllDocumentsById(id = id)

    def deleteAllDocuments(id: String): Future[Int] = deleteById(id = id)

    def checkFileExists(id: String, documentType: String): Future[Boolean] = checkByIdAndDocumentType(id = id, documentType = documentType)

    def getProfilePicture(id: String): Future[Array[Byte]] = getFileByIdDocumentType(id = id, documentType = constants.File.Account.PROFILE_PICTURE)

    def checkFileNameExists(id: String, fileName: String): Future[Boolean] = checkByIdAndFileName(id = id, fileName = fileName)
  }

}