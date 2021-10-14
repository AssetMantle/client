package dbActors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Address, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent, UnreachableMember}
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import models.Abstract.PublicKey
import models.blockchain.Account
import play.api.Logger
import models.blockchain.Account
import models.master

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt

object BlockchainActor {
  def props(blockchainBalance: models.blockchain.Balances) = Props(new BlockchainActor(blockchainBalance))
}

@Singleton
class BlockchainActor @Inject()(
                                 blockchainBalance: models.blockchain.Balances
                               )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)
  import context.dispatcher
  implicit val timeout = Timeout(3 seconds)

  val cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(
      self,
      initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent],
      classOf[UnreachableMember]
    )
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  override def receive: Receive = {
    case TryGet(address) => {
      println(s"address is fetched $address")
      sender() ! blockchainBalance.Service.get2(address)
      println(self.path)
    }
  }

}


case class Create(address: String, username: String, accountType: String, publicKey: Option[PublicKey])
case class Get(address: String)
case class TryGet(address: String)