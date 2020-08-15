package models.blockchain

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Serializable.Fact
import models.common.TransactionMessages.MetaReveal
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.GetMeta
import queries.responses.MetaResponse.{Response => MetaResponse}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Meta(id: String, data: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

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

  private def add(meta: Meta): Future[String] = db.run((metaTable returning metaTable.map(_.id) += meta).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.META_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(metas: Seq[Meta]): Future[Seq[String]] = db.run((metaTable returning metaTable.map(_.id) ++= metas).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.META_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(meta: Meta): Future[Int] = db.run(metaTable.insertOrUpdate(meta).asTry).map {
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

  private def getAllMetas = db.run(metaTable.result)

  private[models] class MetaTable(tag: Tag) extends Table[Meta](tag, "Meta_BC") {

    def * = (id, data, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Meta.tupled, Meta.unapply)

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

    def tryGet(id: String): Future[Meta] = tryGetByID(id)

    def getAll: Future[Seq[Meta]] = getAllMetas

    def insertMultiple(metas: Seq[Meta]): Future[Seq[String]] = addMultiple(metas)

    def insertOrUpdate(meta: Meta): Future[Int] = upsert(meta)

  }

  object Utility {

    def onReveal(metaReveal: MetaReveal): Future[Unit] = {
      val metaID = utilities.Hash.getHash(metaReveal.data)
      val metaResponse = getMeta.Service.get(metaID)

      def upsert(metaResponse: MetaResponse) = metaResponse.result.value.metas.value.list.find(_.value.id.value.idString == metaID).fold(throw new BaseException(constants.Response.META_NOT_FOUND))(meta => Service.insertOrUpdate(Meta(id = metaID, data = meta.value.data)))

      (for {
        metaResponse <- metaResponse
        _ <- upsert(metaResponse)
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def checkAndUpsertMetas(facts: Seq[Fact]): Future[Seq[Int]] = Future.traverse(facts)(fact => if (fact.value.isMeta) Service.insertOrUpdate(Meta(id = fact.value.getHash, data = fact.value.get)) else Future(0))
  }

}