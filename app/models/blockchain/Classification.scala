package models.blockchain

import models.common.Parameters.ClassificationParameter
import models.traits.{Entity, GenericDaoImpl, Logging, ModelTable}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import schema.data.base.NumberData
import schema.document.Document
import schema.id.base.{ClassificationID, HashID, IdentityID}
import schema.list.PropertyList
import schema.qualified.{Immutables, Mutables}
import slick.jdbc.H2Profile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class Classification(id: Array[Byte], idString: String, immutables: Array[Byte], mutables: Array[Byte], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity[Array[Byte]] {

  def getIDString: String = utilities.Secrets.base64URLEncoder(this.id)

  def getID: ClassificationID = ClassificationID(HashID(this.id))

  def getImmutables: Immutables = Immutables(this.immutables)

  def getMutables: Mutables = Mutables(this.mutables)

  def getDocument: Document = Document(this.getID, this.getImmutables, this.getMutables)

}

object Classifications {

  implicit val module: String = constants.Module.BLOCKCHAIN_CLASSIFICATION

  implicit val logger: Logger = Logger(this.getClass)

  class DataTable(tag: Tag) extends Table[Classification](tag, "Classification") with ModelTable[Array[Byte]] {

    def * = (id, idString, immutables, mutables, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (Classification.tupled, Classification.unapply)

    def id = column[Array[Byte]]("id", O.PrimaryKey)

    def idString = column[String]("idString")

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
                                 blockchainBalances: Balances,
                                 blockchainParameters: models.blockchain.Parameters,
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

    def add(classification: Classification): Future[Array[Byte]] = create(classification)

    def add(classifications: Seq[Classification]): Future[Unit] = create(classifications)

    def insertOrUpdate(classification: Classification): Future[Unit] = upsert(classification)

    def get(id: String): Future[Option[Classification]] = getById(utilities.Secrets.base64URLDecode(id))

    def get(id: Array[Byte]): Future[Option[Classification]] = getById(id)

    def tryGet(id: String): Future[Classification] = tryGetById(utilities.Secrets.base64URLDecode(id))

    def tryGet(id: Array[Byte]): Future[Classification] = tryGetById(id)

    def fetchAll: Future[Seq[Classification]] = getAll


  }

  object Utility {

    def onDefineAsset(msg: com.assets.transactions.define.Message): Future[String] = {
      val immutables = Immutables(PropertyList(msg.getImmutableMetaProperties).add(PropertyList(msg.getImmutableProperties).propertyList))
      val mutables = Mutables(PropertyList(msg.getMutableMetaProperties).add(PropertyList(msg.getMutableProperties).propertyList))
      val add = define(mutables, immutables)

      def addMaintainer(classificationID: ClassificationID): Future[String] = blockchainMaintainers.Utility.superAuxiliary(classificationID, IdentityID(msg.getFromID), mutables)

      for {
        classificationID <- add
        _ <- addMaintainer(classificationID)
      } yield msg.getFrom
    }

    def onDefineIdentity(msg: com.identities.transactions.define.Message): Future[String] = {
      val immutables = Immutables(PropertyList(msg.getImmutableMetaProperties).add(PropertyList(msg.getImmutableProperties).propertyList))
      val mutables = Mutables(PropertyList(msg.getMutableMetaProperties).add(PropertyList(msg.getMutableProperties).add(Seq(constants.Blockchain.AuthenticationProperty)).propertyList))
      val add = define(mutables, immutables)

      def addMaintainer(classificationID: ClassificationID): Future[String] = blockchainMaintainers.Utility.superAuxiliary(classificationID, IdentityID(msg.getFromID), mutables)

      for {
        classificationID <- add
        _ <- addMaintainer(classificationID)
      } yield msg.getFrom
    }

    def onDefineOrder(msg: com.orders.transactions.define.Message): Future[String] = {
      val immutables = Immutables(PropertyList(msg.getImmutableMetaProperties)
        .add(Seq(constants.Blockchain.ExchangeRateProperty, constants.Blockchain.CreationHeightProperty, constants.Blockchain.MakerOwnableIDProperty, constants.Blockchain.TakerOwnableIDProperty, constants.Blockchain.MakerIDProperty, constants.Blockchain.TakerIDProperty))
        .add(PropertyList(msg.getImmutableProperties).propertyList))
      val mutables = Mutables(PropertyList(msg.getMutableMetaProperties)
        .add(PropertyList(msg.getMutableProperties)
          .add(Seq(constants.Blockchain.ExpiryHeightProperty, constants.Blockchain.MakerOwnableSplitProperty)).getProperties))
      val add = define(mutables, immutables)

      def addMaintainer(classificationID: ClassificationID): Future[String] = blockchainMaintainers.Utility.superAuxiliary(classificationID, IdentityID(msg.getFromID), mutables)

      for {
        classificationID <- add
        _ <- addMaintainer(classificationID)
      } yield msg.getFrom
    }

    private def define(mutables: Mutables, immutables: Immutables): Future[ClassificationID] = {
      val classificationParameter = blockchainParameters.Service.tryGetClassificationParameter
      val totalWeight = mutables.propertyList.propertyList.map(_.getBondedWeight).sum + immutables.propertyList.propertyList.map(_.getBondedWeight).sum

      def add(classificationParameter: ClassificationParameter) = {
        val updatedImmutables = Immutables(PropertyList(immutables.propertyList.propertyList ++ Seq(constants.Blockchain.BondAmountProperty.copy(data = NumberData(totalWeight * classificationParameter.getBondRate).toAnyData))))
        val classificationID = utilities.ID.getClassificationID(immutables = updatedImmutables, mutables = mutables)
        val classification = Classification(classificationID.getBytes, idString = classificationID.asString, immutables = updatedImmutables.asProtoImmutables.toByteString.toByteArray, mutables = mutables.asProtoMutables.toByteString.toByteArray)
        Service.add(classification)
      }

      for {
        classificationParameter <- classificationParameter
        classificationIDBytes <- add(classificationParameter)
      } yield ClassificationID(HashID(classificationIDBytes))
    }

    def bondAuxiliary(address: String): Future[Unit] = {
      blockchainBalances.Utility.insertOrUpdateBalance(address)
    }

    def unbondAuxiliary(address: String): Future[Unit] = {
      blockchainBalances.Utility.insertOrUpdateBalance(address)
    }


  }
}