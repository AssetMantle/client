package actors.blockchainModels

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.blockchain.{Maintainer}
import play.api.Logger
import javax.inject.{Inject, Singleton}
import constants.Actor.{NUMBER_OF_SHARDS, NUMBER_OF_ENTITIES}

object MaintainerActor {
  def props(blockchainMaintainers: models.blockchain.Maintainers) = Props(new MaintainerActor(blockchainMaintainers))

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateMaintainer(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetMaintainer(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertMultipleMaintainer(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateMaintainer(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllMaintainer(uid) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetMaintainer(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@DeleteMaintainer(uid, _) => ((uid.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateMaintainer(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetMaintainer(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertMultipleMaintainer(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateMaintainer(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllMaintainer(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetMaintainer(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case DeleteMaintainer(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }
}

@Singleton
class MaintainerActor @Inject()(
                                 blockchainMaintainers: models.blockchain.Maintainers
                               )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateMaintainer(_, maintainer) => {
      blockchainMaintainers.Service.create(maintainer) pipeTo sender()
    }
    case InsertMultipleMaintainer(_, maintainers) => {
      blockchainMaintainers.Service.insertMultiple(maintainers) pipeTo sender()
    }
    case InsertOrUpdateMaintainer(_, maintainer) => {
      blockchainMaintainers.Service.insertOrUpdate(maintainer) pipeTo sender()
    }
    case TryGetMaintainer(_, id) => {
      blockchainMaintainers.Service.tryGet(id) pipeTo sender()
    }
    case GetMaintainer(_, id) => {
      blockchainMaintainers.Service.get(id) pipeTo sender()
    }
    case GetAllMaintainer(_) => {
      blockchainMaintainers.Service.getAll pipeTo sender()
    }
    case DeleteMaintainer(_, id) => {
      blockchainMaintainers.Service.delete(id) pipeTo sender()
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

