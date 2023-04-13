package models.archive

import exceptions.BaseException
import models.{archive, blockchain, masterTransaction}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Block(height: Int, time: Long, proposerAddress: String, validators: Seq[String])

@Singleton
class Blocks @Inject()(
                        protected val databaseConfigProvider: DatabaseConfigProvider,
                        blockchainBlocks: blockchain.Blocks,
                        blockchainTransactions: blockchain.Transactions,
                        masterTransactionWalletTransactions: masterTransaction.WalletTransactions,
                        archiveWalletTransactions: archive.WalletTransactions,
                        archiveTransactions: archive.Transactions,
                      )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_BLOCK

  import databaseConfig.profile.api._

  private[models] val blockTable = TableQuery[BlockTable]

  case class BlockSerialized(height: Int, time: Long, proposerAddress: String, validators: String) {
    def deserialize: Block = Block(height = height, time = time, proposerAddress = proposerAddress, validators = utilities.JSON.convertJsonStringToObject[Seq[String]](validators))
  }

  def serialize(block: Block): BlockSerialized = BlockSerialized(height = block.height, time = block.time, proposerAddress = block.proposerAddress, validators = Json.toJson(block.validators).toString)

  private def addMultiple(blocks: Seq[Block]): Future[Seq[Int]] = db.run((blockTable returning blockTable.map(_.height) ++= blocks.map(serialize)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.TRANSACTION_INSERT_FAILED, psqlException)
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

  private def getByList(heights: Seq[Int]): Future[Seq[BlockSerialized]] = db.run(blockTable.filter(_.height.inSet(heights)).result)

  private def getBlocksByHeightRange(heightRange: Seq[Int]): Future[Seq[BlockSerialized]] = db.run(blockTable.filter(_.height inSet heightRange).sortBy(_.time.desc).result)

  private[models] class BlockTable(tag: Tag) extends Table[BlockSerialized](tag, Option("archive"), "Block") {

    def * = (height, time, proposerAddress, validators) <> (BlockSerialized.tupled, BlockSerialized.unapply)

    def height = column[Int]("height", O.PrimaryKey)

    def time = column[Long]("time")

    def proposerAddress = column[String]("proposerAddress")

    def validators = column[String]("validators")

  }

  object Service {

    def create(blocks: Seq[Block]): Future[Seq[Int]] = addMultiple(blocks)

    def tryGet(height: Int): Future[Block] = tryGetBlockByHeight(height).map(_.deserialize)

    def get(heights: Seq[Int]): Future[Seq[Block]] = getByList(heights).map(_.map(_.deserialize))

  }

  object Utility {

    def checkAndUpdate(latestHeight: Int): Future[Unit] = {
      val firstHeight = Await.result(blockchainBlocks.Service.getFirstHeight, Duration.Inf)

      if (firstHeight > 0 && latestHeight > (firstHeight + 250000 + 10000)) {
        migrateBlocks(start = firstHeight, end = firstHeight + 10000)
      } else Future()
    }

    private def migrateBlocks(start: Int, end: Int) = {

      val moveWalletTransactions = {
        val walletTransactions = Await.result(masterTransactionWalletTransactions.Service.getByHeight(start = start, end = end), Duration.Inf)
        if (walletTransactions.nonEmpty) archiveWalletTransactions.Service.add(walletTransactions.map(x => archive.WalletTransaction(address = x.address, txHash = x.txHash, height = x.height))) else Future(Seq())
      }

      def deleteWalletTransactions() = masterTransactionWalletTransactions.Service.deleteByHeight(start = start, end = end)

      def moveTransactions() = {
        val txs = Await.result(blockchainTransactions.Service.getByHeight(start = start, end = end), Duration.Inf)
        if (txs.nonEmpty) archiveTransactions.Service.create(txs.map(x => archive.Transaction(hash = x.hash, height = x.height, code = x.code, gasWanted = x.gasWanted, gasUsed = x.gasUsed, txBytes = x.txBytes, log = x.log))) else Future(Seq())
      }

      def deleteTransactions() = blockchainTransactions.Service.deleteByHeight(start = start, end = end)

      def moveBlocks() = {
        val blocks = Await.result(blockchainBlocks.Service.getByHeight(start = start, end = end), Duration.Inf)
        if (blocks.nonEmpty) Service.create(blocks.map(x => archive.Block(height = x.height, time = x.time, proposerAddress = x.proposerAddress, validators = x.validators))) else Future(Seq())
      }

      def deleteBlocks() = blockchainBlocks.Service.deleteByHeight(start = start, end = end)

      (for {
        moved1 <- moveWalletTransactions
        _ <- if (moved1.nonEmpty) deleteWalletTransactions() else Future(0)
        moved2 <- moveTransactions()
        _ <- if (moved2.nonEmpty) deleteTransactions() else Future(0)
        moved3 <- moveBlocks()
        _ <- if (moved3.nonEmpty) deleteBlocks() else Future(0)
      } yield ()
        ).recover {
        case exception: Exception => logger.error(exception.getLocalizedMessage)
      }
    }

  }

}