package models.blockchain

import akka.actor.ActorSystem
import cosmos.staking.v1beta1.{Tx => stakingTx}
import exceptions.BaseException
import models.Trait.Logging
import models.common.Serializable.UndelegationEntry
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.blockchain.GetValidatorDelegatorUndelegation
import queries.responses.blockchain.ValidatorDelegatorUndelegationResponse.{Response => ValidatorDelegatorUndelegationResponse}
import queries.responses.common.Header
import slick.jdbc.JdbcProfile
import utilities.Date.RFC3339
import utilities.MicroNumber

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Undelegation(delegatorAddress: String, validatorAddress: String, entries: Seq[UndelegationEntry], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging

@Singleton
class Undelegations @Inject()(
                               actorSystem: ActorSystem,
                               protected val databaseConfigProvider: DatabaseConfigProvider,
                               configuration: Configuration,
                               getValidatorDelegatorUndelegation: GetValidatorDelegatorUndelegation,
                               blockchainValidators: Validators,
                               blockchainDelegations: Delegations,
                               blockchainWithdrawAddresses: WithdrawAddresses,
                               blockchainBalances: Balances,
                               utilitiesOperations: utilities.Operations,
                             )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_UNDELEGATION

  import databaseConfig.profile.api._

  private[models] val undelegationTable = TableQuery[UndelegationTable]

  case class UndelegationSerialized(delegatorAddress: String, validatorAddress: String, entries: String, createdBy: Option[String], createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) {
    def deserialize: Undelegation = Undelegation(delegatorAddress = delegatorAddress, validatorAddress = validatorAddress, entries = utilities.JSON.convertJsonStringToObject[Seq[UndelegationEntry]](entries), createdBy = createdBy, createdOnMillisEpoch = createdOnMillisEpoch, updatedBy = updatedBy, updatedOnMillisEpoch = updatedOnMillisEpoch)
  }

  def serialize(undelegation: Undelegation): UndelegationSerialized = UndelegationSerialized(delegatorAddress = undelegation.delegatorAddress, validatorAddress = undelegation.validatorAddress, entries = Json.toJson(undelegation.entries).toString, createdBy = undelegation.createdBy, createdOnMillisEpoch = undelegation.createdOnMillisEpoch, updatedBy = undelegation.updatedBy, updatedOnMillisEpoch = undelegation.updatedOnMillisEpoch)

  private def add(undelegation: Undelegation): Future[String] = db.run((undelegationTable returning undelegationTable.map(_.delegatorAddress) += serialize(undelegation)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.UNDELEGATION_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(undelegations: Seq[Undelegation]): Future[Seq[String]] = db.run((undelegationTable returning undelegationTable.map(_.delegatorAddress) ++= undelegations.map(serialize)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.UNDELEGATION_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(undelegation: Undelegation): Future[Int] = db.run(undelegationTable.insertOrUpdate(serialize(undelegation)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.UNDELEGATION_UPSERT_FAILED, psqlException)
    }
  }

  private def findAllByDelegator(delegatorAddress: String): Future[Seq[UndelegationSerialized]] = db.run(undelegationTable.filter(_.delegatorAddress === delegatorAddress).result)

  private def findAllByValidator(validatorAddress: String): Future[Seq[UndelegationSerialized]] = db.run(undelegationTable.filter(_.validatorAddress === validatorAddress).result)

  private def findAll: Future[Seq[UndelegationSerialized]] = db.run(undelegationTable.result)

  private def deleteByAddress(delegatorAddress: String, validatorAddress: String): Future[Int] = db.run(undelegationTable.filter(x => x.delegatorAddress === delegatorAddress && x.validatorAddress === validatorAddress).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.UNDELEGATION_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.UNDELEGATION_DELETE_FAILED, noSuchElementException)
    }
  }

  private def tryGetByAddress(delegatorAddress: String, validatorAddress: String): Future[UndelegationSerialized] = db.run(undelegationTable.filter(x => x.delegatorAddress === delegatorAddress && x.validatorAddress === validatorAddress).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.UNDELEGATION_NOT_FOUND, noSuchElementException)
    }
  }

  private[models] class UndelegationTable(tag: Tag) extends Table[UndelegationSerialized](tag, "Undelegation") {

    def * = (delegatorAddress, validatorAddress, entries, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (UndelegationSerialized.tupled, UndelegationSerialized.unapply)

    def delegatorAddress = column[String]("delegatorAddress", O.PrimaryKey)

    def validatorAddress = column[String]("validatorAddress", O.PrimaryKey)

    def entries = column[String]("entries")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")
  }

  object Service {

    def create(undelegation: Undelegation): Future[String] = add(undelegation)

    def insertMultiple(undelegations: Seq[Undelegation]): Future[Seq[String]] = addMultiple(undelegations)

    def insertOrUpdate(undelegation: Undelegation): Future[Int] = upsert(undelegation)

    def getAllByDelegator(address: String): Future[Seq[Undelegation]] = findAllByDelegator(address).map(_.map(_.deserialize))

    def getAllByValidator(address: String): Future[Seq[Undelegation]] = findAllByValidator(address).map(_.map(_.deserialize))

    def getAll: Future[Seq[Undelegation]] = findAll.map(_.map(_.deserialize))

    def delete(delegatorAddress: String, validatorAddress: String): Future[Int] = deleteByAddress(delegatorAddress = delegatorAddress, validatorAddress = validatorAddress)

    def tryGet(delegatorAddress: String, validatorAddress: String): Future[Undelegation] = tryGetByAddress(delegatorAddress = delegatorAddress, validatorAddress = validatorAddress).map(_.deserialize)

  }

  object Utility {

    def onUndelegation(undelegate: stakingTx.MsgUndelegate)(implicit header: Header): Future[String] = {
      val undelegationsResponse = getValidatorDelegatorUndelegation.Service.get(delegatorAddress = undelegate.getDelegatorAddress, validatorAddress = undelegate.getValidatorAddress)
      val updateOrDeleteDelegation = blockchainDelegations.Utility.updateOrDelete(delegatorAddress = undelegate.getDelegatorAddress, validatorAddress = undelegate.getValidatorAddress)
      val updateValidator = blockchainValidators.Utility.insertOrUpdateValidator(undelegate.getValidatorAddress)
      val withdrawAddressBalanceUpdate = blockchainWithdrawAddresses.Utility.withdrawRewards(undelegate.getDelegatorAddress)

      def upsertUndelegation(undelegationsResponse: ValidatorDelegatorUndelegationResponse) = Service.insertOrUpdate(undelegationsResponse.unbond.toUndelegation)

      def updateActiveValidatorSet() = blockchainValidators.Utility.updateActiveValidatorSet()

      (for {
        undelegationsResponse <- undelegationsResponse
        _ <- updateValidator
        _ <- upsertUndelegation(undelegationsResponse)
        _ <- withdrawAddressBalanceUpdate
        _ <- updateOrDeleteDelegation
        _ <- updateActiveValidatorSet()
      } yield undelegate.getDelegatorAddress).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.UNDELEGATE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          undelegate.getDelegatorAddress
      }
    }

    def onUnbondingCompletionEvent(delegatorAddress: String, validatorAddress: String, currentBlockTimeStamp: RFC3339): Future[Unit] = {
      val updateBalance = blockchainBalances.Utility.insertOrUpdateBalance(delegatorAddress)
      val undelegation = Service.tryGet(delegatorAddress = delegatorAddress, validatorAddress = validatorAddress)

      def updateOrDelete(undelegation: Undelegation) = {
        val updatedEntries = undelegation.entries.filterNot(_.isMature(currentBlockTimeStamp))
        if (updatedEntries.isEmpty) Service.delete(delegatorAddress = undelegation.delegatorAddress, validatorAddress = undelegation.validatorAddress)
        else Service.insertOrUpdate(undelegation.copy(entries = updatedEntries))
      }

      (for {
        _ <- updateBalance
        undelegation <- undelegation
        _ <- updateOrDelete(undelegation)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def slashUndelegation(undelegation: Undelegation, currentBlockTime: RFC3339, infractionHeight: Int, slashingFraction: BigDecimal): Future[Unit] = {
      val updatedEntries = undelegation.entries.map(entry => {
        val slashAmount = MicroNumber((slashingFraction * BigDecimal(entry.initialBalance.value)).toBigInt)
        val unbondingSlashAmount = if (slashAmount < entry.balance) slashAmount else entry.balance
        if (entry.creationHeight >= infractionHeight && !entry.isMature(currentBlockTime) && unbondingSlashAmount != MicroNumber.zero) {
          entry.copy(balance = entry.balance - unbondingSlashAmount)
        } else entry
      })
      val updateUndelegation = Service.insertOrUpdate(undelegation.copy(entries = updatedEntries))

      (for {
        _ <- updateUndelegation
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def unbond(delegation: Delegation, validator: Validator, shares: BigDecimal): Future[MicroNumber] = {
      if (delegation.validatorAddress == validator.operatorAddress && delegation.shares >= shares) {
        val isDelegatorValidator = utilities.Crypto.convertOperatorAddressToAccountAddress(validator.operatorAddress) == delegation.delegatorAddress

        val withdrawDelegatorRewards = blockchainWithdrawAddresses.Utility.withdrawRewards(delegation.delegatorAddress)

        val jailValidator = if (isDelegatorValidator && !validator.jailed && validator.getTokensFromShares(delegation.shares) < validator.minimumSelfDelegation) {
          blockchainValidators.Service.jailValidator(validator.operatorAddress)
        } else Future(0)

        val updateOrDeleteDelegation = if (delegation.shares == 0) blockchainDelegations.Service.delete(delegatorAddress = delegation.delegatorAddress, validatorAddress = delegation.validatorAddress)
        else blockchainDelegations.Service.insertOrUpdate(delegation.copy(shares = delegation.shares - shares))

        val (updatedValidator, removedTokens) = validator.removeDelegatorShares(shares)
        val deleteValidator = updatedValidator.delegatorShares == 0 && validator.isUnbonded

        val deleteOrUpdateValidator = if (deleteValidator) blockchainValidators.Service.delete(validator.operatorAddress)
        else blockchainValidators.Service.insertOrUpdate(updatedValidator)

        val withdrawValidatorRewards = if (deleteValidator) blockchainWithdrawAddresses.Utility.withdrawRewards(utilities.Crypto.convertOperatorAddressToAccountAddress(delegation.validatorAddress)) else Future()

        (for {
          _ <- withdrawDelegatorRewards
          _ <- jailValidator
          _ <- updateOrDeleteDelegation
          _ <- deleteOrUpdateValidator
          _ <- withdrawValidatorRewards
        } yield removedTokens).recover {
          case baseException: BaseException => throw baseException
        }

      } else Future(throw new BaseException(constants.Response.INVALID_VALIDATOR_OR_DELEGATION_OR_SHARES))
    }

  }

}