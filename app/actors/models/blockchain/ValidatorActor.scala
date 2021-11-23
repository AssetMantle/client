package actors.models.blockchain

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.blockchain.{Validator}
import play.api.Logger
import javax.inject.{Inject, Singleton}
import constants.Actor.{NUMBER_OF_SHARDS, NUMBER_OF_ENTITIES}

object ValidatorActor {
  def props(blockchainValidators: models.blockchain.Validators) = Props(new ValidatorActor(blockchainValidators))

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateValidatorWithActor(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertMultipleValidators(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateValidator(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetValidator(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllValidatorByHexAddresses(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetValidatorByOperatorAddress(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetValidatorByHexAddress(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetValidatorOperatorAddress(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetValidatorHexAddress(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetValidatorProposerName(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllValidators(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllActiveValidatorList(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllInactiveValidatorList(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllUnbondingValidatorList(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllUnbondedValidatorList(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetValidatorByOperatorAddresses(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@ValidatorExists(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@JailValidator(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@DeleteValidatorWithOpAddress(id, _)  => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetTotalVotingPower(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateValidatorWithActor(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertMultipleValidators(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateValidator(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetValidator(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllValidatorByHexAddresses(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetValidatorByOperatorAddress(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetValidatorByHexAddress(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetValidatorOperatorAddress(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetValidatorHexAddress(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetValidatorProposerName(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllValidators(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllActiveValidatorList(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllInactiveValidatorList(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllUnbondingValidatorList(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllUnbondedValidatorList(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetValidatorByOperatorAddresses(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case ValidatorExists(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case JailValidator(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case DeleteValidatorWithOpAddress(id, _)  => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetTotalVotingPower(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }
}

@Singleton
class ValidatorActor @Inject()(
                               blockchainValidators: models.blockchain.Validators
                             )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateValidatorWithActor(_, validator) => {
      blockchainValidators.Service.create(validator) pipeTo sender()
    }
    case InsertMultipleValidators(_, validators) => {
      blockchainValidators.Service.insertMultiple(validators) pipeTo sender()
    }
    case InsertOrUpdateValidator(_, validator) => {
      blockchainValidators.Service.insertOrUpdate(validator) pipeTo sender()
    }
    case TryGetValidator(_, id) => {
      blockchainValidators.Service.tryGet(id) pipeTo sender()
    }
    case GetAllValidatorByHexAddresses(_, hexAddresses) => {
      blockchainValidators.Service.getAllByHexAddresses(hexAddresses) pipeTo sender()
    }
    case TryGetValidatorByOperatorAddress(_, operatorAddress) => {
      blockchainValidators.Service.tryGetByOperatorAddress(operatorAddress) pipeTo sender()
    }
    case TryGetValidatorByHexAddress(_, hexAddress) => {
      blockchainValidators.Service.tryGetByHexAddress(hexAddress) pipeTo sender()
    }
    case TryGetValidatorOperatorAddress(_, hexAddress) => {
      blockchainValidators.Service.tryGetOperatorAddress(hexAddress) pipeTo sender()
    }
    case TryGetValidatorHexAddress(_, operatorAddress) => {
      blockchainValidators.Service.tryGetHexAddress(operatorAddress) pipeTo sender()
    }
    case TryGetValidatorProposerName(_, hexAddress) => {
      blockchainValidators.Service.tryGetProposerName(hexAddress) pipeTo sender()
    }
    case GetAllValidators(_) => {
      blockchainValidators.Service.getAll pipeTo sender()
    }
    case GetAllActiveValidatorList(_) => {
      blockchainValidators.Service.getAllActiveValidatorList pipeTo sender()
    }
    case GetAllInactiveValidatorList(_) => {
      blockchainValidators.Service.getAllInactiveValidatorList pipeTo sender()
    }
    case GetAllUnbondingValidatorList(_) => {
      blockchainValidators.Service.getAllUnbondingValidatorList pipeTo sender()
    }
    case GetAllUnbondedValidatorList(_) => {
      blockchainValidators.Service.getAllUnbondedValidatorList pipeTo sender()
    }
    case GetValidatorByOperatorAddresses(_, operatorAddresses) => {
      blockchainValidators.Service.getByOperatorAddresses(operatorAddresses) pipeTo sender()
    }
    case ValidatorExists(_, operatorAddress) => {
      blockchainValidators.Service.exists(operatorAddress) pipeTo sender()
    }
    case JailValidator(_, operatorAddress) => {
      blockchainValidators.Service.jailValidator(operatorAddress) pipeTo sender()
    }
    case DeleteValidatorWithOpAddress(_, operatorAddress)  => {
      blockchainValidators.Service.delete(operatorAddress) pipeTo sender()
    }
    case GetTotalVotingPower(_) => {
      blockchainValidators.Service.getTotalVotingPower pipeTo sender()
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