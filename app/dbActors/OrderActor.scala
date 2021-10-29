package dbActors

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Order}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object OrderActor {
  def props(blockchainOrder: models.blockchain.Orders) = Props(new OrderActor(blockchainOrder))
}

@Singleton
class OrderActor @Inject()(
                               blockchainOrder: models.blockchain.Orders
                             )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateOrder(order) => {
      blockchainOrder.Service.create(order) pipeTo sender()
    }
    case TryGetOrder(id) => {
      blockchainOrder.Service.tryGet(id) pipeTo sender()
    }
    case GetOrder(id) => {
      blockchainOrder.Service.get(id) pipeTo sender()
    }
    case InsertMultipleOrder(orders) => {
      blockchainOrder.Service.insertMultiple(orders) pipeTo sender()
    }
    case DeleteOrder(id) => {
      blockchainOrder.Service.delete(id) pipeTo sender()
    }
    case GetAllOrder() => {
      blockchainOrder.Service.getAll pipeTo sender()
    }
    case CheckExistsOrder(id) => {
      blockchainOrder.Service.checkExists(id) pipeTo sender()
    }
    case InsertOrUpdateOrder(order) => {
      blockchainOrder.Service.insertOrUpdate(order) pipeTo sender()
    }
    case GetAllPublicOrderIDs() => {
      blockchainOrder.Service.getAllPublicOrderIDs pipeTo sender()
    }
    case GetAllPrivateOrderIDs(identityIDs) => {
      blockchainOrder.Service.getAllPrivateOrderIDs(identityIDs) pipeTo sender()
    }
  }

}

case class CreateOrder(order: Order)
case class TryGetOrder(id: String)
case class GetOrder(id: String)
case class InsertMultipleOrder(order: Seq[Order])
case class DeleteOrder(id: String)
case class InsertOrUpdateOrder(order: Order)
case class CheckExistsOrder(id: String)
case class GetAllOrder()
case class GetAllPublicOrderIDs()
case class GetAllPrivateOrderIDs(identityIDs: Seq[String])
