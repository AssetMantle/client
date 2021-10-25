package dbActors

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Classification}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object ClassificationActor {
  def props(blockchainClassification: models.blockchain.Classifications) = Props(new ClassificationActor(blockchainClassification))
}

@Singleton
class ClassificationActor @Inject()(
                            blockchainClassification: models.blockchain.Classifications
                          )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateClassification(classification) => {
      blockchainClassification.Service.create(classification) pipeTo sender()
      println(self.path)
    }
    case TryGetClassification(id) => {
      blockchainClassification.Service.tryGet(id) pipeTo sender()
      println(self.path)
    }
    case GetClassification(id) => {
      blockchainClassification.Service.get(id) pipeTo sender()
      println(self.path)
    }

    case GetAllClassification() => {
      blockchainClassification.Service.getAll pipeTo sender()
      println(self.path)
    }
    case InsertMultipleClassification(classifications) => {
      blockchainClassification.Service.insertMultiple(classifications) pipeTo sender()
      println(self.path)
    }
    case InsertOrUpdateClassification(classification) => {
      blockchainClassification.Service.insertOrUpdate(classification) pipeTo sender()
      println(self.path)
    }
    case DeleteClassification(id) => {
      blockchainClassification.Service.delete(id) pipeTo sender()
      println(self.path)
    }
    case CheckExistsClassification(id) => {
      blockchainClassification.Service.checkExists(id) pipeTo sender()
      println(self.path)
    }
  }

}

case class CreateClassification(classification: Classification)
case class TryGetClassification(id: String)
case class GetClassification(id: String)
case class GetAllClassification()
case class InsertMultipleClassification(classifications: Seq[Classification])
case class InsertOrUpdateClassification(classification: Classification)
case class DeleteClassification(id: String)
case class CheckExistsClassification(id: String)
