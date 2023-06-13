package services

import actors.{Message => actorsMessage}
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
import com.google.protobuf.{Any => protoAny}
import com.ibc.applications.transfer.{v1 => transferTx}
import com.ibc.core.channel.{v1 => channelTx}
import com.ibc.core.client.{v1 => clientTx}
import com.ibc.core.connection.{v1 => connectionTx}
import exceptions.BaseException
import models.blockchain.{FeeGrant, Proposal, Redelegation, Undelegation, Validator, Transaction => blockchainTransaction}
import models.common.Parameters.SlashingParameter
import models.common.ProposalContents.ParameterChange
import models.{analytic, blockchain, masterTransaction}
import play.api.i18n.{Lang, MessagesApi}
import play.api.{Configuration, Logger}
import queries.blockchain._
import queries.responses.blockchain.BlockCommitResponse.{Response => BlockCommitResponse}
import queries.responses.blockchain.BlockResponse.{Response => BlockResponse}
import queries.responses.blockchain.TransactionByHeightResponse.{Response => TransactionByHeightResponse, Tx => TransactionByHeightResponseTx}
import queries.responses.common.ProposalContents.CommunityPoolSpend
import queries.responses.common.{Event, Header}
import utilities.Date.RFC3339
import utilities.MicroNumber

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.CollectionHasAsScala

