package models.blockchain

import models.common.Parameters.ClassificationParameter
import models.traits.{Entity, GenericDaoImpl, Logging, ModelTable}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import schema.data.base.NumberData
import schema.document.Document
import schema.id.base.{ClassificationID, HashID, PropertyID}
import schema.list.PropertyList
import schema.property.Property
import schema.property.base.MetaProperty
import schema.qualified.{Immutables, Mutables}
import slick.jdbc.H2Profile.api._
import utilities.MicroNumber

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class Classification(id: Array[Byte], idString: String, immutables: Array[Byte], mutables: Array[Byte], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity[Array[Byte]] {

  def getIDString: String = utilities.Secrets.base64URLEncoder(this.id)

  def getID: ClassificationID = ClassificationID(HashID(this.id))

  def getImmutables: Immutables = Immutables(this.immutables)

  def getMutables: Mutables = Mutables(this.mutables)

  def getDocument: Document = Document(this.getID, this.getImmutables, this.getMutables)

  def getProperty(id: PropertyID): Option[Property] = this.getDocument.getProperty(id)

  def getBondAmount: MicroNumber = {
    val property = this.getProperty(schema.constants.Properties.BondAmountProperty.getID)
    MicroNumber((if (property.isDefined && property.get.isMeta) NumberData(MetaProperty(property.get.getProtoBytes).getData.getProtoBytes) else NumberData(0)).value)
  }

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
                                 blockchainTokens: Tokens,
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

    def define(definer: String, mutables: Mutables, immutables: Immutables): Future[ClassificationID] = {
      val updateBalance = blockchainBalances.Utility.insertOrUpdateBalance(definer)
      val classificationParameter = blockchainParameters.Service.tryGetClassificationParameter
      val totalWeight = mutables.getTotalBondWeight + immutables.getTotalBondWeight

      def add(classificationParameter: ClassificationParameter) = {
        val updatedImmutables = Immutables(PropertyList(immutables.propertyList.properties ++ Seq(schema.constants.Properties.BondAmountProperty.copy(data = NumberData(totalWeight * classificationParameter.bondRate)))))
        val classificationID = utilities.ID.getClassificationID(immutables = updatedImmutables, mutables = mutables)
        val classification = Classification(classificationID.getBytes, idString = classificationID.asString, immutables = updatedImmutables.asProtoImmutables.toByteString.toByteArray, mutables = mutables.asProtoMutables.toByteString.toByteArray)
        Service.add(classification)
      }

      for {
        classificationParameter <- classificationParameter
        classificationIDBytes <- add(classificationParameter)
        _ <- updateBalance
      } yield ClassificationID(HashID(classificationIDBytes))
    }

    def bondAuxiliary(address: String, classificationID: ClassificationID): Future[Unit] = {
      val addressBalance = blockchainBalances.Utility.insertOrUpdateBalance(address)
      val classification = tryGetById(classificationID.getBytes)

      def locked(classification: Classification) = blockchainTokens.Service.addTotalLocked(denom = constants.Blockchain.StakingDenom, locked = classification.getBondAmount)

      for {
        _ <- addressBalance
        classification <- classification
        _ <- locked(classification)
      } yield ()
    }

    def unbondAuxiliary(address: String, classificationID: ClassificationID): Future[Unit] = {
      val addressBalance = blockchainBalances.Utility.insertOrUpdateBalance(address)
      val classification = tryGetById(classificationID.getBytes)

      def unlocked(classification: Classification) = blockchainTokens.Service.subtractTotalLocked(denom = constants.Blockchain.StakingDenom, locked = classification.getBondAmount)

      for {
        _ <- addressBalance
        classification <- classification
        _ <- unlocked(classification)
      } yield ()
    }

    def burnAuxiliary(classificationID: ClassificationID): Future[Unit] = {
      val classification = tryGetById(classificationID.getBytes)

      def burn(classification: Classification) = blockchainTokens.Service.addTotalBurnt(denom = constants.Blockchain.StakingDenom, burnt = classification.getBondAmount)

      for {
        classification <- classification
        _ <- burn(classification)
      } yield ()

    }
  }
}