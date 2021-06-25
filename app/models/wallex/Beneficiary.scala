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

case class Beneficiary(
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
class Beneficiaries @Inject()(
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.WALLEX_BENEFICIARY

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val beneficiaryTable =
    TableQuery[BeneficiaryTable]

  private def serialize(
      beneficiary: Beneficiary
  ): BeneficiarySerialized =
    BeneficiarySerialized(
      wallexID = beneficiary.wallexID,
      beneficiaryID = beneficiary.beneficiaryID,
      address = beneficiary.address,
      country = beneficiary.country,
      city = beneficiary.city,
      entityType = beneficiary.entityType,
      companyName = beneficiary.companyName,
      nickname = beneficiary.nickname,
      accountType = beneficiary.accountType,
      bankAccount = Json.toJson(beneficiary.bankAccount).toString,
      createdBy = beneficiary.createdBy,
      createdOn = beneficiary.createdOn,
      createdOnTimeZone = beneficiary.createdOnTimeZone,
      updatedBy = beneficiary.updatedBy,
      updatedOn = beneficiary.updatedOn,
      updatedOnTimeZone = beneficiary.updatedOnTimeZone
    )

  private def add(
     beneficiarySerialized: BeneficiarySerialized
  ): Future[String] =
    db.run(
        (beneficiaryTable returning beneficiaryTable
          .map(_.wallexID) += beneficiarySerialized).asTry
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
    beneficiarySerialized: BeneficiarySerialized
  ): Future[Int] =
    db.run(
        beneficiaryTable
          .insertOrUpdate(beneficiarySerialized)
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

  private def getByWallexID(
      wallexID: String
  ): Future[Seq[BeneficiarySerialized]] =
    db.run(
      beneficiaryTable
        .filter(_.wallexID === wallexID)
        .result
    )

  private def tryGetByWallexID(
       wallexID: String
  ): Future[BeneficiarySerialized] =
    db.run(
        beneficiaryTable
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

  private def tryGetByBeneficiaryId(
      beneficiaryID: String
  ): Future[BeneficiarySerialized] =
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

  case class BeneficiarySerialized(
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

    def deserialize: Beneficiary =
      Beneficiary(
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

  private[models] class BeneficiaryTable(tag: Tag)
      extends Table[BeneficiarySerialized](
        tag,
        "Beneficiary"
      ) {

    override def * =
      (
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
      ) <> (BeneficiarySerialized.tupled, BeneficiarySerialized.unapply)

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
          Beneficiary(
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
          Beneficiary(
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

    def tryGet(wallexID: String): Future[Beneficiary] =
      tryGetByWallexID(wallexID).map(_.deserialize)

    def get(wallexID: String
    ): Future[Seq[Beneficiary]] =
      getByWallexID(wallexID).map(_.map(_.deserialize))

    def getByBeneficiaryId(
        beneficiaryID: String
    ): Future[Beneficiary] =
      tryGetByBeneficiaryId(beneficiaryID).map(_.deserialize)

    def delete(beneficiaryID: String): Future[Int] =
      deleteById(beneficiaryID)
  }

}
