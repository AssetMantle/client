package models.wallex

import exceptions.BaseException
import models.Trait.Logged
import models.common.Serializable.{Company, UserProfile, companyReads}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class OrganizationAccount(
    wallexID: String,
    organizationID: String,
    accountID: String,
    email: String,
    firstName: String,
    lastName: String,
    status: String,
    countryCode: String,
    accountType: String,
    traderID: String,
    company: Option[Company] = None,
    userProfile: Option[UserProfile] = None,
    createdBy: Option[String] = None,
    createdOn: Option[Timestamp] = None,
    createdOnTimeZone: Option[String] = None,
    updatedBy: Option[String] = None,
    updatedOn: Option[Timestamp] = None,
    updatedOnTimeZone: Option[String] = None
) extends Logged

@Singleton
class OrganizationAccounts @Inject() (
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.WALLEX_ORGANIZATION_ACCOUNT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val organizationAccountTable =
    TableQuery[OrganizationAccountTable]

  private def serialize(
      organizationAccount: OrganizationAccount
  ): OrganizationAccountSerialized =
    OrganizationAccountSerialized(
      wallexID = organizationAccount.wallexID,
      organizationID = organizationAccount.organizationID,
      accountID = organizationAccount.accountID,
      email = organizationAccount.email,
      firstName = organizationAccount.firstName,
      lastName = organizationAccount.lastName,
      status = organizationAccount.status,
      countryCode = organizationAccount.countryCode,
      accountType = organizationAccount.accountType,
      traderID = organizationAccount.traderID,
      company = Option(Json.toJson(organizationAccount.company).toString),
      userProfile = Option(Json.toJson(organizationAccount.userProfile).toString),
      createdBy = organizationAccount.createdBy,
      createdOn = organizationAccount.createdOn,
      createdOnTimeZone = organizationAccount.createdOnTimeZone,
      updatedBy = organizationAccount.updatedBy,
      updatedOn = organizationAccount.updatedOn,
      updatedOnTimeZone = organizationAccount.updatedOnTimeZone
    )

  private def add(
      organizationAccountSerialized: OrganizationAccountSerialized
  ): Future[String] =
    db.run(
        (organizationAccountTable returning organizationAccountTable
          .map(_.organizationID) += organizationAccountSerialized).asTry
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
      organizationAccountSerialized: OrganizationAccountSerialized
  ): Future[Int] =
    db.run(
        organizationAccountTable
          .insertOrUpdate(organizationAccountSerialized)
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
  ): Future[Option[OrganizationAccountSerialized]] =
    db.run(
      organizationAccountTable
        .filter(_.organizationID === organizationID)
        .result
        .headOption
    )

  private def findByAccountId(
      accountID: String
  ): Future[OrganizationAccountSerialized] =
    db.run(
        organizationAccountTable
          .filter(_.accountID === accountID)
          .result
          .head
          .asTry
      )
      .map {
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

  private def updateStatusById(
      wallexID: String,
      status: String
  ): Future[Int] =
    db.run(
        organizationAccountTable
          .filter(_.wallexID === wallexID)
          .map(_.status)
          .update(status)
          .asTry
      )
      .map {
        case Success(result) =>
          result match {
            case 0 =>
              throw new BaseException(
                constants.Response.NO_SUCH_ELEMENT_EXCEPTION
              )
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

  private def getByOrganizationIDsAndStatus(
      organizationIDs: Seq[String],
      status: String
  ): Future[Seq[OrganizationAccountSerialized]] =
    db.run(
      organizationAccountTable
        .filter(_.organizationID inSet organizationIDs)
        .filter(_.status === status)
        .result
    )

  private def tryGetByOrganizationID(
      organizationID: String
  ): Future[OrganizationAccountSerialized] =
    db.run(
        organizationAccountTable
          .filter(_.organizationID === organizationID)
          .result
          .head
          .asTry
      )
      .map {
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

  private[models] class OrganizationAccountTable(tag: Tag)
      extends Table[OrganizationAccountSerialized](
        tag,
        "OrganizationAccount"
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
        traderID,
        company.?,
        userProfile.?,
        createdBy.?,
        createdOn.?,
        createdOnTimeZone.?,
        updatedBy.?,
        updatedOn.?,
        updatedOnTimeZone.?
      ) <> (OrganizationAccountSerialized.tupled, OrganizationAccountSerialized.unapply)

    def wallexID = column[String]("wallexID", O.PrimaryKey)

    def organizationID = column[String]("organizationID")

    def accountID = column[String]("accountID")

    def email = column[String]("email")

    def firstName = column[String]("firstName")

    def lastName = column[String]("lastName")

    def status = column[String]("status")

    def countryCode = column[String]("countryCode")

    def accountType = column[String]("accountType")

    def traderID = column[String]("traderID")

    def company = column[String]("company")

    def userProfile = column[String]("userProfile")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  case class OrganizationAccountSerialized(
      wallexID: String,
      organizationID: String,
      accountID: String,
      email: String,
      firstName: String,
      lastName: String,
      status: String,
      countryCode: String,
      accountType: String,
      traderID: String,
      company: Option[String],
      userProfile: Option[String],
      createdBy: Option[String] = None,
      createdOn: Option[Timestamp] = None,
      createdOnTimeZone: Option[String] = None,
      updatedBy: Option[String] = None,
      updatedOn: Option[Timestamp] = None,
      updatedOnTimeZone: Option[String] = None
  ) {

    def deserialize: OrganizationAccount =
      OrganizationAccount(
        wallexID = wallexID,
        organizationID = organizationID,
        accountID = accountID,
        email = email,
        firstName = firstName,
        lastName = lastName,
        status = status,
        countryCode = countryCode,
        accountType = accountType,
        traderID = traderID,
        company = company match { case Some(company) => Option(utilities.JSON.convertJsonStringToObject[Company](company)) case None => None},
        userProfile = userProfile match { case Some(userProfile) => Option(utilities.JSON.convertJsonStringToObject[UserProfile](userProfile)) case None => None},
        createdBy = createdBy,
        createdOn = createdOn,
        createdOnTimeZone = createdOnTimeZone,
        updatedBy = updatedBy,
        updatedOn = updatedOn,
        updatedOnTimeZone = updatedOnTimeZone
      )

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
        accountType: String,
        traderID: String,
        company: Company,
        userProfile: UserProfile
    ): Future[String] =
      add(
        serialize(
          OrganizationAccount(
            wallexID = wallexID,
            organizationID = organizationID,
            accountID = accountID,
            email = email,
            firstName = firstName,
            lastName = lastName,
            status = status,
            countryCode = countryCode,
            accountType = accountType,
            traderID = traderID,
            company = Some(company),
            userProfile = Some(userProfile)
          )
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
        accountType: String,
        traderID: String,
        company: Option[Company] = None,
        userProfile: Option[UserProfile] = None
    ): Future[Int] =
      upsert(
        serialize(
          OrganizationAccount(
            wallexID = wallexID,
            organizationID = organizationID,
            accountID = accountID,
            email = email,
            firstName = firstName,
            lastName = lastName,
            status = status,
            countryCode = countryCode,
            accountType = accountType,
            traderID = traderID,
            company = company,
            userProfile = userProfile
          )
        )
      )

    def updateStatus(
        wallexID: String,
        status: String
    ): Future[Int] =
      updateStatusById(wallexID, status)

    def tryGet(organizationID: String): Future[OrganizationAccount] =
      tryGetByOrganizationID(organizationID).map(_.deserialize)

    def get(organizationID: String): Future[Option[OrganizationAccount]] =
      findById(organizationID).map(_.map(_.deserialize))

    def tryGetByAccountId(
        accountID: String
    ): Future[OrganizationAccount] =
      findByAccountId(accountID).map(_.deserialize)

    def tryGetPendingScreening(
        organizationIDs: Seq[String]
    ): Future[Seq[OrganizationAccount]] =
      getByOrganizationIDsAndStatus(
        organizationIDs,
        constants.Status.SendWalletTransfer.ZONE_SEND_FOR_SCREENING
      ).map(_.map(_.deserialize))
  }

}
