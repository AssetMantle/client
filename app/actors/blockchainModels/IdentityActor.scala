package actors.blockchainModels

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.blockchain.Identity
import play.api.Logger
import javax.inject.{Inject, Singleton}
import constants.Actor.{NUMBER_OF_SHARDS, NUMBER_OF_ENTITIES}

object IdentityActor {
  def props(blockchainIdentity: models.blockchain.Identities) = Props(new IdentityActor(blockchainIdentity))

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateIdentity(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetIdentity(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetIdentity(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertMultipleIdentity(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@DeleteIdentity(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllIDsByProvisionedIdentity(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllIDsByUnProvisionedIdentity(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@CheckExistsIdentity(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllProvisionAddressesIdentity(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllUnprovisionedAddressesIdentity(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@AddProvisionAddressIdentity(id, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@DeleteProvisionAddressIdentity(id, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@AddUnprovisionedAddressIdentity(id, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@DeleteUnprovisionedAddressIdentity(id, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateIdentity(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetIdentity(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetIdentity(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertMultipleIdentity(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case DeleteIdentity(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllIDsByProvisionedIdentity(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllIDsByUnProvisionedIdentity(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case CheckExistsIdentity(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllProvisionAddressesIdentity(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllUnprovisionedAddressesIdentity(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case AddProvisionAddressIdentity(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case DeleteProvisionAddressIdentity(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case AddUnprovisionedAddressIdentity(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case DeleteUnprovisionedAddressIdentity(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }
}

@Singleton
class IdentityActor @Inject()(
                                 blockchainIdentity: models.blockchain.Identities
                               )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateIdentity(_, identity) => {
      blockchainIdentity.Service.create(identity) pipeTo sender()
    }
    case TryGetIdentity(_, id) => {
      blockchainIdentity.Service.tryGet(id) pipeTo sender()
    }
    case GetIdentity(_, id) => {
      blockchainIdentity.Service.get(id) pipeTo sender()
    }
    case InsertMultipleIdentity(_, identities) => {
      blockchainIdentity.Service.insertMultiple(identities) pipeTo sender()
    }
    case DeleteIdentity(_, id) => {
      blockchainIdentity.Service.delete(id) pipeTo sender()
    }
    case GetAllIDsByProvisionedIdentity(_, address) => {
      blockchainIdentity.Service.getAllIDsByProvisioned(address) pipeTo sender()
    }
    case GetAllIDsByUnProvisionedIdentity(_, address) => {
      blockchainIdentity.Service.getAllIDsByUnprovisioned(address) pipeTo sender()
    }
    case CheckExistsIdentity(_, id) => {
      blockchainIdentity.Service.checkExists(id) pipeTo sender()
    }
    case GetAllProvisionAddressesIdentity(_, id) => {
      blockchainIdentity.Service.getAllProvisionAddresses(id) pipeTo sender()
    }
    case GetAllUnprovisionedAddressesIdentity(_, id) => {
      blockchainIdentity.Service.getAllUnprovisionAddresses(id) pipeTo sender()
    }
    case AddProvisionAddressIdentity(_, id, address) => {
      blockchainIdentity.Service.addProvisionAddress(id, address) pipeTo sender()
    }
    case DeleteProvisionAddressIdentity(_, id, address) => {
      blockchainIdentity.Service.deleteProvisionAddress(id, address) pipeTo sender()
    }
    case AddUnprovisionedAddressIdentity(_, id, address) => {
      blockchainIdentity.Service.addUnprovisionAddress(id, address) pipeTo sender()
    }
    case DeleteUnprovisionedAddressIdentity(_, id, address) => {
      blockchainIdentity.Service.deleteUnprovisionAddress(id, address) pipeTo sender()
    }
  }
}

case class CreateIdentity(uid: String, identity: Identity)
case class TryGetIdentity(uid: String, id: String)
case class GetIdentity(uid: String, id: String)
case class InsertMultipleIdentity(uid: String, identities: Seq[Identity])
case class DeleteIdentity(uid: String, id: String)
case class GetAllIDsByProvisionedIdentity(uid: String, address: String)
case class GetAllIDsByUnProvisionedIdentity(uid: String, address: String)
case class CheckExistsIdentity(uid: String, id: String)
case class GetAllProvisionAddressesIdentity(uid: String, id: String)
case class GetAllUnprovisionedAddressesIdentity(uid: String, id: String)
case class AddProvisionAddressIdentity(uid: String, id: String, address: String)
case class DeleteProvisionAddressIdentity(uid: String, id: String, address: String)
case class AddUnprovisionedAddressIdentity(uid: String, id: String, address: String)
case class DeleteUnprovisionedAddressIdentity(uid: String, id: String, address: String)
