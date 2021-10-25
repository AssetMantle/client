package dbActors

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, WithdrawAddress}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object WithdrawAddressActor {
  def props(blockchainWithdrawAddress: models.blockchain.WithdrawAddresses) = Props(new WithdrawAddressActor(blockchainWithdrawAddress))
}

@Singleton
class WithdrawAddressActor @Inject()(
                               blockchainWithdrawAddress: models.blockchain.WithdrawAddresses
                             )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateWithdrawAddress(withdrawAddress) => {
      blockchainWithdrawAddress.Service.create(withdrawAddress) pipeTo sender()
      println(self.path)
    }
    case InsertMultipleWithdrawAddress(withdrawAddresses) => {
      blockchainWithdrawAddress.Service.insertMultiple(withdrawAddresses) pipeTo sender()
      println(self.path)
    }
    case InsertOrUpdateWithdrawAddress(withdrawAddress) => {
      blockchainWithdrawAddress.Service.insertOrUpdate(withdrawAddress) pipeTo sender()
      println(self.path)
    }
    case GetAllWithdrawAddresses() => {
      blockchainWithdrawAddress.Service.getAll pipeTo sender()
      println(self.path)
    }
    case GetWithdrawAddress(delegatorAddress) => {
      blockchainWithdrawAddress.Service.get(delegatorAddress) pipeTo sender()
      println(self.path)
    }

  }

}

case class CreateWithdrawAddress(withdrawAddress: WithdrawAddress)
case class InsertMultipleWithdrawAddress(withdrawAddresses: Seq[WithdrawAddress])
case class InsertOrUpdateWithdrawAddress(withdrawAddress: WithdrawAddress)
case class GetAllWithdrawAddresses()
case class GetWithdrawAddress(delegatorAddress: String)

