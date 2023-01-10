package models.blockchain

import modelTraits.{Entity, GenericDaoImpl, Logging, ModelTable}
import org.slf4j.{Logger, LoggerFactory}
import play.api.db.slick.DatabaseConfigProvider
import schema.data.{Data => abstractData}
import slick.jdbc.H2Profile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class Classification(id: Array[Byte], immutables: Array[Byte], mutables: Array[Byte], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity[Array[Byte]]

object Classifications {

  implicit val module: String = constants.Module.BLOCKCHAIN_CLASSIFICATION

  implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  class DataTable(tag: Tag) extends Table[Classification](tag, "Classification") with ModelTable[Array[Byte]] {

    def * = (id, immutables, mutables, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (Classification.tupled, Classification.unapply)

    def id = column[Array[Byte]]("id", O.PrimaryKey)

    def immutables = column[Array[Byte]]("immutables")

    def mutables = column[Array[Byte]]("mutables")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")

  }

  val TableQuery = new TableQuery(tag => new DataTable(tag))

}

@Singleton
class Classifications @Inject()(
                                 protected val databaseConfigProvider: DatabaseConfigProvider
                               )(implicit override val executionContext: ExecutionContext)
  extends GenericDaoImpl[Classifications.DataTable, Classification, Array[Byte]](
    databaseConfigProvider,
    Classifications.TableQuery,
    executionContext,
    Classifications.module,
    Classifications.logger
  ) {

  object Service {

    def add(classification: Classification): Future[String] = create(classification).map(x => commonUtilities.Secrets.base64URLEncoder(x))

    def get(id: String): Future[Option[Classification]] = getById(commonUtilities.Secrets.base64URLDecode(id))

    def get(id: Array[Byte]): Future[Option[Classification]] = getById(id)

    def tryGet(id: String): Future[Classification] = tryGetById(commonUtilities.Secrets.base64URLDecode(id))

    def tryGet(id: Array[Byte]): Future[Classification] = tryGetById(id)


  }

  object Utility {

    def onDefineAsset(msg: com.assets.transactions.define.Message): Future[String] = {
      val add = Service.add()

      for {
        _ <- add
      } yield msg.getFrom
    }

  }
}