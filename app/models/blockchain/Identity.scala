package models.blockchain

import com.assetmantle.modules.identities.{transactions => identityTransactions}
import models.traits._
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import schema.data.Data
import schema.data.base.{AccAddressData, IDData, ListData, NumberData}
import schema.document.Document
import schema.id.base._
import schema.list.PropertyList
import schema.property.Property
import schema.property.base.MetaProperty
import schema.qualified.{Immutables, Mutables}
import slick.jdbc.H2Profile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class Identity(id: Array[Byte], idString: String, classificationID: Array[Byte], immutables: Array[Byte], mutables: Array[Byte], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity[Array[Byte]] {

  def getIDString: String = utilities.Secrets.base64URLEncoder(this.id)

  def getID: IdentityID = IdentityID(HashID(this.id))

  def getClassificationIDString: String = utilities.Secrets.base64URLEncoder(this.classificationID)

  def getClassificationID: ClassificationID = ClassificationID(this.classificationID)

  def getImmutables: Immutables = Immutables(this.immutables)

  def getMutables: Mutables = Mutables(this.mutables)

  def getDocument: Document = Document(this.getClassificationID, this.getImmutables, this.getMutables)

  def getProperty(id: PropertyID): Option[Property] = this.getDocument.getProperty(id)

  def getAuthentication: ListData = {
    val property = this.getProperty(schema.constants.Properties.AuthenticationProperty.getID)
    if (property.isDefined) {
      if (property.get.isMeta) ListData(MetaProperty(property.get.getProtoBytes).getData.toAnyData.getListData)
      else ListData(schema.constants.Properties.AuthenticationProperty.getData.getProtoBytes)
    } else ListData(Seq())
  }

  def getBondAmount: NumberData = {
    val value = this.getMutables.getProperty(schema.constants.Properties.BondAmountProperty.getID)
    NumberData((if (value.isDefined) MetaProperty(value.get.getProtoBytes) else schema.constants.Properties.BondAmountProperty).getData.getProtoBytes)
  }

  def getAuthenticationAddress: Seq[String] = this.getAuthentication.getAnyDataList.map(x => Data(x).viewString)

  def mutate(properties: Seq[Property]): Identity = this.copy(mutables = this.getMutables.mutate(properties).getProtoBytes)

  def provision(accAddressData: AccAddressData): Identity = this.mutate(Seq(MetaProperty(id = schema.constants.Properties.AuthenticationProperty.getID, data = this.getAuthentication.add(accAddressData))))

  def unprovision(accAddressData: AccAddressData): Identity = this.mutate(Seq(MetaProperty(id = schema.constants.Properties.AuthenticationProperty.getID, data = this.getAuthentication.remove(accAddressData))))

}

private[blockchain] object Identities {

  class IdentityTable(tag: Tag) extends Table[Identity](tag, "Identity") with ModelTable[Array[Byte]] {

    def * = (id, idString, classificationID, immutables, mutables, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (Identity.tupled, Identity.unapply)

    def id = column[Array[Byte]]("id", O.PrimaryKey)

    def idString = column[String]("idString")

    def classificationID = column[Array[Byte]]("classificationID")

    def immutables = column[Array[Byte]]("immutables")

    def mutables = column[Array[Byte]]("mutables")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")

  }
}

@Singleton
class Identities @Inject()(
                            blockchainMaintainers: Maintainers,
                            blockchainClassifications: Classifications,
                            protected val dbConfigProvider: DatabaseConfigProvider
                          )(implicit val executionContext: ExecutionContext)
  extends GenericDaoImpl[Identities.IdentityTable, Identity, Array[Byte]]() {

  implicit val module: String = constants.Module.BLOCKCHAIN_IDENTITY

  implicit val logger: Logger = Logger(this.getClass)

  val tableQuery = new TableQuery(tag => new Identities.IdentityTable(tag))

  object Service {

    def add(identity: Identity): Future[Identity] = create(identity)

    def add(identities: Seq[Identity]): Future[Int] = create(identities)

    def insertOrUpdate(identity: Identity): Future[Int] = upsert(identity)

    def insertOrUpdate(identities: Seq[Identity]): Future[Int] = upsertMultiple(identities)

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

    def onDefine(msg: identityTransactions.define.Message): Future[String] = {
      val immutables = Immutables(PropertyList(msg.getImmutableMetaProperties).add(PropertyList(msg.getImmutableProperties).properties))
      val mutables = Mutables(PropertyList(msg.getMutableMetaProperties).add(PropertyList(msg.getMutableProperties).add(Seq(schema.constants.Properties.AuthenticationProperty)).properties))
      val add = blockchainClassifications.Utility.defineAuxiliary(msg.getFrom, mutables, immutables)

      def addMaintainer(classificationID: ClassificationID): Future[String] = blockchainMaintainers.Utility.superAuxiliary(classificationID, IdentityID(msg.getFromID), mutables, schema.utilities.Permissions.getIdentitiesPermissions(true, true))

      for {
        classificationID <- add
        _ <- addMaintainer(classificationID)
      } yield msg.getFrom
    }

    def onDeputize(msg: identityTransactions.deputize.Message): Future[String] = {
      val deputize = blockchainMaintainers.Utility.deputizeAuxiliary(
        fromID = IdentityID(msg.getFromID),
        toID = IdentityID(msg.getToID),
        maintainedClassificationID = ClassificationID(msg.getClassificationID),
        maintainedProperties = PropertyList(msg.getMaintainedProperties),
        permissionIDs = schema.utilities.Permissions.getIdentitiesPermissions(canIssue = msg.getCanIssueIdentity, canQuash = msg.getCanQuashIdentity),
        canAddMaintainer = msg.getCanAddMaintainer, canRemoveMaintainer = msg.getCanRemoveMaintainer, canMutateMaintainer = msg.getCanMutateMaintainer)
      for {
        _ <- deputize
      } yield msg.getFrom
    }


    def onIssue(msg: identityTransactions.issue.Message): Future[String] = {
      val immutables = Immutables(PropertyList(msg.getImmutableMetaProperties).add(PropertyList(msg.getImmutableProperties).properties))
      val classificationID = ClassificationID(msg.getClassificationID)
      val identityID = schema.utilities.ID.getIdentityID(classificationID = classificationID, immutables = immutables)
      val mutables = Mutables(PropertyList(msg.getMutableMetaProperties).add(PropertyList(msg.getMutableProperties).properties))
      val identity = Identity(id = identityID.getBytes, idString = identityID.asString, classificationID = ClassificationID(msg.getClassificationID).getBytes, immutables = immutables.getProtoBytes, mutables = mutables.getProtoBytes)
      val bond = blockchainClassifications.Utility.bondAuxiliary(msg.getFrom, classificationID, identity.getBondAmount)
      val add = Service.add(identity)

      for {
        _ <- add
        _ <- bond
      } yield msg.getFrom
    }

    def onUpdate(msg: identityTransactions.update.Message): Future[String] = {
      val identityID = IdentityID(msg.getIdentityID)
      val mutables = Mutables(PropertyList(msg.getMutableMetaProperties).add(PropertyList(msg.getMutableProperties).properties))
      val identity = Service.tryGet(identityID)

      def update(identity: Identity) = Service.update(identity.mutate(mutables.getProperties))

      for {
        identity <- identity
        _ <- update(identity)
      } yield msg.getFrom
    }

    def onName(msg: identityTransactions.name.Message): Future[String] = {
      val immutables = Immutables(PropertyList(Seq(schema.constants.Properties.NameProperty.mutate(IDData(StringID(msg.getName))))))
      val mutables = Mutables(PropertyList(Seq(schema.constants.Properties.AuthenticationProperty.mutate(ListData(Seq(AccAddressData(msg.getFrom)))))))
      val identityID = schema.utilities.ID.getIdentityID(classificationID = schema.document.NameIdentity.DocumentClassificationID, immutables = immutables)
      val identity = Identity(id = identityID.getBytes, idString = identityID.asString, classificationID = schema.document.NameIdentity.DocumentClassificationID.getBytes, immutables = immutables.getProtoBytes, mutables = mutables.getProtoBytes)
      val add = Service.add(identity)

      for {
        _ <- add
      } yield msg.getFrom
    }

    def onProvision(msg: identityTransactions.provision.Message): Future[String] = {
      val identity = Service.tryGet(IdentityID(msg.getIdentityID))

      def update(identity: Identity) = Service.insertOrUpdate(identity.provision(AccAddressData(msg.getTo)))

      for {
        identity <- identity
        _ <- update(identity)
      } yield msg.getFrom
    }

    def onQuash(msg: identityTransactions.quash.Message): Future[String] = {
      val identity = Service.tryGet(IdentityID(msg.getIdentityID))

      def updateUnbondAndDelete(identity: Identity) = {
        val delete = Service.delete(identity.getID)
        val unbond = blockchainClassifications.Utility.unbondAuxiliary(msg.getFrom, identity.getClassificationID, identity.getBondAmount)
        for {
          _ <- delete
          _ <- unbond
        } yield ()
      }

      for {
        identity <- identity
        _ <- updateUnbondAndDelete(identity)
      } yield msg.getFrom
    }

    def onRevoke(msg: identityTransactions.revoke.Message): Future[String] = {
      val revoke = blockchainMaintainers.Utility.revokeAuxiliary(fromID = IdentityID(msg.getFromID), toID = IdentityID(msg.getToID), maintainedClassificationID = ClassificationID(msg.getClassificationID))
      for {
        _ <- revoke
      } yield msg.getFrom
    }

    def onUnprovision(msg: identityTransactions.unprovision.Message): Future[String] = {
      val identity = Service.tryGet(IdentityID(msg.getIdentityID))

      def update(identity: Identity) = Service.insertOrUpdate(identity.unprovision(AccAddressData(msg.getTo)))

      for {
        identity <- identity
        _ <- update(identity)
      } yield msg.getFrom
    }

    def beforeRun: Future[Int] = {
      val identities = Seq("assets", "classifications", "identities", "maintainers", "metas", "orders", "splits").map(x =>
        Identity(id = schema.document.ModuleIdentity.getModuleIdentityID(x).getBytes, idString = schema.document.ModuleIdentity.getModuleIdentityID(x).asString, classificationID = schema.document.ModuleIdentity.DocumentClassificationID.getBytes, immutables = schema.document.ModuleIdentity.getModuleIdentityImmutables(x).getProtoBytes, mutables = schema.document.ModuleIdentity.getModuleIdentityMutables.getProtoBytes)
      )
      Service.insertOrUpdate(identities)
    }

  }
}