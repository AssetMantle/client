package models.master

import exceptions.BaseException
import models.Trait.Logged
import models.blockchain
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Split(ownableID: String, ownerID: String, entityType: String, status: Option[Boolean], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Splits @Inject()(
                        configuration: Configuration,
                        blockchainClassifications: blockchain.Classifications,
                        protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_SPLIT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val splitTable = TableQuery[SplitTable]

  private def add(split: Split): Future[String] = db.run((splitTable returning splitTable.map(_.ownableID) += split).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def addMultiple(splits: Seq[Split]): Future[Seq[String]] = db.run((splitTable returning splitTable.map(_.ownableID) ++= splits).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(split: Split): Future[Int] = db.run(splitTable.insertOrUpdate(split).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def getByOwnableIDAndOwnerID(ownableID: String, ownerID: String) = db.run(splitTable.filter(x => x.ownableID === ownableID && x.ownerID === ownerID).result.headOption)

  private def tryGetByOwnableIDAndOwnerID(ownableID: String, ownerID: String) = db.run(splitTable.filter(x => x.ownableID === ownableID && x.ownerID === ownerID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def tryGetOwnerIDByOwnableID(ownableID: String) = db.run(splitTable.filter(_.ownableID === ownableID).map(_.ownerID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByOwnableIDAndOwnerID(ownableID: String, ownerID: String) = db.run(splitTable.filter(x => x.ownableID === ownableID && x.ownerID === ownerID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusByOwnableIDAndOwnerID(ownableID: String, ownerID: String, status: Option[Boolean]): Future[Int] = db.run(splitTable.filter(x => x.ownableID === ownableID && x.ownerID === ownerID).map(_.status.?).update(status).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def getListByOwnableIDs(ownableIDs: Seq[String]) = db.run(splitTable.filter(_.ownableID.inSet(ownableIDs)).result)

  private def getListByOwnerIDs(ownerIDs: Seq[String]) = db.run(splitTable.filter(_.ownerID.inSet(ownerIDs)).result)

  private def getByOwnerIDsAndEntityType(ownerIDs: Seq[String], entityType: String) = db.run(splitTable.filter(x => x.ownerID.inSet(ownerIDs) && x.entityType === entityType).result)

  private def checkExistsByOwnableIDAndOwnerID(ownableID: String, ownerID: String) = db.run(splitTable.filter(x => x.ownableID === ownableID && x.ownerID === ownerID).exists.result)

  private[models] class SplitTable(tag: Tag) extends Table[Split](tag, "Split") {

    def * = (ownableID, ownerID, entityType, status.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Split.tupled, Split.unapply)

    def ownableID = column[String]("ownableID", O.PrimaryKey)

    def ownerID = column[String]("ownerID", O.PrimaryKey)

    def entityType = column[String]("entityType")

    def status = column[Boolean]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(split: Split): Future[String] = add(split)

    def insertMultiple(splits: Seq[Split]): Future[Seq[String]] = addMultiple(splits)

    def insertOrUpdate(split: Split): Future[Int] = upsert(split)

    def delete(ownableID: String, ownerID: String): Future[Int] = deleteByOwnableIDAndOwnerID(ownableID = ownableID, ownerID = ownerID)

    def getAllByIDs(ownableIDs: Seq[String]): Future[Seq[Split]] = getListByOwnableIDs(ownableIDs)

    def getAllByOwnerIDs(ownerIDs: Seq[String]): Future[Seq[Split]] = getListByOwnerIDs(ownerIDs)

    def getAllAssetsByOwnerIDs(ownerIDs: Seq[String]): Future[Seq[Split]] = getByOwnerIDsAndEntityType(ownerIDs, constants.Blockchain.Entity.ASSET)

    def get(ownableID: String, ownerID: String): Future[Option[Split]] = getByOwnableIDAndOwnerID(ownableID = ownableID, ownerID = ownerID)

    def tryGet(ownableID: String, ownerID: String): Future[Split] = tryGetByOwnableIDAndOwnerID(ownableID = ownableID, ownerID = ownerID)

    def tryGetOwnerID(ownableID: String): Future[String] = tryGetOwnerIDByOwnableID(ownableID = ownableID)

    def checkExists(ownableID: String, ownerID: String): Future[Boolean] = checkExistsByOwnableIDAndOwnerID(ownableID = ownableID, ownerID = ownerID)

    def markStatusSuccessful(ownableID: String, ownerID: String): Future[Int] = updateStatusByOwnableIDAndOwnerID(ownableID = ownableID, ownerID = ownerID, Option(true))

  }

}