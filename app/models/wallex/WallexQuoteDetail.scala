package models.wallex

import exceptions.BaseException
import models.Trait.Logged
import models.common.Serializable.ConversionDetails
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class WallexQuoteDetail(
    wallexId: String,
    zoneId: String,
    accountId: String,
    quoteId: String,
    partnerRate: Double,
    partnerBuyAmount: Double,
    partnerSellAmount: Double,
    partnerPaymentFee: Option[Double] = None,
    expiresAt: String,
    conversionFee: Double,
    paymentFee: Double,
    paymentChannel: String,
    bankCharges: Double,
    supportingDocumentsRequired: Boolean,
    status: String,
    conversionDetails: ConversionDetails,
    createdBy: Option[String] = None,
    createdOn: Option[Timestamp] = None,
    createdOnTimeZone: Option[String] = None,
    updatedBy: Option[String] = None,
    updatedOn: Option[Timestamp] = None,
    updatedOnTimeZone: Option[String] = None
) extends Logged

@Singleton
class WallexQuoteDetails @Inject() (
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.WALLEX_QUOTE_DETAILS

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val wallexQuoteDetailTable =
    TableQuery[WallexQuoteDetailTable]

  private def serialize(
      wallexQuoteDetail: WallexQuoteDetail
  ): WallexQuoteDetailSerialized =
    WallexQuoteDetailSerialized(
      wallexId = wallexQuoteDetail.wallexId,
      zoneId = wallexQuoteDetail.zoneId,
      accountId = wallexQuoteDetail.accountId,
      quoteId = wallexQuoteDetail.quoteId,
      partnerRate = wallexQuoteDetail.partnerRate,
      partnerBuyAmount = wallexQuoteDetail.partnerBuyAmount,
      partnerSellAmount = wallexQuoteDetail.partnerSellAmount,
      partnerPaymentFee = wallexQuoteDetail.partnerPaymentFee,
      expiresAt = wallexQuoteDetail.expiresAt,
      conversionFee = wallexQuoteDetail.conversionFee,
      paymentFee = wallexQuoteDetail.paymentFee,
      paymentChannel = wallexQuoteDetail.paymentChannel,
      bankCharges = wallexQuoteDetail.bankCharges,
      supportingDocumentsRequired =
        wallexQuoteDetail.supportingDocumentsRequired,
      status = wallexQuoteDetail.status,
      conversionDetails =
        Json.toJson(wallexQuoteDetail.conversionDetails).toString,
      createdBy = wallexQuoteDetail.createdBy,
      createdOn = wallexQuoteDetail.createdOn,
      createdOnTimeZone = wallexQuoteDetail.createdOnTimeZone,
      updatedBy = wallexQuoteDetail.updatedBy,
      updatedOn = wallexQuoteDetail.updatedOn,
      updatedOnTimeZone = wallexQuoteDetail.updatedOnTimeZone
    )

  private def add(
      wallexQuoteDetail: WallexQuoteDetailSerialized
  ): Future[String] =
    db.run(
        (wallexQuoteDetailTable returning wallexQuoteDetailTable
          .map(_.quoteId) += wallexQuoteDetail).asTry
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
      wallexQuoteDetail: WallexQuoteDetailSerialized
  ): Future[Int] =
    db.run(
        wallexQuoteDetailTable
          .insertOrUpdate(wallexQuoteDetail)
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
      quoteId: String
  ): Future[Option[WallexQuoteDetailSerialized]] =
    db.run(
      wallexQuoteDetailTable
        .filter(_.quoteId === quoteId)
        .result
        .headOption
    )

  private def updateStatus(
      quoteId: String,
      status: String
  ): Future[Int] =
    db.run(
        wallexQuoteDetailTable
          .filter(_.quoteId === quoteId)
          .map(_.status)
          .update(status)
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

  case class WallexQuoteDetailSerialized(
      wallexId: String,
      zoneId: String,
      accountId: String,
      quoteId: String,
      partnerRate: Double,
      partnerBuyAmount: Double,
      partnerSellAmount: Double,
      partnerPaymentFee: Option[Double] = None,
      expiresAt: String,
      conversionFee: Double,
      paymentFee: Double,
      paymentChannel: String,
      bankCharges: Double,
      supportingDocumentsRequired: Boolean,
      status: String,
      conversionDetails: String,
      createdBy: Option[String] = None,
      createdOn: Option[Timestamp] = None,
      createdOnTimeZone: Option[String] = None,
      updatedBy: Option[String] = None,
      updatedOn: Option[Timestamp] = None,
      updatedOnTimeZone: Option[String] = None
  ) {
    def deserialize: WallexQuoteDetail =
      WallexQuoteDetail(
        wallexId = wallexId,
        zoneId = zoneId,
        accountId = accountId,
        quoteId = quoteId,
        partnerRate = partnerRate,
        partnerBuyAmount = partnerBuyAmount,
        partnerSellAmount = partnerSellAmount,
        partnerPaymentFee = partnerPaymentFee,
        expiresAt = expiresAt,
        conversionFee = conversionFee,
        paymentFee = paymentFee,
        paymentChannel = paymentChannel,
        bankCharges = bankCharges,
        supportingDocumentsRequired = supportingDocumentsRequired,
        status = status,
        conversionDetails = utilities.JSON
          .convertJsonStringToObject[ConversionDetails](conversionDetails),
        createdBy = createdBy,
        createdOn = createdOn,
        createdOnTimeZone = createdOnTimeZone,
        updatedBy = updatedBy,
        updatedOn = updatedOn,
        updatedOnTimeZone = updatedOnTimeZone
      )

  }
  private[models] class WallexQuoteDetailTable(tag: Tag)
      extends Table[WallexQuoteDetailSerialized](
        tag,
        "WallexQuoteDetail"
      ) {

    override def * =
      (
        wallexId,
        zoneId,
        accountId,
        quoteId,
        partnerRate,
        partnerBuyAmount,
        partnerSellAmount,
        partnerPaymentFee.?,
        expiresAt,
        conversionFee,
        paymentFee,
        paymentChannel,
        bankCharges,
        supportingDocumentsRequired,
        status,
        conversionDetails,
        createdBy.?,
        createdOn.?,
        createdOnTimeZone.?,
        updatedBy.?,
        updatedOn.?,
        updatedOnTimeZone.?
      ) <> (WallexQuoteDetailSerialized.tupled, WallexQuoteDetailSerialized.unapply)

    def wallexId = column[String]("wallexId")

    def zoneId = column[String]("zoneId")

    def accountId = column[String]("accountId")

    def quoteId = column[String]("quoteId", O.PrimaryKey)

    def partnerRate = column[Double]("partnerRate")

    def partnerBuyAmount = column[Double]("partnerBuyAmount")

    def partnerSellAmount = column[Double]("partnerSellAmount")

    def partnerPaymentFee = column[Double]("partnerPaymentFee")

    def expiresAt = column[String]("expiresAt")

    def conversionFee = column[Double]("conversionFee")

    def paymentFee = column[Double]("paymentFee")

    def paymentChannel = column[String]("paymentChannel")

    def bankCharges = column[Double]("bankCharges")

    def supportingDocumentsRequired =
      column[Boolean]("supportingDocumentsRequired")

    def status = column[String]("status")

    def conversionDetails = column[String]("conversionDetails")

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
        accountId: String,
        quoteId: String,
        partnerRate: Double,
        partnerBuyAmount: Double,
        partnerSellAmount: Double,
        partnerPaymentFee: Option[Double] = None,
        expiresAt: String,
        conversionFee: Double,
        paymentFee: Double,
        paymentChannel: String,
        bankCharges: Double,
        supportingDocumentsRequired: Boolean,
        status: String,
        conversionDetails: ConversionDetails
    ): Future[String] =
      add(
        serialize(
          WallexQuoteDetail(
            wallexId = wallexId,
            zoneId = zoneId,
            accountId = accountId,
            partnerRate = partnerRate,
            partnerBuyAmount = partnerBuyAmount,
            partnerSellAmount = partnerSellAmount,
            partnerPaymentFee = partnerPaymentFee,
            expiresAt = expiresAt,
            quoteId = quoteId,
            conversionFee = conversionFee,
            paymentFee = paymentFee,
            paymentChannel = paymentChannel,
            bankCharges = bankCharges,
            supportingDocumentsRequired = supportingDocumentsRequired,
            status = status,
            conversionDetails = conversionDetails
          )
        )
      )

    def insertOrUpdate(
        wallexId: String,
        zoneId: String,
        accountId: String,
        quoteId: String,
        partnerRate: Double,
        partnerBuyAmount: Double,
        partnerSellAmount: Double,
        partnerPaymentFee: Option[Double] = None,
        expiresAt: String,
        conversionFee: Double,
        paymentFee: Double,
        paymentChannel: String,
        bankCharges: Double,
        supportingDocumentsRequired: Boolean,
        status: String,
        conversionDetails: ConversionDetails
    ): Future[Int] =
      upsert(
        serialize(
          WallexQuoteDetail(
            wallexId = wallexId,
            zoneId = zoneId,
            accountId = accountId,
            partnerRate = partnerRate,
            partnerBuyAmount = partnerBuyAmount,
            partnerSellAmount = partnerSellAmount,
            partnerPaymentFee = partnerPaymentFee,
            expiresAt = expiresAt,
            quoteId = quoteId,
            conversionFee = conversionFee,
            paymentFee = paymentFee,
            paymentChannel = paymentChannel,
            bankCharges = bankCharges,
            supportingDocumentsRequired = supportingDocumentsRequired,
            status = status,
            conversionDetails = conversionDetails
          )
        )
      )

    def tryGet(quoteId: String): Future[WallexQuoteDetailSerialized] =
      findById(quoteId).map { detail =>
        detail.getOrElse(
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
        )
      }

    def get(quoteId: String): Future[Option[WallexQuoteDetailSerialized]] =
      findById(quoteId)
  }

  def updateQuoteStatus(quoteId: String, status: String): Future[Int] =
    updateStatus(
      quoteId = quoteId,
      status = status
    )
}
