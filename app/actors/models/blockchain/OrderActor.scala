package actors.models.blockchain

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.blockchain.{Order}
import play.api.Logger
import javax.inject.{Inject, Singleton}
import constants.Actor.{NUMBER_OF_SHARDS, NUMBER_OF_ENTITIES}

object OrderActor {
  def props(blockchainOrders: models.blockchain.Orders) = Props(new OrderActor(blockchainOrders))
  
  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateOrder(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetOrder(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetOrder(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertMultipleOrder(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@DeleteOrder(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateOrder(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@CheckExistsOrder(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllOrder(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllPublicOrderIDs(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllPrivateOrderIDs(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateOrder(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetOrder(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetOrder(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertMultipleOrder(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case DeleteOrder(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateOrder(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case CheckExistsOrder(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllOrder(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllPublicOrderIDs(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllPrivateOrderIDs(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }
}

@Singleton
class OrderActor @Inject()(
                               blockchainOrders: models.blockchain.Orders
                             )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateOrder(_, order) => {
      blockchainOrders.Service.create(order) pipeTo sender()
    }
    case TryGetOrder(_, id) => {
      blockchainOrders.Service.tryGet(id) pipeTo sender()
    }
    case GetOrder(_, id) => {
      blockchainOrders.Service.get(id) pipeTo sender()
    }
    case InsertMultipleOrder(_, orders) => {
      blockchainOrders.Service.insertMultiple(orders) pipeTo sender()
    }
    case DeleteOrder(_, id) => {
      blockchainOrders.Service.delete(id) pipeTo sender()
    }
    case GetAllOrder(_) => {
      blockchainOrders.Service.getAll pipeTo sender()
    }
    case CheckExistsOrder(_, id) => {
      blockchainOrders.Service.checkExists(id) pipeTo sender()
    }
    case InsertOrUpdateOrder(_, order) => {
      blockchainOrders.Service.insertOrUpdate(order) pipeTo sender()
    }
    case GetAllPublicOrderIDs(_) => {
      blockchainOrders.Service.getAllPublicOrderIDs pipeTo sender()
    }
    case GetAllPrivateOrderIDs(_, identityIDs) => {
      blockchainOrders.Service.getAllPrivateOrderIDs(identityIDs) pipeTo sender()
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
