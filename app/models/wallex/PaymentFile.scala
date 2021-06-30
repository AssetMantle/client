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
    quoteID: String,
    fileID: String,
    fileType: String,
    negotiationID: String,
    wallexID: String,
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
    TableQuery[PaymentFileTable]

  private def add(
      paymentFile: PaymentFile
  ): Future[String] =
    db.run(
        (paymentFileTable returning paymentFileTable
          .map(_.quoteID) += paymentFile).asTry
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
      quoteID: String
  ): Future[Option[PaymentFile]] =
    db.run(
      paymentFileTable
        .filter(_.quoteID === quoteID)
        .result
        .headOption
    )

  private[models] class PaymentFileTable(tag: Tag)
      extends Table[PaymentFile](
        tag,
        "PaymentFile"
      ) {

    override def * =
      (
        quoteID,
        fileID,
        fileType,
        negotiationID,
        wallexID,
        createdBy.?,
        createdOn.?,
        createdOnTimeZone.?,
        updatedBy.?,
        updatedOn.?,
        updatedOnTimeZone.?
      ) <> (PaymentFile.tupled, PaymentFile.unapply)

    def quoteID = column[String]("quoteID", O.PrimaryKey)

    def fileID = column[String]("fileID", O.PrimaryKey)

    def fileType = column[String]("fileType")

    def negotiationID = column[String]("negotiationID")

    def wallexID = column[String]("wallexID")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {
    def create(
        negotiationID: String,
        quoteID: String,
        wallexID: String,
        fileID: String,
        fileType: String
    ): Future[String] =
      add(
        PaymentFile(
          quoteID = quoteID,
          negotiationID = negotiationID,
          wallexID = wallexID,
          fileID = fileID,
          fileType = fileType
        )
      )

    def insertOrUpdate(
        organizationID: String,
        negotiationID: String,
        quoteID: String,
        wallexID: String,
        fileID: String,
        fileType: String
    ): Future[String] =
      add(
        PaymentFile(
          quoteID = quoteID,
          negotiationID = negotiationID,
          wallexID = wallexID,
          fileID = fileID,
          fileType = fileType
        )
      )

    def tryGet(quoteID: String): Future[PaymentFile] =
      findById(quoteID).map { detail =>
        detail.getOrElse(
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
        )
      }

    def get(quoteID: String): Future[Option[PaymentFile]] =
      findById(quoteID)
  }
}
