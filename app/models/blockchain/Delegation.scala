package models.blockchain

import exceptions.BaseException
import models.oldBlockchain
import models.traits.Logging
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import queries.blockchain.GetValidatorDelegatorDelegation
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Delegation(delegatorAddress: String, validatorAddress: String, shares: BigDecimal, createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging

@Singleton
class Delegations @Inject()(
                             protected val databaseConfigProvider: DatabaseConfigProvider,
                             oldBlockchainDelegations: oldBlockchain.Delegations,
                             getValidatorDelegatorDelegation: GetValidatorDelegatorDelegation,
                           )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_DELEGATION

  import databaseConfig.profile.api._

  private[models] val delegationTable = TableQuery[DelegationTable]

  private val notFoundRegex = """delegation.with.delegator.*not.found.for.validator.*""".r

  private def add(delegation: Delegation): Future[String] = db.run((delegationTable returning delegationTable.map(_.delegatorAddress) += delegation).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.DELEGATION_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(delegations: Seq[Delegation]): Future[Seq[String]] = db.run((delegationTable returning delegationTable.map(_.delegatorAddress) ++= delegations).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.DELEGATION_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(delegation: Delegation): Future[Int] = db.run(delegationTable.insertOrUpdate(delegation).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.DELEGATION_UPSERT_FAILED, psqlException)
    }
  }

  private def getAllByDelegatorAddress(delegatorAddress: String): Future[Seq[Delegation]] = db.run(delegationTable.filter(_.delegatorAddress === delegatorAddress).result)

  private def getAllByValidatorAddress(validatorAddress: String): Future[Seq[Delegation]] = db.run(delegationTable.filter(_.validatorAddress === validatorAddress).result)

  private def getByDelegatorAndValidatorAddress(delegatorAddress: String, validatorAddress: String): Future[Option[Delegation]] = db.run(delegationTable.filter(x => x.delegatorAddress === delegatorAddress && x.validatorAddress === validatorAddress).result.headOption)

  private def deleteByAddress(delegatorAddress: String, validatorAddress: String): Future[Int] = db.run(delegationTable.filter(x => x.delegatorAddress === delegatorAddress && x.validatorAddress === validatorAddress).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.DELEGATION_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.DELEGATION_DELETE_FAILED, noSuchElementException)
    }
  }

  private[models] class DelegationTable(tag: Tag) extends Table[Delegation](tag, "Delegation") {

    def * = (delegatorAddress, validatorAddress, shares, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (Delegation.tupled, Delegation.unapply)

    def delegatorAddress = column[String]("delegatorAddress", O.PrimaryKey)

    def validatorAddress = column[String]("validatorAddress", O.PrimaryKey)

    def shares = column[BigDecimal]("shares")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")
  }

  object Service {

    def create(delegation: Delegation): Future[String] = add(delegation)

    def insertMultiple(delegations: Seq[Delegation]): Future[Seq[String]] = addMultiple(delegations)

    def insertOrUpdate(delegation: Delegation): Future[Int] = upsert(delegation)

    def getAllForDelegator(address: String): Future[Seq[Delegation]] = getAllByDelegatorAddress(address)

    def getAllForValidator(operatorAddress: String): Future[Seq[Delegation]] = getAllByValidatorAddress(operatorAddress)

    def get(delegatorAddress: String, operatorAddress: String): Future[Option[Delegation]] = getByDelegatorAndValidatorAddress(delegatorAddress = delegatorAddress, validatorAddress = operatorAddress)

    def delete(delegatorAddress: String, validatorAddress: String): Future[Int] = deleteByAddress(delegatorAddress = delegatorAddress, validatorAddress = validatorAddress)

  }

  object Utility {

    //onDelegation moved to blockchain/Validators due to import cycle issues

    def insertOrUpdate(delegatorAddress: String, validatorAddress: String): Future[Unit] = {
      Future()
      //      val delegationResponse = getValidatorDelegatorDelegation.Service.get(delegatorAddress = delegatorAddress, validatorAddress = validatorAddress)
      //
      //      def insertDelegation(delegation: Delegation) = Service.insertOrUpdate(delegation)
      //
      //      (for {
      //        delegationResponse <- delegationResponse
      //        _ <- insertDelegation(delegationResponse.delegation_response.delegation.toDelegation)
      //      } yield ()).recover {
      //        // It's fine if responseErrorDelegationNotFound exception comes, happens when syncing from block 1
      //        case baseException: BaseException => if (notFoundRegex.findFirstIn(baseException.failure.message).isEmpty) throw baseException else logger.info(baseException.failure.logMessage)
      //      }
    }

    def updateOrDelete(delegatorAddress: String, validatorAddress: String): Future[Unit] = {
      val delegationResponse = getValidatorDelegatorDelegation.Service.get(delegatorAddress = delegatorAddress, validatorAddress = validatorAddress)

      def insertDelegation(delegation: Delegation) = Service.insertOrUpdate(delegation)

      (for {
        delegationResponse <- delegationResponse
        _ <- insertDelegation(delegationResponse.delegation_response.delegation.toDelegation)
      } yield ()).recover {
        case baseException: BaseException => if (notFoundRegex.findFirstIn(baseException.failure.message).isDefined) {
          val delete = Service.delete(delegatorAddress = delegatorAddress, validatorAddress = validatorAddress)
          (for {
            _ <- delete
          } yield ()
            ).recover {
            case baseException: BaseException => logger.info(baseException.failure.logMessage)
          }
        } else throw baseException
      }
    }
  }

}