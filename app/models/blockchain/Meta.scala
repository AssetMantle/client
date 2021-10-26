package models.blockchain

import akka.pattern.ask
import akka.util.Timeout
import dbActors.Service.{masterActor, routerActor}
import dbActors.{AddActor, CheckIfExistsMeta, CreateData, CreateMeta, GetData, GetDataList, GetList, GetMeta, GetMetaList, GetMetas, InsertMultipleData, InsertMultipleMetas, InsertOrUpdateData, InsertOrUpdateMeta, MaintainerActor, MetaActor, TryGetData, TryGetMeta}
import exceptions.BaseException
import models.Trait.Logged
import models.common.DataValue
import models.common.Serializable._
import models.common.TransactionMessages.MetaReveal
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.responses.common.Header
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
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

    implicit val timeout = Timeout(5 seconds) // needed for `?` below

    private val metaActor = dbActors.Service.actorSystem.actorOf(MetaActor.props(Metas.this), "metaActor")

    routerActor ! AddActor(None, metaActor.actorRef)

    def createDataWithActor(data: Data): Future[String] = (masterActor ? CreateData(data)).mapTo[String]

    def create(data: Data): Future[String] = add(Meta(id = data.value.generateHash, dataType = data.dataType, dataValue = data.value.asString))

    def createMetaWithActor(meta: Meta): Future[String] = (masterActor ? CreateMeta(meta)).mapTo[String]

    def create(meta: Meta): Future[String] = add(meta)

    def tryGetMetaWithActor(id: String, dataType: String): Future[Meta] = (masterActor ? TryGetMeta(id, dataType)).mapTo[Meta]

    def tryGet(id: String, dataType: String): Future[Meta] = tryGetByIDAndDataType(id = id, dataType = dataType)

    def tryGetDataWithActor(id: String, dataType: String): Future[Data] = (masterActor ? TryGetData(id, dataType)).mapTo[Data]

    def tryGetData(id: String, dataType: String): Future[Data] = tryGetByIDAndDataType(id = id, dataType = dataType).map(x => DataValue.getData(dataType = x.dataType, dataValue = Option(x.dataValue)))

    def getMetaWithActor(id: String, dataType: String): Future[Option[Meta]] = (masterActor ? GetMeta(id, dataType)).mapTo[Option[Meta]]

    def get(id: String, dataType: String): Future[Option[Meta]] = getByIDAndDataType(id = id, dataType = dataType)

    def getDataWithActor(id: String, dataType: String): Future[Option[Data]] = (masterActor ? GetData(id, dataType)).mapTo[Option[Data]]

    def getData(id: String, dataType: String): Future[Option[Data]] = getByIDAndDataType(id = id, dataType = dataType).map(metaOption => metaOption.fold[Option[Data]](None)(x => Option(DataValue.getData(dataType = x.dataType, dataValue = Option(x.dataValue)))))

    def getMetasWithActor(ids: Seq[String]): Future[Seq[Meta]] = (masterActor ? GetMetas(ids)).mapTo[Seq[Meta]]

    def get(ids: Seq[String]): Future[Seq[Meta]] = getByIDs(ids.filter(x => x != ""))

    def getDataListWithActor(ids: Seq[String]): Future[Seq[Data]] = (masterActor ? GetDataList(ids)).mapTo[Seq[Data]]

    def getDataList(ids: Seq[String]): Future[Seq[Data]] = getByIDs(ids.filter(x => x != "")).map(_.map(meta => DataValue.getData(dataType = meta.dataType, dataValue = Option(meta.dataValue))))

    def getListWithActor(ids: Seq[String]): Future[Seq[Meta]] = (masterActor ? GetMetaList(ids)).mapTo[Seq[Meta]]

    def getList(ids: Seq[String]): Future[Seq[Meta]] = getByIDs(ids.filter(x => x != ""))

    def insertMultipleMetasWithActor(metaList: Seq[Meta]): Future[Seq[String]] = (masterActor ? InsertMultipleMetas(metaList)).mapTo[Seq[String]]

    def insertMultiple(metaList: Seq[Meta]): Future[Seq[String]] = addMultiple(metaList)

    def insertMultipleDataWithActor(dataList: Seq[Data]): Future[Seq[String]] = (masterActor ? InsertMultipleData(dataList)).mapTo[Seq[String]]

    def insertMultipleData(dataList: Seq[Data]): Future[Seq[String]] = addMultiple(dataList.map(x => Meta(id = x.value.generateHash, dataType = x.dataType, dataValue = x.value.asString)))

    def insertOrUpdateMetaWithActor(meta: Meta): Future[Int] = (masterActor ? InsertOrUpdateMeta(meta)).mapTo[Int]

    def insertOrUpdate(meta: Meta): Future[Int] = upsert(meta)

    def insertOrUpdateDataWithActor(data: Data): Future[Int] = (masterActor ? InsertOrUpdateData(data)).mapTo[Int]

    def insertOrUpdate(data: Data): Future[Int] = upsert(Meta(id = data.value.generateHash, dataType = data.dataType, dataValue = data.value.asString))

    def checkIfExistsMetaWithActor(id: String, dataType: String): Future[Boolean] = (masterActor ? CheckIfExistsMeta(id, dataType)).mapTo[Boolean]

    def checkIfExists(id: String, dataType: String): Future[Boolean] = checkIfExistsByIDAndDataType(id = id, dataType = dataType)

  }

  object Utility {

    def onReveal(metaReveal: MetaReveal)(implicit header: Header): Future[Unit] = {
      val upsertMeta = Service.insertOrUpdate(metaReveal.metaFact.data)
      (for {
        _ <- upsertMeta
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.META_REVEAL + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

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