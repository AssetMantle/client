package models.blockchain

import akka.pattern.{ask, pipe}
import akka.util.Timeout
import models.blockchain.ProposalDeposits.{DeleteByProposalDepositId, GetByProposalDepositId, GetProposalDepositWithActor, InsertOrUpdateProposalDeposit, ProposalDepositActor, TryGetProposalDeposit}
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.{ShardRegion}
import constants.Actor.{NUMBER_OF_ENTITIES, NUMBER_OF_SHARDS}
import exceptions.BaseException
import models.Abstract.ShardedActorRegion
import models.Trait.Logged
import models.common.Parameters.GovernanceParameter
import models.common.Serializable.Coin
import models.common.TransactionMessages.Deposit
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.blockchain.GetProposalDeposit
import queries.responses.blockchain.ProposalDepositResponse.{depositReads, Response => ProposalDepositResponse}
import queries.responses.common.Header
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ProposalDeposit(proposalID: Int, depositor: String, amount: Seq[Coin], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class ProposalDeposits @Inject()(
                                  protected val databaseConfigProvider: DatabaseConfigProvider,
                                  getProposalDeposit: GetProposalDeposit,
                                  configuration: Configuration,
                                  utilitiesOperations: utilities.Operations,
                                  blockchainBalances: Balances,
                                  blockchainProposals: Proposals,
                                  blockchainParameters: Parameters,
                                )(implicit executionContext: ExecutionContext) extends ShardedActorRegion {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_PROPOSAL_DEPOSIT

  import databaseConfig.profile.api._

  private[models] val proposalTable = TableQuery[ProposalDepositTable]

  private val uniqueId: String = UUID.randomUUID().toString

  private implicit val timeout = Timeout(constants.Actor.ACTOR_ASK_TIMEOUT)

  override def idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@TryGetProposalDeposit(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateProposalDeposit(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetProposalDepositWithActor(id, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@DeleteByProposalDepositId(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetByProposalDepositId(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  override def shardResolver: ShardRegion.ExtractShardId = {
    case TryGetProposalDeposit(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateProposalDeposit(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetProposalDepositWithActor(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case DeleteByProposalDepositId(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetByProposalDepositId(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }

  override def regionName: String = "proposalDepositRegion"

  override def props: Props = ProposalDeposits.props(ProposalDeposits.this)

  case class ProposalDepositSerialized(proposalID: Int, depositor: String, amount: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: ProposalDeposit = ProposalDeposit(proposalID = proposalID, depositor = depositor, amount = utilities.JSON.convertJsonStringToObject[Seq[Coin]](amount), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(proposalDeposit: ProposalDeposit): ProposalDepositSerialized = ProposalDepositSerialized(proposalID = proposalDeposit.proposalID, depositor = proposalDeposit.depositor, amount = Json.toJson(proposalDeposit.amount).toString, createdBy = proposalDeposit.createdBy, createdOn = proposalDeposit.createdOn, createdOnTimeZone = proposalDeposit.createdOnTimeZone, updatedBy = proposalDeposit.updatedBy, updatedOn = proposalDeposit.updatedOn, updatedOnTimeZone = proposalDeposit.updatedOnTimeZone)

  private def add(proposalDeposit: ProposalDeposit): Future[Int] = db.run((proposalTable returning proposalTable.map(_.proposalID) += serialize(proposalDeposit)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(proposalDeposit: ProposalDeposit): Future[Int] = db.run(proposalTable.insertOrUpdate(serialize(proposalDeposit)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def tryGetByID(proposalID: Int): Future[ProposalDepositSerialized] = db.run(proposalTable.filter(_.proposalID === proposalID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getByIDAndDepositor(proposalID: Int, depositor: String): Future[Option[ProposalDepositSerialized]] = db.run(proposalTable.filter(x => x.proposalID === proposalID && x.depositor === depositor).result.headOption)

  private def getByID(proposalID: Int): Future[Seq[ProposalDepositSerialized]] = db.run(proposalTable.filter(_.proposalID === proposalID).result)

  private def deleteByID(proposalID: Int): Future[Int] = db.run(proposalTable.filter(_.proposalID === proposalID).delete)

  private[models] class ProposalDepositTable(tag: Tag) extends Table[ProposalDepositSerialized](tag, "ProposalDeposit_BC") {

    def * = (proposalID, depositor, amount, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (ProposalDepositSerialized.tupled, ProposalDepositSerialized.unapply)

    def proposalID = column[Int]("proposalID", O.PrimaryKey)

    def depositor = column[String]("depositor", O.PrimaryKey)

    def amount = column[String]("amount")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def tryGetProposalWithActor(proposalID: Int): Future[ProposalDeposit] = (actorRegion ? TryGetProposalDeposit(uniqueId, proposalID)).mapTo[ProposalDeposit]

    def tryGet(proposalID: Int): Future[ProposalDeposit] = tryGetByID(proposalID).map(_.deserialize)

    def insertOrUpdateProposalWithActor(proposalDeposit: ProposalDeposit): Future[Int] = (actorRegion ? InsertOrUpdateProposalDeposit(uniqueId, proposalDeposit)).mapTo[Int]

    def insertOrUpdate(proposalDeposit: ProposalDeposit): Future[Int] = upsert(proposalDeposit)

    def getProposalWithActor(proposalID: Int, depositor: String): Future[Option[ProposalDeposit]] = (actorRegion ? GetProposalDepositWithActor(uniqueId, proposalID, depositor)).mapTo[Option[ProposalDeposit]]

    def get(proposalID: Int, depositor: String): Future[Option[ProposalDeposit]] = getByIDAndDepositor(proposalID = proposalID, depositor = depositor).map(_.map(_.deserialize))

    def getByProposalIDWithActor(proposalID: Int): Future[Seq[ProposalDeposit]] = (actorRegion ? GetByProposalDepositId(uniqueId, proposalID)).mapTo[Seq[ProposalDeposit]]

    def getByProposalID(proposalID: Int): Future[Seq[ProposalDeposit]] = getByID(proposalID).map(_.map(_.deserialize))

    def deleteByProposalIDWithActor(proposalID: Int): Future[Seq[ProposalDeposit]] = (actorRegion ? DeleteByProposalDepositId(uniqueId, proposalID)).mapTo[Seq[ProposalDeposit]]

    def deleteByProposalID(proposalID: Int): Future[Int] = deleteByID(proposalID)

  }

  object Utility {

    def onDeposit(deposit: Deposit)(implicit header: Header): Future[Unit] = {
      val proposal = blockchainProposals.Service.tryGet(deposit.proposalID)
      val governanceParameters = blockchainParameters.Service.tryGetGovernanceParameter
      val proposalDeposit = Service.get(deposit.proposalID, deposit.depositor)

      def updateAccountBalance() = blockchainBalances.Utility.insertOrUpdateBalance(deposit.depositor)

      def updateProposal(proposal: Proposal, governanceParameters: GovernanceParameter) = {
        if (proposal.status == constants.Blockchain.Proposal.Status.DEPOSIT_PERIOD && proposal.isTotalDepositGTEMinimum(governanceParameters.minDeposit)) {
          blockchainProposals.Service.insertOrUpdate(proposal.addDeposit(deposit.amount).activateVotingPeriod(currentTime = header.time, votingPeriod = governanceParameters.votingPeriod))
        } else blockchainProposals.Service.insertOrUpdate(proposal.addDeposit(deposit.amount))
      }

      def upsertDeposit(proposalDeposit: Option[ProposalDeposit]) = proposalDeposit.fold(Service.insertOrUpdate(ProposalDeposit(proposalID = deposit.proposalID, depositor = deposit.depositor, amount = deposit.amount)))(x => Service.insertOrUpdate(x.copy(amount = utilities.Blockchain.addCoins(oldCoins = x.amount, add = deposit.amount))))

      (for {
        proposal <- proposal
        governanceParameters <- governanceParameters
        proposalDeposit <- proposalDeposit
        _ <- updateAccountBalance()
        _ <- updateProposal(proposal, governanceParameters)
        _ <- upsertDeposit(proposalDeposit)
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.DEPOSIT + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def burnOrRefundDeposits(proposalID: Int): Future[Unit] = {
      val deposits = Service.getByProposalID(proposalID)

      def updateAll(deposits: Seq[ProposalDeposit]) = utilitiesOperations.traverse(deposits)(deposit => blockchainBalances.Utility.insertOrUpdateBalance(deposit.depositor))

      (for {
        deposits <- deposits
        _ <- updateAll(deposits)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }
  }

}

object ProposalDeposits {
  def props(blockchainProposalDeposits: models.blockchain.ProposalDeposits) (implicit executionContext: ExecutionContext) = Props(new ProposalDepositActor(blockchainProposalDeposits))

  @Singleton
  class ProposalDepositActor @Inject()(
                                        blockchainProposalDeposits: models.blockchain.ProposalDeposits
                                      ) (implicit executionContext: ExecutionContext) extends Actor with ActorLogging {
    private implicit val logger: Logger = Logger(this.getClass)

    override def receive: Receive = {
      case InsertOrUpdateProposalDeposit(_, proposal) => {
        blockchainProposalDeposits.Service.insertOrUpdate(proposal) pipeTo sender()
      }
      case TryGetProposalDeposit(_, proposalID) => {
        blockchainProposalDeposits.Service.tryGet(proposalID) pipeTo sender()
      }
      case GetProposalDepositWithActor(_, proposalID, depositor) => {
        blockchainProposalDeposits.Service.get(proposalID, depositor) pipeTo sender()
      }
      case DeleteByProposalDepositId(_, id) => {
        blockchainProposalDeposits.Service.deleteByProposalID(id) pipeTo sender()
      }
      case GetByProposalDepositId(_, id) => {
        blockchainProposalDeposits.Service.getByProposalID(id) pipeTo sender()
      }
    }

  }

  case class TryGetProposalDeposit(uid: String, proposalID: Int)
  case class InsertOrUpdateProposalDeposit(uid: String, proposalDeposit: ProposalDeposit)
  case class GetProposalDepositWithActor(uid: String, proposalID: Int, depositor: String)
  case class DeleteByProposalDepositId(uid: String, id: Int)
  case class GetByProposalDepositId(uid: String, id: Int)


}