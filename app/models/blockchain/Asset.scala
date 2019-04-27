package models.blockchain

import akka.actor.ActorSystem
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import slick.jdbc.JdbcProfile
import transactions.GetAsset
import utilities.PushNotifications
import scala.concurrent.duration._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Asset(pegHash: String, documentHash: String, assetType: String, assetQuantity: Int, assetPrice: Int, quantityUnit: String, ownerAddress: String, locked: Boolean)

class Assets @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, actorSystem: ActorSystem, implicit val pushNotifications: PushNotifications)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ASSET

  import databaseConfig.profile.api._

  private[models] val assetTable = TableQuery[AssetTable]

  private def add(asset: Asset)(implicit executionContext: ExecutionContext): Future[String] = db.run((assetTable returning assetTable.map(_.pegHash) += asset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def updateOwnerByPegHash(pegHash: String, ownerAddress: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(assetTable.filter(_.pegHash === pegHash).map(_.ownerAddress).update(ownerAddress).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateOwnerAndLockedStatusByPegHash(pegHash: String, locked: Boolean, ownerAddress: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(assetTable.filter(_.pegHash === pegHash).map(x => (x.ownerAddress, x.locked)).update((ownerAddress, locked)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateLockedStatusByPegHash(pegHash: String, locked: Boolean)(implicit executionContext: ExecutionContext): Future[Int] = db.run(assetTable.filter(_.pegHash === pegHash).map(_.locked).update(locked).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByPegHash(pegHash: String)(implicit executionContext: ExecutionContext): Future[Asset] = db.run(assetTable.filter(_.pegHash === pegHash).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAssetPegWalletByAddress(address: String)(implicit executionContext: ExecutionContext): Future[Seq[Asset]] = db.run(assetTable.filter(_.ownerAddress === address).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.info(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        Nil
    }
  }

  private def deleteByPegHash(pegHash: String)(implicit executionContext: ExecutionContext) = db.run(assetTable.filter(_.pegHash === pegHash).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAllPegHash()(implicit executionContext: ExecutionContext):Future[Seq[String]] = db.run(assetTable.map(_.pegHash).result)

  actorSystem.scheduler.schedule(initialDelay = configuration.get[Int]("blockchain.Iterator.initialDelay").seconds, interval = configuration.get[Int]("blockchain.Iterator.interval").second) {
    AssetIterator.start()
  }

  private[models] class AssetTable(tag: Tag) extends Table[Asset](tag, "Asset_BC") {

    def * = (pegHash, documentHash, assetType, assetQuantity, assetPrice, quantityUnit, ownerAddress, locked) <> (Asset.tupled, Asset.unapply)

    def pegHash = column[String]("pegHash", O.PrimaryKey)

    def documentHash = column[String]("documentHash")

    def assetType = column[String]("assetType")

    def assetQuantity = column[Int]("assetQuantity")

    def assetPrice = column[Int]("assetPrice")

    def quantityUnit = column[String]("quantityUnit")

    def ownerAddress = column[String]("ownerAddress")

    def locked = column[Boolean]("locked")

  }

  object Service{

    def addAsset(pegHash: String, documentHash: String, assetType: String, assetQuantity: Int, assetPrice: Int, quantityUnit: String, ownerAddress: String)(implicit executionContext: ExecutionContext): String = Await.result(add(Asset(pegHash = pegHash, documentHash = documentHash, assetType = assetType, assetPrice = assetPrice, assetQuantity = assetQuantity, quantityUnit = quantityUnit, ownerAddress = ownerAddress, locked = true)), Duration.Inf)

    def getAsset(pegHash: String)(implicit executionContext: ExecutionContext): Asset = Await.result(findByPegHash(pegHash), Duration.Inf)

    def getAssetPegWallet(address: String)(implicit executionContext: ExecutionContext): Seq[Asset] = Await.result(getAssetPegWalletByAddress(address), Duration.Inf)

    def updateOwner(pegHash: String, ownerAddress: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateOwnerByPegHash(pegHash, ownerAddress), Duration.Inf)

    def updateLockedStatus(pegHash: String, locked: Boolean)(implicit executionContext: ExecutionContext): Int = Await.result(updateLockedStatusByPegHash(pegHash, locked), Duration.Inf)

    def deleteAsset(pegHash: String)(implicit executionContext: ExecutionContext): Int = Await.result(deleteByPegHash(pegHash), Duration.Inf)
  }

  object AssetIterator {

    def start()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext, logger: Logger, pushNotifications: PushNotifications): Unit = {
      implicit val getAsset: GetAsset = new GetAsset()(wsClient, configuration, executionContext)
      val pegHashes: Seq[String] = Await.result(getAllPegHash(), Duration.Inf)
      try {
        for(pegHash <- pegHashes){
          val asset = getAsset.Service.get(pegHash).value
          Await.result(updateOwnerAndLockedStatusByPegHash(pegHash, asset.locked, asset.ownerAddress), Duration.Inf)
        }
      }
      catch {
        case blockChainException: BlockChainException  => logger.info(blockChainException.message, blockChainException)
        case baseException: BaseException => logger.info(baseException.message, baseException)
      }
    }
  }

}