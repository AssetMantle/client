package models.blockchain

import actors.{Create, ShutdownActor}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl.Source
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{JsValue, Json}
import play.api.{Configuration, Logger}
import queries.GetAccount
import queries.responses.AccountResponse.Response
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Asset(pegHash: String, documentHash: String, assetType: String, assetQuantity: String, assetPrice: String, quantityUnit: String, ownerAddress: String, locked: Boolean, moderated: Boolean, takerAddress: Option[String], dirtyBit: Boolean)

@Singleton
class Assets @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, actorsCreate: actors.Create, actorSystem: ActorSystem, shutdownActors: ShutdownActor, getAccount: GetAccount, masterAccounts: master.Accounts, implicit val utilitiesNotification: utilities.Notification)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ASSET

  import databaseConfig.profile.api._

  private[models] val assetTable = TableQuery[AssetTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private def add(asset: Asset): Future[String] = db.run((assetTable returning assetTable.map(_.pegHash) += asset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(asset: Asset): Future[Int] = db.run(assetTable.insertOrUpdate(asset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByPegHash(pegHash: String): Future[Asset] = db.run(assetTable.filter(_.pegHash === pegHash).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findAllAssetsByPublic(excludedAssets: Seq[String]): Future[Seq[Asset]] = db.run(assetTable.filter(assets => !(assets.ownerAddress inSet excludedAssets)).result)

  private def findAllAssetsByLockedStatus(ownerAddresses: Seq[String], locked: Boolean): Future[Seq[Asset]] = db.run(assetTable.filter(_.locked === locked).filter(asset => asset.ownerAddress inSet ownerAddresses).result)

  private def findByPegHashes(pegHashes: Seq[String]): Future[Seq[Asset]] = db.run(assetTable.filter(asset => asset.pegHash inSet pegHashes).result)

  private def getAssetPegWalletByAddress(address: String): Future[Seq[Asset]] = db.run(assetTable.filter(_.ownerAddress === address).result)

  private def getAssetPegHashesByAddress(address: String): Future[Seq[String]] = db.run(assetTable.filter(_.ownerAddress === address).map(_.pegHash).result)

  private def updateDirtyBitByPegHash(pegHash: String, dirtyBit: Boolean): Future[Int] = db.run(assetTable.filter(_.pegHash === pegHash).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAssetsByDirtyBit(dirtyBit: Boolean): Future[Seq[Asset]] = db.run(assetTable.filter(_.dirtyBit === dirtyBit).result)

  private def tryGetLockedStatusByPegHash(pegHash: String): Future[Boolean] = db.run(assetTable.filter(_.pegHash === pegHash).map(_.locked).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByPegHash(pegHash: String): Future[Int] = db.run(assetTable.filter(_.pegHash === pegHash).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteAssetPegWalletByAddress(ownerAddress: String) = db.run(assetTable.filter(_.ownerAddress === ownerAddress).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        0
    }
  }

  private[models] class AssetTable(tag: Tag) extends Table[Asset](tag, _tableName = "Asset_BC") {

    def * = (pegHash, documentHash, assetType, assetQuantity, assetPrice, quantityUnit, ownerAddress, locked, moderated, takerAddress.?, dirtyBit) <> (Asset.tupled, Asset.unapply)

    def pegHash = column[String]("pegHash", O.PrimaryKey)

    def documentHash = column[String]("documentHash")

    def assetType = column[String]("assetType")

    def assetQuantity = column[String]("assetQuantity")

    def assetPrice = column[String]("assetPrice")

    def quantityUnit = column[String]("quantityUnit")

    def ownerAddress = column[String]("ownerAddress")

    def locked = column[Boolean]("locked")

    def moderated = column[Boolean]("moderated")

    def takerAddress = column[String]("takerAddress")

    def dirtyBit = column[Boolean]("dirtyBit")

  }

  object Service {

    def create(pegHash: String, documentHash: String, assetType: String, assetQuantity: String, assetPrice: String, quantityUnit: String, ownerAddress: String, moderated: Boolean, takerAddress: Option[String], locked: Boolean, dirtyBit: Boolean): Future[String] = add(Asset(pegHash = pegHash, documentHash = documentHash, assetType = assetType, assetPrice = assetPrice, assetQuantity = assetQuantity, quantityUnit = quantityUnit, ownerAddress = ownerAddress, moderated = moderated, takerAddress = takerAddress, locked = locked, dirtyBit = dirtyBit))

    def get(pegHash: String): Future[Asset] = findByPegHash(pegHash)

    def getAllPublic(excludedAssets: Seq[String]): Future[Seq[Asset]] = findAllAssetsByPublic(excludedAssets)

    def getAllLocked(ownerAddresses: Seq[String]): Future[Seq[Asset]] = findAllAssetsByLockedStatus(ownerAddresses = ownerAddresses, locked = true)

    def getByPegHashes(pegHashes: Seq[String]): Future[Seq[Asset]] = findByPegHashes(pegHashes)

    def getAssetPegWallet(address: String): Future[Seq[Asset]] = getAssetPegWalletByAddress(address)

    def getAssetPegHashes(address: String): Future[Seq[String]] = getAssetPegHashesByAddress(address)

    def insertOrUpdate(pegHash: String, documentHash: String, assetType: String, assetQuantity: String, assetPrice: String, quantityUnit: String, ownerAddress: String, moderated: Boolean, takerAddress: Option[String], locked: Boolean, dirtyBit: Boolean): Future[Int] = upsert(Asset(pegHash = pegHash, documentHash = documentHash, assetType = assetType, assetQuantity = assetQuantity, assetPrice = assetPrice, quantityUnit = quantityUnit, ownerAddress = ownerAddress, moderated = moderated, takerAddress = takerAddress, locked = locked, dirtyBit = dirtyBit))

    def deleteAsset(pegHash: String): Future[Int] = deleteByPegHash(pegHash)

    def deleteAssetPegWallet(ownerAddress: String): Future[Int] = deleteAssetPegWalletByAddress(ownerAddress)

    def getDirtyAssets: Future[Seq[Asset]] = getAssetsByDirtyBit(dirtyBit = true)

    def tryGetLockedStatus(pegHash: String): Future[Boolean] = tryGetLockedStatusByPegHash(pegHash)

    def markDirty(pegHash: String): Future[Int] = updateDirtyBitByPegHash(pegHash, dirtyBit = true)
  }

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = {
      val dirtyAssets = Service.getDirtyAssets
      Thread.sleep(sleepTime)

      def insertOrUpdateAndSendCometMessage(dirtyAssets: Seq[Asset]) = {
        Future.sequence {
          dirtyAssets.map { dirtyAsset =>
            val ownerAccount = getAccount.Service.get(dirtyAsset.ownerAddress)

            def insertOrUpdate(ownerAccount: Response): Future[Unit] = {
              val assetPegWallet = ownerAccount.value.assetPegWallet.getOrElse(throw new BaseException(constants.Response.NO_RESPONSE))
              val upsert = Future.sequence(assetPegWallet.map { assetPeg => if (assetPegWallet.map(_.pegHash) contains dirtyAsset.pegHash) Service.insertOrUpdate(pegHash = assetPeg.pegHash, documentHash = assetPeg.documentHash, assetType = assetPeg.assetType, assetPrice = assetPeg.assetPrice, assetQuantity = assetPeg.assetQuantity, quantityUnit = assetPeg.quantityUnit, ownerAddress = dirtyAsset.ownerAddress, locked = assetPeg.locked, moderated = assetPeg.moderated, takerAddress = if (assetPeg.takerAddress == "") None else Option(assetPeg.takerAddress), dirtyBit = false) else Service.deleteAsset(dirtyAsset.pegHash) })
              for {
                _ <- upsert
              } yield {}
            }

            def accountID: Future[String] = masterAccounts.Service.getId(dirtyAsset.ownerAddress)

            (for {
              ownerAccount <- ownerAccount
              _ <- insertOrUpdate(ownerAccount)
              accountID <- accountID
            } yield {
               actorsCreate.mainActor ! actors.Message.makeCometMessage(username = accountID, messageType = constants.Comet.ASSET, messageContent = actors.Message.Asset())
            }).recover {
              case baseException: BaseException => logger.info(baseException.failure.message, baseException)
                if (baseException.failure == constants.Response.NO_RESPONSE) {
                  val deleteAssetPegWallet = Service.deleteAssetPegWallet(dirtyAsset.ownerAddress)
                  val id = masterAccounts.Service.getId(dirtyAsset.ownerAddress)
                  for {
                    _ <- deleteAssetPegWallet
                    id <- id
                  } yield actorsCreate.mainActor ! actors.Message.makeCometMessage(username = id, messageType = constants.Comet.ASSET, messageContent = actors.Message.Asset())
                }
            }
          }
        }
      }

      (for {
        dirtyAssets <- dirtyAssets
        _ <- insertOrUpdateAndSendCometMessage(dirtyAssets)
      } yield {}) (schedulerExecutionContext)
    }
  }

  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }(schedulerExecutionContext)
}