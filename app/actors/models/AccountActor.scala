package actors.models

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}

object AccountActor {
  def props(blockchainAccount: models.blockchain.Accounts) = Props(new AccountActor(blockchainAccount))

  val numberOfEntities = 10
  val numberOfShards = 100

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@GetAccount(id, _) => (id, attempt)
    case attempt@TryGetAccount(id, _) => (id, attempt)
    case attempt@CreateAccount(id, _, _, _, _) => (id, attempt)
    case attempt@InsertOrUpdateAccount(id, _) => (id, attempt)
    case attempt@GetListAccount(id, _) => (id, attempt)
    case attempt@TryGetByUsernameAccount(id, _) => (id, attempt)
    case attempt@TryGetUsernameAccount(id, _) => (id, attempt)
    case attempt@GetUsernameAccount(id, _) => (id, attempt)
    case attempt@TryGetAddressAccount(id, _) => (id, attempt)
    case attempt@CheckAccountExists(id, _) => (id, attempt)
  }
  val shardResolver: ShardRegion.ExtractShardId = {
    case GetAccount(id, _) => (id.hashCode % numberOfShards).toString
    case TryGetAccount(id, _) => (id.hashCode % numberOfShards).toString
    case CreateAccount(id, _, _, _, _) => (id.hashCode % numberOfShards).toString
    case InsertOrUpdateAccount(id, _) => (id.hashCode % numberOfShards).toString
    case GetListAccount(id, _) => (id.hashCode % numberOfShards).toString
    case TryGetByUsernameAccount(id, _) => (id.hashCode % numberOfShards).toString
    case TryGetUsernameAccount(id, _) => (id.hashCode % numberOfShards).toString
    case GetUsernameAccount(id, _) => (id.hashCode % numberOfShards).toString
    case TryGetAddressAccount(id, _) => (id.hashCode % numberOfShards).toString
    case CheckAccountExists(id, _) => (id.hashCode % numberOfShards).toString
  }
}

@Singleton
class AccountActor @Inject()(
                              blockchainAccount: models.blockchain.Accounts
                               )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case GetAccount(_, address) => {
      blockchainAccount.Service.get(address) pipeTo sender()
    }
    case CreateAccount(_, address, username, accountType, publicKey) => {
      blockchainAccount.Service.create(address, username, accountType, publicKey) pipeTo sender()
    }
    case TryGetAccount(_, address) => {
      blockchainAccount.Service.tryGet(address) pipeTo sender()
    }
    case InsertOrUpdateAccount(_, account) => {
      blockchainAccount.Service.insertOrUpdate(account) pipeTo sender()
    }
    case GetListAccount(_, addresses) => {
      blockchainAccount.Service.getList(addresses) pipeTo sender()
    }
    case TryGetByUsernameAccount(_, username) => {
      blockchainAccount.Service.tryGetByUsername(username) pipeTo sender()
    }
    case TryGetUsernameAccount(_, username) => {
      blockchainAccount.Service.tryGetUsername(username) pipeTo sender()
    }
    case GetUsernameAccount(_, address) => {
      blockchainAccount.Service.getUsername(address) pipeTo sender()
    }
    case TryGetAddressAccount(_, address) => {
      blockchainAccount.Service.tryGetAddress(address) pipeTo sender()
    }
    case CheckAccountExists(_, address) => {
      blockchainAccount.Service.checkAccountExists(address) pipeTo sender()
    }
  }

}


case class GetAccount(id:String, address: String)
case class TryGetAccount(id:String, address: String)
case class CreateAccount(id:String, address: String, username: String, accountType: String, publicKey: Option[PublicKey])
case class InsertOrUpdateAccount(id:String, account: Account)
case class GetListAccount(id:String, addresses: Seq[String])
case class TryGetByUsernameAccount(id:String, username: String)
case class TryGetUsernameAccount(id:String, address: String)
case class GetUsernameAccount(id:String, address: String)
case class TryGetAddressAccount(id:String, username: String)
case class CheckAccountExists(id:String, username: String)