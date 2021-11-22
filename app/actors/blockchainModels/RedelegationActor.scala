package actors.blockchainModels

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Redelegation}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object RedelegationActor {
  def props(blockchainRedelegation: models.blockchain.Redelegations) = Props(new RedelegationActor(blockchainRedelegation))

  val numberOfEntities = 10
  val numberOfShards = 100

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateRedelegation(id, _) => (id, attempt)
    case attempt@InsertMultipleRedelegation(id, _) => (id, attempt)
    case attempt@InsertOrUpdateRedelegation(id, _) => (id, attempt)
    case attempt@GetAllRedelegationBySourceValidator(id, _) => (id, attempt)
    case attempt@GetAllRedelegation(id) => (id, attempt)
    case attempt@DeleteRedelegation(id, _, _, _) => (id, attempt)
    case attempt@TryGetRedelegation(id, _, _, _) => (id, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateRedelegation(id, _) => (id.hashCode % numberOfShards).toString
    case InsertMultipleRedelegation(id, _) => (id.hashCode % numberOfShards).toString
    case InsertOrUpdateRedelegation(id, _) => (id.hashCode % numberOfShards).toString
    case GetAllRedelegationBySourceValidator(id, _) => (id.hashCode % numberOfShards).toString
    case GetAllRedelegation(id) => (id.hashCode % numberOfShards).toString
    case DeleteRedelegation(id, _, _, _) => (id.hashCode % numberOfShards).toString
    case TryGetRedelegation(id, _, _, _) => (id.hashCode % numberOfShards).toString
  }
}

@Singleton
class RedelegationActor @Inject()(
                               blockchainRedelegation: models.blockchain.Redelegations
                             )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateRedelegation(_, redelegation) => {
      blockchainRedelegation.Service.create(redelegation) pipeTo sender()
    }
    case InsertMultipleRedelegation(_, redelegations) => {
      blockchainRedelegation.Service.insertMultiple(redelegations) pipeTo sender()
    }
    case InsertOrUpdateRedelegation(_, redelegation) => {
      blockchainRedelegation.Service.insertOrUpdate(redelegation) pipeTo sender()
    }
    case GetAllRedelegationBySourceValidator(_, address) => {
      blockchainRedelegation.Service.getAllBySourceValidator(address) pipeTo sender()
    }
    case GetAllRedelegation(_) => {
      blockchainRedelegation.Service.getAll pipeTo sender()
    }
    case DeleteRedelegation(_, delegatorAddress, validatorSourceAddress, validatorDestinationAddress) => {
      blockchainRedelegation.Service.delete(delegatorAddress, validatorSourceAddress, validatorDestinationAddress) pipeTo sender()
    }
    case TryGetRedelegation(_, delegatorAddress, validatorSourceAddress, validatorDestinationAddress) => {
      blockchainRedelegation.Service.tryGet(delegatorAddress, validatorSourceAddress, validatorDestinationAddress) pipeTo sender()
    }
  }

}

case class CreateRedelegation(uid: String, redelegation: Redelegation)
case class InsertMultipleRedelegation(uid: String, redelegations: Seq[Redelegation])
case class InsertOrUpdateRedelegation(uid: String, redelegation: Redelegation)
case class GetAllRedelegationBySourceValidator(uid: String, address: String)
case class GetAllRedelegation(uid: String)
case class DeleteRedelegation(uid: String, delegatorAddress: String, validatorSourceAddress: String, validatorDestinationAddress: String)
case class TryGetRedelegation(uid: String, delegatorAddress: String, validatorSourceAddress: String, validatorDestinationAddress: String)


