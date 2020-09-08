package models.blockchain

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Serializable.RedelegationEntry
import models.common.TransactionMessages.Redelegate
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries._
import queries.responses.ValidatorDelegatorDelegationResponse.{Response => ValidatorDelegatorDelegationResponse}
import queries.responses.ValidatorDelegatorRedelegationsResponse.{Response => ValidatorDelegatorRedelegationsResponse}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Redelegation(delegatorAddress: String, validatorSourceAddress: String, validatorDestinationAddress: String, entries: Seq[RedelegationEntry], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Redelegations @Inject()(
                               blockchainDelegations: Delegations,
                               protected val databaseConfigProvider: DatabaseConfigProvider,
                               configuration: Configuration,
                               getValidatorDelegatorRedelegations: GetValidatorDelegatorRedelegations,
                               getValidatorDelegatorDelegation: GetValidatorDelegatorDelegation,
                               getValidator: GetValidator,
                               blockchainValidators: Validators,
                               blockchainWithdrawAddresses: WithdrawAddresses,
                             )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_REDELEGATION

  import databaseConfig.profile.api._

  private[models] val redelegationTable = TableQuery[RedelegationTable]

  case class RedelegationSerialized(delegatorAddress: String, validatorSourceAddress: String, validatorDestinationAddress: String, serializedEntries: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Redelegation = Redelegation(delegatorAddress = delegatorAddress, validatorSourceAddress = validatorSourceAddress, validatorDestinationAddress = validatorDestinationAddress, entries = utilities.JSON.convertJsonStringToObject[Seq[RedelegationEntry]](serializedEntries), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(redelegation: Redelegation): RedelegationSerialized = RedelegationSerialized(delegatorAddress = redelegation.delegatorAddress, validatorSourceAddress = redelegation.validatorSourceAddress, validatorDestinationAddress = redelegation.validatorDestinationAddress, serializedEntries = Json.toJson(redelegation.entries).toString, createdBy = redelegation.createdBy, createdOn = redelegation.createdOn, createdOnTimeZone = redelegation.createdOnTimeZone, updatedBy = redelegation.updatedBy, updatedOn = redelegation.updatedOn, updatedOnTimeZone = redelegation.updatedOnTimeZone)

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
      case psqlException: PSQLException => throw new BaseException(constants.Response.REDELEGATION_UPSERT_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.REDELEGATION_NOT_FOUND, noSuchElementException)
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

    def onRedelegation(redelegate: Redelegate): Future[Unit] = {
      val redelegationsResponse = getValidatorDelegatorRedelegations.Service.get
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

      def upsertRedelegation(redelegationsResponse: ValidatorDelegatorRedelegationsResponse) = Service.insertOrUpdate(redelegationsResponse.result.find(x => x.delegator_address == redelegate.delegatorAddress && x.validator_src_address == redelegate.validatorSrcAddress && x.validator_dst_address == redelegate.validatorDstAddress).getOrElse(throw new BaseException(constants.Response.REDELEGATION_RESPONSE_NOT_FOUND)).toRedelegation)

      (for {
        redelegationsResponse <- redelegationsResponse
        _ <- upsertRedelegation(redelegationsResponse)
        _ <- updateSrcValidatorDelegation
        _ <- updateDstValidatorDelegation
        _ <- withdrawAddressBalanceUpdate
        _ <- updateValidators
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onSlashingEvent(validatorAddress: String): Future[Unit] = {
      val redelegations = Service.getAllBySourceValidator(validatorAddress)

      def updateDelegations(redelegations: Seq[Redelegation]) = if (redelegations.nonEmpty) {
        val redelegationsResponse = getValidatorDelegatorRedelegations.Service.get

        def update(redelegationsResponse: ValidatorDelegatorRedelegationsResponse) = Future.traverse(redelegationsResponse.result.map(_.toRedelegation)) { redelegation =>
          val delegationResponse = getValidatorDelegatorDelegation.Service.get(delegatorAddress = redelegation.delegatorAddress, validatorAddress = redelegation.validatorDestinationAddress)

          def updateDelegation(delegationResponse: ValidatorDelegatorDelegationResponse) = blockchainDelegations.Service.insertOrUpdate(delegationResponse.result.toDelegation)

          for {
            delegationResponse <- delegationResponse
            _ <- updateDelegation(delegationResponse)
          } yield ()
        }

        for {
          redelegationsResponse <- redelegationsResponse
          _ <- update(redelegationsResponse)
        } yield ()
      } else {
        Future()
      }

      (for {
        redelegations <- redelegations
        _ <- updateDelegations(redelegations)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def updateOnNewBlock(blockTime: String): Future[Unit] = {
      val allRedelegations = Service.getAll

      def checkAndDelete(allRedelegations: Seq[Redelegation]) = Future.traverse(allRedelegations) { redelegation =>
        val updatedRedelegation = redelegation.copy(entries = redelegation.entries.filter(entry => utilities.Date.isMature(completionTimestamp = entry.completionTime, currentTimeStamp = blockTime)))
        val update = if (updatedRedelegation.entries.nonEmpty) Service.insertOrUpdate(updatedRedelegation) else Service.delete(delegatorAddress = updatedRedelegation.delegatorAddress, validatorSourceAddress = updatedRedelegation.validatorSourceAddress, validatorDestinationAddress = updatedRedelegation.validatorDestinationAddress)
        for {
          _ <- update
        } yield ()
      }

      (for {
        allRedelegations <- allRedelegations
        _ <- checkAndDelete(allRedelegations)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }

  }

}