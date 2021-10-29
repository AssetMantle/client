package dbActors

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Proposal}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object ProposalActor {
  def props(blockchainProposal: models.blockchain.Proposals) = Props(new ProposalActor(blockchainProposal))
}

@Singleton
class ProposalActor @Inject()(
                                 blockchainProposal: models.blockchain.Proposals
                               )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case InsertOrUpdateProposal(proposal) => {
      blockchainProposal.Service.insertOrUpdate(proposal) pipeTo sender()
    }
    case TryGetProposal(id) => {
      blockchainProposal.Service.tryGet(id) pipeTo sender()
    }
    case GetProposalWithActor(id) => {
      blockchainProposal.Service.get(id) pipeTo sender()
    }
    case GetLatestProposalID() => {
      blockchainProposal.Service.getLatestProposalID pipeTo sender()
    }
    case GetAllActiveProposals(time) => {
      blockchainProposal.Service.getAllActiveProposals(time) pipeTo sender()
    }
    case GetAllInActiveProposals(time) => {
      blockchainProposal.Service.getAllInactiveProposals(time) pipeTo sender()
    }
    case DeleteProposal(id) => {
      blockchainProposal.Service.delete(id) pipeTo sender()
    }
    case GetProposals() => {
      blockchainProposal.Service.get() pipeTo sender()
    }
  }

}

case class TryGetProposal(id: Int)
case class InsertOrUpdateProposal(proposal: Proposal)
case class GetAllProposal()
case class GetProposalWithActor(id: Int)
case class GetLatestProposalID()
case class GetAllActiveProposals(time: String)
case class GetAllInActiveProposals(time: String)
case class GetProposals()
case class DeleteProposal(id: Int)

