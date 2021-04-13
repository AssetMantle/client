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

case class OrganizationWallexDetail(
    zoneID: String,
    organizationID: String,
    wallexId: String,
    email: String,
    firstName: String,
    lastName: String,
    status: String,
    accountId: String,
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
class OrganizationWallexDetails @Inject() (
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.ORGANIZATION_WALLEX_ACCOUNT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val organizationWallexAccountDetailTable =
    TableQuery[OrganizationWallexAccountDetailTable]

  private def add(
      organizationWallexDetail: OrganizationWallexDetail
  ): Future[String] =
    db.run(
        (organizationWallexAccountDetailTable returning organizationWallexAccountDetailTable
          .map(_.organizationID) += organizationWallexDetail).asTry
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
      organizationWallexDetail: OrganizationWallexDetail
  ): Future[Int] =
    db.run(
        organizationWallexAccountDetailTable
          .insertOrUpdate(organizationWallexDetail)
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
  ): Future[Option[OrganizationWallexDetail]] =
    db.run(
      organizationWallexAccountDetailTable
        .filter(_.organizationID === organizationID)
        .result
        .headOption
    )

  private def findByAccountId(
      accountId: String
  ): Future[Option[OrganizationWallexDetail]] =
    db.run(
      organizationWallexAccountDetailTable
        .filter(_.accountId === accountId)
        .result
        .headOption
    )

  private def findByZoneID(
      zoneID: String
  ): Future[Seq[OrganizationWallexDetail]] =
    db.run(
      organizationWallexAccountDetailTable
        .filter(_.zoneID === zoneID)
        .filter(_.status === constants.Status.SendWalletTransfer.ZONE_SEND_FOR_SCREENING)
        .result
    )

  private def updateStatusById(
      wallexID: String,
      status: String
  ): Future[Int] =
    db.run(
        organizationWallexAccountDetailTable
          .filter(_.wallexId === wallexID)
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

  private[models] class OrganizationWallexAccountDetailTable(tag: Tag)
      extends Table[OrganizationWallexDetail](
        tag,
        "OrganizationWallexAccountDetail"
      ) {

    override def * =
      (
        zoneID,
        organizationID,
        wallexId,
        email,
        firstName,
        lastName,
        status,
        accountId,
        countryCode,
        accountType,
        createdBy.?,
        createdOn.?,
        createdOnTimeZone.?,
        updatedBy.?,
        updatedOn.?,
        updatedOnTimeZone.?
      ) <> (OrganizationWallexDetail.tupled, OrganizationWallexDetail.unapply)

    def zoneID = column[String]("zoneID", O.PrimaryKey)

    def organizationID = column[String]("organizationID", O.PrimaryKey)

    def wallexId = column[String]("wallexId", O.PrimaryKey)

    def email = column[String]("email")

    def firstName = column[String]("firstName")

    def lastName = column[String]("lastName")

    def status = column[String]("status")

    def accountId = column[String]("accountId", O.PrimaryKey)

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
        zoneID: String,
        organizationID: String,
        wallexId: String,
        email: String,
        firstName: String,
        lastName: String,
        status: String,
        accountId: String,
        countryCode: String,
        accountType: String
    ): Future[String] =
      add(
        OrganizationWallexDetail(
          zoneID = zoneID,
          organizationID = organizationID,
          wallexId = wallexId,
          email = email,
          firstName = firstName,
          lastName = lastName,
          status = status,
          accountId = accountId,
          countryCode = countryCode,
          accountType = accountType
        )
      )

    def insertOrUpdate(
        zoneID: String,
        organizationID: String,
        wallexId: String,
        email: String,
        firstName: String,
        lastName: String,
        status: String,
        accountId: String,
        countryCode: String,
        accountType: String
    ): Future[Int] =
      upsert(
        OrganizationWallexDetail(
          zoneID = zoneID,
          organizationID = organizationID,
          wallexId = wallexId,
          email = email,
          firstName = firstName,
          lastName = lastName,
          status = status,
          accountId = accountId,
          countryCode = countryCode,
          accountType = accountType
        )
      )

    def updateStatus(
        wallexID: String,
        status: String
    ): Future[Int] =
      updateStatusById(wallexID, status)

    def tryGet(organizationID: String): Future[OrganizationWallexDetail] =
      findById(organizationID).map { detail =>
        detail.getOrElse(
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
        )
      }

    def get(organizationID: String): Future[Option[OrganizationWallexDetail]] =
      findById(organizationID)

    def tryGetByAccountId(accountId: String): Future[OrganizationWallexDetail] =
      findByAccountId(accountId).map { detail =>
        detail.getOrElse(
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
        )
      }

    def tryGetPendingByZoneID(zoneID: String): Future[Seq[OrganizationWallexDetail]] =
      findByZoneID(zoneID)
  }

}
