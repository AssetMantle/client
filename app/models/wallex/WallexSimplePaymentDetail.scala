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

case class WallexSimplePaymentDetail(
    wallexId: String,
    zoneId: String,
    simplePaymentId: String,
    status: String,
    createdAt: String,
    referenceId: String,
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
class WallexSimplePaymentDetails @Inject() (
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.WALLEX_PAYMENT_DETAILS

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val wallexPaymentDetailTable =
    TableQuery[WallexSimplePaymentDetailTable]

  private def serialize(
      simplePaymentDetail: WallexSimplePaymentDetail
  ): WallexSimplePaymentDetailSerialized =
    WallexSimplePaymentDetailSerialized(
      wallexId = simplePaymentDetail.wallexId,
      zoneId = simplePaymentDetail.zoneId,
      simplePaymentId = simplePaymentDetail.simplePaymentId,
      status = simplePaymentDetail.status,
      createdAt = simplePaymentDetail.createdAt,
      referenceId = simplePaymentDetail.referenceId,
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
      wallexSimplePaymentDetail: WallexSimplePaymentDetailSerialized
  ): Future[String] =
    db.run(
        (wallexPaymentDetailTable returning wallexPaymentDetailTable
          .map(_.simplePaymentId) += wallexSimplePaymentDetail).asTry
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
      wallexSimplePaymentDetail: WallexSimplePaymentDetailSerialized
  ): Future[Int] =
    db.run(
        wallexPaymentDetailTable
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

  private def findById(
      simplePaymentId: String
  ): Future[Option[WallexSimplePaymentDetailSerialized]] =
    db.run(
      wallexPaymentDetailTable
        .filter(_.simplePaymentId === simplePaymentId)
        .result
        .headOption
    )

  private def updateStatus(
      simplePaymentId: String,
      zoneApproved: Boolean
  ): Future[Int] =
    db.run(
        wallexPaymentDetailTable
          .filter(_.simplePaymentId === simplePaymentId)
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

  case class WallexSimplePaymentDetailSerialized(
      wallexId: String,
      zoneId: String,
      simplePaymentId: String,
      status: String,
      createdAt: String,
      referenceId: String,
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

    def deserialize: WallexSimplePaymentDetail =
      WallexSimplePaymentDetail(
        wallexId = wallexId,
        zoneId = zoneId,
        simplePaymentId = simplePaymentId,
        status = status,
        createdAt = createdAt,
        referenceId = referenceId,
        fundingSource = fundingSource,
        purposeOfTransfer = purposeOfTransfer,
        fundingReference = fundingReference,
        fundingCutoffTime = fundingCutoffTime,
        beneficiary =
          utilities.JSON.convertJsonStringToObject[BeneficiaryPayment](beneficiary),
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

  private[models] class WallexSimplePaymentDetailTable(tag: Tag)
      extends Table[WallexSimplePaymentDetailSerialized](
        tag,
        "WallexPaymentDetail"
      ) {

    override def * =
      (
        wallexId,
        zoneId,
        simplePaymentId,
        status,
        createdAt,
        referenceId,
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
      ) <> (WallexSimplePaymentDetailSerialized.tupled, WallexSimplePaymentDetailSerialized.unapply)

    def wallexId = column[String]("wallexId")

    def zoneId = column[String]("zoneId")

    def simplePaymentId = column[String]("simplePaymentId", O.PrimaryKey)

    def status = column[String]("status")

    def createdAt = column[String]("createdAt")

    def referenceId = column[String]("referenceId")

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
        wallexId: String,
        zoneId: String,
        simplePaymentId: String,
        status: String,
        createdAt: String,
        referenceId: String,
        fundingSource: String,
        purposeOfTransfer: String,
        fundingReference: String,
        fundingCutoffTime: String,
        beneficiary: BeneficiaryPayment,
        conversionDetails: ConversionDetails,
        zoneApproved: Option[Boolean],
        createdBy: Option[String] = None,
        createdOn: Option[Timestamp] = None,
        createdOnTimeZone: Option[String] = None,
        updatedBy: Option[String] = None,
        updatedOn: Option[Timestamp] = None,
        updatedOnTimeZone: Option[String] = None
    ): Future[String] =
      add(
        serialize(
          WallexSimplePaymentDetail(
            wallexId = wallexId,
            zoneId = zoneId,
            simplePaymentId = simplePaymentId,
            status = status,
            createdAt = createdAt,
            referenceId = referenceId,
            fundingSource = fundingSource,
            purposeOfTransfer = purposeOfTransfer,
            fundingReference = fundingReference,
            fundingCutoffTime = fundingCutoffTime,
            beneficiary = beneficiary,
            conversionDetails = conversionDetails,
            zoneApproved = zoneApproved,
            createdBy = createdBy,
            createdOn = createdOn,
            createdOnTimeZone = createdOnTimeZone,
            updatedBy = updatedBy,
            updatedOn = updatedOn,
            updatedOnTimeZone = updatedOnTimeZone
          )
        )
      )

    def insertOrUpdate(
        wallexId: String,
        zoneId: String,
        simplePaymentId: String,
        status: String,
        createdAt: String,
        referenceId: String,
        failureReason: String,
        fundingSource: String,
        purposeOfTransfer: String,
        fundingReference: String,
        fundingCutoffTime: String,
        beneficiary: BeneficiaryPayment,
        conversionDetails: ConversionDetails,
        zoneApproved: Option[Boolean],
        createdBy: Option[String] = None,
        createdOn: Option[Timestamp] = None,
        createdOnTimeZone: Option[String] = None,
        updatedBy: Option[String] = None,
        updatedOn: Option[Timestamp] = None,
        updatedOnTimeZone: Option[String] = None
    ): Future[Int] =
      upsert(
        serialize(
          WallexSimplePaymentDetail(
            wallexId = wallexId,
            zoneId = zoneId,
            simplePaymentId = simplePaymentId,
            status = status,
            createdAt = createdAt,
            referenceId = referenceId,
            fundingSource = fundingSource,
            purposeOfTransfer = purposeOfTransfer,
            fundingReference = fundingReference,
            fundingCutoffTime = fundingCutoffTime,
            beneficiary = beneficiary,
            conversionDetails = conversionDetails,
            zoneApproved = zoneApproved,
            createdBy = createdBy,
            createdOn = createdOn,
            createdOnTimeZone = createdOnTimeZone,
            updatedBy = updatedBy,
            updatedOn = updatedOn,
            updatedOnTimeZone = updatedOnTimeZone
          )
        )
      )

    def tryGet(
        simplePaymentId: String
    ): Future[WallexSimplePaymentDetailSerialized] =
      findById(simplePaymentId).map { detail =>
        detail.getOrElse(
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
        )
      }

    def get(
        quoteId: String
    ): Future[Option[WallexSimplePaymentDetailSerialized]] =
      findById(quoteId)
  }

  def updateZoneApprovedStatus(
      simplePaymentId: String,
      zoneApproved: Boolean
  ): Future[Int] =
    updateStatus(
      simplePaymentId = simplePaymentId,
      zoneApproved = zoneApproved
    )
}
