package models.blockchain

import exceptions.BaseException
import models.Trait.Logged
import models.common.Authz
import models.common.TransactionMessages._
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import queries.responses.common.Header
import slick.jdbc.JdbcProfile
import utilities.Date.RFC3339

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Authorization(granter: String, grantee: String, msgTypeURL: String, grantedAuthorization: Authz.Authorization, expiration: RFC3339, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Authorizations @Inject()(
                                protected val databaseConfigProvider: DatabaseConfigProvider,
                                utilitiesOperations: utilities.Operations
                              )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_BALANCE

  import databaseConfig.profile.api._

  private[models] val authorizationTable = TableQuery[AuthorizationTable]

  case class AuthorizationSerialized(granter: String, grantee: String, msgTypeURL: String, grantedAuthorization: String, expiration: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Authorization = Authorization(granter = granter, grantee = grantee, msgTypeURL = msgTypeURL, grantedAuthorization = utilities.JSON.convertJsonStringToObject[Authz.Authorization](grantedAuthorization), expiration = RFC3339(expiration), createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(authorization: Authorization): AuthorizationSerialized = AuthorizationSerialized(granter = authorization.granter, grantee = authorization.grantee, msgTypeURL = authorization.msgTypeURL, grantedAuthorization = Json.toJson(authorization.grantedAuthorization).toString, expiration = authorization.expiration.timestamp, createdBy = authorization.createdBy, createdOn = authorization.createdOn, createdOnTimeZone = authorization.createdOnTimeZone, updatedBy = authorization.updatedBy, updatedOn = authorization.updatedOn, updatedOnTimeZone = authorization.updatedOnTimeZone)

  private def add(authorization: Authorization): Future[String] = db.run((authorizationTable returning authorizationTable.map(_.granter) += serialize(authorization)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.AUTHORIZATION_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(authorization: Authorization): Future[Int] = db.run(authorizationTable.insertOrUpdate(serialize(authorization)).asTry).map {
    case Success(result) => result
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

  private def findByGranterGranteeAndMsgType(granter: String, grantee: String, msgTypeURL: String): Future[AuthorizationSerialized] = db.run(authorizationTable.filter(x => x.granter === granter && x.grantee === grantee && x.msgTypeURL === msgTypeURL).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.AUTHORIZATION_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.AUTHORIZATION_DELETE_FAILED, noSuchElementException)
    }
  }

  private def getByGranter(granter: String): Future[Seq[AuthorizationSerialized]] = db.run(authorizationTable.filter(_.granter === granter).result)

  private def getByGrantee(grantee: String): Future[Seq[AuthorizationSerialized]] = db.run(authorizationTable.filter(_.grantee === grantee).result)

  private[models] class AuthorizationTable(tag: Tag) extends Table[AuthorizationSerialized](tag, "Authorization_BC") {

    def * = (granter, grantee, msgTypeURL, grantedAuthorization, expiration, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (AuthorizationSerialized.tupled, AuthorizationSerialized.unapply)

    def granter = column[String]("granter", O.PrimaryKey)

    def grantee = column[String]("grantee", O.PrimaryKey)

    def msgTypeURL = column[String]("msgTypeURL", O.PrimaryKey)

    def grantedAuthorization = column[String]("grantedAuthorization")

    def expiration = column[String]("expiration")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(granter: String, grantee: String, msgTypeURL: String, grantedAuthorization: Authz.Authorization, expiration: RFC3339): Future[String] = add(Authorization(granter = granter, grantee = grantee, msgTypeURL = msgTypeURL, grantedAuthorization = grantedAuthorization, expiration = expiration))

    def tryGet(granter: String, grantee: String, msgTypeURL: String): Future[Authorization] = findByGranterGranteeAndMsgType(granter = granter, grantee = grantee, msgTypeURL = msgTypeURL).map(_.deserialize)

    def insertOrUpdate(authorization: Authorization): Future[Int] = upsert(authorization)

    def getListByGranter(address: String): Future[Seq[Authorization]] = getByGranter(address).map(_.map(_.deserialize))

    def getListByGrantee(address: String): Future[Seq[Authorization]] = getByGrantee(address).map(_.map(_.deserialize))

    def delete(granter: String, grantee: String, msgTypeURL: String): Future[Int] = deleteByGranterGranteeAndMsgType(granter = granter, grantee = grantee, msgTypeURL = msgTypeURL)
  }

  object Utility {

    def onGrantAuthorization(grantAuthorization: GrantAuthorization)(implicit header: Header): Future[Unit] = {
      val insertOrUpdate = Service.insertOrUpdate(Authorization(granter = grantAuthorization.granter, grantee = grantAuthorization.grantee, msgTypeURL = grantAuthorization.grant.authorization.value.getMsgTypeURL, grantedAuthorization = grantAuthorization.grant.authorization, expiration = grantAuthorization.grant.expiration))
      (for {
        _ <- insertOrUpdate
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.GRANT_AUTHORIZATION + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def onRevokeAuthorization(revokeAuthorization: RevokeAuthorization)(implicit header: Header): Future[Unit] = {
      val delete = Service.delete(granter = revokeAuthorization.granter, grantee = revokeAuthorization.grantee, msgTypeURL = revokeAuthorization.messageTypeURL)
      (for {
        _ <- delete
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.REVOKE_AUTHORIZATION + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def onExecuteAuthorization(executeAuthorization: ExecuteAuthorization)(implicit header: Header): Future[Seq[StdMsg]] = {
      val execute = utilitiesOperations.traverse(executeAuthorization.messages) { msg =>
        val granter = msg.message.getSigners.head
        if (granter != executeAuthorization.grantee) {
          val authorization = Service.tryGet(granter = granter, grantee = executeAuthorization.grantee, msgTypeURL = msg.messageType)

          def updateOrDelete(authorization: Authorization) = {
            val response = authorization.grantedAuthorization.value.validate(msg)
            val deleteOrUpdate = if (response.delete) Service.delete(granter = granter, grantee = executeAuthorization.grantee, msgTypeURL = msg.messageType)
            else if (response.updated.nonEmpty) Service.insertOrUpdate(authorization.copy(grantedAuthorization = response.updated.fold(throw new BaseException(constants.Response.GRANT_AUTHORIZATION_NOT_FOUND))(x => Authz.Authorization(x.getAuthorizationType, value = x))))
            else Future(0)

            for {
              _ <- deleteOrUpdate
            } yield ()
          }

          for {
            authorization <- authorization
            _ <- updateOrDelete(authorization)
          } yield msg
        } else Future(msg)
      }

      (for {
        msgs <- execute
      } yield msgs).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.EXECUTE_AUTHORIZATION + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          Seq.empty
      }
    }

  }

}