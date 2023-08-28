package models.blockchain

import com.cosmos.authz.{v1beta1 => authzTx}
import com.google.protobuf.{Any => protoAny}
import exceptions.BaseException
import models.Abstract.{Authorization => AbstractAuthorization}
import models.traits.Logging
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import queries.blockchain.GetAuthorizations
import queries.responses.common.Header
import slick.jdbc.JdbcProfile
import utilities.Date.RFC3339

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.util.{Failure, Success}

case class Authorization(granter: String, grantee: String, msgTypeURL: String, grantedAuthorization: Array[Byte], expiration: Long, createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging {

  def getAuthorization: AbstractAuthorization = AbstractAuthorization(protoAny.parseFrom(this.grantedAuthorization))

}

@Singleton
class Authorizations @Inject()(
                                getAuthorizations: GetAuthorizations,
                                protected val databaseConfigProvider: DatabaseConfigProvider,
                                utilitiesOperations: utilities.Operations
                              )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_BALANCE

  import databaseConfig.profile.api._

  private[models] val authorizationTable = TableQuery[AuthorizationTable]

  private def add(authorization: Authorization): Future[String] = db.run((authorizationTable returning authorizationTable.map(_.granter) += authorization).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.AUTHORIZATION_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(authorization: Authorization): Future[Int] = db.run(authorizationTable.insertOrUpdate(authorization).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.AUTHORIZATION_UPSERT_FAILED, psqlException)
    }
  }

  private def upsertMultiple(authorizations: Seq[Authorization]): Future[Unit] = db.run(DBIO.sequence(authorizations.map(x => authorizationTable.insertOrUpdate(x))).asTry).map {
    case Success(result) => ()
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.AUTHORIZATION_UPSERT_FAILED, psqlException)
    }
  }

  private def deleteByGranterGranteeAndMsgType(granter: String, grantee: String, msgTypeURL: String): Future[Int] = db.run(authorizationTable.filter(x => x.granter === granter && x.grantee === grantee && x.msgTypeURL === msgTypeURL).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.AUTHORIZATION_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.AUTHORIZATION_DELETE_FAILED, noSuchElementException)
    }
  }

  private def deleteByGranterAndGrantee(granter: String, grantee: String): Future[Int] = db.run(authorizationTable.filter(x => x.granter === granter && x.grantee === grantee).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.AUTHORIZATION_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.AUTHORIZATION_DELETE_FAILED, noSuchElementException)
    }
  }

  private def findByGranterAndGrantee(granter: String, grantee: String): Future[Seq[Authorization]] = db.run(authorizationTable.filter(x => x.granter === granter && x.grantee === grantee).result)

  private def findByGranterGranteeAndMsgType(granter: String, grantee: String, msgTypeURL: String): Future[Authorization] = db.run(authorizationTable.filter(x => x.granter === granter && x.grantee === grantee && x.msgTypeURL === msgTypeURL).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.AUTHORIZATION_NOT_FOUND, noSuchElementException)
    }
  }

  private def findByGranterGranteeAndMsgTypeOptional(granter: String, grantee: String, msgTypeURL: String): Future[Option[Authorization]] = db.run(authorizationTable.filter(x => x.granter === granter && x.grantee === grantee && x.msgTypeURL === msgTypeURL).result.headOption)

  private def getByGranter(granter: String): Future[Seq[Authorization]] = db.run(authorizationTable.filter(_.granter === granter).result)

  private def getByGrantee(grantee: String): Future[Seq[Authorization]] = db.run(authorizationTable.filter(_.grantee === grantee).result)

  private[models] class AuthorizationTable(tag: Tag) extends Table[Authorization](tag, "Authorization") {

    def * = (granter, grantee, msgTypeURL, grantedAuthorization, expiration, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (Authorization.tupled, Authorization.unapply)

    def granter = column[String]("granter", O.PrimaryKey)

    def grantee = column[String]("grantee", O.PrimaryKey)

    def msgTypeURL = column[String]("msgTypeURL", O.PrimaryKey)

    def grantedAuthorization = column[Array[Byte]]("grantedAuthorization")

    def expiration = column[Long]("expiration")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")
  }

  object Service {

    def create(granter: String, grantee: String, msgTypeURL: String, grantedAuthorization: protoAny, expiration: RFC3339): Future[String] = add(Authorization(granter = granter, grantee = grantee, msgTypeURL = msgTypeURL, grantedAuthorization = grantedAuthorization.toByteString.toByteArray, expiration = expiration.epoch))

    def tryGet(granter: String, grantee: String, msgTypeURL: String): Future[Authorization] = findByGranterGranteeAndMsgType(granter = granter, grantee = grantee, msgTypeURL = msgTypeURL)

    def get(granter: String, grantee: String, msgTypeURL: String): Future[Option[Authorization]] = findByGranterGranteeAndMsgTypeOptional(granter = granter, grantee = grantee, msgTypeURL = msgTypeURL)

    def insertOrUpdate(authorization: Authorization): Future[Int] = upsert(authorization)

    def insertOrUpdate(authorizations: Seq[Authorization]): Future[Unit] = upsertMultiple(authorizations)

    def getListByGranter(address: String): Future[Seq[Authorization]] = getByGranter(address)

    def getListByGrantee(address: String): Future[Seq[Authorization]] = getByGrantee(address)

    def get(granter: String, grantee: String) = findByGranterAndGrantee(granter = granter, grantee = grantee)

    def delete(granter: String, grantee: String, msgTypeURL: String): Future[Int] = deleteByGranterGranteeAndMsgType(granter = granter, grantee = grantee, msgTypeURL = msgTypeURL)

    def delete(granter: String, grantee: String): Future[Int] = deleteByGranterAndGrantee(granter = granter, grantee = grantee)
  }

  object Utility {

    def onGrantAuthorization(grantAuthorization: authzTx.MsgGrant)(implicit header: Header): Future[String] = {
      val insertOrUpdate = Service.insertOrUpdate(Authorization(granter = grantAuthorization.getGranter, grantee = grantAuthorization.getGrantee, msgTypeURL = AbstractAuthorization(grantAuthorization.getGrant.getAuthorization).getMsgTypeURL, grantedAuthorization = grantAuthorization.getGrant.getAuthorization.toByteString.toByteArray, expiration = grantAuthorization.getGrant.getExpiration.getSeconds))
      (for {
        _ <- insertOrUpdate
      } yield grantAuthorization.getGranter).recover {
        case _: BaseException => logger.error(schema.constants.Messages.GRANT_AUTHORIZATION + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          grantAuthorization.getGranter
      }
    }

    def onRevokeAuthorization(revokeAuthorization: authzTx.MsgRevoke)(implicit header: Header): Future[String] = {
      val delete = Service.delete(granter = revokeAuthorization.getGranter, grantee = revokeAuthorization.getGrantee, msgTypeURL = revokeAuthorization.getMsgTypeUrl)
      (for {
        _ <- delete
      } yield revokeAuthorization.getGranter).recover {
        case _: BaseException => logger.error(schema.constants.Messages.REVOKE_AUTHORIZATION + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          revokeAuthorization.getGranter
      }
    }

    def onExecuteAuthorization(executeAuthorization: authzTx.MsgExec, granter: String)(implicit header: Header): Future[String] = {
      val execute = if (granter != executeAuthorization.getGrantee) {
        utilitiesOperations.traverse(executeAuthorization.getMsgsList.asScala.toSeq) { msg =>

          val authorizations = getAuthorizations.Service.get(granter = granter, grantee = executeAuthorization.getGrantee)
          val delete = Service.delete(granter = granter, grantee = executeAuthorization.getGrantee)

          //          def updateOrDelete(optionalAuthorization: Option[Authorization]) = optionalAuthorization.fold {

          //            val response = authorization.getAuthorization.validate(msg)
          //            val deleteOrUpdate = if (response.delete) Service.delete(granter = granter, grantee = executeAuthorization.getGrantee, msgTypeURL = msg.getTypeUrl)
          //            else if (response.updated.nonEmpty) Service.insertOrUpdate(authorization.copy(grantedAuthorization = response.updated.fold(throw new BaseException(constants.Response.GRANT_AUTHORIZATION_NOT_FOUND))(x => x.toProto.toByteString.toByteArray)))
          //            else Future(0)
          //
          //            for {
          //              _ <- deleteOrUpdate
          //            } yield ()
          //          }

          def upsert(authorizationResponse: queries.responses.blockchain.AuthorizationsResponse.Response) = {
            if (authorizationResponse.grants.nonEmpty)
              Service.insertOrUpdate(authorizationResponse.grants.map(x => Authorization(granter = granter, grantee = executeAuthorization.getGrantee, msgTypeURL = x.authorization.value.toSerializable.getMsgTypeURL, grantedAuthorization = x.authorization.toSerializable.toAnyProto.toByteString.toByteArray, expiration = x.expiration.epoch)))
            else Future()
          }

          for {
            authorizations <- authorizations
            _ <- delete
            _ <- upsert(authorizations)
          } yield ()
        }
      } else Future(Seq())

      (for {
        _ <- execute
      } yield executeAuthorization.getGrantee).recover {
        case _: BaseException => logger.error(schema.constants.Messages.EXECUTE_AUTHORIZATION + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          executeAuthorization.getGrantee
      }
    }

  }

}