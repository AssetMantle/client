package models.blockchain

import com.google.protobuf.{Any => protoAny}
import com.cosmos.feegrant.{v1beta1 => feegrantTx}
import exceptions.BaseException
import models.Abstract.{FeeAllowance => AbstractFeeAllowance}
import models.traits.Logging
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import queries.responses.common.Header
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class FeeGrant(granter: String, grantee: String, allowance: Array[Byte], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging {

  def getAllowance: AbstractFeeAllowance = AbstractFeeAllowance(protoAny.parseFrom(this.allowance))

}

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

  private def add(feeGrant: FeeGrant): Future[String] = db.run((feeGrantTable returning feeGrantTable.map(_.granter) += feeGrant).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.AUTHORIZATION_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(feeGrant: FeeGrant): Future[Int] = db.run(feeGrantTable.insertOrUpdate(feeGrant).asTry).map {
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

  private def findByGranterGranteeAndMsgType(granter: String, grantee: String): Future[FeeGrant] = db.run(feeGrantTable.filter(x => x.granter === granter && x.grantee === grantee).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.AUTHORIZATION_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.AUTHORIZATION_DELETE_FAILED, noSuchElementException)
    }
  }

  private def getByGranter(granter: String): Future[Seq[FeeGrant]] = db.run(feeGrantTable.filter(_.granter === granter).result)

  private def getByGrantee(grantee: String): Future[Seq[FeeGrant]] = db.run(feeGrantTable.filter(_.grantee === grantee).result)

  private[models] class FeeGrantTable(tag: Tag) extends Table[FeeGrant](tag, "FeeGrant") {

    def * = (granter, grantee, allowance, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (FeeGrant.tupled, FeeGrant.unapply)

    def granter = column[String]("granter", O.PrimaryKey)

    def grantee = column[String]("grantee", O.PrimaryKey)

    def allowance = column[Array[Byte]]("allowance")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")
  }

  object Service {

    def create(granter: String, grantee: String, allowance: AbstractFeeAllowance): Future[String] = add(FeeGrant(granter = granter, grantee = grantee, allowance = allowance.toProto.toByteString.toByteArray))

    def tryGet(granter: String, grantee: String): Future[FeeGrant] = findByGranterGranteeAndMsgType(granter = granter, grantee = grantee)

    def insertOrUpdate(feeGrant: FeeGrant): Future[Int] = upsert(feeGrant)

    def getListByGranter(address: String): Future[Seq[FeeGrant]] = getByGranter(address)

    def getListByGrantee(address: String): Future[Seq[FeeGrant]] = getByGrantee(address)

    def delete(granter: String, grantee: String): Future[Int] = deleteByGranterGranteeAndMsgType(granter = granter, grantee = grantee)
  }

  object Utility {

    def onFeeGrantAllowance(feeGrantAllowance: feegrantTx.MsgGrantAllowance)(implicit header: Header): Future[String] = {
      val upsert = Service.insertOrUpdate(FeeGrant(granter = feeGrantAllowance.getGranter, grantee = feeGrantAllowance.getGrantee, allowance = AbstractFeeAllowance(feeGrantAllowance.getAllowance).toProto.toByteString.toByteArray))
      (for {
        _ <- upsert
      } yield feeGrantAllowance.getGranter).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.FEE_GRANT_ALLOWANCE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          feeGrantAllowance.getGranter
      }
    }

    def onFeeRevokeAllowance(feeRevokeAllowance: feegrantTx.MsgRevokeAllowance)(implicit header: Header): Future[String] = {
      val delete = Service.delete(granter = feeRevokeAllowance.getGranter, grantee = feeRevokeAllowance.getGrantee)

      (for {
        _ <- delete
      } yield feeRevokeAllowance.getGranter).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.FEE_REVOKE_ALLOWANCE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          feeRevokeAllowance.getGranter
      }
    }
  }

}