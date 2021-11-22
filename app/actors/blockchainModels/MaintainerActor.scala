package actors.blockchainModels

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Maintainer}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object MaintainerActor {
  def props(blockchainMaintainer: models.blockchain.Maintainers) = Props(new MaintainerActor(blockchainMaintainer))

  val numberOfEntities = 10
  val numberOfShards = 100

  val idExtractor: ShardRegion.ExtractEntityId = {

    case attempt@CreateMaintainer(uid, _) => (uid, attempt)
    case attempt@TryGetMaintainer(uid, _) => (uid, attempt)
    case attempt@InsertMultipleMaintainer(uid, _) => (uid, attempt)
    case attempt@InsertOrUpdateMaintainer(uid, _) => (uid, attempt)
    case attempt@GetAllMaintainer(uid) => (uid, attempt)
    case attempt@GetMaintainer(uid, _) => (uid, attempt)
    case attempt@DeleteMaintainer(uid, _) => (uid, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateMaintainer(id, _) => (id.hashCode % numberOfShards).toString
    case TryGetMaintainer(id, _) => (id.hashCode % numberOfShards).toString
    case InsertMultipleMaintainer(id, _) => (id.hashCode % numberOfShards).toString
    case InsertOrUpdateMaintainer(id, _) => (id.hashCode % numberOfShards).toString
    case GetAllMaintainer(id) => (id.hashCode % numberOfShards).toString
    case GetMaintainer(id, _) => (id.hashCode % numberOfShards).toString
    case DeleteMaintainer(id, _) => (id.hashCode % numberOfShards).toString

  }
}

@Singleton
class MaintainerActor @Inject()(
                                 blockchainMaintainer: models.blockchain.Maintainers
                               )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateMaintainer(_, maintainer) => {
      blockchainMaintainer.Service.create(maintainer) pipeTo sender()
    }
    case InsertMultipleMaintainer(_, maintainers) => {
      blockchainMaintainer.Service.insertMultiple(maintainers) pipeTo sender()
    }
    case InsertOrUpdateMaintainer(_, maintainer) => {
      blockchainMaintainer.Service.insertOrUpdate(maintainer) pipeTo sender()
    }
    case TryGetMaintainer(_, id) => {
      blockchainMaintainer.Service.tryGet(id) pipeTo sender()
    }
    case GetMaintainer(_, id) => {
      blockchainMaintainer.Service.get(id) pipeTo sender()
    }
    case GetAllMaintainer(_) => {
      blockchainMaintainer.Service.getAll pipeTo sender()
    }
    case DeleteMaintainer(_, id) => {
      blockchainMaintainer.Service.delete(id) pipeTo sender()
    }
  }

}

case class CreateMaintainer(uid: String, maintainer: Maintainer)
case class TryGetMaintainer(uid: String, id: String)
case class InsertMultipleMaintainer(uid: String, maintainer: Seq[Maintainer])
case class InsertOrUpdateMaintainer(uid: String, maintainer: Maintainer)
case class GetAllMaintainer(uid: String)
case class GetMaintainer(uid: String, id: String)
case class DeleteMaintainer(uid: String, id: String)

