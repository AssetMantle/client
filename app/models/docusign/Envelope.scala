package models.docusign

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Envelope(id: String, envelopeID: String, documentType: String, status: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Envelopes @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.DOCUSIGN_ENVELOPE

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val envelopeTable = TableQuery[EnvelopeTable]

  private def add(envelope: Envelope): Future[String] = db.run((envelopeTable returning envelopeTable.map(_.id) += envelope).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateStatusByEnvelopeID(envelopeID: String, status: String): Future[Int] = db.run(envelopeTable.filter(_.envelopeID === envelopeID).map(_.status).update(status).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByEnvelopeID(envelopeID: String): Future[Envelope] = db.run(envelopeTable.filter(_.envelopeID === envelopeID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def tryGetByID(id: String, documentType: String): Future[Envelope] = db.run(envelopeTable.filter(x => x.id === id && x.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getByID(id: String, documentType: String) = db.run(envelopeTable.filter(x => x.id === id && x.documentType === documentType).result.headOption)

  private def getAllByID(id: String) = db.run(envelopeTable.filter(_.id === id).result)

  private def findEnvelopeIDByID(id: String) = db.run(envelopeTable.filter(_.id === id).map(_.envelopeID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getEnvelopeStatusByID(id: String): Future[Option[String]] = db.run(envelopeTable.filter(_.id === id).map(_.status).result.headOption)

  private def deleteById(id: String): Future[Int] = db.run(envelopeTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class EnvelopeTable(tag: Tag) extends Table[Envelope](tag, "Envelope") {

    def * = (id, envelopeID, documentType, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Envelope.tupled, Envelope.unapply)

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

  }

  object Service {

    def create(id: String, envelopeID: String, documentType: String): Future[String] = add(Envelope(id, envelopeID, documentType, constants.External.Docusign.Status.CREATED))

    def get(id: String, documentType: String): Future[Option[Envelope]] = getByID(id, documentType)

    def tryGet(id: String, documentType: String): Future[Envelope] = tryGetByID(id, documentType)

    def getAll(id: String): Future[Seq[Envelope]] = getAllByID(id)

    def tryGetByEnvelopeID(envelopeID: String): Future[Envelope] = findByEnvelopeID(envelopeID)

    def markSent(envelopeID: String): Future[Int] = updateStatusByEnvelopeID(envelopeID, constants.External.Docusign.Status.SENT)

    def markComplete(envelopeID: String): Future[Int] = updateStatusByEnvelopeID(envelopeID, constants.External.Docusign.Status.COMPLETED)

    def tryGetEnvelopeID(id: String): Future[String] = findEnvelopeIDByID(id)

    def getEnvelopeStatus(id: String): Future[Option[String]] = getEnvelopeStatusByID(id)
  }

}
