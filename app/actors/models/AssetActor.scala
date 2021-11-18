package actors.models

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Asset, Balance}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object AssetActor {
  def props(blockchainAsset: models.blockchain.Assets) = Props(new AssetActor(blockchainAsset))

  val numberOfEntities = 10
  val numberOfShards = 100

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateAsset(uid, _) => (uid, attempt)
    case attempt@TryGetAsset(uid, _) => (uid, attempt)
    case attempt@GetAsset(uid, _) => (uid, attempt)
    case attempt@GetAllAsset(uid) => (uid, attempt)
    case attempt@InsertMultipleAssets(uid, _) => (uid, attempt)
    case attempt@InsertOrUpdateAsset(uid, _) => (uid, attempt)
    case attempt@DeleteAsset(uid, _) => (uid, attempt)
    case attempt@CheckExistsAsset(uid, _) => (uid, attempt)

  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateAsset(id, _) => (id.hashCode % numberOfShards).toString
    case TryGetAsset(id, _) => (id.hashCode % numberOfShards).toString
    case GetAsset(id, _) => (id.hashCode % numberOfShards).toString
    case GetAllAsset(id) => (id.hashCode % numberOfShards).toString
    case InsertMultipleAssets(id, _) => (id.hashCode % numberOfShards).toString
    case InsertOrUpdateAsset(id, _) => (id.hashCode % numberOfShards).toString
    case DeleteAsset(id, _) => (id.hashCode % numberOfShards).toString
    case CheckExistsAsset(id, _) => (id.hashCode % numberOfShards).toString

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
