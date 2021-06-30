package models.wallex

import exceptions.BaseException
import models.Trait.Logged
import models.common.Serializable.{BeneficiaryPayment, ConversionDetails}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SimplePayment(
    simplePaymentID: String,
    wallexID: String,
    status: String,
    createdAt: String,
    referenceID: String,
    fundingSource: String,
    purposeOfTransfer: String,
    fundingReference: String,
    fundingCutoffTime: String,
    beneficiary: BeneficiaryPayment,
    conversionDetails: ConversionDetails,
    zoneApproved: Option[Boolean] = None,
    createdBy: Option[String] = None,
    createdOn: Option[Timestamp] = None,
    createdOnTimeZone: Option[String] = None,
    updatedBy: Option[String] = None,
    updatedOn: Option[Timestamp] = None,
    updatedOnTimeZone: Option[String] = None
) extends Logged

@Singleton
class SimplePayments @Inject() (
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.WALLEX_PAYMENT_DETAILS

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val simplePaymentTable =
    TableQuery[SimplePaymentTable]

  private def serialize(
      simplePaymentDetail: SimplePayment
  ): SimplePaymentSerialized =
    SimplePaymentSerialized(
      simplePaymentID = simplePaymentDetail.simplePaymentID,
      wallexID = simplePaymentDetail.wallexID,
      status = simplePaymentDetail.status,
      createdAt = simplePaymentDetail.createdAt,
      referenceID = simplePaymentDetail.referenceID,
      fundingSource = simplePaymentDetail.fundingSource,
      purposeOfTransfer = simplePaymentDetail.purposeOfTransfer,
      fundingReference = simplePaymentDetail.fundingReference,
      fundingCutoffTime = simplePaymentDetail.fundingCutoffTime,
      beneficiary = Json.toJson(simplePaymentDetail.beneficiary).toString,
      conversionDetails =
        Json.toJson(simplePaymentDetail.conversionDetails).toString,
      zoneApproved = simplePaymentDetail.zoneApproved,
      createdBy = simplePaymentDetail.createdBy,
      createdOn = simplePaymentDetail.createdOn,
      createdOnTimeZone = simplePaymentDetail.createdOnTimeZone,
      updatedBy = simplePaymentDetail.updatedBy,
      updatedOn = simplePaymentDetail.updatedOn,
      updatedOnTimeZone = simplePaymentDetail.updatedOnTimeZone
    )

  private def add(
      wallexSimplePaymentDetail: SimplePaymentSerialized
  ): Future[String] =
    db.run(
        (simplePaymentTable returning simplePaymentTable
          .map(_.simplePaymentID) += wallexSimplePaymentDetail).asTry
      )
      .map {
        case Success(result) => result
        case Failure(exception) =>
          exception match {
            case psqlException: PSQLException =>
              throw new BaseException(
                constants.Response.PSQL_EXCEPTION,
                psqlException
              )
          }
      }

  private def upsert(
      wallexSimplePaymentDetail: SimplePaymentSerialized
  ): Future[Int] =
    db.run(
        simplePaymentTable
          .insertOrUpdate(wallexSimplePaymentDetail)
          .asTry
      )
      .map {
        case Success(result) => result
        case Failure(exception) =>
          exception match {
            case psqlException: PSQLException =>
              throw new BaseException(
                constants.Response.PSQL_EXCEPTION,
                psqlException
              )
          }
      }

  private def findById(simplePaymentID: String): Future[SimplePaymentSerialized] =
    db.run(simplePaymentTable.filter(_.simplePaymentID === simplePaymentID).result.head.asTry).map {
      case Success(result) => result
      case Failure(exception) =>
        exception match {
          case noSuchElementException: NoSuchElementException =>
            throw new BaseException(
              constants.Response.NO_SUCH_ELEMENT_EXCEPTION,
              noSuchElementException
            )
        }
    }

  private def updateStatus(
      simplePaymentID: String,
      zoneApproved: Boolean
  ): Future[Int] =
    db.run(
        simplePaymentTable
          .filter(_.simplePaymentID === simplePaymentID)
          .map(_.zoneApproved)
          .update(zoneApproved)
          .asTry
      )
      .map {
        case Success(result) => result
        case Failure(exception) =>
          exception match {
            case psqlException: PSQLException =>
              throw new BaseException(
                constants.Response.PSQL_EXCEPTION,
                psqlException
              )
            case noSuchElementException: NoSuchElementException =>
              throw new BaseException(
                constants.Response.NO_SUCH_ELEMENT_EXCEPTION,
                noSuchElementException
              )
          }
      }

  case class SimplePaymentSerialized(
      simplePaymentID: String,
      wallexID: String,
      status: String,
      createdAt: String,
      referenceID: String,
      fundingSource: String,
      purposeOfTransfer: String,
      fundingReference: String,
      fundingCutoffTime: String,
      beneficiary: String,
      conversionDetails: String,
      zoneApproved: Option[Boolean],
      createdBy: Option[String] = None,
      createdOn: Option[Timestamp] = None,
      createdOnTimeZone: Option[String] = None,
      updatedBy: Option[String] = None,
      updatedOn: Option[Timestamp] = None,
      updatedOnTimeZone: Option[String] = None
  ) {

    def deserialize: SimplePayment =
      SimplePayment(
        simplePaymentID = simplePaymentID,
        wallexID = wallexID,
        status = status,
        createdAt = createdAt,
        referenceID = referenceID,
        fundingSource = fundingSource,
        purposeOfTransfer = purposeOfTransfer,
        fundingReference = fundingReference,
        fundingCutoffTime = fundingCutoffTime,
        beneficiary = utilities.JSON
          .convertJsonStringToObject[BeneficiaryPayment](beneficiary),
        conversionDetails = utilities.JSON
          .convertJsonStringToObject[ConversionDetails](conversionDetails),
        zoneApproved = zoneApproved,
        createdBy = createdBy,
        createdOn = createdOn,
        createdOnTimeZone = createdOnTimeZone,
        updatedBy = updatedBy,
        updatedOn = updatedOn,
        updatedOnTimeZone = updatedOnTimeZone
      )

  }

  private[models] class SimplePaymentTable(tag: Tag)
      extends Table[SimplePaymentSerialized](
        tag,
        "SimplePayment"
      ) {

    override def * =
      (
        simplePaymentID,
        wallexID,
        status,
        createdAt,
        referenceID,
        fundingSource,
        purposeOfTransfer,
        fundingReference,
        fundingCutoffTime,
        beneficiary,
        conversionDetails,
        zoneApproved.?,
        createdBy.?,
        createdOn.?,
        createdOnTimeZone.?,
        updatedBy.?,
        updatedOn.?,
        updatedOnTimeZone.?
      ) <> (SimplePaymentSerialized.tupled, SimplePaymentSerialized.unapply)

    def simplePaymentID = column[String]("simplePaymentID", O.PrimaryKey)

    def wallexID = column[String]("wallexID", O.PrimaryKey)

    def status = column[String]("status")

    def createdAt = column[String]("createdAt")

    def referenceID = column[String]("referenceID")

    def fundingSource = column[String]("fundingSource")

    def purposeOfTransfer = column[String]("purposeOfTransfer")

    def fundingReference = column[String]("fundingReference")

    def fundingCutoffTime = column[String]("fundingCutoffTime")

    def beneficiary = column[String]("beneficiary")

    def conversionDetails = column[String]("conversionDetails")

    def zoneApproved = column[Boolean]("zoneApproved")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {
    def create(
        simplePaymentID: String,
        wallexID: String,
        status: String,
        createdAt: String,
        referenceID: String,
        fundingSource: String,
        purposeOfTransfer: String,
        fundingReference: String,
        fundingCutoffTime: String,
        beneficiary: BeneficiaryPayment,
        conversionDetails: ConversionDetails,
        zoneApproved: Option[Boolean]
    ): Future[String] =
      add(
        serialize(
          SimplePayment(
            simplePaymentID = simplePaymentID,
            wallexID = wallexID,
            status = status,
            createdAt = createdAt,
            referenceID = referenceID,
            fundingSource = fundingSource,
            purposeOfTransfer = purposeOfTransfer,
            fundingReference = fundingReference,
            fundingCutoffTime = fundingCutoffTime,
            beneficiary = beneficiary,
            conversionDetails = conversionDetails,
            zoneApproved = zoneApproved
          )
        )
      )

    def insertOrUpdate(
        simplePaymentID: String,
        wallexID: String,
        status: String,
        createdAt: String,
        referenceID: String,
        fundingSource: String,
        purposeOfTransfer: String,
        fundingReference: String,
        fundingCutoffTime: String,
        beneficiary: BeneficiaryPayment,
        conversionDetails: ConversionDetails,
        zoneApproved: Option[Boolean]
    ): Future[Int] =
      upsert(
        serialize(
          SimplePayment(
            simplePaymentID = simplePaymentID,
            wallexID = wallexID,
            status = status,
            createdAt = createdAt,
            referenceID = referenceID,
            fundingSource = fundingSource,
            purposeOfTransfer = purposeOfTransfer,
            fundingReference = fundingReference,
            fundingCutoffTime = fundingCutoffTime,
            beneficiary = beneficiary,
            conversionDetails = conversionDetails,
            zoneApproved = zoneApproved
          )
        )
      )

    def tryGet(
        simplePaymentID: String
    ): Future[SimplePayment] =
      findById(simplePaymentID).map(_.deserialize)

    def updateZoneApprovedStatus(simplePaymentID: String,zoneApproved: Boolean): Future[Int] =
      updateStatus(simplePaymentID = simplePaymentID, zoneApproved = zoneApproved
      )
  }

}
