package models.archive

import exceptions.BaseException
import models.blockchain
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Block(height: Int, time: Long, proposerAddress: String, validators: Seq[String]) {
  def toBlock: blockchain.Block = blockchain.Block(height = this.height, time = this.time, proposerAddress = this.proposerAddress, validators = this.validators)
}

@Singleton
class Blocks @Inject()(
                        protected val databaseConfigProvider: DatabaseConfigProvider,
                      )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ARCHIVE_BLOCK

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

  private def tryGetLatestHeight: Future[Int] = db.run(blockTable.map(_.height).sorted.max.result.map(_.getOrElse(0)))

  private def getByList(heights: Seq[Int]): Future[Seq[BlockSerialized]] = db.run(blockTable.filter(_.height.inSet(heights)).result)

  private def getByHeight(height: Int): Future[Option[BlockSerialized]] = db.run(blockTable.filter(_.height === height).result.headOption)

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

    def get(height: Int): Future[Option[Block]] = getByHeight(height).map(_.map(_.deserialize))

    def getLatestHeight: Future[Int] = tryGetLatestHeight

  }

}