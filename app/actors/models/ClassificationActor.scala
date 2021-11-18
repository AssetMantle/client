package actors.models

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Classification}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object ClassificationActor {
  def props(blockchainClassification: models.blockchain.Classifications) = Props(new ClassificationActor(blockchainClassification))

  val numberOfEntities = 10
  val numberOfShards = 100

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateClassification(uid, _) => (uid, attempt)
    case attempt@TryGetClassification(uid, _) => (uid, attempt)
    case attempt@GetClassification(uid, _) => (uid, attempt)
    case attempt@GetAllClassification(uid) => (uid, attempt)
    case attempt@InsertMultipleClassification(uid, _) => (uid, attempt)
    case attempt@InsertOrUpdateClassification(uid, _) => (uid, attempt)
    case attempt@DeleteClassification(uid, _) => (uid, attempt)
    case attempt@CheckExistsClassification(uid, _) => (uid, attempt)

  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateClassification(id, _) => (id.hashCode % numberOfShards).toString
    case TryGetClassification(id, _) => (id.hashCode % numberOfShards).toString
    case GetClassification(id, _) => (id.hashCode % numberOfShards).toString
    case GetAllClassification(id) => (id.hashCode % numberOfShards).toString
    case InsertMultipleClassification(id, _) => (id.hashCode % numberOfShards).toString
    case InsertOrUpdateClassification(id, _) => (id.hashCode % numberOfShards).toString
    case DeleteClassification(id, _) => (id.hashCode % numberOfShards).toString
    case CheckExistsClassification(id, _) => (id.hashCode % numberOfShards).toString

  }
}

@Singleton
class ClassificationActor @Inject()(
                            blockchainClassification: models.blockchain.Classifications
                          )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateClassification(_, classification) => {
      blockchainClassification.Service.create(classification) pipeTo sender()
    }
    case TryGetClassification(_, id) => {
      blockchainClassification.Service.tryGet(id) pipeTo sender()
    }
    case GetClassification(_, id) => {
      blockchainClassification.Service.get(id) pipeTo sender()
    }

    case GetAllClassification(_) => {
      blockchainClassification.Service.getAll pipeTo sender()
    }
    case InsertMultipleClassification(_, classifications) => {
      blockchainClassification.Service.insertMultiple(classifications) pipeTo sender()
    }
    case InsertOrUpdateClassification(_, classification) => {
      blockchainClassification.Service.insertOrUpdate(classification) pipeTo sender()
    }
    case DeleteClassification(_, id) => {
      blockchainClassification.Service.delete(id) pipeTo sender()
    }
    case CheckExistsClassification(_, id) => {
      blockchainClassification.Service.checkExists(id) pipeTo sender()
    }
  }

}

case class CreateClassification(uid: String, classification: Classification)
case class TryGetClassification(uid: String, id: String)
case class GetClassification(uid: String, id: String)
case class GetAllClassification(uid: String)
case class InsertMultipleClassification(uid: String, classifications: Seq[Classification])
case class InsertOrUpdateClassification(uid: String, classification: Classification)
case class DeleteClassification(uid: String, id: String)
case class CheckExistsClassification(uid: String, id: String)
