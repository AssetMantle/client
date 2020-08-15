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
import queries.responses.SplitResponse.{Response => SplitResponse}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Split(ownerID: String, ownableID: String, split: BigDecimal, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Splits @Inject()(
                        protected val databaseConfigProvider: DatabaseConfigProvider,
                        configuration: Configuration,
                        blockchainAccountBalances: AccountBalances,
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
      case psqlException: PSQLException => throw new BaseException(constants.Response.SPLIT_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(splits: Seq[Split]): Future[Seq[String]] = db.run((splitTable returning splitTable.map(_.ownerID) ++= splits).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.SPLIT_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(split: Split): Future[Int] = db.run(splitTable.insertOrUpdate(split).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.SPLIT_UPSERT_FAILED, psqlException)
    }
  }

  private def getByOwnerID(ownerID: String) = db.run(splitTable.filter(_.ownerID === ownerID).result)

  private def getByOwnableID(ownableID: String) = db.run(splitTable.filter(_.ownableID === ownableID).result)

  private def getByOwnerOrOwnableID(id: String) = db.run(splitTable.filter(x => x.ownerID === id || x.ownableID === id).result)

  private def getByOwnerAndOwnable(ownerID: String, ownableID: String) = db.run(splitTable.filter(_.ownableID === ownableID).filter(_.ownerID === ownerID).result.headOption.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.SPLIT_NOT_FOUND, noSuchElementException)
    }
  }

  private def getAllSplits = db.run(splitTable.result)

  private def deleteByIDs(ownerID: String, ownableID: String): Future[Int] = db.run(splitTable.filter(_.ownerID === ownerID).filter(_.ownableID === ownableID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.SPLIT_UPSERT_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.SPLIT_NOT_FOUND, noSuchElementException)
    }
  }

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

    def delete(ownerID: String, ownableID: String): Future[Int] = deleteByIDs(ownerID = ownerID, ownableID = ownableID)

  }

  object Utility {

    def onSend(splitSend: SplitSend): Future[Unit] = {
      val allSplitsResponse = getSplit.Service.get(ownerID = "-", ownableID = "-")

      def updateSplits(allSplitsResponse: SplitResponse) = {
        val updateFromSplit = allSplitsResponse.result.value.splits.value.list.find(x => x.value.id.value.ownerID.value.idString == splitSend.fromID && x.value.id.value.ownableID.value.idString == splitSend.ownableID).fold(
          Service.delete(ownerID = splitSend.fromID, ownableID = splitSend.ownableID)
        )(splitResponse => Service.insertOrUpdate(Split(ownerID = splitSend.fromID, ownableID = splitSend.ownableID, split = splitResponse.value.split)))

        val updateToSplit = allSplitsResponse.result.value.splits.value.list.find(x => x.value.id.value.ownerID.value.idString == splitSend.toID && x.value.id.value.ownableID.value.idString == splitSend.ownableID).fold(
          throw new BaseException(constants.Response.SPLIT_NOT_FOUND)
        )(splitResponse => Service.insertOrUpdate(Split(ownerID = splitSend.toID, ownableID = splitSend.ownableID, split = splitResponse.value.split)))

        for {
          _ <- updateFromSplit
          _ <- updateToSplit
        } yield ()
      }

      (for {
        allSplitsResponse <- allSplitsResponse
        _ <- updateSplits(allSplitsResponse)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def onWrap(splitWrap: SplitWrap): Future[Unit] = {
      val allSplitsResponse = getSplit.Service.get(ownerID = "-", ownableID = "-")
      val updateAccountBalance = blockchainAccountBalances.Utility.insertOrUpdateAccountBalance(splitWrap.from)

      def updateSplits(allSplitsResponse: SplitResponse) = Future.traverse(splitWrap.coins) { coin =>
        allSplitsResponse.result.value.splits.value.list.find(x => x.value.id.value.ownerID.value.idString == splitWrap.fromID && x.value.id.value.ownableID.value.idString == coin.denom).fold(
          throw new BaseException(constants.Response.SPLIT_NOT_FOUND)
        )(splitResponse => Service.insertOrUpdate(Split(ownerID = splitWrap.fromID, ownableID = coin.denom, split = splitResponse.value.split)))
      }

      (for {
        allSplitsResponse <- allSplitsResponse
        _ <- updateSplits(allSplitsResponse)
        _ <- updateAccountBalance
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def onUnwrap(splitUnwrap: SplitUnwrap): Future[Unit] = {
      val allSplitsResponse = getSplit.Service.get(ownerID = "-", ownableID = "-")
      val updateAccountBalance = blockchainAccountBalances.Utility.insertOrUpdateAccountBalance(splitUnwrap.from)

      def updateSplits(allSplitsResponse: SplitResponse) = allSplitsResponse.result.value.splits.value.list.find(x => x.value.id.value.ownerID.value.idString == splitUnwrap.fromID && x.value.id.value.ownableID.value.idString == splitUnwrap.ownableID).fold(
        Service.delete(ownerID = splitUnwrap.fromID, ownableID = splitUnwrap.ownableID)
      )(splitResponse => Service.insertOrUpdate(Split(ownerID = splitUnwrap.fromID, ownableID = splitUnwrap.ownableID, split = splitResponse.value.split)))

      (for {
        allSplitsResponse <- allSplitsResponse
        _ <- updateSplits(allSplitsResponse)
        _ <- updateAccountBalance
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def splitsMint(ownerID: String, ownableID: String): Future[Unit] = {
      val splitResponse = getSplit.Service.get(ownerID = ownerID, ownableID = ownableID)

      def insertOrUpdate(splitResponse: SplitResponse) = splitResponse.result.value.splits.value.list.find(x => x.value.id.value.ownerID.value.idString == ownerID && x.value.id.value.ownableID.value.idString == ownableID).fold(
        throw new BaseException(constants.Response.SPLIT_NOT_FOUND)
      )(split =>
        Service.insertOrUpdate(Split(ownerID = split.value.id.value.ownerID.value.idString, ownableID = split.value.id.value.ownableID.value.idString, split = split.value.split)))

      (for {
        splitResponse <- splitResponse
        _ <- insertOrUpdate(splitResponse)
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def splitsBurn(ownerID: String, ownableID: String): Future[Unit] = {
      val splitResponse = getSplit.Service.get(ownerID = ownerID, ownableID = ownableID)

      def updateOrDelete(splitResponse: SplitResponse) = splitResponse.result.value.splits.value.list.find(x => x.value.id.value.ownerID.value.idString == ownerID && x.value.id.value.ownableID.value.idString == ownableID).fold(
        Service.delete(ownerID = ownerID, ownableID = ownableID)
      )(split => Service.insertOrUpdate(Split(ownerID = split.value.id.value.ownerID.value.idString, ownableID = split.value.id.value.ownableID.value.idString, split = split.value.split)))

      (for {
        splitResponse <- splitResponse
        _ <- updateOrDelete(splitResponse)
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

  }

}