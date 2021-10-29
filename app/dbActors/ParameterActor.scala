package dbActors

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Parameter}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object ParameterActor {
  def props(blockchainParameter: models.blockchain.Parameters) = Props(new ParameterActor(blockchainParameter))
}

@Singleton
class ParameterActor @Inject()(
                               blockchainParameter: models.blockchain.Parameters
                             )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateParameter(parameter) => {
      blockchainParameter.Service.create(parameter) pipeTo sender()
    }
    case TryGetParameter(parameterType) => {
      blockchainParameter.Service.tryGet(parameterType) pipeTo sender()
    }
    case InsertOrUpdateParameter(parameter) => {
      blockchainParameter.Service.insertOrUpdate(parameter) pipeTo sender()
    }
    case TryGetAuthParameter() => {
      blockchainParameter.Service.tryGetAuthParameter pipeTo sender()
    }
    case TryGetBankParameter() => {
      blockchainParameter.Service.tryGetBankParameter pipeTo sender()
    }
    case TryGetDistributionParameter() => {
      blockchainParameter.Service.tryGetDistributionParameter pipeTo sender()
    }
    case TryGetGovernanceParameter() => {
      blockchainParameter.Service.tryGetGovernanceParameter pipeTo sender()
    }
    case TryGetHalvingParameter() => {
      blockchainParameter.Service.tryGetHalvingParameter pipeTo sender()
    }
    case TryGetMintingParameter() => {
      blockchainParameter.Service.tryGetMintingParameter pipeTo sender()
    }
    case TryGetSlashingParameter() => {
      blockchainParameter.Service.tryGetSlashingParameter pipeTo sender()
    }
    case TryGetStakingParameter() => {
      blockchainParameter.Service.tryGetStakingParameter pipeTo sender()
    }
    case GetAllParameter() => {
      blockchainParameter.Service.getAll pipeTo sender()
    }
  }

}

case class CreateParameter(parameter: Parameter)
case class InsertOrUpdateParameter(parameter: Parameter)
case class TryGetParameter(parameterType: String)
case class TryGetAuthParameter()
case class TryGetBankParameter()
case class TryGetDistributionParameter()
case class TryGetGovernanceParameter()
case class TryGetHalvingParameter()
case class TryGetMintingParameter()
case class TryGetSlashingParameter()
case class TryGetStakingParameter()
case class GetAllParameter()

