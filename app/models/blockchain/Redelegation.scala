package models.blockchain

import com.cosmos.staking.{v1beta1 => stakingTx}
import exceptions.BaseException
import models.common.Serializable.RedelegationEntry
import models.traits.Logging
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.blockchain.GetDelegatorRedelegations
import queries.responses.blockchain.DelegatorRedelegationsResponse.{Response => DelegatorRedelegationsResponse}
import queries.responses.common.Header
import slick.jdbc.JdbcProfile
import utilities.Date.RFC3339
import utilities.MicroNumber

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Redelegation(delegatorAddress: String, validatorSourceAddress: String, validatorDestinationAddress: String, entries: Seq[RedelegationEntry], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging

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

  case class RedelegationSerialized(delegatorAddress: String, validatorSourceAddress: String, validatorDestinationAddress: String, entries: String, createdBy: Option[String], createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) {
    def deserialize: Redelegation = Redelegation(delegatorAddress = delegatorAddress, validatorSourceAddress = validatorSourceAddress, validatorDestinationAddress = validatorDestinationAddress, entries = utilities.JSON.convertJsonStringToObject[Seq[RedelegationEntry]](entries), createdBy = createdBy, createdOnMillisEpoch = createdOnMillisEpoch, updatedBy = updatedBy, updatedOnMillisEpoch = updatedOnMillisEpoch)
  }

  def serialize(redelegation: Redelegation): RedelegationSerialized = RedelegationSerialized(delegatorAddress = redelegation.delegatorAddress, validatorSourceAddress = redelegation.validatorSourceAddress, validatorDestinationAddress = redelegation.validatorDestinationAddress, entries = Json.toJson(redelegation.entries).toString, createdBy = redelegation.createdBy, createdOnMillisEpoch = redelegation.createdOnMillisEpoch, updatedBy = redelegation.updatedBy, updatedOnMillisEpoch = redelegation.updatedOnMillisEpoch)

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

  private def upsertMultiple(redelegations: Seq[Redelegation]): Future[Int] = db.run(DBIO.sequence(redelegations.map(redelegation => redelegationTable.insertOrUpdate(serialize(redelegation)))).asTry).map {
    case Success(result) => result.count(_ != 0)
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.REDELEGATION_UPSERT_FAILED, psqlException)
    }
  }

  private def findAllByValidatorSource(address: String): Future[Seq[RedelegationSerialized]] = db.run(redelegationTable.filter(_.validatorSourceAddress === address).result)

  private def findAllByDelegator(delegatorAddress: String): Future[Seq[RedelegationSerialized]] = db.run(redelegationTable.filter(_.delegatorAddress === delegatorAddress).result)

  private def findAll: Future[Seq[RedelegationSerialized]] = db.run(redelegationTable.result)

  private def deleteByAddresses(delegatorAddress: String, validatorSourceAddress: String, validatorDestinationAddress: String): Future[Int] = db.run(redelegationTable.filter(x => x.delegatorAddress === delegatorAddress && x.validatorSourceAddress === validatorSourceAddress && x.validatorDestinationAddress === validatorDestinationAddress).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.REDELEGATION_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.REDELEGATION_DELETE_FAILED, noSuchElementException)
    }
  }

  private def getByAddress(delegatorAddress: String, validatorSourceAddress: String, validatorDestinationAddress: String): Future[Option[RedelegationSerialized]] = db.run(redelegationTable.filter(x => x.delegatorAddress === delegatorAddress && x.validatorSourceAddress === validatorSourceAddress && x.validatorDestinationAddress === validatorDestinationAddress).result.headOption)

  private[models] class RedelegationTable(tag: Tag) extends Table[RedelegationSerialized](tag, "Redelegation") {

    def * = (delegatorAddress, validatorSourceAddress, validatorDestinationAddress, entries, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (RedelegationSerialized.tupled, RedelegationSerialized.unapply)

    def delegatorAddress = column[String]("delegatorAddress", O.PrimaryKey)

    def validatorSourceAddress = column[String]("validatorSourceAddress", O.PrimaryKey)

    def validatorDestinationAddress = column[String]("validatorDestinationAddress", O.PrimaryKey)

    def entries = column[String]("entries")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")
  }

  object Service {

    def create(redelegation: Redelegation): Future[String] = add(redelegation)

    def insertMultiple(redelegations: Seq[Redelegation]): Future[Seq[String]] = addMultiple(redelegations)

    def insertOrUpdate(redelegation: Redelegation): Future[Int] = upsert(redelegation)

    def insertOrUpdateMultiple(redelegations: Seq[Redelegation]): Future[Int] = upsertMultiple(redelegations)

    def getAllBySourceValidator(address: String): Future[Seq[Redelegation]] = findAllByValidatorSource(address).map(_.map(_.deserialize))

    def getAllByDelegator(address: String): Future[Seq[Redelegation]] = findAllByDelegator(address).map(_.map(_.deserialize))

    def getAll: Future[Seq[Redelegation]] = findAll.map(_.map(_.deserialize))

    def delete(delegatorAddress: String, validatorSourceAddress: String, validatorDestinationAddress: String): Future[Int] = deleteByAddresses(delegatorAddress = delegatorAddress, validatorSourceAddress = validatorSourceAddress, validatorDestinationAddress = validatorDestinationAddress)

    def get(delegatorAddress: String, validatorSourceAddress: String, validatorDestinationAddress: String): Future[Option[Redelegation]] = getByAddress(delegatorAddress = delegatorAddress, validatorSourceAddress = validatorSourceAddress, validatorDestinationAddress = validatorDestinationAddress).map(_.map(_.deserialize))
  }

  object Utility {

    def onRedelegation(redelegate: stakingTx.MsgBeginRedelegate)(implicit header: Header): Future[String] = {
      val redelegationResponse = getDelegatorRedelegations.Service.getWithSourceAndDestinationValidator(delegatorAddress = redelegate.getDelegatorAddress, sourceValidatorAddress = redelegate.getValidatorSrcAddress, destinationValidatorAddress = redelegate.getValidatorDstAddress)
      val updateSrcValidatorDelegation = blockchainDelegations.Utility.upsertOrDelete(delegatorAddress = redelegate.getDelegatorAddress, validatorAddress = redelegate.getValidatorSrcAddress)
      val updateDstValidatorDelegation = blockchainDelegations.Utility.upsertOrDelete(delegatorAddress = redelegate.getDelegatorAddress, validatorAddress = redelegate.getValidatorDstAddress)
      val withdrawAddressBalanceUpdate = blockchainWithdrawAddresses.Utility.withdrawRewards(redelegate.getDelegatorAddress)
      val updateValidators = {
        val updateSrcValidatorResponse = blockchainValidators.Utility.insertOrUpdateValidator(redelegate.getValidatorSrcAddress)
        val updateDstValidatorResponse = blockchainValidators.Utility.insertOrUpdateValidator(redelegate.getValidatorDstAddress)
        for {
          _ <- updateSrcValidatorResponse
          _ <- updateDstValidatorResponse
        } yield ()
      }

      def upsertOrDeleteRedelegation(redelegationResponse: DelegatorRedelegationsResponse) = {
        if (redelegationResponse.redelegation_responses.nonEmpty) Service.insertOrUpdateMultiple(redelegationResponse.redelegation_responses.map(_.toRedelegation))
        else Service.delete(delegatorAddress = redelegate.getDelegatorAddress, validatorSourceAddress = redelegate.getValidatorSrcAddress, validatorDestinationAddress = redelegate.getValidatorDstAddress)
      }

      def updateActiveValidatorSet() = blockchainValidators.Utility.updateActiveValidatorSet()

      (for {
        redelegationResponse <- redelegationResponse
        _ <- upsertOrDeleteRedelegation(redelegationResponse)
        _ <- updateSrcValidatorDelegation
        _ <- updateDstValidatorDelegation
        _ <- withdrawAddressBalanceUpdate
        _ <- updateValidators
        _ <- updateActiveValidatorSet()
      } yield redelegate.getDelegatorAddress).recover {
        case _: BaseException => logger.error(schema.constants.Messages.REDELEGATE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          redelegate.getDelegatorAddress
      }
    }

    def onRedelegationCompletionEvent(delegator: String, srcValidator: String, dstValidator: String, currentBlockTimeStamp: RFC3339): Future[Unit] = {
      val redelegation = Service.get(delegatorAddress = delegator, validatorSourceAddress = srcValidator, validatorDestinationAddress = dstValidator)

      def updateOrDelete(optionalRedelegation: Option[Redelegation]) = optionalRedelegation.fold(Future(0)) { redelegation =>
        val updatedEntries = redelegation.entries.filterNot(_.isMature(currentBlockTimeStamp))
        if (updatedEntries.isEmpty) Service.delete(delegatorAddress = redelegation.delegatorAddress, validatorSourceAddress = srcValidator, validatorDestinationAddress = dstValidator)
        else Service.insertOrUpdate(redelegation.copy(entries = updatedEntries))
      }

      for {
        redelegation <- redelegation
        _ <- updateOrDelete(redelegation)
      } yield ()
    }

    def slashRedelegation(redelegation: Redelegation, infractionHeight: Int, currentBlockTIme: RFC3339, slashingFraction: BigDecimal): Future[MicroNumber] = {
      val delegation = blockchainDelegations.Service.get(delegatorAddress = redelegation.delegatorAddress, operatorAddress = redelegation.validatorDestinationAddress)
      val destinationValidator = blockchainValidators.Service.tryGet(redelegation.validatorDestinationAddress)

      def update(optionalDelegation: Option[Delegation], destinationValidator: Validator) = optionalDelegation.fold(Future(MicroNumber.zero))(delegation => {
        val updateEntries = utilitiesOperations.traverse(redelegation.entries)(entry => {
          val sharesToUnbond = slashingFraction * entry.sharesDestination
          val unbond = if (entry.creationHeight >= infractionHeight && !entry.isMature(currentBlockTIme) && sharesToUnbond != 0) {
            val slashShares = if (sharesToUnbond > delegation.shares) delegation.shares else sharesToUnbond
            blockchainUndelegations.Utility.unbond(delegation, destinationValidator, slashShares)
          } else Future(MicroNumber.zero)

          for (
            _ <- unbond
          ) yield MicroNumber((slashingFraction * BigDecimal(entry.initialBalance.value)).toBigInt)
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