package models.blockchain

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.TransactionMessages.{SplitSend, SplitUnwrap, SplitWrap}
import models.master
import models.master.{Split => masterSplit}
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
                        blockchainAccounts: Accounts,
                        getSplit: GetSplit,
                        masterSplits: master.Splits
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

  private def getAllByOwnerIDs(ownerIDs: Seq[String]) = db.run(splitTable.filter(_.ownerID.inSet(ownerIDs)).result)

  private def getByOwnableID(ownableID: String) = db.run(splitTable.filter(_.ownableID === ownableID).result)

  private def getByOwnerOrOwnableID(id: String) = db.run(splitTable.filter(x => x.ownerID === id || x.ownableID === id).result)

  private def getByOwnerAndOwnableID(ownerID: String, ownableID: String) = db.run(splitTable.filter(_.ownableID === ownableID).filter(_.ownerID === ownerID).result.headOption)

  private def tryGetByOwnerAndOwnableID(ownerID: String, ownableID: String) = db.run(splitTable.filter(_.ownableID === ownableID).filter(_.ownerID === ownerID).result.head.asTry).map {
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

    def getByOwnerIDs(ownerIDs: Seq[String]): Future[Seq[Split]] = getAllByOwnerIDs(ownerIDs)

    def getByOwnable(ownableID: String): Future[Seq[Split]] = getByOwnableID(ownableID)

    def getByOwnerOrOwnable(id: String): Future[Seq[Split]] = getByOwnerOrOwnableID(id)

    def get(ownerID: String, ownableID: String): Future[Option[Split]] = getByOwnerAndOwnableID(ownerID = ownerID, ownableID = ownableID)

    def tryGet(ownerID: String, ownableID: String): Future[Split] = tryGetByOwnerAndOwnableID(ownerID = ownerID, ownableID = ownableID)

    def getAll: Future[Seq[Split]] = getAllSplits

    def insertMultiple(splits: Seq[Split]): Future[Seq[String]] = addMultiple(splits)

    def insertOrUpdate(split: Split): Future[Int] = upsert(split)

    def delete(ownerID: String, ownableID: String): Future[Int] = deleteByIDs(ownerID = ownerID, ownableID = ownableID)

  }

  object Utility {

    def onSend(splitSend: SplitSend): Future[Unit] = {
      if (splitSend.fromID != splitSend.toID) {
        val oldFromSplit = Service.tryGet(ownerID = splitSend.fromID, ownableID = splitSend.ownableID)
        val oldToSplit = Service.get(ownerID = splitSend.toID, ownableID = splitSend.ownableID)

        def updateOrDeleteFromSplit(oldFromSplit: Split) = if ((oldFromSplit.split - splitSend.split) == 0) Service.delete(ownerID = splitSend.fromID, ownableID = splitSend.ownableID) else Service.insertOrUpdate(oldFromSplit.copy(split = oldFromSplit.split - splitSend.split))

        def upsertToSplit(oldToSplit: Option[Split]) = oldToSplit.fold(Service.insertOrUpdate(Split(ownerID = splitSend.toID, ownableID = splitSend.ownableID, split = splitSend.split)))(oldSplit => Service.insertOrUpdate(oldSplit.copy(split = oldSplit.split + splitSend.split)))

        def masterOperations(oldFromSplitDeleted: Boolean) = {
          val oldFromMasterSplit = masterSplits.Service.tryGet(entityID = splitSend.ownableID, ownerID = splitSend.fromID)
          val oldToSplitExists = masterSplits.Service.checkExists(entityID = splitSend.ownableID, ownerID = splitSend.toID)

          def updateSplits(oldFromMasterSplit: masterSplit, oldToSplitExists: Boolean) = {
            val updateOrDeleteFromSplit = if (oldFromSplitDeleted) masterSplits.Service.delete(entityID = splitSend.ownableID, ownerID = splitSend.fromID) else Future(0)
            val updateToSplit = if (!oldToSplitExists) masterSplits.Service.insertOrUpdate(masterSplit(entityID = splitSend.ownableID, ownerID = splitSend.toID, entityType = oldFromMasterSplit.entityType, label = oldFromMasterSplit.label, status = Option(true))) else Future(0)
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
          case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
        }
      } else Future()
    }

    def onWrap(splitWrap: SplitWrap): Future[Unit] = {
      val updateAccountBalance = blockchainAccounts.Utility.insertOrUpdateAccountBalance(splitWrap.from)
      val updateSplits = Future.traverse(splitWrap.coins) { coin =>
        val oldSplit = Service.get(ownerID = splitWrap.fromID, ownableID = coin.denom)

        def upsertSplit(oldSplit: Option[Split]) = oldSplit.fold(Service.insertOrUpdate(Split(ownerID = splitWrap.fromID, ownableID = coin.denom, split = BigDecimal(coin.amount.value))))(oldSplit => Service.insertOrUpdate(oldSplit.copy(split = oldSplit.split + BigDecimal(coin.amount.value))))

        for {
          oldSplit <- oldSplit
          _ <- upsertSplit(oldSplit)
        } yield ()
      }

      val masterOperations = Future.traverse(splitWrap.coins) { coin =>
        val oldSplit = masterSplits.Service.get(entityID = coin.denom, ownerID = splitWrap.fromID)

        def upsertSplits(oldSplit: Option[masterSplit]) = oldSplit.fold(masterSplits.Service.insertOrUpdate(masterSplit(entityID = coin.denom, ownerID = splitWrap.fromID, entityType = constants.Blockchain.Entity.WRAPPED_COIN, label = None, status = Option(true))))(_ => masterSplits.Service.markStatusSuccessful(entityID = coin.denom, ownerID = splitWrap.fromID))

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
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onUnwrap(splitUnwrap: SplitUnwrap): Future[Unit] = {
      val oldFromSplit = Service.tryGet(ownerID = splitUnwrap.fromID, ownableID = splitUnwrap.ownableID)
      val updateAccountBalance = blockchainAccounts.Utility.insertOrUpdateAccountBalance(splitUnwrap.from)

      def updateSplits(oldFromSplit: Split) = if ((oldFromSplit.split - splitUnwrap.split) == 0) Service.delete(ownerID = splitUnwrap.fromID, ownableID = splitUnwrap.ownableID) else Service.insertOrUpdate(oldFromSplit.copy(split = oldFromSplit.split - splitUnwrap.split))

      def masterOperations(oldFromSplit: Split) = if ((oldFromSplit.split - splitUnwrap.split) == 0) masterSplits.Service.delete(entityID = splitUnwrap.ownableID, ownerID = splitUnwrap.fromID) else Future(0)

      (for {
        oldFromSplit <- oldFromSplit
        _ <- updateSplits(oldFromSplit)
        _ <- updateAccountBalance
        _ <- masterOperations(oldFromSplit)
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def auxiliaryMint(ownerID: String, ownableID: String, splitValue: BigDecimal): Future[Unit] = {
      val oldSplit = Service.get(ownerID = ownerID, ownableID = ownableID)

      def upsertSplit(oldSplit: Option[Split]) = oldSplit.fold(Service.insertOrUpdate(Split(ownerID = ownerID, ownableID = ownableID, split = splitValue)))(oldSplit => Service.insertOrUpdate(oldSplit.copy(split = oldSplit.split + splitValue)))

      val masterOperations = {
        val oldSplit = masterSplits.Service.get(entityID = ownableID, ownerID = ownerID)

        def upsertSplits(oldSplit: Option[masterSplit]) = oldSplit.fold(masterSplits.Service.insertOrUpdate(masterSplit(entityID = ownableID, ownerID = ownerID, entityType = constants.Blockchain.Entity.ASSET, label = None, status = Option(true))))(_ => masterSplits.Service.markStatusSuccessful(entityID = ownableID, ownerID = ownerID))

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

      def masterOperations(oldFromSplit: Split) = if ((oldFromSplit.split - splitValue) == 0) masterSplits.Service.delete(entityID = ownableID, ownerID = ownerID) else Future(0)

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
          val oldFromMasterSplit = masterSplits.Service.tryGet(entityID = ownableID, ownerID = fromID)
          val oldToSplitExists = masterSplits.Service.checkExists(entityID = ownableID, ownerID = toID)

          def updateSplits(oldFromMasterSplit: masterSplit, oldToSplitExists: Boolean) = {
            val updateOrDeleteFromSplit = if (oldFromSplitDeleted) masterSplits.Service.delete(entityID = ownableID, ownerID = fromID) else Future(0)
            val updateToSplit = if (!oldToSplitExists) masterSplits.Service.insertOrUpdate(masterSplit(entityID = ownableID, ownerID = toID, entityType = oldFromMasterSplit.entityType, label = oldFromMasterSplit.label, status = Option(true))) else Future(0)
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
          _ <- masterOperations((oldFromSplit.split - splitValue) == 0)
        } yield ()).recover {
          case baseException: BaseException => throw baseException
        }
      } else Future()
    }
  }

}