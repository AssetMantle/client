package models.blockchain

import models.traits.{Entity, GenericDaoImpl, Logging, ModelTable}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import schema.id.base.{ClassificationID, HashID, IdentityID}
import schema.list.PropertyList
import schema.qualified.{Immutables, Mutables}
import slick.jdbc.H2Profile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class Classification(id: Array[Byte], immutables: Array[Byte], mutables: Array[Byte], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity[Array[Byte]] {

  def getIDString: String = utilities.Secrets.base64URLEncoder(this.id)

  def getID: ClassificationID = ClassificationID(HashID(this.id))

  def getImmutables: Immutables = Immutables(this.immutables)

  def getMutables: Mutables = Mutables(this.mutables)

}

object Classifications {

  implicit val module: String = constants.Module.BLOCKCHAIN_CLASSIFICATION

  implicit val logger: Logger = Logger(this.getClass)

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
                                 blockchainMaintainers: Maintainers,
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

    def add(classification: Classification): Future[String] = create(classification).map(x => utilities.Secrets.base64URLEncoder(x))

    def insertOrUpdate(classification: Classification): Future[Unit] = upsert(classification)

    def get(id: String): Future[Option[Classification]] = getById(utilities.Secrets.base64URLDecode(id))

    def get(id: Array[Byte]): Future[Option[Classification]] = getById(id)

    def tryGet(id: String): Future[Classification] = tryGetById(utilities.Secrets.base64URLDecode(id))

    def tryGet(id: Array[Byte]): Future[Classification] = tryGetById(id)

    def fetchAll: Future[Seq[Classification]] = getAll


  }

  object Utility {

    def onDefineAsset(msg: com.assets.transactions.define.Message): Future[String] = {
      val immutables = Immutables(PropertyList(PropertyList(msg.getImmutableMetaProperties).propertyList ++ PropertyList(msg.getImmutableProperties).propertyList))
      val mutables = Mutables(PropertyList(PropertyList(msg.getMutableMetaProperties).propertyList ++ PropertyList(msg.getMutableProperties).propertyList))
      val classificationID = utilities.ID.getClassificationID(immutables = immutables, mutables = mutables)
      val classification = Classification(classificationID.getBytes, immutables.asProtoImmutables.toByteString.toByteArray, mutables.asProtoMutables.toByteString.toByteArray)
      val add = Service.add(classification)

      def addMaintainer(): Future[String] = blockchainMaintainers.Utility.superAuxiliary(classificationID, IdentityID(msg.getFromID), mutables)

      for {
        _ <- add
        _ <- addMaintainer()
      } yield msg.getFrom
    }

    def onDefineIdentity(msg: com.identities.transactions.define.Message): Future[String] = {
      val immutables = Immutables(PropertyList(PropertyList(msg.getImmutableMetaProperties).propertyList ++ PropertyList(msg.getImmutableProperties).propertyList))
      val mutables = Mutables(PropertyList(PropertyList(msg.getMutableMetaProperties).propertyList ++ PropertyList(msg.getMutableProperties).propertyList))
      val classificationID = utilities.ID.getClassificationID(immutables = immutables, mutables = mutables)
      val classification = Classification(classificationID.getBytes, immutables.asProtoImmutables.toByteString.toByteArray, mutables.asProtoMutables.toByteString.toByteArray)
      val add = Service.add(classification)
      def addMaintainer(): Future[String] = blockchainMaintainers.Utility.superAuxiliary(classificationID, IdentityID(msg.getFromID), mutables)

      for {
        _ <- add
        _ <- addMaintainer()
      } yield msg.getFrom
    }

    def onDefineOrder(msg: com.orders.transactions.define.Message): Future[String] = {
      val immutables = Immutables(PropertyList(PropertyList(msg.getImmutableMetaProperties).propertyList ++ PropertyList(msg.getImmutableProperties).propertyList))
      val mutables = Mutables(PropertyList(PropertyList(msg.getMutableMetaProperties).propertyList ++ PropertyList(msg.getMutableProperties).propertyList))
      val classificationID = utilities.ID.getClassificationID(immutables = immutables, mutables = mutables)
      val classification = Classification(classificationID.getBytes, immutables.asProtoImmutables.toByteString.toByteArray, mutables.asProtoMutables.toByteString.toByteArray)
      val add = Service.add(classification)
      def addMaintainer(): Future[String] = blockchainMaintainers.Utility.superAuxiliary(classificationID, IdentityID(msg.getFromID), mutables)

      for {
        _ <- add
        _ <- addMaintainer()
      } yield msg.getFrom
    }


  }
}