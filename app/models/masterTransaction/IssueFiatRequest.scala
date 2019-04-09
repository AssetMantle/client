package models.masterTransaction

import exceptions.BaseException
import javax.inject.Inject
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Random, Success}

case class IssueFiatRequest(id: String, accountID: String, transactionID: String, transactionAmount: Int, gas: Option[Int], status: Option[Boolean])

class IssueFiatRequests  @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_ISSUE_FIAT_REQUESTS

  import databaseConfig.profile.api._

  private[models] val issueFiatRequestTable = TableQuery[IssueFiatRequestTable]

  private def add(issueFiatRequest: IssueFiatRequest)(implicit executionContext: ExecutionContext): Future[String] = db.run((issueFiatRequestTable returning issueFiatRequestTable.map(_.id) += issueFiatRequest).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def findByAccountID(accountID: String)(implicit executionContext: ExecutionContext): Future[IssueFiatRequest] = db.run(issueFiatRequestTable.filter(_.accountID === accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndGasByAccountID(accountID: String, status: Boolean, gas: Int)(implicit executionContext: ExecutionContext) = db.run(issueFiatRequestTable.filter(_.accountID === accountID).map(faucet => (faucet.status, faucet.gas)).update((status, gas)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAllWithNullStatus()(implicit executionContext: ExecutionContext): Future[Seq[IssueFiatRequest]] = db.run(issueFiatRequestTable.filter(_.status.?.isEmpty).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByID(id: String)(implicit executionContext: ExecutionContext) = db.run(issueFiatRequestTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class IssueFiatRequestTable(tag: Tag) extends Table[IssueFiatRequest](tag, "IssueFiatRequest") {

    def * = (id, accountID, transactionID, transactionAmount, gas.?, status.?) <> (IssueFiatRequest.tupled, IssueFiatRequest.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def accountID = column[String]("accountID")

    def transactionID = column[String]("transactionID")

    def transactionAmount = column[Int]("transactionAmount")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

  }

  object Service {

    def addIssueFiatRequest(accountID: String, transactionID: String, transactionAmount: Int)(implicit executionContext: ExecutionContext): String = Await.result(add(IssueFiatRequest(id = (Random.nextInt(899999999) + 100000000).toString, accountID = accountID, transactionID = transactionID, transactionAmount = transactionAmount, null, null)), Duration.Inf)

    def getIssueFiatRequest(accountID: String)(implicit executionContext: ExecutionContext): IssueFiatRequest = Await.result(findByAccountID(accountID), Duration.Inf)

    def updateStatusAndGas(accountID: String, status: Boolean, gas: Int)(implicit executionContext: ExecutionContext): Int = Await.result(updateStatusAndGasByAccountID(accountID, status, gas), Duration.Inf)

    def getStatus()(implicit executionContext: ExecutionContext): Seq[IssueFiatRequest] = Await.result(getAllWithNullStatus(), Duration.Inf)

    def deleteIssueFiatRequest(id: String)(implicit executionContext: ExecutionContext): Int = Await.result(deleteByID(id), Duration.Inf)
  }

}
