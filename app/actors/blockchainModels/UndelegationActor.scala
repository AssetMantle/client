package actors.blockchainModels

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.blockchain.{Undelegation}
import play.api.Logger
import javax.inject.{Inject, Singleton}
import constants.Actor.{NUMBER_OF_SHARDS, NUMBER_OF_ENTITIES}

object UndelegationActor {
  def props(blockchainUndelegations: models.blockchain.Undelegations) = Props(new UndelegationActor(blockchainUndelegations))

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateUndelegation(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertMultipleUndelegation(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateUndelegation(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllUndelegationByDelegator(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllUndelegationByValidator(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllUndelegation(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@DeleteUndelegation(id, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetUndelegation(id, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateUndelegation(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertMultipleUndelegation(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateUndelegation(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllUndelegationByDelegator(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllUndelegationByValidator(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllUndelegation(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case DeleteUndelegation(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetUndelegation(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }
}

@Singleton
class UndelegationActor @Inject()(
                                   blockchainUndelegations: models.blockchain.Undelegations
                                 )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateUndelegation(_, undelegation) => {
      blockchainUndelegations.Service.create(undelegation) pipeTo sender()
    }
    case InsertMultipleUndelegation(_, undelegations) => {
      blockchainUndelegations.Service.insertMultiple(undelegations) pipeTo sender()
    }
    case InsertOrUpdateUndelegation(_, undelegation) => {
      blockchainUndelegations.Service.insertOrUpdate(undelegation) pipeTo sender()
    }
    case GetAllUndelegationByDelegator(_, address) => {
      blockchainUndelegations.Service.getAllByDelegator(address) pipeTo sender()
    }
    case GetAllUndelegationByValidator(_, address) => {
      blockchainUndelegations.Service.getAllByValidator(address) pipeTo sender()
    }
    case GetAllUndelegation(_) => {
      blockchainUndelegations.Service.getAll pipeTo sender()
    }
    case DeleteUndelegation(_, delegatorAddress, validatorAddress) => {
      blockchainUndelegations.Service.delete(delegatorAddress, validatorAddress) pipeTo sender()
    }
    case TryGetUndelegation(_, delegatorAddress, validatorAddress) => {
      blockchainUndelegations.Service.tryGet(delegatorAddress, validatorAddress) pipeTo sender()
    }
  }
}

case class CreateUndelegation(uid: String, undelegation: Undelegation)
case class InsertMultipleUndelegation(uid: String, undelegations: Seq[Undelegation])
case class InsertOrUpdateUndelegation(uid: String, undelegation: Undelegation)
case class GetAllUndelegationByDelegator(uid: String, address: String)
case class GetAllUndelegationByValidator(uid: String, address: String)
case class GetAllUndelegation(uid: String)
case class DeleteUndelegation(uid: String, delegatorAddress: String, validatorAddress: String)
case class TryGetUndelegation(uid: String, delegatorAddress: String, validatorAddress: String)


