package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.HistoryLogged
import models.common.Serializable._
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery
import utilities.MicroNumber

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class AssetHistory(id: String, label: Option[String] = None, status: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None, deletedBy: String, deletedOn: Timestamp, deletedOnTimeZone: String) extends HistoryLogged

@Singleton
class AssetHistories @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private[models] val assetHistoryTable = TableQuery[AssetHistoryTable]

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ASSET_HISTORY

  import databaseConfig.profile.api._

  private def tryGetByID(id: String): Future[AssetHistory] = db.run(assetHistoryTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findAllByIDs(ids: Seq[String]): Future[Seq[AssetHistory]] = db.run(assetHistoryTable.filter(_.id.inSet(ids)).result)

  private[models] class AssetHistoryTable(tag: Tag) extends Table[AssetHistory](tag, "Asset_History") {

    def * = (id, label.?, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?, deletedBy, deletedOn, deletedOnTimeZone) <> (AssetHistory.tupled, AssetHistory.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def label = column[String]("label")

    def status = column[String]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

    def deletedBy = column[String]("deletedBy")

    def deletedOn = column[Timestamp]("deletedOn")

    def deletedOnTimeZone = column[String]("deletedOnTimeZone")

  }

  object Service {

    def tryGet(id: String): Future[AssetHistory] = tryGetByID(id)

    def getAllAssetsByID(ids: Seq[String]): Future[Seq[AssetHistory]] = findAllByIDs(ids)

  }

}
