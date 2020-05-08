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

case class OrganizationBackgroundCheck(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], status: Option[Boolean] = Option(true), createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Document[OrganizationBackgroundCheck] with Logged[OrganizationBackgroundCheck] {

  def updateFileName(newFileName: String): OrganizationBackgroundCheck = copy(fileName = newFileName)

  def updateFile(newFile: Option[Array[Byte]]): OrganizationBackgroundCheck = copy(file = newFile)

  def createLog()(implicit node: Node): OrganizationBackgroundCheck = copy(createdBy = Option(node.id), createdOn = Option(new Timestamp(System.currentTimeMillis())), createdOnTimeZone = Option(node.timeZone))

  def updateLog()(implicit node: Node): OrganizationBackgroundCheck = copy(updatedBy = Option(node.id), updatedOn = Option(new Timestamp(System.currentTimeMillis())), updatedOnTimeZone = Option(node.timeZone))

}

@Singleton
class OrganizationBackgroundChecks @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ORGANIZATION_BACKGROUND_CHECK

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val node: Node = Node(id = configuration.get[String]("node.id"), timeZone = configuration.get[String]("node.timeZone"))

  import databaseConfig.profile.api._

  private[models] val organizationBackgroundCheckTable = TableQuery[OrganizationBackgroundCheckTable]

  private def add(organizationBackgroundCheck: OrganizationBackgroundCheck): Future[String] = db.run((organizationBackgroundCheckTable returning organizationBackgroundCheckTable.map(_.id) += organizationBackgroundCheck.createLog()).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def update(organizationBackgroundCheck: OrganizationBackgroundCheck): Future[Int] = db.run(organizationBackgroundCheckTable.filter(_.id === organizationBackgroundCheck.id).filter(_.documentType === organizationBackgroundCheck.documentType).update(organizationBackgroundCheck.updateLog()).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def tryGetByIDAndDocumentType(id: String, documentType: String): Future[OrganizationBackgroundCheck] = db.run(organizationBackgroundCheckTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def tryGetFileNameByIdDocumentType(id: String, documentType: String): Future[String] = db.run(organizationBackgroundCheckTable.filter(_.id === id).filter(_.documentType === documentType).map(_.fileName).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAllDocumentsById(id: String): Future[Seq[OrganizationBackgroundCheck]] = db.run(organizationBackgroundCheckTable.filter(_.id === id).result)

  private def getAllDocumentTypesByIDStatusAndDocumentSet(id: String, documentTypes: Seq[String], status: Boolean): Future[Seq[String]] = db.run(organizationBackgroundCheckTable.filter(_.id === id).filter(_.documentType.inSet(documentTypes)).filter(_.status === status).map(_.documentType).result)

  private[models] class OrganizationBackgroundCheckTable(tag: Tag) extends Table[OrganizationBackgroundCheck](tag, "OrganizationBackgroundCheck") {

    def * = (id, documentType, fileName, file.?, status.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (OrganizationBackgroundCheck.tupled, OrganizationBackgroundCheck.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def status = column[Boolean]("status")

    def fileName = column[String]("fileName", O.Unique)

    def file = column[Array[Byte]]("file")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(organizationBackgroundCheck: OrganizationBackgroundCheck): Future[String] = add(organizationBackgroundCheck)

    def updateOldDocument(organizationBackgroundCheck: OrganizationBackgroundCheck): Future[Int] = update(organizationBackgroundCheck)

    def tryGet(id: String, documentType: String): Future[OrganizationBackgroundCheck] = tryGetByIDAndDocumentType(id = id, documentType = documentType)

    def tryGetFileName(id: String, documentType: String): Future[String] = tryGetFileNameByIdDocumentType(id = id, documentType = documentType)

    def getAllDocuments(id: String): Future[Seq[OrganizationBackgroundCheck]] = getAllDocumentsById(id = id)

    def checkAllBackgroundFilesVerified(id: String): Future[Boolean] = getAllDocumentTypesByIDStatusAndDocumentSet(id = id, documentTypes = constants.File.ORGANIZATION_BACKGROUND_CHECK_DOCUMENT_TYPES, status = true).map {
      constants.File.ORGANIZATION_BACKGROUND_CHECK_DOCUMENT_TYPES.diff(_).isEmpty
    }
  }

}