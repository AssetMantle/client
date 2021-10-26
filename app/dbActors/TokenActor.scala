package dbActors

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import models.blockchain.{Token}
import play.api.Logger
import utilities.MicroNumber

import javax.inject.{Inject, Singleton}

object TokenActor {
  def props(blockchainToken: models.blockchain.Tokens) = Props(new TokenActor(blockchainToken))
}

@Singleton
class TokenActor @Inject()(
                            blockchainToken: models.blockchain.Tokens
                          )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateToken(token) => {
      blockchainToken.Service.create(token) pipeTo sender()
      println(self.path)
    }
    case GetToken(denom) => {
      blockchainToken.Service.get(denom) pipeTo sender()
      println(self.path)
    }
    case GetAllToken() => {
      blockchainToken.Service.getAll pipeTo sender()
      println(self.path)
    }
    case GetAllDenoms() => {
      blockchainToken.Service.getAllDenoms pipeTo sender()
      println(self.path)
    }
    case GetStakingToken() => {
      blockchainToken.Service.getStakingToken pipeTo sender()
      println(self.path)
    }
    case InsertMultipleToken(tokens) => {
      blockchainToken.Service.insertMultiple(tokens) pipeTo sender()
      println(self.path)
    }
    case InsertOrUpdateToken(token) => {
      blockchainToken.Service.insertOrUpdate(token) pipeTo sender()
      println(self.path)
    }

    case UpdateStakingAmounts(denom, bondedAmount, notBondedAmount) => {
      blockchainToken.Service.updateStakingAmounts(denom, bondedAmount, notBondedAmount) pipeTo sender()
      println(self.path)
    }
    case UpdateTotalSupplyAndInflation(denom, totalSupply, inflation) => {
      blockchainToken.Service.updateTotalSupplyAndInflation(denom, totalSupply, inflation) pipeTo sender()
      println(self.path)
    }
    case GetTotalBondedAmount() => {
      blockchainToken.Service.getTotalBondedAmount pipeTo sender()
      println(self.path)
    }
  }

}

case class CreateToken(Token: Token)
case class GetToken(denom: String)
case class GetAllToken()
case class GetAllDenoms()
case class GetStakingToken()
case class InsertMultipleToken(Tokens: Seq[Token])
case class InsertOrUpdateToken(Token: Token)
case class UpdateStakingAmounts(denom: String, bondedAmount: MicroNumber, notBondedAmount: MicroNumber)
case class UpdateTotalSupplyAndInflation(denom: String, totalSupply: MicroNumber, inflation: BigDecimal)
case class GetTotalBondedAmount()


