package models.blockchain

import akka.pattern.{ask, pipe}
import akka.util.Timeout
import models.blockchain.Blocks.{CreateBlock, GetBlocksPerPage, GetLastNBlocks, GetLatestBlock, GetLatestBlockHeight, InsertOrUpdateBlock, TryGetBlock, TryGetProposerAddressBlock}
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.{ ShardRegion}

import java.sql.Timestamp
import exceptions.BaseException

import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import java.time.Duration
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import constants.Actor.{NUMBER_OF_ENTITIES, NUMBER_OF_SHARDS}
import models.Abstract.ShardedActorRegion


case class Block(height: Int, time: String, proposerAddress: String, validators: Seq[String], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Blocks @Inject()(
                        protected val databaseConfigProvider: DatabaseConfigProvider,
                        configuration: Configuration
                      )(implicit executionContext: ExecutionContext) extends ShardedActorRegion {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_BLOCK

  private val blocksPerPage = configuration.get[Int]("blockchain.blocks.perPage")

  private val numBlocksAvgBlockTimes = configuration.get[Int]("blockchain.avgBlockTimes")

  private val blockchainStartHeight = configuration.get[Int]("blockchain.startHeight")

  private val uniqueId: String = UUID.randomUUID().toString

  import databaseConfig.profile.api._

  private[models] val blockTable = TableQuery[BlockTable]

  private implicit val timeout = Timeout(constants.Actor.ACTOR_ASK_TIMEOUT)

  override def idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateBlock(id, _, _, _, _ ) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateBlock(id, _, _, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetBlock(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetProposerAddressBlock(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetLatestBlockHeight(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetLatestBlock(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetBlocksPerPage(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetLastNBlocks(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  override def shardResolver: ShardRegion.ExtractShardId = {
    case CreateBlock(id, _, _, _, _ ) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateBlock(id, _, _, _, _ ) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetBlock(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetProposerAddressBlock(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetLatestBlockHeight(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetLatestBlock(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetBlocksPerPage(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetLastNBlocks(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }

  override def regionName: String = "blockRegion"

  override def props: Props = Blocks.props(Blocks.this)


  case class BlockSerialized(height: Int, time: String, proposerAddress: String, validators: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Block = Block(height = height, time = time, proposerAddress = proposerAddress, validators = utilities.JSON.convertJsonStringToObject[Seq[String]](validators), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(block: Block): BlockSerialized = BlockSerialized(height = block.height, time = block.time, proposerAddress = block.proposerAddress, validators = Json.toJson(block.validators).toString, createdBy = block.createdBy, createdOn = block.createdOn, createdOnTimeZone = block.createdOnTimeZone, updatedBy = block.updatedBy, updatedOn = block.updatedOn, updatedOnTimeZone = block.updatedOnTimeZone)

  private def add(block: Block): Future[String] = db.run((blockTable returning blockTable.map(_.height) += serialize(block)).asTry).map {
    case Success(result) => result.toString
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.BLOCK_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(block: Block): Future[Int] = db.run(blockTable.insertOrUpdate(serialize(block)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.BLOCK_UPSERT_FAILED, psqlException)
    }
  }

  private def tryGetLatestBlockHeight: Future[Int] = db.run(blockTable.map(_.height).max.result.asTry).map {
    case Success(result) => result.getOrElse(0)
    case Failure(exception) => exception match {
      case _: NoSuchElementException => 0
    }
  }

  private def tryGetLatestBlock: Future[BlockSerialized] = db.run(blockTable.sortBy(_.height.desc).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.BLOCK_NOT_FOUND, noSuchElementException)
    }
  }

  private def tryGetBlockByHeight(height: Int): Future[BlockSerialized] = db.run(blockTable.filter(_.height === height).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.BLOCK_NOT_FOUND, noSuchElementException)
    }
  }

  private def tryGetProposerAddressByHeight(height: Int): Future[String] = db.run(blockTable.filter(_.height === height).map(_.proposerAddress).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.BLOCK_NOT_FOUND, noSuchElementException)
    }
  }

  private def getBlocksByHeightRange(heightRange: Seq[Int]): Future[Seq[BlockSerialized]] = db.run(blockTable.filter(_.height inSet heightRange).sortBy(_.time.desc).result)

  private[models] class BlockTable(tag: Tag) extends Table[BlockSerialized](tag, "Block") {

    def * = (height, time, proposerAddress, validators, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (BlockSerialized.tupled, BlockSerialized.unapply)

    def height = column[Int]("height", O.PrimaryKey)

    def time = column[String]("time")

    def proposerAddress = column[String]("proposerAddress")

    def validators = column[String]("validators")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def createBlockWithActor(height: Int, time: String, proposerAddress: String, validators: Seq[String]): Future[String] = (actorRegion ? CreateBlock(uniqueId, height, time, proposerAddress, validators)).mapTo[String]

    def create(height: Int, time: String, proposerAddress: String, validators: Seq[String]): Future[String] = add(Block(height = height, time = time, proposerAddress = proposerAddress, validators = validators))

    def insertOrUpdateBlockWithActor(height: Int, time: String, proposerAddress: String, validators: Seq[String]): Future[Int] = (actorRegion ? InsertOrUpdateBlock(uniqueId, height, time, proposerAddress, validators)).mapTo[Int]

    def insertOrUpdate(height: Int, time: String, proposerAddress: String, validators: Seq[String]): Future[Int] = upsert(Block(height = height, time = time, proposerAddress = proposerAddress, validators = validators))

    def tryGetBlockWithActor(height: Int): Future[Block] = (actorRegion ? TryGetBlock(uniqueId, height)).mapTo[Block]

    def tryGet(height: Int): Future[Block] = tryGetBlockByHeight(height).map(_.deserialize)

    def tryGetProposerAddressBlockWithActor(height: Int): Future[String] = (actorRegion ? TryGetProposerAddressBlock(uniqueId, height)).mapTo[String]

    def tryGetProposerAddress(height: Int): Future[String] = tryGetProposerAddressByHeight(height)

    def getLatestBlockHeightWithActor: Future[Int] = (actorRegion ? GetLatestBlockHeight(uniqueId)).mapTo[Int]

    def getLatestBlockHeight: Future[Int] = tryGetLatestBlockHeight

    def getLatestBlockWithActor: Future[Block] = (actorRegion ? GetLatestBlock(uniqueId)).mapTo[Block]

    def getLatestBlock: Future[Block] = {
      val latestBlockHeight = tryGetLatestBlockHeight
      for {
        latestBlockHeight <- latestBlockHeight
        block <- tryGetBlockByHeight(latestBlockHeight).map(_.deserialize)
      } yield block
    }

    def getBlocksPerPageWithActor(pageNumber: Int): Future[Seq[Block]] = (actorRegion ? GetBlocksPerPage(uniqueId, pageNumber)).mapTo[Seq[Block]]

    def getBlocksPerPage(pageNumber: Int): Future[Seq[Block]] = {
      val latestBlockHeight = tryGetLatestBlockHeight
      for {
        latestBlockHeight <- latestBlockHeight
        blockList <- getBlocksByHeightRange(latestBlockHeight - pageNumber * blocksPerPage + 1 to latestBlockHeight - (pageNumber - 1) * blocksPerPage).map(_.map(_.deserialize))
      } yield blockList
    }

    def getLastNBlocksWithActor(n: Int): Future[Seq[Block]] = (actorRegion ? GetLastNBlocks(uniqueId, n)).mapTo[Seq[Block]]

    def getLastNBlocks(n: Int): Future[Seq[Block]] = {
      val latestBlockHeight = tryGetLatestBlockHeight

      for {
        latestBlockHeight <- latestBlockHeight
        blockList <- getBlocksByHeightRange(latestBlockHeight - n + 1 to latestBlockHeight).map(_.map(_.deserialize))
      } yield blockList
    }
  }

  object Utility {

    def getAverageBlockTime(fromBlock: Option[Int] = None, numBlocks: Int = numBlocksAvgBlockTimes): Future[Double] = {
      val lastBlock = fromBlock.fold(Service.getLatestBlock)(height => Service.tryGet(height))

      // Should not use block height 1 since time difference between block 1 and block 2 can be very high
      def getFirstBlock(lastBlock: Block) = if (lastBlock.height == blockchainStartHeight) Future(lastBlock) else if (numBlocks >= (lastBlock.height - blockchainStartHeight)) Service.tryGet(blockchainStartHeight + 1) else Service.tryGet(lastBlock.height - numBlocks)

      (for {
        lastBlock <- lastBlock
        firstBlock <- getFirstBlock(lastBlock)
      } yield utilities.NumericOperation.roundOff(Duration.between(utilities.Date.bcTimestampToZonedDateTime(lastBlock.time), utilities.Date.bcTimestampToZonedDateTime(firstBlock.time)).abs().toSeconds.toDouble / (lastBlock.height - firstBlock.height))
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

  }

}

object Blocks {
  def props(blockchainBlocks: models.blockchain.Blocks) (implicit executionContext: ExecutionContext) = Props(new BlockActor(blockchainBlocks))

  @Singleton
  class BlockActor @Inject()(
                              blockchainBlocks: models.blockchain.Blocks
                            ) (implicit executionContext: ExecutionContext) extends Actor with ActorLogging {
    private implicit val logger: Logger = Logger(this.getClass)

    override def receive: Receive = {
      case CreateBlock(_, height, time, proposerAddress, validators) => {
        blockchainBlocks.Service.create(height, time, proposerAddress, validators) pipeTo sender()
      }
      case InsertOrUpdateBlock(_, height, time, proposerAddress, validators) => {
        blockchainBlocks.Service.insertOrUpdate(height, time, proposerAddress, validators) pipeTo sender()
      }
      case TryGetBlock(_, height) => {
        blockchainBlocks.Service.tryGet(height) pipeTo sender()
      }
      case TryGetProposerAddressBlock(_, height) => {
        blockchainBlocks.Service.tryGetProposerAddress(height) pipeTo sender()
      }
      case GetLatestBlockHeight(_) => {
        blockchainBlocks.Service.getLatestBlockHeight pipeTo sender()
      }
      case GetLatestBlock(_) => {
        blockchainBlocks.Service.getLatestBlock pipeTo sender()
      }
      case GetBlocksPerPage(_, pageNumber) => {
        blockchainBlocks.Service.getBlocksPerPage(pageNumber) pipeTo sender()
      }
      case GetLastNBlocks(_, n) => {
        blockchainBlocks.Service.getLastNBlocks(n) pipeTo sender()
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
}
