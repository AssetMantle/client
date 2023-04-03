package models.blockchain

import com.cosmos.bank.{v1beta1 => bankTx}
import com.ibc.applications.transfer.v2.FungibleTokenPacketData
import com.ibc.applications.transfer.{v1 => transferTx}
import com.ibc.core.channel.{v1 => channelTx}
import exceptions.BaseException
import models.common.Serializable.Coin
import models.traits.Logging
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.blockchain.GetBalance
import queries.responses.blockchain.BalanceResponse.{Response => BalanceResponse}
import queries.responses.common.Header
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.{Failure, Success}

case class Balance(address: String, coins: Seq[Coin], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging

@Singleton
class Balances @Inject()(
                          protected val databaseConfigProvider: DatabaseConfigProvider,
                          getBalance: GetBalance,
                          configuration: Configuration,
                          utilitiesOperations: utilities.Operations
                        )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_BALANCE

  import databaseConfig.profile.api._

  private[models] val balanceTable = TableQuery[BalanceTable]

  case class BalanceSerialized(address: String, coins: String, createdBy: Option[String], createdOnMillisEpoch: Option[Long], updatedBy: Option[String], updatedOnMillisEpoch: Option[Long]) {
    def deserialize: Balance = Balance(address = address, coins = utilities.JSON.convertJsonStringToObject[Seq[Coin]](coins), createdBy = createdBy, createdOnMillisEpoch = createdOnMillisEpoch, updatedBy = updatedBy, updatedOnMillisEpoch = updatedOnMillisEpoch)
  }

  def serialize(balance: Balance): BalanceSerialized = BalanceSerialized(address = balance.address, coins = Json.toJson(balance.coins).toString, createdBy = balance.createdBy, createdOnMillisEpoch = balance.createdOnMillisEpoch, updatedBy = balance.updatedBy, updatedOnMillisEpoch = balance.updatedOnMillisEpoch)

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

  private def fetchAllAddresses: Future[Seq[String]] = db.run(balanceTable.map(_.address).result)

  private def getListByAddress(addresses: Seq[String]): Future[Seq[BalanceSerialized]] = db.run(balanceTable.filter(_.address.inSet(addresses)).result)

  private[models] class BalanceTable(tag: Tag) extends Table[BalanceSerialized](tag, "Balance") {

    def * = (address, coins, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (BalanceSerialized.tupled, BalanceSerialized.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def coins = column[String]("coins")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")
  }

  object Service {

    def create(address: String, coins: Seq[Coin]): Future[String] = add(Balance(address = address, coins = coins))

    def tryGet(address: String): Future[Balance] = tryGetByAddress(address).map(_.deserialize)

    def insertOrUpdate(balance: Balance): Future[Int] = upsert(balance)

    def get(address: String): Future[Option[Balance]] = getByAddress(address).map(_.map(_.deserialize))

    def getList(addresses: Seq[String]): Future[Seq[Balance]] = getListByAddress(addresses).map(_.map(_.deserialize))

    def getTotalAccounts: Future[Int] = getTotalAccountNumber

    def getAllAddressess: Future[Seq[String]] = fetchAllAddresses
  }

  object Utility {

    def onSendCoin(sendCoin: bankTx.MsgSend)(implicit header: Header): Future[String] = {
      val fromAccount = insertOrUpdateBalance(sendCoin.getFromAddress)

      def toAccount = insertOrUpdateBalance(sendCoin.getToAddress)

      (for {
        _ <- fromAccount
        _ <- toAccount
      } yield sendCoin.getFromAddress).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.SEND_COIN + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          sendCoin.getFromAddress
      }
    }

    def onMultiSend(multiSend: bankTx.MsgMultiSend)(implicit header: Header): Future[String] = {
      val inputAccounts = utilitiesOperations.traverse(multiSend.getInputsList.asScala.toSeq)(input => insertOrUpdateBalance(input.getAddress))

      def outputAccounts = utilitiesOperations.traverse(multiSend.getOutputsList.asScala.toSeq)(output => insertOrUpdateBalance(output.getAddress))

      (for {
        _ <- inputAccounts
        _ <- outputAccounts
      } yield multiSend.getInputs(0).getAddress).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.MULTI_SEND + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          multiSend.getInputs(0).getAddress
      }
    }

    def onRecvPacket(recvPacket: channelTx.MsgRecvPacket)(implicit header: Header): Future[String] = {
      val isSenderOnChain = FungibleTokenPacketData.parseFrom(recvPacket.getPacket.getData).getSender.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)
      val isReceiverOnChain = FungibleTokenPacketData.parseFrom(recvPacket.getPacket.getData).getReceiver.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)
      val updateSender = if (isSenderOnChain) insertOrUpdateBalance(FungibleTokenPacketData.parseFrom(recvPacket.getPacket.getData).getSender) else Future()
      val updateReceiver = if (isReceiverOnChain) insertOrUpdateBalance(FungibleTokenPacketData.parseFrom(recvPacket.getPacket.getData).getReceiver) else Future()

      (for {
        _ <- updateSender
        _ <- updateReceiver
      } yield recvPacket.getSigner).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.RECV_PACKET + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          recvPacket.getSigner
      }
    }

    def onTimeout(timeout: channelTx.MsgTimeout)(implicit header: Header): Future[String] = {
      val isSenderOnChain = FungibleTokenPacketData.parseFrom(timeout.getPacket.getData).getSender.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)
      val isReceiverOnChain = FungibleTokenPacketData.parseFrom(timeout.getPacket.getData).getReceiver.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)
      val updateSender = if (isSenderOnChain) insertOrUpdateBalance(FungibleTokenPacketData.parseFrom(timeout.getPacket.getData).getSender) else Future()
      val updateReceiver = if (isReceiverOnChain) insertOrUpdateBalance(FungibleTokenPacketData.parseFrom(timeout.getPacket.getData).getReceiver) else Future()

      (for {
        _ <- updateSender
        _ <- updateReceiver
      } yield timeout.getSigner).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.TIMEOUT + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          timeout.getSigner
      }
    }

    def onTimeoutOnClose(timeoutOnClose: channelTx.MsgTimeoutOnClose)(implicit header: Header): Future[String] = {
      val isSenderOnChain = FungibleTokenPacketData.parseFrom(timeoutOnClose.getPacket.getData).getSender.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)
      val isReceiverOnChain = FungibleTokenPacketData.parseFrom(timeoutOnClose.getPacket.getData).getReceiver.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)
      val updateSender = if (isSenderOnChain) insertOrUpdateBalance(FungibleTokenPacketData.parseFrom(timeoutOnClose.getPacket.getData).getSender) else Future()
      val updateReceiver = if (isReceiverOnChain) insertOrUpdateBalance(FungibleTokenPacketData.parseFrom(timeoutOnClose.getPacket.getData).getReceiver) else Future()

      (for {
        _ <- updateSender
        _ <- updateReceiver
      } yield timeoutOnClose.getSigner).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.TIMEOUT_ON_CLOSE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          timeoutOnClose.getSigner
      }
    }

    def onAcknowledgement(acknowledgement: channelTx.MsgAcknowledgement)(implicit header: Header): Future[String] = {
      val isSenderOnChain = FungibleTokenPacketData.parseFrom(acknowledgement.getPacket.getData).getSender.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)
      val isReceiverOnChain = FungibleTokenPacketData.parseFrom(acknowledgement.getPacket.getData).getReceiver.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)
      val updateSender = if (isSenderOnChain) insertOrUpdateBalance(FungibleTokenPacketData.parseFrom(acknowledgement.getPacket.getData).getSender) else Future()
      val updateReceiver = if (isReceiverOnChain) insertOrUpdateBalance(FungibleTokenPacketData.parseFrom(acknowledgement.getPacket.getData).getReceiver) else Future()

      (for {
        _ <- updateSender
        _ <- updateReceiver
      } yield acknowledgement.getSigner).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.ACKNOWLEDGEMENT + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          acknowledgement.getSigner
      }
    }

    def onIBCTransfer(transfer: transferTx.MsgTransfer)(implicit header: Header): Future[String] = {
      val isSenderOnChain = transfer.getSender.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)
      val isReceiverOnChain = transfer.getReceiver.matches(constants.Blockchain.AccountPrefix + constants.RegularExpression.ADDRESS_SUFFIX.regex)
      val updateSender = if (isSenderOnChain) insertOrUpdateBalance(transfer.getSender) else Future()
      val updateReceiver = if (isReceiverOnChain) insertOrUpdateBalance(transfer.getReceiver) else Future()

      (for {
        _ <- updateSender
        _ <- updateReceiver
      } yield transfer.getSender).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.TRANSFER + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          transfer.getSender
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