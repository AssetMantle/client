package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class OrganizationBankAccountDetail(id: String, accountHolder: String, nickName: String, accountNumber: String, bankName: String, swiftAddress: String, address: String, country: String, zipCode: String, status: Option[Boolean] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class OrganizationBankAccountDetails @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ORGANIZATION_BANK_ACCOUNT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val organizationBankAccountDetailTable = TableQuery[OrganizationBankAccountDetailTable]

  private def add(organizationBankAccountDetail: OrganizationBankAccountDetail): Future[String] = db.run((organizationBankAccountDetailTable returning organizationBankAccountDetailTable.map(_.id) += organizationBankAccountDetail).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(organizationBankAccountDetail: OrganizationBankAccountDetail): Future[Int] = db.run(organizationBankAccountDetailTable.insertOrUpdate(organizationBankAccountDetail).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findById(id: String): Future[Option[OrganizationBankAccountDetail]] = db.run(organizationBankAccountDetailTable.filter(_.id === id).result.headOption)

  private def deleteById(id: String) = db.run(organizationBankAccountDetailTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class OrganizationBankAccountDetailTable(tag: Tag) extends Table[OrganizationBankAccountDetail](tag, "OrganizationBankAccountDetail") {

    def * = (id, accountHolder, nickName, accountNumber, bankName, swiftAddress, address, country, zipCode, status.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (OrganizationBankAccountDetail.tupled, OrganizationBankAccountDetail.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def accountHolder = column[String]("accountHolder")

    def nickName = column[String]("nickName")

    def accountNumber = column[String]("accountNumber")

    def bankName = column[String]("bankName")

    def swiftAddress = column[String]("swiftAddress")

    def address = column[String]("address")

    def country = column[String]("country")

    def zipCode = column[String]("zipCode")

    def status = column[Boolean]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {
    def create(id: String, accountHolder: String, nickName: String, accountNumber: String, bankName: String, swiftAddress: String, country: String, address: String, zipCode: String): Future[String] = add(OrganizationBankAccountDetail(id = id, accountHolder = accountHolder, nickName = nickName, accountNumber = accountNumber, bankName = bankName, swiftAddress = swiftAddress, country = country, address = address, zipCode = zipCode))

    def insertOrUpdate(id: String, accountHolder: String, nickName: String, accountNumber: String, bankName: String, swiftAddress: String, country: String, address: String, zipCode: String): Future[Int] = upsert(OrganizationBankAccountDetail(id = id, accountHolder = accountHolder, nickName = nickName, accountNumber = accountNumber, bankName = bankName, swiftAddress = swiftAddress, country = country, address = address, zipCode = zipCode))

    def tryGet(id: String): Future[OrganizationBankAccountDetail] = findById(id).map { detail => detail.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)) }

    def get(id: String): Future[Option[OrganizationBankAccountDetail]] = findById(id)
  }

}