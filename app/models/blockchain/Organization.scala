package models.blockchain

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Organization(id: String, address: String)

@Singleton
class Organizations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ORGANIZATION

  import databaseConfig.profile.api._

  private[models] val organizationTable = TableQuery[OrganizationTable]

  private def add(organization: Organization)(implicit executionContext: ExecutionContext): Future[String] = db.run((organizationTable returning organizationTable.map(_.id) += organization).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String)(implicit executionContext: ExecutionContext): Future[Organization] = db.run(organizationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAddressById(id: String)(implicit executionContext: ExecutionContext): Future[String] = db.run(organizationTable.filter(_.id === id).map(_.address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getIdByAddress(address: String)(implicit executionContext: ExecutionContext): Future[String] = db.run(organizationTable.filter(_.address === address).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String)(implicit executionContext: ExecutionContext):Future[Int] = db.run(organizationTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class OrganizationTable(tag: Tag) extends Table[Organization](tag, "Organization_BC") {

    def * = (id, address) <> (Organization.tupled, Organization.unapply)

    def ? = (id.?, address.?).shaped.<>({ r => import r._; _1.map(_ => Organization.tupled((_1.get, _2.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    def id = column[String]("id", O.PrimaryKey)

    def address = column[String]("address")

  }

  object Service {

    def addOrganization(id: String, address: String)(implicit executionContext: ExecutionContext):String = Await.result(add(Organization(id = id, address = address)), Duration.Inf)

    def getAddress(id: String)(implicit executionContext: ExecutionContext):String = Await.result(getAddressById(id), Duration.Inf)

    def getID(address: String)(implicit executionContext: ExecutionContext):String = Await.result(getIdByAddress(address), Duration.Inf)

  }

}