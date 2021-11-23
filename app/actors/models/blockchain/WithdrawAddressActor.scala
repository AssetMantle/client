package actors.models.blockchain

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.blockchain.{WithdrawAddress}
import play.api.Logger
import javax.inject.{Inject, Singleton}
import constants.Actor.{NUMBER_OF_SHARDS, NUMBER_OF_ENTITIES}

object WithdrawAddressActor {
  def props(blockchainWithdrawAddresses: models.blockchain.WithdrawAddresses) = Props(new WithdrawAddressActor(blockchainWithdrawAddresses))

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateWithdrawAddress(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertMultipleWithdrawAddress(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateWithdrawAddress(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllWithdrawAddresses(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetWithdrawAddress(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateWithdrawAddress(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertMultipleWithdrawAddress(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateWithdrawAddress(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllWithdrawAddresses(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetWithdrawAddress(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }
}

@Singleton
class WithdrawAddressActor @Inject()(
                               blockchainWithdrawAddresses: models.blockchain.WithdrawAddresses
                             )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateWithdrawAddress(_, withdrawAddress) => {
      blockchainWithdrawAddresses.Service.create(withdrawAddress) pipeTo sender()
    }
    case InsertMultipleWithdrawAddress(_, withdrawAddresses) => {
      blockchainWithdrawAddresses.Service.insertMultiple(withdrawAddresses) pipeTo sender()
    }
    case InsertOrUpdateWithdrawAddress(_, withdrawAddress) => {
      blockchainWithdrawAddresses.Service.insertOrUpdate(withdrawAddress) pipeTo sender()
    }
    case GetAllWithdrawAddresses(_) => {
      blockchainWithdrawAddresses.Service.getAll pipeTo sender()
    }
    case GetWithdrawAddress(_, delegatorAddress) => {
      blockchainWithdrawAddresses.Service.get(delegatorAddress) pipeTo sender()
    }
  }
}

case class CreateWithdrawAddress(uid: String, withdrawAddress: WithdrawAddress)
case class InsertMultipleWithdrawAddress(uid: String, withdrawAddresses: Seq[WithdrawAddress])
case class InsertOrUpdateWithdrawAddress(uid: String, withdrawAddress: WithdrawAddress)
case class GetAllWithdrawAddresses(uid: String)
case class GetWithdrawAddress(uid: String, delegatorAddress: String)

