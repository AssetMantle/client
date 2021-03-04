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

case class Asset(id: String, label: Option[String] = None, status: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

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

  private def tryGetByID(id: String) = db.run(assetTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findAllByOwnerIDsAndStatus(assetIDs: Seq[String], status: String): Future[Seq[Asset]] = db.run(assetTable.filter(_.id.inSet(assetIDs)).filter(_.status === status).result)

  private def findStatusByID(id: String): Future[String] = db.run(assetTable.filter(_.id === id).map(_.status).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

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

  private def updateStatusByID(id: String, status: String): Future[Int] = db.run(assetTable.filter(_.id === id).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findAllTradableAssetsByStatues(assetIDs: Seq[String], statuses: String*): Future[Seq[Asset]] = db.run(assetTable.filter(_.id.inSet(assetIDs)).filter(_.status.inSet(statuses)).sortBy(x => x.updatedOn.ifNull(x.createdOn).desc).result)

  private def getAllByAssetIDs(ids: Seq[String]) = db.run(assetTable.filter(_.id.inSet(ids)).result)

  private[models] class AssetTable(tag: Tag) extends Table[Asset](tag, "Asset") {

    def * = (id, label.?, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Asset.tupled, Asset.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def label = column[String]("label")

    def status = column[String]("status")

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

    def tryGet(id: String): Future[Asset] = tryGetByID(id)

    def checkExists(id: String): Future[Boolean] = checkByID(id)

    def updateLabel(id: String, label: String): Future[Int] = updateLabelByID(id = id, label = Option(label))

    def tryGetStatus(id: String): Future[String] = findStatusByID(id)

    def getPendingIssueAssetRequests(assetIDs: Seq[String]): Future[Seq[Asset]] = findAllByOwnerIDsAndStatus(assetIDs = assetIDs, status = constants.Status.Asset.REQUESTED_TO_ZONE)

    def getAllTradableAssets(assetIDs: Seq[String]): Future[Seq[Asset]] = findAllTradableAssetsByStatues(assetIDs = assetIDs, constants.Status.Asset.REQUESTED_TO_ZONE, constants.Status.Asset.AWAITING_BLOCKCHAIN_RESPONSE, constants.Status.Asset.ISSUED)

    def markTraded(id: String): Future[Int] = updateStatusByID(id = id, status = constants.Status.Asset.TRADED)

  }

}
