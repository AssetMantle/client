package models.docusign

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class EnvelopeHistory(id: String, envelopeID: String, documentType: String, status: String)

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

    def * = (id, envelopeID, documentType, status) <> (EnvelopeHistory.tupled, EnvelopeHistory.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def envelopeID = column[String]("envelopeID")

    def documentType = column[String]("documentType")

    def status = column[String]("status")

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
