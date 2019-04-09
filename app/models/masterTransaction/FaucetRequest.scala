package models.masterTransaction

import exceptions.BaseException
import javax.inject.Inject
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class FaucetRequest(id: String, accountID: String, amount: Int, gas: Option[Int], status: Option[Boolean] )

class FaucetRequests @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_FAUCET_REQUESTS

  import databaseConfig.profile.api._

  private[models] val faucetRequestTable = TableQuery[FaucetRequestTable]

  private def add(faucetRequest: FaucetRequest)(implicit executionContext: ExecutionContext): Future[String] = db.run((faucetRequestTable returning faucetRequestTable.map(_.id) += faucetRequest).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def findByAccountID(accountID: String)(implicit executionContext: ExecutionContext): Future[FaucetRequest] = db.run(faucetRequestTable.filter(_.accountID === accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAllWithNullStatus()(implicit executionContext: ExecutionContext): Future[Seq[FaucetRequest]] = db.run(faucetRequestTable.filter(_.status.?.isEmpty).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndGasByAccountID(accountID: String, status: Boolean, gas: Int)(implicit executionContext: ExecutionContext) = db.run(faucetRequestTable.filter(_.accountID === accountID).map(faucet => (faucet.status, faucet.gas)).update((status, gas)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByID(id: String)(implicit executionContext: ExecutionContext) = db.run(faucetRequestTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class FaucetRequestTable(tag: Tag) extends Table[FaucetRequest](tag, "FaucetRequest") {

    def * = (id, accountID, amount, gas.?, status.?) <> (FaucetRequest.tupled, FaucetRequest.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def accountID = column[String]("accountID")

    def amount = column[Int]("amount")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

  }

  object Service {

    def addFaucetRequest(accountID: String, amount: Int)(implicit executionContext: ExecutionContext):String = Await.result(add(FaucetRequest(Random.nextString(32), accountID, amount, null, null)), Duration.Inf)

    def getFaucetRequest(accountID: String)(implicit executionContext: ExecutionContext):FaucetRequest = Await.result(findByAccountID(accountID), Duration.Inf)

    def updateStatusAndGas(accountID: String, status: Boolean, gas: Int)(implicit executionContext: ExecutionContext) = Await.result(updateStatusAndGasByAccountID(accountID, status, gas), Duration.Inf)

    def getStatus()(implicit executionContext: ExecutionContext): Seq[FaucetRequest] = Await.result(getAllWithNullStatus(), Duration.Inf)

    def deleteFaucetRequest(id: String)(implicit executionContext: ExecutionContext) = Await.result(deleteByID(id), Duration.Inf)
  }

}
