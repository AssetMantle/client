package dbActors

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Proposal, ProposalDeposit}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object ProposalDepositActor {
  def props(blockchainProposalDeposit: models.blockchain.ProposalDeposits) = Props(new ProposalDepositActor(blockchainProposalDeposit))
}

@Singleton
class ProposalDepositActor @Inject()(
                                      blockchainProposalDeposit: models.blockchain.ProposalDeposits
                             )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case InsertOrUpdateProposalDeposit(proposal) => {
      blockchainProposalDeposit.Service.insertOrUpdate(proposal) pipeTo sender()
    }
    case TryGetProposalDeposit(proposalID) => {
      blockchainProposalDeposit.Service.tryGet(proposalID) pipeTo sender()
    }
    case GetProposalDepositWithActor(proposalID, depositor) => {
      blockchainProposalDeposit.Service.get(proposalID, depositor) pipeTo sender()
    }
    case DeleteByProposalDepositId(id) => {
      blockchainProposalDeposit.Service.deleteByProposalID(id) pipeTo sender()
    }
    case GetByProposalDepositId(id) => {
      blockchainProposalDeposit.Service.getByProposalID(id) pipeTo sender()
    }
  }

}

case class TryGetProposalDeposit(proposalID: Int)
case class InsertOrUpdateProposalDeposit(proposalDeposit: ProposalDeposit)
case class GetProposalDepositWithActor(proposalID: Int, depositor: String)
case class DeleteByProposalDepositId(id: Int)
case class GetByProposalDepositId(id: Int)

