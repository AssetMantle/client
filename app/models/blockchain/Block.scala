package models.blockchain

import akka.pattern.ask
import akka.util.Timeout
import dbActors.{AccountActor, AddActor, BlockActor}
import dbActors.Service.{masterActor, routerActor}

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
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Block(height: Int, time: String, proposerAddress: String, validators: Seq[String], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Blocks @Inject()(
                        protected val databaseConfigProvider: DatabaseConfigProvider,
                        configuration: Configuration
                      )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_BLOCK

  private val blocksPerPage = configuration.get[Int]("blockchain.blocks.perPage")

  private val numBlocksAvgBlockTimes = configuration.get[Int]("blockchain.avgBlockTimes")

  import databaseConfig.profile.api._

  private[models] val blockTable = TableQuery[BlockTable]

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

    implicit val timeout = Timeout(5 seconds) // needed for `?` below

    private val blockActor = dbActors.Service.actorSystem.actorOf(BlockActor.props(Blocks.this), "blockActor")

    routerActor ! AddActor(None, blockActor.actorRef)

    def createBlockWithActor(height: Int, time: String, proposerAddress: String, validators: Seq[String]): Future[String] = (masterActor ? dbActors.CreateBlock(height, time, proposerAddress, validators)).mapTo[String]

    def create(height: Int, time: String, proposerAddress: String, validators: Seq[String]): Future[String] = add(Block(height = height, time = time, proposerAddress = proposerAddress, validators = validators))

    def insertOrUpdateBlockWithActor(height: Int, time: String, proposerAddress: String, validators: Seq[String]): Future[Int] = (masterActor ? dbActors.InsertOrUpdateBlock(height, time, proposerAddress, validators)).mapTo[Int]

    def insertOrUpdate(height: Int, time: String, proposerAddress: String, validators: Seq[String]): Future[Int] = upsert(Block(height = height, time = time, proposerAddress = proposerAddress, validators = validators))

    def tryGetBlockWithActor(height: Int): Future[Block] = (masterActor ? dbActors.TryGetBlock(height)).mapTo[Block]

    def tryGet(height: Int): Future[Block] = tryGetBlockByHeight(height).map(_.deserialize)

    def tryGetProposerAddressBlockWithActor(height: Int): Future[String] = (masterActor ? dbActors.TryGetProposerAddressBlock(height)).mapTo[String]

    def tryGetProposerAddress(height: Int): Future[String] = tryGetProposerAddressByHeight(height)

    def getLatestBlockHeightWithActor: Future[Int] = (masterActor ? dbActors.GetLatestBlockHeight()).mapTo[Int]

    def getLatestBlockHeight: Future[Int] = tryGetLatestBlockHeight

    def getLatestBlockWithActor: Future[Block] = (masterActor ? dbActors.GetLatestBlock()).mapTo[Block]

    def getLatestBlock: Future[Block] = {
      val latestBlockHeight = tryGetLatestBlockHeight
      for {
        latestBlockHeight <- latestBlockHeight
        block <- tryGetBlockByHeight(latestBlockHeight).map(_.deserialize)
      } yield block
    }

    def getBlocksPerPageWithActor(pageNumber: Int): Future[Seq[Block]] = (masterActor ? dbActors.GetBlocksPerPage(pageNumber)).mapTo[Seq[Block]]

    def getBlocksPerPage(pageNumber: Int): Future[Seq[Block]] = {
      val latestBlockHeight = tryGetLatestBlockHeight
      for {
        latestBlockHeight <- latestBlockHeight
        blockList <- getBlocksByHeightRange(latestBlockHeight - pageNumber * blocksPerPage + 1 to latestBlockHeight - (pageNumber - 1) * blocksPerPage).map(_.map(_.deserialize))
      } yield blockList
    }

    def getLastNBlocksWithActor(n: Int): Future[Seq[Block]] = (masterActor ? dbActors.GetLastNBlocks(n)).mapTo[Seq[Block]]

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
      def getFirstBlock(lastBlock: Block) = if (lastBlock.height == 1) Future(lastBlock) else if (lastBlock.height <= numBlocks) Service.tryGet(2) else Service.tryGet(lastBlock.height - numBlocks)

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