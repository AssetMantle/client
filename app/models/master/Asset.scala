package models.master

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Serializable
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Asset(pegHash: String, status: String)

@Singleton
class Assets @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {
  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db
  private[models] val assetTable = TableQuery[AssetTable]

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ASSET

  import databaseConfig.profile.api._

  private def add(asset: Asset): Future[String] = db.run((assetTable returning assetTable.map(_.pegHash) += asset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateStatusbyPegHash(pegHash:String, status:String)=db.run(assetTable.filter(_.pegHash === pegHash).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def upsert(asset: Asset): Future[Int] = db.run(assetTable.insertOrUpdate(asset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getStatusByPegHash(pegHash: String): Future[String] = db.run(assetTable.filter(_.pegHash === pegHash).map(_.status).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAssetListByPegHashes(pegHashes: Seq[String])=db.run(assetTable.filter(_.pegHash inSet pegHashes).result)

  private def getAssetListByStatuses(statuses: Seq[String])=db.run(assetTable.filter(_.status inSet statuses).result)


  private[models] class AssetTable(tag: Tag) extends Table[Asset](tag, "Asset") {

    def * = (pegHash, status) <> (Asset.tupled, Asset.unapply)

    def pegHash = column[String]("pegHash", O.PrimaryKey)

    def status = column[String]("status")

  }

  object Service {

    def create(pegHash:String,status: String)= add(Asset(pegHash,status))

    def insertOrUpdate(pegHash:String,status: String)=upsert(Asset(pegHash =pegHash, status=status))

    def getStatus(pegHash:String)= getStatusByPegHash(pegHash)

    def getAssetsByPegHashes(pegHashes:Seq[String]):Future[Seq[Asset]]= getAssetListByPegHashes(pegHashes)

    def getMarketAssets()=getAssetListByStatuses(Seq(constants.Status.Asset.LISTED_FOR_TRADE, constants.Status.Asset.UNDER_NEGOTIATION))

    def markReedemed(pegHash:String)=updateStatusbyPegHash(pegHash,constants.Status.Asset.REDEEMED)

    def markTradeCompleted(pegHash:String)=updateStatusbyPegHash(pegHash,constants.Status.Asset.COMPLETED_TRADE)

  }

}
