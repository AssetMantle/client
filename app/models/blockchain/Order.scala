package models.blockchain

import akka.actor.ActorSystem
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import utilities.PushNotification

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Order(id: String, fiatProofHash: Option[String], awbProofHash: Option[String], dirtyBit: Boolean)

@Singleton
class Orders @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, getAccount: queries.GetAccount, blockchainNegotiations: Negotiations, blockchainAssets: Assets, blockchainFiats: Fiats, getOrder: queries.GetOrder, actorSystem: ActorSystem, implicit val pushNotification: PushNotification)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ORDER

  private[models] val orderTable = TableQuery[OrderTable]
  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private def add(order: Order)(implicit executionContext: ExecutionContext): Future[String] = db.run((orderTable returning orderTable.map(_.id) += order).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def upsert(order: Order)(implicit executionContext: ExecutionContext): Future[Int] = db.run(orderTable.insertOrUpdate(order).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String)(implicit executionContext: ExecutionContext): Future[Order] = db.run(orderTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getOrdersByDirtyBit(dirtyBit: Boolean): Future[Seq[Order]] = db.run(orderTable.filter(_.dirtyBit === dirtyBit).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        Nil
    }
  }

  private def updateDirtyBitById(id: String, dirtyBit: Boolean): Future[Int] = db.run(orderTable.filter(_.id === id).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(orderTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class OrderTable(tag: Tag) extends Table[Order](tag, "Order_BC") {

    def * = (id, fiatProofHash.?, awbProofHash.?, dirtyBit) <> (Order.tupled, Order.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def fiatProofHash = column[String]("fiatProofHash")

    def awbProofHash = column[String]("awbProofHash")

    def dirtyBit = column[Boolean]("dirtyBit")

  }

  object Service {

    def addOrder(id: String, fiatProofHash: Option[String], awbProofHash: Option[String])(implicit executionContext: ExecutionContext): String = Await.result(add(Order(id = id, fiatProofHash = fiatProofHash, awbProofHash = awbProofHash, dirtyBit = false)), Duration.Inf)

    def insertOrUpdate(id: String, fiatProofHash: Option[String], awbProofHash: Option[String], dirtyBit: Boolean)(implicit executionContext: ExecutionContext): Int = Await.result(upsert(Order(id = id, fiatProofHash = fiatProofHash, awbProofHash = awbProofHash, dirtyBit = dirtyBit)), Duration.Inf)

    def getDirtyOrders(dirtyBit: Boolean): Seq[Order] = Await.result(getOrdersByDirtyBit(dirtyBit), Duration.Inf)

    def markDirty(id: String): Int = Await.result(updateDirtyBitById(id, dirtyBit = true), Duration.Inf)
  }

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = Future {
      val dirtyOrders = Service.getDirtyOrders(true)
      Thread.sleep(sleepTime)
      for (dirtyOrder <- dirtyOrders) {
        try {
          val orderResponse = getOrder.Service.get(dirtyOrder.id)
          val negotiation = blockchainNegotiations.Service.getNegotiation(dirtyOrder.id)
          if ((orderResponse.value.awbProofHash != "" && orderResponse.value.fiatProofHash != "") || (orderResponse.value.awbProofHash == "" && orderResponse.value.fiatProofHash == "")) {
            val sellerAccount = getAccount.Service.get(negotiation.sellerAddress)
            if (sellerAccount.value.assetPegWallet.isDefined) {
              sellerAccount.value.assetPegWallet.get.foreach(asset => blockchainAssets.Service.insertOrUpdate(pegHash = asset.pegHash, documentHash = asset.documentHash, assetType = asset.assetType, assetQuantity = asset.assetQuantity, quantityUnit = asset.quantityUnit, assetPrice = asset.assetPrice, ownerAddress = negotiation.sellerAddress, moderator = asset.moderator, locked = asset.locked, dirtyBit = false))
            } else {
              blockchainAssets.Service.deleteAssetPegWallet(negotiation.sellerAddress)
            }
            sellerAccount.value.fiatPegWallet.getOrElse(Seq()).foreach(fiatPeg => {
              blockchainFiats.Service.insertOrUpdate(fiatPeg.pegHash, negotiation.sellerAddress, fiatPeg.transactionID, fiatPeg.transactionAmount, fiatPeg.redeemedAmount, dirtyBit = true)
            })

            val buyerAccount = getAccount.Service.get(negotiation.buyerAddress)
            if (buyerAccount.value.assetPegWallet.isDefined) {
              buyerAccount.value.assetPegWallet.get.foreach(asset => blockchainAssets.Service.insertOrUpdate(pegHash = asset.pegHash, documentHash = asset.documentHash, assetType = asset.assetType, assetQuantity = asset.assetQuantity, quantityUnit = asset.quantityUnit, assetPrice = asset.assetPrice, ownerAddress = negotiation.buyerAddress, moderator = asset.moderator, locked = asset.locked, dirtyBit = false))
            } else {
              blockchainAssets.Service.deleteAssetPegWallet(negotiation.buyerAddress)
            }
            buyerAccount.value.fiatPegWallet.getOrElse(Seq()).foreach(fiatPeg => {
              blockchainFiats.Service.insertOrUpdate(fiatPeg.pegHash, negotiation.buyerAddress, fiatPeg.transactionID, fiatPeg.transactionAmount, fiatPeg.redeemedAmount, dirtyBit = true)
            })

            blockchainFiats.Service.deleteFiatPegWallet(dirtyOrder.id)
          }
          Service.insertOrUpdate(dirtyOrder.id, awbProofHash = Option(orderResponse.value.awbProofHash), fiatProofHash = Option(orderResponse.value.fiatProofHash), dirtyBit = false)
        }
        catch {
          case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
          case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
        }
      }
    }
  }

  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }

}