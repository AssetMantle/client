package models.wallex

import exceptions.BaseException
import models.Trait.Logged
import models.common.Serializable.{EmploymentDetails, ResidentialAddressDetails}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class AccountProfile(
    wallexID: String,
    firstName: String,
    lastName: String,
    mobileNumber: String,
    gender: String,
    nationality: String,
    countryOfBirth: String,
    residentialAddressDetails: ResidentialAddressDetails,
    dateOfBirth: String,
    identificationType: String,
    identificationNumber: String,
    issueDate: String,
    expiryDate: String,
    employmentDetails: EmploymentDetails,
    createdBy: Option[String] = None,
    createdOn: Option[Timestamp] = None,
    createdOnTimeZone: Option[String] = None,
    updatedBy: Option[String] = None,
    updatedOn: Option[Timestamp] = None,
    updatedOnTimeZone: Option[String] = None
) extends Logged

@Singleton
class AccountProfiles @Inject() (
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.WALLEX_ACCOUNT_PROFILE

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val accountProfileTable =
    TableQuery[AccountProfileTable]

  private def serialize(
      accountProfile: AccountProfile
  ): AccountProfileSerialized =
    AccountProfileSerialized(
      wallexID = accountProfile.wallexID,
      firstName = accountProfile.firstName,
      lastName = accountProfile.lastName,
      mobileNumber = accountProfile.mobileNumber,
      gender = accountProfile.gender,
      nationality = accountProfile.nationality,
      countryOfBirth = accountProfile.countryOfBirth,
      residentialAddressDetails =
        Json.toJson(accountProfile.residentialAddressDetails).toString,
      dateOfBirth = accountProfile.dateOfBirth,
      identificationType = accountProfile.identificationType,
      identificationNumber = accountProfile.identificationNumber,
      issueDate = accountProfile.issueDate,
      expiryDate = accountProfile.expiryDate,
      employmentDetails =
        Json.toJson(accountProfile.employmentDetails).toString,
      createdBy = accountProfile.createdBy,
      createdOn = accountProfile.createdOn,
      createdOnTimeZone = accountProfile.createdOnTimeZone,
      updatedBy = accountProfile.updatedBy,
      updatedOn = accountProfile.updatedOn,
      updatedOnTimeZone = accountProfile.updatedOnTimeZone
    )

  private def add(
      accountProfileSerialized: AccountProfileSerialized
  ): Future[String] =
    db.run(
        (accountProfileTable returning accountProfileTable
          .map(_.wallexID) += accountProfileSerialized).asTry
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
      accountProfileSerialized: AccountProfileSerialized
  ): Future[Int] =
    db.run(
        accountProfileTable
          .insertOrUpdate(accountProfileSerialized)
          .asTry
      )
      .map {
        case Success(result) => result
        case Failure(exception) =>
          exception match {
          case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
          case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
          }
      }

  private def findByID(
      wallexID: String
  ): Future[AccountProfileSerialized] =
    db.run(
        accountProfileTable
          .filter(_.wallexID === wallexID)
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

  case class AccountProfileSerialized(
      wallexID: String,
      firstName: String,
      lastName: String,
      mobileNumber: String,
      gender: String,
      nationality: String,
      countryOfBirth: String,
      residentialAddressDetails: String,
      dateOfBirth: String,
      identificationType: String,
      identificationNumber: String,
      issueDate: String,
      expiryDate: String,
      employmentDetails: String,
      createdBy: Option[String] = None,
      createdOn: Option[Timestamp] = None,
      createdOnTimeZone: Option[String] = None,
      updatedBy: Option[String] = None,
      updatedOn: Option[Timestamp] = None,
      updatedOnTimeZone: Option[String] = None
  ) {

    def deserialize: AccountProfile =
      AccountProfile(
        wallexID = wallexID,
        firstName = firstName,
        lastName = lastName,
        mobileNumber = mobileNumber,
        gender = gender,
        nationality = nationality,
        countryOfBirth = countryOfBirth,
        residentialAddressDetails = utilities.JSON
          .convertJsonStringToObject[ResidentialAddressDetails](residentialAddressDetails),
        dateOfBirth = dateOfBirth,
        identificationType = identificationType,
        identificationNumber = identificationNumber,
        issueDate = issueDate,
        expiryDate = expiryDate,
        employmentDetails = utilities.JSON
          .convertJsonStringToObject[EmploymentDetails](employmentDetails),
        createdBy = createdBy,
        createdOn = createdOn,
        createdOnTimeZone = createdOnTimeZone,
        updatedBy = updatedBy,
        updatedOn = updatedOn,
        updatedOnTimeZone = updatedOnTimeZone
      )

  }
  private[models] class AccountProfileTable(tag: Tag)
      extends Table[AccountProfileSerialized](
        tag,
        "AccountProfile"
      ) {

    override def * =
      (
        wallexID,
        firstName,
        lastName,
        mobileNumber,
        gender,
        nationality,
        countryOfBirth,
        residentialAddressDetails,
        dateOfBirth,
        identificationType,
        identificationNumber,
        issueDate,
        expiryDate,
        employmentDetails,
        createdBy.?,
        createdOn.?,
        createdOnTimeZone.?,
        updatedBy.?,
        updatedOn.?,
        updatedOnTimeZone.?
      ) <> (AccountProfileSerialized.tupled, AccountProfileSerialized.unapply)

    def wallexID = column[String]("wallexID", O.PrimaryKey)

    def firstName = column[String]("firstName")

    def lastName = column[String]("lastName")

    def mobileNumber = column[String]("mobileNumber")

    def gender = column[String]("gender")

    def nationality = column[String]("nationality")

    def countryOfBirth = column[String]("countryOfBirth")

    def residentialAddressDetails = column[String]("residentialAddressDetails")

    def dateOfBirth = column[String]("dateOfBirth")

    def identificationType = column[String]("identificationType")

    def identificationNumber = column[String]("identificationNumber")

    def issueDate = column[String]("issueDate")

    def expiryDate = column[String]("expiryDate")

    def employmentDetails = column[String]("employmentDetails")

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
        firstName: String,
        lastName: String,
        mobileNumber: String,
        gender: String,
        nationality: String,
        countryOfBirth: String,
        residentialAddressDetails: ResidentialAddressDetails,
        dateOfBirth: String,
        identificationType: String,
        identificationNumber: String,
        issueDate: String,
        expiryDate: String,
        employmentDetails: EmploymentDetails
    ): Future[String] =
      add(
        serialize(
          AccountProfile(
            wallexID = wallexID,
            firstName = firstName,
            lastName = lastName,
            mobileNumber = mobileNumber,
            gender = gender,
            nationality = nationality,
            countryOfBirth = countryOfBirth,
            residentialAddressDetails = residentialAddressDetails,
            dateOfBirth = dateOfBirth,
            identificationType = identificationType,
            identificationNumber = identificationNumber,
            issueDate = issueDate,
            expiryDate = expiryDate,
            employmentDetails = employmentDetails
          )
        )
      )

    def insertOrUpdate(
        wallexID: String,
        firstName: String,
        lastName: String,
        mobileNumber: String,
        gender: String,
        nationality: String,
        countryOfBirth: String,
        residentialAddressDetails: ResidentialAddressDetails,
        dateOfBirth: String,
        identificationType: String,
        identificationNumber: String,
        issueDate: String,
        expiryDate: String,
        employmentDetails: EmploymentDetails
    ): Future[Int] =
      upsert(
        serialize(
          AccountProfile(
            wallexID = wallexID,
            firstName = firstName,
            lastName = lastName,
            mobileNumber = mobileNumber,
            gender = gender,
            nationality = nationality,
            countryOfBirth = countryOfBirth,
            residentialAddressDetails = residentialAddressDetails,
            dateOfBirth = dateOfBirth,
            identificationType = identificationType,
            identificationNumber = identificationNumber,
            issueDate = issueDate,
            expiryDate = expiryDate,
            employmentDetails = employmentDetails
          )
        )
      )

    def tryGetByWallexID(wallexID: String): Future[AccountProfile] =
      findByID(wallexID).map(_.deserialize)

  }

}
