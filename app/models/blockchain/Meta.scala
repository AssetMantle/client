package models.blockchain

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Serializable._
import models.common.TransactionMessages.MetaReveal
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.GetMeta
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Meta(id: String, data: Data, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Metas @Inject()(
                       protected val databaseConfigProvider: DatabaseConfigProvider,
                       configuration: Configuration,
                       getMeta: GetMeta,
                     )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_META

  import databaseConfig.profile.api._

  private[models] val metaTable = TableQuery[MetaTable]

  case class MetaSerialized(id: String, data: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Meta = Meta(id = id, data = utilities.JSON.convertJsonStringToObject[Data](data), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(meta: Meta): MetaSerialized = MetaSerialized(id = meta.id, data = Json.toJson(meta.data).toString, createdBy = meta.createdBy, createdOn = meta.createdOn, createdOnTimeZone = meta.createdOnTimeZone, updatedBy = meta.updatedBy, updatedOn = meta.updatedOn, updatedOnTimeZone = meta.updatedOnTimeZone)


  private def add(meta: Meta): Future[String] = db.run((metaTable returning metaTable.map(_.id) += serialize(meta)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.META_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(metas: Seq[Meta]): Future[Seq[String]] = db.run((metaTable returning metaTable.map(_.id) ++= metas.map(x => serialize(x))).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.META_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(meta: Meta): Future[Int] = db.run(metaTable.insertOrUpdate(serialize(meta)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.META_UPSERT_FAILED, psqlException)
    }
  }

  private def tryGetByID(id: String) = db.run(metaTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.META_NOT_FOUND, noSuchElementException)
    }
  }

  private def getByID(id: String) = db.run(metaTable.filter(_.id === id).result.headOption)

  private def getAllMetas = db.run(metaTable.result)

  private def checkIfExistsByID(id: String) = db.run(metaTable.filter(_.id === id).exists.result)

  private[models] class MetaTable(tag: Tag) extends Table[MetaSerialized](tag, "Meta_BC") {

    def * = (id, data, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (MetaSerialized.tupled, MetaSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def data = column[String]("data")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(meta: Meta): Future[String] = add(meta)

    def tryGet(id: String): Future[Meta] = tryGetByID(id).map(_.deserialize)

    def getAll: Future[Seq[Meta]] = getAllMetas.map(_.map(_.deserialize))

    def get(id: String): Future[Option[Meta]] = getByID(id).map(_.map(_.deserialize))

    def insertMultiple(metas: Seq[Meta]): Future[Seq[String]] = addMultiple(metas)

    def insertOrUpdate(meta: Meta): Future[Int] = upsert(meta)

    def checkIfExists(id: String): Future[Boolean] = checkIfExistsByID(id)

  }

  object Utility {

    def onReveal(metaReveal: MetaReveal): Future[Unit] = {
      val upsertMeta = Service.insertOrUpdate(Meta(id = utilities.Hash.getHash(metaReveal.metaFact.getHash), data = metaReveal.metaFact.data))

      (for {
        _ <- upsertMeta
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def auxiliaryScrub(metaPropertyList: Seq[MetaProperty]): Future[Seq[Property]] = {
      val upsertMetas = Future.traverse(metaPropertyList)(metaProperty => Service.insertOrUpdate(Meta(id = metaProperty.metaFact.getHash, data = metaProperty.metaFact.data)))

      (for {
        _ <- upsertMetas
      } yield metaPropertyList.map(_.removeData())
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

  }

}