package models.masterTransaction

import com.assetmantle.modules.assets.{transactions => assetsTransactions}
import com.assetmantle.modules.identities.{transactions => identitiesTransactions}
import com.assetmantle.modules.metas.{transactions => metasTransactions}
import com.assetmantle.modules.orders.{transactions => ordersTransactions}
import com.assetmantle.modules.splits.{transactions => splitsTransactions}
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
import exceptions.BaseException
import models.archive
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.{Failure, Success}

case class WalletTransaction(address: String, txHash: String, height: Int)

@Singleton
class WalletTransactions @Inject()(
                                    protected val databaseConfigProvider: DatabaseConfigProvider,
                                    configuration: Configuration,
                                    archiveWalletTransactions: archive.WalletTransactions,
                                  )(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_WALLET_TRANSACTION

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

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

  private def countWalletTransactions(address: String): Future[Int] = db.run(walletTransactionTable.filter(_.address === address).length.result)

  private def getByHeightRange(start: Int, end: Int): Future[Seq[WalletTransaction]] = db.run(walletTransactionTable.filter(x => x.height >= start && x.height <= end).result)

  private def deleteByHeightRange(start: Int, end: Int): Future[Int] = db.run(walletTransactionTable.filter(x => x.height >= start && x.height <= end).delete)

  private[models] class WalletTransactionTable(tag: Tag) extends Table[WalletTransaction](tag, Option("master_transaction"), "WalletTransaction") {

    def * = (address, txHash, height) <> (WalletTransaction.tupled, WalletTransaction.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def txHash = column[String]("txHash", O.PrimaryKey)

    def height = column[Int]("height")

  }

  object Service {

    def add(walletTransactions: Seq[WalletTransaction]): Future[Unit] = create(walletTransactions)

    def getTransactions(address: String, pageNumber: Int): Future[Seq[WalletTransaction]] = {
      val totalWant = pageNumber * transactionsPerPage
      val count = countWalletTransactions(address)
      val txs = findWalletTransactions(address = address, offset = (pageNumber - 1) * transactionsPerPage, limit = transactionsPerPage)

      def getWalletTxs(count: Int) = if (totalWant <= count) Future(Seq())
      else {
        val (offset, limit) = if (totalWant - count < transactionsPerPage) {
          (0, totalWant - count)
        } else {
          val drop = (transactionsPerPage * (count / transactionsPerPage)) + ((totalWant - count) % transactionsPerPage)
          (drop, transactionsPerPage)
        }
        archiveWalletTransactions.Service.getTransactions(address = address, offset = offset, limit = limit)
      }

      for {
        count <- count
        txs <- txs
        archiveWalletTxs <- getWalletTxs(count)
      } yield txs ++ archiveWalletTxs.map(_.toWalletTx)
    }

    def getByHeight(start: Int, end: Int): Future[Seq[WalletTransaction]] = getByHeightRange(start = start, end = end)

    def deleteByHeight(start: Int, end: Int): Future[Int] = deleteByHeightRange(start = start, end = end)

  }

  object Utility {

    def addForTransactions(txs: Seq[models.blockchain.Transaction], height: Int): Future[Unit] = {
      val walletTransactions = txs.map { tx =>
        val txAddresses: Seq[Seq[String]] = tx.getMessages.map { stdMsg =>
          stdMsg.getTypeUrl match {
            //auth
            case schema.constants.Messages.CREATE_VESTING_ACCOUNT => val msg = VestingTx.MsgCreateVestingAccount.parseFrom(stdMsg.getValue)
              Seq(msg.getFromAddress, msg.getToAddress).distinct
            //authz
            case schema.constants.Messages.GRANT_AUTHORIZATION => val msg = authzTx.MsgGrant.parseFrom(stdMsg.getValue)
              Seq(msg.getGranter, msg.getGrantee).distinct
            case schema.constants.Messages.REVOKE_AUTHORIZATION => val msg = authzTx.MsgRevoke.parseFrom(stdMsg.getValue)
              Seq(msg.getGranter, msg.getGrantee).distinct
            case schema.constants.Messages.EXECUTE_AUTHORIZATION => Seq(authzTx.MsgExec.parseFrom(stdMsg.getValue).getGrantee)
            //bank
            case schema.constants.Messages.SEND_COIN => val msg = bankTx.MsgSend.parseFrom(stdMsg.getValue)
              Seq(msg.getFromAddress, msg.getToAddress).distinct
            case schema.constants.Messages.MULTI_SEND => val msg = bankTx.MsgMultiSend.parseFrom(stdMsg.getValue)
              (msg.getInputsList.asScala.toSeq.map(_.getAddress) ++ msg.getOutputsList.asScala.toSeq.map(_.getAddress)).distinct
            //crisis
            case schema.constants.Messages.VERIFY_INVARIANT => Seq(crisisTx.MsgVerifyInvariant.parseFrom(stdMsg.getValue).getSender)
            //distribution
            case schema.constants.Messages.SET_WITHDRAW_ADDRESS => val msg = distributionTx.MsgSetWithdrawAddress.parseFrom(stdMsg.getValue)
              Seq(msg.getWithdrawAddress, msg.getDelegatorAddress).distinct
            case schema.constants.Messages.WITHDRAW_DELEGATOR_REWARD => Seq(distributionTx.MsgWithdrawDelegatorReward.parseFrom(stdMsg.getValue).getDelegatorAddress)
            case schema.constants.Messages.WITHDRAW_VALIDATOR_COMMISSION => Seq(utilities.Crypto.convertOperatorAddressToAccountAddress(distributionTx.MsgWithdrawValidatorCommission.parseFrom(stdMsg.getValue).getValidatorAddress))
            case schema.constants.Messages.FUND_COMMUNITY_POOL => Seq(distributionTx.MsgFundCommunityPool.parseFrom(stdMsg.getValue).getDepositor)
            //evidence
            case schema.constants.Messages.SUBMIT_EVIDENCE => Seq(evidenceTx.MsgSubmitEvidence.parseFrom(stdMsg.getValue).getSubmitter)
            //feeGrant
            case schema.constants.Messages.FEE_GRANT_ALLOWANCE => val msg = feegrantTx.MsgGrantAllowance.parseFrom(stdMsg.getValue)
              Seq(msg.getGranter, msg.getGranter).distinct
            case schema.constants.Messages.FEE_REVOKE_ALLOWANCE => val msg = feegrantTx.MsgRevokeAllowance.parseFrom(stdMsg.getValue)
              Seq(msg.getGranter, msg.getGranter).distinct
            //gov
            case schema.constants.Messages.DEPOSIT => Seq(govTx.MsgDeposit.parseFrom(stdMsg.getValue).getDepositor)
            case schema.constants.Messages.SUBMIT_PROPOSAL => Seq(govTx.MsgSubmitProposal.parseFrom(stdMsg.getValue).getProposer)
            case schema.constants.Messages.VOTE => Seq(govTx.MsgVote.parseFrom(stdMsg.getValue).getVoter)
            case schema.constants.Messages.WEIGHTED_VOTE => Seq(govTx.MsgVoteWeighted.parseFrom(stdMsg.getValue).getVoter)
            //slashing
            case schema.constants.Messages.UNJAIL => Seq(utilities.Crypto.convertOperatorAddressToAccountAddress(slashingTx.MsgUnjail.parseFrom(stdMsg.getValue).getValidatorAddr))
            //staking
            case schema.constants.Messages.CREATE_VALIDATOR => Seq(stakingTx.MsgCreateValidator.parseFrom(stdMsg.getValue).getDelegatorAddress)
            case schema.constants.Messages.EDIT_VALIDATOR => Seq(utilities.Crypto.convertOperatorAddressToAccountAddress(stakingTx.MsgEditValidator.parseFrom(stdMsg.getValue).getValidatorAddress))
            case schema.constants.Messages.DELEGATE => Seq(stakingTx.MsgDelegate.parseFrom(stdMsg.getValue).getDelegatorAddress)
            case schema.constants.Messages.REDELEGATE => Seq(stakingTx.MsgBeginRedelegate.parseFrom(stdMsg.getValue).getDelegatorAddress)
            case schema.constants.Messages.UNDELEGATE => Seq(stakingTx.MsgUndelegate.parseFrom(stdMsg.getValue).getDelegatorAddress)
            //ibc-client
            case schema.constants.Messages.CREATE_CLIENT => Seq(clientTx.MsgCreateClient.parseFrom(stdMsg.getValue).getSigner)
            case schema.constants.Messages.UPDATE_CLIENT => Seq(clientTx.MsgUpdateClient.parseFrom(stdMsg.getValue).getSigner)
            case schema.constants.Messages.SUBMIT_MISBEHAVIOUR => Seq(clientTx.MsgSubmitMisbehaviour.parseFrom(stdMsg.getValue).getSigner)
            case schema.constants.Messages.UPGRADE_CLIENT => Seq(clientTx.MsgUpgradeClient.parseFrom(stdMsg.getValue).getSigner)
            //ibc-connection
            case schema.constants.Messages.CONNECTION_OPEN_INIT => Seq(connectionTx.MsgConnectionOpenInit.parseFrom(stdMsg.getValue).getSigner)
            case schema.constants.Messages.CONNECTION_OPEN_CONFIRM => Seq(connectionTx.MsgConnectionOpenConfirm.parseFrom(stdMsg.getValue).getSigner)
            case schema.constants.Messages.CONNECTION_OPEN_ACK => Seq(connectionTx.MsgConnectionOpenAck.parseFrom(stdMsg.getValue).getSigner)
            case schema.constants.Messages.CONNECTION_OPEN_TRY => Seq(connectionTx.MsgConnectionOpenTry.parseFrom(stdMsg.getValue).getSigner)
            //ibc-channel
            case schema.constants.Messages.CHANNEL_OPEN_INIT => Seq(channelTx.MsgChannelOpenInit.parseFrom(stdMsg.getValue).getSigner)
            case schema.constants.Messages.CHANNEL_OPEN_TRY => Seq(channelTx.MsgChannelOpenTry.parseFrom(stdMsg.getValue).getSigner)
            case schema.constants.Messages.CHANNEL_OPEN_ACK => Seq(channelTx.MsgChannelOpenAck.parseFrom(stdMsg.getValue).getSigner)
            case schema.constants.Messages.CHANNEL_OPEN_CONFIRM => Seq(channelTx.MsgChannelOpenConfirm.parseFrom(stdMsg.getValue).getSigner)
            case schema.constants.Messages.CHANNEL_CLOSE_INIT => Seq(channelTx.MsgChannelCloseInit.parseFrom(stdMsg.getValue).getSigner)
            case schema.constants.Messages.CHANNEL_CLOSE_CONFIRM => Seq(channelTx.MsgChannelCloseConfirm.parseFrom(stdMsg.getValue).getSigner)
            case schema.constants.Messages.RECV_PACKET => val msg = channelTx.MsgRecvPacket.parseFrom(stdMsg.getValue)
              Seq(msg.getSigner, com.ibc.applications.transfer.v2.FungibleTokenPacketData.parseFrom(msg.getPacket.getData.toByteArray).getReceiver).distinct
            case schema.constants.Messages.TIMEOUT => Seq(channelTx.MsgTimeout.parseFrom(stdMsg.getValue).getSigner)
            case schema.constants.Messages.TIMEOUT_ON_CLOSE => Seq(channelTx.MsgTimeoutOnClose.parseFrom(stdMsg.getValue).getSigner)
            case schema.constants.Messages.ACKNOWLEDGEMENT => Seq(channelTx.MsgAcknowledgement.parseFrom(stdMsg.getValue).getSigner)
            //ibc-transfer
            case schema.constants.Messages.TRANSFER => Seq(transferTx.MsgTransfer.parseFrom(stdMsg.getValue).getSender)
            //assets
            case schema.constants.Messages.ASSET_BURN => Seq(assetsTransactions.burn.Message.parseFrom(stdMsg.getValue).getFrom)
            case schema.constants.Messages.ASSET_DEFINE => Seq(assetsTransactions.define.Message.parseFrom(stdMsg.getValue).getFrom)
            case schema.constants.Messages.ASSET_DEPUTIZE => Seq(assetsTransactions.deputize.Message.parseFrom(stdMsg.getValue).getFrom)
            case schema.constants.Messages.ASSET_MINT => Seq(assetsTransactions.mint.Message.parseFrom(stdMsg.getValue).getFrom)
            case schema.constants.Messages.ASSET_MUTATE => Seq(assetsTransactions.mutate.Message.parseFrom(stdMsg.getValue).getFrom)
            case schema.constants.Messages.ASSET_RENUMERATE => Seq(assetsTransactions.renumerate.Message.parseFrom(stdMsg.getValue).getFrom)
            case schema.constants.Messages.ASSET_REVOKE => Seq(assetsTransactions.revoke.Message.parseFrom(stdMsg.getValue).getFrom)
            //identities
            case schema.constants.Messages.IDENTITY_DEFINE => Seq(identitiesTransactions.define.Message.parseFrom(stdMsg.getValue).getFrom)
            case schema.constants.Messages.IDENTITY_DEPUTIZE => Seq(identitiesTransactions.deputize.Message.parseFrom(stdMsg.getValue).getFrom)
            case schema.constants.Messages.IDENTITY_ISSUE => val msg = identitiesTransactions.issue.Message.parseFrom(stdMsg.getValue)
              Seq(msg.getFrom, msg.getTo).distinct
            case schema.constants.Messages.IDENTITY_MUTATE => Seq(identitiesTransactions.mutate.Message.parseFrom(stdMsg.getValue).getFrom)
            case schema.constants.Messages.IDENTITY_NUB => Seq(identitiesTransactions.nub.Message.parseFrom(stdMsg.getValue).getFrom)
            case schema.constants.Messages.IDENTITY_PROVISION => val msg = identitiesTransactions.provision.Message.parseFrom(stdMsg.getValue)
              Seq(msg.getFrom, msg.getTo).distinct
            case schema.constants.Messages.IDENTITY_QUASH => Seq(identitiesTransactions.quash.Message.parseFrom(stdMsg.getValue).getFrom)
            case schema.constants.Messages.IDENTITY_REVOKE => Seq(identitiesTransactions.revoke.Message.parseFrom(stdMsg.getValue).getFrom)
            case schema.constants.Messages.IDENTITY_UNPROVISION => val msg = identitiesTransactions.unprovision.Message.parseFrom(stdMsg.getValue)
              Seq(msg.getFrom, msg.getTo).distinct
            //orders
            case schema.constants.Messages.ORDER_CANCEL => Seq(ordersTransactions.cancel.Message.parseFrom(stdMsg.getValue).getFrom)
            case schema.constants.Messages.ORDER_DEFINE => Seq(ordersTransactions.define.Message.parseFrom(stdMsg.getValue).getFrom)
            case schema.constants.Messages.ORDER_DEPUTIZE => Seq(ordersTransactions.deputize.Message.parseFrom(stdMsg.getValue).getFrom)
            case schema.constants.Messages.ORDER_IMMEDIATE => Seq(ordersTransactions.immediate.Message.parseFrom(stdMsg.getValue).getFrom)
            case schema.constants.Messages.ORDER_MAKE => Seq(ordersTransactions.make.Message.parseFrom(stdMsg.getValue).getFrom)
            case schema.constants.Messages.ORDER_MODIFY => Seq(ordersTransactions.modify.Message.parseFrom(stdMsg.getValue).getFrom)
            case schema.constants.Messages.ORDER_REVOKE => Seq(ordersTransactions.revoke.Message.parseFrom(stdMsg.getValue).getFrom)
            case schema.constants.Messages.ORDER_TAKE => Seq(ordersTransactions.take.Message.parseFrom(stdMsg.getValue).getFrom)
            //metas
            case schema.constants.Messages.META_REVEAL => Seq(metasTransactions.reveal.Message.parseFrom(stdMsg.getValue).getFrom)
            // splits
            case schema.constants.Messages.SPLIT_SEND => Seq(splitsTransactions.send.Message.parseFrom(stdMsg.getValue).getFrom)
            case schema.constants.Messages.SPLIT_WRAP => Seq(splitsTransactions.wrap.Message.parseFrom(stdMsg.getValue).getFrom)
            case schema.constants.Messages.SPLIT_UNWRAP => Seq(splitsTransactions.unwrap.Message.parseFrom(stdMsg.getValue).getFrom)
            case _ => Seq()
          }
        }
        txAddresses.flatten.distinct.map(x => WalletTransaction(address = x, txHash = tx.hash, height = height))
      }

      Service.add(walletTransactions.flatten)
    }

  }
}