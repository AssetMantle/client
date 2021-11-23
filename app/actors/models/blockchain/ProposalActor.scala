package actors.models.blockchain

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.blockchain.{Proposal}
import play.api.Logger
import javax.inject.{Inject, Singleton}
import constants.Actor.{NUMBER_OF_SHARDS, NUMBER_OF_ENTITIES}

object ProposalActor {
  def props(blockchainProposals: models.blockchain.Proposals) = Props(new ProposalActor(blockchainProposals))
  
  val idExtractor: ShardRegion.ExtractEntityId = {
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

  val shardResolver: ShardRegion.ExtractShardId = {
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
}

@Singleton
class ProposalActor @Inject()(
                                 blockchainProposals: models.blockchain.Proposals
                               )extends Actor with ActorLogging {
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

