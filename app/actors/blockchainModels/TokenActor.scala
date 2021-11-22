package actors.blockchainModels

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.blockchain.Token
import play.api.Logger
import utilities.MicroNumber
import javax.inject.{Inject, Singleton}
import constants.Actor.{NUMBER_OF_SHARDS, NUMBER_OF_ENTITIES}

object TokenActor {

  def props(blockchainToken: models.blockchain.Tokens) = Props(new TokenActor(blockchainToken))

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateToken(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetToken(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllToken(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllDenoms(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetStakingToken(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertMultipleToken(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateToken(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@UpdateStakingAmounts(id, _, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@UpdateTotalSupplyAndInflation(id, _, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetTotalBondedAmount(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateToken(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetToken(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllToken(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllDenoms(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetStakingToken(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertMultipleToken(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateToken(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case UpdateStakingAmounts(id, _, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case UpdateTotalSupplyAndInflation(id, _, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetTotalBondedAmount(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }

}

@Singleton
class TokenActor @Inject()(
                            blockchainToken: models.blockchain.Tokens
                          )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateToken(_, token) => {
      blockchainToken.Service.create(token) pipeTo sender()
    }
    case GetToken(_, denom) => {
      blockchainToken.Service.get(denom) pipeTo sender()
    }
    case GetAllToken(_) => {
      blockchainToken.Service.getAll pipeTo sender()
    }
    case GetAllDenoms(_) => {
      blockchainToken.Service.getAllDenoms pipeTo sender()
    }
    case GetStakingToken(_) => {
      blockchainToken.Service.getStakingToken pipeTo sender()
    }
    case InsertMultipleToken(_, tokens) => {
      blockchainToken.Service.insertMultiple(tokens) pipeTo sender()
    }
    case InsertOrUpdateToken(_, token) => {
      blockchainToken.Service.insertOrUpdate(token) pipeTo sender()
    }
    case UpdateStakingAmounts(_, denom, bondedAmount, notBondedAmount) => {
      blockchainToken.Service.updateStakingAmounts(denom, bondedAmount, notBondedAmount) pipeTo sender()
    }
    case UpdateTotalSupplyAndInflation(_, denom, totalSupply, inflation) => {
      blockchainToken.Service.updateTotalSupplyAndInflation(denom, totalSupply, inflation) pipeTo sender()
    }
    case GetTotalBondedAmount(_) => {
      blockchainToken.Service.getTotalBondedAmount pipeTo sender()
    }
  }
}

case class CreateToken(id: String, Token: Token)
case class GetToken(id: String, denom: String)
case class GetAllToken(id: String)
case class GetAllDenoms(id: String)
case class GetStakingToken(id: String)
case class InsertMultipleToken(id: String, Tokens: Seq[Token])
case class InsertOrUpdateToken(id: String, Token: Token)
case class UpdateStakingAmounts(id: String, denom: String, bondedAmount: MicroNumber, notBondedAmount: MicroNumber)
case class UpdateTotalSupplyAndInflation(id: String, denom: String, totalSupply: MicroNumber, inflation: BigDecimal)
case class GetTotalBondedAmount(id: String)


