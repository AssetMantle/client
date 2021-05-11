package models.blockchain

import java.sql.Timestamp
import exceptions.BaseException

import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Parameters.SlashingParameter
import models.common.Serializable.RedelegationEntry
import models.common.TransactionMessages.Redelegate
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.blockchain.{GetDelegatorRedelegations, GetValidatorDelegatorDelegation}
import queries.responses.blockchain.ValidatorDelegatorDelegationResponse.{Response => ValidatorDelegatorDelegationResponse}
import queries.responses.blockchain.DelegatorRedelegationsResponse.{Response => DelegatorRedelegationsResponse}
import queries.responses.common.Header
import slick.jdbc.JdbcProfile
import utilities.MicroNumber

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Redelegation(delegatorAddress: String, validatorSourceAddress: String, validatorDestinationAddress: String, entries: Seq[RedelegationEntry], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Redelegations @Inject()(
                               blockchainDelegations: Delegations,
                               protected val databaseConfigProvider: DatabaseConfigProvider,
                               configuration: Configuration,
                               getDelegatorRedelegations: GetDelegatorRedelegations,
                               blockchainValidators: Validators,
                               blockchainWithdrawAddresses: WithdrawAddresses,
                               blockchainParameters: Parameters,
                               blockchainUndelegations: Undelegations,
                               utilitiesOperations: utilities.Operations,
                             )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_REDELEGATION

  import databaseConfig.profile.api._

  private[models] val redelegationTable = TableQuery[RedelegationTable]

  case class RedelegationSerialized(delegatorAddress: String, validatorSourceAddress: String, validatorDestinationAddress: String, entries: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Redelegation = Redelegation(delegatorAddress = delegatorAddress, validatorSourceAddress = validatorSourceAddress, validatorDestinationAddress = validatorDestinationAddress, entries = utilities.JSON.convertJsonStringToObject[Seq[RedelegationEntry]](entries), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(redelegation: Redelegation): RedelegationSerialized = RedelegationSerialized(delegatorAddress = redelegation.delegatorAddress, validatorSourceAddress = redelegation.validatorSourceAddress, validatorDestinationAddress = redelegation.validatorDestinationAddress, entries = Json.toJson(redelegation.entries).toString, createdBy = redelegation.createdBy, createdOn = redelegation.createdOn, createdOnTimeZone = redelegation.createdOnTimeZone, updatedBy = redelegation.updatedBy, updatedOn = redelegation.updatedOn, updatedOnTimeZone = redelegation.updatedOnTimeZone)

  private def add(redelegation: Redelegation): Future[String] = db.run((redelegationTable returning redelegationTable.map(_.delegatorAddress) += serialize(redelegation)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.REDELEGATION_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(redelegations: Seq[Redelegation]): Future[Seq[String]] = db.run((redelegationTable returning redelegationTable.map(_.delegatorAddress) ++= redelegations.map(x => serialize(x))).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.REDELEGATION_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(redelegation: Redelegation): Future[Int] = db.run(redelegationTable.insertOrUpdate(serialize(redelegation)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.REDELEGATION_UPSERT_FAILED, psqlException)
    }
  }

  private def findAllByValidatorSource(address: String): Future[Seq[RedelegationSerialized]] = db.run(redelegationTable.filter(_.validatorSourceAddress === address).result)

  private def findAll: Future[Seq[RedelegationSerialized]] = db.run(redelegationTable.result)

  private def deleteByAddresses(delegatorAddress: String, validatorSourceAddress: String, validatorDestinationAddress: String): Future[Int] = db.run(redelegationTable.filter(x => x.delegatorAddress === delegatorAddress && x.validatorSourceAddress === validatorSourceAddress && x.validatorDestinationAddress === validatorDestinationAddress).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.REDELEGATION_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.REDELEGATION_DELETE_FAILED, noSuchElementException)
    }
  }

  private[models] class RedelegationTable(tag: Tag) extends Table[RedelegationSerialized](tag, "Redelegation") {

    def * = (delegatorAddress, validatorSourceAddress, validatorDestinationAddress, entries, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (RedelegationSerialized.tupled, RedelegationSerialized.unapply)

    def delegatorAddress = column[String]("delegatorAddress", O.PrimaryKey)

    def validatorSourceAddress = column[String]("validatorSourceAddress", O.PrimaryKey)

    def validatorDestinationAddress = column[String]("validatorDestinationAddress", O.PrimaryKey)

    def entries = column[String]("entries")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(redelegation: Redelegation): Future[String] = add(redelegation)

    def insertMultiple(redelegations: Seq[Redelegation]): Future[Seq[String]] = addMultiple(redelegations)

    def insertOrUpdate(redelegation: Redelegation): Future[Int] = upsert(redelegation)

    def getAllBySourceValidator(address: String): Future[Seq[Redelegation]] = findAllByValidatorSource(address).map(_.map(_.deserialize))

    def getAll: Future[Seq[Redelegation]] = findAll.map(_.map(_.deserialize))

    def delete(delegatorAddress: String, validatorSourceAddress: String, validatorDestinationAddress: String): Future[Int] = deleteByAddresses(delegatorAddress = delegatorAddress, validatorSourceAddress = validatorSourceAddress, validatorDestinationAddress = validatorDestinationAddress)

  }

  object Utility {

    def onRedelegation(redelegate: Redelegate)(implicit header: Header): Future[Unit] = {
      val redelegationResponse = getDelegatorRedelegations.Service.getWithSourceAndDestinationValidator(delegatorAddress = redelegate.delegatorAddress, sourceValidatorAddress = redelegate.validatorSrcAddress, destinationValidatorAddress = redelegate.validatorDstAddress)
      val updateSrcValidatorDelegation = blockchainDelegations.Utility.updateOrDelete(delegatorAddress = redelegate.delegatorAddress, validatorAddress = redelegate.validatorSrcAddress)
      val updateDstValidatorDelegation = blockchainDelegations.Utility.insertOrUpdate(delegatorAddress = redelegate.delegatorAddress, validatorAddress = redelegate.validatorDstAddress)
      val withdrawAddressBalanceUpdate = blockchainWithdrawAddresses.Utility.withdrawRewards(redelegate.delegatorAddress)
      val updateValidators = {
        val updateSrcValidatorResponse = blockchainValidators.Utility.insertOrUpdateValidator(redelegate.validatorSrcAddress)
        val updateDstValidatorResponse = blockchainValidators.Utility.insertOrUpdateValidator(redelegate.validatorDstAddress)
        for {
          _ <- updateSrcValidatorResponse
          _ <- updateDstValidatorResponse
        } yield ()
      }

      def upsertRedelegation(redelegationResponse: DelegatorRedelegationsResponse) = redelegationResponse.redelegation_responses.headOption.map(x => Service.insertOrUpdate(x.toRedelegation)).getOrElse(Future(throw new BaseException(constants.Response.REDELEGATION_RESPONSE_NOT_FOUND)))

      def updateActiveValidatorSet() = blockchainValidators.Utility.updateActiveValidatorSet()

      (for {
        redelegationResponse <- redelegationResponse
        _ <- upsertRedelegation(redelegationResponse)
        _ <- updateSrcValidatorDelegation
        _ <- updateDstValidatorDelegation
        _ <- withdrawAddressBalanceUpdate
        _ <- updateValidators
        _ <- updateActiveValidatorSet()
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.REDELEGATE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def onRedelegationCompletionEvent(delegator: String, srcValidator: String, dstValidator: String): Future[Unit] = {
      val redelegationResponse = getDelegatorRedelegations.Service.getWithSourceAndDestinationValidator(delegatorAddress = delegator, sourceValidatorAddress = srcValidator, destinationValidatorAddress = dstValidator)

      def checkAndDelete(redelegationResponse: DelegatorRedelegationsResponse) = if (redelegationResponse.redelegation_responses.nonEmpty) Service.insertOrUpdate(redelegationResponse.redelegation_responses.head.toRedelegation)
      else Service.delete(delegatorAddress = delegator, validatorSourceAddress = srcValidator, validatorDestinationAddress = dstValidator)

      (for {
        redelegationResponse <- redelegationResponse
        _ <- checkAndDelete(redelegationResponse)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def slashRedelegation(redelegation: Redelegation, infractionHeight: Int, currentBlockTIme: String, slashingFraction: BigDecimal): Future[MicroNumber] = {
      val delegation = blockchainDelegations.Service.get(delegatorAddress = redelegation.delegatorAddress, operatorAddress = redelegation.validatorDestinationAddress)
      val destinationValidator = blockchainValidators.Service.tryGet(redelegation.validatorDestinationAddress)

      def update(optionalDelegation: Option[Delegation], destinationValidator: Validator) = optionalDelegation.fold(Future(MicroNumber.zero))(delegation => {
        val updateEntries = utilitiesOperations.traverse(redelegation.entries)(entry => {
          val sharesToUnbond = slashingFraction * entry.sharesDestination
          val unbond = if (!(entry.creationHeight < infractionHeight) || !utilities.Date.isMature(completionTimestamp = entry.completionTime, currentTimeStamp = currentBlockTIme) || !(sharesToUnbond < 0)) {
            val slashShares = if (sharesToUnbond > delegation.shares) delegation.shares else sharesToUnbond
            blockchainUndelegations.Utility.unbond(delegation, destinationValidator, slashShares)
          } else Future(MicroNumber.zero)

          for (
            _ <- unbond
          ) yield MicroNumber((slashingFraction * BigDecimal(entry.initialBalance.value)).toBigInt())
        })
        for (
          slashedAmounts <- updateEntries
        ) yield slashedAmounts.sum
      })

      for {
        delegation <- delegation
        destinationValidator <- destinationValidator
        totalSlashedAmount <- update(delegation, destinationValidator)
      } yield totalSlashedAmount
    }

  }

}