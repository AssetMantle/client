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

case class Asset(id: String, label: Option[String] = None, status: Option[Boolean], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Assets @Inject()(
                        configuration: Configuration,
                        protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_ASSET

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val assetTable = TableQuery[AssetTable]

  private def add(asset: Asset): Future[String] = db.run((assetTable returning assetTable.map(_.id) += asset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def addMultiple(assets: Seq[Asset]): Future[Seq[String]] = db.run((assetTable returning assetTable.map(_.id) ++= assets).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(asset: Asset): Future[Int] = db.run(assetTable.insertOrUpdate(asset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def getByID(id: String) = db.run(assetTable.filter(_.id === id).result.headOption)

  private def checkByID(id: String) = db.run(assetTable.filter(_.id === id).exists.result)

  private def deleteByID(id: String) = db.run(assetTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateLabelByID(id: String, label: Option[String]): Future[Int] = db.run(assetTable.filter(x => x.id === id).map(_.label.?).update(label).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getAllByAssetIDs(ids: Seq[String]) = db.run(assetTable.filter(_.id.inSet(ids)).result)

  private[models] class AssetTable(tag: Tag) extends Table[Asset](tag, "Asset") {

    def * = (id, label.?, status.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Asset.tupled, Asset.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def label = column[String]("label")

    def status = column[Boolean]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(asset: Asset): Future[String] = add(asset)

    def insertMultiple(assets: Seq[Asset]): Future[Seq[String]] = addMultiple(assets)

    def insertOrUpdate(asset: Asset): Future[Int] = upsert(asset)

    def delete(id: String): Future[Int] = deleteByID(id)

    def getAllByIDs(ids: Seq[String]): Future[Seq[Asset]] = getAllByAssetIDs(ids)

    def get(id: String): Future[Option[Asset]] = getByID(id)

    def checkExists(id: String): Future[Boolean] = checkByID(id)

    def updateLabel(id: String, label: String): Future[Int] = updateLabelByID(id = id, label = Option(label))

  }

}