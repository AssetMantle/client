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

case class Quote(
    wallexID: String,
    quoteID: String,
    zoneID: String,
    accountID: String,
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
class Quotes @Inject() (
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.WALLEX_QUOTE_DETAILS

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val quoteTable =
    TableQuery[QuoteTable]

  private def serialize(
      quote: Quote
  ): QuoteSerialized =
    QuoteSerialized(
      wallexID = quote.wallexID,
      zoneID = quote.zoneID,
      accountID = quote.accountID,
      quoteID = quote.quoteID,
      partnerRate = quote.partnerRate,
      partnerBuyAmount = quote.partnerBuyAmount,
      partnerSellAmount = quote.partnerSellAmount,
      partnerPaymentFee = quote.partnerPaymentFee,
      expiresAt = quote.expiresAt,
      conversionFee = quote.conversionFee,
      paymentFee = quote.paymentFee,
      paymentChannel = quote.paymentChannel,
      bankCharges = quote.bankCharges,
      supportingDocumentsRequired = quote.supportingDocumentsRequired,
      status = quote.status,
      conversionDetails = Json.toJson(quote.conversionDetails).toString,
      createdBy = quote.createdBy,
      createdOn = quote.createdOn,
      createdOnTimeZone = quote.createdOnTimeZone,
      updatedBy = quote.updatedBy,
      updatedOn = quote.updatedOn,
      updatedOnTimeZone = quote.updatedOnTimeZone
    )

  private def add(
      quote: QuoteSerialized
  ): Future[String] =
    db.run(
        (quoteTable returning quoteTable
          .map(_.quoteID) += quote).asTry
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
      quote: QuoteSerialized
  ): Future[Int] =
    db.run(
        quoteTable
          .insertOrUpdate(quote)
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

  private def findById(quoteID: String): Future[QuoteSerialized] =
    db.run(quoteTable.filter(_.quoteID === quoteID).result.head.asTry).map {
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
      quoteID: String,
      status: String
  ): Future[Int] =
    db.run(
        quoteTable
          .filter(_.quoteID === quoteID)
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

  case class QuoteSerialized(
      wallexID: String,
      zoneID: String,
      accountID: String,
      quoteID: String,
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
    def deserialize: Quote =
      Quote(
        wallexID = wallexID,
        zoneID = zoneID,
        accountID = accountID,
        quoteID = quoteID,
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
  private[models] class QuoteTable(tag: Tag)
      extends Table[QuoteSerialized](
        tag,
        "Quote"
      ) {

    override def * =
      (
        wallexID,
        zoneID,
        accountID,
        quoteID,
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
      ) <> (QuoteSerialized.tupled, QuoteSerialized.unapply)

    def quoteID = column[String]("quoteID", O.PrimaryKey)

    def wallexID = column[String]("wallexID", O.PrimaryKey)

    def zoneID = column[String]("zoneID")

    def accountID = column[String]("accountID")

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
        wallexID: String,
        zoneID: String,
        accountID: String,
        quoteID: String,
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
          Quote(
            wallexID = wallexID,
            zoneID = zoneID,
            accountID = accountID,
            partnerRate = partnerRate,
            partnerBuyAmount = partnerBuyAmount,
            partnerSellAmount = partnerSellAmount,
            partnerPaymentFee = partnerPaymentFee,
            expiresAt = expiresAt,
            quoteID = quoteID,
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
        wallexID: String,
        zoneID: String,
        accountID: String,
        quoteID: String,
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
          Quote(
            wallexID = wallexID,
            zoneID = zoneID,
            accountID = accountID,
            partnerRate = partnerRate,
            partnerBuyAmount = partnerBuyAmount,
            partnerSellAmount = partnerSellAmount,
            partnerPaymentFee = partnerPaymentFee,
            expiresAt = expiresAt,
            quoteID = quoteID,
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

    def tryGet(quoteID: String): Future[Quote] =
      findById(quoteID).map(_.deserialize)

    def updateQuoteStatus(quoteID: String, status: String): Future[Int] =
      updateStatus(
        quoteID = quoteID,
        status = status
      )
  }

}