package actors.blockchainModels

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import play.api.Logger

import javax.inject.{Inject, Singleton}
import constants.Actor.{NUMBER_OF_SHARDS, NUMBER_OF_ENTITIES}

object BlockActor {
  def props(blockchainBlock: models.blockchain.Blocks) = Props(new BlockActor(blockchainBlock))

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateBlock(id, _, _, _, _ ) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateBlock(id, _, _, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetBlock(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetProposerAddressBlock(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetLatestBlockHeight(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetLatestBlock(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetBlocksPerPage(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetLastNBlocks(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateBlock(id, _, _, _, _ ) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateBlock(id, _, _, _, _ ) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetBlock(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetProposerAddressBlock(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetLatestBlockHeight(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetLatestBlock(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetBlocksPerPage(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetLastNBlocks(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }
}

@Singleton
class BlockActor @Inject()(
                            blockchainBlock: models.blockchain.Blocks
                          )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case StartActor(actorRef) => {
      logger.info("Actor Started")
    }
    case CreateBlock(_, height, time, proposerAddress, validators) => {
      blockchainBlock.Service.create(height, time, proposerAddress, validators) pipeTo sender()
    }
    case InsertOrUpdateBlock(_, height, time, proposerAddress, validators) => {
      blockchainBlock.Service.insertOrUpdate(height, time, proposerAddress, validators) pipeTo sender()
    }
    case TryGetBlock(_, height) => {
      blockchainBlock.Service.tryGet(height) pipeTo sender()
    }
    case TryGetProposerAddressBlock(_, height) => {
      blockchainBlock.Service.tryGetProposerAddress(height) pipeTo sender()
    }
    case GetLatestBlockHeight(_) => {
      blockchainBlock.Service.getLatestBlockHeight pipeTo sender()
    }
    case GetLatestBlock(_) => {
      blockchainBlock.Service.getLatestBlock pipeTo sender()
    }
    case GetBlocksPerPage(_, pageNumber) => {
      blockchainBlock.Service.getBlocksPerPage(pageNumber) pipeTo sender()
    }
    case GetLastNBlocks(_, n) => {
      blockchainBlock.Service.getLastNBlocks(n) pipeTo sender()
    }
  }
}

case class CreateBlock(id: String, height: Int, time: String, proposerAddress: String, validators: Seq[String])
case class InsertOrUpdateBlock(id: String, height: Int, time: String, proposerAddress: String, validators: Seq[String])
case class TryGetBlock(id: String, height: Int)
case class TryGetProposerAddressBlock(id: String, height: Int)
case class GetLatestBlockHeight(id: String)
case class GetLatestBlock(id: String)
case class GetBlocksPerPage(id: String, pageNumber: Int)
case class GetLastNBlocks(id: String, n: Int)
