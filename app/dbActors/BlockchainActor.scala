package dbActors

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import models.Abstract.PublicKey
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
  }

}


case class Create(address: String, username: String, accountType: String, publicKey: Option[PublicKey])
case class Get(address: String)
