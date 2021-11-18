package actors.models

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Validator}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object ValidatorActor {
  val  numberOfShards = 10
  val numberOfEntities = 100

  def props(blockchainValidator: models.blockchain.Validators) = Props(new ValidatorActor(blockchainValidator))

  val idExtractor: ShardRegion.ExtractEntityId = {

    case attempt@CreateValidatorWithActor(id, _) => (id, attempt)
    case attempt@InsertMultipleValidators(id, _) => (id, attempt)
    case attempt@InsertOrUpdateValidator(id, _) => (id, attempt)
    case attempt@TryGetValidator(id, _) => (id, attempt)
    case attempt@GetAllValidatorByHexAddresses(id, _) => (id, attempt)
    case attempt@TryGetValidatorByOperatorAddress(id, _) => (id, attempt)
    case attempt@TryGetValidatorByHexAddress(id, _) => (id, attempt)
    case attempt@TryGetValidatorOperatorAddress(id, _) => (id, attempt)
    case attempt@TryGetValidatorHexAddress(id, _) => (id, attempt)
    case attempt@TryGetValidatorProposerName(id, _) => (id, attempt)
    case attempt@GetAllValidators(id) => (id, attempt)
    case attempt@GetAllActiveValidatorList(id) => (id, attempt)
    case attempt@GetAllInactiveValidatorList(id) => (id, attempt)
    case attempt@GetAllUnbondingValidatorList(id) => (id, attempt)
    case attempt@GetAllUnbondedValidatorList(id) => (id, attempt)
    case attempt@GetValidatorByOperatorAddresses(id, _) => (id, attempt)
    case attempt@ValidatorExists(id, _) => (id, attempt)
    case attempt@JailValidator(id, _) => (id, attempt)
    case attempt@DeleteValidatorWithOpAddress(id, _)  => (id, attempt)
    case attempt@GetTotalVotingPower(id) => (id, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateValidatorWithActor(id, _) => (id.hashCode % numberOfShards).toString
    case InsertMultipleValidators(id, _) => (id.hashCode % numberOfShards).toString
    case InsertOrUpdateValidator(id, _) => (id.hashCode % numberOfShards).toString
    case TryGetValidator(id, _) => (id.hashCode % numberOfShards).toString
    case GetAllValidatorByHexAddresses(id, _) => (id.hashCode % numberOfShards).toString
    case TryGetValidatorByOperatorAddress(id, _) => (id.hashCode % numberOfShards).toString
    case TryGetValidatorByHexAddress(id, _) => (id.hashCode % numberOfShards).toString
    case TryGetValidatorOperatorAddress(id, _) => (id.hashCode % numberOfShards).toString
    case TryGetValidatorHexAddress(id, _) => (id.hashCode % numberOfShards).toString
    case TryGetValidatorProposerName(id, _) => (id.hashCode % numberOfShards).toString
    case GetAllValidators(id) => (id.hashCode % numberOfShards).toString
    case GetAllActiveValidatorList(id) => (id.hashCode % numberOfShards).toString
    case GetAllInactiveValidatorList(id) => (id.hashCode % numberOfShards).toString
    case GetAllUnbondingValidatorList(id) => (id.hashCode % numberOfShards).toString
    case GetAllUnbondedValidatorList(id) => (id.hashCode % numberOfShards).toString
    case GetValidatorByOperatorAddresses(id, _) => (id.hashCode % numberOfShards).toString
    case ValidatorExists(id, _) => (id.hashCode % numberOfShards).toString
    case JailValidator(id, _) => (id.hashCode % numberOfShards).toString
    case DeleteValidatorWithOpAddress(id, _)  => (id.hashCode % numberOfShards).toString
    case GetTotalVotingPower(id) => (id.hashCode % numberOfShards).toString
  }
}

@Singleton
class ValidatorActor @Inject()(
                               blockchainValidator: models.blockchain.Validators
                             )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case StartActor(actorRef) => {
      logger.info("Actor Started")
    }
    case CreateValidatorWithActor(_, validator) => {
      blockchainValidator.Service.create(validator) pipeTo sender()
    }
    case InsertMultipleValidators(_, validators) => {
      blockchainValidator.Service.insertMultiple(validators) pipeTo sender()
    }
    case InsertOrUpdateValidator(_, validator) => {
      blockchainValidator.Service.insertOrUpdate(validator) pipeTo sender()
    }
    case TryGetValidator(_, id) => {
      blockchainValidator.Service.tryGet(id) pipeTo sender()
    }
    case GetAllValidatorByHexAddresses(_, hexAddresses) => {
      blockchainValidator.Service.getAllByHexAddresses(hexAddresses) pipeTo sender()
    }
    case TryGetValidatorByOperatorAddress(_, operatorAddress) => {
      blockchainValidator.Service.tryGetByOperatorAddress(operatorAddress) pipeTo sender()
    }
    case TryGetValidatorByHexAddress(_, hexAddress) => {
      blockchainValidator.Service.tryGetByHexAddress(hexAddress) pipeTo sender()
    }
    case TryGetValidatorOperatorAddress(_, hexAddress) => {
      blockchainValidator.Service.tryGetOperatorAddress(hexAddress) pipeTo sender()
    }
    case TryGetValidatorHexAddress(_, operatorAddress) => {
      blockchainValidator.Service.tryGetHexAddress(operatorAddress) pipeTo sender()
    }
    case TryGetValidatorProposerName(_, hexAddress) => {
      blockchainValidator.Service.tryGetProposerName(hexAddress) pipeTo sender()
    }
    case GetAllValidators(_) => {
      blockchainValidator.Service.getAll pipeTo sender()
    }
    case GetAllActiveValidatorList(_) => {
      blockchainValidator.Service.getAllActiveValidatorList pipeTo sender()
    }
    case GetAllInactiveValidatorList(_) => {
      blockchainValidator.Service.getAllInactiveValidatorList pipeTo sender()
    }
    case GetAllUnbondingValidatorList(_) => {
      blockchainValidator.Service.getAllUnbondingValidatorList pipeTo sender()
    }
    case GetAllUnbondedValidatorList(_) => {
      blockchainValidator.Service.getAllUnbondedValidatorList pipeTo sender()
    }
    case GetValidatorByOperatorAddresses(_, operatorAddresses) => {
      blockchainValidator.Service.getByOperatorAddresses(operatorAddresses) pipeTo sender()
    }
    case ValidatorExists(_, operatorAddress) => {
      blockchainValidator.Service.exists(operatorAddress) pipeTo sender()
    }
    case JailValidator(_, operatorAddress) => {
      blockchainValidator.Service.jailValidator(operatorAddress) pipeTo sender()
    }
    case DeleteValidatorWithOpAddress(_, operatorAddress)  => {
      blockchainValidator.Service.delete(operatorAddress) pipeTo sender()
    }
    case GetTotalVotingPower(_) => {
      blockchainValidator.Service.getTotalVotingPower pipeTo sender()
    }
  }
}

