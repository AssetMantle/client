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
    orgId: String,
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
          .map(_.orgId) += organizationWallexDetail).asTry
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
      orgId: String
  ): Future[Option[OrganizationWallexDetail]] =
    db.run(
      organizationWallexAccountDetailTable
        .filter(_.orgId === orgId)
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

  private[models] class OrganizationWallexAccountDetailTable(tag: Tag)
      extends Table[OrganizationWallexDetail](
        tag,
        "OrganizationWallexAccountDetail"
      ) {

    override def * =
      (
        orgId,
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

    def orgId = column[String]("orgId", O.PrimaryKey)

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
        orgId: String,
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
          orgId = orgId,
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
        orgId: String,
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
          orgId = orgId,
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

    def tryGet(orgId: String): Future[OrganizationWallexDetail] =
      findById(orgId).map { detail =>
        detail.getOrElse(
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
        )
      }

    def get(orgId: String): Future[Option[OrganizationWallexDetail]] =
      findById(orgId)

    def tryGetByAccountId(accountId: String): Future[OrganizationWallexDetail] =
      findByAccountId(accountId).map { detail =>
        detail.getOrElse(
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
        )
      }
  }

}
