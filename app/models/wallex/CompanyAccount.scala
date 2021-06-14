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

case class CompanyAccount(
    accountID: String,
    name: String,
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
class CompanyAccounts @Inject() (
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.WALLEX_ACCOUNT_COMPANY

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val companyAccountTable =
    TableQuery[CompanyAccountTable]

  private def add(
      companyAccount: CompanyAccount
  ): Future[String] =
    db.run(
        (companyAccountTable returning companyAccountTable
          .map(_.accountID) += companyAccount).asTry
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
      companyAccount: CompanyAccount
  ): Future[Int] =
    db.run(
        companyAccountTable
          .insertOrUpdate(companyAccount)
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

  private def findByID(
      accountID: String
  ): Future[Option[CompanyAccount]] =
    db.run(
      companyAccountTable
        .filter(_.accountID === accountID)
        .result
        .headOption
    )

  private[models] class CompanyAccountTable(tag: Tag)
      extends Table[CompanyAccount](
        tag,
        "CompanyAccount"
      ) {

    override def * =
      (
        accountID,
        name,
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
      ) <> (CompanyAccount.tupled, CompanyAccount.unapply)

    def accountID = column[String]("accountID", O.PrimaryKey)

    def name = column[String]("name")

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
        name: String,
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
        CompanyAccount(
          accountID = accountID,
          name = name,
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
        name: String,
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
        CompanyAccount(
          accountID,
          name,
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

    def tryGet(accountID: String): Future[CompanyAccount] =
      findByID(accountID).map { companyAccount =>
        companyAccount.getOrElse(
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
        )
      }

    def get(accountID: String): Future[Option[CompanyAccount]] =
      findByID(accountID)

  }

}
