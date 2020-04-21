package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.{Document, Logged}
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import models.common.Node
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class AccountKYC(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], status: Option[Boolean] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimezone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Document[AccountKYC] with Logged[AccountKYC] {

  def updateFileName(newFileName: String): AccountKYC = copy(fileName = newFileName)

  def updateFile(newFile: Option[Array[Byte]]): AccountKYC = copy(file = newFile)

  def createLog()(implicit node: Node): AccountKYC = copy(createdBy = Option(node.id), createdOn = Option(new Timestamp(System.currentTimeMillis())), createdOnTimezone = Option(node.timeZone))

  def updateLog()(implicit node: Node): AccountKYC = copy(updatedBy = Option(node.id), updatedOn = Option(new Timestamp(System.currentTimeMillis())), updatedOnTimeZone = Option(node.timeZone))
}

@Singleton
class AccountKYCs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ACCOUNT_KYC

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private implicit val node: Node = Node(id = configuration.get[String]("node.id"), timeZone = configuration.get[String]("node.timeZone"))

  private[models] val accountKYCTable = TableQuery[AccountKYCTable]

  private def add(accountKYC: AccountKYC): Future[String] = db.run((accountKYCTable returning accountKYCTable.map(_.id) += accountKYC.createLog()).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def update(accountKYC: AccountKYC): Future[Int] = db.run(accountKYCTable.filter(_.id === accountKYC.id).filter(_.documentType === accountKYC.documentType).update(accountKYC.updateLog()).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def tryGetIDAndDocumentType(id: String, documentType: String): Future[AccountKYC] = db.run(accountKYCTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getByIDAndDocumentType(id: String, documentType: String): Future[Option[AccountKYC]] = db.run(accountKYCTable.filter(_.id === id).filter(_.documentType === documentType).result.headOption)

  private def tryGetFileNameByIdDocumentType(id: String, documentType: String): Future[String] = db.run(accountKYCTable.filter(_.id === id).filter(_.documentType === documentType).map(_.fileName).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusByIdAndDocumentType(id: String, documentType: String, status: Option[Boolean]): Future[Int] = db.run(accountKYCTable.filter(_.id === id).filter(_.documentType === documentType).map(x => (x.status.?, x.updatedBy, x.updatedOn, x.updatedOnTimezone)).update((status, node.id, new Timestamp(System.currentTimeMillis()), node.timeZone)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAllDocumentsById(id: String): Future[Seq[AccountKYC]] = db.run(accountKYCTable.filter(_.id === id).result)

  private def deleteById(id: String) = db.run(accountKYCTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def checkFileExistsByIdAndDocumentType(id: String, documentType: String): Future[Boolean] = db.run(accountKYCTable.filter(_.id === id).filter(_.documentType === documentType).exists.result)

  private def checkByIdAndFileName(id: String, fileName: String): Future[Boolean] = db.run(accountKYCTable.filter(_.id === id).filter(_.fileName === fileName).exists.result)

  private[models] class AccountKYCTable(tag: Tag) extends Table[AccountKYC](tag, "AccountKYC") {

    def * = (id, documentType, fileName, file.?, status.?, createdBy.?, createdOn.?, createdOnTimezone.?, updatedBy.?, updatedOn.?, updatedOnTimezone.?) <> (AccountKYC.tupled, AccountKYC.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def status = column[Boolean]("status")

    def fileName = column[String]("fileName", O.Unique)

    def file = column[Array[Byte]]("file")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimezone = column[String]("createdOnTimezone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimezone = column[String]("updatedOnTimezone")

  }

  object Service {

    def create(accountKYC: AccountKYC): Future[String] = add(accountKYC)

    def updateOldDocument(accountKYC: AccountKYC): Future[Int] = update(accountKYC)

    def get(id: String, documentType: String): Future[Option[AccountKYC]] = getByIDAndDocumentType(id = id, documentType = documentType)

    def tryGet(id: String, documentType: String): Future[AccountKYC] = tryGetIDAndDocumentType(id = id, documentType = documentType)

    def tryGetFileName(id: String, documentType: String): Future[String] = tryGetFileNameByIdDocumentType(id = id, documentType = documentType)

    def getAllDocuments(id: String): Future[Seq[AccountKYC]] = getAllDocumentsById(id = id)

    def verify(id: String, documentType: String): Future[Int] = updateStatusByIdAndDocumentType(id = id, documentType = documentType, status = Option(true))

    def deleteAllDocuments(id: String): Future[Int] = deleteById(id = id)

    def checkFileExists(id: String, documentType: String): Future[Boolean] = checkFileExistsByIdAndDocumentType(id = id, documentType = documentType)

    def checkFileNameExists(id: String, fileName: String): Future[Boolean] = checkByIdAndFileName(id = id, fileName = fileName)
  }

}