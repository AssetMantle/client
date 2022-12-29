package services

import actors.{Message => actorsMessage}
import com.google.protobuf.{Any => protoAny}
import cosmos.authz.v1beta1.{Tx => authzTx}
import cosmos.bank.v1beta1.{Tx => bankTx}
import cosmos.crisis.v1beta1.{Tx => crisisTx}
import cosmos.distribution.v1beta1.{Tx => distributionTx}
import cosmos.feegrant.v1beta1.{Tx => feegrantTx}
import cosmos.gov.v1beta1.{Tx => govTx}
import cosmos.slashing.v1beta1.{Tx => slashingTx}
import cosmos.staking.v1beta1.{Tx => stakingTx}
import cosmos.vesting.v1beta1.{Tx => VestingTx}
import exceptions.BaseException
import ibc.applications.transfer.v1.{Tx => transferTx}
import ibc.core.channel.v1.{Tx => channelTx}
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
                       utilitiesOperations: utilities.Operations,
                       messagesApi: MessagesApi
                     )(implicit exec: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.SERVICES_BLOCK

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val webSocketMessageLang: Lang = Lang(configuration.get[String]("blockchain.explorer.webSocketMessageLang"))

  private val slashingNotificationFactor: BigDecimal = BigDecimal(configuration.get[String]("blockchain.explorer.slashingNotificationFactor"))

  private val txPerPage = 100

  def insertOnBlock(height: Int): Future[BlockCommitResponse] = {
    val blockCommitResponse = getBlockCommit.Service.get(height)
    val blockResponse = getBlock.Service.get(height)

    def insertBlock(blockCommitResponse: BlockCommitResponse): Future[Int] = blockchainBlocks.Service.insertOrUpdate(height = blockCommitResponse.result.signed_header.header.height, time = blockCommitResponse.result.signed_header.header.time, proposerAddress = blockCommitResponse.result.signed_header.header.proposer_address, validators = blockCommitResponse.result.signed_header.commit.signatures.flatten.map(_.validator_address))

    (for {
      blockCommitResponse <- blockCommitResponse
      _ <- insertBlock(blockCommitResponse)
      blockResponse <- blockResponse
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

    def insertTransactions(transactions: Seq[TransactionByHeightResponseTx]) = {
      val bcTxs = transactions.map(_.toTransaction)
      val insertTxs = blockchainTransactions.Service.insertMultiple(bcTxs)

      val updateTransactionCounter = analyticTransactionCounters.Utility.addStatisticsData(epoch = header.time.epoch, totalTxs = transactions.length)

      val updateMessageCounter = analyticMessageCounters.Utility.updateMessageCounter(bcTxs)

      for {
        _ <- insertTxs
        _ <- updateTransactionCounter
        _ <- updateMessageCounter
        _ <- actionsOnTransactions(bcTxs)(header)
      } yield bcTxs
    }

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
    val halving = blockchainParameters.Utility.onNewBlock(header)
    val tokens = blockchainTokens.Utility.updateAll()
    val validators = blockchainValidators.Utility.onNewBlock(header)
    // Evidence BeginBlocker is handled via Events
    // Gov EndBlocker is handled via Events
    // Slashing BeginBlocker is handled via Events
    // Staking Unbonding and Redelegation Completion EndBlocker is handled via Events

    (for {
      _ <- halving
      _ <- tokens
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
    val messages = if (transaction.status) utilitiesOperations.traverse(transaction.getMessages)(stdMsg => actionOnTxMessages(stdMsg = stdMsg))
    else Future(Seq.empty)
    val updateAccount = utilitiesOperations.traverse(transaction.getSigners)(signer => blockchainAccounts.Utility.incrementSequence(signer))

    // Should always be called after messages are processed, otherwise can create conflict
    def deductFees = {
      val deductFeesFrom = if (transaction.getFeeGranter != "") {
        val grantee = transaction.getFeePayer
        val feeGrant = blockchainFeeGrants.Service.tryGet(granter = transaction.getFeeGranter, grantee = grantee)

        def updateOrDeleteFeeGrant(feeGrant: FeeGrant) = {
          val response = feeGrant.allowance.validate(blockTime = header.time, fees = transaction.getFee.amount)
          if (response.delete) blockchainFeeGrants.Service.delete(granter = transaction.getFeeGranter, grantee = grantee)
          else blockchainFeeGrants.Service.insertOrUpdate(feeGrant.copy(allowance = response.updated))
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
      _ <- messages
      _ <- updateAccount
      _ <- deductFees
    } yield ()).recover {
      case baseException: BaseException => throw baseException
    }
  }

  def actionOnTxMessages(stdMsg: protoAny)(implicit header: Header): Future[Unit] = {
    try {
      val processMsg = stdMsg.getTypeUrl match {
        //auth
        case constants.Blockchain.TransactionMessage.CREATE_VESTING_ACCOUNT => blockchainAccounts.Utility.onCreateVestingAccount(VestingTx.MsgCreateVestingAccount.parseFrom(stdMsg.getValue))
        //authz
        case constants.Blockchain.TransactionMessage.GRANT_AUTHORIZATION => blockchainAuthorizations.Utility.onGrantAuthorization(authzTx.MsgGrant.parseFrom(stdMsg.getValue))
        case constants.Blockchain.TransactionMessage.REVOKE_AUTHORIZATION => blockchainAuthorizations.Utility.onRevokeAuthorization(authzTx.MsgRevoke.parseFrom(stdMsg.getValue))
        case constants.Blockchain.TransactionMessage.EXECUTE_AUTHORIZATION => {
          val messages = blockchainAuthorizations.Utility.onExecuteAuthorization(authzTx.MsgExec.parseFrom(stdMsg.getValue))

          def processMessages(messages: Seq[protoAny]) = utilitiesOperations.traverse(messages)(message => actionOnTxMessages(message))

          for {
            messages <- messages
            _ <- processMessages(messages)
          } yield ()
        }
        //bank
        case constants.Blockchain.TransactionMessage.SEND_COIN => blockchainBalances.Utility.onSendCoin(bankTx.MsgSend.parseFrom(stdMsg.getValue))
        case constants.Blockchain.TransactionMessage.MULTI_SEND => blockchainBalances.Utility.onMultiSend(bankTx.MsgMultiSend.parseFrom(stdMsg.getValue))
        //crisis
        case constants.Blockchain.TransactionMessage.VERIFY_INVARIANT => blockchainBalances.Utility.insertOrUpdateBalance(crisisTx.MsgVerifyInvariant.parseFrom(stdMsg.getValue).getSender) // Since no crisis module, so directly updating the account balance
        //distribution
        case constants.Blockchain.TransactionMessage.SET_WITHDRAW_ADDRESS => blockchainWithdrawAddresses.Utility.onSetWithdrawAddress(distributionTx.MsgSetWithdrawAddress.parseFrom(stdMsg.getValue))
        case constants.Blockchain.TransactionMessage.WITHDRAW_DELEGATOR_REWARD => blockchainValidators.Utility.onWithdrawDelegatorReward(distributionTx.MsgWithdrawDelegatorReward.parseFrom(stdMsg.getValue))
        case constants.Blockchain.TransactionMessage.WITHDRAW_VALIDATOR_COMMISSION => blockchainValidators.Utility.onWithdrawValidatorCommission(distributionTx.MsgWithdrawValidatorCommission.parseFrom(stdMsg.getValue))
        case constants.Blockchain.TransactionMessage.FUND_COMMUNITY_POOL => blockchainBalances.Utility.insertOrUpdateBalance(distributionTx.MsgFundCommunityPool.parseFrom(stdMsg.getValue).getDepositor)
        //evidence
        case constants.Blockchain.TransactionMessage.SUBMIT_EVIDENCE => Future()
        //feeGrant
        case constants.Blockchain.TransactionMessage.FEE_GRANT_ALLOWANCE => blockchainFeeGrants.Utility.onFeeGrantAllowance(feegrantTx.MsgGrantAllowance.parseFrom(stdMsg.getValue))
        case constants.Blockchain.TransactionMessage.FEE_REVOKE_ALLOWANCE => blockchainFeeGrants.Utility.onFeeRevokeAllowance(feegrantTx.MsgRevokeAllowance.parseFrom(stdMsg.getValue))
        //gov
        case constants.Blockchain.TransactionMessage.DEPOSIT => blockchainProposalDeposits.Utility.onDeposit(govTx.MsgDeposit.parseFrom(stdMsg.getValue))
        case constants.Blockchain.TransactionMessage.SUBMIT_PROPOSAL => blockchainProposals.Utility.onSubmitProposal(govTx.MsgSubmitProposal.parseFrom(stdMsg.getValue))
        case constants.Blockchain.TransactionMessage.VOTE => blockchainProposalVotes.Utility.onVote(govTx.MsgVote.parseFrom(stdMsg.getValue))
        //slashing
        case constants.Blockchain.TransactionMessage.UNJAIL => blockchainValidators.Utility.onUnjail(slashingTx.MsgUnjail.parseFrom(stdMsg.getValue))
        //staking
        case constants.Blockchain.TransactionMessage.CREATE_VALIDATOR => blockchainValidators.Utility.onCreateValidator(stakingTx.MsgCreateValidator.parseFrom(stdMsg.getValue))
        case constants.Blockchain.TransactionMessage.EDIT_VALIDATOR => blockchainValidators.Utility.onEditValidator(stakingTx.MsgEditValidator.parseFrom(stdMsg.getValue))
        case constants.Blockchain.TransactionMessage.DELEGATE => blockchainValidators.Utility.onDelegation(stakingTx.MsgDelegate.parseFrom(stdMsg.getValue))
        case constants.Blockchain.TransactionMessage.REDELEGATE => blockchainRedelegations.Utility.onRedelegation(stakingTx.MsgBeginRedelegate.parseFrom(stdMsg.getValue))
        case constants.Blockchain.TransactionMessage.UNDELEGATE => blockchainUndelegations.Utility.onUndelegation(stakingTx.MsgUndelegate.parseFrom(stdMsg.getValue))
        //ibc-client
        case constants.Blockchain.TransactionMessage.CREATE_CLIENT => Future()
        case constants.Blockchain.TransactionMessage.UPDATE_CLIENT => Future()
        case constants.Blockchain.TransactionMessage.SUBMIT_MISBEHAVIOUR => Future()
        case constants.Blockchain.TransactionMessage.UPGRADE_CLIENT => Future()
        //ibc-connection
        case constants.Blockchain.TransactionMessage.CONNECTION_OPEN_INIT => Future()
        case constants.Blockchain.TransactionMessage.CONNECTION_OPEN_CONFIRM => Future()
        case constants.Blockchain.TransactionMessage.CONNECTION_OPEN_ACK => Future()
        case constants.Blockchain.TransactionMessage.CONNECTION_OPEN_TRY => Future()
        //ibc-channel
        case constants.Blockchain.TransactionMessage.CHANNEL_OPEN_INIT => Future()
        case constants.Blockchain.TransactionMessage.CHANNEL_OPEN_TRY => Future()
        case constants.Blockchain.TransactionMessage.CHANNEL_OPEN_ACK => Future()
        case constants.Blockchain.TransactionMessage.CHANNEL_OPEN_CONFIRM => Future()
        case constants.Blockchain.TransactionMessage.CHANNEL_CLOSE_INIT => Future()
        case constants.Blockchain.TransactionMessage.CHANNEL_CLOSE_CONFIRM => Future()
        case constants.Blockchain.TransactionMessage.RECV_PACKET => blockchainBalances.Utility.onRecvPacket(channelTx.MsgRecvPacket.parseFrom(stdMsg.getValue))
        case constants.Blockchain.TransactionMessage.TIMEOUT => blockchainBalances.Utility.onTimeout(channelTx.MsgTimeout.parseFrom(stdMsg.getValue))
        case constants.Blockchain.TransactionMessage.TIMEOUT_ON_CLOSE => blockchainBalances.Utility.onTimeoutOnClose(stdMsg.message.asInstanceOf[TimeoutOnClose])
        case constants.Blockchain.TransactionMessage.ACKNOWLEDGEMENT => blockchainBalances.Utility.onAcknowledgement(stdMsg.message.asInstanceOf[Acknowledgement])
        //ibc-transfer
        case constants.Blockchain.TransactionMessage.TRANSFER => blockchainBalances.Utility.onIBCTransfer(transferTx.MsgTransfer.parseFrom(stdMsg.getValue))
        case _ => Future(logger.info(constants.Response.TRANSACTION_TYPE_NOT_FOUND.logMessage + ": " + stdMsg.getTypeUrl))
      }
      (for {
        _ <- processMsg
      } yield ()).recover {
        case _: BaseException => logger.error(stdMsg.getTypeUrl + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
        case _: Exception => logger.error(stdMsg.getTypeUrl + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }
    catch {
      case _: Exception => Future(logger.error(constants.Response.TRANSACTION_STRUCTURE_CHANGED.logMessage + ": " + stdMsg.getTypeUrl))
    }
  }

  def onSlashingEvents(slashingEvents: Seq[Event], height: Int): Future[Unit] = if (slashingEvents.nonEmpty) {
    val blockResponse = getBlock.Service.get(height)
    val slashingParameter = blockchainParameters.Service.tryGetSlashingParameter
    val slashing = blockchainTokens.Utility.onSlashing

    def update(blockResponse: BlockResponse, slashingParameter: SlashingParameter): Future[Map[String, String]] = {
      val validatorReasons = utilitiesOperations.traverse(slashingEvents.filter(_.attributes.exists(_.key == constants.Blockchain.Event.Attribute.Reason))) { slashingEvent =>
        slashingEvent.attributes.find(_.key == constants.Blockchain.Event.Attribute.Reason).fold(Future("", ""))(slashingReasonAttribute => {
          val slashingReason = Future(slashingReasonAttribute.value.getOrElse(throw new BaseException(constants.Response.SLASHING_EVENT_REASON_ATTRIBUTE_VALUE_NOT_FOUND)))
          val hexAddress = utilities.Bech32.convertConsensusAddressToHexAddress(slashingEvent.attributes.find(_.key == constants.Blockchain.Event.Attribute.Address).fold("")(_.value.getOrElse("")))
          val operatorAddress = if (hexAddress != "") blockchainValidators.Service.tryGetOperatorAddress(hexAddress) else Future(throw new BaseException(constants.Response.SLASHING_EVENT_ADDRESS_NOT_FOUND))

          // Shouldn't throw exception because even with light client attack reason double signing and validator address is present
          def getDistributionHeight(slashingReason: String) = if (slashingReason == constants.Blockchain.Event.Attribute.MissingSignature) (height - 2)
          else (blockResponse.result.block.evidence.evidence.flatMap(_.getSlashingEvidences).find(_.validatorHexAddress == hexAddress).getOrElse(throw new BaseException(constants.Response.TENDERMINT_EVIDENCE_NOT_FOUND)).height - 1)

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
      val validator = if (consensusAddress != "") blockchainValidators.Service.tryGetByHexAddress(utilities.Bech32.convertConsensusAddressToHexAddress(consensusAddress)) else Future(throw new BaseException(constants.Response.LIVENESS_EVENT_CONSENSUS_ADDRESS_NOT_FOUND))

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
      } yield if (proposalID == 0) throw new BaseException(constants.Response.EVENT_PROPOSAL_ID_NOT_FOUND) else ()
    } else Future()

    val processActiveProposal = if (event.`type` == constants.Blockchain.Event.ActiveProposal) {
      val proposalID = event.attributes.find(_.key == constants.Blockchain.Event.Attribute.ProposalID).fold(0)(_.value.getOrElse("0").toInt)
      val oldProposal = blockchainProposals.Service.tryGet(proposalID)

      def process(oldProposal: Proposal) = {

        def updateDeposits() = blockchainProposalDeposits.Utility.burnOrRefundDeposits(oldProposal.id)

        def updateProposal() = blockchainProposals.Utility.insertOrUpdateProposal(oldProposal.id)

        def actOnProposal(proposal: Proposal): Future[Unit] = if (proposal.isPassed) {
          proposal.content.proposalContentType match {
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
    val process = if (validator != "" && delegator != "") blockchainUndelegations.Utility.onUnbondingCompletionEvent(delegatorAddress = delegator, validatorAddress = validator, currentBlockTimeStamp = currentBlockTimeStamp) else Future(throw new BaseException(constants.Response.INVALID_UNBONDING_COMPLETION_EVENT))
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
    val process = if (srcValidator != "" && dstValidator != "" && delegator != "") blockchainRedelegations.Utility.onRedelegationCompletionEvent(delegator = delegator, srcValidator = srcValidator, dstValidator = dstValidator, currentBlockTimeStamp = currentBlockTimeStamp) else Future(throw new BaseException(constants.Response.INVALID_REDELEGATION_COMPLETION_EVENT))
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
