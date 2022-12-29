package models.blockchain

import exceptions.BaseException
import models.Trait.Logging
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import utilities.Date.RFC3339

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Block(height: Int, time: RFC3339, proposerAddress: String, validators: Seq[String], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging

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

  private val blockchainStartHeight = configuration.get[Int]("blockchain.startHeight")

  import databaseConfig.profile.api._

  private[models] val blockTable = TableQuery[BlockTable]

  case class BlockSerialized(height: Int, time: Long, proposerAddress: String, validators: String, createdBy: Option[String], createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) {
    def deserialize: Block = Block(height = height, time = RFC3339(time), proposerAddress = proposerAddress, validators = utilities.JSON.convertJsonStringToObject[Seq[String]](validators), createdBy = createdBy, createdOnMillisEpoch = createdOnMillisEpoch, updatedBy = updatedBy, updatedOnMillisEpoch = updatedOnMillisEpoch)
  }

  def serialize(block: Block): BlockSerialized = BlockSerialized(height = block.height, time = block.time.epoch, proposerAddress = block.proposerAddress, validators = Json.toJson(block.validators).toString, createdBy = block.createdBy, createdOnMillisEpoch = block.createdOnMillisEpoch, updatedBy = block.updatedBy, updatedOnMillisEpoch = block.updatedOnMillisEpoch)

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

    def * = (height, time, proposerAddress, validators, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (BlockSerialized.tupled, BlockSerialized.unapply)

    def height = column[Int]("height", O.PrimaryKey)

    def time = column[Long]("time")

    def proposerAddress = column[String]("proposerAddress")

    def validators = column[String]("validators")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")
  }

  object Service {

    def create(height: Int, time: RFC3339, proposerAddress: String, validators: Seq[String]): Future[String] = add(Block(height = height, time = time, proposerAddress = proposerAddress, validators = validators))

    def insertOrUpdate(height: Int, time: RFC3339, proposerAddress: String, validators: Seq[String]): Future[Int] = upsert(Block(height = height, time = time, proposerAddress = proposerAddress, validators = validators))

    def tryGet(height: Int): Future[Block] = tryGetBlockByHeight(height).map(_.deserialize)

    def tryGetProposerAddress(height: Int): Future[String] = tryGetProposerAddressByHeight(height)

    def getLatestBlockHeight: Future[Int] = tryGetLatestBlockHeight

    def getLatestBlock: Future[Block] = {
      val latestBlockHeight = tryGetLatestBlockHeight
      for {
        latestBlockHeight <- latestBlockHeight
        block <- tryGetBlockByHeight(latestBlockHeight).map(_.deserialize)
      } yield block
    }

    def getBlocksPerPage(pageNumber: Int): Future[Seq[Block]] = {
      val latestBlockHeight = tryGetLatestBlockHeight
      for {
        latestBlockHeight <- latestBlockHeight
        blockList <- getBlocksByHeightRange(latestBlockHeight - pageNumber * blocksPerPage + 1 to latestBlockHeight - (pageNumber - 1) * blocksPerPage).map(_.map(_.deserialize))
      } yield blockList
    }

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
      } yield utilities.NumericOperation.roundOff(lastBlock.time.difference(firstBlock.time).abs().toSeconds.toDouble / (lastBlock.height - firstBlock.height))
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

  }

}