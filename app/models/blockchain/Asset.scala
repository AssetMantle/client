package models.blockchain

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Asset(pegHash: String, documentHash: String, assetType: String, assetQuantity: Int, assetPrice: Int, quantityUnit: String, ownerAddress: String, locked: Boolean)

@Singleton
class Assets @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

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

  private def insertOrUpdate(asset: Asset)(implicit executionContext: ExecutionContext): Future[Int] = db.run(assetTable.insertOrUpdate(asset).asTry).map {
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

  private def updateOwnerAddressAndLockedStatusByPegHash(pegHash: String, locked: Boolean, ownerAddress: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(assetTable.filter(_.pegHash === pegHash).map(x => (x.ownerAddress, x.locked)).update((ownerAddress, locked)).asTry).map {
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

  private def getAllPegHash()(implicit executionContext: ExecutionContext): Future[Seq[String]] = db.run(assetTable.map(_.pegHash).result)

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

    def insertOrUpdateAsset(pegHash: String, documentHash: String, assetType: String, assetQuantity: Int, assetPrice: Int, quantityUnit: String, ownerAddress: String, locked: Boolean)(implicit executionContext: ExecutionContext): Int = Await.result(insertOrUpdate(Asset(pegHash = pegHash, documentHash = documentHash, assetType = assetType, assetQuantity = assetQuantity, assetPrice = assetPrice, quantityUnit = quantityUnit, ownerAddress = ownerAddress, locked = locked)), Duration.Inf)

    def updateOwner(pegHash: String, ownerAddress: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateOwnerByPegHash(pegHash, ownerAddress), Duration.Inf)

    def updateLockedStatus(pegHash: String, locked: Boolean)(implicit executionContext: ExecutionContext): Int = Await.result(updateLockedStatusByPegHash(pegHash, locked), Duration.Inf)

    def updateOwnerAddressAndLockedStatus(pegHash: String, locked: Boolean, ownerAddress: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateOwnerAddressAndLockedStatusByPegHash(pegHash, locked, ownerAddress), Duration.Inf)

    def deleteAsset(pegHash: String)(implicit executionContext: ExecutionContext): Int = Await.result(deleteByPegHash(pegHash), Duration.Inf)
  }

}