case class CreateValidatorWithActor(id: String, validator: Validator)
case class InsertMultipleValidators(id: String, validators: Seq[Validator])
case class InsertOrUpdateValidator(id: String, validator: Validator)
case class TryGetValidator(id: String, address: String)
case class GetAllValidatorByHexAddresses(id: String, hexAddresses: Seq[String])
case class TryGetValidatorByOperatorAddress(id: String, operatorAddress: String)
case class TryGetValidatorByHexAddress(id: String, hexAddress: String)
case class TryGetValidatorOperatorAddress(id: String, hexAddress: String)
case class TryGetValidatorHexAddress(id: String, operatorAddress: String)
case class TryGetValidatorProposerName(id: String, hexAddress: String)
case class GetAllValidators(id: String)
case class GetAllActiveValidatorList(id: String)
case class GetAllInactiveValidatorList(id: String)
case class GetAllUnbondingValidatorList(id: String)
case class GetAllUnbondedValidatorList(id: String)
case class GetValidatorByOperatorAddresses(id: String, operatorAddresses: Seq[String])
case class ValidatorExists(id: String, operatorAddress: String)
case class JailValidator(id: String, operatorAddress: String)
case class DeleteValidatorWithOpAddress(id: String, operatorAddress: String)
case class GetTotalVotingPower(id: String)