package dbActors

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Asset, Balance}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object AssetActor {
  def props(blockchainAsset: models.blockchain.Assets) = Props(new AssetActor(blockchainAsset))
}

@Singleton
class AssetActor @Inject()(
                            blockchainAsset: models.blockchain.Assets
                            )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateAsset(asset) => {
      blockchainAsset.Service.create(asset) pipeTo sender()
    }
    case TryGetAsset(id) => {
      blockchainAsset.Service.tryGet(id) pipeTo sender()
    }
    case GetAsset(id) => {
      blockchainAsset.Service.get(id) pipeTo sender()
    }
    case GetAllAsset() => {
      blockchainAsset.Service.getAll pipeTo sender()
    }
    case InsertMultipleAssets(assets) => {
      blockchainAsset.Service.insertMultiple(assets) pipeTo sender()
    }
    case InsertOrUpdateAsset(asset) => {
      blockchainAsset.Service.insertOrUpdate(asset) pipeTo sender()
    }
    case DeleteAsset(id) => {
      blockchainAsset.Service.delete(id) pipeTo sender()
    }
    case CheckExistsAsset(id) => {
      blockchainAsset.Service.checkExistsAssetWithActor(id) pipeTo sender()
    }
  }

}

case class CreateAsset(asset: Asset)
case class TryGetAsset(id: String)
case class GetAsset(id: String)
case class GetAllAsset()
case class InsertMultipleAssets(assets: Seq[Asset])
case class InsertOrUpdateAsset(asset: Asset)
case class DeleteAsset(id: String)
case class CheckExistsAsset(id: String)
