package actors.models.blockchain

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.blockchain.{ProposalVote}
import play.api.Logger
import javax.inject.{Inject, Singleton}
import constants.Actor.{NUMBER_OF_SHARDS, NUMBER_OF_ENTITIES}

object ProposalVoteActor {
  def props(blockchainProposalVotes: models.blockchain.ProposalVotes) = Props(new ProposalVoteActor(blockchainProposalVotes))

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@TryGetProposalVote(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateProposalVote(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllByProposalVoteId(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }
  val shardResolver: ShardRegion.ExtractShardId = {
    case TryGetProposalVote(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateProposalVote(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllByProposalVoteId(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }
}

@Singleton
class ProposalVoteActor @Inject()(
                                      blockchainProposalVotes: models.blockchain.ProposalVotes
                                    )extends Actor with ActorLogging {
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