@Singleton
class Block @Inject()(
                       analyticTransactionCounters: analytic.TransactionCounters,
                       analyticMessageCounters: analytic.MessageCounters,
                       blockchainBlocks: blockchain.Blocks,
                       blockchainAccounts: blockchain.Accounts,
                       blockchainProposals: blockchain.Proposals,
                       blockchainProposalDeposits: blockchain.ProposalDeposits,
                       blockchainProposalVotes: blockchain.ProposalVotes,
                       blockchainBalances: blockchain.Balances,
                       blockchainClassifications: blockchain.Classifications,
                       blockchainAssets: blockchain.Assets,
                       blockchainOrders: blockchain.Orders,
                       blockchainMaintainers: blockchain.Maintainers,
                       blockchainMetas: blockchain.Metas,
                       blockchainIdentities: blockchain.Identities,
                       blockchainSplits: blockchain.Splits,
                       blockchainFeeGrants: blockchain.FeeGrants,
                       blockchainAuthorizations: blockchain.Authorizations,
                       blockchainParameters: blockchain.Parameters,
                       blockchainRedelegations: blockchain.Redelegations,
                       blockchainTransactions: blockchain.Transactions,
                       blockchainTokens: blockchain.Tokens,
                       blockchainUndelegations: blockchain.Undelegations,
                       blockchainValidators: blockchain.Validators,
                       blockchainWithdrawAddresses: blockchain.WithdrawAddresses,
                       getBlockCommit: GetBlockCommit,
                       getBlock: GetBlock,
                       getTransactionsByHeight: GetTransactionsByHeight,
                       masterTransactionNotifications: masterTransaction.Notifications,
                       masterTransactionWalletTransactions: masterTransaction.WalletTransactions,
                       masterTransactionValidatorTransactions: masterTransaction.ValidatorTransactions,
                       utilitiesOperations: utilities.Operations,
                       messagesApi: MessagesApi
                     )(implicit exec: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.SERVICES_BLOCK

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val webSocketMessageLang: Lang = Lang(configuration.get[String]("blockchain.explorer.webSocketMessageLang"))

  private val slashingNotificationFactor: BigDecimal = BigDecimal(configuration.get[String]("blockchain.explorer.slashingNotificationFactor"))

  private val txPerPage = 100

  private var syncing = true

  def setSyncing(value: Boolean): Unit = {
    syncing = value
  }

  def insertOnBlock(height: Int): Future[BlockCommitResponse] = {
    val blockCommitResponse = getBlockCommit.Service.get(height)

    def insertBlock(blockCommitResponse: BlockCommitResponse): Future[Unit] = blockchainBlocks.Service.insertOrUpdate(height = blockCommitResponse.result.signed_header.header.height, time = blockCommitResponse.result.signed_header.header.time, proposerAddress = blockCommitResponse.result.signed_header.header.proposer_address, validators = blockCommitResponse.result.signed_header.commit.signatures.flatten.map(_.validator_address))

    (for {
      blockCommitResponse <- blockCommitResponse
      _ <- insertBlock(blockCommitResponse)
    } yield blockCommitResponse
      ).recover {
      case baseException: BaseException => throw baseException
    }
  }

  def insertTransactionsOnBlock(header: Header): Future[Seq[blockchainTransaction]] = {
    val transactionByHeightResponseTxs = {
      val transactionsByHeightResponse = getTransactionsByHeight.Service.get(height = header.height, perPage = txPerPage, page = 1)

      def getAllTxHashes(transactionsByHeightResponse: TransactionByHeightResponse): Future[Seq[TransactionByHeightResponseTx]] = if (transactionsByHeightResponse.result.total_count.toInt > txPerPage) {
        val restTxHashes = utilitiesOperations.traverse(Range.inclusive(2, math.ceil(transactionsByHeightResponse.result.total_count.toDouble / txPerPage).toInt)) { page =>
          val transactionsByHeightResponse = getTransactionsByHeight.Service.get(height = header.height, perPage = txPerPage, page = page)
          for {
            transactionsByHeightResponse <- transactionsByHeightResponse
          } yield transactionsByHeightResponse.result.txs
        }
        for {
          restTxHashes <- restTxHashes
        } yield transactionsByHeightResponse.result.txs ++ restTxHashes.flatten
      } else Future(transactionsByHeightResponse.result.txs)

      for {
        transactionsByHeightResponse <- transactionsByHeightResponse
        allTransactionByHeightResponseTx <- getAllTxHashes(transactionsByHeightResponse)
      } yield allTransactionByHeightResponseTx

    }

    def insertTransactions(transactions: Seq[TransactionByHeightResponseTx]): Future[Seq[blockchainTransaction]] = if (transactions.nonEmpty) {
      val bcTxs = transactions.map(_.toTransaction)
      val insertTxs = blockchainTransactions.Service.insertMultiple(bcTxs)
      val updateTransactionCounter = analyticTransactionCounters.Utility.addStatisticsData(epoch = header.time.epoch, totalTxs = transactions.length)
      val updateMessageCounter = analyticMessageCounters.Utility.updateMessageCounter(bcTxs)
      val wallet = masterTransactionWalletTransactions.Utility.addForTransactions(bcTxs, header.height)
      val validator = masterTransactionValidatorTransactions.Utility.addForTransactions(bcTxs, header.height)

      for {
        _ <- insertTxs
        _ <- updateTransactionCounter
        _ <- updateMessageCounter
        _ <- actionsOnTransactions(bcTxs)(header)
        _ <- wallet
        _ <- validator
      } yield bcTxs
    } else Future(Seq())

    (for {
      transactionByHeightResponseTxs <- transactionByHeightResponseTxs
      transactions <- insertTransactions(transactionByHeightResponseTxs)
    } yield transactions
      ).recover {
      case baseException: BaseException => if (baseException.failure == constants.Response.JSON_UNMARSHALLING_ERROR || baseException.failure == constants.Response.JSON_PARSE_EXCEPTION) {
        logger.error(baseException.failure.message)
        Seq.empty
      } else throw baseException
    }
  }

  //Should not be called at the same time as when processing txs as it can lead race to update same db table.
  def checksAndUpdatesOnNewBlock(header: Header): Future[Unit] = {
    val tokens = if (!syncing) blockchainTokens.Utility.updateAll() else Future()
    val validators = blockchainValidators.Utility.onNewBlock(header)
    val orders = blockchainOrders.Utility.onNewBlock(header)
    // Evidence BeginBlocker is handled via Events
    // Gov EndBlocker is handled via Events
    // Slashing BeginBlocker is handled via Events
    // Staking Unbonding and Redelegation Completion EndBlocker is handled via Events

    (for {
      _ <- tokens
      _ <- orders
      _ <- validators
    } yield ()
      ).recover {
      case baseException: BaseException => throw baseException
    }
  }

  def sendNewBlockWebSocketMessage(blockCommitResponse: BlockCommitResponse, transactions: Seq[blockchainTransaction], averageBlockTime: Double): Future[Unit] = {
    val proposer = blockchainValidators.Service.tryGetProposerName(blockCommitResponse.result.signed_header.header.proposer_address)

    def getWebSocketNewBlock(proposer: String): actorsMessage.WebSocket.NewBlock = actorsMessage.WebSocket.NewBlock(
      block = actorsMessage.WebSocket.Block(
        height = blockCommitResponse.result.signed_header.header.height,
        time = blockCommitResponse.result.signed_header.header.time.toString,
        proposer = proposer),
      txs = transactions.map(tx => actorsMessage.WebSocket.Tx(
        hash = tx.hash,
        status = tx.status,
        numMsgs = tx.getMessages.length,
        messageTypes = if (tx.getMessages.length == 1) messagesApi(constants.View.TxMessagesMap.getOrElse(tx.getMessages.head.getTypeUrl, tx.getMessages.head.getTypeUrl)) else s"${messagesApi(constants.View.TxMessagesMap.getOrElse(tx.getMessages.head.getTypeUrl, tx.getMessages.head.getTypeUrl))} (+${tx.getMessages.length - 1})",
        fees = tx.getFee)),
      averageBlockTime = averageBlockTime,
      validators = blockCommitResponse.result.signed_header.commit.signatures.flatten.map(_.validator_address)
    )

    (for {
      proposer <- proposer
    } yield actors.Service.appWebSocketActor ! getWebSocketNewBlock(proposer)
      ).recover {
      case baseException: BaseException => throw baseException
    }
  }

  def actionsOnTransactions(transactions: Seq[blockchainTransaction])(implicit header: Header): Future[Seq[Unit]] = utilitiesOperations.traverse(transactions) { transaction =>
    val signers = if (transaction.status) utilitiesOperations.traverse(transaction.getMessages)(stdMsg => actionOnTxMessages(stdMsg = stdMsg)) else Future(Seq())
    val updateAccount = blockchainAccounts.Utility.incrementSequence(transaction.getSigners.head)

    // Should always be called after messages are processed, otherwise can create conflict
    def deductFees = {
      val deductFeesFrom = if (transaction.getFeeGranter != "") {
        val grantee = transaction.getFeePayer
        val feeGrant = blockchainFeeGrants.Service.tryGet(granter = transaction.getFeeGranter, grantee = grantee)

        def updateOrDeleteFeeGrant(feeGrant: FeeGrant) = {
          val response = feeGrant.getAllowance.validate(blockTime = header.time, fees = transaction.getFee.amount)
          if (response.delete) blockchainFeeGrants.Service.delete(granter = transaction.getFeeGranter, grantee = grantee)
          else blockchainFeeGrants.Service.insertOrUpdate(feeGrant.copy(allowance = response.updated.toProto.toByteString.toByteArray))
        }

        for {
          feeGrant <- feeGrant
          _ <- updateOrDeleteFeeGrant(feeGrant)
        } yield transaction.getFeeGranter
      } else Future(transaction.getFeePayer)

      def updateBalance(address: String) = blockchainBalances.Utility.insertOrUpdateBalance(address)

      for {
        deductFeesFrom <- deductFeesFrom
        _ <- updateBalance(deductFeesFrom)
      } yield ()
    }

    (for {
      _ <- signers
      _ <- updateAccount
      _ <- deductFees
    } yield ()).recover {
      case baseException: BaseException => throw baseException
    }
  }

  def actionOnTxMessages(stdMsg: protoAny)(implicit header: Header): Future[String] = {
    val processMsg: Future[String] = stdMsg.getTypeUrl match {
      //auth
      case schema.constants.Messages.CREATE_VESTING_ACCOUNT => blockchainAccounts.Utility.onCreateVestingAccount(VestingTx.MsgCreateVestingAccount.parseFrom(stdMsg.getValue))
      //authz
      case schema.constants.Messages.GRANT_AUTHORIZATION => blockchainAuthorizations.Utility.onGrantAuthorization(authzTx.MsgGrant.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.REVOKE_AUTHORIZATION => blockchainAuthorizations.Utility.onRevokeAuthorization(authzTx.MsgRevoke.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.EXECUTE_AUTHORIZATION => {
        val executeMsg = authzTx.MsgExec.parseFrom(stdMsg.getValue)
        val processMessages = utilitiesOperations.traverse(executeMsg.getMsgsList.asScala.toSeq)(message => actionOnTxMessages(message))

        def updateAuthorization(granters: Seq[String]) = blockchainAuthorizations.Utility.onExecuteAuthorization(executeMsg, granters.head)

        for {
          granters <- processMessages
          _ <- updateAuthorization(granters)
        } yield executeMsg.getGrantee
      }
      //bank
      case schema.constants.Messages.SEND_COIN => blockchainBalances.Utility.onSendCoin(bankTx.MsgSend.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.MULTI_SEND => blockchainBalances.Utility.onMultiSend(bankTx.MsgMultiSend.parseFrom(stdMsg.getValue))
      //crisis
      case schema.constants.Messages.VERIFY_INVARIANT => Future(crisisTx.MsgVerifyInvariant.parseFrom(stdMsg.getValue).getSender)
      //distribution
      case schema.constants.Messages.SET_WITHDRAW_ADDRESS => blockchainWithdrawAddresses.Utility.onSetWithdrawAddress(distributionTx.MsgSetWithdrawAddress.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.WITHDRAW_DELEGATOR_REWARD => blockchainValidators.Utility.onWithdrawDelegatorReward(distributionTx.MsgWithdrawDelegatorReward.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.WITHDRAW_VALIDATOR_COMMISSION => blockchainValidators.Utility.onWithdrawValidatorCommission(distributionTx.MsgWithdrawValidatorCommission.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.FUND_COMMUNITY_POOL => Future(distributionTx.MsgFundCommunityPool.parseFrom(stdMsg.getValue).getDepositor)
      //evidence
      case schema.constants.Messages.SUBMIT_EVIDENCE => Future(evidenceTx.MsgSubmitEvidence.parseFrom(stdMsg.getValue).getSubmitter)
      //feeGrant
      case schema.constants.Messages.FEE_GRANT_ALLOWANCE => blockchainFeeGrants.Utility.onFeeGrantAllowance(feegrantTx.MsgGrantAllowance.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.FEE_REVOKE_ALLOWANCE => blockchainFeeGrants.Utility.onFeeRevokeAllowance(feegrantTx.MsgRevokeAllowance.parseFrom(stdMsg.getValue))
      //gov
      case schema.constants.Messages.DEPOSIT => blockchainProposalDeposits.Utility.onDeposit(govTx.MsgDeposit.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.SUBMIT_PROPOSAL => blockchainProposals.Utility.onSubmitProposal(govTx.MsgSubmitProposal.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.VOTE => blockchainProposalVotes.Utility.onVote(govTx.MsgVote.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.WEIGHTED_VOTE => blockchainProposalVotes.Utility.onWeightedVote(govTx.MsgVoteWeighted.parseFrom(stdMsg.getValue))
      //slashing
      case schema.constants.Messages.UNJAIL => blockchainValidators.Utility.onUnjail(slashingTx.MsgUnjail.parseFrom(stdMsg.getValue))
      //staking
      case schema.constants.Messages.CREATE_VALIDATOR => blockchainValidators.Utility.onCreateValidator(stakingTx.MsgCreateValidator.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.EDIT_VALIDATOR => blockchainValidators.Utility.onEditValidator(stakingTx.MsgEditValidator.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.DELEGATE => blockchainValidators.Utility.onDelegation(stakingTx.MsgDelegate.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.REDELEGATE => blockchainRedelegations.Utility.onRedelegation(stakingTx.MsgBeginRedelegate.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.UNDELEGATE => blockchainUndelegations.Utility.onUndelegation(stakingTx.MsgUndelegate.parseFrom(stdMsg.getValue))
      //ibc-client
      case schema.constants.Messages.CREATE_CLIENT => Future(clientTx.MsgCreateClient.parseFrom(stdMsg.getValue).getSigner)
      case schema.constants.Messages.UPDATE_CLIENT => Future(clientTx.MsgUpdateClient.parseFrom(stdMsg.getValue).getSigner)
      case schema.constants.Messages.SUBMIT_MISBEHAVIOUR => Future(clientTx.MsgSubmitMisbehaviour.parseFrom(stdMsg.getValue).getSigner)
      case schema.constants.Messages.UPGRADE_CLIENT => Future(clientTx.MsgUpgradeClient.parseFrom(stdMsg.getValue).getSigner)
      //ibc-connection
      case schema.constants.Messages.CONNECTION_OPEN_INIT => Future(connectionTx.MsgConnectionOpenInit.parseFrom(stdMsg.getValue).getSigner)
      case schema.constants.Messages.CONNECTION_OPEN_CONFIRM => Future(connectionTx.MsgConnectionOpenConfirm.parseFrom(stdMsg.getValue).getSigner)
      case schema.constants.Messages.CONNECTION_OPEN_ACK => Future(connectionTx.MsgConnectionOpenAck.parseFrom(stdMsg.getValue).getSigner)
      case schema.constants.Messages.CONNECTION_OPEN_TRY => Future(connectionTx.MsgConnectionOpenTry.parseFrom(stdMsg.getValue).getSigner)
      //ibc-channel
      case schema.constants.Messages.CHANNEL_OPEN_INIT => Future(channelTx.MsgChannelOpenInit.parseFrom(stdMsg.getValue).getSigner)
      case schema.constants.Messages.CHANNEL_OPEN_TRY => Future(channelTx.MsgChannelOpenTry.parseFrom(stdMsg.getValue).getSigner)
      case schema.constants.Messages.CHANNEL_OPEN_ACK => Future(channelTx.MsgChannelOpenAck.parseFrom(stdMsg.getValue).getSigner)
      case schema.constants.Messages.CHANNEL_OPEN_CONFIRM => Future(channelTx.MsgChannelOpenConfirm.parseFrom(stdMsg.getValue).getSigner)
      case schema.constants.Messages.CHANNEL_CLOSE_INIT => Future(channelTx.MsgChannelCloseInit.parseFrom(stdMsg.getValue).getSigner)
      case schema.constants.Messages.CHANNEL_CLOSE_CONFIRM => Future(channelTx.MsgChannelCloseConfirm.parseFrom(stdMsg.getValue).getSigner)
      case schema.constants.Messages.RECV_PACKET => blockchainBalances.Utility.onRecvPacket(channelTx.MsgRecvPacket.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.TIMEOUT => blockchainBalances.Utility.onTimeout(channelTx.MsgTimeout.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.TIMEOUT_ON_CLOSE => blockchainBalances.Utility.onTimeoutOnClose(channelTx.MsgTimeoutOnClose.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.ACKNOWLEDGEMENT => blockchainBalances.Utility.onAcknowledgement(channelTx.MsgAcknowledgement.parseFrom(stdMsg.getValue))
      //ibc-transfer
      case schema.constants.Messages.TRANSFER => blockchainBalances.Utility.onIBCTransfer(transferTx.MsgTransfer.parseFrom(stdMsg.getValue))
      //assets
      case schema.constants.Messages.ASSET_BURN => blockchainAssets.Utility.onBurn(assetsTransactions.burn.Message.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.ASSET_DEFINE => blockchainAssets.Utility.onDefine(assetsTransactions.define.Message.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.ASSET_DEPUTIZE => blockchainAssets.Utility.onDeputize(assetsTransactions.deputize.Message.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.ASSET_MINT => blockchainAssets.Utility.onMint(assetsTransactions.mint.Message.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.ASSET_MUTATE => blockchainAssets.Utility.onMutate(assetsTransactions.mutate.Message.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.ASSET_RENUMERATE => blockchainAssets.Utility.onRenumerate(assetsTransactions.renumerate.Message.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.ASSET_REVOKE => blockchainAssets.Utility.onRevoke(assetsTransactions.revoke.Message.parseFrom(stdMsg.getValue))
      //identities
      case schema.constants.Messages.IDENTITY_DEFINE => blockchainIdentities.Utility.onDefine(identitiesTransactions.define.Message.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.IDENTITY_DEPUTIZE => blockchainIdentities.Utility.onDeputize(identitiesTransactions.deputize.Message.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.IDENTITY_ISSUE => blockchainIdentities.Utility.onIssue(identitiesTransactions.issue.Message.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.IDENTITY_MUTATE => blockchainIdentities.Utility.onMutate(identitiesTransactions.mutate.Message.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.IDENTITY_NUB => blockchainIdentities.Utility.onNub(identitiesTransactions.nub.Message.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.IDENTITY_PROVISION => blockchainIdentities.Utility.onProvision(identitiesTransactions.provision.Message.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.IDENTITY_QUASH => blockchainIdentities.Utility.onQuash(identitiesTransactions.quash.Message.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.IDENTITY_REVOKE => blockchainIdentities.Utility.onRevoke(identitiesTransactions.revoke.Message.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.IDENTITY_UNPROVISION => blockchainIdentities.Utility.onUnprovision(identitiesTransactions.unprovision.Message.parseFrom(stdMsg.getValue))
      //orders
      case schema.constants.Messages.ORDER_CANCEL => blockchainOrders.Utility.onCancel(ordersTransactions.cancel.Message.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.ORDER_DEFINE => blockchainOrders.Utility.onDefine(ordersTransactions.define.Message.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.ORDER_DEPUTIZE => blockchainOrders.Utility.onDeputize(ordersTransactions.deputize.Message.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.ORDER_IMMEDIATE => Future(ordersTransactions.immediate.Message.parseFrom(stdMsg.getValue).getFrom)
      case schema.constants.Messages.ORDER_MAKE => blockchainOrders.Utility.onMake(ordersTransactions.make.Message.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.ORDER_MODIFY => blockchainOrders.Utility.onModify(ordersTransactions.modify.Message.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.ORDER_REVOKE => blockchainOrders.Utility.onRevoke(ordersTransactions.revoke.Message.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.ORDER_TAKE => blockchainOrders.Utility.onTake(ordersTransactions.take.Message.parseFrom(stdMsg.getValue))
      //metas
      case schema.constants.Messages.META_REVEAL => blockchainMetas.Utility.onRevealMeta(metasTransactions.reveal.Message.parseFrom(stdMsg.getValue))
      // splits
      case schema.constants.Messages.SPLIT_SEND => blockchainSplits.Utility.onSend(splitsTransactions.send.Message.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.SPLIT_WRAP => blockchainSplits.Utility.onWrap(splitsTransactions.wrap.Message.parseFrom(stdMsg.getValue))
      case schema.constants.Messages.SPLIT_UNWRAP => blockchainSplits.Utility.onUnwrap(splitsTransactions.unwrap.Message.parseFrom(stdMsg.getValue))
      case _ => logger.error(constants.Response.TRANSACTION_TYPE_NOT_FOUND.logMessage + ": " + stdMsg.getTypeUrl)
        Future("")
    }
    (for {
      signer <- processMsg
    } yield signer).recover {
      case _: Exception => logger.error(stdMsg.getTypeUrl + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
        ""
    }
  }

  def onSlashingEvents(slashingEvents: Seq[Event], height: Int): Future[Unit] = if (slashingEvents.nonEmpty) {
    val blockResponse = getBlock.Service.get(height)
    val slashingParameter = blockchainParameters.Service.tryGetSlashingParameter
    val slashing = blockchainTokens.Utility.onSlashing

    def update(blockResponse: BlockResponse, slashingParameter: SlashingParameter): Future[Map[String, String]] = {
      val validatorReasons = utilitiesOperations.traverse(slashingEvents.filter(_.attributes.exists(_.key == constants.Blockchain.Event.Attribute.Reason))) { slashingEvent =>
        slashingEvent.attributes.find(_.key == constants.Blockchain.Event.Attribute.Reason).fold(Future("", ""))(slashingReasonAttribute => {
          val slashingReason = Future(slashingReasonAttribute.value.getOrElse(constants.Response.SLASHING_EVENT_REASON_ATTRIBUTE_VALUE_NOT_FOUND.throwBaseException()))
          val hexAddress = utilities.Crypto.convertConsensusAddressToHexAddress(slashingEvent.attributes.find(_.key == constants.Blockchain.Event.Attribute.Address).fold("")(_.value.getOrElse("")))
          val operatorAddress = if (hexAddress != "") blockchainValidators.Service.tryGetOperatorAddress(hexAddress) else constants.Response.SLASHING_EVENT_ADDRESS_NOT_FOUND.throwBaseException()

          // Shouldn't throw exception because even with light client attack reason double signing and validator address is present
          def getDistributionHeight(slashingReason: String) = if (slashingReason == constants.Blockchain.Event.Attribute.MissingSignature) (height - 2)
          else (blockResponse.result.block.evidence.evidence.flatMap(_.getSlashingEvidences).find(_.validatorHexAddress == hexAddress).getOrElse(constants.Response.TENDERMINT_EVIDENCE_NOT_FOUND.throwBaseException()).height - 1)

          def slashing(operatorAddress: String, slashingReason: String, distributionHeight: Int) = {
            val slashingFraction = if (slashingReason == constants.Blockchain.Event.Attribute.MissingSignature) slashingParameter.slashFractionDowntime else slashingParameter.slashFractionDoubleSign

            slash(validatorAddress = operatorAddress, infractionHeight = distributionHeight, currentBlockHeight = height, currentBlockTIme = blockResponse.result.block.header.time, slashingFraction: BigDecimal)
          }

          for {
            operatorAddress <- operatorAddress
            slashingReason <- slashingReason
            _ <- slashing(operatorAddress = operatorAddress, slashingReason = slashingReason, distributionHeight = getDistributionHeight(slashingReason))
          } yield (operatorAddress, slashingReason)
        })
      }
      for {
        validatorReasons <- validatorReasons
      } yield Map(validatorReasons.filter(_._1 != ""): _*)
    }

    def updateActiveValidatorSet() = blockchainValidators.Utility.updateActiveValidatorSet()

    def addEvents(validatorReasons: Map[String, String]): Future[Seq[Unit]] = utilitiesOperations.traverse(validatorReasons.keySet.toSeq) { operatorAddress =>
      val validator = blockchainValidators.Service.tryGet(operatorAddress)

      def insertNotification(validator: Validator) = utilities.Validator.getSlashingReason(validatorReasons.getOrElse(operatorAddress, "")) match {
        case constants.View.MISSING_SIGNATURE => masterTransactionNotifications.Service.create(constants.Notification.VALIDATOR_MISSING_SIGNATURE_SLASHING, validator.description.moniker, height.toString)(s"'${validator.operatorAddress}'")
        case constants.View.DOUBLE_SIGNING => masterTransactionNotifications.Service.create(constants.Notification.VALIDATOR_DOUBLE_SIGNING_SLASHING, validator.description.moniker, height.toString)(s"'${validator.operatorAddress}'")
        case _ => Future("")
      }

      for {
        validator <- validator
        _ <- insertNotification(validator)
      } yield ()
    }

    (for {
      blockResponse <- blockResponse
      slashingParameter <- slashingParameter
      validatorReasons <- update(blockResponse, slashingParameter)
      _ <- updateActiveValidatorSet()
      _ <- slashing
      _ <- addEvents(validatorReasons)
    } yield ()
      ).recover {
      case baseException: BaseException => throw baseException
    }
  } else Future()

  def onMissedBlockEvents(livenessEvents: Seq[Event], height: Int): Future[Unit] = if (livenessEvents.nonEmpty) {
    val slashingParameter = blockchainParameters.Service.tryGetSlashingParameter

    def addEvent(validator: Validator, missedBlockCounter: Int, height: Int, slashingParameter: SlashingParameter): Future[String] = {
      //TODO criteria needs to be set to send notification
      //TODO In future if private notifications is asked for missing blocks then it needs to be done from here.
      val slashingOnMissingBlocks = slashingParameter.minSignedPerWindow * slashingParameter.signedBlocksWindow
      if ((missedBlockCounter % (slashingOnMissingBlocks * slashingNotificationFactor) == 0) && missedBlockCounter != slashingOnMissingBlocks) {
        masterTransactionNotifications.Service.create(constants.Notification.VALIDATOR_MISSED_BLOCKS, validator.description.moniker, missedBlockCounter.toString, height.toString)(s"'${validator.operatorAddress}'")
      } else Future("")
    }

    def update(slashingParameter: SlashingParameter) = Future.traverse(livenessEvents) { event =>
      val consensusAddress = event.attributes.find(x => x.key == constants.Blockchain.Event.Attribute.Address).fold("")(_.value.getOrElse(""))
      val missedBlocks = event.attributes.find(x => x.key == constants.Blockchain.Event.Attribute.MissedBlocks).fold(0)(_.value.getOrElse("0").toInt)
      val validator = if (consensusAddress != "") blockchainValidators.Service.tryGetByHexAddress(utilities.Crypto.convertConsensusAddressToHexAddress(consensusAddress)) else constants.Response.LIVENESS_EVENT_CONSENSUS_ADDRESS_NOT_FOUND.throwBaseException()

      (for {
        validator <- validator
        _ <- addEvent(validator = validator, missedBlockCounter = missedBlocks, height = height, slashingParameter = slashingParameter)
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    (for {
      slashingParameter <- slashingParameter
      _ <- update(slashingParameter)
    } yield ()
      ).recover {
      case baseException: BaseException => throw baseException
    }
  } else Future()

  def onProposalEvents(proposalEvents: Seq[Event]): Future[Seq[Unit]] = utilitiesOperations.traverse(proposalEvents)(event => {
    val processInactiveProposal = if (event.`type` == constants.Blockchain.Event.InactiveProposal) {
      val proposalID = event.attributes.find(_.key == constants.Blockchain.Event.Attribute.ProposalID).fold(0)(_.value.getOrElse("0").toInt)
      val deleteDeposits = blockchainProposalDeposits.Service.deleteByProposalID(proposalID)

      def deleteProposal() = blockchainProposals.Service.delete(proposalID)

      for {
        _ <- deleteDeposits
        _ <- deleteProposal()
      } yield if (proposalID == 0) constants.Response.EVENT_PROPOSAL_ID_NOT_FOUND.throwBaseException() else ()
    } else Future()

    val processActiveProposal = if (event.`type` == constants.Blockchain.Event.ActiveProposal) {
      val proposalID = event.attributes.find(_.key == constants.Blockchain.Event.Attribute.ProposalID).fold(0)(_.value.getOrElse("0").toInt)
      val oldProposal = blockchainProposals.Service.tryGet(proposalID)

      def process(oldProposal: Proposal) = {

        def updateDeposits() = blockchainProposalDeposits.Utility.burnOrRefundDeposits(oldProposal.id)

        def updateProposal() = blockchainProposals.Utility.insertOrUpdateProposal(oldProposal.id)

        def actOnProposal(proposal: Proposal): Future[Unit] = if (proposal.isPassed) {
          proposal.content.toProto.getTypeUrl match {
            case constants.Blockchain.Proposal.PARAMETER_CHANGE => blockchainParameters.Utility.onParameterChange(proposal.content.asInstanceOf[ParameterChange])
            case constants.Blockchain.Proposal.COMMUNITY_POOL_SPEND => blockchainBalances.Utility.insertOrUpdateBalance(proposal.content.asInstanceOf[CommunityPoolSpend].recipient)
            case constants.Blockchain.Proposal.SOFTWARE_UPGRADE => Future()
            case constants.Blockchain.Proposal.CANCEL_SOFTWARE_UPGRADE => Future()
            case constants.Blockchain.Proposal.TEXT => Future()
            case _ => Future()
          }
        } else Future()

        for {
          _ <- updateDeposits()
          proposal <- updateProposal()
          _ <- actOnProposal(proposal)
        } yield ()
      }

      for {
        oldProposal <- oldProposal
        _ <- process(oldProposal)
      } yield ()

    } else Future()

    (for {
      _ <- processInactiveProposal
      _ <- processActiveProposal
    } yield ()
      ).recover {
      case baseException: BaseException => throw baseException
    }
  })

  def onUnbondingCompletionEvents(unbondingCompletionEvents: Seq[Event], currentBlockTimeStamp: RFC3339): Future[Seq[Unit]] = utilitiesOperations.traverse(unbondingCompletionEvents)(event => {
    val validator = event.attributes.find(_.key == constants.Blockchain.Event.Attribute.Validator).fold("")(_.value.getOrElse(""))
    val delegator = event.attributes.find(_.key == constants.Blockchain.Event.Attribute.Delegator).fold("")(_.value.getOrElse(""))
    val process = if (validator != "" && delegator != "") blockchainUndelegations.Utility.onUnbondingCompletionEvent(delegatorAddress = delegator, validatorAddress = validator, currentBlockTimeStamp = currentBlockTimeStamp) else constants.Response.INVALID_UNBONDING_COMPLETION_EVENT.throwBaseException()
    (for {
      _ <- process
    } yield ()
      ).recover {
      case baseException: BaseException => throw baseException
    }
  })

  def onRedelegationCompletionEvents(redelegationCompletionEvents: Seq[Event], currentBlockTimeStamp: RFC3339): Future[Seq[Unit]] = utilitiesOperations.traverse(redelegationCompletionEvents)(event => {
    val srcValidator = event.attributes.find(_.key == constants.Blockchain.Event.Attribute.SrcValidator).fold("")(_.value.getOrElse(""))
    val dstValidator = event.attributes.find(_.key == constants.Blockchain.Event.Attribute.DstValidator).fold("")(_.value.getOrElse(""))
    val delegator = event.attributes.find(_.key == constants.Blockchain.Event.Attribute.Delegator).fold("")(_.value.getOrElse(""))
    val process = if (srcValidator != "" && dstValidator != "" && delegator != "") blockchainRedelegations.Utility.onRedelegationCompletionEvent(delegator = delegator, srcValidator = srcValidator, dstValidator = dstValidator, currentBlockTimeStamp = currentBlockTimeStamp) else constants.Response.INVALID_REDELEGATION_COMPLETION_EVENT.throwBaseException()
    (for {
      _ <- process
    } yield ()
      ).recover {
      case baseException: BaseException => throw baseException
    }
  })

  private def slash(validatorAddress: String, infractionHeight: Int, currentBlockHeight: Int, currentBlockTIme: RFC3339, slashingFraction: BigDecimal) = {
    val validator = blockchainValidators.Service.tryGet(validatorAddress)

    def update(validator: Validator) = if (!validator.isUnbonded && infractionHeight < currentBlockHeight) {
      val redelegations = blockchainRedelegations.Service.getAllBySourceValidator(validatorAddress)
      val undelegations = blockchainUndelegations.Service.getAllByValidator(validatorAddress)

      def slashUndelegations(undelegations: Seq[Undelegation]) = utilitiesOperations.traverse(undelegations)(undelegation => blockchainUndelegations.Utility.slashUndelegation(undelegation, currentBlockTIme, infractionHeight, slashingFraction))

      def slashedRedelegation(redelegations: Seq[Redelegation]) = utilitiesOperations.traverse(redelegations)(redelegation => blockchainRedelegations.Utility.slashRedelegation(redelegation, infractionHeight, currentBlockTIme, slashingFraction))

      def updateValidator() = blockchainValidators.Utility.insertOrUpdateValidator(validator.operatorAddress)

      for {
        redelegations <- redelegations
        undelegations <- undelegations
        _ <- slashedRedelegation(redelegations)
        _ <- slashUndelegations(undelegations)
        _ <- updateValidator()
      } yield ()
    } else Future(MicroNumber.zero)

    (for {
      validator <- validator
      _ <- update(validator)
    } yield ()).recover {
      case baseException: BaseException => throw baseException
    }
  }
}
