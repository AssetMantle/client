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
    }
    case InsertMultipleUndelegation(undelegations) => {
      blockchainUndelegation.Service.insertMultiple(undelegations) pipeTo sender()
    }
    case InsertOrUpdateUndelegation(undelegation) => {
      blockchainUndelegation.Service.insertOrUpdate(undelegation) pipeTo sender()
    }
    case GetAllUndelegationByDelegator(address) => {
      blockchainUndelegation.Service.getAllByDelegator(address) pipeTo sender()
    }
    case GetAllUndelegationByValidator(address) => {
      blockchainUndelegation.Service.getAllByValidator(address) pipeTo sender()
    }
    case GetAllUndelegation() => {
      blockchainUndelegation.Service.getAll pipeTo sender()
    }
    case DeleteUndelegation(delegatorAddress, validatorAddress) => {
      blockchainUndelegation.Service.delete(delegatorAddress, validatorAddress) pipeTo sender()
    }
    case TryGetUndelegation(delegatorAddress, validatorAddress) => {
      blockchainUndelegation.Service.tryGet(delegatorAddress, validatorAddress) pipeTo sender()
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


