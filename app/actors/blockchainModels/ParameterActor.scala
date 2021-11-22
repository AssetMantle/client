package actors.blockchainModels

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props, Status}
import akka.cluster.sharding.ShardRegion
import akka.pattern.{AskTimeoutException, pipe}
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Parameter}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object ParameterActor {
  def props(blockchainParameter: models.blockchain.Parameters) = Props(new ParameterActor(blockchainParameter))

  val numberOfEntities = 10
  val numberOfShards = 100

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateParameter(id, _) => (id, attempt)
    case attempt@InsertOrUpdateParameter(id, _) => (id, attempt)
    case attempt@TryGetParameter(id, _) => (id, attempt)
    case attempt@TryGetAuthParameter(id) => (id, attempt)
    case attempt@TryGetBankParameter(id) => (id, attempt)
    case attempt@TryGetDistributionParameter(id) => (id, attempt)
    case attempt@TryGetGovernanceParameter(id) => (id, attempt)
    case attempt@TryGetHalvingParameter(id) => (id, attempt)
    case attempt@TryGetMintingParameter(id) => (id, attempt)
    case attempt@TryGetSlashingParameter(id) => (id, attempt)
    case attempt@TryGetStakingParameter(id) => (id, attempt)
    case attempt@GetAllParameter(id) => (id, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateParameter(id, _) => (id.hashCode % numberOfShards).toString
    case InsertOrUpdateParameter(id, _) => (id.hashCode % numberOfShards).toString
    case TryGetParameter(id, _) => (id.hashCode % numberOfShards).toString
    case TryGetAuthParameter(id) => (id.hashCode % numberOfShards).toString
    case TryGetBankParameter(id) => (id.hashCode % numberOfShards).toString
    case TryGetDistributionParameter(id) => (id.hashCode % numberOfShards).toString
    case TryGetGovernanceParameter(id) => (id.hashCode % numberOfShards).toString
    case TryGetHalvingParameter(id) => (id.hashCode % numberOfShards).toString
    case TryGetMintingParameter(id) => (id.hashCode % numberOfShards).toString
    case TryGetSlashingParameter(id) => (id.hashCode % numberOfShards).toString
    case TryGetStakingParameter(id) => (id.hashCode % numberOfShards).toString
    case GetAllParameter(id) => (id.hashCode % numberOfShards).toString
  }
}

@Singleton
class ParameterActor @Inject()(
                               blockchainParameter: models.blockchain.Parameters
                             )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateParameter(_, parameter) => {
      blockchainParameter.Service.create(parameter) pipeTo sender()
    }
    case TryGetParameter(_, parameterType) => {
      blockchainParameter.Service.tryGet(parameterType) pipeTo sender()
    }
    case InsertOrUpdateParameter(_, parameter) => {
      blockchainParameter.Service.insertOrUpdate(parameter) pipeTo sender()
    }
    case TryGetAuthParameter(_) => {
      blockchainParameter.Service.tryGetAuthParameter pipeTo sender()
    }
    case TryGetBankParameter(_) => {
      blockchainParameter.Service.tryGetBankParameter pipeTo sender()
    }
    case TryGetDistributionParameter(_) => {
      blockchainParameter.Service.tryGetDistributionParameter pipeTo sender()
    }
    case TryGetGovernanceParameter(_) => {
      blockchainParameter.Service.tryGetGovernanceParameter pipeTo sender()
    }
    case TryGetHalvingParameter(_) => {
      blockchainParameter.Service.tryGetHalvingParameter pipeTo sender()
    }
    case TryGetMintingParameter(_) => {
      blockchainParameter.Service.tryGetMintingParameter pipeTo sender()
    }
    case TryGetSlashingParameter(_) => {
      blockchainParameter.Service.tryGetSlashingParameter pipeTo sender()
    }
    case TryGetStakingParameter(_) => {
      blockchainParameter.Service.tryGetStakingParameter pipeTo sender()
    }
    case GetAllParameter(_) => {
      blockchainParameter.Service.getAll pipeTo sender()
    }
    case e: Exception =>
      logger.info(s"the exception is $e")
  }

}

case class CreateParameter(id: String, parameter: Parameter)
case class InsertOrUpdateParameter(id: String, parameter: Parameter)
case class TryGetParameter(id: String, parameterType: String)
case class TryGetAuthParameter(id: String)
case class TryGetBankParameter(id: String)
case class TryGetDistributionParameter(id: String)
case class TryGetGovernanceParameter(id: String)
case class TryGetHalvingParameter(id: String)
case class TryGetMintingParameter(id: String)
case class TryGetSlashingParameter(id: String)
case class TryGetStakingParameter(id: String)
case class GetAllParameter(id: String)

