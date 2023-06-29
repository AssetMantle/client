package models.blockchain

import models.traits._
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import schema.data.Data
import schema.id.base.DataID
import slick.jdbc.H2Profile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

// TODO rename dataBytes to dataProtoBytes
case class Meta(dataTypeID: String, dataHashID: Array[Byte], dataHashIDString: String, dataBytes: Array[Byte], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity2[String, Array[Byte]] {

  lazy val data: Data = Data(this.dataBytes)

  def id1: String = this.dataTypeID

  def id2: Array[Byte] = this.dataHashID

}

private[blockchain] object Metas {

  class MetaTable(tag: Tag) extends Table[Meta](tag, "Meta") with ModelTable2[String, Array[Byte]] {

    def * = (dataTypeID, dataHashID, dataHashIDString, dataBytes, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (Meta.tupled, Meta.unapply)

    def dataTypeID = column[String]("dataTypeID", O.PrimaryKey)

    def dataHashID = column[Array[Byte]]("dataHashID", O.PrimaryKey)

    def dataHashIDString = column[String]("dataHashIDString")

    def dataBytes = column[Array[Byte]]("dataBytes")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")

    def id1 = dataTypeID

    def id2 = dataHashID

  }
}

@Singleton
class Metas @Inject()(
                       protected val dbConfigProvider: DatabaseConfigProvider,
                     )(implicit val executionContext: ExecutionContext)
  extends GenericDaoImpl2[Metas.MetaTable, Meta, String, Array[Byte]]() {

  implicit val module: String = constants.Module.BLOCKCHAIN_META

  implicit val logger: Logger = Logger(this.getClass)

  val tableQuery = new TableQuery(tag => new Metas.MetaTable(tag))

  object Service {

    def add(data: Data): Future[Array[Byte]] = create(Meta(dataTypeID = data.getType.value, dataHashID = data.generateHashID.getBytes, dataHashIDString = data.generateHashID.asString, dataBytes = data.getProtoBytes)).map(_.dataHashID)

    def add(datas: Seq[Data]): Future[Int] = create(datas.map(x => Meta(dataTypeID = x.getType.value, dataHashID = x.generateHashID.getBytes, dataHashIDString = x.generateHashID.asString, dataBytes = x.getProtoBytes)))

    def get(id: DataID): Future[Option[Meta]] = getById(id1 = id.typeID.value, id2 = id.hashID.getBytes)

    def tryGet(id: DataID): Future[Meta] = tryGetById1AndId2(id1 = id.typeID.value, id2 = id.hashID.getBytes)

  }

  object Utility {

    def onRevealMeta(msg: com.assetmantle.modules.metas.transactions.reveal.Message): Future[String] = {
      val add = Service.add(Data(msg.getData))

      for {
        _ <- add
      } yield msg.getFrom
    }

  }
}