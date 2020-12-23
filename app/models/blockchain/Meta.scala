package models.blockchain

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.DataValue
import models.common.Serializable._
import models.common.TransactionMessages.MetaReveal
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.GetMeta
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Meta(id: String, dataType: String, dataValue: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Metas @Inject()(
                       protected val databaseConfigProvider: DatabaseConfigProvider,
                       configuration: Configuration,
                       utilitiesOperations: utilities.Operations,
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

  private def tryGetByIDAndDataType(id: String, dataType: String) = db.run(metaTable.filter(x => x.id === id && x.dataType === dataType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.META_NOT_FOUND, noSuchElementException)
    }
  }

  private def getByIDAndDataType(id: String, dataType: String) = db.run(metaTable.filter(x => x.id === id && x.dataType === dataType).result.headOption)

  private def getByIDs(ids: Seq[String]) = db.run(metaTable.filter(_.id.inSet(ids)).result)

  private def checkIfExistsByIDAndDataType(id: String, dataType: String) = db.run(metaTable.filter(x => x.id === id && x.dataType === dataType).exists.result)

  private[models] class MetaTable(tag: Tag) extends Table[Meta](tag, "Meta_BC") {

    def * = (id, dataType, dataValue, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Meta.tupled, Meta.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def dataType = column[String]("dataType", O.PrimaryKey)

    def dataValue = column[String]("dataValue")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(data: Data): Future[String] = add(Meta(id = data.value.generateHash, dataType = data.dataType, dataValue = data.value.asString))

    def create(meta: Meta): Future[String] = add(meta)

    def tryGet(id: String, dataType: String): Future[Meta] = tryGetByIDAndDataType(id = id, dataType = dataType)

    def tryGetData(id: String, dataType: String): Future[Data] = tryGetByIDAndDataType(id = id, dataType = dataType).map(x => DataValue.getData(dataType = x.dataType, dataValue = Option(x.dataValue)))

    def get(id: String, dataType: String): Future[Option[Meta]] = getByIDAndDataType(id = id, dataType = dataType)

    def getData(id: String, dataType: String): Future[Option[Data]] = getByIDAndDataType(id = id, dataType = dataType).map(metaOption => metaOption.fold[Option[Data]](None)(x => Option(DataValue.getData(dataType = x.dataType, dataValue = Option(x.dataValue)))))

    def get(ids: Seq[String]): Future[Seq[Meta]] = getByIDs(ids.filter(x => x != ""))

    def getDataList(ids: Seq[String]): Future[Seq[Data]] = getByIDs(ids.filter(x => x != "")).map(_.map(meta => DataValue.getData(dataType = meta.dataType, dataValue = Option(meta.dataValue))))

    def insertMultiple(metaList: Seq[Meta]): Future[Seq[String]] = addMultiple(metaList)

    def insertMultipleData(dataList: Seq[Data]): Future[Seq[String]] = addMultiple(dataList.map(x => Meta(id = x.value.generateHash, dataType = x.dataType, dataValue = x.value.asString)))

    def insertOrUpdate(meta: Meta): Future[Int] = upsert(meta)

    def insertOrUpdate(data: Data): Future[Int] = upsert(Meta(id = data.value.generateHash, dataType = data.dataType, dataValue = data.value.asString))

    def checkIfExists(id: String, dataType: String): Future[Boolean] = checkIfExistsByIDAndDataType(id = id, dataType = dataType)

  }

  object Utility {

    def onReveal(metaReveal: MetaReveal): Future[Unit] = {
      val upsertMeta = Service.insertOrUpdate(metaReveal.metaFact.data)

      (for {
        _ <- upsertMeta
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    //TODO Can be optimized by directly doing removeData() wherever auxiliaryScrub is called. Haven't done to keep logic in same order as BC
    def auxiliaryScrub(metaPropertyList: Seq[MetaProperty]): Future[Seq[Property]] = {
      val upsertMetas = utilitiesOperations.traverse(metaPropertyList) { metaProperty =>
        if (metaProperty.metaFact.getHash != "") {
          val upsertMeta = Service.insertOrUpdate(metaProperty.metaFact.data)

          (for {
            _ <- upsertMeta
          } yield ()
            ).recover {
            case _: BaseException =>
          }
        } else Future()
      }

      (for {
        _ <- upsertMetas
      } yield metaPropertyList.map(_.removeData())
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

  }

}