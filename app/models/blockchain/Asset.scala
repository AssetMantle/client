package models.blockchain

import akka.actor.ActorSystem
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.master
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.GetAccount
import slick.jdbc.JdbcProfile
import utilities.PushNotifications

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Asset(pegHash: String, documentHash: String, assetType: String, assetQuantity: String, assetPrice: String, quantityUnit: String, ownerAddress: String, locked: Boolean, moderator: Boolean, dirtyBit: Boolean)

@Singleton
class Assets @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, getAccount: GetAccount, masterAccounts: master.Accounts, actorSystem: ActorSystem, implicit val pushNotifications: PushNotifications)(implicit executionContext: ExecutionContext, configuration: Configuration) {

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

  private def upsert(asset: Asset)(implicit executionContext: ExecutionContext): Future[Int] = db.run(assetTable.insertOrUpdate(asset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
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

  private def updateDirtyBitByPegHash(pegHash: String, dirtyBit: Boolean)(implicit executionContext: ExecutionContext): Future[Int] = db.run(assetTable.filter(_.pegHash === pegHash).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateOwnerAddressByPegHash(pegHash: String, ownerAddress: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(assetTable.filter(_.pegHash === pegHash).map(_.ownerAddress).update(ownerAddress).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAssetsByDirtyBit(dirtyBit: Boolean)(implicit executionContext: ExecutionContext): Future[Seq[Asset]] = db.run(assetTable.filter(_.dirtyBit === dirtyBit).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
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

  private def deleteAssetPegWalletByAddress(ownerAddress: String)(implicit executionContext: ExecutionContext) = db.run(assetTable.filter(_.ownerAddress === ownerAddress).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.info(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        0
    }
  }

  private[models] class AssetTable(tag: Tag) extends Table[Asset](tag, _tableName = "Asset_BC") {

    def * = (pegHash, documentHash, assetType, assetQuantity, assetPrice, quantityUnit, ownerAddress, locked, moderator, dirtyBit) <> (Asset.tupled, Asset.unapply)

    def pegHash = column[String]("pegHash", O.PrimaryKey)

    def documentHash = column[String]("documentHash")

    def assetType = column[String]("assetType")

    def assetQuantity = column[String]("assetQuantity")

    def assetPrice = column[String]("assetPrice")

    def quantityUnit = column[String]("quantityUnit")

    def ownerAddress = column[String]("ownerAddress")

    def locked = column[Boolean]("locked")

    def moderator = column[Boolean]("moderator")

    def dirtyBit = column[Boolean]("dirtyBit")

  }

  object Service {

    def addAsset(pegHash: String, documentHash: String, assetType: String, assetQuantity: String, assetPrice: String, quantityUnit: String, ownerAddress: String, moderator: Boolean, locked: Boolean, dirtyBit: Boolean)(implicit executionContext: ExecutionContext): String = Await.result(add(Asset(pegHash = pegHash, documentHash = documentHash, assetType = assetType, assetPrice = assetPrice, assetQuantity = assetQuantity, quantityUnit = quantityUnit, ownerAddress = ownerAddress, moderator = moderator, locked = locked, dirtyBit = dirtyBit)), Duration.Inf)

    def getAsset(pegHash: String)(implicit executionContext: ExecutionContext): Asset = Await.result(findByPegHash(pegHash), Duration.Inf)

    def getAssetPegWallet(address: String)(implicit executionContext: ExecutionContext): Seq[Asset] = Await.result(getAssetPegWalletByAddress(address), Duration.Inf)

    def insertOrUpdate(pegHash: String, documentHash: String, assetType: String, assetQuantity: String, assetPrice: String, quantityUnit: String, ownerAddress: String, moderator: Boolean, locked: Boolean, dirtyBit: Boolean)(implicit executionContext: ExecutionContext): Int = Await.result(upsert(Asset(pegHash = pegHash, documentHash = documentHash, assetType = assetType, assetQuantity = assetQuantity, assetPrice = assetPrice, quantityUnit = quantityUnit, ownerAddress = ownerAddress, moderator = moderator, locked = locked, dirtyBit = dirtyBit)), Duration.Inf)

    def deleteAsset(pegHash: String)(implicit executionContext: ExecutionContext): Int = Await.result(deleteByPegHash(pegHash), Duration.Inf)

    def deleteAssetPegWallet(ownerAddress: String)(implicit executionContext: ExecutionContext): Int = Await.result(deleteAssetPegWalletByAddress(ownerAddress), Duration.Inf)

    def getDirtyAssets: Seq[Asset] = Await.result(getAssetsByDirtyBit(dirtyBit = true), Duration.Inf)

    def markDirty(pegHash: String): Int = Await.result(updateDirtyBitByPegHash(pegHash, dirtyBit = true), Duration.Inf)

    def updateOwnerAddress(pegHash: String, ownerAddress: String): Int = Await.result(updateOwnerAddressByPegHash(pegHash, ownerAddress), Duration.Inf)

  }

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = Future {
      val dirtyAssets = Service.getDirtyAssets
      Thread.sleep(sleepTime)
      for (dirtyAsset <- dirtyAssets) {
        try {
          val assetPegWallet = getAccount.Service.get(dirtyAsset.ownerAddress).value.assetPegWallet.getOrElse(throw new BaseException(constants.Error.NO_RESPONSE))
          assetPegWallet.foreach(assetPeg => if (assetPegWallet.map(_.pegHash) contains dirtyAsset.pegHash) Service.insertOrUpdate(pegHash = assetPeg.pegHash, documentHash = assetPeg.documentHash, assetType = assetPeg.assetType, assetPrice = assetPeg.assetPrice, assetQuantity = assetPeg.assetQuantity, quantityUnit = assetPeg.quantityUnit, ownerAddress = dirtyAsset.ownerAddress, locked = assetPeg.locked, moderator = assetPeg.moderator, dirtyBit = false) else Service.deleteAsset(dirtyAsset.pegHash))
        }
        catch {
          case baseException: BaseException => logger.info(constants.Error.BASE_EXCEPTION, baseException)
            if (baseException.message == module + "." + constants.Error.NO_RESPONSE) {
              Service.deleteAssetPegWallet(dirtyAsset.ownerAddress)
            }
          case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
        }
      }
    }
  }


  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }

}