package actors.models

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Undelegation}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object UndelegationActor {
  def props(blockchainUndelegation: models.blockchain.Undelegations) = Props(new UndelegationActor(blockchainUndelegation))

  val  numberOfShards = 10
  val numberOfEntities = 100

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateUndelegation(id, _) => (id, attempt)
    case attempt@InsertMultipleUndelegation(id, _) => (id, attempt)
    case attempt@InsertOrUpdateUndelegation(id, _) => (id, attempt)
    case attempt@GetAllUndelegationByDelegator(id, _) => (id, attempt)
    case attempt@GetAllUndelegationByValidator(id, _) => (id, attempt)
    case attempt@GetAllUndelegation(id) => (id, attempt)
    case attempt@DeleteUndelegation(id, _, _) => (id, attempt)
    case attempt@TryGetUndelegation(id, _, _) => (id, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateUndelegation(id, _) => (id.hashCode % numberOfShards).toString
    case InsertMultipleUndelegation(id, _) => (id.hashCode % numberOfShards).toString
    case InsertOrUpdateUndelegation(id, _) => (id.hashCode % numberOfShards).toString
    case GetAllUndelegationByDelegator(id, _) => (id.hashCode % numberOfShards).toString
    case GetAllUndelegationByValidator(id, _) => (id.hashCode % numberOfShards).toString
    case GetAllUndelegation(id) => (id.hashCode % numberOfShards).toString
    case DeleteUndelegation(id, _, _) => (id.hashCode % numberOfShards).toString
    case TryGetUndelegation(id, _, _) => (id.hashCode % numberOfShards).toString
  }
}

@Singleton
class UndelegationActor @Inject()(
                                   blockchainUndelegation: models.blockchain.Undelegations
                                 )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateUndelegation(_, undelegation) => {
      blockchainUndelegation.Service.create(undelegation) pipeTo sender()
    }
    case InsertMultipleUndelegation(_, undelegations) => {
      blockchainUndelegation.Service.insertMultiple(undelegations) pipeTo sender()
    }
    case InsertOrUpdateUndelegation(_, undelegation) => {
      blockchainUndelegation.Service.insertOrUpdate(undelegation) pipeTo sender()
    }
    case GetAllUndelegationByDelegator(_, address) => {
      blockchainUndelegation.Service.getAllByDelegator(address) pipeTo sender()
    }
    case GetAllUndelegationByValidator(_, address) => {
      blockchainUndelegation.Service.getAllByValidator(address) pipeTo sender()
    }
    case GetAllUndelegation(_) => {
      blockchainUndelegation.Service.getAll pipeTo sender()
    }
    case DeleteUndelegation(_, delegatorAddress, validatorAddress) => {
      blockchainUndelegation.Service.delete(delegatorAddress, validatorAddress) pipeTo sender()
    }
    case TryGetUndelegation(_, delegatorAddress, validatorAddress) => {
      blockchainUndelegation.Service.tryGet(delegatorAddress, validatorAddress) pipeTo sender()
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


