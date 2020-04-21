package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.{Document, Logged}
import models.common.Node
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class AccountFile(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimezone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Document[AccountFile] with Logged[AccountFile] {

  val status: Option[Boolean] = Option(true)

  def updateFileName(newFileName: String): AccountFile = copy(fileName = newFileName)

  def updateFile(newFile: Option[Array[Byte]]): AccountFile = copy(file = newFile)

  def createLog()(implicit node: Node): AccountFile = copy(createdBy = Option(node.id), createdOn = Option(new Timestamp(System.currentTimeMillis())), createdOnTimezone = Option(node.timeZone))

  def updateLog()(implicit node: Node): AccountFile = copy(updatedBy = Option(node.id), updatedOn = Option(new Timestamp(System.currentTimeMillis())), updatedOnTimeZone = Option(node.timeZone))

}

@Singleton
class AccountFiles @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ACCOUNT_FILE

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private implicit val node: Node = Node(id = configuration.get[String]("node.id"), timeZone = configuration.get[String]("node.timeZone"))

  private[models] val accountFileTable = TableQuery[AccountFileTable]

  private def add(accountFile: AccountFile): Future[String] = db.run((accountFileTable returning accountFileTable.map(_.id) += accountFile.createLog()).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def update(accountFile: AccountFile): Future[Int] = db.run(accountFileTable.filter(_.id === accountFile.id).filter(_.documentType === accountFile.documentType).update(accountFile.updateLog()).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByIdDocumentType(id: String, documentType: String): Future[AccountFile] = db.run(accountFileTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getFileNameByIdDocumentType(id: String, documentType: String): Future[String] = db.run(accountFileTable.filter(_.id === id).filter(_.documentType === documentType).map(_.fileName).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getFileByIdDocumentType(id: String, documentType: String): Future[Array[Byte]] = db.run(accountFileTable.filter(_.id === id).filter(_.documentType === documentType).map(_.file).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => documentType match {
        case constants.File.PROFILE_PICTURE => Array[Byte]()
        case _ => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      }
    }
  }

  private def getAllDocumentsById(id: String): Future[Seq[AccountFile]] = db.run(accountFileTable.filter(_.id === id).result)

  private def deleteById(id: String): Future[Int] = db.run(accountFileTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def checkByIdAndDocumentType(id: String, documentType: String): Future[Boolean] = db.run(accountFileTable.filter(_.id === id).filter(_.documentType === documentType).exists.result)

  private def checkByIdAndFileName(id: String, fileName: String): Future[Boolean] = db.run(accountFileTable.filter(_.id === id).filter(_.fileName === fileName).exists.result)

  private[models] class AccountFileTable(tag: Tag) extends Table[AccountFile](tag, "AccountFile") {

    def * = (id, documentType, fileName, file.?, createdBy.?, createdOn.?, createdOnTimezone.?, updatedBy.?, updatedOn.?, updatedOnTimezone.?) <> (AccountFile.tupled, AccountFile.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def fileName = column[String]("fileName")

    def file = column[Array[Byte]]("file")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimezone = column[String]("createdOnTimezone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimezone = column[String]("updatedOnTimezone")

  }

  object Service {

    def create(accountFile: AccountFile): Future[String] = add(accountFile)

    def updateOldDocument(accountFile: AccountFile): Future[Int] = update(accountFile)

    def get(id: String, documentType: String): Future[AccountFile] = findByIdDocumentType(id = id, documentType = documentType)

    def getFileName(id: String, documentType: String): Future[String] = getFileNameByIdDocumentType(id = id, documentType = documentType)

    def getAllDocuments(id: String): Future[Seq[AccountFile]] = getAllDocumentsById(id = id)

    def deleteAllDocuments(id: String): Future[Int] = deleteById(id = id)

    def checkFileExists(id: String, documentType: String): Future[Boolean] = checkByIdAndDocumentType(id = id, documentType = documentType)

    def getProfilePicture(id: String): Future[Array[Byte]] = getFileByIdDocumentType(id = id, documentType = constants.File.PROFILE_PICTURE)

    def checkFileNameExists(id: String, fileName: String): Future[Boolean] = checkByIdAndFileName(id = id, fileName = fileName)
  }

}