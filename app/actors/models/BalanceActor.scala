package actors.models

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import models.blockchain.Balance
import models.common.Serializable.Coin
import play.api.Logger

import java.util.Date
import javax.inject.{Inject, Singleton}


object BalanceActor {
  def props(blockchainBalance: models.blockchain.Balances) = Props(new BalanceActor(blockchainBalance))

  val numberOfEntities = 10
  val numberOfShards = 100

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@Get(id, _) => (id, attempt)
    case attempt@TryGet(id, _) => (id, attempt)
    case attempt@Create(id, _, _) => (id, attempt)
    case attempt@InsertOrUpdate(id, _) => (id, attempt)
    case attempt@GetList(id, _) => (id, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case Get(id, _) => (id.hashCode % numberOfShards).toString
    case TryGet(id, _) => (id.hashCode % numberOfShards).toString
    case Create(id, _, _) => (id.hashCode % numberOfShards).toString
    case InsertOrUpdate(id, _) => (id.hashCode % numberOfShards).toString
    case GetList(id, _) => (id.hashCode % numberOfShards).toString
  }
}

@Singleton
class BalanceActor @Inject()(
                                 blockchainBalance: models.blockchain.Balances
                               )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case Get(_, address) => {
      blockchainBalance.Service.get(address) pipeTo sender()
      logger.info( s"Im the actor $self")
    }
    case Create(_, address, coins) => {
      blockchainBalance.Service.create(address, coins) pipeTo sender()
    }
    case TryGet(_, address) => {
      blockchainBalance.Service.tryGet(address) pipeTo sender()
    }
    case InsertOrUpdate(_, balance) => {
      blockchainBalance.Service.insertOrUpdate(balance) pipeTo sender()
    }
    case GetList(_, addresses) => {
      blockchainBalance.Service.getList(addresses) pipeTo sender()
    }
  }

}


case class Get(id: String, address: String)
case class TryGet(id: String, address: String)
case class Create(id: String, address: String, coins: Seq[Coin])
case class InsertOrUpdate(id: String, balance: Balance)
case class GetList(id: String, addresses: Seq[String])