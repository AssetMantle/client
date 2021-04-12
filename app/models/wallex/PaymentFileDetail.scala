package models.wallex

import exceptions.BaseException
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class PaymentFileDetail(
    orgId: String,
    quoteId:String,
    negotiationId: String,
    wallexId: String,
    fileId: String,
    fileType: String,
    createdBy: Option[String] = None,
    createdOn: Option[Timestamp] = None,
    createdOnTimeZone: Option[String] = None,
    updatedBy: Option[String] = None,
    updatedOn: Option[Timestamp] = None,
    updatedOnTimeZone: Option[String] = None
) extends Logged

@Singleton
class PaymentFileDetails @Inject() (
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.WALLEX_PAYMENT_FILE

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val paymentFileDetailTable =
    TableQuery[PaymentFileDetailTable]

  private def add(
      paymentFileDetail: PaymentFileDetail
  ): Future[String] =
    db.run(
        (paymentFileDetailTable returning paymentFileDetailTable
          .map(_.quoteId) += paymentFileDetail).asTry
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
      paymentFileDetail: PaymentFileDetail
  ): Future[Int] =
    db.run(
        paymentFileDetailTable
          .insertOrUpdate(paymentFileDetail)
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
  ): Future[Option[PaymentFileDetail]] =
    db.run(
      paymentFileDetailTable
        .filter(_.quoteId === quoteId)
        .result
        .headOption
    )

  private[models] class PaymentFileDetailTable(tag: Tag)
      extends Table[PaymentFileDetail](
        tag,
        "PaymentFileDetail"
      ) {

    override def * =
      (
        orgId,
        quoteId,
        negotiationId,
        wallexId,
        fileId,
        fileType,
        createdBy.?,
        createdOn.?,
        createdOnTimeZone.?,
        updatedBy.?,
        updatedOn.?,
        updatedOnTimeZone.?
      ) <> (PaymentFileDetail.tupled, PaymentFileDetail.unapply)

    def orgId = column[String]("orgId")

    def quoteId = column[String]("quoteId", O.PrimaryKey)

    def negotiationId = column[String]("negotiationId")

    def wallexId = column[String]("wallexId")

    def fileId = column[String]("fileId", O.PrimaryKey)

    def fileType = column[String]("fileType", O.PrimaryKey)

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {
    def create(
        orgId: String,
        negotiationId: String,
        quoteId:String,
        wallexId: String,
        fileId: String,
        fileType: String
    ): Future[String] =
      add(
        PaymentFileDetail(
          orgId = orgId,
          quoteId = quoteId,
          negotiationId = negotiationId,
          wallexId = wallexId,
          fileId = fileId,
          fileType = fileType
        )
      )

    def insertOrUpdate(
        orgId: String,
        negotiationId: String,
        quoteId:String,
        wallexId: String,
        fileId: String,
        fileType: String
    ): Future[String] =
      add(
        PaymentFileDetail(
          orgId = orgId,
          quoteId = quoteId,
          negotiationId = negotiationId,
          wallexId = wallexId,
          fileId = fileId,
          fileType = fileType
        )
      )

    def tryGet(quoteId: String): Future[PaymentFileDetail] =
      findById(quoteId).map { detail =>
        detail.getOrElse(
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
        )
      }

    def get(quoteId: String): Future[Option[PaymentFileDetail]] =
      findById(quoteId)
  }
}
