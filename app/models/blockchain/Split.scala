package models.blockchain

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.TransactionMessages.{SplitSend, SplitUnwrap, SplitWrap}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.GetSplit
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Split(ownerID: String, ownableID: String, split: BigDecimal, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Splits @Inject()(
                        protected val databaseConfigProvider: DatabaseConfigProvider,
                        configuration: Configuration,
                        getSplit: GetSplit,
                      )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_SPLIT

  import databaseConfig.profile.api._

  private[models] val splitTable = TableQuery[SplitTable]

  private def add(split: Split): Future[String] = db.run((splitTable returning splitTable.map(_.ownerID) += split).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ASSET_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(splits: Seq[Split]): Future[Seq[String]] = db.run((splitTable returning splitTable.map(_.ownerID) ++= splits).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ASSET_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(split: Split): Future[Int] = db.run(splitTable.insertOrUpdate(split).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ASSET_UPSERT_FAILED, psqlException)
    }
  }

  private def getByOwnerID(ownerID: String) = db.run(splitTable.filter(_.ownerID === ownerID).result)

  private def getByOwnableID(ownableID: String) = db.run(splitTable.filter(_.ownableID === ownableID).result)

  private def getByOwnerOrOwnableID(id: String) = db.run(splitTable.filter(x => x.ownerID === id || x.ownableID === id).result)

  private def getByOwnerAndOwnable(ownerID: String, ownableID: String) = db.run(splitTable.filter(_.ownableID === ownableID).filter(_.ownerID === ownerID).result.headOption.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ASSET_NOT_FOUND, noSuchElementException)
    }
  }

  private def getAllSplits = db.run(splitTable.result)

  private[models] class SplitTable(tag: Tag) extends Table[Split](tag, "Split_BC") {

    def * = (ownerID, ownableID, split, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Split.tupled, Split.unapply)

    def ownerID = column[String]("ownerID", O.PrimaryKey)

    def ownableID = column[String]("ownableID", O.PrimaryKey)

    def split = column[BigDecimal]("split")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(split: Split): Future[String] = add(split)

    def getByOwner(ownerID: String): Future[Seq[Split]] = getByOwnerID(ownerID)

    def getByOwnable(ownableID: String): Future[Seq[Split]] = getByOwnableID(ownableID)

    def getByOwnerOrOwnable(id: String): Future[Seq[Split]] = getByOwnerOrOwnableID(id)

    def get(ownerID: String, ownableID: String): Future[Option[Split]] = getByOwnerAndOwnable(ownerID = ownerID, ownableID = ownableID)

    def getAll: Future[Seq[Split]] = getAllSplits

    def insertMultiple(splits: Seq[Split]): Future[Seq[String]] = addMultiple(splits)

    def insertOrUpdate(split: Split): Future[Int] = upsert(split)

  }

  object Utility {
    def onSend(splitSend: SplitSend): Future[Unit] = {
      val split = Service.get(ownerID = splitSend.toID, ownableID = splitSend.ownableID)

      def insert(split: Option[Split]) = split.fold(Service.insertOrUpdate(Split(ownerID = splitSend.toID, ownableID = splitSend.ownableID, split = splitSend.split)))(oldSplit => Service.insertOrUpdate(Split(ownerID = splitSend.toID, ownableID = splitSend.ownableID, split = (oldSplit.split + splitSend.split))))

      (for {
        split <- split
        _ <- insert(split)
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def onWrap(splitWrap: SplitWrap): Future[Unit] = {
      Future()
    }

    def onUnwrap(splitUnwrap: SplitUnwrap): Future[Unit] = {
      Future()
    }
  }

}