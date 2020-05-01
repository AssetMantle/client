package models.blockchain

import akka.actor.{ActorSystem}
import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{master, masterTransaction}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Order(id: String, fiatProofHash: Option[String], awbProofHash: Option[String], dirtyBit: Boolean)

@Singleton
class Orders @Inject()(
                        masterAccounts: master.Accounts,
                        masterNegotiations: master.Negotiations,
                        masterAssets: master.Assets,
                        actorSystem: ActorSystem,
                        protected val databaseConfigProvider: DatabaseConfigProvider,
                        getAccount: queries.GetAccount,
                        blockchainNegotiations: Negotiations,
                        blockchainTraderFeedbackHistories: TraderFeedbackHistories,
                        blockchainAssets: Assets,
                        blockchainFiats: Fiats,
                        getOrder: queries.GetOrder,
                        utilitiesNotification: utilities.Notification
                      )(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ORDER

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  private[models] val orderTable = TableQuery[OrderTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private def add(order: Order): Future[String] = db.run((orderTable returning orderTable.map(_.id) += order).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(order: Order): Future[Int] = db.run(orderTable.insertOrUpdate(order).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[Order] = db.run(orderTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getOrdersByDirtyBit(dirtyBit: Boolean): Future[Seq[Order]] = db.run(orderTable.filter(_.dirtyBit === dirtyBit).result)

  private def updateDirtyBitById(id: String, dirtyBit: Boolean): Future[Int] = db.run(orderTable.filter(_.id === id).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getOrdersByIDs(ids: Seq[String]): Future[Seq[Order]] = db.run(orderTable.filter(_.id.inSet(ids)).result)

  private def getOrderIDs: Future[Seq[String]] = db.run(orderTable.map(_.id).result)

  private def getOrderIDsWithoutFiatProofHash: Future[Seq[String]] = db.run(orderTable.filter(_.fiatProofHash.?.isEmpty).map(_.id).result)

  private def getOrderIDsWithoutAWBProofHash: Future[Seq[String]] = db.run(orderTable.filter(_.awbProofHash.?.isEmpty).map(_.id).result)

  private def deleteById(id: String): Future[Int] = db.run(orderTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
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

    def create(id: String, fiatProofHash: Option[String], awbProofHash: Option[String]): Future[String] = add(Order(id = id, fiatProofHash = fiatProofHash, awbProofHash = awbProofHash, dirtyBit = true))

    def insertOrUpdate(id: String, fiatProofHash: Option[String], awbProofHash: Option[String], dirtyBit: Boolean): Future[Int] = upsert(Order(id = id, fiatProofHash = fiatProofHash, awbProofHash = awbProofHash, dirtyBit = dirtyBit))

    def getDirtyOrders: Future[Seq[Order]] = getOrdersByDirtyBit(dirtyBit = true)

    def getOrders(ids: Seq[String]): Future[Seq[Order]] = getOrdersByIDs(ids)

    def getAllOrderIds: Future[Seq[String]] = getOrderIDs

    def getAllOrderIdsWithoutFiatProofHash: Future[Seq[String]] = getOrderIDsWithoutFiatProofHash

    def getAllOrderIdsWithoutAWBProofHash: Future[Seq[String]] = getOrderIDsWithoutAWBProofHash

    def markDirty(id: String): Future[Int] = updateDirtyBitById(id, dirtyBit = true)
  }

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = {
      val dirtyOrders = Service.getDirtyOrders
      Thread.sleep(sleepTime)

      def insertOrUpdateAndSendCometMessage(dirtyOrders: Seq[Order]): Future[Seq[Unit]] = {
        Future.sequence {
          dirtyOrders.map { dirtyOrder =>
            val orderResponse = getOrder.Service.get(dirtyOrder.id)
            val negotiation = blockchainNegotiations.Service.get(dirtyOrder.id)

            def sellerOrBuyerUpsertAssetsOrFiats(orderResponse: queries.responses.OrderResponse.Response, negotiation: Negotiation): Future[Unit] = {
              if ((orderResponse.value.awbProofHash != "" && orderResponse.value.fiatProofHash != "") || (orderResponse.value.awbProofHash == "" && orderResponse.value.fiatProofHash == "")) {
                val sellerAccount = getAccount.Service.get(negotiation.sellerAddress)
                val buyerAccount = getAccount.Service.get(negotiation.buyerAddress)

                def upsertSellerAccountAssets(sellerAccount: queries.responses.AccountResponse.Response) = {
                  sellerAccount.value.assetPegWallet match {
                    case Some(assets) => Future.sequence(assets.map(asset => blockchainAssets.Service.insertOrUpdate(pegHash = asset.pegHash, documentHash = asset.documentHash, assetType = asset.assetType, assetQuantity = asset.assetQuantity, quantityUnit = asset.quantityUnit, assetPrice = asset.assetPrice, ownerAddress = negotiation.sellerAddress, moderated = asset.moderated, takerAddress = if (asset.takerAddress == "") None else Option(asset.takerAddress), locked = asset.locked, dirtyBit = true)))
                    case None => Future {}
                  }
                }

                def upsertSellerAccountFiats(sellerAccount: queries.responses.AccountResponse.Response) = {
                  sellerAccount.value.fiatPegWallet match {
                    case Some(fiats) => Future.sequence(fiats.map(fiatPeg => blockchainFiats.Service.insertOrUpdate(fiatPeg.pegHash, negotiation.sellerAddress, fiatPeg.transactionID, fiatPeg.transactionAmount, fiatPeg.redeemedAmount, dirtyBit = true)))
                    case None => Future {}
                  }
                }

                def upsertBuyerAccountAssets(buyerAccount: queries.responses.AccountResponse.Response) = {
                  buyerAccount.value.assetPegWallet match {
                    case Some(assets) => Future.sequence(assets.map(asset => blockchainAssets.Service.insertOrUpdate(pegHash = asset.pegHash, documentHash = asset.documentHash, assetType = asset.assetType, assetQuantity = asset.assetQuantity, quantityUnit = asset.quantityUnit, assetPrice = asset.assetPrice, ownerAddress = negotiation.buyerAddress, moderated = asset.moderated, takerAddress = if (asset.takerAddress == "") None else Option(asset.takerAddress), locked = asset.locked, dirtyBit = true)))
                    case None => Future {}
                  }
                }

                def upsertBuyerAccountFiats(buyerAccount: queries.responses.AccountResponse.Response) = {
                  buyerAccount.value.fiatPegWallet match {
                    case Some(fiats) => Future.sequence(fiats.map(fiatPeg => blockchainFiats.Service.insertOrUpdate(fiatPeg.pegHash, negotiation.buyerAddress, fiatPeg.transactionID, fiatPeg.transactionAmount, fiatPeg.redeemedAmount, dirtyBit = true)))
                    case None => Future {}
                  }
                }

                def deleteFiatPegWallet: Future[Int] = blockchainFiats.Service.deleteFiatPegWallet(dirtyOrder.id)

                for {
                  sellerAccount <- sellerAccount
                  _ <- upsertSellerAccountAssets(sellerAccount)
                  _ <- upsertSellerAccountFiats(sellerAccount)
                  buyerAccount <- buyerAccount
                  _ <- upsertBuyerAccountAssets(buyerAccount)
                  _ <- upsertBuyerAccountFiats(buyerAccount)
                  _ <- deleteFiatPegWallet
                } yield Unit
              } else Future(Unit)
            }

            def insertOrUpdateTraderFeedbackHistories(orderResponse: queries.responses.OrderResponse.Response, negotiation: Negotiation): Future[Unit] = {
              if (orderResponse.value.awbProofHash != "" && orderResponse.value.fiatProofHash != "") {
                val insertOrUpdateSellerFeedbackHistories = blockchainTraderFeedbackHistories.Service.insertOrUpdate(negotiation.sellerAddress, negotiation.buyerAddress, negotiation.sellerAddress, negotiation.assetPegHash, rating = "")
                val insertOrUpdateBuyerFeedbackHistories = blockchainTraderFeedbackHistories.Service.insertOrUpdate(negotiation.buyerAddress, negotiation.buyerAddress, negotiation.sellerAddress, negotiation.assetPegHash, rating = "")

                def markTradeCompleted: Future[Unit] = {
                  val updateNegotiationStatus = masterNegotiations.Service.markTradeCompletedByNegotiationID(negotiation.id)
                  val updateAssetStatus = masterAssets.Service.markTradeCompletedByPegHash(negotiation.assetPegHash)
                  for {
                    _ <- updateNegotiationStatus
                    _ <- updateAssetStatus
                  } yield Unit
                }

                def deleteNegotiations: Future[Int] = blockchainNegotiations.Service.deleteNegotiations(negotiation.assetPegHash)

                for {
                  _ <- insertOrUpdateSellerFeedbackHistories
                  _ <- insertOrUpdateBuyerFeedbackHistories
                  _ <- markTradeCompleted
                  _ <- deleteNegotiations
                } yield Unit
              } else Future(Unit)
            }

            def insertOrUpdateOrder(orderResponse: queries.responses.OrderResponse.Response): Future[Int] = Service.insertOrUpdate(dirtyOrder.id, awbProofHash = if (orderResponse.value.awbProofHash == "") None else Option(orderResponse.value.awbProofHash), fiatProofHash = if (orderResponse.value.fiatProofHash == "") None else Option(orderResponse.value.fiatProofHash), dirtyBit = false)

            def ids(negotiation: Negotiation): Future[(String, String)] = {
              val buyerAddressID = masterAccounts.Service.tryGetId(negotiation.buyerAddress)
              val sellerAddressID = masterAccounts.Service.tryGetId(negotiation.sellerAddress)
              for {
                buyerAddressID <- buyerAddressID
                sellerAddressID <- sellerAddressID
              } yield (buyerAddressID, sellerAddressID)
            }

            (for {
              orderResponse <- orderResponse
              negotiation <- negotiation
              _ <- sellerOrBuyerUpsertAssetsOrFiats(orderResponse, negotiation)
              _ <- insertOrUpdateTraderFeedbackHistories(orderResponse, negotiation)
              _ <- insertOrUpdateOrder(orderResponse)
              (buyerAddressID, sellerAddressID) <- ids(negotiation)
            } yield {
              actors.Service.cometActor ! actors.Message.makeCometMessage(username = buyerAddressID, messageType = constants.Comet.ORDER, messageContent = actors.Message.Order())
              actors.Service.cometActor ! actors.Message.makeCometMessage(username = sellerAddressID, messageType = constants.Comet.ORDER, messageContent = actors.Message.Order())
            }).recover {
              case baseException: BaseException => logger.error(baseException.failure.message, baseException)
            }
          }
        }
      }

      (for {
        dirtyOrders <- dirtyOrders
        _ <- insertOrUpdateAndSendCometMessage(dirtyOrders)
      } yield {}) (schedulerExecutionContext)
    }
  }

  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }(schedulerExecutionContext)
}