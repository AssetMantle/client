package actors.models.blockchain

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props, Status}
import akka.cluster.sharding.ShardRegion
import akka.pattern.{pipe}
import models.blockchain.{Parameter}
import play.api.Logger
import javax.inject.{Inject, Singleton}
import constants.Actor.{NUMBER_OF_SHARDS, NUMBER_OF_ENTITIES}

object ParameterActor {
  def props(blockchainParameters: models.blockchain.Parameters) = Props(new ParameterActor(blockchainParameters))

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateParameter(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateParameter(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetParameter(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetAuthParameter(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetBankParameter(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetDistributionParameter(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetGovernanceParameter(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetHalvingParameter(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetMintingParameter(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetSlashingParameter(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetStakingParameter(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllParameter(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateParameter(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateParameter(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetParameter(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetAuthParameter(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetBankParameter(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetDistributionParameter(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetGovernanceParameter(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetHalvingParameter(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetMintingParameter(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetSlashingParameter(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetStakingParameter(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllParameter(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }
}

@Singleton
class ParameterActor @Inject()(
                               blockchainParameters: models.blockchain.Parameters
                             )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateParameter(_, parameter) => {
      blockchainParameters.Service.create(parameter) pipeTo sender()
    }
    case TryGetParameter(_, parameterType) => {
      blockchainParameters.Service.tryGet(parameterType) pipeTo sender()
    }
    case InsertOrUpdateParameter(_, parameter) => {
      blockchainParameters.Service.insertOrUpdate(parameter) pipeTo sender()
    }
    case TryGetAuthParameter(_) => {
      blockchainParameters.Service.tryGetAuthParameter pipeTo sender()
    }
    case TryGetBankParameter(_) => {
      blockchainParameters.Service.tryGetBankParameter pipeTo sender()
    }
    case TryGetDistributionParameter(_) => {
      blockchainParameters.Service.tryGetDistributionParameter pipeTo sender()
    }
    case TryGetGovernanceParameter(_) => {
      blockchainParameters.Service.tryGetGovernanceParameter pipeTo sender()
    }
    case TryGetHalvingParameter(_) => {
      blockchainParameters.Service.tryGetHalvingParameter pipeTo sender()
    }
    case TryGetMintingParameter(_) => {
      blockchainParameters.Service.tryGetMintingParameter pipeTo sender()
    }
    case TryGetSlashingParameter(_) => {
      blockchainParameters.Service.tryGetSlashingParameter pipeTo sender()
    }
    case TryGetStakingParameter(_) => {
      blockchainParameters.Service.tryGetStakingParameter pipeTo sender()
    }
    case GetAllParameter(_) => {
      blockchainParameters.Service.getAll pipeTo sender()
    }
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

