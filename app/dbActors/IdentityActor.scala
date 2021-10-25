package dbActors

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Identity}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object IdentityActor {
  def props(blockchainIdentity: models.blockchain.Identities) = Props(new IdentityActor(blockchainIdentity))
}

@Singleton
class IdentityActor @Inject()(
                                 blockchainIdentity: models.blockchain.Identities
                               )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateIdentity(identity) => {
      blockchainIdentity.Service.create(identity) pipeTo sender()
      println(self.path)
    }
    case TryGetIdentity(id) => {
      blockchainIdentity.Service.tryGet(id) pipeTo sender()
      println(self.path)
    }
    case GetIdentity(id) => {
      blockchainIdentity.Service.get(id) pipeTo sender()
      println(self.path)
    }
    case InsertMultipleIdentity(identities) => {
      blockchainIdentity.Service.insertMultiple(identities) pipeTo sender()
      println(self.path)
    }

    case DeleteIdentity(id) => {
      blockchainIdentity.Service.delete(id) pipeTo sender()
      println(self.path)
    }
    case GetAllIDsByProvisionedIdentity(address) => {
      blockchainIdentity.Service.getAllIDsByProvisioned(address) pipeTo sender()
      println(self.path)
    }
    case GetAllIDsByUnProvisionedIdentity(address) => {
      blockchainIdentity.Service.delete(address) pipeTo sender()
      println(self.path)
    }
    case CheckExistsIdentity(id) => {
      blockchainIdentity.Service.checkExists(id) pipeTo sender()
      println(self.path)
    }
    case GetAllProvisionAddressesIdentity(id) => {
      blockchainIdentity.Service.getAllProvisionAddresses(id) pipeTo sender()
      println(self.path)
    }
    case GetAllUnprovisionedAddressesIdentity(id) => {
      blockchainIdentity.Service.getAllUnprovisionAddresses(id) pipeTo sender()
      println(self.path)
    }
    case AddProvisionAddressIdentity(id, address) => {
      blockchainIdentity.Service.addProvisionAddress(id, address) pipeTo sender()
      println(self.path)
    }
    case DeleteProvisionAddressIdentity(id, address) => {
      blockchainIdentity.Service.deleteProvisionAddress(id, address) pipeTo sender()
      println(self.path)
    }
    case AddUnprovisionedAddressIdentity(id, address) => {
      blockchainIdentity.Service.addUnprovisionAddress(id, address) pipeTo sender()
      println(self.path)
    }
    case DeleteUnprovisionedAddressIdentity(id, address) => {
      blockchainIdentity.Service.deleteUnprovisionAddress(id, address) pipeTo sender()
      println(self.path)
    }
  }

}

case class CreateIdentity(identity: Identity)
case class TryGetIdentity(id: String)
case class GetIdentity(id: String)
case class InsertMultipleIdentity(identities: Seq[Identity])
case class DeleteIdentity(id: String)
case class GetAllIDsByProvisionedIdentity(address: String)
case class GetAllIDsByUnProvisionedIdentity(address: String)
case class CheckExistsIdentity(id: String)
case class GetAllProvisionAddressesIdentity(id: String)
case class GetAllUnprovisionedAddressesIdentity(id: String)
case class AddProvisionAddressIdentity(id: String, address: String)
case class DeleteProvisionAddressIdentity(id: String, address: String)
case class AddUnprovisionedAddressIdentity(id: String, address: String)
case class DeleteUnprovisionedAddressIdentity(id: String, address: String)
