package models.master

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Document
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class TraderKYC(id: String, documentType: String, fileName: String, file: Option[Array[Byte]], zoneStatus: Option[Boolean], organizationStatus: Option[Boolean]) extends Document[TraderKYC] {

  val status: Option[Boolean] = Option(zoneStatus.getOrElse(false) && organizationStatus.getOrElse(false))

  def updateFileName(newFileName: String): TraderKYC = TraderKYC(id = id, documentType = documentType, zoneStatus = zoneStatus, organizationStatus = organizationStatus, fileName = newFileName, file = file)

  def updateFile(newFile: Option[Array[Byte]]): TraderKYC = TraderKYC(id = id, documentType = documentType, zoneStatus = zoneStatus, organizationStatus = organizationStatus, fileName = fileName, file = newFile)
}

@Singleton
class TraderKYCs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ORGANIZATION_KYC

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val traderKYCTable = TableQuery[TraderKYCTable]

  private def add(traderKYC: TraderKYC): Future[String] = db.run((traderKYCTable returning traderKYCTable.map(_.id) += traderKYC).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(traderKYC: TraderKYC): Future[Int] = db.run(traderKYCTable.insertOrUpdate(traderKYC).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  private def findByIdDocumentType(id: String, documentType: String): Future[TraderKYC] = db.run(traderKYCTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getFileNameByIdDocumentType(id: String, documentType: String): Future[String] = db.run(traderKYCTable.filter(_.id === id).filter(_.documentType === documentType).map(_.fileName).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def zoneUpdateStatusById(id: String, zoneStatus: Option[Boolean]): Future[Int] = db.run(traderKYCTable.filter(_.id === id).map(_.zoneStatus.?).update(zoneStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def zoneUpdateStatusByIdAndDocumentType(id: String, documentType: String, zoneStatus: Option[Boolean]): Future[Int] = db.run(traderKYCTable.filter(_.id === id).filter(_.documentType === documentType).map(_.zoneStatus.?).update(zoneStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def organizationUpdateStatusById(id: String, status: Option[Boolean]): Future[Int] = db.run(traderKYCTable.filter(_.id === id).map(_.organizationStatus.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def organizationUpdateStatusByIdAndDocumentType(id: String, documentType: String, status: Option[Boolean]): Future[Int] = db.run(traderKYCTable.filter(_.id === id).filter(_.documentType === documentType).map(_.organizationStatus.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAllDocumentsById(id: String): Future[Seq[TraderKYC]] = db.run(traderKYCTable.filter(_.id === id).result)

  private def deleteById(id: String) = db.run(traderKYCTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def checkByIdAndDocumentType(id: String, documentType: String): Future[Boolean] = db.run(traderKYCTable.filter(_.id === id).filter(_.documentType === documentType).exists.result)

  private def checkByIdAndFileName(id: String, fileName: String): Future[Boolean] = db.run(traderKYCTable.filter(_.id === id).filter(_.fileName === fileName).exists.result)

  private[models] class TraderKYCTable(tag: Tag) extends Table[TraderKYC](tag, "TraderKYC") {

    def * = (id, documentType, fileName, file.?, zoneStatus.?, organizationStatus.?) <> (TraderKYC.tupled, TraderKYC.unapply)
    
    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def zoneStatus = column[Boolean]("zoneStatus")

    def organizationStatus = column[Boolean]("organizationStatus")

    def fileName = column[String]("fileName")

    def file = column[Array[Byte]]("file")

  }

  object Service {

    def create(traderKYC: TraderKYC): String = Await.result(add(TraderKYC(id = traderKYC.id, documentType = traderKYC.documentType, fileName = traderKYC.fileName, file = traderKYC.file, zoneStatus = None, organizationStatus = None)), Duration.Inf)

    def updateOldDocument(traderKYC: TraderKYC): Int = Await.result(upsert(TraderKYC(id = traderKYC.id, documentType = traderKYC.documentType, fileName = traderKYC.fileName, file = traderKYC.file, zoneStatus = None, organizationStatus = None)), Duration.Inf)

    def get(id: String, documentType: String): TraderKYC = Await.result(findByIdDocumentType(id = id, documentType = documentType), Duration.Inf)

    def getFileName(id: String, documentType: String): String = Await.result(getFileNameByIdDocumentType(id = id, documentType = documentType), Duration.Inf)

    def getAllDocuments(id: String): Seq[TraderKYC] = Await.result(getAllDocumentsById(id = id), Duration.Inf)

    def zoneVerifyAll(id: String): Int = Await.result(zoneUpdateStatusById(id = id, zoneStatus = Option(true)), Duration.Inf)

    def zoneVerify(id: String, documentType: String): Int = Await.result(zoneUpdateStatusByIdAndDocumentType(id = id, documentType = documentType, zoneStatus = Option(true)), Duration.Inf)

    def zoneReject(id: String, documentType: String): Int = Await.result(zoneUpdateStatusByIdAndDocumentType(id = id, documentType = documentType, zoneStatus = Option(false)), Duration.Inf)

    def zoneRejectAll(id: String): Int = Await.result(zoneUpdateStatusById(id = id, zoneStatus = Option(false)), Duration.Inf)

    def organizationVerifyAll(id: String): Int = Await.result(organizationUpdateStatusById(id = id, status = Option(true)), Duration.Inf)

    def organizationVerify(id: String, documentType: String): Int = Await.result(organizationUpdateStatusByIdAndDocumentType(id = id, documentType = documentType, status = Option(true)), Duration.Inf)

    def organizationReject(id: String, documentType: String): Int = Await.result(organizationUpdateStatusByIdAndDocumentType(id = id, documentType = documentType, status = Option(false)), Duration.Inf)

    def organizationRejectAll(id: String): Int = Await.result(organizationUpdateStatusById(id = id, status = Option(false)), Duration.Inf)

    def deleteAllDocuments(id: String): Int = Await.result(deleteById(id = id), Duration.Inf)

    def checkFileExists(id: String, documentType: String): Boolean = Await.result(checkByIdAndDocumentType(id = id, documentType = documentType), Duration.Inf)

    def checkFileNameExists(id: String, fileName: String): Boolean = Await.result(checkByIdAndFileName(id = id, fileName = fileName), Duration.Inf)

  }
  
}