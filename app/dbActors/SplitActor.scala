package dbActors

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Split}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object SplitActor {
  def props(blockchainSplit: models.blockchain.Splits) = Props(new SplitActor(blockchainSplit))
}

@Singleton
class SplitActor @Inject()(
                                   blockchainSplit: models.blockchain.Splits
                                 )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateSplit(split) => {
      blockchainSplit.Service.create(split) pipeTo sender()
    }
    case InsertMultipleSplit(splits) => {
      blockchainSplit.Service.insertMultiple(splits) pipeTo sender()
    }
    case InsertOrUpdateSplit(split) => {
      blockchainSplit.Service.insertOrUpdate(split) pipeTo sender()
    }
    case GetByOwner(ownerID) => {
      blockchainSplit.Service.getByOwner(ownerID) pipeTo sender()
    }
    case GetByOwnerIDs(ownerIDs) => {
      blockchainSplit.Service.getByOwnerIDs(ownerIDs) pipeTo sender()
    }
    case GetByOwnable(ownableID) => {
      blockchainSplit.Service.getByOwnable(ownableID) pipeTo sender()
    }
    case GetByOwnerOrOwnable(id) => {
      blockchainSplit.Service.getByOwnerOrOwnable(id) pipeTo sender()
    }
    case GetAllSplit() => {
      blockchainSplit.Service.getAll pipeTo sender()
    }
    case DeleteSplit(ownerID, ownableID) => {
      blockchainSplit.Service.delete(ownerID, ownableID) pipeTo sender()
    }
    case TryGetSplit(ownerID, ownableID) => {
      blockchainSplit.Service.tryGet(ownerID, ownableID) pipeTo sender()
    }
    case GetSplit(ownerID, ownableID) => {
      blockchainSplit.Service.get(ownerID, ownableID) pipeTo sender()
    }

  }

}

case class CreateSplit(split: Split)
case class GetByOwner(ownerID: String)
case class GetByOwnerIDs(ownerIDs: Seq[String])
case class GetByOwnable(ownableID: String)
case class GetByOwnerOrOwnable(id: String)
case class GetAllSplit()
case class GetSplit(ownerID: String, ownableID: String)
case class TryGetSplit(ownerID: String, ownableID: String)
case class InsertMultipleSplit(splits: Seq[Split])
case class InsertOrUpdateSplit(split: Split)
case class DeleteSplit(ownerID: String, ownableID: String)

