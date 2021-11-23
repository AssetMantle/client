package actors.blockchainModels

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.blockchain.{ProposalDeposit}
import play.api.Logger
import javax.inject.{Inject, Singleton}
import constants.Actor.{NUMBER_OF_SHARDS, NUMBER_OF_ENTITIES}

object ProposalDepositActor {
  def props(blockchainProposalDeposits: models.blockchain.ProposalDeposits) = Props(new ProposalDepositActor(blockchainProposalDeposits))

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@TryGetProposalDeposit(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateProposalDeposit(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetProposalDepositWithActor(id, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@DeleteByProposalDepositId(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetByProposalDepositId(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case TryGetProposalDeposit(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateProposalDeposit(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetProposalDepositWithActor(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case DeleteByProposalDepositId(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetByProposalDepositId(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }
}

@Singleton
class ProposalDepositActor @Inject()(
                                      blockchainProposalDeposits: models.blockchain.ProposalDeposits
                             )extends Actor with ActorLogging {
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

