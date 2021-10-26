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
      println(self.path)
    }
    case TryGetProposalDeposit(proposalID) => {
      blockchainProposalDeposit.Service.tryGet(proposalID) pipeTo sender()
      println(self.path)
    }
    case GetProposalDepositWithActor(proposalID, depositor) => {
      blockchainProposalDeposit.Service.get(proposalID, depositor) pipeTo sender()
      println(self.path)
    }

    case DeleteByProposalDepositId(id) => {
      blockchainProposalDeposit.Service.deleteByProposalID(id) pipeTo sender()
      println(self.path)
    }
    case GetByProposalDepositId(id) => {
      blockchainProposalDeposit.Service.getByProposalID(id) pipeTo sender()
      println(self.path)
    }

  }

}

case class TryGetProposalDeposit(proposalID: Int)
case class InsertOrUpdateProposalDeposit(proposalDeposit: ProposalDeposit)
case class GetProposalDepositWithActor(proposalID: Int, depositor: String)
case class DeleteByProposalDepositId(id: Int)
case class GetByProposalDepositId(id: Int)

