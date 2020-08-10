package models.docusign

import java.net.ConnectException
import java.sql.Timestamp

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.responses.DocusignRegenerateTokenResponse.Response
import slick.jdbc.JdbcProfile
import transactions.DocusignRegenerateToken
import utilities.KeyStore

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class OAuthToken(id: String, accessToken: String, expiresAt: Long, refreshToken: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class OAuthTokens @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, actorSystem: ActorSystem, docusignRegenerateToken: DocusignRegenerateToken, utilitiesNotification: utilities.Notification, keyStore: KeyStore)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.DOCUSIGN_OAUTH_TOKEN

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  private val accountID = keyStore.getPassphrase(constants.KeyStore.DOCUSIGN_ACCOUNT_ID)

  private val docusignOAuthTokenInitialDelay = configuration.get[Int]("docusign.scheduler.initialDelay").seconds
  private val docusignOAuthTokenIntervalTime = configuration.get[Int]("docusign.scheduler.intervalTime").minutes

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val oauthTokenTable = TableQuery[OAuthTokenTable]

  private def add(token: OAuthToken): Future[String] = db.run((oauthTokenTable returning oauthTokenTable.map(_.id) += token).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }


  private def updateByID(id: String, accessToken: String, expiresAt: Long, refreshToken: String): Future[Int] = db.run(oauthTokenTable.filter(_.id === id).map(x => (x.accessToken, x.expiresAt, x.refreshToken)).update((accessToken, expiresAt, refreshToken)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def tryGetByID(id: String): Future[OAuthToken] = db.run(oauthTokenTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getByID(id: String): Future[Option[OAuthToken]] = db.run(oauthTokenTable.filter(_.id === id).result.headOption)

  private def deleteById(id: String): Future[Int] = db.run(oauthTokenTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class OAuthTokenTable(tag: Tag) extends Table[OAuthToken](tag, "OAuthToken") {

    def * = (id, accessToken, expiresAt, refreshToken, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (OAuthToken.tupled, OAuthToken.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def accessToken = column[String]("accessToken")

    def expiresAt = column[Long]("expiresAt")

    def refreshToken = column[String]("refreshToken")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(id: String, accessToken: String, expiresAt: Long, refreshToken: String): Future[String] = add(OAuthToken(id, accessToken, expiresAt, refreshToken))

    def get(id: String): Future[Option[OAuthToken]] = getByID(id)

    def tryGet(id: String): Future[OAuthToken] = tryGetByID(id)

    def update(id: String, accessToken: String, expiresAt: Long, refreshToken: String): Future[Int] = updateByID(id, accessToken, expiresAt, refreshToken)
  }

  object Utility {
    def regenerateOAuthToken(): Future[Unit] = {
      val oauthToken = Service.tryGet(accountID)

      def regenerateAndUpdateOAuthToken(oauthToken: OAuthToken): Future[Unit] = if (System.currentTimeMillis() > (oauthToken.expiresAt - docusignOAuthTokenIntervalTime.toMillis)) {
        val response = docusignRegenerateToken.Service.post(docusignRegenerateToken.Request(constants.External.Docusign.REFRESH_TOKEN, oauthToken.refreshToken))

        def updateOauthToken(response: Response): Future[Int] = Service.update(accountID, response.access_token, System.currentTimeMillis() + response.expires_in * 1000, response.refresh_token)

        for {
          response <- response
          _ <- updateOauthToken(response)
        } yield {}
      } else Future()

      (for {
        oauthToken <- oauthToken
        _ <- regenerateAndUpdateOAuthToken(oauthToken)
      } yield {}
        ).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          baseException.failure match {
            case constants.Response.NO_SUCH_ELEMENT_EXCEPTION => utilitiesNotification.send(constants.User.MAIN_ACCOUNT, constants.Notification.DOCUSIGN_AUTHORIZATION_PENDING)()
            case _ => throw baseException
          }
        case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
          throw new BaseException(constants.Response.CONNECT_EXCEPTION)
      }
    }
  }

  actorSystem.scheduler.schedule(initialDelay = docusignOAuthTokenInitialDelay, interval = docusignOAuthTokenIntervalTime) {
    Utility.regenerateOAuthToken()
  }(schedulerExecutionContext)
}
