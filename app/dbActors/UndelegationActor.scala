package dbActors

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Undelegation}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object UndelegationActor {
  def props(blockchainUndelegation: models.blockchain.Undelegations) = Props(new UndelegationActor(blockchainUndelegation))
}

@Singleton
class UndelegationActor @Inject()(
                                   blockchainUndelegation: models.blockchain.Undelegations
                                 )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateUndelegation(undelegation) => {
      blockchainUndelegation.Service.create(undelegation) pipeTo sender()
      println(self.path)
    }
    case InsertMultipleUndelegation(undelegations) => {
      blockchainUndelegation.Service.insertMultiple(undelegations) pipeTo sender()
      println(self.path)
    }
    case InsertOrUpdateUndelegation(undelegation) => {
      blockchainUndelegation.Service.insertOrUpdate(undelegation) pipeTo sender()
      println(self.path)
    }
    case GetAllUndelegationByDelegator(address) => {
      blockchainUndelegation.Service.getAllByDelegator(address) pipeTo sender()
      println(self.path)
    }
    case GetAllUndelegationByValidator(address) => {
      blockchainUndelegation.Service.getAllByValidator(address) pipeTo sender()
      println(self.path)
    }
    case GetAllUndelegation() => {
      blockchainUndelegation.Service.getAll pipeTo sender()
      println(self.path)
    }
    case DeleteUndelegation(delegatorAddress, validatorAddress) => {
      blockchainUndelegation.Service.delete(delegatorAddress, validatorAddress) pipeTo sender()
      println(self.path)
    }
    case TryGetUndelegation(delegatorAddress, validatorAddress) => {
      blockchainUndelegation.Service.tryGet(delegatorAddress, validatorAddress) pipeTo sender()
      println(self.path)
    }

  }

}

case class CreateUndelegation(undelegation: Undelegation)
case class InsertMultipleUndelegation(undelegations: Seq[Undelegation])
case class InsertOrUpdateUndelegation(undelegation: Undelegation)
case class GetAllUndelegationByDelegator(address: String)
case class GetAllUndelegationByValidator(address: String)
case class GetAllUndelegation()
case class DeleteUndelegation(delegatorAddress: String, validatorAddress: String)
case class TryGetUndelegation(delegatorAddress: String, validatorAddress: String)


