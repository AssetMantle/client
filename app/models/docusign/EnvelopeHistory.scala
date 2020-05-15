package models.docusign

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.HistoryLogged
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class EnvelopeHistory(id: String, envelopeID: String, documentType: String, status: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None, deletedBy: String, deletedOn: Timestamp, deletedOnTimeZone: String) extends HistoryLogged

@Singleton
class EnvelopeHistories @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.DOCUSIGN_ENVELOPE_HISTORY

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val envelopeHistoryTable = TableQuery[EnvelopeHistoryTable]

  private def findByEnvelopeHistoryID(envelopeID: String): Future[EnvelopeHistory] = db.run(envelopeHistoryTable.filter(_.envelopeID === envelopeID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def tryGetByID(id: String, documentType: String): Future[EnvelopeHistory] = db.run(envelopeHistoryTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getByID(id: String, documentType: String) = db.run(envelopeHistoryTable.filter(_.id === id).filter(_.documentType === documentType).result.headOption)

  private def getAllByID(id: String) = db.run(envelopeHistoryTable.filter(_.id === id).result)

  private def findEnvelopeHistoryIDByID(id: String) = db.run(envelopeHistoryTable.filter(_.id === id).map(_.envelopeID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getEnvelopeHistoryStatusByID(id: String): Future[Option[String]] = db.run(envelopeHistoryTable.filter(_.id === id).map(_.status).result.headOption)

  private[models] class EnvelopeHistoryTable(tag: Tag) extends Table[EnvelopeHistory](tag, "Envelope_History") {

    def * = (id, envelopeID, documentType, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?, deletedBy, deletedOn, deletedOnTimeZone) <> (EnvelopeHistory.tupled, EnvelopeHistory.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def envelopeID = column[String]("envelopeID")

    def documentType = column[String]("documentType")

    def status = column[String]("status")

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

    def get(id: String, documentType: String): Future[Option[EnvelopeHistory]] = getByID(id, documentType)

    def tryGet(id: String, documentType: String): Future[EnvelopeHistory] = tryGetByID(id, documentType)

    def getAll(id: String): Future[Seq[EnvelopeHistory]] = getAllByID(id)

    def tryGetByEnvelopeHistoryID(envelopeID: String): Future[EnvelopeHistory] = findByEnvelopeHistoryID(envelopeID)

    def tryGetEnvelopeHistoryID(id: String): Future[String] = findEnvelopeHistoryIDByID(id)

    def getEnvelopeHistoryStatus(id: String): Future[Option[String]] = getEnvelopeHistoryStatusByID(id)
  }

}
