package models.master

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class OrganizationBankAccountDetail(id: String, accountHolder: String, nickName: String, accountNumber: String, bankName: String, swiftAddress: String, address: String, country: String, zipCode: String, status: Option[Boolean] = None)

@Singleton
class OrganizationBankAccountDetails @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ORGANIZATION_BANK_ACCOUNT_DETAIL

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val organizationBankAccountDetailTable = TableQuery[OrganizationBankAccountDetailTable]

  private def add(organizationBankAccountDetail: OrganizationBankAccountDetail): Future[String] = db.run((organizationBankAccountDetailTable returning organizationBankAccountDetailTable.map(_.id) += organizationBankAccountDetail).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(organizationBankAccountDetail: OrganizationBankAccountDetail): Future[Int] = db.run(organizationBankAccountDetailTable.insertOrUpdate(organizationBankAccountDetail).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[Option[OrganizationBankAccountDetail]] = db.run(organizationBankAccountDetailTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => Option(result)
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        None
    }
  }

  private def deleteById(id: String) = db.run(organizationBankAccountDetailTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class OrganizationBankAccountDetailTable(tag: Tag) extends Table[OrganizationBankAccountDetail](tag, "OrganizationBankAccountDetail") {

    def * = (id, accountHolder, nickName, accountNumber, bankName, swiftAddress, address, country, zipCode, status.?) <> (OrganizationBankAccountDetail.tupled, OrganizationBankAccountDetail.unapply)

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

  }

  object Service {
    def create(id: String, accountHolder: String, nickName: String, accountNumber: String, bankName: String, swiftAddress: String, country: String, address: String, zipCode: String): String = Await.result(add(OrganizationBankAccountDetail(id = id, accountHolder = accountHolder, nickName = nickName, accountNumber = accountNumber, bankName = bankName, swiftAddress = swiftAddress, country = country, address = address, zipCode = zipCode)), Duration.Inf)

    def insertOrUpdate(id: String, accountHolder: String, nickName: String, accountNumber: String, bankName: String, swiftAddress: String, country: String, address: String, zipCode: String): Future[Int] = upsert(OrganizationBankAccountDetail(id = id, accountHolder = accountHolder, nickName = nickName, accountNumber = accountNumber, bankName = bankName, swiftAddress = swiftAddress, country = country, address = address, zipCode = zipCode))

    def get(id: String): Future[OrganizationBankAccountDetail] = findById(id).map{detail=> detail.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION))}
  }

}