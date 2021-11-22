package actors.blockchainModels

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.blockchain.{Asset}
import play.api.Logger
import constants.Actor.{NUMBER_OF_SHARDS, NUMBER_OF_ENTITIES}
import javax.inject.{Inject, Singleton}

object AssetActor {
  def props(blockchainAsset: models.blockchain.Assets) = Props(new AssetActor(blockchainAsset))

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateAsset(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetAsset(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAsset(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllAsset(uid) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertMultipleAssets(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateAsset(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@DeleteAsset(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@CheckExistsAsset(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateAsset(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetAsset(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAsset(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllAsset(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertMultipleAssets(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateAsset(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case DeleteAsset(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case CheckExistsAsset(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }
}

@Singleton
class AssetActor @Inject()(
                            blockchainAsset: models.blockchain.Assets
                            )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateAsset(_, asset) => {
      blockchainAsset.Service.create(asset) pipeTo sender()
    }
    case TryGetAsset(_, id) => {
      blockchainAsset.Service.tryGet(id) pipeTo sender()
    }
    case GetAsset(_, id) => {
      blockchainAsset.Service.get(id) pipeTo sender()
    }
    case GetAllAsset(_) => {
      blockchainAsset.Service.getAll pipeTo sender()
    }
    case InsertMultipleAssets(_, assets) => {
      blockchainAsset.Service.insertMultiple(assets) pipeTo sender()
    }
    case InsertOrUpdateAsset(_, asset) => {
      blockchainAsset.Service.insertOrUpdate(asset) pipeTo sender()
    }
    case DeleteAsset(_, id) => {
      blockchainAsset.Service.delete(id) pipeTo sender()
    }
    case CheckExistsAsset(_, id) => {
      blockchainAsset.Service.checkExistsAssetWithActor(id) pipeTo sender()
    }
  }

}

case class CreateAsset(uid: String, asset: Asset)
case class TryGetAsset(uid: String, id: String)
case class GetAsset(uid: String, id: String)
case class GetAllAsset(uid: String)
case class InsertMultipleAssets(uid: String, assets: Seq[Asset])
case class InsertOrUpdateAsset(uid: String, asset: Asset)
case class DeleteAsset(uid: String, id: String)
case class CheckExistsAsset(uid: String, id: String)
