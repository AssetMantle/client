package models.masterTransaction

import com.assets.{transactions => assetsTransactions}
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
import com.ibc.applications.transfer.{v1 => transferTx}
import com.ibc.core.channel.{v1 => channelTx}
import com.ibc.core.client.{v1 => clientTx}
import com.ibc.core.connection.{v1 => connectionTx}
import com.identities.{transactions => identitiesTransactions}
import com.metas.{transactions => metasTransactions}
import com.orders.{transactions => ordersTransactions}
import com.splits.{transactions => splitsTransactions}
import exceptions.BaseException
import models.Trait.Logging
import org.postgresql.util.PSQLException
import org.slf4j.{Logger, LoggerFactory}
import play.api.Configuration
import play.api.db.slick.DatabaseConfigProvider
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
        val txAddresses: Seq[Seq[String]] = tx.getMessages.map { stdMsg =>
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
              Seq(msg.getSigner, com.ibc.applications.transfer.v2.FungibleTokenPacketData.parseFrom(msg.getPacket.getData.toByteArray).getReceiver)
            case constants.Blockchain.TransactionMessage.TIMEOUT => Seq(channelTx.MsgTimeout.parseFrom(stdMsg.getValue).getSigner)
            case constants.Blockchain.TransactionMessage.TIMEOUT_ON_CLOSE => Seq(channelTx.MsgTimeoutOnClose.parseFrom(stdMsg.getValue).getSigner)
            case constants.Blockchain.TransactionMessage.ACKNOWLEDGEMENT => Seq(channelTx.MsgAcknowledgement.parseFrom(stdMsg.getValue).getSigner)
            //ibc-transfer
            case constants.Blockchain.TransactionMessage.TRANSFER => Seq(transferTx.MsgTransfer.parseFrom(stdMsg.getValue).getSender)
            //assets
            case constants.Blockchain.TransactionMessage.ASSET_BURN => Seq(assetsTransactions.burn.Message.parseFrom(stdMsg.getValue).getFrom)
            case constants.Blockchain.TransactionMessage.ASSET_DEFINE => Seq(assetsTransactions.define.Message.parseFrom(stdMsg.getValue).getFrom)
            case constants.Blockchain.TransactionMessage.ASSET_DEPUTIZE => Seq(assetsTransactions.deputize.Message.parseFrom(stdMsg.getValue).getFrom)
            case constants.Blockchain.TransactionMessage.ASSET_MINT => Seq(assetsTransactions.mint.Message.parseFrom(stdMsg.getValue).getFrom)
            case constants.Blockchain.TransactionMessage.ASSET_MUTATE => Seq(assetsTransactions.mutate.Message.parseFrom(stdMsg.getValue).getFrom)
            case constants.Blockchain.TransactionMessage.ASSET_RENUMERATE => Seq(assetsTransactions.renumerate.Message.parseFrom(stdMsg.getValue).getFrom)
            case constants.Blockchain.TransactionMessage.ASSET_REVOKE => Seq(assetsTransactions.revoke.Message.parseFrom(stdMsg.getValue).getFrom)
            //identities
            case constants.Blockchain.TransactionMessage.IDENTITY_DEFINE => Seq(identitiesTransactions.define.Message.parseFrom(stdMsg.getValue).getFrom)
            case constants.Blockchain.TransactionMessage.IDENTITY_DEPUTIZE => Seq(identitiesTransactions.deputize.Message.parseFrom(stdMsg.getValue).getFrom)
            case constants.Blockchain.TransactionMessage.IDENTITY_ISSUE => val msg = identitiesTransactions.issue.Message.parseFrom(stdMsg.getValue)
              Seq(msg.getFrom, msg.getTo)
            case constants.Blockchain.TransactionMessage.IDENTITY_MUTATE => Seq(identitiesTransactions.mutate.Message.parseFrom(stdMsg.getValue).getFrom)
            case constants.Blockchain.TransactionMessage.IDENTITY_NUB => Seq(identitiesTransactions.nub.Message.parseFrom(stdMsg.getValue).getFrom)
            case constants.Blockchain.TransactionMessage.IDENTITY_PROVISION => val msg = identitiesTransactions.provision.Message.parseFrom(stdMsg.getValue)
              Seq(msg.getFrom, msg.getTo)
            case constants.Blockchain.TransactionMessage.IDENTITY_QUASH => Seq(identitiesTransactions.quash.Message.parseFrom(stdMsg.getValue).getFrom)
            case constants.Blockchain.TransactionMessage.IDENTITY_REVOKE => Seq(identitiesTransactions.revoke.Message.parseFrom(stdMsg.getValue).getFrom)
            case constants.Blockchain.TransactionMessage.IDENTITY_UNPROVISION => val msg = identitiesTransactions.unprovision.Message.parseFrom(stdMsg.getValue)
              Seq(msg.getFrom, msg.getTo)
            //orders
            case constants.Blockchain.TransactionMessage.ORDER_CANCEL => Seq(ordersTransactions.cancel.Message.parseFrom(stdMsg.getValue).getFrom)
            case constants.Blockchain.TransactionMessage.ORDER_DEFINE => Seq(ordersTransactions.define.Message.parseFrom(stdMsg.getValue).getFrom)
            case constants.Blockchain.TransactionMessage.ORDER_DEPUTIZE => Seq(ordersTransactions.deputize.Message.parseFrom(stdMsg.getValue).getFrom)
            case constants.Blockchain.TransactionMessage.ORDER_IMMEDIATE => Seq(ordersTransactions.immediate.Message.parseFrom(stdMsg.getValue).getFrom)
            case constants.Blockchain.TransactionMessage.ORDER_MAKE => Seq(ordersTransactions.make.Message.parseFrom(stdMsg.getValue).getFrom)
            case constants.Blockchain.TransactionMessage.ORDER_MODIFY => Seq(ordersTransactions.modify.Message.parseFrom(stdMsg.getValue).getFrom)
            case constants.Blockchain.TransactionMessage.ORDER_REVOKE => Seq(ordersTransactions.revoke.Message.parseFrom(stdMsg.getValue).getFrom)
            case constants.Blockchain.TransactionMessage.ORDER_TAKE => Seq(ordersTransactions.take.Message.parseFrom(stdMsg.getValue).getFrom)
            //metas
            case constants.Blockchain.TransactionMessage.META_REVEAL => Seq(metasTransactions.reveal.Message.parseFrom(stdMsg.getValue).getFrom)
            // splits
            case constants.Blockchain.TransactionMessage.SPLIT_SEND => Seq(splitsTransactions.send.Message.parseFrom(stdMsg.getValue).getFrom)
            case constants.Blockchain.TransactionMessage.SPLIT_WRAP => Seq(splitsTransactions.wrap.Message.parseFrom(stdMsg.getValue).getFrom)
            case constants.Blockchain.TransactionMessage.SPLIT_UNWRAP => Seq(splitsTransactions.unwrap.Message.parseFrom(stdMsg.getValue).getFrom)
            case _ => Seq()
          }
        }
        txAddresses.flatten.distinct.map(x => WalletTransaction(address = x, txHash = tx.hash, height = height))
      }

      Service.add(walletTransactions.flatten)
    }

  }
}