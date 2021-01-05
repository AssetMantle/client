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

case class Classification(id: String, entityType: String, fromID: String, label: Option[String], status: Option[Boolean], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Classifications @Inject()(
                                 configuration: Configuration,
                                 blockchainClassifications: blockchain.Classifications,
                                 protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_CLASSIFICATION

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val identityTable = TableQuery[ClassificationTable]

  private def add(identity: Classification): Future[String] = db.run((identityTable returning identityTable.map(_.id) += identity).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def addMultiple(identities: Seq[Classification]): Future[Seq[String]] = db.run((identityTable returning identityTable.map(_.id) ++= identities).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(identity: Classification): Future[Int] = db.run(identityTable.insertOrUpdate(identity).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateStatusByIDAndEntityType(id: String, entityType: String, status: Option[Boolean]): Future[Int] = db.run(identityTable.filter(x => x.id === id && x.entityType === entityType).map(_.status.?).update(status).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateLabelByIDAndEntityType(id: String, entityType: String, label: Option[String]): Future[Int] = db.run(identityTable.filter(x => x.id === id && x.entityType === entityType).map(_.label.?).update(label).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def tryGetFromIDByIDAndEntityType(id: String, entityType: String): Future[String] = db.run(identityTable.filter(x => x.id === id && x.entityType === entityType).map(_.fromID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getByID(id: String) = db.run(identityTable.filter(_.id === id).result.headOption)

  private def getByFromIDs(ids: Seq[String]) = db.run(identityTable.filter(_.fromID.inSet(ids)).result)

  private def getByEntityTypeAndFromIDs(entityType: String, ids: Seq[String]) = db.run(identityTable.filter(x => x.fromID.inSet(ids) && x.entityType === entityType).result)

  private def deleteByID(id: String) = db.run(identityTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case _: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.logMessage + " " + id)
        0
    }
  }

  private[models] class ClassificationTable(tag: Tag) extends Table[Classification](tag, "Classification") {

    def * = (id, entityType, fromID, label.?, status.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Classification.tupled, Classification.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def entityType = column[String]("entityType", O.PrimaryKey)

    def fromID = column[String]("fromID")

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

    def create(id: String, entityType: String, fromID: String, label: Option[String], status: Option[Boolean]): Future[String] = add(Classification(id = id, entityType = entityType, fromID = fromID, label = label, status = status))

    def insertMultiple(classifications: Seq[Classification]): Future[Seq[String]] = addMultiple(classifications)

    def insertOrUpdate(id: String, entityType: String, fromID: String, label: Option[String], status: Option[Boolean]): Future[Int] = upsert(Classification(id = id, entityType = entityType, fromID = fromID, label = label, status = status))

    def delete(id: String): Future[Int] = deleteByID(id)

    def get(id: String): Future[Option[Classification]] = getByID(id)

    def markStatusSuccessful(id: String, entityType: String): Future[Int] = updateStatusByIDAndEntityType(id = id, entityType = entityType, status = Option(true))

    def updateLabel(id: String, entityType: String, label: String): Future[Int] = updateLabelByIDAndEntityType(id = id, entityType = entityType, label = Option(label))

    def getByIdentityIDs(identities: Seq[String]): Future[Seq[Classification]] = getByFromIDs(identities)

    def getIdentityDefinitionsByIdentityIDs(identities: Seq[String]): Future[Seq[Classification]] = getByEntityTypeAndFromIDs(entityType = constants.Blockchain.Entity.IDENTITY_DEFINITION, ids = identities)

    def getAssetDefinitionsByIdentityIDs(identities: Seq[String]): Future[Seq[Classification]] = getByEntityTypeAndFromIDs(entityType = constants.Blockchain.Entity.ASSET_DEFINITION, ids = identities)

    def getOrderDefinitionsByIdentityIDs(identities: Seq[String]): Future[Seq[Classification]] = getByEntityTypeAndFromIDs(entityType = constants.Blockchain.Entity.ORDER_DEFINITION, ids = identities)

    def tryGetFromID(id: String, entityType: String): Future[String] = tryGetFromIDByIDAndEntityType(id = id, entityType = entityType)
  }

}