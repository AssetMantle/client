package actors.blockchainModels

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Meta}
import models.common.Serializable.{Coin, Data}
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object MetaActor {
  def props(blockchainMeta: models.blockchain.Metas) = Props(new MetaActor(blockchainMeta))

  val numberOfEntities = 10
  val numberOfShards = 100

  val idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateData(id, _) => (id, attempt)
    case attempt@CreateMeta(id, _) => (id, attempt)
    case attempt@TryGetMeta(id, _, _) => (id, attempt)
    case attempt@TryGetData(id, _, _) => (id, attempt)
    case attempt@GetMeta(id, _, _) => (id, attempt)
    case attempt@GetData(id, _, _) => (id, attempt)
    case attempt@GetMetas(id, _) => (id, attempt)
    case attempt@GetDataList(id, _) => (id, attempt)
    case attempt@GetMetaList(id, _) => (id, attempt)
    case attempt@InsertMultipleMetas(id, _) => (id, attempt)
    case attempt@InsertMultipleData(id, _) => (id, attempt)
    case attempt@InsertOrUpdateMeta(id, _) => (id, attempt)
    case attempt@InsertOrUpdateData(id, _) => (id, attempt)
    case attempt@CheckIfExistsMeta(id, _, _) => (id, attempt)

  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case CreateData(id, _) => (id.hashCode % numberOfShards).toString
    case CreateMeta(id, _) => (id.hashCode % numberOfShards).toString
    case TryGetMeta(id, _, _) => (id.hashCode % numberOfShards).toString
    case TryGetData(id, _, _) => (id.hashCode % numberOfShards).toString
    case GetMeta(id, _, _) => (id.hashCode % numberOfShards).toString
    case GetData(id, _, _) => (id.hashCode % numberOfShards).toString
    case GetMetas(id, _) => (id.hashCode % numberOfShards).toString
    case GetDataList(id, _) => (id.hashCode % numberOfShards).toString
    case GetMetaList(id, _) => (id.hashCode % numberOfShards).toString
    case InsertMultipleMetas(id, _) => (id.hashCode % numberOfShards).toString
    case InsertMultipleData(id, _) => (id.hashCode % numberOfShards).toString
    case InsertOrUpdateMeta(id, _) => (id.hashCode % numberOfShards).toString
    case InsertOrUpdateData(id, _) => (id.hashCode % numberOfShards).toString
    case CheckIfExistsMeta(id, _, _) => (id.hashCode % numberOfShards).toString

  }
}

@Singleton
class MetaActor @Inject()(
                               blockchainMeta: models.blockchain.Metas
                             )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateMeta(_, meta) => {
      blockchainMeta.Service.create(meta) pipeTo sender()
    }
    case CreateData(_, data) => {
      blockchainMeta.Service.create(data) pipeTo sender()
    }
    case TryGetMeta(_, id, dataType) => {
      blockchainMeta.Service.tryGet(id, dataType) pipeTo sender()
    }
    case TryGetData(_, id, dataType) => {
      blockchainMeta.Service.tryGetData(id, dataType) pipeTo sender()
    }
    case GetMeta(_, id, dataType) => {
      blockchainMeta.Service.get(id, dataType) pipeTo sender()
    }
    case GetData(_, id, dataType) => {
      blockchainMeta.Service.getData(id, dataType) pipeTo sender()
    }
    case GetMetas(_, ids) => {
      blockchainMeta.Service.get(ids) pipeTo sender()
    }
    case GetDataList(_, ids) => {
      blockchainMeta.Service.getDataList(ids) pipeTo sender()
    }
    case GetMetaList(_, ids) => {
      blockchainMeta.Service.getList(ids) pipeTo sender()
    }
    case InsertMultipleMetas(_, metaList) => {
      blockchainMeta.Service.insertMultiple(metaList) pipeTo sender()
    }
    case InsertMultipleData(_, dataList) => {
      blockchainMeta.Service.insertMultipleData(dataList) pipeTo sender()
    }
    case InsertOrUpdateMeta(_, meta) => {
      blockchainMeta.Service.insertOrUpdate(meta) pipeTo sender()
    }
    case InsertOrUpdateData(_, data) => {
      blockchainMeta.Service.insertOrUpdate(data) pipeTo sender()
    }
    case CheckIfExistsMeta(_, id, dataType) => {
      blockchainMeta.Service.checkIfExists(id, dataType) pipeTo sender()
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