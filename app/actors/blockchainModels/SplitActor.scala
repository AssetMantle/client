package actors.blockchainModels

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.blockchain.{Split}
import play.api.Logger
import javax.inject.{Inject, Singleton}
import constants.Actor.{NUMBER_OF_SHARDS, NUMBER_OF_ENTITIES}

object SplitActor {
  def props(blockchainSplits: models.blockchain.Splits) = Props(new SplitActor(blockchainSplits))

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateSplit(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetByOwner(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetByOwnerIDs(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetByOwnable(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetByOwnerOrOwnable(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllSplit(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetSplit(id, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetSplit(id, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertMultipleSplit(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateSplit(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@DeleteSplit(id, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)

  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateSplit(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetByOwner(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetByOwnerIDs(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetByOwnable(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetByOwnerOrOwnable(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllSplit(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetSplit(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetSplit(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertMultipleSplit(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateSplit(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case DeleteSplit(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString

  }
}

@Singleton
class SplitActor @Inject()(
                            blockchainSplits: models.blockchain.Splits
                          )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateSplit(_, split) => {
      blockchainSplits.Service.create(split) pipeTo sender()
    }
    case InsertMultipleSplit(_, splits) => {
      blockchainSplits.Service.insertMultiple(splits) pipeTo sender()
    }
    case InsertOrUpdateSplit(_, split) => {
      blockchainSplits.Service.insertOrUpdate(split) pipeTo sender()
    }
    case GetByOwner(_, ownerID) => {
      blockchainSplits.Service.getByOwner(ownerID) pipeTo sender()
    }
    case GetByOwnerIDs(_, ownerIDs) => {
      blockchainSplits.Service.getByOwnerIDs(ownerIDs) pipeTo sender()
    }
    case GetByOwnable(_, ownableID) => {
      blockchainSplits.Service.getByOwnable(ownableID) pipeTo sender()
    }
    case GetByOwnerOrOwnable(_, id) => {
      blockchainSplits.Service.getByOwnerOrOwnable(id) pipeTo sender()
    }
    case GetAllSplit(_) => {
      blockchainSplits.Service.getAll pipeTo sender()
    }
    case DeleteSplit(_, ownerID, ownableID) => {
      blockchainSplits.Service.delete(ownerID, ownableID) pipeTo sender()
    }
    case TryGetSplit(_, ownerID, ownableID) => {
      blockchainSplits.Service.tryGet(ownerID, ownableID) pipeTo sender()
    }
    case GetSplit(_, ownerID, ownableID) => {
      blockchainSplits.Service.get(ownerID, ownableID) pipeTo sender()
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

