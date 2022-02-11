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

case class Identity(id: String, accountID: String, nubID: Option[String], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Identities @Inject()(
                            configuration: Configuration,
                            protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_IDENTITY

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val identityTable = TableQuery[IdentityTable]

  private def add(identity: Identity): Future[String] = db.run((identityTable returning identityTable.map(_.id) += identity).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def addMultiple(identities: Seq[Identity]): Future[Seq[String]] = db.run((identityTable returning identityTable.map(_.id) ++= identities).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(identity: Identity): Future[Int] = db.run(identityTable.insertOrUpdate(identity).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def getByID(id: String, accountID: String) = db.run(identityTable.filter(x => x.id === id && x.accountID === accountID).result.headOption)

  private def getAllByIdentityIDs(ids: Seq[String]) = db.run(identityTable.filter(_.id.inSet(ids)).result)

  private[models] class IdentityTable(tag: Tag) extends Table[Identity](tag, "Identity") {

    def * = (id, accountID, nubID.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Identity.tupled, Identity.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def accountID = column[String]("accountID", O.PrimaryKey)

    def nubID = column[String]("nubID")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def insertMultiple(identities: Seq[Identity]): Future[Seq[String]] = addMultiple(identities)

    def insertOrUpdate(identity: Identity): Future[Int] = upsert(identity)

    def getAllByIDs(ids: Seq[String]): Future[Seq[Identity]] = getAllByIdentityIDs(ids)

    def get(id: String, accountID: String): Future[Option[Identity]] = getByID(id = id, accountID = accountID)
  }

  object Utility {

    def onSignIn(accountID: String, identityId: String): Future[Unit] = {
      val optionalIdentity = Service.get(id = identityId, accountID = accountID)

    }
  }

}