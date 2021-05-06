package models.wallex

import exceptions.BaseException
import models.Trait.Logged
import models.common.Serializable.BankAccount
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class OrganizationBeneficiary(
    organizationID: String,
    wallexID: String,
    beneficiaryID: String,
    address: String,
    country: String,
    city: String,
    entityType: String,
    companyName: String,
    nickname: String,
    accountType: String,
    bankAccount: BankAccount,
    createdBy: Option[String] = None,
    createdOn: Option[Timestamp] = None,
    createdOnTimeZone: Option[String] = None,
    updatedBy: Option[String] = None,
    updatedOn: Option[Timestamp] = None,
    updatedOnTimeZone: Option[String] = None
) extends Logged

@Singleton
class OrganizationBeneficiaries @Inject()(
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.ORGANIZATION_WALLEX_BENEFICIARY_DETAILS

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val beneficiaryTable =
    TableQuery[OrganizationBeneficiaryTable]

  private def serialize(
      beneficiaryDetail: OrganizationBeneficiary
  ): OrganizationBeneficiarySerialized =
    OrganizationBeneficiarySerialized(
      organizationID = beneficiaryDetail.organizationID,
      wallexID = beneficiaryDetail.wallexID,
      beneficiaryID = beneficiaryDetail.beneficiaryID,
      address = beneficiaryDetail.address,
      country = beneficiaryDetail.country,
      city = beneficiaryDetail.city,
      entityType = beneficiaryDetail.entityType,
      companyName = beneficiaryDetail.companyName,
      nickname = beneficiaryDetail.nickname,
      accountType = beneficiaryDetail.accountType,
      bankAccount = Json.toJson(beneficiaryDetail.bankAccount).toString,
      createdBy = beneficiaryDetail.createdBy,
      createdOn = beneficiaryDetail.createdOn,
      createdOnTimeZone = beneficiaryDetail.createdOnTimeZone,
      updatedBy = beneficiaryDetail.updatedBy,
      updatedOn = beneficiaryDetail.updatedOn,
      updatedOnTimeZone = beneficiaryDetail.updatedOnTimeZone
    )

  private def add(
      orgWallexBeneficiaryDetail: OrganizationBeneficiarySerialized
  ): Future[String] =
    db.run(
        (beneficiaryTable returning beneficiaryTable
          .map(_.organizationID) += orgWallexBeneficiaryDetail).asTry
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
      organizationWallexDetail: OrganizationBeneficiarySerialized
  ): Future[Int] =
    db.run(
        beneficiaryTable
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

  private def getByOrganizationID(
      organizationID: String
  ): Future[Seq[OrganizationBeneficiarySerialized]] =
    db.run(
      beneficiaryTable
        .filter(_.organizationID === organizationID)
        .result
    )

  private def tryGetByOrganizationID(
      organizationID: String
  ): Future[OrganizationBeneficiarySerialized] =
    db.run(
        beneficiaryTable
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

  private def tryGetByBeneficiaryId(
      beneficiaryID: String
  ): Future[OrganizationBeneficiarySerialized] =
    db.run(
        beneficiaryTable
          .filter(_.beneficiaryID === beneficiaryID)
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

  private def deleteById(beneficiaryID: String) =
    db.run(
        beneficiaryTable
          .filter(_.beneficiaryID === beneficiaryID)
          .delete
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
            case psqlException: PSQLException =>
              throw new BaseException(
                constants.Response.PSQL_EXCEPTION,
                psqlException
              )
          }
      }

  case class OrganizationBeneficiarySerialized(
      organizationID: String,
      wallexID: String,
      beneficiaryID: String,
      address: String,
      country: String,
      city: String,
      entityType: String,
      companyName: String,
      nickname: String,
      accountType: String,
      bankAccount: String,
      createdBy: Option[String] = None,
      createdOn: Option[Timestamp] = None,
      createdOnTimeZone: Option[String] = None,
      updatedBy: Option[String] = None,
      updatedOn: Option[Timestamp] = None,
      updatedOnTimeZone: Option[String] = None
  ) {

    def deserialize: OrganizationBeneficiary =
      OrganizationBeneficiary(
        organizationID = organizationID,
        wallexID = wallexID,
        beneficiaryID = beneficiaryID,
        address = address,
        country = country,
        city = city,
        entityType = entityType,
        companyName = companyName,
        nickname = nickname,
        accountType = accountType,
        bankAccount =
          utilities.JSON.convertJsonStringToObject[BankAccount](bankAccount),
        createdBy = createdBy,
        createdOn = createdOn,
        createdOnTimeZone = createdOnTimeZone,
        updatedBy = updatedBy,
        updatedOn = updatedOn,
        updatedOnTimeZone = updatedOnTimeZone
      )

  }

  private[models] class OrganizationBeneficiaryTable(tag: Tag)
      extends Table[OrganizationBeneficiarySerialized](
        tag,
        "OrganizationBeneficiary"
      ) {

    override def * =
      (
        organizationID,
        wallexID,
        beneficiaryID,
        address,
        country,
        city,
        entityType,
        companyName,
        nickname,
        accountType,
        bankAccount,
        createdBy.?,
        createdOn.?,
        createdOnTimeZone.?,
        updatedBy.?,
        updatedOn.?,
        updatedOnTimeZone.?
      ) <> (OrganizationBeneficiarySerialized.tupled, OrganizationBeneficiarySerialized.unapply)

    def organizationID = column[String]("organizationID", O.PrimaryKey)

    def wallexID = column[String]("wallexID", O.PrimaryKey)

    def beneficiaryID = column[String]("beneficiaryID", O.PrimaryKey)

    def address = column[String]("address")

    def country = column[String]("country")

    def city = column[String]("city")

    def entityType = column[String]("entityType")

    def companyName = column[String]("companyName")

    def nickname = column[String]("nickname")

    def accountType = column[String]("accountType")

    def bankAccount = column[String]("bankAccount")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {
    def create(
        organizationID: String,
        wallexID: String,
        beneficiaryID: String,
        address: String,
        country: String,
        city: String,
        entityType: String,
        companyName: String,
        nickname: String,
        accountType: String,
        bankAccount: BankAccount
    ): Future[String] =
      add(
        serialize(
          OrganizationBeneficiary(
            organizationID = organizationID,
            wallexID = wallexID,
            beneficiaryID = beneficiaryID,
            address = address,
            country = country,
            city = city,
            entityType = entityType,
            companyName = companyName,
            nickname = nickname,
            accountType = accountType,
            bankAccount = bankAccount
          )
        )
      )

    def insertOrUpdate(
        organizationID: String,
        wallexID: String,
        beneficiaryID: String,
        address: String,
        country: String,
        city: String,
        entityType: String,
        companyName: String,
        nickname: String,
        accountType: String,
        bankAccount: BankAccount
    ): Future[Int] =
      upsert(
        serialize(
          OrganizationBeneficiary(
            organizationID = organizationID,
            wallexID = wallexID,
            beneficiaryID = beneficiaryID,
            address = address,
            country = country,
            city = city,
            entityType = entityType,
            companyName = companyName,
            nickname = nickname,
            accountType = accountType,
            bankAccount = bankAccount
          )
        )
      )

    def tryGet(organizationID: String): Future[OrganizationBeneficiary] =
      tryGetByOrganizationID(organizationID).map(_.deserialize)

    def get(
        organizationID: String
    ): Future[Seq[OrganizationBeneficiary]] =
      getByOrganizationID(organizationID).map(_.map(_.deserialize))

    def getByBeneficiaryId(
        beneficiaryID: String
    ): Future[OrganizationBeneficiary] =
      tryGetByBeneficiaryId(beneficiaryID).map(_.deserialize)

    def delete(beneficiaryID: String): Future[Int] =
      deleteById(beneficiaryID)
  }

}
