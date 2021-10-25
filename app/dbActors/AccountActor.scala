package dbActors

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}

object AccountActor {
  def props(blockchainAccount: models.blockchain.Accounts) = Props(new AccountActor(blockchainAccount))
}

@Singleton
class AccountActor @Inject()(
                              blockchainAccount: models.blockchain.Accounts
                               )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case GetAccount(address) => {
      println(s"address is fetched $address")
      blockchainAccount.Service.get(address) pipeTo sender()
      println(self.path)
    }
    case CreateAccount(address, username, accountType, publicKey) => {
      blockchainAccount.Service.create(address, username, accountType, publicKey) pipeTo sender()
      println(self.path)
    }
    case TryGetAccount(address) => {
      blockchainAccount.Service.tryGet(address) pipeTo sender()
      println(self.path)
    }
    case InsertOrUpdateAccount(account) => {
      blockchainAccount.Service.insertOrUpdate(account) pipeTo sender()
      println(self.path)
    }
    case GetListAccount(addresses) => {
      blockchainAccount.Service.getList(addresses) pipeTo sender()
      println(self.path)
    }
    case TryGetByUsernameAccount(username) => {
      blockchainAccount.Service.tryGetByUsername(username) pipeTo sender()
      println(self.path)
    }
    case TryGetUsernameAccount(username) => {
      blockchainAccount.Service.tryGetUsername(username) pipeTo sender()
      println(self.path)
    }
    case GetUsernameAccount(address) => {
      blockchainAccount.Service.getUsername(address) pipeTo sender()
      println(self.path)
    }
    case TryGetAddressAccount(address) => {
      blockchainAccount.Service.tryGetAddress(address) pipeTo sender()
      println(self.path)
    }
    case CheckAccountExists(address) => {
      blockchainAccount.Service.checkAccountExists(address) pipeTo sender()
      println(self.path)
    }
  }

}


case class GetAccount(address: String)
case class TryGetAccount(address: String)
case class CreateAccount(address: String, username: String, accountType: String, publicKey: Option[PublicKey])
case class InsertOrUpdateAccount(account: Account)
case class GetListAccount(addresses: Seq[String])
case class TryGetByUsernameAccount(username: String)
case class TryGetUsernameAccount(address: String)
case class GetUsernameAccount(address: String)
case class TryGetAddressAccount(username: String)
case class CheckAccountExists(username: String)