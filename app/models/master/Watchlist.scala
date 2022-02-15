package models.master

import exceptions.BaseException
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Watchlist(accountID: String, watching: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Watchlists @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_PROFILE

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private implicit val logger: Logger = Logger(this.getClass)

  private[models] val watchlistTable = TableQuery[WatchlistTable]

  private def add(watchlist: Watchlist): Future[String] = db.run((watchlistTable returning watchlistTable.map(_.accountID) += watchlist).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def getListByID(id: String): Future[Seq[Watchlist]] = db.run(watchlistTable.filter(_.accountID === id).result)

  private def tryGetByID(id: String): Future[Watchlist] = db.run(watchlistTable.filter(_.accountID === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByWatching(id: String, watching: String): Future[Int] = db.run(watchlistTable.filter(x => x.accountID === id && x.watching === watching).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private[models] class WatchlistTable(tag: Tag) extends Table[Watchlist](tag, "Watchlist") {

    def * = (accountID, watching, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Watchlist.tupled, Watchlist.unapply)

    def accountID = column[String]("accountID", O.PrimaryKey)

    def watching = column[String]("watching", O.PrimaryKey)

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def getList(id: String): Future[Seq[Watchlist]] = getListByID(id)

    def create(id: String, watching: String): Future[String] = add(Watchlist(accountID = id, watching = watching))

    def delete(id: String, watching: String): Future[Int] = deleteByWatching(id = id, watching=watching)

  }

}

