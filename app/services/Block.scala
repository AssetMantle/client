package services

import actors.{Message => actorsMessage}
import exceptions.BaseException
import models.blockchain.{Proposal, Redelegation, Undelegation, Validator, Transaction => blockchainTransaction}
import models.common.Parameters.{GovernanceParameter, SlashingParameter}
import models.common.ProposalContents.ParameterChange
import models.common.Serializable.StdMsg
import models.common.TransactionMessages._
import models.{blockchain, keyBase, masterTransaction}
import play.api.{Configuration, Logger}
import queries.Abstract.TendermintEvidence
import queries.responses.blockchain.BlockCommitResponse.{Response => BlockCommitResponse}
import queries.responses.blockchain.ProposalResponse.{Response => ProposalResponse}
import queries.responses.blockchain.BlockResponse.{Response => BlockResponse}
import queries.responses.blockchain.TransactionResponse.{Response => TransactionResponse}
import queries.responses.common.{Event, Header}
import queries.blockchain.{GetBlock, GetBlockCommit, GetProposal, GetTransaction, GetTransactionsByHeight}
import queries.responses.common.ProposalContents.CommunityPoolSpend
import utilities.MicroNumber

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Block @Inject()(
                       blockchainBlocks: blockchain.Blocks,
                       blockchainAccounts: blockchain.Accounts,
                       blockchainAverageBlockTimes: blockchain.AverageBlockTimes,
                       blockchainAssets: blockchain.Assets,
                       blockchainProposals: blockchain.Proposals,
                       blockchainProposalDeposits: blockchain.ProposalDeposits,
                       blockchainProposalVotes: blockchain.ProposalVotes,
                       blockchainBalances: blockchain.Balances,
                       blockchainClassifications: blockchain.Classifications,
                       blockchainDelegations: blockchain.Delegations,
                       blockchainIdentities: blockchain.Identities,
                       blockchainMetas: blockchain.Metas,
                       blockchainParameters: blockchain.Parameters,
                       blockchainMaintainers: blockchain.Maintainers,
                       blockchainOrders: blockchain.Orders,
                       blockchainRedelegations: blockchain.Redelegations,
                       blockchainSplits: blockchain.Splits,
                       blockchainTransactions: blockchain.Transactions,
                       blockchainTokens: blockchain.Tokens,
                       blockchainUndelegations: blockchain.Undelegations,
                       blockchainValidators: blockchain.Validators,
                       blockchainWithdrawAddresses: blockchain.WithdrawAddresses,
                       keyBaseValidatorAccounts: keyBase.ValidatorAccounts,
                       getBlockCommit: GetBlockCommit,
                       getBlock: GetBlock,
                       getTransaction: GetTransaction,
                       getTransactionsByHeight: GetTransactionsByHeight,
                       getProposal: GetProposal,
                       masterTransactionNotifications: masterTransaction.Notifications,
                       utilitiesOperations: utilities.Operations,
                     )(implicit exec: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.SERVICES_BLOCK

  private implicit val logger: Logger = Logger(this.getClass)

  def insertOnBlock(height: Int): Future[BlockCommitResponse] = {
    val blockCommitResponse = getBlockCommit.Service.get(height)

    def insertBlock(blockCommitResponse: BlockCommitResponse): Future[Int] = blockchainBlocks.Service.insertOrUpdate(height = blockCommitResponse.result.signed_header.header.height, time = blockCommitResponse.result.signed_header.header.time, proposerAddress = blockCommitResponse.result.signed_header.header.proposer_address, validators = blockCommitResponse.result.signed_header.commit.signatures.flatten.map(_.validator_address))

    (for {
      blockCommitResponse <- blockCommitResponse
      _ <- insertBlock(blockCommitResponse)
    } yield blockCommitResponse
      ).recover {
      case baseException: BaseException => throw baseException
    }
  }

  def insertTransactionsOnBlock(header: Header): Future[Seq[blockchainTransaction]] = {
    val transactionsByHeightResponse = getTransactionsByHeight.Service.get(header.height)

    def insertTransactions(transactionsHash: Seq[String]): Future[Seq[blockchainTransaction]] = if (transactionsHash.nonEmpty) {
      val transactionResponses = Future.traverse(transactionsHash)(txHash => getTransaction.Service.get(txHash))

      def getTransactions(transactionResponses: Seq[TransactionResponse]): Future[Seq[blockchainTransaction]] = Future(transactionResponses.map(_.tx_response.toTransaction))

      def insertTxs(transactions: Seq[blockchainTransaction]): Future[Seq[Int]] = blockchainTransactions.Service.insertMultiple(transactions)

      for {
        transactionResponses <- transactionResponses
        transactions <- getTransactions(transactionResponses)
        _ <- actionsOnTransactions(transactions)(header)
        _ <- insertTxs(transactions)
      } yield transactions
    } else Future(Seq.empty)

    (for {
      transactionsByHeightResponse <- transactionsByHeightResponse
      transactions <- insertTransactions(transactionsByHeightResponse.result.txs.map(_.hash))
    } yield transactions
      ).recover {
      case baseException: BaseException => if (baseException.failure == constants.Response.JSON_UNMARSHALLING_ERROR) {
        logger.error(baseException.failure.message)
        Seq.empty
      } else throw baseException
    }
  }

  def setAverageBlockTime(header: Header): Future[Double] = blockchainAverageBlockTimes.Service.set(header)

  def getAverageBlockTime: Future[Double] = blockchainAverageBlockTimes.Service.get

  //Should not be called at the same time as when processing txs as it can lead race to update same db table.
  def checksAndUpdatesOnNewBlock(header: Header): Future[Unit] = {
    val validators = blockchainValidators.Utility.onNewBlock(header)
    val halving = blockchainParameters.Utility.onNewBlock(header)
    val tokens = blockchainTokens.Utility.updateAll()
    // Evidence BeginBlocker is handled via Events
    // Gov EndBlocker is handled via Events
    // Slashing BeginBlocker is handled via Events
    // Staking Unbonding and Redelegation Completion EndBlocker is handled via Events

    (for {
      _ <- validators
      _ <- halving
      _ <- tokens
    } yield ()
      ).recover {
      case baseException: BaseException => throw baseException
    }
  }

  def sendNewBlockWebSocketMessage(blockCommitResponse: BlockCommitResponse, transactions: Seq[blockchainTransaction], averageBlockTime: Double): Future[Unit] = {
    val proposer = blockchainValidators.Service.tryGetProposerName(blockCommitResponse.result.signed_header.header.proposer_address)

    def getWebSocketNewBlock(proposer: String): actorsMessage.WebSocket.NewBlock = actorsMessage.WebSocket.NewBlock(
      block = actorsMessage.WebSocket.Block(height = blockCommitResponse.result.signed_header.header.height, time = utilities.Date.bcTimestampToString(blockCommitResponse.result.signed_header.header.time), proposer = proposer),
      txs = transactions.map(tx => actorsMessage.WebSocket.Tx(hash = tx.hash, status = tx.status, numMsgs = tx.messages.length, fees = tx.fee)),
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

  private def actionsOnTransactions(transactions: Seq[blockchainTransaction])(implicit header: Header): Future[Seq[Seq[Unit]]] = Future.traverse(transactions) { transaction =>
    if (transaction.status) Future.traverse(transaction.messages)(stdMsg => actionOnTxMessages(stdMsg = stdMsg))
    else Future(Seq.empty)
    //TODO blockchainAccounts.Utility.insertOrUpdateAccount(fromAddress)
  }

  def actionOnTxMessages(stdMsg: StdMsg)(implicit header: Header): Future[Unit] = {
    try {
      stdMsg.messageType match {
        //auth
        case constants.Blockchain.TransactionMessage.CREATE_VESTING_ACCOUNT => blockchainAccounts.Utility.onCreateVestingAccount(stdMsg.message.asInstanceOf[CreateVestingAccount])
        //bank
        case constants.Blockchain.TransactionMessage.SEND_COIN => blockchainBalances.Utility.onSendCoin(stdMsg.message.asInstanceOf[SendCoin])
        case constants.Blockchain.TransactionMessage.MULTI_SEND => blockchainBalances.Utility.onMultiSend(stdMsg.message.asInstanceOf[MultiSend])
        //crisis
        case constants.Blockchain.TransactionMessage.VERIFY_INVARIANT => blockchainBalances.Utility.insertOrUpdateBalance(stdMsg.message.asInstanceOf[VerifyInvariant].sender) // Since no crisis module, so directly updating the account balance
        //distribution
        case constants.Blockchain.TransactionMessage.SET_WITHDRAW_ADDRESS => blockchainWithdrawAddresses.Utility.onSetWithdrawAddress(stdMsg.message.asInstanceOf[SetWithdrawAddress])
        case constants.Blockchain.TransactionMessage.WITHDRAW_DELEGATOR_REWARD => blockchainValidators.Utility.onWithdrawDelegationReward(stdMsg.message.asInstanceOf[WithdrawDelegatorReward])
        case constants.Blockchain.TransactionMessage.WITHDRAW_VALIDATOR_COMMISSION => blockchainValidators.Utility.onWithdrawValidatorCommission(stdMsg.message.asInstanceOf[WithdrawValidatorCommission])
        case constants.Blockchain.TransactionMessage.FUND_COMMUNITY_POOL => blockchainBalances.Utility.subtractCoinsFromAccount(stdMsg.message.asInstanceOf[FundCommunityPool].depositor, stdMsg.message.asInstanceOf[FundCommunityPool].amount)
        //evidence
        case constants.Blockchain.TransactionMessage.SUBMIT_EVIDENCE => Future()
        //gov
        case constants.Blockchain.TransactionMessage.DEPOSIT => blockchainProposalDeposits.Utility.onDeposit(stdMsg.message.asInstanceOf[Deposit])
        case constants.Blockchain.TransactionMessage.SUBMIT_PROPOSAL => blockchainProposals.Utility.onSubmitProposal(stdMsg.message.asInstanceOf[SubmitProposal])
        case constants.Blockchain.TransactionMessage.VOTE => blockchainProposalVotes.Utility.onVote(stdMsg.message.asInstanceOf[Vote])
        //slashing
        case constants.Blockchain.TransactionMessage.UNJAIL => blockchainValidators.Utility.onUnjail(stdMsg.message.asInstanceOf[Unjail])
        //staking
        case constants.Blockchain.TransactionMessage.CREATE_VALIDATOR => blockchainValidators.Utility.onCreateValidator(stdMsg.message.asInstanceOf[CreateValidator])
        case constants.Blockchain.TransactionMessage.EDIT_VALIDATOR => blockchainValidators.Utility.onEditValidator(stdMsg.message.asInstanceOf[EditValidator])
        case constants.Blockchain.TransactionMessage.DELEGATE => blockchainValidators.Utility.onDelegation(stdMsg.message.asInstanceOf[Delegate])
        case constants.Blockchain.TransactionMessage.REDELEGATE => blockchainRedelegations.Utility.onRedelegation(stdMsg.message.asInstanceOf[Redelegate])
        case constants.Blockchain.TransactionMessage.UNDELEGATE => blockchainUndelegations.Utility.onUndelegation(stdMsg.message.asInstanceOf[Undelegate])
        //ibc-transfer
        case constants.Blockchain.TransactionMessage.TRANSFER => blockchainBalances.Utility.onIBCTransfer(stdMsg.message.asInstanceOf[Transfer])
        //Asset
        case constants.Blockchain.TransactionMessage.ASSET_DEFINE => blockchainAssets.Utility.onDefine(stdMsg.message.asInstanceOf[AssetDefine])
        case constants.Blockchain.TransactionMessage.ASSET_MINT => blockchainAssets.Utility.onMint(stdMsg.message.asInstanceOf[AssetMint])
        case constants.Blockchain.TransactionMessage.ASSET_MUTATE => blockchainAssets.Utility.onMutate(stdMsg.message.asInstanceOf[AssetMutate])
        case constants.Blockchain.TransactionMessage.ASSET_BURN => blockchainAssets.Utility.onBurn(stdMsg.message.asInstanceOf[AssetBurn])
        //Identity
        case constants.Blockchain.TransactionMessage.IDENTITY_DEFINE => blockchainIdentities.Utility.onDefine(stdMsg.message.asInstanceOf[IdentityDefine])
        case constants.Blockchain.TransactionMessage.IDENTITY_ISSUE => blockchainIdentities.Utility.onIssue(stdMsg.message.asInstanceOf[IdentityIssue])
        case constants.Blockchain.TransactionMessage.IDENTITY_PROVISION => blockchainIdentities.Utility.onProvision(stdMsg.message.asInstanceOf[IdentityProvision])
        case constants.Blockchain.TransactionMessage.IDENTITY_UNPROVISION => blockchainIdentities.Utility.onUnprovision(stdMsg.message.asInstanceOf[IdentityUnprovision])
        case constants.Blockchain.TransactionMessage.IDENTITY_NUB => blockchainIdentities.Utility.onNub(stdMsg.message.asInstanceOf[IdentityNub])
        //Split
        case constants.Blockchain.TransactionMessage.SPLIT_SEND => blockchainSplits.Utility.onSend(stdMsg.message.asInstanceOf[SplitSend])
        case constants.Blockchain.TransactionMessage.SPLIT_WRAP => blockchainSplits.Utility.onWrap(stdMsg.message.asInstanceOf[SplitWrap])
        case constants.Blockchain.TransactionMessage.SPLIT_UNWRAP => blockchainSplits.Utility.onUnwrap(stdMsg.message.asInstanceOf[SplitUnwrap])
        //Order
        case constants.Blockchain.TransactionMessage.ORDER_DEFINE => blockchainOrders.Utility.onDefine(stdMsg.message.asInstanceOf[OrderDefine])
        case constants.Blockchain.TransactionMessage.ORDER_MAKE => blockchainOrders.Utility.onMake(stdMsg.message.asInstanceOf[OrderMake])
        case constants.Blockchain.TransactionMessage.ORDER_TAKE => blockchainOrders.Utility.onTake(stdMsg.message.asInstanceOf[OrderTake])
        case constants.Blockchain.TransactionMessage.ORDER_CANCEL => blockchainOrders.Utility.onCancel(stdMsg.message.asInstanceOf[OrderCancel])
        //meta
        case constants.Blockchain.TransactionMessage.META_REVEAL => blockchainMetas.Utility.onReveal(stdMsg.message.asInstanceOf[MetaReveal])
        //maintainer
        case constants.Blockchain.TransactionMessage.MAINTAINER_DEPUTIZE => blockchainMaintainers.Utility.onDeputize(stdMsg.message.asInstanceOf[MaintainerDeputize])
        case _ => Future(logger.info(constants.Response.TRANSACTION_TYPE_NOT_FOUND.logMessage + ": " + stdMsg.messageType))
      }
    } catch {
      case _: Exception => Future(logger.error(constants.Response.TRANSACTION_STRUCTURE_CHANGED.logMessage + ": " + stdMsg.messageType))
    }
  }

  def onSlashingEvents(slashingEvents: Seq[Event], height: Int): Future[Unit] = {
    val blockResponse = getBlock.Service.get(height)
    val slashingParameter = blockchainParameters.Service.tryGetSlashingParameter
    val slashing = blockchainTokens.Utility.onSlashing

    def update(blockResponse: BlockResponse, slashingParameter: SlashingParameter): Future[Map[String, String]] = {
      val validatorReasons = utilitiesOperations.traverse(slashingEvents.filter(_.attributes.exists(_.key == constants.Blockchain.Event.Attribute.Reason))) { slashingEvent =>
        val slashingReasonAttribute = slashingEvent.attributes.find(_.key == constants.Blockchain.Event.Attribute.Reason)
        slashingReasonAttribute.fold(Future("", ""))(reasonAttribute => {
          val hexAddress = utilities.Bech32.convertConsensusAddressToHexAddress(slashingEvent.attributes.find(_.key == constants.Blockchain.Event.Attribute.Address).fold("")(_.value))
          val operatorAddress = if (hexAddress != "") blockchainValidators.Service.tryGetOperatorAddress(hexAddress) else Future(throw new BaseException(constants.Response.SLASHING_EVENT_ADDRESS_NOT_FOUND))
          val slashingFraction = if (reasonAttribute.value == constants.Blockchain.Event.Attribute.MissingSignature) slashingParameter.slashFractionDowntime else slashingParameter.slashFractionDoubleSign

          val slashingInfractionHeight = if (reasonAttribute.value == constants.Blockchain.Event.Attribute.MissingSignature) Future(height - 2)
          else Future(blockResponse.result.block.evidence.evidence.find(_.validatorHexAddress == hexAddress).getOrElse(throw new BaseException(constants.Response.TENDERMINT_EVIDENCE_NOT_FOUND)).height - 1)

          def slashing(operatorAddress: String, slashingInfractionHeight: Int) = {
            //TODO Check if correct when double signing
            if (reasonAttribute.value == constants.Blockchain.Event.Attribute.DoubleSign) {
              println("/////////////////// When double signing slashingInfractionHeight: ")
              println(slashingInfractionHeight)
            }
            slash(validatorAddress = operatorAddress, infractionHeight = slashingInfractionHeight, currentBlockHeight = height, currentBlockTIme = blockResponse.result.block.header.time, slashingFraction: BigDecimal)
          }

          for {
            operatorAddress <- operatorAddress
            slashingInfractionHeight <- slashingInfractionHeight
            _ <- slashing(operatorAddress, slashingInfractionHeight)
          } yield (operatorAddress, reasonAttribute.value)
        })
      }
      for {
        validatorReasons <- validatorReasons
      } yield Map(validatorReasons.filter(_._1 != ""): _*)
    }

    def updateActiveValidatorSet() = blockchainValidators.Utility.updateActiveValidatorSet()

    def addEvents(validatorReasons: Map[String, String]): Future[Seq[Unit]] = {
      utilitiesOperations.traverse(validatorReasons.keySet.toSeq) { operatorAddress =>
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
  }

  def onMissedBlockEvents(livenessEvents: Seq[Event], height: Int): Future[Unit] = if (livenessEvents.nonEmpty) {

    val slashingParameter = blockchainParameters.Service.tryGetSlashingParameter

    def addEvent(validator: Validator, missedBlockCounter: Int, height: Int, slashingParameter: SlashingParameter): Future[String] = {
      //TODO criteria needs to be set to send notification
      //TODO In future if private notifications is asked for missing blocks then it needs to be done from here.
      val slashingOnMissingBlocks = slashingParameter.minSignedPerWindow * slashingParameter.signedBlocksWindow
      if ((missedBlockCounter % (slashingOnMissingBlocks / 10) == 0) && missedBlockCounter != slashingOnMissingBlocks) {
        masterTransactionNotifications.Service.create(constants.Notification.VALIDATOR_MISSED_BLOCKS, validator.description.moniker, missedBlockCounter.toString, height.toString)(validator.operatorAddress)
      } else Future("")
    }

    def update(slashingParameter: SlashingParameter) = Future.traverse(livenessEvents) {
      event =>
        val consensusAddress = event.attributes.find(x => x.key == constants.Blockchain.Event.Attribute.Address).fold("")(_.value)
        val missedBlocks = event.attributes.find(x => x.key == constants.Blockchain.Event.Attribute.MissedBlocks).fold(0)(_.value.toInt)
        val validator = blockchainValidators.Service.tryGetByHexAddress(utilities.Bech32.convertConsensusAddressToHexAddress(consensusAddress))

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
      val proposalID = event.attributes.find(_.key == constants.Blockchain.Event.Attribute.ProposalID).fold(0)(_.value.toInt)
      val deleteDeposits = blockchainProposalDeposits.Service.deleteByProposalID(proposalID)

      def deleteProposal() = blockchainProposals.Service.delete(proposalID)

      for {
        _ <- deleteDeposits
        _ <- deleteProposal()
      } yield if (proposalID == 0) throw new BaseException(constants.Response.EVENT_PROPOSAL_ID_NOT_FOUND) else ()
    } else Future()

    val processActiveProposal = if (event.`type` == constants.Blockchain.Event.ActiveProposal) {
      val proposalID = event.attributes.find(_.key == constants.Blockchain.Event.Attribute.ProposalID).fold(0)(_.value.toInt)
      val oldProposal = blockchainProposals.Service.tryGet(proposalID)
      val proposalResponse = getProposal.Service.get(proposalID)

      def process(oldProposal: Proposal, proposalResponse: ProposalResponse) = {
        val tally = blockchainProposals.Utility.tally(proposalResponse.proposal.final_tally_result.toSerializableFinalTallyResult)

        def updateDeposits(burnDeposits: Boolean) = if (burnDeposits) blockchainProposalDeposits.Service.deleteByProposalID(oldProposal.id)
        else blockchainProposalDeposits.Utility.refundDeposits(oldProposal.id)

        def updateProposal() = blockchainProposals.Service.insertOrUpdate(oldProposal.copy(finalTallyResult = proposalResponse.proposal.final_tally_result.toSerializableFinalTallyResult, status = proposalResponse.proposal.status))

        def actOnProposal(proposalPasses: Boolean): Future[Unit] = if (proposalPasses) {
          proposalResponse.proposal.content.proposalContentType match {
            case constants.Blockchain.Proposal.PARAMETER_CHANGE => blockchainParameters.Utility.onParameterChange(proposalResponse.proposal.content.toSerializableProposalContent.asInstanceOf[ParameterChange])
            case constants.Blockchain.Proposal.COMMUNITY_POOL_SPEND => blockchainBalances.Utility.insertOrUpdateBalance(proposalResponse.proposal.content.asInstanceOf[CommunityPoolSpend].recipient)
            case constants.Blockchain.Proposal.SOFTWARE_UPGRADE => Future()
            case constants.Blockchain.Proposal.CANCEL_SOFTWARE_UPGRADE => Future()
            case constants.Blockchain.Proposal.TEXT => Future()
            case _ => Future()
          }
        } else Future()

        for {
          (proposalPasses, burnDeposits) <- tally
          _ <- updateDeposits(burnDeposits)
          _ <- updateProposal()
          _ <- actOnProposal(proposalPasses)
        } yield ()
      }

      for {
        oldProposal <- oldProposal
        proposalResponse <- proposalResponse
        _ <- process(oldProposal, proposalResponse)
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

  def onUnbondingCompletionEvents(unbondingCompletionEvents: Seq[Event]): Future[Seq[Unit]] = utilitiesOperations.traverse(unbondingCompletionEvents)(event => {
    val validator = event.attributes.find(_.key == constants.Blockchain.Event.Attribute.Validator).fold("")(_.value)
    val delegator = event.attributes.find(_.key == constants.Blockchain.Event.Attribute.Delegator).fold("")(_.value)
    val process = if (validator != "" && delegator != "") blockchainUndelegations.Utility.onUnbondingCompletionEvent(delegatorAddress = delegator, validatorAddress = validator) else Future(throw new BaseException(constants.Response.INVALID_UNBONDING_COMPLETION_EVENT))
    (for {
      _ <- process
    } yield ()
      ).recover {
      case baseException: BaseException => throw baseException
    }
  })


  def onRedelegationCompletionEvents(redelegationCompletionEvents: Seq[Event]): Future[Seq[Unit]] = utilitiesOperations.traverse(redelegationCompletionEvents)(event => {
    val srcValidator = event.attributes.find(_.key == constants.Blockchain.Event.Attribute.SrcValidator).fold("")(_.value)
    val dstValidator = event.attributes.find(_.key == constants.Blockchain.Event.Attribute.DstValidator).fold("")(_.value)
    val delegator = event.attributes.find(_.key == constants.Blockchain.Event.Attribute.Delegator).fold("")(_.value)
    val process = if (srcValidator != "" && dstValidator != "" && delegator != "") blockchainRedelegations.Utility.onRedelegationCompletionEvent(delegator = delegator, srcValidator = srcValidator, dstValidator = dstValidator) else Future(throw new BaseException(constants.Response.INVALID_REDELEGATION_COMPLETION_EVENT))
    (for {
      _ <- process
    } yield ()
      ).recover {
      case baseException: BaseException => throw baseException
    }
  })

  private def slash(validatorAddress: String, infractionHeight: Int, currentBlockHeight: Int, currentBlockTIme: String, slashingFraction: BigDecimal) = {
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
