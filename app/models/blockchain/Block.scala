package models.blockchain

import exceptions.BaseException
import models.archive
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import utilities.Date.RFC3339

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Block(height: Int, time: Long, proposerAddress: String, validators: Seq[String])

@Singleton
class Blocks @Inject()(
                        protected val databaseConfigProvider: DatabaseConfigProvider,
                        configuration: Configuration,
                        archiveBlocks: archive.Blocks,
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

  case class BlockSerialized(height: Int, time: Long, proposerAddress: String, validators: String) {
    def deserialize: Block = Block(height = height, time = time, proposerAddress = proposerAddress, validators = utilities.JSON.convertJsonStringToObject[Seq[String]](validators))
  }

  def serialize(block: Block): BlockSerialized = BlockSerialized(height = block.height, time = block.time, proposerAddress = block.proposerAddress, validators = Json.toJson(block.validators).toString)

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

  private def tryGetFirstHeight: Future[Int] = db.run(blockTable.map(_.height).min.result.asTry).map {
    case Success(result) => result.getOrElse(0)
    case Failure(exception) => exception match {
      case _: NoSuchElementException => 0
    }
  }

  private def getByHeightRange(start: Int, end: Int): Future[Seq[BlockSerialized]] = db.run(blockTable.filter(x => x.height >= start && x.height <= end).result)

  private def deleteByHeightRange(start: Int, end: Int): Future[Int] = db.run(blockTable.filter(x => x.height >= start && x.height <= end).delete)

  private def tryGetBlockByHeight(height: Int): Future[BlockSerialized] = db.run(blockTable.filter(_.height === height).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.BLOCK_NOT_FOUND, noSuchElementException)
    }
  }

  private def getBlockByHeight(height: Int): Future[Option[BlockSerialized]] = db.run(blockTable.filter(_.height === height).result.headOption)

  private def getByList(heights: Seq[Int]): Future[Seq[BlockSerialized]] = db.run(blockTable.filter(_.height.inSet(heights)).result)

  private def getBlocksByHeightRange(heightRange: Seq[Int]): Future[Seq[BlockSerialized]] = db.run(blockTable.filter(_.height inSet heightRange).sortBy(_.time.desc).result)

  private[models] class BlockTable(tag: Tag) extends Table[BlockSerialized](tag, Option("blockchain"), "Block") {

    def * = (height, time, proposerAddress, validators) <> (BlockSerialized.tupled, BlockSerialized.unapply)

    def height = column[Int]("height", O.PrimaryKey)

    def time = column[Long]("time")

    def proposerAddress = column[String]("proposerAddress")

    def validators = column[String]("validators")
  }

  object Service {

    def create(height: Int, time: RFC3339, proposerAddress: String, validators: Seq[String]): Future[String] = add(Block(height = height, time = time.epoch, proposerAddress = proposerAddress, validators = validators))

    def insertOrUpdate(height: Int, time: RFC3339, proposerAddress: String, validators: Seq[String]): Future[Int] = upsert(Block(height = height, time = time.epoch, proposerAddress = proposerAddress, validators = validators))

    def tryGet(height: Int): Future[Block] = get(height).map(_.getOrElse(constants.Response.BLOCK_NOT_FOUND.throwBaseException()))

    def get(height: Int): Future[Option[Block]] = {
      val block = getBlockByHeight(height).map(_.map(_.deserialize))

      def getFromArchive(defined: Boolean) = if (!defined) archiveBlocks.Service.get(height) else Future(None)

      for {
        block <- block
        archiveBlock <- getFromArchive(block.isDefined)
      } yield if (block.isDefined) block else if (archiveBlock.isDefined) archiveBlock.map(_.toBlock) else None
    }

    def get(heights: Seq[Int]): Future[Seq[Block]] = getByList(heights).map(_.map(_.deserialize))

    def getLatestBlockHeight: Future[Int] = tryGetLatestBlockHeight

    def getFirstHeight: Future[Int] = tryGetFirstHeight

    def tryGetLatestBlock: Future[Block] = {
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

    def getByHeight(start: Int, end: Int): Future[Seq[Block]] = getByHeightRange(start = start, end = end).map(_.map(_.deserialize))

    def deleteByHeight(start: Int, end: Int): Future[Int] = deleteByHeightRange(start = start, end = end)

  }

  object Utility {

    def getAverageBlockTime(fromBlock: Option[Int] = None, numBlocks: Int = numBlocksAvgBlockTimes): Future[Double] = {
      val lastBlock = fromBlock.fold(Service.tryGetLatestBlock)(height => Service.tryGet(height))

      // Should not use block height 1 since time difference between block 1 and block 2 can be very high
      def getFirstBlock(lastBlock: Block) = if (lastBlock.height == blockchainStartHeight) Future(lastBlock) else if (numBlocks >= (lastBlock.height - blockchainStartHeight)) Service.tryGet(blockchainStartHeight + 1) else Service.tryGet(lastBlock.height - numBlocks)

      (for {
        lastBlock <- lastBlock
        firstBlock <- getFirstBlock(lastBlock)
      } yield utilities.NumericOperation.roundOff((lastBlock.time - firstBlock.time).abs.toDouble / (lastBlock.height - firstBlock.height))
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

  }

}