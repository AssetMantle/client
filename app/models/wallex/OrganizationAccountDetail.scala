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

case class OrganizationAccountDetail(
    wallexID: String,
    organizationID: String,
    accountID: String,
    email: String,
    firstName: String,
    lastName: String,
    status: String,
    countryCode: String,
    accountType: String,
    createdBy: Option[String] = None,
    createdOn: Option[Timestamp] = None,
    createdOnTimeZone: Option[String] = None,
    updatedBy: Option[String] = None,
    updatedOn: Option[Timestamp] = None,
    updatedOnTimeZone: Option[String] = None
) extends Logged

@Singleton
class OrganizationAccountDetails @Inject() (
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.WALLEX_ORGANIZATION_ACCOUNT_DETAIL

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val organizationAccountDetailTable =
    TableQuery[OrganizationAccountDetailTable]

  private def add(
      organizationAccountDetail: OrganizationAccountDetail
  ): Future[String] =
    db.run(
        (organizationAccountDetailTable returning organizationAccountDetailTable
          .map(_.organizationID) += organizationAccountDetail).asTry
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
      organizationAccountDetail: OrganizationAccountDetail
  ): Future[Int] =
    db.run(
        organizationAccountDetailTable
          .insertOrUpdate(organizationAccountDetail)
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
      organizationID: String
  ): Future[Option[OrganizationAccountDetail]] =
    db.run(
      organizationAccountDetailTable
        .filter(_.organizationID === organizationID)
        .result
        .headOption
    )

  private def findByAccountId(
      accountID: String
  ): Future[Option[OrganizationAccountDetail]] =
    db.run(
      organizationAccountDetailTable
        .filter(_.accountID === accountID)
        .result
        .headOption
    )

  private def updateStatusById(
      wallexID: String,
      status: String
  ): Future[Int] =
    db.run(
        organizationAccountDetailTable
          .filter(_.wallexID === wallexID)
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

  private def getByOrganizationIDsAndStatus(
      organizationIDs: Seq[String],
      status: String
  ): Future[Seq[OrganizationAccountDetail]] =
    db.run(
      organizationAccountDetailTable
        .filter(_.organizationID inSet organizationIDs)
        .filter(_.status === status)
        .result
    )

  private[models] class OrganizationAccountDetailTable(tag: Tag)
      extends Table[OrganizationAccountDetail](
        tag,
        "OrganizationAccountDetail"
      ) {

    override def * =
      (
        wallexID,
        organizationID,
        accountID,
        email,
        firstName,
        lastName,
        status,
        countryCode,
        accountType,
        createdBy.?,
        createdOn.?,
        createdOnTimeZone.?,
        updatedBy.?,
        updatedOn.?,
        updatedOnTimeZone.?
      ) <> (OrganizationAccountDetail.tupled, OrganizationAccountDetail.unapply)

    def wallexID = column[String]("wallexID", O.PrimaryKey)

    def organizationID = column[String]("organizationID")

    def accountID = column[String]("accountID")

    def email = column[String]("email")

    def firstName = column[String]("firstName")

    def lastName = column[String]("lastName")

    def status = column[String]("status")

    def countryCode = column[String]("countryCode")

    def accountType = column[String]("accountType")

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
        organizationID: String,
        accountID: String,
        email: String,
        firstName: String,
        lastName: String,
        status: String,
        countryCode: String,
        accountType: String
    ): Future[String] =
      add(
        OrganizationAccountDetail(
          wallexID = wallexID,
          organizationID = organizationID,
          accountID = accountID,
          email = email,
          firstName = firstName,
          lastName = lastName,
          status = status,
          countryCode = countryCode,
          accountType = accountType
        )
      )

    def insertOrUpdate(
        wallexID: String,
        organizationID: String,
        accountID: String,
        email: String,
        firstName: String,
        lastName: String,
        status: String,
        countryCode: String,
        accountType: String
    ): Future[Int] =
      upsert(
        OrganizationAccountDetail(
          wallexID = wallexID,
          organizationID = organizationID,
          accountID = accountID,
          email = email,
          firstName = firstName,
          lastName = lastName,
          status = status,
          countryCode = countryCode,
          accountType = accountType
        )
      )

    def updateStatus(
        wallexID: String,
        status: String
    ): Future[Int] =
      updateStatusById(wallexID, status)

    def tryGet(organizationID: String): Future[OrganizationAccountDetail] =
      findById(organizationID).map { detail =>
        detail.getOrElse(
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
        )
      }

    def get(organizationID: String): Future[Option[OrganizationAccountDetail]] =
      findById(organizationID)

    def tryGetByAccountId(
        accountID: String
    ): Future[OrganizationAccountDetail] =
      findByAccountId(accountID).map { detail =>
        detail.getOrElse(
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
        )
      }

    def tryGetPendingScreening(
        organizationIDs: Seq[String]
    ): Future[Seq[OrganizationAccountDetail]] =
      getByOrganizationIDsAndStatus(
        organizationIDs,
        constants.Status.SendWalletTransfer.ZONE_SEND_FOR_SCREENING
      )
  }

}
