package models.master

import exceptions.BaseException
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class IdentityNub(identityID: String, nubID: String, creatorAddress: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class IdentityNubs @Inject()(
                              configuration: Configuration,
                              protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_IDENTITY_NUB

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val identityNubTable = TableQuery[IdentityNubTable]

  private def add(identityNub: IdentityNub): Future[String] = db.run((identityNubTable returning identityNubTable.map(_.identityID) += identityNub).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def addMultiple(identityNubs: Seq[IdentityNub]): Future[Seq[String]] = db.run((identityNubTable returning identityNubTable.map(_.identityID) ++= identityNubs).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(identityNub: IdentityNub): Future[Int] = db.run(identityNubTable.insertOrUpdate(identityNub).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def getByID(identityID: String) = db.run(identityNubTable.filter(_.identityID === identityID).result.headOption)

  private[models] class IdentityNubTable(tag: Tag) extends Table[IdentityNub](tag, "IdentityNub") {

    def * = (identityID, nubID, creatorAddress, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (IdentityNub.tupled, IdentityNub.unapply)

    def identityID = column[String]("identityID", O.PrimaryKey)

    def nubID = column[String]("nubID", O.Unique)

    def creatorAddress = column[String]("creatorAddress")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(identityID: String, nubID: String, creatorAddress: String): Future[String] = add(IdentityNub(identityID = identityID, nubID = nubID, creatorAddress = creatorAddress))

    def insertMultiple(identities: Seq[IdentityNub]): Future[Seq[String]] = addMultiple(identities)

    def insertOrUpdate(identityNub: IdentityNub): Future[Int] = upsert(identityNub)

    def get(identityID: String): Future[Option[IdentityNub]] = getByID(identityID = identityID)
  }

  object Utility {

    def onIdentityNubTx(identityID: String, nubID: String, creatorAddress: String): Future[Unit] = {
      val add = Service.create(identityID = identityID, nubID = nubID, creatorAddress = creatorAddress)

      (for {
        _ <- add
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

  }

}