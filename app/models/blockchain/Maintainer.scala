package models.blockchain

import models.traits.{Entity, GenericDaoImpl, Logging, ModelTable}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import schema.document.Document
import schema.id.base.{ClassificationID, PropertyID}
import schema.list.PropertyList
import schema.property.Property
import schema.qualified.{Immutables, Mutables}
import slick.jdbc.H2Profile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class Maintainer(id: Array[Byte], classificationID: Array[Byte], immutables: Array[Byte], mutables: Array[Byte], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity[Array[Byte]] {

  def getID: String = utilities.Secrets.base64URLEncoder(this.id)

  def getClassificationIDString: String = utilities.Secrets.base64URLEncoder(this.classificationID)

  def getClassificationID: ClassificationID = ClassificationID(this.classificationID)

  def getImmutables: Immutables = Immutables(this.immutables)

  def getMutables: Mutables = Mutables(this.mutables)

  def getDocument: Document = Document(this.getClassificationID, this.getImmutables, this.getMutables)

  def getProperty(id: PropertyID): Option[Property] = this.getDocument.getProperty(id)

  def mutate(properties: Seq[Property]): Maintainer = this.copy(mutables = Mutables(PropertyList(this.getMutables.mutate(properties))).getProtoBytes)
}

object Maintainers {

  implicit val module: String = constants.Module.BLOCKCHAIN_MAINTAINER

  implicit val logger: Logger = Logger(this.getClass)

  class DataTable(tag: Tag) extends Table[Maintainer](tag, "Maintainer") with ModelTable[Array[Byte]] {

    def * = (id, classificationID, immutables, mutables, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (Maintainer.tupled, Maintainer.unapply)

    def id = column[Array[Byte]]("id", O.PrimaryKey)

    def classificationID = column[Array[Byte]]("classificationID")

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
class Maintainers @Inject()(
                        protected val databaseConfigProvider: DatabaseConfigProvider
                      )(implicit override val executionContext: ExecutionContext)
  extends GenericDaoImpl[Maintainers.DataTable, Maintainer, Array[Byte]](
    databaseConfigProvider,
    Maintainers.TableQuery,
    executionContext,
    Maintainers.module,
    Maintainers.logger
  ) {

  object Service {

    def add(maintainer: Maintainer): Future[String] = create(maintainer).map(x => utilities.Secrets.base64URLEncoder(x))

    def get(id: String): Future[Option[Maintainer]] = getById(utilities.Secrets.base64URLDecode(id))

    def get(id: Array[Byte]): Future[Option[Maintainer]] = getById(id)

    def tryGet(id: String): Future[Maintainer] = tryGetById(utilities.Secrets.base64URLDecode(id))

    def tryGet(id: Array[Byte]): Future[Maintainer] = tryGetById(id)

    def fetchAll: Future[Seq[Maintainer]] = getAll


  }

  object Utility {


  }
}