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

case class AccountProfileDetail(
    wallexID: String,
    firstName: String,
    lastName: String,
    mobileCountryCode: String,
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
class AccountProfileDetails @Inject() (
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.WALLEX_ACCOUNT_PROFILE_DETAIL

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val accountProfileDetailTable =
    TableQuery[AccountProfileDetailTable]

  private def serialize(
      accountProfileDetail: AccountProfileDetail
  ): AccountProfileDetailSerialized =
    AccountProfileDetailSerialized(
      wallexID = accountProfileDetail.wallexID,
      firstName = accountProfileDetail.firstName,
      lastName = accountProfileDetail.lastName,
      mobileCountryCode = accountProfileDetail.mobileCountryCode,
      mobileNumber = accountProfileDetail.mobileNumber,
      gender = accountProfileDetail.gender,
      nationality = accountProfileDetail.nationality,
      countryOfBirth = accountProfileDetail.countryOfBirth,
      residentialAddressDetails =
        Json.toJson(accountProfileDetail.residentialAddressDetails).toString,
      dateOfBirth = accountProfileDetail.dateOfBirth,
      identificationType = accountProfileDetail.identificationType,
      identificationNumber = accountProfileDetail.identificationNumber,
      issueDate = accountProfileDetail.issueDate,
      expiryDate = accountProfileDetail.expiryDate,
      employmentDetails =
        Json.toJson(accountProfileDetail.employmentDetails).toString,
      createdBy = accountProfileDetail.createdBy,
      createdOn = accountProfileDetail.createdOn,
      createdOnTimeZone = accountProfileDetail.createdOnTimeZone,
      updatedBy = accountProfileDetail.updatedBy,
      updatedOn = accountProfileDetail.updatedOn,
      updatedOnTimeZone = accountProfileDetail.updatedOnTimeZone
    )

  private def add(
      accountProfileDetail: AccountProfileDetailSerialized
  ): Future[String] =
    db.run(
        (accountProfileDetailTable returning accountProfileDetailTable
          .map(_.wallexID) += accountProfileDetail).asTry
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
      accountProfileDetail: AccountProfileDetailSerialized
  ): Future[Int] =
    db.run(
        accountProfileDetailTable
          .insertOrUpdate(accountProfileDetail)
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
      wallexID: String
  ): Future[AccountProfileDetailSerialized] =
    db.run(
        accountProfileDetailTable
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

  case class AccountProfileDetailSerialized(
      wallexID: String,
      firstName: String,
      lastName: String,
      mobileCountryCode: String,
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

    def deserialize: AccountProfileDetail =
      AccountProfileDetail(
        wallexID = wallexID,
        firstName = firstName,
        lastName = lastName,
        mobileCountryCode = mobileCountryCode,
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
  private[models] class AccountProfileDetailTable(tag: Tag)
      extends Table[AccountProfileDetailSerialized](
        tag,
        "AccountProfileDetail"
      ) {

    override def * =
      (
        wallexID,
        firstName,
        lastName,
        mobileCountryCode,
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
      ) <> (AccountProfileDetailSerialized.tupled, AccountProfileDetailSerialized.unapply)

    def wallexID = column[String]("wallexID", O.PrimaryKey)

    def firstName = column[String]("firstName")

    def lastName = column[String]("lastName")

    def mobileCountryCode = column[String]("mobileCountryCode")

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
        mobileCountryCode: String,
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
          AccountProfileDetail(
            wallexID = wallexID,
            firstName = firstName,
            lastName = lastName,
            mobileCountryCode = mobileCountryCode,
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
        mobileCountryCode: String,
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
          AccountProfileDetail(
            wallexID = wallexID,
            firstName = firstName,
            lastName = lastName,
            mobileCountryCode = mobileCountryCode,
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

    def tryGetByWallexID(wallexID: String): Future[AccountProfileDetail] =
      findById(wallexID).map(_.deserialize)

  }

}
