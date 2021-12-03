package models.blockchain

import akka.pattern.{ask, pipe}
import akka.util.Timeout
import models.blockchain.Metas.{CheckIfExistsMeta, CreateData, CreateMeta, GetData, GetDataList, GetMeta, GetMetaList, GetMetas, InsertMultipleData, InsertMultipleMetas, InsertOrUpdateData, InsertOrUpdateMeta, MetaActor, TryGetData, TryGetMeta}
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import constants.Actor.{NUMBER_OF_ENTITIES, NUMBER_OF_SHARDS}
import exceptions.BaseException
import models.Abstract.ShardedActorRegion
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
import java.util.UUID
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
                     )(implicit executionContext: ExecutionContext) extends ShardedActorRegion {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_META

  import databaseConfig.profile.api._

  private[models] val metaTable = TableQuery[MetaTable]

  private val uniqueId: String = UUID.randomUUID().toString

  private implicit val timeout = Timeout(constants.Actor.ACTOR_ASK_TIMEOUT)

  override def idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateData(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@CreateMeta(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetMeta(id, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetData(id, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetMeta(id, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetData(id, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetMetas(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetDataList(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetMetaList(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertMultipleMetas(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertMultipleData(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateMeta(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateData(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@CheckIfExistsMeta(id, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  override def shardResolver: ShardRegion.ExtractShardId = {
    case CreateData(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case CreateMeta(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetMeta(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetData(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetMeta(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetData(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetMetas(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetDataList(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetMetaList(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertMultipleMetas(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertMultipleData(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateMeta(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateData(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case CheckIfExistsMeta(id, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }

  override def regionName: String = "metaRegion"

  override def props: Props = Metas.props(Metas.this)
  
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

    def createDataWithActor(data: Data): Future[String] = (actorRegion ? CreateData(uniqueId, data)).mapTo[String]

    def create(data: Data): Future[String] = add(Meta(id = data.value.generateHash, dataType = data.dataType, dataValue = data.value.asString))

    def createMetaWithActor(meta: Meta): Future[String] = (actorRegion ? CreateMeta(uniqueId, meta)).mapTo[String]

    def create(meta: Meta): Future[String] = add(meta)

    def tryGetMetaWithActor(id: String, dataType: String): Future[Meta] = (actorRegion ? TryGetMeta(uniqueId, id, dataType)).mapTo[Meta]

    def tryGet(id: String, dataType: String): Future[Meta] = tryGetByIDAndDataType(id = id, dataType = dataType)

    def tryGetDataWithActor(id: String, dataType: String): Future[Data] = (actorRegion ? TryGetData(uniqueId, id, dataType)).mapTo[Data]

    def tryGetData(id: String, dataType: String): Future[Data] = tryGetByIDAndDataType(id = id, dataType = dataType).map(x => DataValue.getData(dataType = x.dataType, dataValue = Option(x.dataValue)))

    def getMetaWithActor(id: String, dataType: String): Future[Option[Meta]] = (actorRegion ? GetMeta(uniqueId, id, dataType)).mapTo[Option[Meta]]

    def get(id: String, dataType: String): Future[Option[Meta]] = getByIDAndDataType(id = id, dataType = dataType)

    def getDataWithActor(id: String, dataType: String): Future[Option[Data]] = (actorRegion ? GetData(uniqueId, id, dataType)).mapTo[Option[Data]]

    def getData(id: String, dataType: String): Future[Option[Data]] = getByIDAndDataType(id = id, dataType = dataType).map(metaOption => metaOption.fold[Option[Data]](None)(x => Option(DataValue.getData(dataType = x.dataType, dataValue = Option(x.dataValue)))))

    def getMetasWithActor(ids: Seq[String]): Future[Seq[Meta]] = (actorRegion ? GetMetas(uniqueId, ids)).mapTo[Seq[Meta]]

    def get(ids: Seq[String]): Future[Seq[Meta]] = getByIDs(ids.filter(x => x != ""))

    def getDataListWithActor(ids: Seq[String]): Future[Seq[Data]] = (actorRegion ? GetDataList(uniqueId, ids)).mapTo[Seq[Data]]

    def getDataList(ids: Seq[String]): Future[Seq[Data]] = getByIDs(ids.filter(x => x != "")).map(_.map(meta => DataValue.getData(dataType = meta.dataType, dataValue = Option(meta.dataValue))))

    def getListWithActor(ids: Seq[String]): Future[Seq[Meta]] = (actorRegion ? GetMetaList(uniqueId, ids)).mapTo[Seq[Meta]]

    def getList(ids: Seq[String]): Future[Seq[Meta]] = getByIDs(ids.filter(x => x != ""))

    def insertMultipleMetasWithActor(metaList: Seq[Meta]): Future[Seq[String]] = (actorRegion ? InsertMultipleMetas(uniqueId, metaList)).mapTo[Seq[String]]

    def insertMultiple(metaList: Seq[Meta]): Future[Seq[String]] = addMultiple(metaList)

    def insertMultipleDataWithActor(dataList: Seq[Data]): Future[Seq[String]] = (actorRegion ? InsertMultipleData(uniqueId, dataList)).mapTo[Seq[String]]

    def insertMultipleData(dataList: Seq[Data]): Future[Seq[String]] = addMultiple(dataList.map(x => Meta(id = x.value.generateHash, dataType = x.dataType, dataValue = x.value.asString)))

    def insertOrUpdateMetaWithActor(meta: Meta): Future[Int] = (actorRegion ? InsertOrUpdateMeta(uniqueId, meta)).mapTo[Int]

    def insertOrUpdate(meta: Meta): Future[Int] = upsert(meta)

    def insertOrUpdateDataWithActor(data: Data): Future[Int] = (actorRegion ? InsertOrUpdateData(uniqueId, data)).mapTo[Int]

    def insertOrUpdate(data: Data): Future[Int] = upsert(Meta(id = data.value.generateHash, dataType = data.dataType, dataValue = data.value.asString))

    def checkIfExistsMetaWithActor(id: String, dataType: String): Future[Boolean] = (actorRegion ? CheckIfExistsMeta(uniqueId, id, dataType)).mapTo[Boolean]

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

object Metas {
  def props(blockchainMetas: models.blockchain.Metas) (implicit executionContext: ExecutionContext) = Props(new MetaActor(blockchainMetas))

  @Singleton
  class MetaActor @Inject()(
                             blockchainMetas: models.blockchain.Metas
                           ) (implicit executionContext: ExecutionContext) extends Actor with ActorLogging {
    private implicit val logger: Logger = Logger(this.getClass)

    override def receive: Receive = {
      case CreateMeta(_, meta) => {
        blockchainMetas.Service.create(meta) pipeTo sender()
      }
      case CreateData(_, data) => {
        blockchainMetas.Service.create(data) pipeTo sender()
      }
      case TryGetMeta(_, id, dataType) => {
        blockchainMetas.Service.tryGet(id, dataType) pipeTo sender()
      }
      case TryGetData(_, id, dataType) => {
        blockchainMetas.Service.tryGetData(id, dataType) pipeTo sender()
      }
      case GetMeta(_, id, dataType) => {
        blockchainMetas.Service.get(id, dataType) pipeTo sender()
      }
      case GetData(_, id, dataType) => {
        blockchainMetas.Service.getData(id, dataType) pipeTo sender()
      }
      case GetMetas(_, ids) => {
        blockchainMetas.Service.get(ids) pipeTo sender()
      }
      case GetDataList(_, ids) => {
        blockchainMetas.Service.getDataList(ids) pipeTo sender()
      }
      case GetMetaList(_, ids) => {
        blockchainMetas.Service.getList(ids) pipeTo sender()
      }
      case InsertMultipleMetas(_, metaList) => {
        blockchainMetas.Service.insertMultiple(metaList) pipeTo sender()
      }
      case InsertMultipleData(_, dataList) => {
        blockchainMetas.Service.insertMultipleData(dataList) pipeTo sender()
      }
      case InsertOrUpdateMeta(_, meta) => {
        blockchainMetas.Service.insertOrUpdate(meta) pipeTo sender()
      }
      case InsertOrUpdateData(_, data) => {
        blockchainMetas.Service.insertOrUpdate(data) pipeTo sender()
      }
      case CheckIfExistsMeta(_, id, dataType) => {
        blockchainMetas.Service.checkIfExists(id, dataType) pipeTo sender()
      }
    }
  }

  case class CreateData(uid: String, data: Data)
  case class CreateMeta(uid: String, meta: Meta)
  case class TryGetMeta(uid: String, id: String, dataType: String)
  case class TryGetData(uid: String, id: String, dataType: String)
  case class GetMeta(uid: String, id: String, dataType: String)
  case class GetData(uid: String, id: String, dataType: String)
  case class GetMetas(uid: String, ids: Seq[String])
  case class GetDataList(uid: String, ids: Seq[String])
  case class GetMetaList(uid: String, ids: Seq[String])
  case class InsertMultipleMetas(uid: String, metaList: Seq[Meta])
  case class InsertMultipleData(uid: String, dataList: Seq[Data])
  case class InsertOrUpdateMeta(uid: String, meta: Meta)
  case class InsertOrUpdateData(uid: String, data: Data)
  case class CheckIfExistsMeta(uid: String, id: String, dataType: String)
}