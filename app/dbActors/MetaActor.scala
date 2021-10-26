package dbActors

import actors.Service.actorSystem.dispatcher
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import models.Abstract.PublicKey
import models.blockchain.{Account, Balance, Block, Meta}
import models.common.Serializable.{Coin, Data}
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

object MetaActor {
  def props(blockchainMeta: models.blockchain.Metas) = Props(new MetaActor(blockchainMeta))
}

@Singleton
class MetaActor @Inject()(
                               blockchainMeta: models.blockchain.Metas
                             )extends Actor with ActorLogging {
  private implicit val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case CreateMeta(meta) => {
      blockchainMeta.Service.create(meta) pipeTo sender()
      println(self.path)
    }
    case CreateData(data) => {
      blockchainMeta.Service.create(data) pipeTo sender()
      println(self.path)
    }
    case TryGetMeta(id, dataType) => {
      blockchainMeta.Service.tryGet(id, dataType) pipeTo sender()
      println(self.path)
    }
    case TryGetData(id, dataType) => {
      blockchainMeta.Service.tryGetData(id, dataType) pipeTo sender()
      println(self.path)
    }
    case GetMeta(id, dataType) => {
      blockchainMeta.Service.get(id, dataType) pipeTo sender()
      println(self.path)
    }
    case GetData(id, dataType) => {
      blockchainMeta.Service.getData(id, dataType) pipeTo sender()
      println(self.path)
    }
    case GetMetas(ids) => {
      blockchainMeta.Service.get(ids) pipeTo sender()
      println(self.path)
    }
    case GetDataList(ids) => {
      blockchainMeta.Service.getDataList(ids) pipeTo sender()
      println(self.path)
    }
    case GetMetaList(ids) => {
      blockchainMeta.Service.getList(ids) pipeTo sender()
      println(self.path)
    }
    case InsertMultipleMetas(metaList) => {
      blockchainMeta.Service.insertMultiple(metaList) pipeTo sender()
      println(self.path)
    }
    case InsertMultipleData(dataList) => {
      blockchainMeta.Service.insertMultipleData(dataList) pipeTo sender()
      println(self.path)
    }
    case InsertOrUpdateMeta(meta) => {
      blockchainMeta.Service.insertOrUpdate(meta) pipeTo sender()
      println(self.path)
    }
    case InsertOrUpdateData(data) => {
      blockchainMeta.Service.insertOrUpdate(data) pipeTo sender()
      println(self.path)
    }
    case CheckIfExistsMeta(id, dataType) => {
      blockchainMeta.Service.checkIfExists(id, dataType) pipeTo sender()
      println(self.path)
    }
  }

}

case class CreateData(data: Data)
case class CreateMeta(meta: Meta)
case class TryGetMeta(id: String, dataType: String)
case class TryGetData(id: String, dataType: String)
case class GetMeta(id: String, dataType: String)
case class GetData(id: String, dataType: String)
case class GetMetas(ids: Seq[String])
case class GetDataList(ids: Seq[String])
case class GetMetaList(ids: Seq[String])
case class InsertMultipleMetas(metaList: Seq[Meta])
case class InsertMultipleData(dataList: Seq[Data])
case class InsertOrUpdateMeta(meta: Meta)
case class InsertOrUpdateData(data: Data)
case class CheckIfExistsMeta(id: String, dataType: String)