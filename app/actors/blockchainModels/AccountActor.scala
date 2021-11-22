package actors.blockchainModels

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance}
import models.common.Serializable.Coin
import play.api.Logger
import constants.Actor.{NUMBER_OF_SHARDS, NUMBER_OF_ENTITIES}

import javax.inject.{Inject, Singleton}

object AccountActor {
  def props(blockchainAccount: models.blockchain.Accounts) = Props(new AccountActor(blockchainAccount))

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@GetAccount(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetAccount(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@CreateAccount(id, _, _, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateAccount(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetListAccount(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetByUsernameAccount(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetUsernameAccount(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetUsernameAccount(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetAddressAccount(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@CheckAccountExists(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }
  val shardResolver: ShardRegion.ExtractShardId = {
    case GetAccount(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetAccount(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case CreateAccount(id, _, _, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateAccount(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetListAccount(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetByUsernameAccount(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetUsernameAccount(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetUsernameAccount(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetAddressAccount(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case CheckAccountExists(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
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