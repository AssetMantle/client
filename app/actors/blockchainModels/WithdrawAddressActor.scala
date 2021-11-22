package actors.blockchainModels

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, WithdrawAddress}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object WithdrawAddressActor {
  def props(blockchainWithdrawAddress: models.blockchain.WithdrawAddresses) = Props(new WithdrawAddressActor(blockchainWithdrawAddress))

  val  numberOfShards = 10
  val numberOfEntities = 100

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateWithdrawAddress(id, _) => (id, attempt)
    case attempt@InsertMultipleWithdrawAddress(id, _) => (id, attempt)
    case attempt@InsertOrUpdateWithdrawAddress(id, _) => (id, attempt)
    case attempt@GetAllWithdrawAddresses(id) => (id, attempt)
    case attempt@GetWithdrawAddress(id, _) => (id, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateWithdrawAddress(id, _) => (id.hashCode % numberOfShards).toString
    case InsertMultipleWithdrawAddress(id, _) => (id.hashCode % numberOfShards).toString
    case InsertOrUpdateWithdrawAddress(id, _) => (id.hashCode % numberOfShards).toString
    case GetAllWithdrawAddresses(id) => (id.hashCode % numberOfShards).toString
    case GetWithdrawAddress(id, _) => (id.hashCode % numberOfShards).toString

  }
}

@Singleton
class WithdrawAddressActor @Inject()(
                               blockchainWithdrawAddress: models.blockchain.WithdrawAddresses
                             )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateWithdrawAddress(_, withdrawAddress) => {
      blockchainWithdrawAddress.Service.create(withdrawAddress) pipeTo sender()
    }
    case InsertMultipleWithdrawAddress(_, withdrawAddresses) => {
      blockchainWithdrawAddress.Service.insertMultiple(withdrawAddresses) pipeTo sender()
    }
    case InsertOrUpdateWithdrawAddress(_, withdrawAddress) => {
      blockchainWithdrawAddress.Service.insertOrUpdate(withdrawAddress) pipeTo sender()
    }
    case GetAllWithdrawAddresses(_) => {
      blockchainWithdrawAddress.Service.getAll pipeTo sender()
    }
    case GetWithdrawAddress(_, delegatorAddress) => {
      blockchainWithdrawAddress.Service.get(delegatorAddress) pipeTo sender()
    }
  }
}

case class CreateWithdrawAddress(uid: String, withdrawAddress: WithdrawAddress)
case class InsertMultipleWithdrawAddress(uid: String, withdrawAddresses: Seq[WithdrawAddress])
case class InsertOrUpdateWithdrawAddress(uid: String, withdrawAddress: WithdrawAddress)
case class GetAllWithdrawAddresses(uid: String)
case class GetWithdrawAddress(uid: String, delegatorAddress: String)

