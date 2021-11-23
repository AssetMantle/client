package models.blockchain

import akka.pattern.ask
import akka.util.Timeout
import actors.models.blockchain
import actors.models.blockchain.{CreateSplit, DeleteSplit, GetAllSplit, GetByOwnable, GetByOwner, GetByOwnerIDs, GetByOwnerOrOwnable, GetSplit, InsertMultipleSplit, InsertOrUpdateSplit, SplitActor, TryGetSplit}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import exceptions.BaseException
import models.Trait.Logged
import models.common.TransactionMessages.{SplitSend, SplitUnwrap, SplitWrap}
import models.master
import models.master.{Split => masterSplit}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.responses.common.Header
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Split(ownerID: String, ownableID: String, split: BigDecimal, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Splits @Inject()(
                        protected val databaseConfigProvider: DatabaseConfigProvider,
                        configuration: Configuration,
                        blockchainBalances: Balances,
                        masterSplits: master.Splits
                      )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_SPLIT

  import databaseConfig.profile.api._

  private[models] val splitTable = TableQuery[SplitTable]

  private val uniqueId: String = UUID.randomUUID().toString

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

  private def getAllByOwnerIDs(ownerIDs: Seq[String]) = db.run(splitTable.filter(_.ownerID.inSet(ownerIDs)).result)

  private def getByOwnableID(ownableID: String) = db.run(splitTable.filter(_.ownableID === ownableID).result)

  private def getByOwnerOrOwnableID(id: String) = db.run(splitTable.filter(x => x.ownerID === id || x.ownableID === id).result)

  private def getByOwnerAndOwnableID(ownerID: String, ownableID: String) = db.run(splitTable.filter(x => x.ownableID === ownableID && x.ownerID === ownerID).result.headOption)

  private def tryGetByOwnerAndOwnableID(ownerID: String, ownableID: String) = db.run(splitTable.filter(x => x.ownableID === ownableID && x.ownerID === ownerID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.SPLIT_NOT_FOUND, noSuchElementException)
    }
  }

  private def getAllSplits = db.run(splitTable.result)

  private def deleteByIDs(ownerID: String, ownableID: String): Future[Int] = db.run(splitTable.filter(x => x.ownerID === ownerID && x.ownableID === ownableID).delete.asTry).map {
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
    private implicit val timeout = Timeout(constants.Actor.ACTOR_ASK_TIMEOUT) // needed for `?` below
    private val splitActorRegion = {
      ClusterSharding(blockchain.Service.actorSystem).start(
        typeName = "splitClusterRegion",
        entityProps = SplitActor.props(Splits.this),
        settings = ClusterShardingSettings(blockchain.Service.actorSystem),
        extractEntityId = SplitActor.idExtractor,
        extractShardId = SplitActor.shardResolver
      )
    }

    def createSplitWithActor(split: Split): Future[String] = (splitActorRegion ? CreateSplit(uniqueId, split)).mapTo[String]

    def create(split: Split): Future[String] = add(split)

    def getSplitByOwnerWithActor(ownerID: String): Future[Seq[Split]] = (splitActorRegion ? GetByOwner(uniqueId, ownerID)).mapTo[Seq[Split]]

    def getByOwner(ownerID: String): Future[Seq[Split]] = getByOwnerID(ownerID)

    def getSplitByOwnerIDsWithActo(ownerIDs: Seq[String]): Future[Seq[Split]] = (splitActorRegion ? GetByOwnerIDs(uniqueId, ownerIDs)).mapTo[Seq[Split]]

    def getByOwnerIDs(ownerIDs: Seq[String]): Future[Seq[Split]] = getAllByOwnerIDs(ownerIDs)

    def getSplitByOwnableWithActo(ownableID: String): Future[Seq[Split]] = (splitActorRegion ? GetByOwnable(uniqueId, ownableID)).mapTo[Seq[Split]]

    def getByOwnable(ownableID: String): Future[Seq[Split]] = getByOwnableID(ownableID)

    def getSplitByOwnerOrOwnableWithActo(id: String): Future[Seq[Split]] = (splitActorRegion ? GetByOwnerOrOwnable(uniqueId, id)).mapTo[Seq[Split]]

    def getByOwnerOrOwnable(id: String): Future[Seq[Split]] = getByOwnerOrOwnableID(id)

    def getSplitWithActor(ownerID: String, ownableID: String): Future[Option[Split]] = (splitActorRegion ? GetSplit(uniqueId, ownerID, ownableID)).mapTo[Option[Split]]

    def get(ownerID: String, ownableID: String): Future[Option[Split]] = getByOwnerAndOwnableID(ownerID = ownerID, ownableID = ownableID)

    def tryGetSplitWithActor(ownerID: String, ownableID: String): Future[Option[Split]] = (splitActorRegion ? TryGetSplit(uniqueId, ownerID, ownableID)).mapTo[Option[Split]]

    def tryGet(ownerID: String, ownableID: String): Future[Split] = tryGetByOwnerAndOwnableID(ownerID = ownerID, ownableID = ownableID)

    def getAllSplitWithActor: Future[Seq[Split]] = (splitActorRegion ? GetAllSplit(uniqueId)).mapTo[Seq[Split]]

    def getAll: Future[Seq[Split]] = getAllSplits

    def insertMultipleSplitWithActor(splits: Seq[Split]): Future[Seq[String]] = (splitActorRegion ? InsertMultipleSplit(uniqueId, splits)).mapTo[Seq[String]]

    def insertMultiple(splits: Seq[Split]): Future[Seq[String]] = addMultiple(splits)

    def insertOrUpdateSplitWithActor(split: Split): Future[Int] = (splitActorRegion ? InsertOrUpdateSplit(uniqueId, split)).mapTo[Int]

    def insertOrUpdate(split: Split): Future[Int] = upsert(split)

    def deleteSplitWithActor(ownerID: String, ownableID: String): Future[Int] = (splitActorRegion ? DeleteSplit(uniqueId, ownerID, ownableID)).mapTo[Int]

    def delete(ownerID: String, ownableID: String): Future[Int] = deleteByIDs(ownerID = ownerID, ownableID = ownableID)

  }

  object Utility {

    def onSend(splitSend: SplitSend)(implicit header: Header): Future[Unit] = {
      if (splitSend.fromID != splitSend.toID) {
        val oldFromSplit = Service.tryGet(ownerID = splitSend.fromID, ownableID = splitSend.ownableID)
        val oldToSplit = Service.get(ownerID = splitSend.toID, ownableID = splitSend.ownableID)

        def updateOrDeleteFromSplit(oldFromSplit: Split) = if ((oldFromSplit.split - splitSend.split) == 0) Service.delete(ownerID = splitSend.fromID, ownableID = splitSend.ownableID) else Service.insertOrUpdate(oldFromSplit.copy(split = oldFromSplit.split - splitSend.split))

        def upsertToSplit(oldToSplit: Option[Split]) = oldToSplit.fold(Service.insertOrUpdate(Split(ownerID = splitSend.toID, ownableID = splitSend.ownableID, split = splitSend.split)))(oldSplit => Service.insertOrUpdate(oldSplit.copy(split = oldSplit.split + splitSend.split)))

        def masterOperations(oldFromSplitDeleted: Boolean) = {
          val oldFromMasterSplit = masterSplits.Service.tryGet(ownableID = splitSend.ownableID, ownerID = splitSend.fromID)
          val oldToSplitExists = masterSplits.Service.checkExists(ownableID = splitSend.ownableID, ownerID = splitSend.toID)

          def updateSplits(oldFromMasterSplit: masterSplit, oldToSplitExists: Boolean) = {
            val updateOrDeleteFromSplit = if (oldFromSplitDeleted) masterSplits.Service.delete(ownableID = splitSend.ownableID, ownerID = splitSend.fromID) else Future(0)
            val updateToSplit = if (!oldToSplitExists) masterSplits.Service.insertOrUpdate(masterSplit(ownableID = splitSend.ownableID, ownerID = splitSend.toID, entityType = oldFromMasterSplit.entityType, status = Option(true))) else Future(0)
            for {
              _ <- updateOrDeleteFromSplit
              _ <- updateToSplit
            } yield ()
          }

          for {
            oldFromMasterSplit <- oldFromMasterSplit
            oldToSplitExists <- oldToSplitExists
            _ <- updateSplits(oldFromMasterSplit = oldFromMasterSplit, oldToSplitExists = oldToSplitExists)
          } yield ()
        }

        (for {
          oldFromSplit <- oldFromSplit
          oldToSplit <- oldToSplit
          _ <- updateOrDeleteFromSplit(oldFromSplit)
          _ <- upsertToSplit(oldToSplit)
          _ <- masterOperations((oldFromSplit.split - splitSend.split) == 0)
        } yield ()).recover {
          case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.SPLIT_SEND + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
        }
      } else Future()
    }

    def onWrap(splitWrap: SplitWrap)(implicit header: Header): Future[Unit] = {
      val updateAccountBalance = blockchainBalances.Utility.insertOrUpdateBalance(splitWrap.from)
      val updateSplits = Future.traverse(splitWrap.coins) { coin =>
        val oldSplit = Service.get(ownerID = splitWrap.fromID, ownableID = coin.denom)

        def upsertSplit(oldSplit: Option[Split]) = oldSplit.fold(Service.insertOrUpdate(Split(ownerID = splitWrap.fromID, ownableID = coin.denom, split = BigDecimal(coin.amount.value))))(oldSplit => Service.insertOrUpdate(oldSplit.copy(split = oldSplit.split + BigDecimal(coin.amount.value))))

        for {
          oldSplit <- oldSplit
          _ <- upsertSplit(oldSplit)
        } yield ()
      }

      val masterOperations = Future.traverse(splitWrap.coins) { coin =>
        val oldSplit = masterSplits.Service.get(ownableID = coin.denom, ownerID = splitWrap.fromID)

        def upsertSplits(oldSplit: Option[masterSplit]) = oldSplit.fold(masterSplits.Service.insertOrUpdate(masterSplit(ownableID = coin.denom, ownerID = splitWrap.fromID, entityType = constants.Blockchain.Entity.WRAPPED_COIN, status = Option(true))))(_ => masterSplits.Service.markStatusSuccessful(ownableID = coin.denom, ownerID = splitWrap.fromID))

        for {
          oldSplit <- oldSplit
          _ <- upsertSplits(oldSplit)
        } yield ()
      }

      (for {
        _ <- updateAccountBalance
        _ <- updateSplits
        _ <- masterOperations
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.SPLIT_WRAP + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def onUnwrap(splitUnwrap: SplitUnwrap)(implicit header: Header): Future[Unit] = {
      val oldFromSplit = Service.tryGet(ownerID = splitUnwrap.fromID, ownableID = splitUnwrap.ownableID)
      val updateAccountBalance = blockchainBalances.Utility.insertOrUpdateBalance(splitUnwrap.from)

      def updateSplits(oldFromSplit: Split) = if ((oldFromSplit.split - splitUnwrap.split) == 0) Service.delete(ownerID = splitUnwrap.fromID, ownableID = splitUnwrap.ownableID) else Service.insertOrUpdate(oldFromSplit.copy(split = oldFromSplit.split - splitUnwrap.split))

      def masterOperations(oldFromSplit: Split) = if ((oldFromSplit.split - splitUnwrap.split) == 0) masterSplits.Service.delete(ownableID = splitUnwrap.ownableID, ownerID = splitUnwrap.fromID) else Future(0)

      (for {
        oldFromSplit <- oldFromSplit
        _ <- updateSplits(oldFromSplit)
        _ <- updateAccountBalance
        _ <- masterOperations(oldFromSplit)
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.SPLIT_UNWRAP + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def auxiliaryMint(ownerID: String, ownableID: String, splitValue: BigDecimal): Future[Unit] = {
      val oldSplit = Service.get(ownerID = ownerID, ownableID = ownableID)

      def upsertSplit(oldSplit: Option[Split]) = oldSplit.fold(Service.insertOrUpdate(Split(ownerID = ownerID, ownableID = ownableID, split = splitValue)))(oldSplit => Service.insertOrUpdate(oldSplit.copy(split = oldSplit.split + splitValue)))

      val masterOperations = {
        val oldSplit = masterSplits.Service.get(ownableID = ownableID, ownerID = ownerID)

        def upsertSplits(oldSplit: Option[masterSplit]) = oldSplit.fold(masterSplits.Service.insertOrUpdate(masterSplit(ownableID = ownableID, ownerID = ownerID, entityType = constants.Blockchain.Entity.ASSET, status = Option(true))))(_ => masterSplits.Service.markStatusSuccessful(ownableID = ownableID, ownerID = ownerID))

        for {
          oldSplit <- oldSplit
          _ <- upsertSplits(oldSplit)
        } yield ()
      }

      (for {
        oldSplit <- oldSplit
        _ <- upsertSplit(oldSplit)
        _ <- masterOperations
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def auxiliaryBurn(ownerID: String, ownableID: String, splitValue: BigDecimal): Future[Unit] = {
      val oldSplit = Service.tryGet(ownerID = ownerID, ownableID = ownableID)

      def updateOrDelete(oldSplit: Split) = if ((oldSplit.split - splitValue) == 0) Service.delete(ownerID = ownerID, ownableID = ownableID) else Service.insertOrUpdate(oldSplit.copy(split = oldSplit.split - splitValue))

      def masterOperations(oldFromSplit: Split) = if ((oldFromSplit.split - splitValue) == 0) masterSplits.Service.delete(ownableID = ownableID, ownerID = ownerID) else Future(0)

      (for {
        oldSplit <- oldSplit
        _ <- updateOrDelete(oldSplit)
        _ <- masterOperations(oldSplit)
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def auxiliaryTransfer(fromID: String, toID: String, ownableID: String, splitValue: BigDecimal): Future[Unit] = {
      if (fromID != toID) {
        val oldFromSplit = Service.tryGet(ownerID = fromID, ownableID = ownableID)
        val oldToSplit = Service.get(ownerID = toID, ownableID = ownableID)

        def updateOrDeleteFromSplit(oldFromSplit: Split) = if ((oldFromSplit.split - splitValue) == 0) Service.delete(ownerID = fromID, ownableID = ownableID) else Service.insertOrUpdate(oldFromSplit.copy(split = oldFromSplit.split - splitValue))

        def upsertToSplit(oldToSplit: Option[Split]) = oldToSplit.fold(Service.insertOrUpdate(Split(ownerID = toID, ownableID = ownableID, split = splitValue)))(oldSplit => Service.insertOrUpdate(oldSplit.copy(split = oldSplit.split + splitValue)))

        def masterOperations(oldFromSplitDeleted: Boolean) = {
          val oldFromMasterSplit = masterSplits.Service.tryGet(ownableID = ownableID, ownerID = fromID)
          val oldToSplitExists = masterSplits.Service.checkExists(ownableID = ownableID, ownerID = toID)

          def updateSplits(oldFromMasterSplit: masterSplit, oldToSplitExists: Boolean) = {
            val updateOrDeleteFromSplit = if (oldFromSplitDeleted) masterSplits.Service.delete(ownableID = ownableID, ownerID = fromID) else Future(0)
            val updateToSplit = if (!oldToSplitExists) masterSplits.Service.insertOrUpdate(masterSplit(ownableID = ownableID, ownerID = toID, entityType = oldFromMasterSplit.entityType, status = Option(true))) else Future(0)
            for {
              _ <- updateOrDeleteFromSplit
              _ <- updateToSplit
            } yield ()
          }

          for {
            oldFromMasterSplit <- oldFromMasterSplit
            oldToSplitExists <- oldToSplitExists
            _ <- updateSplits(oldFromMasterSplit = oldFromMasterSplit, oldToSplitExists = oldToSplitExists)
          } yield ()
        }

        for {
          oldFromSplit <- oldFromSplit
          oldToSplit <- oldToSplit
          _ <- updateOrDeleteFromSplit(oldFromSplit)
          _ <- upsertToSplit(oldToSplit)
          _ <- masterOperations((oldFromSplit.split - splitValue) == 0)
        } yield ()
      } else Future()
    }
  }

}