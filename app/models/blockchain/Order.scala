package models.blockchain

import akka.actor.ActorSystem
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.responses.AccountResponse
import slick.jdbc.JdbcProfile
import utilities.PushNotifications

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Order(id: String, fiatProofHash: Option[String], awbProofHash: Option[String], executed: Boolean, dirtyBit: Boolean)

@Singleton
class Orders @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, getAccount: queries.GetAccount, blockchainNegotiations: Negotiations, blockchainAssets: Assets, blockchainFiats: Fiats, getOrder: queries.GetOrder, actorSystem: ActorSystem, implicit val pushNotifications: PushNotifications)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ORDER

  private[models] val orderTable = TableQuery[OrderTable]
  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val sleepTime = configuration.get[Long]("blockchain.kafka.entityIterator.threadSleep")

  private def add(order: Order)(implicit executionContext: ExecutionContext): Future[String] = db.run((orderTable returning orderTable.map(_.id) += order).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def insertOrUpdate(order: Order)(implicit executionContext: ExecutionContext): Future[Int] = db.run(orderTable.insertOrUpdate(order).asTry).map {
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

    def * = (id, fiatProofHash.?, awbProofHash.?, executed, dirtyBit) <> (Order.tupled, Order.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def fiatProofHash = column[String]("fiatProofHash")

    def awbProofHash = column[String]("awbProofHash")

    def executed = column[Boolean]("executed")

    def dirtyBit = column[Boolean]("dirtyBit")

  }

  object Service {

    def addOrder(id: String, fiatProofHash: Option[String], awbProofHash: Option[String], executed: Boolean)(implicit executionContext: ExecutionContext): String = Await.result(add(Order(id = id, fiatProofHash = fiatProofHash, awbProofHash = awbProofHash, executed = executed, dirtyBit = false)), Duration.Inf)

    def insertOrUpdateOrder(id: String, fiatProofHash: Option[String], awbProofHash: Option[String], executed: Boolean, dirtyBit: Boolean)(implicit executionContext: ExecutionContext): Int = Await.result(insertOrUpdate(Order(id = id, fiatProofHash = fiatProofHash, awbProofHash = awbProofHash, executed = executed, dirtyBit = dirtyBit)), Duration.Inf)

    def getDirtyOrders(dirtyBit: Boolean): Seq[Order] = Await.result(getOrdersByDirtyBit(dirtyBit), Duration.Inf)

    def updateDirtyBit(id: String, dirtyBit: Boolean): Int = Await.result(updateDirtyBitById(id, dirtyBit), Duration.Inf)
  }

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = Future {
      val dirtyOrders = Service.getDirtyOrders(true)
      Thread.sleep(sleepTime)
      for (dirtyOrder <- dirtyOrders) {
        try {
          val orderResponse = getOrder.Service.get(dirtyOrder.id)
          val negotiation = blockchainNegotiations.Service.getNegotiation(dirtyOrder.id)
          if (orderResponse.value.awbProofHash != "" && orderResponse.value.fiatProofHash != "") {
            Service.insertOrUpdateOrder(dirtyOrder.id, awbProofHash = Option(orderResponse.value.awbProofHash), fiatProofHash = Option(orderResponse.value.fiatProofHash), executed = true, dirtyBit = false)

            blockchainAssets.Service.updateOwnerAddress(pegHash = negotiation.assetPegHash, ownerAddress = negotiation.buyerAddress)

            getAccount.Service.get(negotiation.sellerAddress).value.fiatPegWallet.getOrElse(Seq(AccountResponse.Fiat(null, null, null, null, null))).foreach(fiatWallet => {
              blockchainFiats.Service.insertOrUpdateFiat(fiatWallet.pegHash, negotiation.sellerAddress, fiatWallet.transactionID, fiatWallet.transactionAmount, fiatWallet.redeemedAmount, dirtyBit = true)
            })

          } else {
            Service.insertOrUpdateOrder(dirtyOrder.id, awbProofHash = Option(orderResponse.value.awbProofHash), fiatProofHash = Option(orderResponse.value.fiatProofHash), executed = false, dirtyBit = false)
          }

        }
        catch {
          case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
          case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
        }
      }
    }
  }

  //Scheduler- iterates orders with dirty tags
  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }

}