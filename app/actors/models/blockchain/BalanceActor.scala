package actors.models.blockchain

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import models.blockchain.Balance
import models.common.Serializable.Coin
import play.api.Logger
import constants.Actor.{NUMBER_OF_SHARDS, NUMBER_OF_ENTITIES}

import java.util.Date
import javax.inject.{Inject, Singleton}


object BalanceActor {
  def props(blockchainBalances: models.blockchain.Balances) = Props(new BalanceActor(blockchainBalances))

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@Get(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGet(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@Create(id, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdate(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetList(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case Get(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGet(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case Create(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdate(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetList(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }
}

@Singleton
class BalanceActor @Inject()(
                                 blockchainBalances: models.blockchain.Balances
                               )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case Get(_, address) => {
      blockchainBalances.Service.get(address) pipeTo sender()
      logger.info( s"Im the actor $self")
    }
    case Create(_, address, coins) => {
      blockchainBalances.Service.create(address, coins) pipeTo sender()
    }
    case TryGet(_, address) => {
      blockchainBalances.Service.tryGet(address) pipeTo sender()
    }
    case InsertOrUpdate(_, balance) => {
      blockchainBalances.Service.insertOrUpdate(balance) pipeTo sender()
    }
    case GetList(_, addresses) => {
      blockchainBalances.Service.getList(addresses) pipeTo sender()
    }
  }

}

case class Get(id: String, address: String)
case class TryGet(id: String, address: String)
case class Create(id: String, address: String, coins: Seq[Coin])
case class InsertOrUpdate(id: String, balance: Balance)
case class GetList(id: String, addresses: Seq[String])