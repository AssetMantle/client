package models.blockchain

import akka.pattern.{ask, pipe}
import akka.util.Timeout
import models.blockchain.Proposals.{DeleteProposal, GetAllActiveProposals, GetAllInActiveProposals, GetAllProposal, GetLatestProposalID, GetProposalWithActor, GetProposals, InsertOrUpdateProposal, ProposalActor, TryGetProposal}
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.{ShardRegion}
import constants.Actor.{NUMBER_OF_ENTITIES, NUMBER_OF_SHARDS}
import exceptions.BaseException
import models.Abstract.{ProposalContent, ShardedActorRegion}
import models.Trait.Logged
import models.common.ProposalContents._
import models.common.Serializable.{Coin, FinalTallyResult}
import models.common.TransactionMessages.{Deposit, SubmitProposal}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.blockchain.GetProposal
import queries.responses.blockchain.ProposalResponse.{Response => ProposalResponse}
import queries.responses.common.Header
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Proposal(id: Int, content: ProposalContent, status: String, finalTallyResult: FinalTallyResult, submitTime: String, depositEndTime: String, totalDeposit: Seq[Coin], votingStartTime: String, votingEndTime: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {

  def addDeposit(depositCoins: Seq[Coin]): Proposal = Proposal(
    id = id, content = content, status = status, finalTallyResult = finalTallyResult, submitTime = submitTime, depositEndTime = depositEndTime,
    totalDeposit = utilities.Blockchain.addCoins(totalDeposit, depositCoins),
    votingStartTime = votingStartTime, votingEndTime = votingEndTime)

  def isTotalDepositGTEMinimum(minimumDeposit: Seq[Coin]): Boolean = {
    if (minimumDeposit.isEmpty) {
      return true
    }

    if (totalDeposit.isEmpty) {
      return false
    }

    minimumDeposit.foreach(minimumCoin => {
      if (!totalDeposit.find(_.denom == minimumCoin.denom).fold(false)(_.amount >= minimumCoin.amount)) {
        return false
      }
    })
    true
  }

  def activateVotingPeriod(currentTime: String, votingPeriod: Long): Proposal = Proposal(
    id = id, content = content,
    status = constants.Blockchain.Proposal.Status.VOTING_PERIOD,
    finalTallyResult = finalTallyResult, submitTime = submitTime, depositEndTime = depositEndTime,
    totalDeposit = totalDeposit,
    votingStartTime = currentTime,
    votingEndTime = utilities.Date.addTime(currentTime, votingPeriod)
  )

  def isPassed: Boolean = status match {
    case constants.Blockchain.Proposal.Status.PASSED => true
    case _ => false
  }

}

@Singleton
class Proposals @Inject()(
                           protected val databaseConfigProvider: DatabaseConfigProvider,
                           getProposal: GetProposal,
                           configuration: Configuration,
                           utilitiesOperations: utilities.Operations,
                           blockchainParameters: Parameters,
                           blockchainBalances: Balances,
                           blockchainValidators: Validators,
                           blockchainTokens: Tokens,
                         )(implicit executionContext: ExecutionContext) extends ShardedActorRegion {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_PROPOSAL

  private val startingProposalID = configuration.get[Int]("blockchain.govStartingProposalID")

  import databaseConfig.profile.api._

  private[models] val proposalTable = TableQuery[ProposalTable]

  private val uniqueId: String = UUID.randomUUID().toString

  private implicit val timeout = Timeout(constants.Actor.ACTOR_ASK_TIMEOUT)

  override def idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@TryGetProposal(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateProposal(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllProposal(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetProposalWithActor(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetLatestProposalID(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllActiveProposals(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllInActiveProposals(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetProposals(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@DeleteProposal(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  override def shardResolver: ShardRegion.ExtractShardId = {
    case TryGetProposal(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateProposal(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllProposal(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetProposalWithActor(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetLatestProposalID(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllActiveProposals(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllInActiveProposals(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetProposals(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case DeleteProposal(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }

  override def regionName: String = "proposalRegion"

  override def props: Props = Proposals.props(Proposals.this)

  case class ProposalSerialized(id: Int, content: String, status: String, finalTallyResult: String, submitTime: String, depositEndTime: String, totalDeposit: String, votingStartTime: String, votingEndTime: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Proposal = Proposal(id = id, content = utilities.JSON.convertJsonStringToObject[ProposalContent](content), status = status, finalTallyResult = utilities.JSON.convertJsonStringToObject[FinalTallyResult](finalTallyResult), submitTime = submitTime, depositEndTime = depositEndTime, totalDeposit = utilities.JSON.convertJsonStringToObject[Seq[Coin]](totalDeposit), votingStartTime = votingStartTime, votingEndTime = votingEndTime, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(proposal: Proposal): ProposalSerialized = ProposalSerialized(id = proposal.id, content = Json.toJson(proposal.content).toString, status = proposal.status, finalTallyResult = Json.toJson(proposal.finalTallyResult).toString, submitTime = proposal.submitTime, depositEndTime = proposal.depositEndTime, totalDeposit = Json.toJson(proposal.totalDeposit).toString, votingStartTime = proposal.votingStartTime, votingEndTime = proposal.votingEndTime, createdBy = proposal.createdBy, createdOn = proposal.createdOn, createdOnTimeZone = proposal.createdOnTimeZone, updatedBy = proposal.updatedBy, updatedOn = proposal.updatedOn, updatedOnTimeZone = proposal.updatedOnTimeZone)

  private def add(proposal: Proposal): Future[Int] = db.run((proposalTable returning proposalTable.map(_.id) += serialize(proposal)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(proposal: Proposal): Future[Int] = db.run(proposalTable.insertOrUpdate(serialize(proposal)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def tryGetByID(id: Int): Future[ProposalSerialized] = db.run(proposalTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.PROPOSAL_NOT_FOUND, noSuchElementException)
    }
  }

  private def deleteByID(id: Int): Future[Int] = db.run(proposalTable.filter(_.id === id).delete)

  private def getByID(id: Int): Future[Option[ProposalSerialized]] = db.run(proposalTable.filter(_.id === id).result.headOption)

  private def getMaxProposalID: Future[Int] = db.run(proposalTable.map(_.id).max.result.asTry).map {
    case Success(result) => result.getOrElse(0)
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def getAllProposals: Future[Seq[ProposalSerialized]] = db.run(proposalTable.result)

  private[models] class ProposalTable(tag: Tag) extends Table[ProposalSerialized](tag, "Proposal_BC") {

    def * = (id, content, status, finalTallyResult, submitTime, depositEndTime, totalDeposit, votingStartTime, votingEndTime, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (ProposalSerialized.tupled, ProposalSerialized.unapply)

    def id = column[Int]("id", O.PrimaryKey)

    def content = column[String]("content")

    def status = column[String]("status")

    def finalTallyResult = column[String]("finalTallyResult")

    def submitTime = column[String]("submitTime")

    def depositEndTime = column[String]("depositEndTime")

    def totalDeposit = column[String]("totalDeposit")

    def votingStartTime = column[String]("votingStartTime")

    def votingEndTime = column[String]("votingEndTime")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def tryGetProposalWithActor(id: Int): Future[Proposal] = (actorRegion ? TryGetProposal(uniqueId, id)).mapTo[Proposal]

    def tryGet(id: Int): Future[Proposal] = tryGetByID(id).map(_.deserialize)

    def insertOrUpdateProposalWithActor(proposal: Proposal): Future[Int] = (actorRegion ? InsertOrUpdateProposal(uniqueId, proposal)).mapTo[Int]

    def insertOrUpdate(proposal: Proposal): Future[Int] = upsert(proposal)

    def getProposalWithActor(id: Int): Future[Option[Proposal]] = (actorRegion ? GetProposalWithActor(uniqueId, id)).mapTo[Option[Proposal]]

    def get(id: Int): Future[Option[Proposal]] = getByID(id).map(_.map(_.deserialize))

    def getLatestProposalIDWithActor: Future[Int] = (actorRegion ? GetLatestProposalID(uniqueId)).mapTo[Int]

    def getLatestProposalID: Future[Int] = getMaxProposalID

    def getAllActiveProposalsWithActor(time: String): Future[Seq[Proposal]] = (actorRegion ? GetAllActiveProposals(uniqueId, time)).mapTo[Seq[Proposal]]

    def getAllActiveProposals(time: String): Future[Seq[Proposal]] = getAllProposals.map(_.filter(x => x.votingEndTime != "" && utilities.Date.isMature(completionTimestamp = x.votingEndTime, currentTimeStamp = time)).map(_.deserialize))

    def getAllInActiveProposalsWithActor(time: String): Future[Seq[Proposal]] = (actorRegion ? GetAllInActiveProposals(uniqueId, time)).mapTo[Seq[Proposal]]

    def getAllInactiveProposals(time: String): Future[Seq[Proposal]] = getAllProposals.map(_.filter(x => x.depositEndTime != "" && utilities.Date.isMature(completionTimestamp = x.depositEndTime, currentTimeStamp = time)).map(_.deserialize))

    def deleteProposalsWithActor(id: Int): Future[Int] = (actorRegion ? DeleteProposal(uniqueId, id)).mapTo[Int]

    def delete(id: Int): Future[Int] = deleteByID(id)

    def getProposalsWithActor(): Future[Seq[Proposal]] = (actorRegion ? GetProposals(uniqueId)).mapTo[Seq[Proposal]]

    def get(): Future[Seq[Proposal]] = getAllProposals.map(_.map(_.deserialize))

  }

  object Utility {

    def onSubmitProposal(submitProposal: SubmitProposal)(implicit header: Header): Future[Unit] = {
      val latestProposalID = Service.getLatestProposalID

      def upsert(latestProposalID: Int) = insertOrUpdateProposal(latestProposalID + startingProposalID)

      (for {
        latestProposalID <- latestProposalID
        _ <- upsert(latestProposalID)
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.SUBMIT_PROPOSAL + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def insertOrUpdateProposal(id: Int): Future[Proposal] = {
      val proposalResponse = getProposal.Service.get(id)

      def upsert(proposalResponse: ProposalResponse) = Service.insertOrUpdate(proposalResponse.proposal.toSerializableProposal)

      (for {
        proposalResponse <- proposalResponse
        _ <- upsert(proposalResponse)
      } yield proposalResponse.proposal.toSerializableProposal).recover {
        case baseException: BaseException => throw baseException
      }
    }

    // WARNING: Risky to use due to state difference
    //    def tally(finalTallyResult: FinalTallyResult): Future[(Boolean, Boolean)] = {
    //      val governanceParameter = blockchainParameters.Service.tryGetGovernanceParameter
    //      val totalVotingPower = blockchainValidators.Service.getTotalVotingPower.map(x => BigDecimal(x.toMicroString))
    //      val totalBondedTokens = blockchainTokens.Service.getTotalBondedAmount.map(x => BigDecimal(x.toMicroString))
    //
    //      def getResults(governanceParameter: GovernanceParameter, totalVotingPower: BigDecimal, totalBondedTokens: BigDecimal): (Boolean, Boolean) = {
    //        // Maintain this order
    //        if (totalBondedTokens == 0) {
    //          return (false, false)
    //        }
    //
    //        if ((totalVotingPower / totalBondedTokens) < governanceParameter.quorum) {
    //          return (false, true)
    //        }
    //
    //        if ((totalVotingPower - finalTallyResult.abstain) == 0) {
    //          return (false, false)
    //        }
    //
    //        if ((finalTallyResult.noWithVeto / totalVotingPower) > governanceParameter.vetoThreshold) {
    //          return (false, true)
    //        }
    //
    //        if ((finalTallyResult.yes / (totalVotingPower - finalTallyResult.abstain)) > governanceParameter.threshold) {
    //          (true, false)
    //        }
    //        else {
    //          (false, false)
    //        }
    //      }
    //
    //      (for {
    //        governanceParameter <- governanceParameter
    //        totalVotingPower <- totalVotingPower
    //        totalBondedTokens <- totalBondedTokens
    //      } yield getResults(governanceParameter = governanceParameter, totalVotingPower = totalVotingPower, totalBondedTokens = totalBondedTokens)).recover {
    //        case baseException: BaseException => throw baseException
    //      }
    //    }

  }

}

object Proposals {
  def props(blockchainProposals: models.blockchain.Proposals) (implicit executionContext: ExecutionContext) = Props(new ProposalActor(blockchainProposals))

  @Singleton
  class ProposalActor @Inject()(
                                 blockchainProposals: models.blockchain.Proposals
                               ) (implicit executionContext: ExecutionContext) extends Actor with ActorLogging {
    private implicit val logger: Logger = Logger(this.getClass)

    override def receive: Receive = {
      case InsertOrUpdateProposal(_, proposal) => {
        blockchainProposals.Service.insertOrUpdate(proposal) pipeTo sender()
      }
      case TryGetProposal(_, id) => {
        blockchainProposals.Service.tryGet(id) pipeTo sender()
      }
      case GetProposalWithActor(_, id) => {
        blockchainProposals.Service.get(id) pipeTo sender()
      }
      case GetLatestProposalID(_) => {
        blockchainProposals.Service.getLatestProposalID pipeTo sender()
      }
      case GetAllActiveProposals(_, time) => {
        blockchainProposals.Service.getAllActiveProposals(time) pipeTo sender()
      }
      case GetAllInActiveProposals(_, time) => {
        blockchainProposals.Service.getAllInactiveProposals(time) pipeTo sender()
      }
      case DeleteProposal(_, id) => {
        blockchainProposals.Service.delete(id) pipeTo sender()
      }
      case GetProposals(_) => {
        blockchainProposals.Service.get() pipeTo sender()
      }
    }
  }

  case class TryGetProposal(uid: String, id: Int)
  case class InsertOrUpdateProposal(uid: String, proposal: Proposal)
  case class GetAllProposal(uid: String)
  case class GetProposalWithActor(uid: String, id: Int)
  case class GetLatestProposalID(uid: String)
  case class GetAllActiveProposals(uid: String, time: String)
  case class GetAllInActiveProposals(uid: String, time: String)
  case class GetProposals(uid: String)
  case class DeleteProposal(uid: String, id: Int)
}