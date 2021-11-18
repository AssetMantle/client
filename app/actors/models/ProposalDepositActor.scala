package actors.models

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Proposal, ProposalDeposit}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object ProposalDepositActor {
  def props(blockchainProposalDeposit: models.blockchain.ProposalDeposits) = Props(new ProposalDepositActor(blockchainProposalDeposit))

  val numberOfEntities = 10
  val numberOfShards = 100

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@TryGetProposalDeposit(id, _) => (id, attempt)
    case attempt@InsertOrUpdateProposalDeposit(id, _) => (id, attempt)
    case attempt@GetProposalDepositWithActor(id, _, _) => (id, attempt)
    case attempt@DeleteByProposalDepositId(id, _) => (id, attempt)
    case attempt@GetByProposalDepositId(id, _) => (id, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case TryGetProposalDeposit(id, _) => (id.hashCode % numberOfShards).toString
    case InsertOrUpdateProposalDeposit(id, _) => (id.hashCode % numberOfShards).toString
    case GetProposalDepositWithActor(id, _, _) => (id.hashCode % numberOfShards).toString
    case DeleteByProposalDepositId(id, _) => (id.hashCode % numberOfShards).toString
    case GetByProposalDepositId(id, _) => (id.hashCode % numberOfShards).toString
  }
}

@Singleton
class ProposalDepositActor @Inject()(
                                      blockchainProposalDeposit: models.blockchain.ProposalDeposits
                             )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case InsertOrUpdateProposalDeposit(_, proposal) => {
      blockchainProposalDeposit.Service.insertOrUpdate(proposal) pipeTo sender()
    }
    case TryGetProposalDeposit(_, proposalID) => {
      blockchainProposalDeposit.Service.tryGet(proposalID) pipeTo sender()
    }
    case GetProposalDepositWithActor(_, proposalID, depositor) => {
      blockchainProposalDeposit.Service.get(proposalID, depositor) pipeTo sender()
    }
    case DeleteByProposalDepositId(_, id) => {
      blockchainProposalDeposit.Service.deleteByProposalID(id) pipeTo sender()
    }
    case GetByProposalDepositId(_, id) => {
      blockchainProposalDeposit.Service.getByProposalID(id) pipeTo sender()
    }
  }

}

case class TryGetProposalDeposit(uid: String, proposalID: Int)
case class InsertOrUpdateProposalDeposit(uid: String, proposalDeposit: ProposalDeposit)
case class GetProposalDepositWithActor(uid: String, proposalID: Int, depositor: String)
case class DeleteByProposalDepositId(uid: String, id: Int)
case class GetByProposalDepositId(uid: String, id: Int)

