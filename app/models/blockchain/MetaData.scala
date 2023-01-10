package models.blockchain

import modelTraits.{Entity2, GenericDaoImpl2, Logging, ModelTable2}
import org.slf4j.{Logger, LoggerFactory}
import play.api.db.slick.DatabaseConfigProvider
import schema.data.{Data => abstractData}
import schema.id.base.DataID
import slick.jdbc.H2Profile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class MetaData(dataTypeID: String, dataHashID: Array[Byte], dataBytes: Array[Byte], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity2[String, Array[Byte]] {

  lazy val data: abstractData = abstractData(this.dataTypeID, this.dataBytes)

  def id1: String = this.dataTypeID

  def id2: Array[Byte] = this.dataHashID

}

object MetaDatas {

  implicit val module: String = constants.Module.BLOCKCHAIN_META_DATA

  implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  class DataTable(tag: Tag) extends Table[MetaData](tag, "MetaData") with ModelTable2[String, Array[Byte]] {

    def * = (dataTypeID, dataHashID, dataBytes, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (MetaData.tupled, MetaData.unapply)

    def dataTypeID = column[String]("dataTypeID", O.PrimaryKey)

    def dataHashID = column[Array[Byte]]("dataHashID", O.PrimaryKey)

    def dataBytes = column[Array[Byte]]("dataBytes")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")

    override def id1 = dataTypeID

    override def id2 = dataHashID

  }

  val TableQuery = new TableQuery(tag => new DataTable(tag))

}

@Singleton
class MetaDatas @Inject()(
                           protected val databaseConfigProvider: DatabaseConfigProvider
                         )(implicit override val executionContext: ExecutionContext)
  extends GenericDaoImpl2[MetaDatas.DataTable, MetaData, String, Array[Byte]](
    databaseConfigProvider,
    MetaDatas.TableQuery,
    executionContext,
    MetaDatas.module,
    MetaDatas.logger
  ) {
  object Service {

    def add(data: abstractData): Future[Unit] = create(MetaData(dataTypeID = data.getType.value, dataHashID = data.generateHashID.getBytes, dataBytes = data.getProtoBytes))

    def get(id: DataID): Future[Option[MetaData]] = getById(id1 = id.typeID.value, id2 = id.hashID.getBytes)

    def tryGet(id: DataID): Future[MetaData] = tryGetById1AndId2(id1 = id.typeID.value, id2 = id.hashID.getBytes)

  }

  object Utility {

    def onRevealMeta(msg: com.metas.transactions.reveal.Message): Future[String] = {
      val add = Service.add(abstractData(msg.getData))

      for {
        _ <- add
      } yield msg.getFrom
    }

  }
}