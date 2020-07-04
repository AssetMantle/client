package models.westernUnion

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import utilities.MicroNumber

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class RTCB(id: String, reference: String, externalReference: String, invoiceNumber: String, buyerBusinessId: String, buyerFirstName: String, buyerLastName: String, createdDate: Timestamp, lastUpdatedDate: Timestamp, status: String, dealType: String, paymentTypeId: String, paidOutAmount: MicroNumber, requestSignature: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class RTCBs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  def serialize(rtcb: RTCB): RTCBSerialized = RTCBSerialized(id = rtcb.id, reference = rtcb.reference, externalReference = rtcb.externalReference, invoiceNumber = rtcb.invoiceNumber, buyerBusinessId = rtcb.buyerBusinessId, buyerFirstName = rtcb.buyerFirstName, buyerLastName = rtcb.buyerLastName, createdDate = rtcb.createdDate, lastUpdatedDate = rtcb.lastUpdatedDate, status = rtcb.status, dealType = rtcb.dealType, paymentTypeId = rtcb.paymentTypeId, paidOutAmount = rtcb.paidOutAmount.toMicroString, requestSignature = rtcb.requestSignature, createdBy = rtcb.createdBy, createdOn = rtcb.createdOn, createdOnTimeZone = rtcb.createdOnTimeZone, updatedBy = rtcb.updatedBy, updatedOn = rtcb.updatedOn, updatedOnTimeZone = rtcb.updatedOnTimeZone)

  case class RTCBSerialized(id: String, reference: String, externalReference: String, invoiceNumber: String, buyerBusinessId: String, buyerFirstName: String, buyerLastName: String, createdDate: Timestamp, lastUpdatedDate: Timestamp, status: String, dealType: String, paymentTypeId: String, paidOutAmount: String, requestSignature: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: RTCB = RTCB(id = id, reference = reference, externalReference = externalReference, invoiceNumber = invoiceNumber, buyerBusinessId = buyerBusinessId, buyerFirstName = buyerFirstName, buyerLastName = buyerLastName, createdDate = createdDate, lastUpdatedDate = lastUpdatedDate, status = status, dealType = dealType, paymentTypeId = paymentTypeId, paidOutAmount = new MicroNumber(BigInt(paidOutAmount)), requestSignature = requestSignature, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  private implicit val module: String = constants.Module.WESTERN_UNION_RTCB

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val rtcbTable = TableQuery[RTCBTable]

  private def add(rtcbSerialized: RTCBSerialized): Future[String] = db.run((rtcbTable returning rtcbTable.map(_.id) += rtcbSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findById(id: String): Future[RTCBSerialized] = db.run(rtcbTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getAllByByTransactionIDs(externalReferences: Seq[String]) = db.run(rtcbTable.filter(_.externalReference inSet externalReferences).result)

  private def getAmountsByExternalReference(externalReference: String): Future[Seq[String]] = db.run(rtcbTable.filter(_.externalReference === externalReference).map(_.paidOutAmount).result)

  private def deleteById(id: String): Future[Int] = db.run(rtcbTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class RTCBTable(tag: Tag) extends Table[RTCBSerialized](tag, "RTCB") {

    def * = (id, reference, externalReference, invoiceNumber, buyerBusinessId, buyerFirstName, buyerLastName, createdDate, lastUpdatedDate, status, dealType, paymentTypeId, paidOutAmount, requestSignature, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (RTCBSerialized.tupled, RTCBSerialized.unapply)

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

    def paidOutAmount = column[String]("paidOutAmount")

    def requestSignature = column[String]("requestSignature")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(id: String, reference: String, externalReference: String, invoiceNumber: String, buyerBusinessId: String, buyerFirstName: String, buyerLastName: String, createdDate: Timestamp, lastUpdatedDate: Timestamp, status: String, dealType: String, paymentTypeId: String, paidOutAmount: MicroNumber, requestSignature: String): Future[String] = add(serialize(RTCB(id, reference, externalReference, invoiceNumber, buyerBusinessId, buyerFirstName, buyerLastName, createdDate, lastUpdatedDate, status, dealType, paymentTypeId, paidOutAmount, requestSignature)))

    def get(id: String): Future[RTCB] = findById(id).map(_.deserialize)

    def totalRTCBAmountByTransactionID(transactionID: String): Future[MicroNumber] = getAmountsByExternalReference(transactionID).map(y => new MicroNumber(y.map(x => BigInt(x)).sum))

    def getAll(transactionIDs: Seq[String]): Future[Seq[RTCB]] = getAllByByTransactionIDs(transactionIDs).map(_.map(_.deserialize))
  }

}
