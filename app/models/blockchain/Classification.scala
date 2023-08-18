package models.blockchain

import models.common.Parameters.ClassificationParameter
import models.traits._
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import schema.data.base.NumberData
import schema.document.Document
import schema.id.base.{ClassificationID, HashID, PropertyID}
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

private[blockchain] object Classifications {

  class ClassificationTable(tag: Tag) extends Table[Classification](tag, "Classification") with ModelTable[Array[Byte]] {

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

}

@Singleton
class Classifications @Inject()(
                                 blockchainMaintainers: Maintainers,
                                 blockchainBalances: Balances,
                                 blockchainTokens: Tokens,
                                 blockchainParameters: models.blockchain.Parameters,
                                 protected val dbConfigProvider: DatabaseConfigProvider
                               )(implicit val executionContext: ExecutionContext)
  extends GenericDaoImpl[Classifications.ClassificationTable, Classification, Array[Byte]]() {

  implicit val module: String = constants.Module.BLOCKCHAIN_CLASSIFICATION

  implicit val logger: Logger = Logger(this.getClass)

  val tableQuery = TableQuery[Classifications.ClassificationTable] //new TableQuery(tag => new ClassificationTable(tag))

  object Service {

    def add(classification: Classification): Future[Array[Byte]] = create(classification).map(_.id)

    def add(classifications: Seq[Classification]): Future[Int] = create(classifications)

    def insertOrUpdate(classification: Classification): Future[Int] = upsert(classification)

    def insertOrUpdate(classifications: Seq[Classification]): Future[Int] = upsertMultiple(classifications)

    def get(id: String): Future[Option[Classification]] = getById(utilities.Secrets.base64URLDecode(id))

    def get(id: Array[Byte]): Future[Option[Classification]] = getById(id)

    def tryGet(id: String): Future[Classification] = tryGetById(utilities.Secrets.base64URLDecode(id))

    def tryGet(id: Array[Byte]): Future[Classification] = tryGetById(id)

    def fetchAll: Future[Seq[Classification]] = getAll


  }

  object Utility {

    def defineAuxiliary(address: String, mutables: Mutables, immutables: Immutables): Future[ClassificationID] = {
      val updateBalance = blockchainBalances.Utility.insertOrUpdateBalance(address)
      val classificationParameter = blockchainParameters.Service.tryGetClassificationParameter
      val totalWeight = mutables.getTotalBondWeight + immutables.getTotalBondWeight

      def add(classificationParameter: ClassificationParameter) = {
        val updatedMutables = Mutables(mutables.propertyList.add(Seq(schema.constants.Properties.BondAmountProperty.copy(data = NumberData(totalWeight * classificationParameter.bondRate)))))
        val classificationID = schema.utilities.ID.getClassificationID(immutables = immutables, mutables = updatedMutables)
        val classification = Classification(classificationID.getBytes, idString = classificationID.asString, immutables = immutables.asProtoImmutables.toByteString.toByteArray, mutables = updatedMutables.asProtoMutables.toByteString.toByteArray)
        Service.add(classification)
      }

      for {
        classificationParameter <- classificationParameter
        classificationIDBytes <- add(classificationParameter)
        _ <- updateBalance
      } yield ClassificationID(HashID(classificationIDBytes))
    }

    def bondAuxiliary(address: String, classificationID: ClassificationID, bondAmount: NumberData): Future[Unit] = {
      val addressBalance = blockchainBalances.Utility.insertOrUpdateBalance(address)
      //      val classification = tryGetById(classificationID.getBytes)

      val locked = blockchainTokens.Service.addTotalLocked(denom = constants.Blockchain.StakingDenom, locked = MicroNumber(bondAmount.value))

      for {
        _ <- addressBalance
        //        classification <- classification
        _ <- locked
      } yield ()
    }

    def unbondAuxiliary(address: String, classificationID: ClassificationID, bondAmount: NumberData): Future[Unit] = {
      val addressBalance = blockchainBalances.Utility.insertOrUpdateBalance(address)
      //      val classification = tryGetById(classificationID.getBytes)

      val unlocked = blockchainTokens.Service.subtractTotalLocked(denom = constants.Blockchain.StakingDenom, locked = MicroNumber(bondAmount.value))

      for {
        _ <- addressBalance
        //        classification <- classification
        _ <- unlocked
      } yield ()
    }

    def burnAuxiliary(classificationID: ClassificationID, bondAmount: NumberData): Future[Unit] = {
      //      val classification = tryGetById(classificationID.getBytes)

      val burn = blockchainTokens.Service.addTotalBurnt(denom = constants.Blockchain.StakingDenom, burnt = MicroNumber(bondAmount.value))

      for {
        //        classification <- classification
        _ <- burn
      } yield ()
    }

    def beforeRun: Future[Int] = {
      Service.insertOrUpdate(Seq(
        Classification(schema.document.NameIdentity.DocumentClassificationID.getBytes, idString = schema.document.NameIdentity.DocumentClassificationID.asString, immutables = schema.document.NameIdentity.DocumentImmutables.asProtoImmutables.toByteString.toByteArray, mutables = schema.document.NameIdentity.DocumentMutables.asProtoMutables.toByteString.toByteArray),
        Classification(schema.document.CoinAsset.DocumentClassificationID.getBytes, idString = schema.document.CoinAsset.DocumentClassificationID.asString, immutables = schema.document.CoinAsset.DocumentImmutables.asProtoImmutables.toByteString.toByteArray, mutables = schema.document.CoinAsset.DocumentMutables.asProtoMutables.toByteString.toByteArray),
        Classification(schema.document.Maintainer.DocumentClassificationID.getBytes, idString = schema.document.Maintainer.DocumentClassificationID.asString, immutables = schema.document.Maintainer.DocumentImmutables.asProtoImmutables.toByteString.toByteArray, mutables = schema.document.Maintainer.DocumentMutables.asProtoMutables.toByteString.toByteArray),
        Classification(schema.document.ModuleIdentity.DocumentClassificationID.getBytes, idString = schema.document.ModuleIdentity.DocumentClassificationID.asString, immutables = schema.document.ModuleIdentity.DocumentImmutables.asProtoImmutables.toByteString.toByteArray, mutables = schema.document.ModuleIdentity.DocumentMutables.asProtoMutables.toByteString.toByteArray),
        Classification(schema.document.PutOrder.DocumentClassificationID.getBytes, idString = schema.document.PutOrder.DocumentClassificationID.asString, immutables = schema.document.PutOrder.DocumentImmutables.asProtoImmutables.toByteString.toByteArray, mutables = schema.document.PutOrder.DocumentMutables.asProtoMutables.toByteString.toByteArray),
      ))
    }
  }
}