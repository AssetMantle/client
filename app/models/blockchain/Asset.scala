package models.blockchain

import java.sql.Timestamp

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.blockchain
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.GetAccount
import queries.responses.AccountResponse.Response
import slick.jdbc.JdbcProfile
import utilities.MicroNumber

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Asset(pegHash: String, documentHash: String, assetType: String, assetQuantity: MicroNumber, assetPrice: MicroNumber, quantityUnit: String, ownerAddress: String, locked: Boolean, moderated: Boolean, takerAddress: Option[String], dirtyBit: Boolean, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Assets @Inject()(
                        protected val databaseConfigProvider: DatabaseConfigProvider,
                        actorSystem: ActorSystem,
                        getAccount: GetAccount,
                        blockchainAccounts: blockchain.Accounts,
                        configuration: Configuration,
                      )(implicit executionContext: ExecutionContext) {

  def serialize(asset: Asset): AssetSerialized = AssetSerialized(pegHash = asset.pegHash, documentHash = asset.documentHash, assetType = asset.assetType, assetQuantity = asset.assetQuantity.toMicroString, assetPrice = asset.assetPrice.toMicroString, quantityUnit = asset.quantityUnit, ownerAddress = asset.ownerAddress, locked = asset.locked, moderated = asset.moderated, takerAddress = asset.takerAddress, dirtyBit = asset.dirtyBit, createdBy = asset.createdBy, createdOn = asset.createdOn, createdOnTimeZone = asset.createdOnTimeZone, updatedBy = asset.updatedBy, updatedOn = asset.updatedOn, updatedOnTimeZone = asset.updatedOnTimeZone)

  case class AssetSerialized(pegHash: String, documentHash: String, assetType: String, assetQuantity: String, assetPrice: String, quantityUnit: String, ownerAddress: String, locked: Boolean, moderated: Boolean, takerAddress: Option[String], dirtyBit: Boolean, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Asset = Asset(pegHash = pegHash, documentHash = documentHash, assetType = assetType, assetQuantity = new MicroNumber(BigInt(assetQuantity)), assetPrice = new MicroNumber(BigInt(assetPrice)), quantityUnit = quantityUnit, ownerAddress = ownerAddress, locked = locked, moderated = moderated, takerAddress = takerAddress, dirtyBit = dirtyBit, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ASSET

  import databaseConfig.profile.api._

  private[models] val assetTable = TableQuery[AssetTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private def add(assetSerialized: AssetSerialized): Future[String] = db.run((assetTable returning assetTable.map(_.pegHash) += assetSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateByPegHash(assetSerialized: AssetSerialized): Future[Int] = db.run(assetTable.filter(_.pegHash === assetSerialized.pegHash).update(assetSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateOwnerAddressByPegHash(pegHash: String, address: String): Future[Int] = db.run(assetTable.filter(_.pegHash === pegHash).map(_.ownerAddress).update(address).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findByPegHash(pegHash: String): Future[AssetSerialized] = db.run(assetTable.filter(_.pegHash === pegHash).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getAssetPegWalletByAddress(address: String): Future[Seq[AssetSerialized]] = db.run(assetTable.filter(_.ownerAddress === address).result)

  private def getAssetPegHashesByAddress(address: String): Future[Seq[String]] = db.run(assetTable.filter(_.ownerAddress === address).map(_.pegHash).result)

  private def updateDirtyBitByPegHash(pegHash: String, dirtyBit: Boolean): Future[Int] = db.run(assetTable.filter(_.pegHash === pegHash).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getAssetsByDirtyBit(dirtyBit: Boolean): Future[Seq[AssetSerialized]] = db.run(assetTable.filter(_.dirtyBit === dirtyBit).result)

  private def tryGetLockedStatusByPegHash(pegHash: String): Future[Boolean] = db.run(assetTable.filter(_.pegHash === pegHash).map(_.locked).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByPegHash(pegHash: String): Future[Int] = db.run(assetTable.filter(_.pegHash === pegHash).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteAssetPegWalletByAddress(ownerAddress: String): Future[Int] = db.run(assetTable.filter(_.ownerAddress === ownerAddress).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class AssetTable(tag: Tag) extends Table[AssetSerialized](tag, _tableName = "Asset_BC") {

    def * = (pegHash, documentHash, assetType, assetQuantity, assetPrice, quantityUnit, ownerAddress, locked, moderated, takerAddress.?, dirtyBit, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (AssetSerialized.tupled, AssetSerialized.unapply)

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

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(pegHash: String, documentHash: String, assetType: String, assetQuantity: MicroNumber, assetPrice: MicroNumber, quantityUnit: String, ownerAddress: String, moderated: Boolean, takerAddress: Option[String], locked: Boolean, dirtyBit: Boolean): Future[String] = add(serialize(Asset(pegHash = pegHash, documentHash = documentHash, assetType = assetType, assetPrice = assetPrice, assetQuantity = assetQuantity, quantityUnit = quantityUnit, ownerAddress = ownerAddress, moderated = moderated, takerAddress = takerAddress, locked = locked, dirtyBit = dirtyBit)))

    def tryGet(pegHash: String): Future[Asset] = findByPegHash(pegHash).map(_.deserialize)

    def getAssetPegWallet(address: String): Future[Seq[Asset]] = getAssetPegWalletByAddress(address).map(_.map(_.deserialize))

    def getAssetPegHashes(address: String): Future[Seq[String]] = getAssetPegHashesByAddress(address)

    def update(asset: Asset): Future[Int] = updateByPegHash(serialize(asset))

    def markAssetSentToOrder(pegHash: String, address: String): Future[Int] = updateOwnerAddressByPegHash(pegHash = pegHash, address = address)

    def deleteAsset(pegHash: String): Future[Int] = deleteByPegHash(pegHash)

    def deleteAssetPegWallet(ownerAddress: String): Future[Int] = deleteAssetPegWalletByAddress(ownerAddress)

    def getDirtyAssets: Future[Seq[Asset]] = getAssetsByDirtyBit(dirtyBit = true).map(_.map(_.deserialize))

    def tryGetLockedStatus(pegHash: String): Future[Boolean] = tryGetLockedStatusByPegHash(pegHash)

    def markDirty(pegHash: String): Future[Int] = updateDirtyBitByPegHash(pegHash, dirtyBit = true)
  }

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = {
      val dirtyAssets = Service.getDirtyAssets
      Thread.sleep(sleepTime)

      def insertOrUpdateAndSendCometMessage(dirtyAssets: Seq[Asset]): Future[Seq[Unit]] = {
        Future.sequence {
          dirtyAssets.map { dirtyAsset =>
            val accountResponse = getAccount.Service.get(dirtyAsset.ownerAddress)

            def updateOrDelete(ownerAccount: Response): Future[Int] = {
              ownerAccount.value.asset_peg_wallet match {
                case Some(assetPegWallet) => assetPegWallet.find(_.pegHash == dirtyAsset.pegHash) match {
                  case Some(assetPeg) => Service.update(Asset(pegHash = assetPeg.pegHash, documentHash = assetPeg.documentHash, assetType = assetPeg.assetType, assetPrice = assetPeg.assetPrice, assetQuantity = assetPeg.assetQuantity, quantityUnit = assetPeg.quantityUnit, ownerAddress = dirtyAsset.ownerAddress, locked = assetPeg.locked, moderated = assetPeg.moderated, takerAddress = if (assetPeg.takerAddress == "") None else Option(assetPeg.takerAddress), dirtyBit = false))
                  case None => Service.deleteAsset(dirtyAsset.pegHash)
                }
                case None => Service.deleteAssetPegWallet(dirtyAsset.ownerAddress)
              }
            }

            def accountID: Future[String] = blockchainAccounts.Service.tryGetUsername(dirtyAsset.ownerAddress)

            for {
              accountResponse <- accountResponse
              _ <- updateOrDelete(accountResponse)
              accountID <- accountID
            } yield actors.Service.cometActor ! actors.Message.makeCometMessage(username = accountID, messageType = constants.Comet.ASSET, messageContent = actors.Message.Asset())
          }
        }
      }

      (for {
        dirtyAssets <- dirtyAssets
        _ <- insertOrUpdateAndSendCometMessage(dirtyAssets)
      } yield ()).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }(schedulerExecutionContext)
}