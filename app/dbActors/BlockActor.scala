package dbActors

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Block, Balance}
import models.common.Serializable.Coin
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object BlockActor {
  def props(blockchainBlock: models.blockchain.Blocks) = Props(new BlockActor(blockchainBlock))
}

@Singleton
class BlockActor @Inject()(
                            blockchainBlock: models.blockchain.Blocks
                          )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateBlock(height, time, proposerAddress, validators) => {
      blockchainBlock.Service.create(height, time, proposerAddress, validators) pipeTo sender()
    }
    case InsertOrUpdateBlock(height, time, proposerAddress, validators) => {
      blockchainBlock.Service.insertOrUpdate(height, time, proposerAddress, validators) pipeTo sender()
    }
    case TryGetBlock(height) => {
      blockchainBlock.Service.tryGet(height) pipeTo sender()
    }

    case TryGetProposerAddressBlock(height) => {
      blockchainBlock.Service.tryGetProposerAddress(height) pipeTo sender()
    }
    case GetLatestBlockHeight() => {
      blockchainBlock.Service.getLatestBlockHeight pipeTo sender()
    }
    case GetLatestBlock() => {
      blockchainBlock.Service.getLatestBlock pipeTo sender()
    }
    case GetBlocksPerPage(pageNumber) => {
      blockchainBlock.Service.getBlocksPerPage(pageNumber) pipeTo sender()
    }
    case GetLastNBlocks(n) => {
      blockchainBlock.Service.getLastNBlocks(n) pipeTo sender()
    }
  }

}

case class CreateBlock(height: Int, time: String, proposerAddress: String, validators: Seq[String])
case class InsertOrUpdateBlock(height: Int, time: String, proposerAddress: String, validators: Seq[String])
case class TryGetBlock(height: Int)
case class TryGetProposerAddressBlock(height: Int)
case class GetLatestBlockHeight()
case class GetLatestBlock()
case class GetBlocksPerPage(pageNumber: Int)
case class GetLastNBlocks(n: Int)
