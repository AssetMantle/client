package models.blockchain

import cosmos.distribution.v1beta1.{Tx => distributionTx}
import cosmos.slashing.v1beta1.{Tx => slashingTx}
import cosmos.staking.v1beta1.{Tx => stakingTx}
import exceptions.BaseException
import models.Trait.Logging
import models.common.Serializable.Validator.{Commission, Description}
import models.{keyBase, masterTransaction}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.blockchain.{GetBondedValidators, GetValidator}
import queries.responses.common.Header
import slick.jdbc.JdbcProfile
import utilities.Date.RFC3339
import utilities.MicroNumber

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Validator(operatorAddress: String, hexAddress: String, jailed: Boolean, status: String, tokens: MicroNumber, delegatorShares: BigDecimal, description: Description, unbondingHeight: Int, unbondingTime: RFC3339, commission: Commission, minimumSelfDelegation: MicroNumber, createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging {

  def getTokensFromShares(shares: BigDecimal): MicroNumber = MicroNumber(((shares * BigDecimal(tokens.value)) / delegatorShares).toBigInt)

  def removeDelegatorShares(removeDelegatorShares: BigDecimal): (Validator, MicroNumber) = {
    val remainingShares = delegatorShares - removeDelegatorShares
    val (issuedTokens, validatorTokensLeft) = if (remainingShares == 0) (tokens, MicroNumber.zero) else (getTokensFromShares(removeDelegatorShares), tokens - getTokensFromShares(removeDelegatorShares))
    (Validator(operatorAddress = operatorAddress, hexAddress = hexAddress, jailed = jailed, status = status,
      tokens = validatorTokensLeft,
      delegatorShares = remainingShares,
      description = description, unbondingHeight = unbondingHeight, unbondingTime = unbondingTime, commission = commission, minimumSelfDelegation = minimumSelfDelegation),
      issuedTokens)
  }

  def isUnbonded: Boolean = status == constants.Blockchain.ValidatorStatus.UNBONDED

  def isUnbonding: Boolean = status == constants.Blockchain.ValidatorStatus.UNBONDING

  def isBonded: Boolean = status == constants.Blockchain.ValidatorStatus.BONDED

  def isUnbondingMatured(currentTime: RFC3339): Boolean = !unbondingTime.isAfter(currentTime)
}

@Singleton
class Validators @Inject()(
                            protected val databaseConfigProvider: DatabaseConfigProvider,
                            configuration: Configuration,
                            getValidator: GetValidator,
                            getBondedValidators: GetBondedValidators,
                            masterTransactionNotifications: masterTransaction.Notifications,
                            blockchainDelegations: Delegations,
                            blockchainBalances: Balances,
                            utilitiesOperations: utilities.Operations,
                            keyBaseValidatorAccounts: keyBase.ValidatorAccounts,
                            blockchainWithdrawAddresses: WithdrawAddresses,
                          )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_VALIDATOR

  import databaseConfig.profile.api._

  private[models] val validatorTable = TableQuery[ValidatorTable]

  case class ValidatorSerialized(operatorAddress: String, hexAddress: String, jailed: Boolean, status: String, tokens: BigDecimal, delegatorShares: BigDecimal, description: String, unbondingHeight: Int, unbondingTime: String, commission: String, minimumSelfDelegation: String, createdBy: Option[String], createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) {
    def deserialize: Validator = Validator(operatorAddress = operatorAddress, hexAddress = hexAddress, status = status, jailed = jailed, tokens = new MicroNumber(tokens), delegatorShares = delegatorShares, description = utilities.JSON.convertJsonStringToObject[Description](description), unbondingHeight = unbondingHeight, unbondingTime = RFC3339(unbondingTime), commission = utilities.JSON.convertJsonStringToObject[Commission](commission), minimumSelfDelegation = new MicroNumber(minimumSelfDelegation), createdBy = createdBy, createdOnMillisEpoch = createdOnMillisEpoch, updatedBy = updatedBy, updatedOnMillisEpoch = updatedOnMillisEpoch)
  }

  def serialize(validator: Validator): ValidatorSerialized = ValidatorSerialized(operatorAddress = validator.operatorAddress, hexAddress = validator.hexAddress, status = validator.status, jailed = validator.jailed, tokens = validator.tokens.toBigDecimal, delegatorShares = validator.delegatorShares, description = Json.toJson(validator.description).toString, unbondingHeight = validator.unbondingHeight, unbondingTime = validator.unbondingTime.toString, commission = Json.toJson(validator.commission).toString, minimumSelfDelegation = validator.minimumSelfDelegation.toString, createdBy = validator.createdBy, createdOnMillisEpoch = validator.createdOnMillisEpoch, updatedBy = validator.updatedBy, updatedOnMillisEpoch = validator.updatedOnMillisEpoch)

  private def add(validator: Validator): Future[String] = db.run((validatorTable returning validatorTable.map(_.operatorAddress) += serialize(validator)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def addMultiple(validators: Seq[Validator]): Future[Seq[String]] = db.run((validatorTable returning validatorTable.map(_.operatorAddress) ++= validators.map(x => serialize(x))).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(validator: Validator): Future[Int] = db.run(validatorTable.insertOrUpdate(serialize(validator)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateJailedStatus(operatorAddress: String, jailed: Boolean): Future[Int] = db.run(validatorTable.filter(_.operatorAddress === operatorAddress).map(_.jailed).update(jailed).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.VALIDATOR_NOT_FOUND, noSuchElementException)
    }
  }

  private def tryGetValidatorByOperatorOrHexAddress(address: String): Future[ValidatorSerialized] = db.run(validatorTable.filter(x => x.operatorAddress === address || x.hexAddress === address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.VALIDATOR_NOT_FOUND, noSuchElementException)
    }
  }

  private def tryGetValidatorsByHexAddresses(hexAddresses: Seq[String]): Future[Seq[ValidatorSerialized]] = db.run(validatorTable.filter(_.hexAddress.inSet(hexAddresses)).result)

  private def tryGetValidatorByOperatorAddress(operatorAddress: String): Future[ValidatorSerialized] = db.run(validatorTable.filter(_.operatorAddress === operatorAddress).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.VALIDATOR_NOT_FOUND, noSuchElementException)
    }
  }

  private def tryGetValidatorByHexAddress(hexAddress: String): Future[ValidatorSerialized] = db.run(validatorTable.filter(_.hexAddress === hexAddress).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.VALIDATOR_NOT_FOUND, noSuchElementException)
    }
  }

  private def tryGetOperatorAddressByHexAddress(hexAddress: String): Future[String] = db.run(validatorTable.filter(_.hexAddress === hexAddress).map(_.operatorAddress).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.VALIDATOR_NOT_FOUND, noSuchElementException)
    }
  }

  private def tryGethexAddressByOperatorAddress(operatorAddress: String): Future[String] = db.run(validatorTable.filter(_.operatorAddress === operatorAddress).map(_.hexAddress).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.VALIDATOR_NOT_FOUND, noSuchElementException)
    }
  }

  private def getAllValidators: Future[Seq[ValidatorSerialized]] = db.run(validatorTable.result)

  private def getValidatorsByStatus(status: String): Future[Seq[ValidatorSerialized]] = db.run(validatorTable.filter(_.status === status).result)

  private def getAllInactiveValidators: Future[Seq[ValidatorSerialized]] = db.run(validatorTable.filterNot(_.status === constants.Blockchain.ValidatorStatus.BONDED).result)

  private def getValidatorsByOperatorAddresses(operatorAddresses: Seq[String]): Future[Seq[ValidatorSerialized]] = db.run(validatorTable.filter(_.operatorAddress.inSet(operatorAddresses)).result)

  private def checkExistsByOperatorAddress(operatorAddress: String): Future[Boolean] = db.run(validatorTable.filter(_.operatorAddress === operatorAddress).exists.result)

  private def deleteByOperatorAddress(operatorAddress: String): Future[Int] = db.run(validatorTable.filter(_.operatorAddress === operatorAddress).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.VALIDATOR_NOT_FOUND, noSuchElementException)
      // Here it can happen that validator is deleted in BC but there are some undelegations left over, so this throws foreign key constraint violation. (BC doesn't have FK constraint. Removing FK constraint here is bad idea.)
      case _: PSQLException => 0
    }
  }

  private def getAllVotingPowers: Future[Seq[BigDecimal]] = db.run(validatorTable.filter(_.status === constants.Blockchain.ValidatorStatus.BONDED).map(_.tokens).result)

  private[models] class ValidatorTable(tag: Tag) extends Table[ValidatorSerialized](tag, "Validator") {

    def * = (operatorAddress, hexAddress, jailed, status, tokens, delegatorShares, description, unbondingHeight, unbondingTime, commission, minimumSelfDelegation, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (ValidatorSerialized.tupled, ValidatorSerialized.unapply)

    def operatorAddress = column[String]("operatorAddress", O.PrimaryKey)

    def hexAddress = column[String]("hexAddress", O.Unique)

    def jailed = column[Boolean]("jailed")

    def status = column[String]("status")

    def tokens = column[BigDecimal]("tokens")

    def delegatorShares = column[BigDecimal]("delegatorShares")

    def description = column[String]("description")

    def unbondingHeight = column[Int]("unbondingHeight")

    def unbondingTime = column[String]("unbondingTime")

    def commission = column[String]("commission")

    def minimumSelfDelegation = column[String]("minimumSelfDelegation")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")
  }

  object Service {

    def create(validator: Validator): Future[String] = add(validator)

    def insertMultiple(validators: Seq[Validator]): Future[Seq[String]] = addMultiple(validators)

    def insertOrUpdate(validator: Validator): Future[Int] = upsert(validator)

    def tryGet(address: String): Future[Validator] = tryGetValidatorByOperatorOrHexAddress(address).map(_.deserialize)

    def getAllByHexAddresses(hexAddresses: Seq[String]): Future[Seq[Validator]] = tryGetValidatorsByHexAddresses(hexAddresses).map(_.map(_.deserialize))

    def tryGetByOperatorAddress(operatorAddress: String): Future[Validator] = tryGetValidatorByOperatorAddress(operatorAddress).map(_.deserialize)

    def tryGetByHexAddress(hexAddress: String): Future[Validator] = tryGetValidatorByHexAddress(hexAddress).map(_.deserialize)

    def tryGetOperatorAddress(hexAddress: String): Future[String] = tryGetOperatorAddressByHexAddress(hexAddress)

    def tryGetHexAddress(operatorAddress: String): Future[String] = tryGethexAddressByOperatorAddress(operatorAddress)

    def tryGetProposerName(hexAddress: String): Future[String] = tryGetValidatorByHexAddress(hexAddress).map { x =>
      val validator = x.deserialize
      validator.description.moniker
    }

    def getAll: Future[Seq[Validator]] = getAllValidators.map(_.map(_.deserialize))

    def getAllActiveValidatorList: Future[Seq[Validator]] = getValidatorsByStatus(constants.Blockchain.ValidatorStatus.BONDED).map(_.map(_.deserialize))

    def getAllInactiveValidatorList: Future[Seq[Validator]] = getAllInactiveValidators.map(_.map(_.deserialize))

    def getAllUnbondingValidatorList: Future[Seq[Validator]] = getValidatorsByStatus(constants.Blockchain.ValidatorStatus.UNBONDING).map(_.map(_.deserialize))

    def getAllUnbondedValidatorList: Future[Seq[Validator]] = getValidatorsByStatus(constants.Blockchain.ValidatorStatus.UNBONDED).map(_.map(_.deserialize))

    def getByOperatorAddresses(operatorAddresses: Seq[String]): Future[Seq[Validator]] = getValidatorsByOperatorAddresses(operatorAddresses).map(_.map(_.deserialize))

    def exists(operatorAddress: String): Future[Boolean] = checkExistsByOperatorAddress(operatorAddress)

    def jailValidator(operatorAddress: String): Future[Int] = updateJailedStatus(operatorAddress = operatorAddress, jailed = true)

    def delete(operatorAddress: String): Future[Unit] = {
      val deleteKeyBaseAccount = keyBaseValidatorAccounts.Service.delete(operatorAddress)

      def deleteValidator() = deleteByOperatorAddress(operatorAddress)

      (for {
        _ <- deleteKeyBaseAccount
        _ <- deleteValidator()
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def getTotalVotingPower: Future[MicroNumber] = getAllVotingPowers.map(_.map(x => new MicroNumber(x)).sum)
  }

  object Utility {

    def onCreateValidator(createValidator: stakingTx.MsgCreateValidator)(implicit header: Header): Future[String] = {
      val upsertValidator = insertOrUpdateValidator(createValidator.getValidatorAddress)

      def updateOtherDetails() = {
        val insertDelegation = onDelegation(stakingTx.MsgDelegate.newBuilder()
          .setDelegatorAddress(utilities.Bech32.convertOperatorAddressToAccountAddress(createValidator.getValidatorAddress))
          .setValidatorAddress(createValidator.getValidatorAddress)
          .setAmount(createValidator.getValue).build())
        val addEvent = masterTransactionNotifications.Service.create(constants.Notification.VALIDATOR_CREATED, createValidator.getDescription.getMoniker)(s"'${createValidator.getValidatorAddress}'")
        val insertKeyBaseAccount = keyBaseValidatorAccounts.Utility.insertOrUpdateKeyBaseAccount(createValidator.getValidatorAddress, createValidator.getDescription.getIdentity)

        for {
          _ <- insertDelegation
          _ <- addEvent
          _ <- insertKeyBaseAccount
        } yield ()
      }

      (for {
        _ <- upsertValidator
        _ <- updateOtherDetails()
        _ <- updateActiveValidatorSet()
      } yield createValidator.getDelegatorAddress).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.CREATE_VALIDATOR + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          createValidator.getDelegatorAddress
      }
    }

    def onEditValidator(editValidator: stakingTx.MsgEditValidator)(implicit header: Header): Future[String] = {
      val upsertValidator = insertOrUpdateValidator(editValidator.getValidatorAddress)

      def addEvent(validator: Validator) = masterTransactionNotifications.Service.create(constants.Notification.VALIDATOR_EDITED, validator.description.moniker)(s"'${validator.operatorAddress}'")

      def insertKeyBaseAccount(validator: Validator) = keyBaseValidatorAccounts.Utility.insertOrUpdateKeyBaseAccount(validator.operatorAddress, validator.description.identity)

      (for {
        validator <- upsertValidator
        _ <- addEvent(validator)
        - <- insertKeyBaseAccount(validator)
      } yield utilities.Bech32.convertOperatorAddressToAccountAddress(editValidator.getValidatorAddress)).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.EDIT_VALIDATOR + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          utilities.Bech32.convertOperatorAddressToAccountAddress(editValidator.getValidatorAddress)
      }
    }

    def onUnjail(unjail: slashingTx.MsgUnjail)(implicit header: Header): Future[String] = {
      val upsertValidator = insertOrUpdateValidator(unjail.getValidatorAddr)

      def addEvent(validator: Validator) = masterTransactionNotifications.Service.create(constants.Notification.VALIDATOR_UNJAILED, validator.description.moniker)(s"'${validator.operatorAddress}'")

      (for {
        validator <- upsertValidator
        _ <- updateActiveValidatorSet()
        _ <- addEvent(validator)
      } yield utilities.Bech32.convertOperatorAddressToAccountAddress(unjail.getValidatorAddr)).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.UNJAIL + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          utilities.Bech32.convertOperatorAddressToAccountAddress(unjail.getValidatorAddr)
      }
    }

    def onDelegation(delegate: stakingTx.MsgDelegate)(implicit header: Header): Future[String] = {
      val updateValidator = insertOrUpdateValidator(delegate.getValidatorAddress)
      val accountBalance = blockchainBalances.Utility.insertOrUpdateBalance(delegate.getDelegatorAddress)
      val insertDelegation = blockchainDelegations.Utility.insertOrUpdate(delegatorAddress = delegate.getDelegatorAddress, validatorAddress = delegate.getValidatorAddress)
      val withdrawRewards = blockchainWithdrawAddresses.Utility.withdrawRewards(delegate.getDelegatorAddress)

      (for {
        _ <- updateValidator
        _ <- accountBalance
        _ <- insertDelegation
        _ <- withdrawRewards
        _ <- updateActiveValidatorSet()
      } yield delegate.getDelegatorAddress).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.DELEGATE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          delegate.getDelegatorAddress
      }
    }

    def onWithdrawDelegatorReward(withdrawDelegatorReward: distributionTx.MsgWithdrawDelegatorReward)(implicit header: Header): Future[String] = {
      val withdrawBalance = blockchainWithdrawAddresses.Utility.withdrawRewards(withdrawDelegatorReward.getDelegatorAddress)

      (for {
        _ <- withdrawBalance
      } yield withdrawDelegatorReward.getDelegatorAddress).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.WITHDRAW_DELEGATOR_REWARD + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          withdrawDelegatorReward.getDelegatorAddress
      }
    }

    def onWithdrawValidatorCommission(withdrawValidatorCommission: distributionTx.MsgWithdrawValidatorCommission)(implicit header: Header): Future[String] = {
      val accountAddress = utilities.Bech32.convertOperatorAddressToAccountAddress(withdrawValidatorCommission.getValidatorAddress)
      val withdrawBalance = blockchainWithdrawAddresses.Utility.withdrawRewards(accountAddress)

      (for {
        _ <- withdrawBalance
      } yield utilities.Bech32.convertOperatorAddressToAccountAddress(withdrawValidatorCommission.getValidatorAddress)).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.WITHDRAW_VALIDATOR_COMMISSION + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          utilities.Bech32.convertOperatorAddressToAccountAddress(withdrawValidatorCommission.getValidatorAddress)
      }
    }

    def insertOrUpdateValidator(validatorAddress: String): Future[Validator] = {
      val validatorResponse = getValidator.Service.get(validatorAddress)

      def insertValidator(validator: Validator) = Service.insertOrUpdate(validator)

      (for {
        validatorResponse <- validatorResponse
        _ <- insertValidator(validatorResponse.validator.toValidator)
      } yield validatorResponse.validator.toValidator
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def onNewBlock(header: Header): Future[Unit] = {
      val unbondingValidators = Service.getAllUnbondingValidatorList

      def checkAndUpdateUnbondingValidators(unbondingValidators: Seq[Validator]) = utilitiesOperations.traverse(unbondingValidators)(unbondingValidator => {
        if (header.height >= unbondingValidator.unbondingHeight && unbondingValidator.isUnbondingMatured(header.time)) {
          val updateOrDeleteValidator = if (unbondingValidator.delegatorShares == 0) Service.delete(unbondingValidator.operatorAddress) else Service.insertOrUpdate(unbondingValidator.copy(status = constants.Blockchain.ValidatorStatus.UNBONDED))
          val withdrawValidatorRewards = blockchainWithdrawAddresses.Utility.withdrawRewards(utilities.Bech32.convertOperatorAddressToAccountAddress(unbondingValidator.operatorAddress))

          for {
            _ <- updateOrDeleteValidator
            _ <- withdrawValidatorRewards
          } yield ()

        } else Future()
      })

      // Unbonding delegations updated in separate onNewBlock of Undelegations table

      (for {
        unbondingValidators <- unbondingValidators
        _ <- checkAndUpdateUnbondingValidators(unbondingValidators)
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    //TODO Can be optimized whenever called?
    def updateActiveValidatorSet(): Future[Unit] = {
      val explorerAllActiveValidators = Service.getAllActiveValidatorList.map(_.map(_.operatorAddress))
      val bcAllActiveValidators = getBondedValidators.Service.get().map(_.validators.map(_.operator_address))

      def checkAndUpdate(explorerAllActiveValidators: Seq[String], bcAllActiveValidators: Seq[String]) = utilitiesOperations.traverse(explorerAllActiveValidators.diff(bcAllActiveValidators) ++ bcAllActiveValidators.diff(explorerAllActiveValidators))(operatorAddress => insertOrUpdateValidator(operatorAddress))

      (for {
        explorerAllActiveValidators <- explorerAllActiveValidators
        bcAllActiveValidators <- bcAllActiveValidators
        _ <- checkAndUpdate(explorerAllActiveValidators = explorerAllActiveValidators, bcAllActiveValidators = bcAllActiveValidators)
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }
  }

}