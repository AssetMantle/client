package dbActors

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Delegation}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object DelegationActor {
  def props(blockchainDelegation: models.blockchain.Delegations) = Props(new DelegationActor(blockchainDelegation))
}

@Singleton
class DelegationActor @Inject()(
                                     blockchainDelegation: models.blockchain.Delegations
                                   )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateDelegation(delegation) => {
      blockchainDelegation.Service.create(delegation) pipeTo sender()
      println(self.path)
    }
    case InsertMultipleDelegation(delegations) => {
      blockchainDelegation.Service.insertMultiple(delegations) pipeTo sender()
      println(self.path)
    }
    case InsertOrUpdateDelegation(delegation) => {
      blockchainDelegation.Service.insertOrUpdate(delegation) pipeTo sender()
      println(self.path)
    }
    case GetDelegation(delegatorAddress, operatorAddress) => {
      blockchainDelegation.Service.get(delegatorAddress, operatorAddress) pipeTo sender()
      println(self.path)
    }

    case GetAllDelegationForDelegator(address) => {
      blockchainDelegation.Service.getAllForDelegator(address) pipeTo sender()
      println(self.path)
    }
    case GetAllDelegationForValidator(operatorAddress) => {
      blockchainDelegation.Service.getAllForValidator(operatorAddress) pipeTo sender()
      println(self.path)
    }
    case DeleteDelegation(delegatorAddress, operatorAddress) => {
      blockchainDelegation.Service.delete(delegatorAddress, operatorAddress) pipeTo sender()
      println(self.path)
    }
  }

}

case class CreateDelegation(delegation: Delegation)
case class InsertMultipleDelegation(delegation: Seq[Delegation])
case class InsertOrUpdateDelegation(delegation: Delegation)
case class GetAllDelegationForDelegator(address: String)
case class GetAllDelegationForValidator(operatorAddress: String)
case class GetDelegation(delegatorAddress: String, operatorAddress: String)
case class DeleteDelegation(delegatorAddress: String, operatorAddress: String)
