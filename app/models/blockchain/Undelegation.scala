package models.blockchain

import java.sql.Timestamp
import akka.actor.ActorSystem
import exceptions.BaseException

import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Serializable.UndelegationEntry
import models.common.TransactionMessages.Undelegate
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.blockchain.{GetAllValidatorUndelegations, GetValidatorDelegatorUndelegations}
import queries.responses.blockchain.AllValidatorUndelegationsResponse.{Response => AllValidatorUndelegationsResponse}
import queries.responses.blockchain.ValidatorDelegatorUndelegationsResponse.{Response => ValidatorDelegatorUndelegationsResponse}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Undelegation(delegatorAddress: String, validatorAddress: String, entries: Seq[UndelegationEntry], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Undelegations @Inject()(
                               actorSystem: ActorSystem,
                               protected val databaseConfigProvider: DatabaseConfigProvider,
                               configuration: Configuration,
                               getValidatorDelegatorUndelegations: GetValidatorDelegatorUndelegations,
                               getAllValidatorUndelegations: GetAllValidatorUndelegations,
                               blockchainValidators: Validators,
                               blockchainDelegations: Delegations,
                               blockchainWithdrawAddresses: WithdrawAddresses,
                             )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_UNDELEGATION

  import databaseConfig.profile.api._

  private[models] val undelegationTable = TableQuery[UndelegationTable]

  case class UndelegationSerialized(delegatorAddress: String, validatorAddress: String, entries: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Undelegation = Undelegation(delegatorAddress = delegatorAddress, validatorAddress = validatorAddress, entries = utilities.JSON.convertJsonStringToObject[Seq[UndelegationEntry]](entries), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(undelegation: Undelegation): UndelegationSerialized = UndelegationSerialized(delegatorAddress = undelegation.delegatorAddress, validatorAddress = undelegation.validatorAddress, entries = Json.toJson(undelegation.entries).toString, createdBy = undelegation.createdBy, createdOn = undelegation.createdOn, createdOnTimeZone = undelegation.createdOnTimeZone, updatedBy = undelegation.updatedBy, updatedOn = undelegation.updatedOn, updatedOnTimeZone = undelegation.updatedOnTimeZone)

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

  private[models] class UndelegationTable(tag: Tag) extends Table[UndelegationSerialized](tag, "Undelegation") {

    def * = (delegatorAddress, validatorAddress, entries, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (UndelegationSerialized.tupled, UndelegationSerialized.unapply)

    def delegatorAddress = column[String]("delegatorAddress", O.PrimaryKey)

    def validatorAddress = column[String]("validatorAddress", O.PrimaryKey)

    def entries = column[String]("entries")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(undelegation: Undelegation): Future[String] = add(undelegation)

    def insertMultiple(undelegations: Seq[Undelegation]): Future[Seq[String]] = addMultiple(undelegations)

    def insertOrUpdate(undelegation: Undelegation): Future[Int] = upsert(undelegation)

    def getAllByDelegator(address: String): Future[Seq[Undelegation]] = findAllByDelegator(address).map(_.map(_.deserialize))

    def getAllByValidator(address: String): Future[Seq[Undelegation]] = findAllByValidator(address).map(_.map(_.deserialize))

    def getAll: Future[Seq[Undelegation]] = findAll.map(_.map(_.deserialize))

    def delete(delegatorAddress: String, validatorAddress: String): Future[Int] = deleteByAddress(delegatorAddress = delegatorAddress, validatorAddress = validatorAddress)

  }

  object Utility {

    def onUndelegation(undelegate: Undelegate): Future[Unit] = {
      val undelegationsResponse = getValidatorDelegatorUndelegations.Service.get(delegatorAddress = undelegate.delegatorAddress, validatorAddress = undelegate.validatorAddress)
      val updateOrDeleteDelegation = blockchainDelegations.Utility.updateOrDelete(delegatorAddress = undelegate.delegatorAddress, validatorAddress = undelegate.validatorAddress)
      val updateValidator = blockchainValidators.Utility.insertOrUpdateValidator(undelegate.validatorAddress)
      val withdrawAddressBalanceUpdate = blockchainWithdrawAddresses.Utility.withdrawRewards(undelegate.delegatorAddress)

      def upsertUndelegation(undelegationsResponse: ValidatorDelegatorUndelegationsResponse) = Service.insertOrUpdate(undelegationsResponse.result.toUndelegation)

      def updateActiveValidatorSet() = blockchainValidators.Utility.updateActiveValidatorSet()

      (for {
        undelegationsResponse <- undelegationsResponse
        _ <- updateValidator
        _ <- upsertUndelegation(undelegationsResponse)
        _ <- withdrawAddressBalanceUpdate
        _ <- updateOrDeleteDelegation
        _ <- updateActiveValidatorSet()
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onSlashingEvent(validatorAddress: String): Future[Unit] = {
      val undelegations = Service.getAllByValidator(validatorAddress)

      def updateUndelegations(undelegations: Seq[Undelegation]): Future[Unit] = if (undelegations.nonEmpty) {
        val allValidatorUndelegations = getAllValidatorUndelegations.Service.get(validatorAddress)

        def update(allValidatorUndelegations: AllValidatorUndelegationsResponse) = Future.traverse(undelegations)(undelegation => allValidatorUndelegations.result.find(_.delegator_address == undelegation.delegatorAddress).fold(Service.delete(delegatorAddress = undelegation.delegatorAddress, validatorAddress = undelegation.validatorAddress))(undelegationResponse => Service.insertOrUpdate(undelegationResponse.toUndelegation)))

        for {
          allValidatorUndelegations <- allValidatorUndelegations
          _ <- update(allValidatorUndelegations)
        } yield ()
      } else Future()

      (for {
        undelegations <- undelegations
        _ <- updateUndelegations(undelegations)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def onNewBlock(blockTime: String): Future[Unit] = {
      val allUndelegations = Service.getAll

      def checkAndDelete(allUndelegations: Seq[Undelegation]) = Future.traverse(allUndelegations) { undelegation =>
        val updatedUndelegation = undelegation.copy(entries = undelegation.entries.filterNot(entry => utilities.Date.isMature(completionTimestamp = entry.completionTime, currentTimeStamp = blockTime)))
        val update = if (updatedUndelegation.entries.nonEmpty) Service.insertOrUpdate(updatedUndelegation) else Service.delete(delegatorAddress = updatedUndelegation.delegatorAddress, validatorAddress = updatedUndelegation.validatorAddress)
        for {
          _ <- update
        } yield ()
      }

      (for {
        allUndelegations <- allUndelegations
        _ <- checkAndDelete(allUndelegations)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }

  }

}