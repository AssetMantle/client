package actors.blockchainModels

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.blockchain.{Meta}
import models.common.Serializable.{Data}
import play.api.Logger
import javax.inject.{Inject, Singleton}
import constants.Actor.{NUMBER_OF_SHARDS, NUMBER_OF_ENTITIES}

object MetaActor {
  def props(blockchainMetas: models.blockchain.Metas) = Props(new MetaActor(blockchainMetas))
  
  val idExtractor: ShardRegion.ExtractEntityId = {
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

  val shardResolver: ShardRegion.ExtractShardId = {
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
}

@Singleton
class MetaActor @Inject()(
                               blockchainMetas: models.blockchain.Metas
                             )extends Actor with ActorLogging {
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