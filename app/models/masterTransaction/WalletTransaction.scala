package models.masterTransaction

import com.cosmos.authz.{v1beta1 => authzTx}
import com.cosmos.bank.{v1beta1 => bankTx}
import com.cosmos.crisis.{v1beta1 => crisisTx}
import com.cosmos.distribution.{v1beta1 => distributionTx}
import com.cosmos.evidence.{v1beta1 => evidenceTx}
import com.cosmos.feegrant.{v1beta1 => feegrantTx}
import com.cosmos.gov.{v1beta1 => govTx}
import com.cosmos.slashing.{v1beta1 => slashingTx}
import com.cosmos.staking.{v1beta1 => stakingTx}
import com.cosmos.vesting.{v1beta1 => VestingTx}
import exceptions.BaseException
import ibc.applications.transfer.v1.{Tx => transferTx}
import ibc.core.channel.v1.{Tx => channelTx}
import ibc.core.client.v1.{Tx => clientTx}
import ibc.core.connection.v1.{Tx => connectionTx}
import models.Trait.Logging
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.Configuration
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.{Failure, Success}

case class WalletTransaction(address: String, txHash: String, height: Int, createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging

@Singleton
class WalletTransactions @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_WALLET_TRANSACTION

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  import databaseConfig.profile.api._

  private val transactionsPerPage = configuration.get[Int]("blockchain.transactions.perPage")

  private[models] val walletTransactionTable = TableQuery[WalletTransactionTable]

  private def create(walletTransaction: WalletTransaction): Future[String] = db.run((walletTransactionTable returning walletTransactionTable.map(_.txHash) += walletTransaction).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def create(walletTransactions: Seq[WalletTransaction]) = db.run((walletTransactionTable ++= walletTransactions).asTry).map {
    case Success(result) => ()
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findWalletTransactions(address: String, offset: Int, limit: Int): Future[Seq[WalletTransaction]] = db.run(walletTransactionTable.filter(_.address === address).sortBy(_.height.desc).drop(offset).take(limit).result)

  private[models] class WalletTransactionTable(tag: Tag) extends Table[WalletTransaction](tag, "WalletTransaction") {

    def * = (address, txHash, height, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (WalletTransaction.tupled, WalletTransaction.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def txHash = column[String]("txHash", O.PrimaryKey)

    def height = column[Int]("height")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")

  }

  object Service {

    def add(walletTransactions: Seq[WalletTransaction]): Future[Unit] = create(walletTransactions)

    def getTransactions(address: String, pageNumber: Int): Future[Seq[WalletTransaction]] = findWalletTransactions(address = address, offset = (pageNumber - 1) * transactionsPerPage, limit = transactionsPerPage)

  }

  object Utility {

    def addForTransactions(txs: Seq[models.blockchain.Transaction], height: Int): Future[Unit] = {
      val walletTransactions = txs.map { tx =>
        val txAddresses = tx.getMessages.map { stdMsg =>
          stdMsg.getTypeUrl match {
            //auth
            case constants.Blockchain.TransactionMessage.CREATE_VESTING_ACCOUNT => val msg = VestingTx.MsgCreateVestingAccount.parseFrom(stdMsg.getValue)
              Seq(msg.getFromAddress, msg.getToAddress)
            //authz
            case constants.Blockchain.TransactionMessage.GRANT_AUTHORIZATION => val msg = authzTx.MsgGrant.parseFrom(stdMsg.getValue)
              Seq(msg.getGranter, msg.getGrantee)
            case constants.Blockchain.TransactionMessage.REVOKE_AUTHORIZATION => val msg = authzTx.MsgRevoke.parseFrom(stdMsg.getValue)
              Seq(msg.getGranter, msg.getGrantee)
            case constants.Blockchain.TransactionMessage.EXECUTE_AUTHORIZATION => Seq(authzTx.MsgExec.parseFrom(stdMsg.getValue).getGrantee)
            //bank
            case constants.Blockchain.TransactionMessage.SEND_COIN => val msg = bankTx.MsgSend.parseFrom(stdMsg.getValue)
              Seq(msg.getFromAddress, msg.getToAddress)
            case constants.Blockchain.TransactionMessage.MULTI_SEND => val msg = bankTx.MsgMultiSend.parseFrom(stdMsg.getValue)
              (msg.getInputsList.asScala.toSeq.map(_.getAddress) ++ msg.getOutputsList.asScala.toSeq.map(_.getAddress)).distinct
            //crisis
            case constants.Blockchain.TransactionMessage.VERIFY_INVARIANT => Seq(crisisTx.MsgVerifyInvariant.parseFrom(stdMsg.getValue).getSender)
            //distribution
            case constants.Blockchain.TransactionMessage.SET_WITHDRAW_ADDRESS => val msg = distributionTx.MsgSetWithdrawAddress.parseFrom(stdMsg.getValue)
              Seq(msg.getWithdrawAddress, msg.getDelegatorAddress)
            case constants.Blockchain.TransactionMessage.WITHDRAW_DELEGATOR_REWARD => Seq(distributionTx.MsgWithdrawDelegatorReward.parseFrom(stdMsg.getValue).getDelegatorAddress)
            case constants.Blockchain.TransactionMessage.WITHDRAW_VALIDATOR_COMMISSION => Seq(distributionTx.MsgWithdrawValidatorCommission.parseFrom(stdMsg.getValue).getValidatorAddress)
            case constants.Blockchain.TransactionMessage.FUND_COMMUNITY_POOL => Seq(distributionTx.MsgFundCommunityPool.parseFrom(stdMsg.getValue).getDepositor)
            //evidence
            case constants.Blockchain.TransactionMessage.SUBMIT_EVIDENCE => Seq(evidenceTx.MsgSubmitEvidence.parseFrom(stdMsg.getValue).getSubmitter)
            //feeGrant
            case constants.Blockchain.TransactionMessage.FEE_GRANT_ALLOWANCE => val msg = feegrantTx.MsgGrantAllowance.parseFrom(stdMsg.getValue)
              Seq(msg.getGranter, msg.getGranter)
            case constants.Blockchain.TransactionMessage.FEE_REVOKE_ALLOWANCE => val msg = feegrantTx.MsgRevokeAllowance.parseFrom(stdMsg.getValue)
              Seq(msg.getGranter, msg.getGranter)
            //gov
            case constants.Blockchain.TransactionMessage.DEPOSIT => Seq(govTx.MsgDeposit.parseFrom(stdMsg.getValue).getDepositor)
            case constants.Blockchain.TransactionMessage.SUBMIT_PROPOSAL => Seq(govTx.MsgSubmitProposal.parseFrom(stdMsg.getValue).getProposer)
            case constants.Blockchain.TransactionMessage.VOTE => Seq(govTx.MsgVote.parseFrom(stdMsg.getValue).getVoter)
            case constants.Blockchain.TransactionMessage.WEIGHTED_VOTE => Seq(govTx.MsgVoteWeighted.parseFrom(stdMsg.getValue).getVoter)
            //slashing
            case constants.Blockchain.TransactionMessage.UNJAIL => Seq(slashingTx.MsgUnjail.parseFrom(stdMsg.getValue).getValidatorAddr)
            //staking
            case constants.Blockchain.TransactionMessage.DELEGATE => Seq(stakingTx.MsgDelegate.parseFrom(stdMsg.getValue).getDelegatorAddress)
            case constants.Blockchain.TransactionMessage.REDELEGATE => Seq(stakingTx.MsgBeginRedelegate.parseFrom(stdMsg.getValue).getDelegatorAddress)
            case constants.Blockchain.TransactionMessage.UNDELEGATE => Seq(stakingTx.MsgUndelegate.parseFrom(stdMsg.getValue).getDelegatorAddress)
            //ibc-client
            case constants.Blockchain.TransactionMessage.CREATE_CLIENT => Seq(clientTx.MsgCreateClient.parseFrom(stdMsg.getValue).getSigner)
            case constants.Blockchain.TransactionMessage.UPDATE_CLIENT => Seq(clientTx.MsgUpdateClient.parseFrom(stdMsg.getValue).getSigner)
            case constants.Blockchain.TransactionMessage.SUBMIT_MISBEHAVIOUR => Seq(clientTx.MsgSubmitMisbehaviour.parseFrom(stdMsg.getValue).getSigner)
            case constants.Blockchain.TransactionMessage.UPGRADE_CLIENT => Seq(clientTx.MsgUpgradeClient.parseFrom(stdMsg.getValue).getSigner)
            //ibc-connection
            case constants.Blockchain.TransactionMessage.CONNECTION_OPEN_INIT => Seq(connectionTx.MsgConnectionOpenInit.parseFrom(stdMsg.getValue).getSigner)
            case constants.Blockchain.TransactionMessage.CONNECTION_OPEN_CONFIRM => Seq(connectionTx.MsgConnectionOpenConfirm.parseFrom(stdMsg.getValue).getSigner)
            case constants.Blockchain.TransactionMessage.CONNECTION_OPEN_ACK => Seq(connectionTx.MsgConnectionOpenAck.parseFrom(stdMsg.getValue).getSigner)
            case constants.Blockchain.TransactionMessage.CONNECTION_OPEN_TRY => Seq(connectionTx.MsgConnectionOpenTry.parseFrom(stdMsg.getValue).getSigner)
            //ibc-channel
            case constants.Blockchain.TransactionMessage.CHANNEL_OPEN_INIT => Seq(channelTx.MsgChannelOpenInit.parseFrom(stdMsg.getValue).getSigner)
            case constants.Blockchain.TransactionMessage.CHANNEL_OPEN_TRY => Seq(channelTx.MsgChannelOpenTry.parseFrom(stdMsg.getValue).getSigner)
            case constants.Blockchain.TransactionMessage.CHANNEL_OPEN_ACK => Seq(channelTx.MsgChannelOpenAck.parseFrom(stdMsg.getValue).getSigner)
            case constants.Blockchain.TransactionMessage.CHANNEL_OPEN_CONFIRM => Seq(channelTx.MsgChannelOpenConfirm.parseFrom(stdMsg.getValue).getSigner)
            case constants.Blockchain.TransactionMessage.CHANNEL_CLOSE_INIT => Seq(channelTx.MsgChannelCloseInit.parseFrom(stdMsg.getValue).getSigner)
            case constants.Blockchain.TransactionMessage.CHANNEL_CLOSE_CONFIRM => Seq(channelTx.MsgChannelCloseConfirm.parseFrom(stdMsg.getValue).getSigner)
            case constants.Blockchain.TransactionMessage.RECV_PACKET => val msg = channelTx.MsgRecvPacket.parseFrom(stdMsg.getValue)
              Seq(msg.getSigner, ibc.applications.transfer.v2.Packet.FungibleTokenPacketData.parseFrom(msg.getPacket.getData.toByteArray).getReceiver)
            case constants.Blockchain.TransactionMessage.TIMEOUT => Seq(channelTx.MsgTimeout.parseFrom(stdMsg.getValue).getSigner)
            case constants.Blockchain.TransactionMessage.TIMEOUT_ON_CLOSE => Seq(channelTx.MsgTimeoutOnClose.parseFrom(stdMsg.getValue).getSigner)
            case constants.Blockchain.TransactionMessage.ACKNOWLEDGEMENT => Seq(channelTx.MsgAcknowledgement.parseFrom(stdMsg.getValue).getSigner)
            //ibc-transfer
            case constants.Blockchain.TransactionMessage.TRANSFER => Seq(transferTx.MsgTransfer.parseFrom(stdMsg.getValue).getSender)
            case _ => Seq()
          }
        }
        txAddresses.flatten.distinct.map(x => WalletTransaction(address = x, txHash = tx.hash, height = height))
      }

      Service.add(walletTransactions.flatten)
    }

  }
}