package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class IssueAssetRequest(id: String, ticketID: Option[String], accountID: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, gas: Option[Int], status: Option[Boolean], comment: Option[String])

@Singleton
class IssueAssetRequests @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_ISSUE_ASSET_REQUESTS

  import databaseConfig.profile.api._

  private[models] val issueAssetRequestTable = TableQuery[IssueAssetRequestTable]

  private def add(issueAssetRequest: IssueAssetRequest)(implicit executionContext: ExecutionContext): Future[String] = db.run((issueAssetRequestTable returning issueAssetRequestTable.map(_.id) += issueAssetRequest).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByID(id: String)(implicit executionContext: ExecutionContext): Future[IssueAssetRequest] = db.run(issueAssetRequestTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTicketIDStatusAndGasByID(id: String, ticketID: String, status: Boolean, gas: Int)(implicit executionContext: ExecutionContext) = db.run(issueAssetRequestTable.filter(_.id === id).map(faucet => (faucet.ticketID, faucet.status, faucet.gas)).update((ticketID, status, gas)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateCommentByID(id: String, comment: String)(implicit executionContext: ExecutionContext) = db.run(issueAssetRequestTable.filter(_.id === id).map(_.comment).update(comment).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndCommentByID(id: String, status: Option[Boolean], comment: String)(implicit executionContext: ExecutionContext) = db.run(issueAssetRequestTable.filter(_.id === id).map(issueAssetRequest => (issueAssetRequest.status.?, issueAssetRequest.comment)).update((status, comment)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getIssueAssetRequestsWithNullStatus(accountIDs: Seq[String])(implicit executionContext: ExecutionContext): Future[Seq[IssueAssetRequest]] = db.run(issueAssetRequestTable.filter(_.accountID.inSet(accountIDs)).filter(_.status.?.isEmpty).result)

  private def deleteByID(id: String)(implicit executionContext: ExecutionContext) = db.run(issueAssetRequestTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getStatusByID(id: String)(implicit executionContext: ExecutionContext): Future[Option[Boolean]] = db.run(issueAssetRequestTable.filter(_.id === id).map(_.status.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class IssueAssetRequestTable(tag: Tag) extends Table[IssueAssetRequest](tag, "IssueAssetRequest") {

    def * = (id, ticketID.?, accountID, documentHash, assetType, assetPrice, quantityUnit, assetQuantity, gas.?, status.?, comment.?) <> (IssueAssetRequest.tupled, IssueAssetRequest.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def ticketID = column[String]("ticketID")

    def accountID = column[String]("accountID")

    def documentHash = column[String]("documentHash")

    def assetType = column[String]("assetType")

    def assetPrice = column[Int]("assetPrice")

    def quantityUnit = column[String]("quantityUnit")

    def assetQuantity = column[Int]("assetQuantity")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def comment = column[String]("comment")

  }

  object Service {

    def create(accountID: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int)(implicit executionContext: ExecutionContext): String = Await.result(add(IssueAssetRequest(id = Random.nextString(32), null, accountID = accountID, documentHash = documentHash, assetType = assetType, assetPrice = assetPrice, quantityUnit = quantityUnit, assetQuantity = assetQuantity, null, null, null)), Duration.Inf)

    def accept(id: String, ticketID: String,  gas: Int)(implicit executionContext: ExecutionContext): Int = Await.result(updateTicketIDStatusAndGasByID(id, ticketID, status = true, gas), Duration.Inf)

    def reject(id: String, comment: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateStatusAndCommentByID(id = id, status = Option(false), comment = comment), Duration.Inf)

    def updateComment(id: String, comment: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateCommentByID(id, comment), Duration.Inf)

    def getPendingIssueAssetRequests(accountIDs: Seq[String])(implicit executionContext: ExecutionContext): Seq[IssueAssetRequest] = Await.result(getIssueAssetRequestsWithNullStatus(accountIDs), Duration.Inf)

    def delete(id: String)(implicit executionContext: ExecutionContext): Int = Await.result(deleteByID(id), Duration.Inf)

    def getStatus(id: String)(implicit executionContext: ExecutionContext): Option[Boolean] = Await.result(getStatusByID(id), Duration.Inf)

  }

}
