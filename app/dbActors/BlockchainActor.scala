package dbActors

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import models.blockchain.Balance
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}

object BlockchainActor {
  def props(blockchainBalance: models.blockchain.Balances) = Props(new BlockchainActor(blockchainBalance))
}

@Singleton
class BlockchainActor @Inject()(
                                 blockchainBalance: models.blockchain.Balances
                               )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case Get(address) => {
      println(s"address is fetched $address")
      blockchainBalance.Service.get(address) pipeTo sender()
      println(self.path)
    }
    case Create(address, coins) => {
      blockchainBalance.Service.create(address, coins) pipeTo sender()
      println(self.path)
    }
    case TryGet(address) => {
      blockchainBalance.Service.tryGet(address) pipeTo sender()
      println(self.path)
    }
    case InsertOrUpdate(balance) => {
      blockchainBalance.Service.insertOrUpdate(balance) pipeTo sender()
      println(self.path)
    }
    case GetList(addresses) => {
      blockchainBalance.Service.getList(addresses) pipeTo sender()
      println(self.path)
    }
  }

}


case class Get(address: String)
case class TryGet(address: String)
case class Create(address: String, coins: Seq[Coin])
case class InsertOrUpdate(balance: Balance)
case class GetList(addresses: Seq[String])