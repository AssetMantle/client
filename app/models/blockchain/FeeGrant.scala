package models.blockchain

import exceptions.BaseException
import models.Trait.Logged
import models.common.TransactionMessages._
import models.common.{FeeGrant => commonFeeGrant}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import queries.responses.common.Header
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class FeeGrant(granter: String, grantee: String, allowance: commonFeeGrant.Allowance, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class FeeGrants @Inject()(
                           protected val databaseConfigProvider: DatabaseConfigProvider,
                           utilitiesOperations: utilities.Operations
                         )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_BALANCE

  import databaseConfig.profile.api._

  private[models] val feeGrantTable = TableQuery[FeeGrantTable]

  case class FeeGrantSerialized(granter: String, grantee: String, allowance: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: FeeGrant = FeeGrant(granter = granter, grantee = grantee, allowance = utilities.JSON.convertJsonStringToObject[commonFeeGrant.Allowance](allowance), createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(feeGrant: FeeGrant): FeeGrantSerialized = FeeGrantSerialized(granter = feeGrant.granter, grantee = feeGrant.grantee, allowance = Json.toJson(feeGrant.allowance).toString, createdBy = feeGrant.createdBy, createdOn = feeGrant.createdOn, createdOnTimeZone = feeGrant.createdOnTimeZone, updatedBy = feeGrant.updatedBy, updatedOn = feeGrant.updatedOn, updatedOnTimeZone = feeGrant.updatedOnTimeZone)

  private def add(feeGrant: FeeGrant): Future[String] = db.run((feeGrantTable returning feeGrantTable.map(_.granter) += serialize(feeGrant)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.AUTHORIZATION_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(feeGrant: FeeGrant): Future[Int] = db.run(feeGrantTable.insertOrUpdate(serialize(feeGrant)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.AUTHORIZATION_UPSERT_FAILED, psqlException)
    }
  }

  private def deleteByGranterGranteeAndMsgType(granter: String, grantee: String): Future[Int] = db.run(feeGrantTable.filter(x => x.granter === granter && x.grantee === grantee).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.AUTHORIZATION_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.AUTHORIZATION_DELETE_FAILED, noSuchElementException)
    }
  }

  private def findByGranterGranteeAndMsgType(granter: String, grantee: String): Future[FeeGrantSerialized] = db.run(feeGrantTable.filter(x => x.granter === granter && x.grantee === grantee).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.AUTHORIZATION_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.AUTHORIZATION_DELETE_FAILED, noSuchElementException)
    }
  }

  private def getByGranter(granter: String): Future[Seq[FeeGrantSerialized]] = db.run(feeGrantTable.filter(_.granter === granter).result)

  private def getByGrantee(grantee: String): Future[Seq[FeeGrantSerialized]] = db.run(feeGrantTable.filter(_.grantee === grantee).result)

  private[models] class FeeGrantTable(tag: Tag) extends Table[FeeGrantSerialized](tag, "FeeGrant_BC") {

    def * = (granter, grantee, allowance, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (FeeGrantSerialized.tupled, FeeGrantSerialized.unapply)

    def granter = column[String]("granter", O.PrimaryKey)

    def grantee = column[String]("grantee", O.PrimaryKey)

    def allowance = column[String]("allowance")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(granter: String, grantee: String, allowance: commonFeeGrant.Allowance): Future[String] = add(FeeGrant(granter = granter, grantee = grantee, allowance = allowance))

    def tryGet(granter: String, grantee: String): Future[FeeGrant] = findByGranterGranteeAndMsgType(granter = granter, grantee = grantee).map(_.deserialize)

    def insertOrUpdate(feeGrant: FeeGrant): Future[Int] = upsert(feeGrant)

    def getListByGranter(address: String): Future[Seq[FeeGrant]] = getByGranter(address).map(_.map(_.deserialize))

    def getListByGrantee(address: String): Future[Seq[FeeGrant]] = getByGrantee(address).map(_.map(_.deserialize))

    def delete(granter: String, grantee: String): Future[Int] = deleteByGranterGranteeAndMsgType(granter = granter, grantee = grantee)
  }

  object Utility {

    def onFeeGrantAllowance(feeGrantAllowance: FeeGrantAllowance)(implicit header: Header): Future[Unit] = {
      val upsert = Service.insertOrUpdate(FeeGrant(granter = feeGrantAllowance.granter, grantee = feeGrantAllowance.grantee, allowance = feeGrantAllowance.allowance))
      (for {
        _ <- upsert
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.FEE_GRANT_ALLOWANCE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def onFeeRevokeAllowance(feeRevokeAllowance: FeeRevokeAllowance)(implicit header: Header): Future[Unit] = {
      val delete = Service.delete(granter = feeRevokeAllowance.granter, grantee = feeRevokeAllowance.grantee)

      (for {
        _ <- delete
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.FEE_REVOKE_ALLOWANCE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }
  }

}