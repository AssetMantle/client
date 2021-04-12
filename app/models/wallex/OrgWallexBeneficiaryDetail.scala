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

case class OrgWallexBeneficiaryDetail(
    orgId: String,
    traderId: String,
    wallexId: String,
    beneficiaryId: String,
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
class OrgWallexBeneficiaryDetails @Inject() (
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.ORGANIZATION_WALLEX_BENEFICIARY_DETAILS

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val orgWallexBeneficiaryDetailTable =
    TableQuery[OrgWallexBeneficiaryDetailTable]

  private def serialize(
      beneficiaryDetail: OrgWallexBeneficiaryDetail
  ): OrgWallexBeneficiaryDetailSerialized =
    OrgWallexBeneficiaryDetailSerialized(
      orgId = beneficiaryDetail.orgId,
      traderId = beneficiaryDetail.traderId,
      wallexId = beneficiaryDetail.wallexId,
      beneficiaryId = beneficiaryDetail.beneficiaryId,
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
      orgWallexBeneficiaryDetail: OrgWallexBeneficiaryDetailSerialized
  ): Future[String] =
    db.run(
        (orgWallexBeneficiaryDetailTable returning orgWallexBeneficiaryDetailTable
          .map(_.orgId) += orgWallexBeneficiaryDetail).asTry
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
      organizationWallexDetail: OrgWallexBeneficiaryDetailSerialized
  ): Future[Int] =
    db.run(
        orgWallexBeneficiaryDetailTable
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

  private def getByOrgId(
      orgId: String
  ): Future[Option[OrgWallexBeneficiaryDetailSerialized]] =
    db.run(
      orgWallexBeneficiaryDetailTable
        .filter(_.orgId === orgId)
        .result
        .headOption
    )

  private def tryGetByOrdId(
      orgId: String
  ): Future[OrgWallexBeneficiaryDetailSerialized] =
    db.run(
        orgWallexBeneficiaryDetailTable
          .filter(_.orgId === orgId)
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
      beneficiaryId: String
  ): Future[OrgWallexBeneficiaryDetailSerialized] =
    db.run(
        orgWallexBeneficiaryDetailTable
          .filter(_.beneficiaryId === beneficiaryId)
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
  private def tryGetByTraderId(
      traderId: String
  ): Future[OrgWallexBeneficiaryDetailSerialized] =
    db.run(
        orgWallexBeneficiaryDetailTable
          .filter(_.traderId === traderId)
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

  private def deleteById(beneficiaryId: String) =
    db.run(
        orgWallexBeneficiaryDetailTable
          .filter(_.beneficiaryId === beneficiaryId)
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

  case class OrgWallexBeneficiaryDetailSerialized(
      orgId: String,
      traderId: String,
      wallexId: String,
      beneficiaryId: String,
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

    def deserialize: OrgWallexBeneficiaryDetail =
      OrgWallexBeneficiaryDetail(
        orgId = orgId,
        traderId = traderId,
        wallexId = wallexId,
        beneficiaryId = beneficiaryId,
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

  private[models] class OrgWallexBeneficiaryDetailTable(tag: Tag)
      extends Table[OrgWallexBeneficiaryDetailSerialized](
        tag,
        "WallexBeneficiaryDetail"
      ) {

    override def * =
      (
        orgId,
        traderId,
        wallexId,
        beneficiaryId,
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
      ) <> (OrgWallexBeneficiaryDetailSerialized.tupled, OrgWallexBeneficiaryDetailSerialized.unapply)

    def orgId = column[String]("orgId", O.PrimaryKey)

    def traderId = column[String]("traderId", O.PrimaryKey)

    def wallexId = column[String]("wallexId", O.PrimaryKey)

    def beneficiaryId = column[String]("beneficiaryId", O.PrimaryKey)

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
        orgId: String,
        traderId: String,
        wallexId: String,
        beneficiaryId: String,
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
    ): Future[String] =
      add(
        serialize(
          OrgWallexBeneficiaryDetail(
            orgId = orgId,
            traderId = traderId,
            wallexId = wallexId,
            beneficiaryId = beneficiaryId,
            address = address,
            country = country,
            city = city,
            entityType = entityType,
            companyName = companyName,
            nickname = nickname,
            accountType = accountType,
            bankAccount = bankAccount,
            createdBy = createdBy,
            createdOn = createdOn,
            createdOnTimeZone = createdOnTimeZone,
            updatedBy = updatedBy,
            updatedOn = updatedOn,
            updatedOnTimeZone = updatedOnTimeZone
          )
        )
      )

    def insertOrUpdate(
        orgId: String,
        traderId: String,
        wallexId: String,
        beneficiaryId: String,
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
    ): Future[Int] =
      upsert(
        serialize(
          OrgWallexBeneficiaryDetail(
            orgId = orgId,
            traderId = traderId,
            wallexId = wallexId,
            beneficiaryId = beneficiaryId,
            address = address,
            country = country,
            city = city,
            entityType = entityType,
            companyName = companyName,
            nickname = nickname,
            accountType = accountType,
            bankAccount = bankAccount,
            createdBy = createdBy,
            createdOn = createdOn,
            createdOnTimeZone = createdOnTimeZone,
            updatedBy = updatedBy,
            updatedOn = updatedOn,
            updatedOnTimeZone = updatedOnTimeZone
          )
        )
      )

    def tryGet(orgId: String): Future[OrgWallexBeneficiaryDetail] =
      tryGetByOrdId(orgId).map(_.deserialize)

    def get(
        orgId: String
    ): Future[Option[OrgWallexBeneficiaryDetail]] =
      getByOrgId(orgId).map(_.map(_.deserialize))

    def getByBeneficiaryId(
        beneficiaryId: String
    ): Future[OrgWallexBeneficiaryDetailSerialized] =
      tryGetByBeneficiaryId(beneficiaryId)

    def getByTraderId(
        traderId: String
    ): Future[OrgWallexBeneficiaryDetailSerialized] =
      tryGetByTraderId(traderId)

    def delete(beneficiaryId: String): Future[Int] =
      deleteById(beneficiaryId)
  }

}
