package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.blockchain
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Split(entityID: String, ownerID: String, entityType: String, label: Option[String], status: Option[Boolean], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

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

  private def add(split: Split): Future[String] = db.run((splitTable returning splitTable.map(_.entityID) += split).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def addMultiple(splits: Seq[Split]): Future[Seq[String]] = db.run((splitTable returning splitTable.map(_.entityID) ++= splits).asTry).map {
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

  private def getByEntityIDAndOwnerID(entityID: String, ownerID: String) = db.run(splitTable.filter(x => x.entityID === entityID && x.ownerID === ownerID).result.headOption)

  private def tryGetByEntityIDAndOwnerID(entityID: String, ownerID: String) = db.run(splitTable.filter(x => x.entityID === entityID && x.ownerID === ownerID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def tryGetOwnerIDByEntityIDAndEntityType(entityID: String, entityType: String) = db.run(splitTable.filter(x => x.entityID === entityID && x.entityType === entityType).map(_.ownerID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByEntityIDAndOwnerID(entityID: String, ownerID: String) = db.run(splitTable.filter(x => x.entityID === entityID && x.ownerID === ownerID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusByEntityIDAndOwnerID(entityID: String, ownerID: String, status: Option[Boolean]): Future[Int] = db.run(splitTable.filter(x => x.entityID === entityID && x.ownerID === ownerID).map(_.status.?).update(status).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def getListByEntityIDs(entityIDs: Seq[String]) = db.run(splitTable.filter(_.entityID.inSet(entityIDs)).result)

  private def getListByOwnerIDs(ownerIDs: Seq[String]) = db.run(splitTable.filter(_.ownerID.inSet(ownerIDs)).result)

  private def checkExistsByEntityIDAndOwnerID(entityID: String, ownerID: String) = db.run(splitTable.filter(x => x.entityID === entityID && x.ownerID === ownerID).exists.result)

  private def updateLabelByIDAndEntityType(ownerID: String, entityType: String, label: Option[String]): Future[Int] = db.run(splitTable.filter(x => x.ownerID === ownerID && x.entityType === entityType).map(_.label.?).update(label).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class SplitTable(tag: Tag) extends Table[Split](tag, "Split") {

    def * = (entityID, ownerID, entityType, label.?, status.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Split.tupled, Split.unapply)

    def entityID = column[String]("entityID", O.PrimaryKey)

    def ownerID = column[String]("ownerID", O.PrimaryKey)

    def entityType = column[String]("entityType")

    def label = column[String]("label")

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

    def delete(entityID: String, ownerID: String): Future[Int] = deleteByEntityIDAndOwnerID(entityID = entityID, ownerID = ownerID)

    def getAllByIDs(entityIDs: Seq[String]): Future[Seq[Split]] = getListByEntityIDs(entityIDs)

    def getAllByOwnerIDs(ownerIDs: Seq[String]): Future[Seq[Split]] = getListByOwnerIDs(ownerIDs)

    def get(entityID: String, ownerID: String): Future[Option[Split]] = getByEntityIDAndOwnerID(entityID = entityID, ownerID = ownerID)

    def tryGet(entityID: String, ownerID: String): Future[Split] = tryGetByEntityIDAndOwnerID(entityID = entityID, ownerID = ownerID)

    def tryGetOwnerID(entityID: String, entityType: String): Future[String] = tryGetOwnerIDByEntityIDAndEntityType(entityID = entityID, entityType = entityType)

    def checkExists(entityID: String, ownerID: String): Future[Boolean] = checkExistsByEntityIDAndOwnerID(entityID = entityID, ownerID = ownerID)

    def markStatusSuccessful(entityID: String, ownerID: String): Future[Int] = updateStatusByEntityIDAndOwnerID(entityID = entityID, ownerID = ownerID, Option(true))

    def updateLabel(ownerID: String, entityType: String, label: String): Future[Int] = updateLabelByIDAndEntityType(ownerID = ownerID, entityType = entityType, label = Option(label))

  }

}