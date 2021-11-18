package actors.models

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Split}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object SplitActor {
  def props(blockchainSplit: models.blockchain.Splits) = Props(new SplitActor(blockchainSplit))

  val numberOfEntities = 10
  val numberOfShards = 100

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateSplit(id, _) => (id, attempt)
    case attempt@GetByOwner(id, _) => (id, attempt)
    case attempt@GetByOwnerIDs(id, _) => (id, attempt)
    case attempt@GetByOwnable(id, _) => (id, attempt)
    case attempt@GetByOwnerOrOwnable(id, _) => (id, attempt)
    case attempt@GetAllSplit(id) => (id, attempt)
    case attempt@GetSplit(id, _, _) => (id, attempt)
    case attempt@TryGetSplit(id, _, _) => (id, attempt)
    case attempt@InsertMultipleSplit(id, _) => (id, attempt)
    case attempt@InsertOrUpdateSplit(id, _) => (id, attempt)
    case attempt@DeleteSplit(id, _, _) => (id, attempt)

  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateSplit(id, _) => (id.hashCode % numberOfShards).toString
    case GetByOwner(id, _) => (id.hashCode % numberOfShards).toString
    case GetByOwnerIDs(id, _) => (id.hashCode % numberOfShards).toString
    case GetByOwnable(id, _) => (id.hashCode % numberOfShards).toString
    case GetByOwnerOrOwnable(id, _) => (id.hashCode % numberOfShards).toString
    case GetAllSplit(id) => (id.hashCode % numberOfShards).toString
    case GetSplit(id, _, _) => (id.hashCode % numberOfShards).toString
    case TryGetSplit(id, _, _) => (id.hashCode % numberOfShards).toString
    case InsertMultipleSplit(id, _) => (id.hashCode % numberOfShards).toString
    case InsertOrUpdateSplit(id, _) => (id.hashCode % numberOfShards).toString
    case DeleteSplit(id, _, _) => (id.hashCode % numberOfShards).toString

  }
}

@Singleton
class SplitActor @Inject()(
                                   blockchainSplit: models.blockchain.Splits
                                 )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateSplit(_, split) => {
      blockchainSplit.Service.create(split) pipeTo sender()
    }
    case InsertMultipleSplit(_, splits) => {
      blockchainSplit.Service.insertMultiple(splits) pipeTo sender()
    }
    case InsertOrUpdateSplit(_, split) => {
      blockchainSplit.Service.insertOrUpdate(split) pipeTo sender()
    }
    case GetByOwner(_, ownerID) => {
      blockchainSplit.Service.getByOwner(ownerID) pipeTo sender()
    }
    case GetByOwnerIDs(_, ownerIDs) => {
      blockchainSplit.Service.getByOwnerIDs(ownerIDs) pipeTo sender()
    }
    case GetByOwnable(_, ownableID) => {
      blockchainSplit.Service.getByOwnable(ownableID) pipeTo sender()
    }
    case GetByOwnerOrOwnable(_, id) => {
      blockchainSplit.Service.getByOwnerOrOwnable(id) pipeTo sender()
    }
    case GetAllSplit(_) => {
      blockchainSplit.Service.getAll pipeTo sender()
    }
    case DeleteSplit(_, ownerID, ownableID) => {
      blockchainSplit.Service.delete(ownerID, ownableID) pipeTo sender()
    }
    case TryGetSplit(_, ownerID, ownableID) => {
      blockchainSplit.Service.tryGet(ownerID, ownableID) pipeTo sender()
    }
    case GetSplit(_, ownerID, ownableID) => {
      blockchainSplit.Service.get(ownerID, ownableID) pipeTo sender()
    }

  }

}

case class CreateSplit(uid: String, split: Split)
case class GetByOwner(uid: String, ownerID: String)
case class GetByOwnerIDs(uid: String, ownerIDs: Seq[String])
case class GetByOwnable(uid: String, ownableID: String)
case class GetByOwnerOrOwnable(uid: String, id: String)
case class GetAllSplit(uid: String)
case class GetSplit(uid: String, ownerID: String, ownableID: String)
case class TryGetSplit(uid: String, ownerID: String, ownableID: String)
case class InsertMultipleSplit(uid: String, splits: Seq[Split])
case class InsertOrUpdateSplit(uid: String, split: Split)
case class DeleteSplit(uid: String, ownerID: String, ownableID: String)

