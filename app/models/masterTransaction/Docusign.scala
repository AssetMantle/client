package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class Docusign(id: String, envelopeID: String, status:String)

@Singleton
class Docusigns @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_EMAIL_OTP

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val docusignTable = TableQuery[DocusignTable]

  private def add(docusign: Docusign): Future[String] = db.run((docusignTable returning docusignTable.map(_.id) += docusign).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(docusign: Docusign): Future[Int] = db.run(docusignTable.insertOrUpdate(docusign).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateStatusByEnvelopeID(envelopeID: String, status:String): Future[Int] = db.run(docusignTable.filter(_.envelopeID === envelopeID).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByEnvelopeID(envelopeID: String): Future[Docusign] = db.run(docusignTable.filter(_.envelopeID === envelopeID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findEnvelopeIDByID(id: String)= db.run(docusignTable.filter(_.id === id).map(_.envelopeID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findEnvelopeStatusByID(id: String)= db.run(docusignTable.filter(_.id === id).map(_.status).result.headOption)

  private def deleteById(id: String): Future[Int] = db.run(docusignTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class DocusignTable(tag: Tag) extends Table[Docusign](tag, "Docusign") {

    def * = (id, envelopeID,status) <> (Docusign.tupled, Docusign.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def envelopeID = column[String]("envelopeID")

    def status = column[String]("status")

  }

  object Service {

    def create(id: String,envelopeID: String): Future[String] = add(Docusign(id, envelopeID, constants.Status.DocuSignEnvelopeStatus.CREATED))

    def tryGetByEnvelopeID(envelopeID: String)= findByEnvelopeID(envelopeID)

    def markSent(envelopeID:String)= updateStatusByEnvelopeID(envelopeID, constants.Status.DocuSignEnvelopeStatus.SENT)

    def markSigningComplete(envelopeID:String)= updateStatusByEnvelopeID(envelopeID, constants.Status.DocuSignEnvelopeStatus.SIGNING_COMPLETE)

    def tryGetEnvelopeID(id:String)= findEnvelopeIDByID(id)

    def getEnvelopeStatus(id:String)= findEnvelopeStatusByID(id)
  }

}
