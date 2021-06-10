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

case class AccountCompanyDetail(
    accountID: String,
    companyName: String,
    countryOfIncorporation: String,
    countryOfOperations: String,
    businessType: String,
    companyAddress: String,
    postalCode: String,
    state: String,
    city: String,
    registrationNumber: String,
    incorporationDate: String,
    createdBy: Option[String] = None,
    createdOn: Option[Timestamp] = None,
    createdOnTimeZone: Option[String] = None,
    updatedBy: Option[String] = None,
    updatedOn: Option[Timestamp] = None,
    updatedOnTimeZone: Option[String] = None
) extends Logged

@Singleton
class AccountCompanyDetails @Inject() (
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.WALLEX_ACCOUNT_COMPANY_DETAIL

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val accountCompanyDetailTable =
    TableQuery[AccountCompanyDetailTable]

  private def add(
      accountCompanyDetail: AccountCompanyDetail
  ): Future[String] =
    db.run(
        (accountCompanyDetailTable returning accountCompanyDetailTable
          .map(_.accountID) += accountCompanyDetail).asTry
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
      accountCompanyDetail: AccountCompanyDetail
  ): Future[Int] =
    db.run(
        accountCompanyDetailTable
          .insertOrUpdate(accountCompanyDetail)
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
      accountID: String
  ): Future[Option[AccountCompanyDetail]] =
    db.run(
      accountCompanyDetailTable
        .filter(_.accountID === accountID)
        .result
        .headOption
    )

  private[models] class AccountCompanyDetailTable(tag: Tag)
      extends Table[AccountCompanyDetail](
        tag,
        "AccountCompanyDetail"
      ) {

    override def * =
      (
        accountID,
        companyName,
        countryOfIncorporation,
        countryOfOperations,
        businessType,
        companyAddress,
        postalCode,
        state,
        city,
        registrationNumber,
        incorporationDate,
        createdBy.?,
        createdOn.?,
        createdOnTimeZone.?,
        updatedBy.?,
        updatedOn.?,
        updatedOnTimeZone.?
      ) <> (AccountCompanyDetail.tupled, AccountCompanyDetail.unapply)

    def accountID = column[String]("accountID", O.PrimaryKey)

    def companyName = column[String]("companyName")

    def countryOfIncorporation = column[String]("countryOfIncorporation")

    def countryOfOperations = column[String]("countryOfOperations")

    def businessType = column[String]("businessType")

    def companyAddress = column[String]("companyAddress")

    def postalCode = column[String]("postalCode")

    def state = column[String]("state")

    def city = column[String]("city")

    def registrationNumber = column[String]("registrationNumber")

    def incorporationDate = column[String]("incorporationDate")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {
    def create(
        accountID: String,
        companyName: String,
        countryOfIncorporation: String,
        countryOfOperations: String,
        businessType: String,
        companyAddress: String,
        postalCode: String,
        state: String,
        city: String,
        registrationNumber: String,
        incorporationDate: String
    ): Future[String] =
      add(
        AccountCompanyDetail(
          accountID = accountID,
          companyName = companyName,
          countryOfIncorporation = countryOfIncorporation,
          countryOfOperations = countryOfOperations,
          businessType = businessType,
          companyAddress = companyAddress,
          postalCode = postalCode,
          state = state,
          city = city,
          registrationNumber = registrationNumber,
          incorporationDate = incorporationDate
        )
      )

    def insertOrUpdate(
        accountID: String,
        companyName: String,
        countryOfIncorporation: String,
        countryOfOperations: String,
        businessType: String,
        companyAddress: String,
        postalCode: String,
        state: String,
        city: String,
        registrationNumber: String,
        incorporationDate: String
    ): Future[Int] =
      upsert(
        AccountCompanyDetail(
          accountID,
          companyName,
          countryOfIncorporation,
          countryOfOperations,
          businessType,
          companyAddress,
          postalCode,
          state,
          city,
          registrationNumber,
          incorporationDate
        )
      )

    def tryGet(accountID: String): Future[AccountCompanyDetail] =
      findById(accountID).map { detail =>
        detail.getOrElse(
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
        )
      }

    def get(accountID: String): Future[Option[AccountCompanyDetail]] =
      findById(accountID)

  }

}
