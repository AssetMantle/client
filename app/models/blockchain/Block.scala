package models.blockchain

import exceptions.BaseException
import models.archive
import models.traits.{Entity, GenericDaoImpl, ModelTable}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import slick.jdbc.H2Profile.api._
import utilities.Date.RFC3339

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class Block(height: Int, time: Long, proposerAddress: String, validators: Seq[String]) {
  def serialize(): Blocks.BlockSerialized = Blocks.BlockSerialized(
    height = this.height,
    time = this.time,
    proposerAddress = this.proposerAddress,
    validators = Json.toJson(this.validators).toString)

}

private[blockchain] object Blocks {
  case class BlockSerialized(height: Int, time: Long, proposerAddress: String, validators: String) extends Entity[Int] {
    def id: Int = height

    def deserialize()(implicit module: String, logger: Logger): Block = Block(
      height = height,
      time = time,
      proposerAddress = proposerAddress,
      validators = utilities.JSON.convertJsonStringToObject[Seq[String]](validators))
  }

  class BlockTable(tag: Tag) extends Table[BlockSerialized](tag, Option("blockchain"), "Block") with ModelTable[Int] {

    def * = (height, time, proposerAddress, validators) <> (BlockSerialized.tupled, BlockSerialized.unapply)

    def height = column[Int]("height", O.PrimaryKey)

    def time = column[Long]("time")

    def proposerAddress = column[String]("proposerAddress")

    def validators = column[String]("validators")

    def id = height
  }

}

@Singleton
class Blocks @Inject()(
                        protected val dbConfigProvider: DatabaseConfigProvider,
                        configuration: Configuration,
                        archiveBlocks: archive.Blocks,
                      )(implicit executionContext: ExecutionContext)
  extends GenericDaoImpl[Blocks.BlockTable, Blocks.BlockSerialized, Int]() {

  implicit val logger: Logger = Logger(this.getClass)

  implicit val module: String = constants.Module.BLOCKCHAIN_BLOCK

  val tableQuery = new TableQuery(tag => new Blocks.BlockTable(tag))

  private val blocksPerPage = configuration.get[Int]("blockchain.blocks.perPage")

  private val numBlocksAvgBlockTimes = configuration.get[Int]("blockchain.avgBlockTimes")

  private val blockchainStartHeight = configuration.get[Int]("blockchain.startHeight")

  object Service {

    def add(height: Int, time: RFC3339, proposerAddress: String, validators: Seq[String]): Future[Int] = create(Block(height = height, time = time.epoch, proposerAddress = proposerAddress, validators = validators).serialize()).map(_.height)

    def insertOrUpdate(height: Int, time: RFC3339, proposerAddress: String, validators: Seq[String]): Future[Int] = upsert(Block(height = height, time = time.epoch, proposerAddress = proposerAddress, validators = validators).serialize())

    def get(height: Int): Future[Option[Block]] = {
      val block = filter(_.height === height).map(_.headOption.map(_.deserialize))

      def getFromArchive(defined: Boolean) = if (!defined) archiveBlocks.Service.get(height) else Future(None)

      for {
        block <- block
        archiveBlock <- getFromArchive(block.isDefined)
      } yield if (block.isDefined) block else if (archiveBlock.isDefined) archiveBlock.map(_.toBlock) else None
    }

    def tryGet(height: Int): Future[Block] = get(height).map(_.getOrElse(constants.Response.BLOCK_NOT_FOUND.throwBaseException()))

    def get(heights: Seq[Int]): Future[Seq[Block]] = filter(_.height.inSet(heights)).map(_.map(_.deserialize))

    def getLatestBlockHeight: Future[Int] = customQuery(tableQuery.map(_.height).max.result).map(_.getOrElse(0))

    def tryGetLatestBlockHeight: Future[Int] = customQuery(tableQuery.map(_.height).max.result).map(_.getOrElse(constants.Response.BLOCK_NOT_FOUND.throwBaseException()))

    def getFirstHeight: Future[Int] = customQuery(tableQuery.map(_.height).min.result).map(_.getOrElse(0))

    def tryGetLatestBlock: Future[Block] = customQuery(tableQuery.sorted(_.height.desc).result.headOption).map(_.fold(constants.Response.BLOCK_NOT_FOUND.throwBaseException())(_.deserialize))

    def getBlocksPerPage(pageNumber: Int): Future[Seq[Block]] = customQuery(tableQuery.sortBy(_.height.desc).drop((pageNumber - 1) * blocksPerPage).take(blocksPerPage).result).map(_.map(_.deserialize))

    def getLastNBlocks(n: Int): Future[Seq[Block]] = customQuery(tableQuery.sortBy(_.height.desc).take(n).result).map(_.map(_.deserialize))

    def getByHeight(start: Int, end: Int): Future[Seq[Block]] = filter(x => x.height >= start && x.height <= end).map(_.map(_.deserialize))

    def deleteByHeight(start: Int, end: Int): Future[Int] = filterAndDelete(x => x.height >= start && x.height <= end)
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
        case _: BaseException => 0.0
      }
    }

  }
}