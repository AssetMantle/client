package actors.models.blockchain

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.blockchain.{Delegation}
import play.api.Logger
import javax.inject.{Inject, Singleton}
import constants.Actor.{NUMBER_OF_SHARDS, NUMBER_OF_ENTITIES}

object DelegationActor {
  def props(blockchainDelegations: models.blockchain.Delegations) = Props(new DelegationActor(blockchainDelegations))
  
  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateDelegation(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertMultipleDelegation(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateDelegation(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllDelegationForDelegator(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllDelegationForValidator(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetDelegation(uid, _, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@DeleteDelegation(uid, _, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateDelegation(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertMultipleDelegation(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateDelegation(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllDelegationForDelegator(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllDelegationForValidator(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetDelegation(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case DeleteDelegation(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }
}

@Singleton
class DelegationActor @Inject()(
                                 blockchainDelegations: models.blockchain.Delegations
                               )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateDelegation(_, delegation) => {
      blockchainDelegations.Service.create(delegation) pipeTo sender()
    }
    case InsertMultipleDelegation(_, delegations) => {
      blockchainDelegations.Service.insertMultiple(delegations) pipeTo sender()
    }
    case InsertOrUpdateDelegation(_, delegation) => {
      blockchainDelegations.Service.insertOrUpdate(delegation) pipeTo sender()
    }
    case GetDelegation(_, delegatorAddress, operatorAddress) => {
      blockchainDelegations.Service.get(delegatorAddress, operatorAddress) pipeTo sender()
    }
    case GetAllDelegationForDelegator(_, address) => {
      blockchainDelegations.Service.getAllForDelegator(address) pipeTo sender()
    }
    case GetAllDelegationForValidator(_, operatorAddress) => {
      blockchainDelegations.Service.getAllForValidator(operatorAddress) pipeTo sender()
    }
    case DeleteDelegation(_, delegatorAddress, operatorAddress) => {
      blockchainDelegations.Service.delete(delegatorAddress, operatorAddress) pipeTo sender()
    }
  }
}

case class CreateDelegation(id: String, delegation: Delegation)
case class InsertMultipleDelegation(id: String, delegation: Seq[Delegation])
case class InsertOrUpdateDelegation(id: String, delegation: Delegation)
case class GetAllDelegationForDelegator(id: String, address: String)
case class GetAllDelegationForValidator(id: String, operatorAddress: String)
case class GetDelegation(id: String, delegatorAddress: String, operatorAddress: String)
case class DeleteDelegation(id: String, delegatorAddress: String, operatorAddress: String)
