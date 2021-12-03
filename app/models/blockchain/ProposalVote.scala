package models.blockchain

import akka.pattern.{ask, pipe}
import akka.util.Timeout
import models.blockchain.ProposalVotes.{GetAllByProposalVoteId, InsertOrUpdateProposalVote, ProposalVoteActor, TryGetProposalVote}
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.{ShardRegion}
import constants.Actor.{NUMBER_OF_ENTITIES, NUMBER_OF_SHARDS}
import exceptions.BaseException
import models.Abstract.ShardedActorRegion
import models.Trait.Logged
import models.common.TransactionMessages.Vote
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.blockchain.GetProposalVote
import queries.responses.blockchain.ProposalVoteResponse.{Response => ProposalVoteResponse}
import queries.responses.common.Header
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ProposalVote(proposalID: Int, voter: String, option: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class ProposalVotes @Inject()(
                               protected val databaseConfigProvider: DatabaseConfigProvider,
                               getProposalVote: GetProposalVote,
                               configuration: Configuration,
                               utilitiesOperations: utilities.Operations
                             )(implicit executionContext: ExecutionContext) extends ShardedActorRegion {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_PROPOSAL_VOTE

  import databaseConfig.profile.api._

  private[models] val proposalVoteTable = TableQuery[ProposalVoteTable]

  private val uniqueId: String = UUID.randomUUID().toString

  private implicit val timeout = Timeout(constants.Actor.ACTOR_ASK_TIMEOUT)

  override def idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@TryGetProposalVote(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateProposalVote(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllByProposalVoteId(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }
  override def shardResolver: ShardRegion.ExtractShardId = {
    case TryGetProposalVote(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateProposalVote(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllByProposalVoteId(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }

  override def regionName: String = "proposalVoteRegion"

  override def props: Props = ProposalVotes.props(ProposalVotes.this)
  
  private def add(proposalVote: ProposalVote): Future[Int] = db.run((proposalVoteTable returning proposalVoteTable.map(_.proposalID) += proposalVote).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(proposalVote: ProposalVote): Future[Int] = db.run(proposalVoteTable.insertOrUpdate(proposalVote).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def tryGetByID(proposalID: Int): Future[ProposalVote] = db.run(proposalVoteTable.filter(_.proposalID === proposalID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getByID(proposalID: Int): Future[Seq[ProposalVote]] = db.run(proposalVoteTable.filter(_.proposalID === proposalID).result)

  private[models] class ProposalVoteTable(tag: Tag) extends Table[ProposalVote](tag, "ProposalVote_BC") {

    def * = (proposalID, voter, option, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (ProposalVote.tupled, ProposalVote.unapply)

    def proposalID = column[Int]("proposalID", O.PrimaryKey)

    def voter = column[String]("voter", O.PrimaryKey)

    def option = column[String]("option")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def tryGetProposalVoteWithActor(proposalID: Int): Future[ProposalVote] = (actorRegion ? TryGetProposalVote(uniqueId, proposalID)).mapTo[ProposalVote]

    def tryGet(proposalID: Int): Future[ProposalVote] = tryGetByID(proposalID)

    def insertOrUpdateProposalVoteWithActor(proposalVote: ProposalVote): Future[ProposalVote] = (actorRegion ? InsertOrUpdateProposalVote(uniqueId, proposalVote)).mapTo[ProposalVote]

    def insertOrUpdate(proposalVote: ProposalVote): Future[Int] = upsert(proposalVote)

    def getAllProposalVoteByIDWithActor(proposalID: Int): Future[Seq[ProposalVote]] = (actorRegion ? GetAllByProposalVoteId(uniqueId, proposalID)).mapTo[Seq[ProposalVote]]

    def getAllByID(proposalID: Int): Future[Seq[ProposalVote]] = getByID(proposalID)
  }

  object Utility {

    def onVote(vote: Vote)(implicit header: Header): Future[Unit] = {
      val upsert = Service.insertOrUpdate(ProposalVote(proposalID = vote.proposalID, voter = vote.voter, option = vote.option))

      (for {
        _ <- upsert
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.VOTE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def insertOrUpdateProposal(proposalID: String, address: String): Future[Unit] = {
      val proposalVoteResponse = getProposalVote.Service.get(id = proposalID, address = address)

      def upsert(proposalVoteResponse: ProposalVoteResponse) = Service.insertOrUpdate(proposalVoteResponse.vote.toSerializableProposalVote)

      (for {
        proposalVoteResponse <- proposalVoteResponse
        _ <- upsert(proposalVoteResponse)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }
  }

}

object ProposalVotes {
  def props(blockchainProposalVotes: models.blockchain.ProposalVotes) (implicit executionContext: ExecutionContext) = Props(new ProposalVoteActor(blockchainProposalVotes))

  @Singleton
  class ProposalVoteActor @Inject()(
                                     blockchainProposalVotes: models.blockchain.ProposalVotes
                                   ) (implicit executionContext: ExecutionContext) extends Actor with ActorLogging {
    private implicit val logger: Logger = Logger(this.getClass)

    override def receive: Receive = {
      case InsertOrUpdateProposalVote(_, proposal) => {
        blockchainProposalVotes.Service.insertOrUpdate(proposal) pipeTo sender()
      }
      case TryGetProposalVote(_, proposalID) => {
        blockchainProposalVotes.Service.tryGet(proposalID) pipeTo sender()
      }
      case GetAllByProposalVoteId(_, id) => {
        blockchainProposalVotes.Service.getAllByID(id) pipeTo sender()
      }
    }
  }

  case class TryGetProposalVote(uid: String, proposalID: Int)
  case class InsertOrUpdateProposalVote(uid: String, proposalVote: ProposalVote)
  case class GetAllByProposalVoteId(uid: String, id: Int)
}