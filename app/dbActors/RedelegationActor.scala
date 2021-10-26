package dbActors

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Redelegation}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object RedelegationActor {
  def props(blockchainRedelegation: models.blockchain.Redelegations) = Props(new RedelegationActor(blockchainRedelegation))
}

@Singleton
class RedelegationActor @Inject()(
                               blockchainRedelegation: models.blockchain.Redelegations
                             )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateRedelegation(redelegation) => {
      blockchainRedelegation.Service.create(redelegation) pipeTo sender()
      println(self.path)
    }
    case InsertMultipleRedelegation(redelegations) => {
      blockchainRedelegation.Service.insertMultiple(redelegations) pipeTo sender()
      println(self.path)
    }
    case InsertOrUpdateRedelegation(redelegation) => {
      blockchainRedelegation.Service.insertOrUpdate(redelegation) pipeTo sender()
      println(self.path)
    }
    case GetAllRedelegationBySourceValidator(address) => {
      blockchainRedelegation.Service.getAllBySourceValidator(address) pipeTo sender()
      println(self.path)
    }
    case GetAllRedelegation() => {
      blockchainRedelegation.Service.getAll pipeTo sender()
      println(self.path)
    }
    case DeleteRedelegation(delegatorAddress, validatorSourceAddress, validatorDestinationAddress) => {
      blockchainRedelegation.Service.delete(delegatorAddress, validatorSourceAddress, validatorDestinationAddress) pipeTo sender()
      println(self.path)
    }
    case TryGetRedelegation(delegatorAddress, validatorSourceAddress, validatorDestinationAddress) => {
      blockchainRedelegation.Service.tryGet(delegatorAddress, validatorSourceAddress, validatorDestinationAddress) pipeTo sender()
      println(self.path)
    }

  }

}

case class CreateRedelegation(redelegation: Redelegation)
case class InsertMultipleRedelegation(redelegations: Seq[Redelegation])
case class InsertOrUpdateRedelegation(redelegation: Redelegation)
case class GetAllRedelegationBySourceValidator(address: String)
case class GetAllRedelegation()
case class DeleteRedelegation(delegatorAddress: String, validatorSourceAddress: String, validatorDestinationAddress: String)
case class TryGetRedelegation(delegatorAddress: String, validatorSourceAddress: String, validatorDestinationAddress: String)


