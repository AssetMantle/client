package dbActors

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Maintainer}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object MaintainerActor {
  def props(blockchainMaintainer: models.blockchain.Maintainers) = Props(new MaintainerActor(blockchainMaintainer))
}

@Singleton
class MaintainerActor @Inject()(
                                 blockchainMaintainer: models.blockchain.Maintainers
                               )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateMaintainer(maintainer) => {
      blockchainMaintainer.Service.create(maintainer) pipeTo sender()
      println(self.path)
    }
    case InsertMultipleMaintainer(maintainers) => {
      blockchainMaintainer.Service.insertMultiple(maintainers) pipeTo sender()
      println(self.path)
    }
    case InsertOrUpdateMaintainer(maintainer) => {
      blockchainMaintainer.Service.insertOrUpdate(maintainer) pipeTo sender()
      println(self.path)
    }
    case TryGetMaintainer(id) => {
      blockchainMaintainer.Service.tryGet(id) pipeTo sender()
      println(self.path)
    }
    case GetMaintainer(id) => {
      blockchainMaintainer.Service.get(id) pipeTo sender()
      println(self.path)
    }

    case GetAllMaintainer() => {
      blockchainMaintainer.Service.getAll pipeTo sender()
      println(self.path)
    }
    case DeleteMaintainer(id) => {
      blockchainMaintainer.Service.delete(id) pipeTo sender()
      println(self.path)
    }
  }

}

case class CreateMaintainer(maintainer: Maintainer)
case class TryGetMaintainer(id: String)
case class InsertMultipleMaintainer(maintainer: Seq[Maintainer])
case class InsertOrUpdateMaintainer(maintainer: Maintainer)
case class GetAllMaintainer()
case class GetMaintainer(id: String)
case class DeleteMaintainer(id: String)

