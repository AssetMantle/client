package models.blockchain

import exceptions.BaseException
import models.Trait.Logged
import models.common.Serializable.{Commission, ValidatorDescription}
import models.common.TransactionMessages._
import models.{keyBase, masterTransaction}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries._
import slick.jdbc.JdbcProfile
import utilities.MicroNumber

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Validator(operatorAddress: String, hexAddress: String, consensusPublicKey: String, jailed: Boolean, status: Int, tokens: MicroNumber, delegatorShares: BigDecimal, description: ValidatorDescription, unbondingHeight: Option[Int], unbondingTime: Option[String], commission: Commission, minimumSelfDelegation: MicroNumber, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Validators @Inject()(
                            protected val databaseConfigProvider: DatabaseConfigProvider,
                            configuration: Configuration,
                            getValidator: GetValidator,
                            getBondedValidators: GetBondedValidators,
                            masterTransactionNotifications: masterTransaction.Notifications,
                            blockchainDelegations: Delegations,
                            blockchainAccounts: Accounts,
                            utilitiesOperations: utilities.Operations,
                            keyBaseValidatorAccounts: keyBase.ValidatorAccounts,
                            blockchainWithdrawAddresses: WithdrawAddresses,
                          )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_VALIDATOR

  private val bondedStatus = configuration.get[Int]("blockchain.validator.status.bonded")

  private val unbondedStatus = configuration.get[Int]("blockchain.validator.status.unbonded")

  private val unbondingStatus = configuration.get[Int]("blockchain.validator.status.unbonding")

  import databaseConfig.profile.api._

  private[models] val validatorTable = TableQuery[ValidatorTable]

  case class ValidatorSerialized(operatorAddress: String, hexAddress: String, consensusPublicKey: String, jailed: Boolean, status: Int, tokens: String, delegatorShares: BigDecimal, description: String, unbondingHeight: Option[Int], unbondingTime: Option[String], commission: String, minimumSelfDelegation: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Validator = Validator(operatorAddress = operatorAddress, hexAddress = hexAddress, consensusPublicKey = consensusPublicKey, status = status, jailed = jailed, tokens = new MicroNumber(tokens), delegatorShares = delegatorShares, description = utilities.JSON.convertJsonStringToObject[ValidatorDescription](description), unbondingHeight = unbondingHeight, unbondingTime = unbondingTime, commission = utilities.JSON.convertJsonStringToObject[Commission](commission), minimumSelfDelegation = new MicroNumber(minimumSelfDelegation), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(validator: Validator): ValidatorSerialized = ValidatorSerialized(operatorAddress = validator.operatorAddress, hexAddress = validator.hexAddress, consensusPublicKey = validator.consensusPublicKey, status = validator.status, jailed = validator.jailed, tokens = validator.tokens.toString, delegatorShares = validator.delegatorShares, description = Json.toJson(validator.description).toString, unbondingHeight = validator.unbondingHeight, unbondingTime = validator.unbondingTime, commission = Json.toJson(validator.commission).toString, minimumSelfDelegation = validator.minimumSelfDelegation.toString, createdBy = validator.createdBy, createdOn = validator.createdOn, createdOnTimeZone = validator.createdOnTimeZone, updatedBy = validator.updatedBy, updatedOn = validator.updatedOn, updatedOnTimeZone = validator.updatedOnTimeZone)

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

  private def getAllActiveValidators: Future[Seq[ValidatorSerialized]] = db.run(validatorTable.filter(_.status === bondedStatus).result)

  private def getAllInactiveValidators: Future[Seq[ValidatorSerialized]] = db.run(validatorTable.filterNot(_.status === bondedStatus).result)

  private def getValidatorsByOperatorAddresses(operatorAddresses: Seq[String]): Future[Seq[ValidatorSerialized]] = db.run(validatorTable.filter(_.operatorAddress.inSet(operatorAddresses)).result)

  private def checkExistsByOperatorAddress(operatorAddress: String): Future[Boolean] = db.run(validatorTable.filter(_.operatorAddress === operatorAddress).exists.result)

  private[models] class ValidatorTable(tag: Tag) extends Table[ValidatorSerialized](tag, "Validator") {

    def * = (operatorAddress, hexAddress, consensusPublicKey, jailed, status, tokens, delegatorShares, description, unbondingHeight.?, unbondingTime.?, commission, minimumSelfDelegation, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (ValidatorSerialized.tupled, ValidatorSerialized.unapply)

    def operatorAddress = column[String]("operatorAddress", O.PrimaryKey)

    def hexAddress = column[String]("hexAddress", O.Unique)

    def consensusPublicKey = column[String]("consensusPublicKey")

    def jailed = column[Boolean]("jailed")

    def status = column[Int]("status")

    def tokens = column[String]("tokens")

    def delegatorShares = column[BigDecimal]("delegatorShares")

    def description = column[String]("description")

    def unbondingHeight = column[Int]("unbondingHeight")

    def unbondingTime = column[String]("unbondingTime")

    def commission = column[String]("commission")

    def minimumSelfDelegation = column[String]("minimumSelfDelegation")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
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
      validator.description.moniker.getOrElse(validator.operatorAddress)
    }

    def getAll: Future[Seq[Validator]] = getAllValidators.map(_.map(_.deserialize))

    def getAllActiveValidatorList: Future[Seq[Validator]] = getAllActiveValidators.map(_.map(_.deserialize))

    def getAllInactiveValidatorList: Future[Seq[Validator]] = getAllInactiveValidators.map(_.map(_.deserialize))

    def getByOperatorAddresses(operatorAddresses: Seq[String]): Future[Seq[Validator]] = getValidatorsByOperatorAddresses(operatorAddresses).map(_.map(_.deserialize))

    def exists(operatorAddress: String): Future[Boolean] = checkExistsByOperatorAddress(operatorAddress)

  }

  object Utility {

    def onCreateValidator(createValidator: CreateValidator): Future[Unit] = {
      val upsertValidator = insertOrUpdateValidator(createValidator.validatorAddress)

      def updateOtherDetails() = {
        val insertDelegation = onDelegation(Delegate(delegatorAddress = utilities.Bech32.convertOperatorAddressToAccountAddress(createValidator.validatorAddress), validatorAddress = createValidator.validatorAddress, amount = createValidator.value))
        val addEvent = masterTransactionNotifications.Service.create(constants.Notification.VALIDATOR_CREATED, createValidator.description.moniker.getOrElse(createValidator.validatorAddress))(s"'${createValidator.validatorAddress}'")
        //        val insertKeyBaseAccount = keyBaseValidatorAccounts.Utility.insertOrUpdateKeyBaseAccount(createValidator.validatorAddress, createValidator.description.identity)

        for {
          _ <- insertDelegation
          _ <- addEvent
          //          _ <- insertKeyBaseAccount
        } yield ()
      }

      (for {
        _ <- upsertValidator
        _ <- updateOtherDetails()
        _ <- updateActiveValidatorSet()
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onEditValidator(editValidator: EditValidator): Future[Unit] = {
      val upsertValidator = insertOrUpdateValidator(editValidator.validatorAddress)

      def addEvent(validator: Validator) = masterTransactionNotifications.Service.create(constants.Notification.VALIDATOR_EDITED, validator.description.moniker.getOrElse(validator.operatorAddress))(s"'${validator.operatorAddress}'")

      //        def insertKeyBaseAccount(validator: Validator) = keyBaseValidatorAccounts.Utility.insertOrUpdateKeyBaseAccount(validator.operatorAddress, validator.description.identity)

      (for {
        validator <- upsertValidator
        _ <- addEvent(validator)
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onUnjail(unjail: Unjail): Future[Unit] = {
      val upsertValidator = insertOrUpdateValidator(unjail.address)

      def addEvent(validator: Validator) = masterTransactionNotifications.Service.create(constants.Notification.VALIDATOR_UNJAILED, validator.description.moniker.getOrElse(validator.operatorAddress))(s"'${validator.operatorAddress}'")

      (for {
        validator <- upsertValidator
        _ <- updateActiveValidatorSet()
        _ <- addEvent(validator)
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onDelegation(delegate: Delegate): Future[Unit] = {
      val updateValidator = insertOrUpdateValidator(delegate.validatorAddress)
      val accountBalance = blockchainAccounts.Utility.insertOrUpdateAccountBalance(delegate.delegatorAddress)
      val insertDelegation = blockchainDelegations.Utility.insertOrUpdate(delegatorAddress = delegate.delegatorAddress, validatorAddress = delegate.validatorAddress)
      val withdrawAddressBalanceUpdate = blockchainWithdrawAddresses.Utility.withdrawRewards(delegate.delegatorAddress)

      (for {
        _ <- updateValidator
        _ <- accountBalance
        _ <- insertDelegation
        _ <- withdrawAddressBalanceUpdate
        _ <- updateActiveValidatorSet()
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onWithdrawDelegationReward(withdrawDelegatorReward: WithdrawDelegatorReward): Future[Unit] = {
      val withdrawBalance = blockchainWithdrawAddresses.Utility.withdrawRewards(withdrawDelegatorReward.delegatorAddress)

      (for {
        _ <- withdrawBalance
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def onWithdrawValidatorCommission(withdrawValidatorCommission: WithdrawValidatorCommission): Future[Unit] = {
      val accountAddress = utilities.Bech32.convertOperatorAddressToAccountAddress(withdrawValidatorCommission.validatorAddress)
      //TODO
      val withdrawBalance = blockchainWithdrawAddresses.Utility.withdrawRewards(accountAddress)
      val updateAccountBalance = blockchainAccounts.Utility.insertOrUpdateAccountBalance(accountAddress)

      (for {
        _ <- withdrawBalance
        _ <- updateAccountBalance
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def insertOrUpdateValidator(validatorAddress: String): Future[Validator] = {
      val validatorResponse = getValidator.Service.get(validatorAddress)

      def insertValidator(validator: Validator) = Service.insertOrUpdate(validator)

      (for {
        validatorResponse <- validatorResponse
        _ <- insertValidator(validatorResponse.result.toValidator)
      } yield validatorResponse.result.toValidator
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def updateActiveValidatorSet(): Future[Unit] = {
      val explorerAllActiveValidators = Service.getAllActiveValidatorList.map(_.map(_.operatorAddress))
      val bcAllActiveValidators = getBondedValidators.Service.get().map(_.result.map(_.operator_address))

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