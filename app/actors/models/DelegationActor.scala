package actors.models

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Delegation}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object DelegationActor {
  def props(blockchainDelegation: models.blockchain.Delegations) = Props(new DelegationActor(blockchainDelegation))

  val numberOfEntities = 10
  val numberOfShards = 100


  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateDelegation(uid, _) => (uid, attempt)
    case attempt@InsertMultipleDelegation(uid, _) => (uid, attempt)
    case attempt@InsertOrUpdateDelegation(uid, _) => (uid, attempt)
    case attempt@GetAllDelegationForDelegator(uid, _) => (uid, attempt)
    case attempt@GetAllDelegationForValidator(uid, _) => (uid, attempt)
    case attempt@GetDelegation(uid, _, _) => (uid, attempt)
    case attempt@DeleteDelegation(uid, _, _) => (uid, attempt)

  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateDelegation(id, _) => (id.hashCode % numberOfShards).toString
    case InsertMultipleDelegation(id, _) => (id.hashCode % numberOfShards).toString
    case InsertOrUpdateDelegation(id, _) => (id.hashCode % numberOfShards).toString
    case GetAllDelegationForDelegator(id, _) => (id.hashCode % numberOfShards).toString
    case GetAllDelegationForValidator(id, _) => (id.hashCode % numberOfShards).toString
    case GetDelegation(id, _, _) => (id.hashCode % numberOfShards).toString
    case DeleteDelegation(id, _, _) => (id.hashCode % numberOfShards).toString
  }
}

@Singleton
class DelegationActor @Inject()(
                                 blockchainDelegation: models.blockchain.Delegations
                               )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateDelegation(_, delegation) => {
      blockchainDelegation.Service.create(delegation) pipeTo sender()
    }
    case InsertMultipleDelegation(_, delegations) => {
      blockchainDelegation.Service.insertMultiple(delegations) pipeTo sender()
    }
    case InsertOrUpdateDelegation(_, delegation) => {
      blockchainDelegation.Service.insertOrUpdate(delegation) pipeTo sender()
    }
    case GetDelegation(_, delegatorAddress, operatorAddress) => {
      blockchainDelegation.Service.get(delegatorAddress, operatorAddress) pipeTo sender()
    }

    case GetAllDelegationForDelegator(_, address) => {
      blockchainDelegation.Service.getAllForDelegator(address) pipeTo sender()
    }
    case GetAllDelegationForValidator(_, operatorAddress) => {
      blockchainDelegation.Service.getAllForValidator(operatorAddress) pipeTo sender()
    }
    case DeleteDelegation(_, delegatorAddress, operatorAddress) => {
      blockchainDelegation.Service.delete(delegatorAddress, operatorAddress) pipeTo sender()
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
