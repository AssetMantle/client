package dbActors

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Proposal, ProposalVote}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object ProposalVoteActor {
  def props(blockchainProposalVote: models.blockchain.ProposalVotes) = Props(new ProposalVoteActor(blockchainProposalVote))
}

@Singleton
class ProposalVoteActor @Inject()(
                                      blockchainProposalVote: models.blockchain.ProposalVotes
                                    )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case InsertOrUpdateProposalVote(proposal) => {
      blockchainProposalVote.Service.insertOrUpdate(proposal) pipeTo sender()
    }
    case TryGetProposalVote(proposalID) => {
      blockchainProposalVote.Service.tryGet(proposalID) pipeTo sender()
    }
    case GetAllByProposalVoteId(id) => {
      blockchainProposalVote.Service.getAllByID(id) pipeTo sender()
    }
  }

}

case class TryGetProposalVote(proposalID: Int)
case class InsertOrUpdateProposalVote(proposalVote: ProposalVote)
case class GetAllByProposalVoteId(id: Int)

