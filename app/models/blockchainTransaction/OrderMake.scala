package models.blockchainTransaction

import exceptions.BaseException
import models.Abstract.BaseTransaction
import models.Trait.Logged
import models.common.Serializable._
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import utilities.MicroNumber

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class OrderMake(from: String, fromID: String, classificationID: String, makerOwnableID: String, takerOwnableID: String, expiresIn: Int, makerOwnableSplit: BigDecimal, immutableMetaProperties: Seq[BaseProperty], immutableProperties: Seq[BaseProperty], mutableMetaProperties: Seq[BaseProperty], mutableProperties: Seq[BaseProperty], gas: MicroNumber, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends BaseTransaction[OrderMake] with Logged {
  def mutateTicketID(newTicketID: String): OrderMake = OrderMake(from = from, fromID = fromID, classificationID = classificationID, makerOwnableID = makerOwnableID, takerOwnableID = takerOwnableID, expiresIn = expiresIn, makerOwnableSplit = makerOwnableSplit, immutableMetaProperties = immutableMetaProperties, immutableProperties = immutableProperties, mutableMetaProperties = mutableMetaProperties, mutableProperties = mutableProperties, gas = gas, status = status, txHash = txHash, ticketID = newTicketID, mode = mode, code = code)
}

@Singleton
class OrderMakes @Inject()(
                            transaction: utilities.Transaction,
                            protected val databaseConfigProvider: DatabaseConfigProvider,
                            utilitiesNotification: utilities.Notification,
                            masterAccounts: master.Accounts,
                            blockchainAccounts: blockchain.Accounts,
                            blockchainTransactions: blockchain.Transactions,
                            masterProperties: master.Properties
                          )(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  case class PropertiesSerialized(immutableMetaProperties: String, immutableProperties: String, mutableMetaProperties: String, mutableProperties: String)

  case class OrderMakeSerialized(from: String, fromID: String, classificationID: String, makerOwnableID: String, takerOwnableID: String, expiresIn: Int, makerOwnableSplit: BigDecimal, propertiesSerialized: PropertiesSerialized, gas: String, status: Option[Boolean], txHash: Option[String], ticketID: String, mode: String, code: Option[String], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: OrderMake = OrderMake(from = from, fromID = fromID, classificationID = classificationID, makerOwnableID = makerOwnableID, takerOwnableID = takerOwnableID, expiresIn = expiresIn, makerOwnableSplit = makerOwnableSplit, immutableMetaProperties = utilities.JSON.convertJsonStringToObject[Seq[BaseProperty]](propertiesSerialized.immutableMetaProperties), immutableProperties = utilities.JSON.convertJsonStringToObject[Seq[BaseProperty]](propertiesSerialized.immutableProperties), mutableMetaProperties = utilities.JSON.convertJsonStringToObject[Seq[BaseProperty]](propertiesSerialized.mutableMetaProperties), mutableProperties = utilities.JSON.convertJsonStringToObject[Seq[BaseProperty]](propertiesSerialized.mutableProperties), gas = new MicroNumber(BigInt(gas)), status = status, txHash = txHash, ticketID = ticketID, mode = mode, code = code, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedOn = updatedOn, updatedBy = updatedBy, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(orderMake: OrderMake): OrderMakeSerialized = OrderMakeSerialized(from = orderMake.from, fromID = orderMake.fromID, classificationID = orderMake.classificationID, makerOwnableID = orderMake.makerOwnableID, takerOwnableID = orderMake.takerOwnableID, expiresIn = orderMake.expiresIn, makerOwnableSplit = orderMake.makerOwnableSplit, propertiesSerialized = PropertiesSerialized(immutableMetaProperties = Json.toJson(orderMake.immutableMetaProperties).toString, immutableProperties = Json.toJson(orderMake.immutableProperties).toString, mutableMetaProperties = Json.toJson(orderMake.mutableMetaProperties).toString, mutableProperties = Json.toJson(orderMake.mutableProperties).toString), gas = orderMake.gas.toMicroString, status = orderMake.status, txHash = orderMake.txHash, ticketID = orderMake.ticketID, mode = orderMake.mode, code = orderMake.code, createdBy = orderMake.createdBy, createdOn = orderMake.createdOn, createdOnTimeZone = orderMake.createdOnTimeZone, updatedBy = orderMake.updatedBy, updatedOn = orderMake.updatedOn, updatedOnTimeZone = orderMake.updatedOnTimeZone)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  private implicit val logger: Logger = Logger(this.getClass)
  val db = databaseConfig.db

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_ORDER_MAKE

  private val schedulerExecutionContext: ExecutionContext = actors.Service.actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val orderMakeTable = TableQuery[OrderMakeTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").second

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private val enableActor = configuration.get[Boolean]("blockchain.enableTransactionSchemaActors")

  private def add(orderMake: OrderMake): Future[String] = db.run((orderMakeTable returning orderMakeTable.map(_.ticketID) += serialize(orderMake)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(orderMakeTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(orderMakeTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateStatusByTicketID(ticketID: String, status: Option[Boolean]): Future[Int] = db.run(orderMakeTable.filter(_.ticketID === ticketID).map(_.status.?).update(status).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByTicketID(ticketID: String): Future[OrderMakeSerialized] = db.run(orderMakeTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(orderMakeTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(orderMakeTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(orderMakeTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(orderMakeTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def getTransactionListByFromAddress(fromAddress: String): Future[Seq[OrderMakeSerialized]] = db.run(orderMakeTable.filter(_.from === fromAddress).result)

  private[models] class OrderMakeTable(tag: Tag) extends Table[OrderMakeSerialized](tag, "OrderMake") {

    def * = (from, fromID, classificationID, makerOwnableID, takerOwnableID, expiresIn, makerOwnableSplit,
      (immutableMetaProperties, immutableProperties, mutableMetaProperties, mutableProperties),
      gas, status.?, txHash.?, ticketID, mode, code.?,
      createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?).shaped <> ( {
      case (from, fromID, classificationID, makerOwnableID, takerOwnableID, expiresIn, makerOwnableSplit,
      propertiesSerialized,
      gas, status, txHash, ticketID, mode, code,
      createdBy, createdOn, createdOnTimeZone, updatedBy, updatedOn, updatedOnTimeZone) => OrderMakeSerialized(
        from = from, fromID = fromID, classificationID = classificationID, makerOwnableID = makerOwnableID, takerOwnableID = takerOwnableID, expiresIn = expiresIn, makerOwnableSplit = makerOwnableSplit,
        propertiesSerialized = PropertiesSerialized.tupled.apply(propertiesSerialized),
        gas = gas, status = status, txHash = txHash, ticketID = ticketID, mode = mode, code = code,
        createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone
      )
    }, { orderMakeSerialized: OrderMakeSerialized =>
      def f1(propertiesSerialized: PropertiesSerialized) = PropertiesSerialized.unapply(propertiesSerialized).get

      Some((orderMakeSerialized.from, orderMakeSerialized.fromID, orderMakeSerialized.classificationID, orderMakeSerialized.makerOwnableID, orderMakeSerialized.takerOwnableID, orderMakeSerialized.expiresIn, orderMakeSerialized.makerOwnableSplit,
        f1(orderMakeSerialized.propertiesSerialized),
        orderMakeSerialized.gas, orderMakeSerialized.status, orderMakeSerialized.txHash, orderMakeSerialized.ticketID,
        orderMakeSerialized.mode, orderMakeSerialized.code,
        orderMakeSerialized.createdBy, orderMakeSerialized.createdOn, orderMakeSerialized.createdOnTimeZone,
        orderMakeSerialized.updatedBy, orderMakeSerialized.updatedOn, orderMakeSerialized.updatedOnTimeZone
      ))
    })

    def from = column[String]("from")

    def fromID = column[String]("fromID")

    def classificationID = column[String]("classificationID")

    def makerOwnableID = column[String]("makerOwnableID")

    def takerOwnableID = column[String]("takerOwnableID")

    def expiresIn = column[Int]("expiresIn")

    def makerOwnableSplit = column[BigDecimal]("makerOwnableSplit")

    def immutableMetaProperties = column[String]("immutableMetaProperties")

    def immutableProperties = column[String]("immutableProperties")

    def mutableMetaProperties = column[String]("mutableMetaProperties")

    def mutableProperties = column[String]("mutableProperties")

    def gas = column[String]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def mode = column[String]("mode")

    def code = column[String]("code")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(orderMake: OrderMake): Future[String] = add(orderMake)

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def resetTransactionStatus(ticketID: String): Future[Int] = updateStatusByTicketID(ticketID, status = null)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[OrderMake] = findByTicketID(ticketID).map(_.deserialize)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

    def getTransactionList(fromAddress: String) = getTransactionListByFromAddress(fromAddress).map(_.map(_.deserialize))
  }

  object Utility {
    def onSuccess(ticketID: String, txHash: String): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, txHash)
      val orderMake = Service.getTransaction(ticketID)
      val getTxHeight = blockchainTransactions.Service.tryGetHeight(txHash)

      def getAccountID(from: String) = blockchainAccounts.Service.tryGetUsername(from)

      def insertProperties(orderMake: OrderMake) = masterProperties.Utilities.upsertProperties(entityID = utilities.IDGenerator.getOrderID(classificationID = orderMake.classificationID, makerOwnableID = orderMake.makerOwnableID, takerOwnableID = orderMake.takerOwnableID, makerID = orderMake.fromID, immutables = Immutables(Properties((orderMake.immutableMetaProperties ++ orderMake.immutableProperties).map(_.toProperty)))),
        entityType = constants.Blockchain.Entity.ORDER, immutableMetas = orderMake.immutableMetaProperties, immutables = orderMake.immutableProperties, mutableMetas = orderMake.mutableMetaProperties, mutables = orderMake.mutableProperties)

      def updateExpiry(orderID: String, height: Int, orderMake: OrderMake) = masterProperties.Service.insertOrUpdate(master.Property(entityID = orderID, entityType = constants.Blockchain.Entity.ORDER, name = constants.Blockchain.Properties.Expiry, value = Option((height + orderMake.expiresIn).toString), dataType = constants.Blockchain.DataType.HEIGHT_DATA, isMeta = true, isMutable = true, hashID = utilities.Hash.getHash((height + orderMake.expiresIn).toString)))

      def updateMakerOwnableSplit(orderID: String, orderMake: OrderMake) = masterProperties.Service.insertOrUpdate(master.Property(entityID = orderID, entityType = constants.Blockchain.Entity.ORDER, name = constants.Blockchain.Properties.MakerOwnableSplit, value = Option(orderMake.makerOwnableSplit.toString), dataType = constants.Blockchain.DataType.DEC_DATA, isMeta = true, isMutable = true, hashID = utilities.Hash.getHash(orderMake.makerOwnableSplit.toString)))

      def sendNotifications(accountID: String, orderID: String) = utilitiesNotification.send(accountID, constants.Notification.ORDER_MADE, orderID, txHash)(s"'$txHash'")

      (for {
        _ <- markTransactionSuccessful
        orderMake <- orderMake
        height <- getTxHeight
        orderID <- insertProperties(orderMake)
        _ <- updateExpiry(orderID, height, orderMake)
        _ <- updateMakerOwnableSplit(orderID, orderMake)
        accountID <- getAccountID(orderMake.from)
        _ <- sendNotifications(accountID = accountID, orderID = orderID)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = {
      val markTransactionFailed = Service.markTransactionFailed(ticketID, message)

      (for {
        _ <- markTransactionFailed
      } yield ()).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  private val txRunnable = new Runnable {
    def run(): Unit = Await.result(transaction.ticketUpdater(Service.getTicketIDsOnStatus, Service.getTransactionHash, Service.getMode, Utility.onSuccess, Utility.onFailure), Duration.Inf)
  }

  if ((kafkaEnabled || transactionMode != constants.Transactions.BLOCK_MODE) && enableActor) {
    actors.Service.actorSystem.scheduler.scheduleWithFixedDelay(initialDelay = schedulerInitialDelay, delay = schedulerInterval)(txRunnable)(schedulerExecutionContext)
  }
}