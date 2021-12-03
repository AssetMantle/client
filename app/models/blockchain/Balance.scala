package models.blockchain

import models.blockchain.Balances.{Create, Get, GetList, InsertOrUpdate, TryGet}
import akka.actor.{Actor, ActorLogging, Props}
import exceptions.BaseException
import models.Trait.Logged
import models.common.Serializable.Coin
import models.common.TransactionMessages.{Acknowledgement, MultiSend, RecvPacket, SendCoin, Timeout, TimeoutOnClose, Transfer}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.blockchain.GetBalance
import queries.responses.blockchain.BalanceResponse.{Response => BalanceResponse}
import queries.responses.common.Header
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import akka.pattern.{ask, pipe}
import akka.util.{Timeout => akkaTimeout}
import akka.cluster.sharding.ShardRegion.{ExtractEntityId, ExtractShardId}
import models.Abstract.ShardedActorRegion
import constants.Actor.{NUMBER_OF_ENTITIES, NUMBER_OF_SHARDS}

import java.util.UUID



case class Balance(address: String, coins: Seq[Coin], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Balances @Inject()(
                          protected val databaseConfigProvider: DatabaseConfigProvider,
                          getBalance: GetBalance,
                          configuration: Configuration,
                          utilitiesOperations: utilities.Operations,
                        )(implicit executionContext: ExecutionContext) extends ShardedActorRegion {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_BALANCE

  private val uniqueId: String = UUID.randomUUID().toString

  import databaseConfig.profile.api._

  private[models] val balanceTable = TableQuery[BalanceTable]

  private implicit val timeout = akkaTimeout(constants.Actor.ACTOR_ASK_TIMEOUT)

  override def idExtractor: ExtractEntityId = {
    case attempt@Get(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGet(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@Create(id, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdate(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetList(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  override def shardResolver: ExtractShardId = {
    case Get(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGet(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case Create(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdate(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetList(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }

  override def regionName: String = "balanceRegion"

  override def props: Props = Balances.props(Balances.this)

  case class BalanceSerialized(address: String, coins: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Balance = Balance(address = address, coins = utilities.JSON.convertJsonStringToObject[Seq[Coin]](coins), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(balance: Balance): BalanceSerialized = BalanceSerialized(address = balance.address, coins = Json.toJson(balance.coins).toString, createdBy = balance.createdBy, createdOn = balance.createdOn, createdOnTimeZone = balance.createdOnTimeZone, updatedBy = balance.updatedBy, updatedOn = balance.updatedOn, updatedOnTimeZone = balance.updatedOnTimeZone)

  private def add(balance: Balance): Future[String] = db.run((balanceTable returning balanceTable.map(_.address) += serialize(balance)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.WALLET_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(balance: Balance): Future[Int] = db.run(balanceTable.insertOrUpdate(serialize(balance)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.WALLET_UPSERT_FAILED, psqlException)
    }
  }

  private def tryGetByAddress(address: String): Future[BalanceSerialized] = db.run(balanceTable.filter(_.address === address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.WALLET_NOT_FOUND, noSuchElementException)
    }
  }

  private def getByAddress(address: String): Future[Option[BalanceSerialized]] = db.run(balanceTable.filter(_.address === address).result.headOption)

  private def getTotalAccountNumber: Future[Int] = db.run(balanceTable.length.result)

  private def getListByAddress(addresses: Seq[String]): Future[Seq[BalanceSerialized]] = db.run(balanceTable.filter(_.address.inSet(addresses)).result)

  private[models] class BalanceTable(tag: Tag) extends Table[BalanceSerialized](tag, "Balance_BC") {

    def * = (address, coins, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (BalanceSerialized.tupled, BalanceSerialized.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def coins = column[String]("coins")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def createWithActor(address: String, coins: Seq[Coin]): Future[String] = (actorRegion ? Create(uniqueId, address, coins)).mapTo[String]

    def create(address: String, coins: Seq[Coin]): Future[String] = add(Balance(address = address, coins = coins))

    def tryGetWithActor(address: String): Future[Balance] = (actorRegion ? TryGet(uniqueId, address)).mapTo[Balance]

    def tryGet(address: String): Future[Balance] = tryGetByAddress(address).map(_.deserialize)

    def insertOrUpdateWithActor(balance: Balance): Future[Int] = (actorRegion ? InsertOrUpdate(uniqueId, balance)).mapTo[Int]

    def insertOrUpdate(balance: Balance): Future[Int] = upsert(balance)

    def getWithActor(address: String): Future[Option[Balance]] = (actorRegion ? Get(uniqueId, address)).mapTo[Option[Balance]]

    def get(address: String): Future[Option[Balance]] = getByAddress(address).map(_.map(_.deserialize))

    def getListWithActor(addresses: Seq[String]): Future[Seq[Balance]] = (actorRegion ? GetList(uniqueId, addresses)).mapTo[Seq[Balance]]

    def getList(addresses: Seq[String]): Future[Seq[Balance]] = getListByAddress(addresses).map(_.map(_.deserialize))

    def getTotalAccounts: Future[Int] = getTotalAccountNumber
  }

  object Utility {

    def onSendCoin(sendCoin: SendCoin)(implicit header: Header): Future[Unit] = {
      val fromAccount = insertOrUpdateBalance(sendCoin.fromAddress)

      def toAccount = insertOrUpdateBalance(sendCoin.toAddress)

      (for {
        _ <- fromAccount
        _ <- toAccount
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.SEND_COIN + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def onMultiSend(multiSend: MultiSend)(implicit header: Header): Future[Unit] = {
      val inputAccounts = utilitiesOperations.traverse(multiSend.inputs)(input => insertOrUpdateBalance(input.address))

      def outputAccounts = utilitiesOperations.traverse(multiSend.outputs)(output => insertOrUpdateBalance(output.address))

      (for {
        _ <- inputAccounts
        _ <- outputAccounts
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.MULTI_SEND + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def onRecvPacket(recvPacket: RecvPacket)(implicit header: Header): Future[Unit] = {
      val isSenderOnChain = recvPacket.packet.data.sender.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)
      val isReceiverOnChain = recvPacket.packet.data.receiver.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)
      val updateSender = if (isSenderOnChain) insertOrUpdateBalance(recvPacket.packet.data.sender) else Future()
      val updateReceiver = if (isReceiverOnChain) insertOrUpdateBalance(recvPacket.packet.data.receiver) else Future()

      (for {
        _ <- updateSender
        _ <- updateReceiver
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Response.IBC_BALANCE_UPDATE_FAILED.logMessage)
          logger.error(constants.Blockchain.TransactionMessage.RECV_PACKET + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def onTimeout(timeout: Timeout)(implicit header: Header): Future[Unit] = {
      val isSenderOnChain = timeout.packet.data.sender.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)
      val isReceiverOnChain = timeout.packet.data.receiver.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)
      val updateSender = if (isSenderOnChain) insertOrUpdateBalance(timeout.packet.data.sender) else Future()
      val updateReceiver = if (isReceiverOnChain) insertOrUpdateBalance(timeout.packet.data.receiver) else Future()

      (for {
        _ <- updateSender
        _ <- updateReceiver
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Response.IBC_BALANCE_UPDATE_FAILED.logMessage)
          logger.error(constants.Blockchain.TransactionMessage.TIMEOUT + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def onTimeoutOnClose(timeoutOnClose: TimeoutOnClose)(implicit header: Header): Future[Unit] = {
      val isSenderOnChain = timeoutOnClose.packet.data.sender.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)
      val isReceiverOnChain = timeoutOnClose.packet.data.receiver.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)
      val updateSender = if (isSenderOnChain) insertOrUpdateBalance(timeoutOnClose.packet.data.sender) else Future()
      val updateReceiver = if (isReceiverOnChain) insertOrUpdateBalance(timeoutOnClose.packet.data.receiver) else Future()

      (for {
        _ <- updateSender
        _ <- updateReceiver
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Response.IBC_BALANCE_UPDATE_FAILED.logMessage)
          logger.error(constants.Blockchain.TransactionMessage.TIMEOUT_ON_CLOSE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def onAcknowledgement(acknowledgement: Acknowledgement)(implicit header: Header): Future[Unit] = {
      val isSenderOnChain = acknowledgement.packet.data.sender.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)
      val isReceiverOnChain = acknowledgement.packet.data.receiver.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)
      val updateSender = if (isSenderOnChain) insertOrUpdateBalance(acknowledgement.packet.data.sender) else Future()
      val updateReceiver = if (isReceiverOnChain) insertOrUpdateBalance(acknowledgement.packet.data.receiver) else Future()

      (for {
        _ <- updateSender
        _ <- updateReceiver
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Response.IBC_BALANCE_UPDATE_FAILED.logMessage)
          logger.error(constants.Blockchain.TransactionMessage.ACKNOWLEDGEMENT + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def onIBCTransfer(transfer: Transfer)(implicit header: Header): Future[Unit] = {
      val isSenderOnChain = transfer.sender.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)
      val isReceiverOnChain = transfer.receiver.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)
      val updateSender = if (isSenderOnChain) insertOrUpdateBalance(transfer.sender) else Future()
      val updateReceiver = if (isReceiverOnChain) insertOrUpdateBalance(transfer.receiver) else Future()

      (for {
        _ <- updateSender
        _ <- updateReceiver
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Response.IBC_BALANCE_UPDATE_FAILED.logMessage)
          logger.error(constants.Blockchain.TransactionMessage.TRANSFER + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }

    }

    //WARNING: Can lead to wrong state, Example: A delegator withdraw rewards and then do send coin later
    //    def subtractCoinsFromAccount(fromAddress: String, subtractCoins: Seq[Coin]): Future[Unit] = {
    //      val fromBalance = Service.tryGet(fromAddress)
    //
    //      def updatedCoins(fromBalance: Balance) = fromBalance.coins.map(oldCoin => subtractCoins.find(_.denom == oldCoin.denom).fold(oldCoin)(subtractCoin => Coin(denom = oldCoin.denom, amount = oldCoin.amount - subtractCoin.amount)))
    //
    //      for {
    //        fromBalance <- fromBalance
    //        _ <- Service.insertOrUpdate(Balance(address = fromAddress, coins = updatedCoins(fromBalance)))
    //      } yield ()
    //    }

    //WARNING: Can lead to wrong state, Example: A delegator withdraw rewards and then do send coin later
    //    def addCoinsToAccount(toAddress: String, addCoins: Seq[Coin]): Future[Unit] = {
    //      val oldBalance = Service.get(toAddress)
    //
    //      def upsert(oldBalance: Option[Balance]) = oldBalance.fold(Service.insertOrUpdate(Balance(address = toAddress, coins = addCoins)))(old => {
    //        Service.insertOrUpdate(Balance(address = toAddress, coins = utilities.Blockchain.addCoins(old.coins, addCoins)))
    //      })
    //
    //      (for {
    //        oldBalance <- oldBalance
    //        _ <- upsert(oldBalance)
    //      } yield ()).recover {
    //        case baseException: BaseException => throw baseException
    //      }
    //    }

    def insertOrUpdateBalance(address: String): Future[Unit] = {
      val balanceResponse = getBalance.Service.get(address)

      def upsert(balanceResponse: BalanceResponse) = Service.insertOrUpdate(Balance(address = address, coins = balanceResponse.balances.map(_.toCoin)))

      (for {
        balanceResponse <- balanceResponse
        _ <- upsert(balanceResponse)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }
  }
}

object Balances {
  def props(blockchainBalances: models.blockchain.Balances) (implicit executionContext: ExecutionContext) = Props(new BalanceActor(blockchainBalances))

  @Singleton
  class BalanceActor @Inject()(
                                blockchainBalances: models.blockchain.Balances
                              ) (implicit executionContext: ExecutionContext) extends Actor with ActorLogging {

    override def receive: Receive = {
      case Get(_, address) => {
        blockchainBalances.Service.get(address) pipeTo sender()
      }
      case Create(_, address, coins) => {
        blockchainBalances.Service.create(address, coins) pipeTo sender()
      }
      case TryGet(_, address) => {
        blockchainBalances.Service.tryGet(address) pipeTo sender()
      }
      case InsertOrUpdate(_, balance) => {
        blockchainBalances.Service.insertOrUpdate(balance) pipeTo sender()
      }
      case GetList(_, addresses) => {
        blockchainBalances.Service.getList(addresses) pipeTo sender()
      }
    }
  }
  case class Get(id: String, address: String)
  case class TryGet(id: String, address: String)
  case class Create(id: String, address: String, coins: Seq[Coin])
  case class InsertOrUpdate(id: String, balance: Balance)
  case class GetList(id: String, addresses: Seq[String])
}



