package models.blockchain

import models.traits.{Entity, GenericDaoImpl, Logging, ModelTable}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import schema.data.Data
import schema.data.base.{AccAddressData, IDData, ListData}
import schema.document.Document
import schema.id.base._
import schema.list.PropertyList
import schema.property.Property
import schema.property.base.MetaProperty
import schema.qualified.{Immutables, Mutables}
import slick.jdbc.H2Profile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class Identity(id: Array[Byte], classificationID: Array[Byte], immutables: Array[Byte], mutables: Array[Byte], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity[Array[Byte]] {

  def getIDString: String = utilities.Secrets.base64URLEncoder(this.id)

  def getID: IdentityID = IdentityID(HashID(this.id))

  def getClassificationIDString: String = utilities.Secrets.base64URLEncoder(this.classificationID)

  def getClassificationID: ClassificationID = ClassificationID(this.classificationID)

  def getImmutables: Immutables = Immutables(this.immutables)

  def getMutables: Mutables = Mutables(this.mutables)

  def getDocument: Document = Document(this.getClassificationID, this.getImmutables, this.getMutables)

  def getProperty(id: PropertyID): Option[Property] = this.getDocument.getProperty(id)

  def getAuthentication: ListData = {
    val property = this.getProperty(constants.Blockchain.AuthenticationProperty.getID)
    if (property.isDefined) {
      if (property.get.isMeta) ListData(MetaProperty(property.get.getProtoBytes).getData.toAnyData.getListData)
      else ListData(Seq(constants.Blockchain.AuthenticationProperty.getData.toAnyData))
    } else ListData(Seq())
  }

  def getAuthenticationAddress: Seq[String] = this.getAuthentication.getAnyDataList.map(x => Data(x).viewString)

  def mutate(properties: Seq[Property]): Identity = this.copy(mutables = this.getMutables.mutate(properties).getProtoBytes)

}

object Identities {

  implicit val module: String = constants.Module.BLOCKCHAIN_IDENTITY

  implicit val logger: Logger = Logger(this.getClass)

  class DataTable(tag: Tag) extends Table[Identity](tag, "Identity") with ModelTable[Array[Byte]] {

    def * = (id, classificationID, immutables, mutables, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (Identity.tupled, Identity.unapply)

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
class Identities @Inject()(
                            blockchainMaintainers: Maintainers,
                            protected val databaseConfigProvider: DatabaseConfigProvider
                          )(implicit override val executionContext: ExecutionContext)
  extends GenericDaoImpl[Identities.DataTable, Identity, Array[Byte]](
    databaseConfigProvider,
    Identities.TableQuery,
    executionContext,
    Identities.module,
    Identities.logger
  ) {

  object Service {

    def add(identity: Identity): Future[String] = create(identity).map(x => utilities.Secrets.base64URLEncoder(x))

    def insertOrUpdate(identity: Identity): Future[Unit] = upsert(identity)

    def get(id: String): Future[Option[Identity]] = getById(utilities.Secrets.base64URLDecode(id))

    def get(id: IdentityID): Future[Option[Identity]] = getById(id.getBytes)

    def get(id: Array[Byte]): Future[Option[Identity]] = getById(id)

    def fetchAll: Future[Seq[Identity]] = getAll

    def tryGet(id: String): Future[Identity] = tryGetById(utilities.Secrets.base64URLDecode(id))

    def tryGet(id: IdentityID): Future[Identity] = tryGetById(id.getBytes)

    def delete(id: IdentityID): Future[Int] = deleteById(id.getBytes)

    def update(identity: Identity): Future[Unit] = updateById(identity)


  }

  object Utility {

    def onNub(msg: com.identities.transactions.nub.Message): Future[String] = {
      val immutables = Immutables(PropertyList(Seq(constants.Blockchain.NubProperty.copy(data = IDData(StringID(msg.getNubID).toAnyID).toAnyData))))
      val mutables = Mutables(PropertyList(Seq(constants.Blockchain.AuthenticationProperty.copy(data = ListData(Seq(AccAddressData(utilities.Crypto.convertAddressToAccAddressBytes(msg.getFrom)).toAnyData)).toAnyData))))
      val identityID = utilities.ID.getIdentityID(classificationID = constants.Blockchain.NubClassificationID, immutables = immutables)
      val identity = Identity(id = identityID.getBytes, classificationID = constants.Blockchain.NubClassificationID.getBytes, immutables = immutables.getProtoBytes, mutables = mutables.getProtoBytes)
      val add = Service.add(identity)

      for {
        _ <- add
      } yield msg.getFrom
    }

    def onIssue(msg: com.identities.transactions.issue.Message): Future[String] = {
      val immutables = Immutables(PropertyList(PropertyList(msg.getImmutableMetaProperties).propertyList ++ PropertyList(msg.getImmutableProperties).propertyList))
      val classificationID = ClassificationID(msg.getClassificationID)
      val identityID = utilities.ID.getIdentityID(classificationID = classificationID, immutables = immutables)
      val authenticationProperty = constants.Blockchain.AuthenticationProperty.copy(data = ListData(Seq(AccAddressData(utilities.Crypto.convertAddressToAccAddressBytes(msg.getTo)).toAnyData)).toAnyData)
      val mutables = Mutables(PropertyList(PropertyList(msg.getMutableMetaProperties).propertyList ++ Seq(authenticationProperty) ++ PropertyList(msg.getMutableProperties).propertyList))
      val identity = Identity(id = identityID.getBytes, classificationID = ClassificationID(msg.getClassificationID).getBytes, immutables = immutables.getProtoBytes, mutables = mutables.getProtoBytes)
      val add = Service.add(identity)

      for {
        _ <- add
      } yield msg.getFrom
    }

    def onMutate(msg: com.identities.transactions.mutate.Message): Future[String] = {
      val identityID = IdentityID(msg.getIdentityID)
      val mutables = Mutables(PropertyList(PropertyList(msg.getMutableMetaProperties).propertyList ++ PropertyList(msg.getMutableProperties).propertyList))
      val identity = Service.tryGet(identityID)

      def update(identity: Identity) = Service.update(identity.mutate(mutables.getProperties))

      for {
        identity <- identity
        _ <- update(identity)
      } yield msg.getFrom
    }

    def onProvision(msg: com.identities.transactions.provision.Message): Future[String] = {
      val identity = Service.tryGet(IdentityID(msg.getIdentityID))

      def update(identity: Identity) = {
        val updatedList = ListData(identity.getAuthentication.dataList :+ AccAddressData(utilities.Crypto.convertAddressToAccAddressBytes(msg.getTo)).toAnyData).toAnyData
        Service.insertOrUpdate(identity.mutate(Seq(MetaProperty(id = constants.Blockchain.AuthenticationProperty.getID, data = updatedList))))
      }

      for {
        identity <- identity
        _ <- update(identity)
      } yield msg.getFrom
    }

    def onRevoke(msg: com.identities.transactions.revoke.Message): Future[String] = {
      val deputize = blockchainMaintainers.Utility.revoke(fromID = IdentityID(msg.getFromID), toID = IdentityID(msg.getToID), maintainedClassificationID = ClassificationID(msg.getClassificationID))
      for {
        _ <- deputize
      } yield msg.getFrom
    }

    def onDeputize(msg: com.identities.transactions.deputize.Message): Future[String] = {
      val deputize = blockchainMaintainers.Utility.deputize(fromID = IdentityID(msg.getFromID), toID = IdentityID(msg.getToID), maintainedClassificationID = ClassificationID(msg.getClassificationID), maintainedProperties = PropertyList(msg.getMaintainedProperties), canMintAsset = msg.getCanMintAsset, canBurnAsset = msg.getCanBurnAsset, canRenumerateAsset = msg.getCanRenumerateAsset, canAddMaintainer = msg.getCanAddMaintainer, canRemoveMaintainer = msg.getCanRemoveMaintainer, canMutateMaintainer = msg.getCanMutateMaintainer)
      for {
        _ <- deputize
      } yield msg.getFrom
    }

    def onQuash(msg: com.identities.transactions.quash.Message): Future[String] = {
      val delete = Service.delete(IdentityID(msg.getIdentityID))
      for {
        _ <- delete
      } yield msg.getFrom
    }

    def onUnprovision(msg: com.identities.transactions.unprovision.Message): Future[String] = {
      val identity = Service.tryGet(IdentityID(msg.getIdentityID))

      def update(identity: Identity) = {
        val updatedList = ListData(identity.getAuthentication.dataList.filterNot(x => AccAddressData(x.getAccAddressData).getBytes.sameElements(AccAddressData(utilities.Crypto.convertAddressToAccAddressBytes(msg.getTo)).getBytes))).toAnyData
        Service.insertOrUpdate(identity.mutate(Seq(MetaProperty(id = constants.Blockchain.AuthenticationProperty.getID, data = updatedList))))
      }

      for {
        identity <- identity
        _ <- update(identity)
      } yield msg.getFrom
    }

  }
}