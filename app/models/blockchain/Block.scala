package models.blockchain

import java.sql.Timestamp

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

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

  private def getTotalRows: Future[Int] = db.run(blockTable.length.result)

  private def tryGetLatestBlockHeight: Future[Int] = db.run(blockTable.map(_.height).sortBy(_.desc).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => 0
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

  private def tryGetHeights(range: Seq[Int]): Future[Seq[Int]] = db.run(blockTable.map(_.height).filter(_.inSet(range)).result)

  private def getBlocksForPageNumber(offset: Int, limit: Int): Future[Seq[BlockSerialized]] = db.run(blockTable.sortBy(_.time.desc).drop(offset).take(limit).result)

  private def getBlocksByLastN(n: Int): Future[Seq[BlockSerialized]] = db.run(blockTable.sortBy(_.height.desc).take(n).result)

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

    def create(height: Int, time: String, proposerAddress: String, validators: Seq[String]): Future[String] = add(Block(height = height, time = time, proposerAddress = proposerAddress, validators = validators))

    def insertOrUpdate(height: Int, time: String, proposerAddress: String, validators: Seq[String]): Future[Int] = upsert(Block(height = height, time = time, proposerAddress = proposerAddress, validators = validators))

    def tryGet(height: Int): Future[Block] = tryGetBlockByHeight(height.toInt).map(_.deserialize)

    def tryGetProposerAddress(height: Int): Future[String] = tryGetProposerAddressByHeight(height.toInt)

    def getLatestBlockHeight: Future[Int] = tryGetLatestBlockHeight

    def getLatestBlock: Future[Block] = tryGetLatestBlock.map(_.deserialize)

    def getMissingBlocks(start: Int, end: Int): Future[Seq[Int]] = {
      val range = Seq.range(start, end + 1)
      (for {
        presentHeights <- tryGetHeights(range)
      } yield range filterNot (presentHeights contains)
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def getBlocksPerPage(pageNumber: Int): Future[Seq[Block]] = getBlocksForPageNumber(offset = (pageNumber - 1) * blocksPerPage, limit = blocksPerPage).map(_.map(_.deserialize))

    def getLastNBlocks(n: Int): Future[Seq[Block]] = getBlocksByLastN(n).map(_.map(_.deserialize))
  }

}