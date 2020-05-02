package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class DocusignEnvelope(id: String, envelopeID: String, documentType: String, status: String)

@Singleton
class DocusignEnvelopes @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_DOCUSIGN_ENVELOPES

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val docusignTable = TableQuery[DocusignEnvelopeTable]

  private def add(docusign: DocusignEnvelope): Future[String] = db.run((docusignTable returning docusignTable.map(_.id) += docusign).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateStatusByEnvelopeID(envelopeID: String, status: String): Future[Int] = db.run(docusignTable.filter(_.envelopeID === envelopeID).map(_.status).update(status).asTry).map {
    case Success(result) => result match {
      case 0 => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByEnvelopeID(envelopeID: String): Future[DocusignEnvelope] = db.run(docusignTable.filter(_.envelopeID === envelopeID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def tryGetByID(id: String, documentType: String): Future[DocusignEnvelope] = db.run(docusignTable.filter(_.id === id).filter(_.documentType === documentType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getByID(id: String, documentType: String) = db.run(docusignTable.filter(_.id === id).filter(_.documentType === documentType).result.headOption)

  private def getAllByID(id: String) = db.run(docusignTable.filter(_.id === id).result)

  private def findEnvelopeIDByID(id: String) = db.run(docusignTable.filter(_.id === id).map(_.envelopeID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getEnvelopeStatusByID(id: String): Future[Option[String]] = db.run(docusignTable.filter(_.id === id).map(_.status).result.headOption)

  private def deleteById(id: String): Future[Int] = db.run(docusignTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class DocusignEnvelopeTable(tag: Tag) extends Table[DocusignEnvelope](tag, "DocusignEnvelope") {

    def * = (id, envelopeID, documentType, status) <> (DocusignEnvelope.tupled, DocusignEnvelope.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def envelopeID = column[String]("envelopeID")

    def documentType = column[String]("documentType")

    def status = column[String]("status")

  }

  object Service {

    def create(id: String, envelopeID: String, documentType: String): Future[String] = add(DocusignEnvelope(id, envelopeID, documentType, constants.Status.DocuSignEnvelopeStatus.CREATED))

    def get(id: String, documentType: String): Future[Option[DocusignEnvelope]] = getByID(id, documentType)

    def tryGet(id: String, documentType: String) = tryGetByID(id, documentType)

    def getAll(id: String): Future[Seq[DocusignEnvelope]] = getAllByID(id)

    def tryGetByEnvelopeID(envelopeID: String): Future[DocusignEnvelope] = findByEnvelopeID(envelopeID)

    def markSent(envelopeID: String): Future[Int] = updateStatusByEnvelopeID(envelopeID, constants.Status.DocuSignEnvelopeStatus.SENT)

    def markComplete(envelopeID: String): Future[Int] = updateStatusByEnvelopeID(envelopeID, constants.Status.DocuSignEnvelopeStatus.COMPLETED)

    def tryGetEnvelopeID(id: String): Future[String] = findEnvelopeIDByID(id)

    def getEnvelopeStatus(id: String): Future[Option[String]] = getEnvelopeStatusByID(id)
  }

}
