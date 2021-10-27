package dbActors

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Validator}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object ValidatorActor {
  def props(blockchainValidator: models.blockchain.Validators) = Props(new ValidatorActor(blockchainValidator))
}

@Singleton
class ValidatorActor @Inject()(
                               blockchainValidator: models.blockchain.Validators
                             )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateValidatorWithActor(validator) => {
      blockchainValidator.Service.create(validator) pipeTo sender()
      println(self.path)
    }
    case InsertMultipleValidators(validators) => {
      blockchainValidator.Service.insertMultiple(validators) pipeTo sender()
      println(self.path)
    }
    case InsertOrUpdateValidator(validator) => {
      blockchainValidator.Service.insertOrUpdate(validator) pipeTo sender()
      println(self.path)
    }
    case TryGetValidator(id) => {
      blockchainValidator.Service.tryGet(id) pipeTo sender()
      println(self.path)
    }
    case GetAllValidatorByHexAddresses(hexAddresses) => {
      blockchainValidator.Service.getAllByHexAddresses(hexAddresses) pipeTo sender()
      println(self.path)
    }
    case TryGetValidatorByOperatorAddress(operatorAddress) => {
      blockchainValidator.Service.tryGetByOperatorAddress(operatorAddress) pipeTo sender()
      println(self.path)
    }
    case TryGetValidatorByHexAddress(hexAddress) => {
      blockchainValidator.Service.tryGetHexAddress(hexAddress) pipeTo sender()
      println(self.path)
    }
    case TryGetValidatorOperatorAddress(hexAddress) => {
      blockchainValidator.Service.tryGetOperatorAddress(hexAddress) pipeTo sender()
      println(self.path)
    }
    case TryGetValidatorHexAddress(operatorAddress) => {
      blockchainValidator.Service.tryGetHexAddress(operatorAddress) pipeTo sender()
      println(self.path)
    }
    case TryGetValidatorProposerName(hexAddress) => {
      blockchainValidator.Service.tryGetProposerName(hexAddress) pipeTo sender()
      println(self.path)
    }
    case GetAllValidators() => {
      blockchainValidator.Service.getAll pipeTo sender()
      println(self.path)
    }
    case GetAllActiveValidatorList() => {
      blockchainValidator.Service.getAllActiveValidatorList pipeTo sender()
      println(self.path)
    }
    case GetAllInactiveValidatorList() => {
      blockchainValidator.Service.getAllInactiveValidatorList pipeTo sender()
      println(self.path)
    }
    case GetAllUnbondingValidatorList() => {
      blockchainValidator.Service.getAllUnbondingValidatorList pipeTo sender()
      println(self.path)
    }
    case GetAllUnbondedValidatorList() => {
      blockchainValidator.Service.getAllUnbondedValidatorList pipeTo sender()
      println(self.path)
    }
    case GetValidatorByOperatorAddresses(operatorAddresses) => {
      blockchainValidator.Service.getByOperatorAddresses(operatorAddresses) pipeTo sender()
      println(self.path)
    }
    case ValidatorExists(operatorAddress) => {
      blockchainValidator.Service.exists(operatorAddress) pipeTo sender()
      println(self.path)
    }
    case JailValidator(operatorAddress) => {
      blockchainValidator.Service.jailValidator(operatorAddress) pipeTo sender()
      println(self.path)
    }
    case DeleteValidatorWithOpAddress(operatorAddress)  => {
      blockchainValidator.Service.delete(operatorAddress) pipeTo sender()
      println(self.path)
    }
    case GetTotalVotingPower() => {
      blockchainValidator.Service.getTotalVotingPower pipeTo sender()
      println(self.path)
    }
  }

}

case class CreateValidatorWithActor(validator: Validator)
case class InsertMultipleValidators(validators: Seq[Validator])
case class InsertOrUpdateValidator(validator: Validator)
case class TryGetValidator(address: String)
case class GetAllValidatorByHexAddresses(hexAddresses: Seq[String])
case class TryGetValidatorByOperatorAddress(operatorAddress: String)
case class TryGetValidatorByHexAddress(hexAddress: String)
case class TryGetValidatorOperatorAddress(hexAddress: String)
case class TryGetValidatorHexAddress(operatorAddress: String)
case class TryGetValidatorProposerName(hexAddress: String)
case class GetAllValidators()
case class GetAllActiveValidatorList()
case class GetAllInactiveValidatorList()
case class GetAllUnbondingValidatorList()
case class GetAllUnbondedValidatorList()
case class GetValidatorByOperatorAddresses(operatorAddresses: Seq[String])
case class ValidatorExists(operatorAddress: String)
case class JailValidator(operatorAddress: String)
case class DeleteValidatorWithOpAddress(operatorAddress: String)
case class GetTotalVotingPower()