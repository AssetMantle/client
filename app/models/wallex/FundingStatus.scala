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

case class FundingStatus(
    id: String,
    balanceID: String,
    accountID: String,
    amount: String,
    reference: String,
    status: String,
    createdBy: Option[String] = None,
    createdOn: Option[Timestamp] = None,
    createdOnTimeZone: Option[String] = None,
    updatedBy: Option[String] = None,
    updatedOn: Option[Timestamp] = None,
    updatedOnTimeZone: Option[String] = None
) extends Logged

@Singleton
class FundingStatusDetails @Inject() (
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.WALLEX_FUNDING_STATUS

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val fundingStatusTable =
    TableQuery[FundingStatusTable]

  private def add(
      fundingStatus: FundingStatus
  ): Future[String] =
    db.run(
        (fundingStatusTable returning fundingStatusTable
          .map(_.id) += fundingStatus).asTry
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
      fundingStatus: FundingStatus
  ): Future[Int] =
    db.run(
        fundingStatusTable
          .insertOrUpdate(fundingStatus)
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
      id: String
  ): Future[Option[FundingStatus]] =
    db.run(
      fundingStatusTable
        .filter(_.id === id)
        .result
        .headOption
    )

  private def updateStatusById(
      id: String,
      status: String
  ): Future[Int] =
    db.run(
        fundingStatusTable
          .filter(_.id === id)
          .map(_.status)
          .update(status)
          .asTry
      )
      .map {
        case Success(result) => result match {
          case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
          case _ => result
        }
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

  private[models] class FundingStatusTable(tag: Tag)
      extends Table[FundingStatus](
        tag,
        "FundingStatus"
      ) {

    override def * =
      (
        id,
        balanceID,
        accountID,
        amount,
        reference,
        status,
        createdBy.?,
        createdOn.?,
        createdOnTimeZone.?,
        updatedBy.?,
        updatedOn.?,
        updatedOnTimeZone.?
      ) <> (FundingStatus.tupled, FundingStatus.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def balanceID = column[String]("balanceID")

    def accountID = column[String]("accountID")

    def amount = column[String]("amount")

    def reference = column[String]("reference")

    def status = column[String]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {
    def create(
        id: String,
        balanceID: String,
        accountID: String,
        amount: String,
        reference: String,
        status: String
    ): Future[String] =
      add(
        FundingStatus(
          id = id,
          balanceID = balanceID,
          accountID = accountID,
          amount = amount,
          reference = reference,
          status = status
        )
      )

    def insertOrUpdate(
        id: String,
        balanceID: String,
        accountID: String,
        amount: String,
        reference: String,
        status: String
    ): Future[Int] =
      upsert(
        FundingStatus(
          id = id,
          balanceID = balanceID,
          accountID = accountID,
          amount = amount,
          reference = reference,
          status = status
        )
      )

    def updateStatus(
        id: String,
        status: String
    ): Future[Int] =
      updateStatusById(id, status)

    def tryGet(id: String): Future[FundingStatus] =
      findById(id).map { detail =>
        detail.getOrElse(
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
        )
      }

    def get(id: String): Future[Option[FundingStatus]] =
      findById(id)

  }

}
