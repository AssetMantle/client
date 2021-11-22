package actors.blockchainModels

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Proposal, ProposalVote}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object ProposalVoteActor {
  def props(blockchainProposalVote: models.blockchain.ProposalVotes) = Props(new ProposalVoteActor(blockchainProposalVote))

  val numberOfEntities = 10
  val numberOfShards = 100

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@TryGetProposalVote(id, _) => (id, attempt)
    case attempt@InsertOrUpdateProposalVote(id, _) => (id, attempt)
    case attempt@GetAllByProposalVoteId(id, _) => (id, attempt)
  }
  val shardResolver: ShardRegion.ExtractShardId = {
    case TryGetProposalVote(id, _) => (id.hashCode % numberOfShards).toString
    case InsertOrUpdateProposalVote(id, _) => (id.hashCode % numberOfShards).toString
    case GetAllByProposalVoteId(id, _) => (id.hashCode % numberOfShards).toString
  }
}

@Singleton
class ProposalVoteActor @Inject()(
                                      blockchainProposalVote: models.blockchain.ProposalVotes
                                    )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case InsertOrUpdateProposalVote(_, proposal) => {
      blockchainProposalVote.Service.insertOrUpdate(proposal) pipeTo sender()
    }
    case TryGetProposalVote(_, proposalID) => {
      blockchainProposalVote.Service.tryGet(proposalID) pipeTo sender()
    }
    case GetAllByProposalVoteId(_, id) => {
      blockchainProposalVote.Service.getAllByID(id) pipeTo sender()
    }
  }

}

case class TryGetProposalVote(uid: String, proposalID: Int)
case class InsertOrUpdateProposalVote(uid: String, proposalVote: ProposalVote)
case class GetAllByProposalVoteId(uid: String, id: Int)

