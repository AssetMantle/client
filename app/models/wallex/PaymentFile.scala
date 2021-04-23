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

case class PaymentFile(
    quoteId: String,
    fileId: String,
    fileType: String,
    organizationID: String,
    negotiationId: String,
    wallexId: String,
    createdBy: Option[String] = None,
    createdOn: Option[Timestamp] = None,
    createdOnTimeZone: Option[String] = None,
    updatedBy: Option[String] = None,
    updatedOn: Option[Timestamp] = None,
    updatedOnTimeZone: Option[String] = None
) extends Logged

@Singleton
class PaymentFiles @Inject()(
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.WALLEX_PAYMENT_FILE

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val paymentFileTable =
    TableQuery[PaymentFileDetailTable]

  private def add(
      paymentFile: PaymentFile
  ): Future[String] =
    db.run(
        (paymentFileTable returning paymentFileTable
          .map(_.quoteId) += paymentFile).asTry
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
      paymentFile: PaymentFile
  ): Future[Int] =
    db.run(
        paymentFileTable
          .insertOrUpdate(paymentFile)
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
  ): Future[Option[PaymentFile]] =
    db.run(
      paymentFileTable
        .filter(_.quoteId === quoteId)
        .result
        .headOption
    )

  private[models] class PaymentFileDetailTable(tag: Tag)
      extends Table[PaymentFile](
        tag,
        "PaymentFile"
      ) {

    override def * =
      (
        quoteId,
        fileId,
        fileType,
        organizationID,
        negotiationId,
        wallexId,
        createdBy.?,
        createdOn.?,
        createdOnTimeZone.?,
        updatedBy.?,
        updatedOn.?,
        updatedOnTimeZone.?
      ) <> (PaymentFile.tupled, PaymentFile.unapply)

    def quoteId = column[String]("quoteId", O.PrimaryKey)

    def fileId = column[String]("fileId", O.PrimaryKey)

    def fileType = column[String]("fileType", O.PrimaryKey)

    def organizationID = column[String]("organizationID")

    def negotiationId = column[String]("negotiationId")

    def wallexId = column[String]("wallexId")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {
    def create(
        organizationID: String,
        negotiationId: String,
        quoteId: String,
        wallexId: String,
        fileId: String,
        fileType: String
    ): Future[String] =
      add(
        PaymentFile(
          organizationID = organizationID,
          quoteId = quoteId,
          negotiationId = negotiationId,
          wallexId = wallexId,
          fileId = fileId,
          fileType = fileType
        )
      )

    def insertOrUpdate(
        organizationID: String,
        negotiationId: String,
        quoteId: String,
        wallexId: String,
        fileId: String,
        fileType: String
    ): Future[String] =
      add(
        PaymentFile(
          organizationID = organizationID,
          quoteId = quoteId,
          negotiationId = negotiationId,
          wallexId = wallexId,
          fileId = fileId,
          fileType = fileType
        )
      )

    def tryGet(quoteId: String): Future[PaymentFile] =
      findById(quoteId).map { detail =>
        detail.getOrElse(
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
        )
      }

    def get(quoteId: String): Future[Option[PaymentFile]] =
      findById(quoteId)
  }
}
