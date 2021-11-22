package actors.blockchainModels

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.blockchain.{Validator}
import play.api.Logger
import javax.inject.{Inject, Singleton}
import constants.Actor.{NUMBER_OF_SHARDS, NUMBER_OF_ENTITIES}

object ValidatorActor {
  def props(blockchainValidator: models.blockchain.Validators) = Props(new ValidatorActor(blockchainValidator))

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
                               blockchainValidator: models.blockchain.Validators
                             )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
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