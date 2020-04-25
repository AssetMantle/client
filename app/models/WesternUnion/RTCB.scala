package models.WesternUnion

import java.sql.Timestamp
import java.util.Date

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class RTCB(id: String, reference: String, externalReference: String, invoiceNumber: String, buyerBusinessId: String, buyerFirstName: String, buyerLastName: String, createdDate: Timestamp, lastUpdatedDate: Timestamp, status: String, dealType: String, paymentTypeId: String, paidOutAmount: Int, requestSignature: String)

@Singleton
class RTCBs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.WESTERN_UNION_RTCB

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val rtcbTable = TableQuery[RTCBTable]

  private def add(rtcb: RTCB): Future[String] = db.run((rtcbTable returning rtcbTable.map(_.id) += rtcb).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(rtcb: RTCB): Future[Int] = db.run(rtcbTable.insertOrUpdate(rtcb).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[RTCB] = db.run(rtcbTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getSumAmountsByExternalReference(externalReference: String): Future[Int] = db.run(rtcbTable.filter(_.externalReference === externalReference).map(_.paidOutAmount).sum.getOrElse(0).result)

    private def deleteById(id: String): Future[Int] = db.run(rtcbTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class RTCBTable(tag: Tag) extends Table[RTCB](tag, "RTCB") {

    def * = (id, reference, externalReference, invoiceNumber, buyerBusinessId, buyerFirstName, buyerLastName, createdDate, lastUpdatedDate, status, dealType, paymentTypeId, paidOutAmount, requestSignature) <> (RTCB.tupled, RTCB.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def reference = column[String]("reference")

    def externalReference = column[String]("externalReference")

    def invoiceNumber = column[String]("invoiceNumber")

    def buyerBusinessId = column[String]("buyerBusinessId")

    def buyerFirstName = column[String]("buyerFirstName")

    def buyerLastName = column[String]("buyerLastName")

    def createdDate = column[Timestamp]("createdDate")

    def lastUpdatedDate = column[Timestamp]("lastUpdatedDate")

    def status = column[String]("status")

    def dealType = column[String]("dealType")

    def paymentTypeId = column[String]("paymentTypeId")

    def paidOutAmount = column[Int]("paidOutAmount")

    def requestSignature = column[String]("requestSignature")

  }

  object Service {

    def create(id: String, reference: String, externalReference: String, invoiceNumber: String, buyerBusinessId: String, buyerFirstName: String, buyerLastName: String, createdDate: Timestamp, lastUpdatedDate: Timestamp, status: String, dealType: String, paymentTypeId: String, paidOutAmount: Int, requestSignature: String): Future[String] = add(RTCB(id, reference, externalReference, invoiceNumber, buyerBusinessId, buyerFirstName, buyerLastName, createdDate, lastUpdatedDate, status, dealType, paymentTypeId, paidOutAmount, requestSignature))

    def get(id: String): Future[RTCB] = findById(id)

    def totalRTCBAmountByTransactionID(transactionID: String): Future[Int] = getSumAmountsByExternalReference(transactionID)
  }

}
