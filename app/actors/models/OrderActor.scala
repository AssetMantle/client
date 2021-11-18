package actors.models

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Order}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object OrderActor {
  def props(blockchainOrder: models.blockchain.Orders) = Props(new OrderActor(blockchainOrder))

  val numberOfEntities = 10
  val numberOfShards = 100

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateOrder(id, _) => (id, attempt)
    case attempt@TryGetOrder(id, _) => (id, attempt)
    case attempt@GetOrder(id, _) => (id, attempt)
    case attempt@InsertMultipleOrder(id, _) => (id, attempt)
    case attempt@DeleteOrder(id, _) => (id, attempt)
    case attempt@InsertOrUpdateOrder(id, _) => (id, attempt)
    case attempt@CheckExistsOrder(id, _) => (id, attempt)
    case attempt@GetAllOrder(id) => (id, attempt)
    case attempt@GetAllPublicOrderIDs(id) => (id, attempt)
    case attempt@GetAllPrivateOrderIDs(id, _) => (id, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateOrder(id, _) => (id.hashCode % numberOfShards).toString
    case TryGetOrder(id, _) => (id.hashCode % numberOfShards).toString
    case GetOrder(id, _) => (id.hashCode % numberOfShards).toString
    case InsertMultipleOrder(id, _) => (id.hashCode % numberOfShards).toString
    case DeleteOrder(id, _) => (id.hashCode % numberOfShards).toString
    case InsertOrUpdateOrder(id, _) => (id.hashCode % numberOfShards).toString
    case CheckExistsOrder(id, _) => (id.hashCode % numberOfShards).toString
    case GetAllOrder(id) => (id.hashCode % numberOfShards).toString
    case GetAllPublicOrderIDs(id) => (id.hashCode % numberOfShards).toString
    case GetAllPrivateOrderIDs(id, _) => (id.hashCode % numberOfShards).toString
  }
}

@Singleton
class OrderActor @Inject()(
                               blockchainOrder: models.blockchain.Orders
                             )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateOrder(_, order) => {
      blockchainOrder.Service.create(order) pipeTo sender()
    }
    case TryGetOrder(_, id) => {
      blockchainOrder.Service.tryGet(id) pipeTo sender()
    }
    case GetOrder(_, id) => {
      blockchainOrder.Service.get(id) pipeTo sender()
    }
    case InsertMultipleOrder(_, orders) => {
      blockchainOrder.Service.insertMultiple(orders) pipeTo sender()
    }
    case DeleteOrder(_, id) => {
      blockchainOrder.Service.delete(id) pipeTo sender()
    }
    case GetAllOrder(_) => {
      blockchainOrder.Service.getAll pipeTo sender()
    }
    case CheckExistsOrder(_, id) => {
      blockchainOrder.Service.checkExists(id) pipeTo sender()
    }
    case InsertOrUpdateOrder(_, order) => {
      blockchainOrder.Service.insertOrUpdate(order) pipeTo sender()
    }
    case GetAllPublicOrderIDs(_) => {
      blockchainOrder.Service.getAllPublicOrderIDs pipeTo sender()
    }
    case GetAllPrivateOrderIDs(_, identityIDs) => {
      blockchainOrder.Service.getAllPrivateOrderIDs(identityIDs) pipeTo sender()
    }
  }

}

case class CreateOrder(uid: String, order: Order)
case class TryGetOrder(uid: String, id: String)
case class GetOrder(uid: String, id: String)
case class InsertMultipleOrder(uid: String, order: Seq[Order])
case class DeleteOrder(uid: String, id: String)
case class InsertOrUpdateOrder(uid: String, order: Order)
case class CheckExistsOrder(uid: String, id: String)
case class GetAllOrder(uid: String)
case class GetAllPublicOrderIDs(uid: String)
case class GetAllPrivateOrderIDs(uid: String, identityIDs: Seq[String])
