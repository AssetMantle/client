package dbActors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import models.Abstract.PublicKey
import models.blockchain.Account
import play.api.Logger
import models.blockchain.Account
import models.master

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
    case TryGet(address) => {
      println(s"address is fetched $address")
      sender() ! blockchainBalance.Service.get2(address)
    }
  }

}


case class Create(address: String, username: String, accountType: String, publicKey: Option[PublicKey])
case class Get(address: String)
case class TryGet(address: String)