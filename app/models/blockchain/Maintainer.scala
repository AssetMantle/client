package models.blockchain

import models.traits._
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import schema.data.base.{IDData, ListData}
import schema.document.Document
import schema.id.base._
import schema.list.{IDList, PropertyList}
import schema.property.Property
import schema.property.base.MetaProperty
import schema.qualified.{Immutables, Mutables}
import slick.jdbc.H2Profile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class Maintainer(id: Array[Byte], idString: String, maintainedClassificationID: Array[Byte], immutables: Array[Byte], mutables: Array[Byte], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity[Array[Byte]] {

  def getIDString: String = utilities.Secrets.base64URLEncoder(this.id)

  def getID: MaintainerID = MaintainerID(HashID(this.id))

  def getClassificationIDString: String = schema.document.Maintainer.MaintainerClassificationID.asString

  def getClassificationID: ClassificationID = schema.document.Maintainer.MaintainerClassificationID

  def getMaintainedClassificationID: ClassificationID = ClassificationID(this.maintainedClassificationID)

  def getMaintainedClassificationIDAsString: String = utilities.Secrets.base64URLEncoder(this.maintainedClassificationID)

  def getPermissions: ListData = {
    val property = this.getProperty(schema.constants.Properties.PermissionsProperty.getID)
    ListData((if (property.isDefined) MetaProperty(property.get.getProtoBytes) else schema.constants.Properties.PermissionsProperty).getData.getProtoBytes)
  }

  def getMaintainedProperties: ListData = {
    val property = this.getProperty(schema.constants.Properties.MaintainedPropertiesProperty.getID)
    ListData((if (property.isDefined) MetaProperty(property.get.getProtoBytes) else schema.constants.Properties.MaintainedPropertiesProperty).getData.getProtoBytes)
  }

  def getImmutables: Immutables = Immutables(this.immutables)

  def getMutables: Mutables = Mutables(this.mutables)

  def getDocument: Document = Document(this.getClassificationID, this.getImmutables, this.getMutables)

  def getProperty(id: PropertyID): Option[Property] = this.getDocument.getProperty(id)

  def mutate(properties: Seq[Property]): Maintainer = this.copy(mutables = this.getMutables.mutate(properties).getProtoBytes)

  def maintainsProperty(id: PropertyID): Boolean = this.getMaintainedProperties.getDataList.exists(_.getBytes.sameElements(IDData(id).getBytes))

  def canAdd: Boolean = this.getPermissions.getDataList.exists(_.getBytes.sameElements(IDData(schema.constants.Properties.Add).getBytes))

  def canMutate: Boolean = this.getPermissions.getDataList.exists(_.getBytes.sameElements(IDData(schema.constants.Properties.Mutate).getBytes))

  def canBurn: Boolean = this.getPermissions.getDataList.exists(_.getBytes.sameElements(IDData(schema.constants.Properties.Burn).getBytes))

  def canMint: Boolean = this.getPermissions.getDataList.exists(_.getBytes.sameElements(IDData(schema.constants.Properties.Mint).getBytes))

  def canRemove: Boolean = this.getPermissions.getDataList.exists(_.getBytes.sameElements(IDData(schema.constants.Properties.Remove).getBytes))

  def canRenumerate: Boolean = this.getPermissions.getDataList.exists(_.getBytes.sameElements(IDData(schema.constants.Properties.Renumerate).getBytes))
}

private[blockchain] object Maintainers {

  class MaintainerTable(tag: Tag) extends Table[Maintainer](tag, "Maintainer") with ModelTable[Array[Byte]] {

    def * = (id, idString, maintainedClassificationID, immutables, mutables, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (Maintainer.tupled, Maintainer.unapply)

    def id = column[Array[Byte]]("id", O.PrimaryKey)

    def idString = column[String]("idString")

    def maintainedClassificationID = column[Array[Byte]]("maintainedClassificationID")

    def immutables = column[Array[Byte]]("immutables")

    def mutables = column[Array[Byte]]("mutables")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")

  }
}

@Singleton
class Maintainers @Inject()(
                             protected val dbConfigProvider: DatabaseConfigProvider
                           )(implicit val executionContext: ExecutionContext)
  extends GenericDaoImpl[Maintainers.MaintainerTable, Maintainer, Array[Byte]]() {

  implicit val module: String = constants.Module.BLOCKCHAIN_MAINTAINER

  implicit val logger: Logger = Logger(this.getClass)

  val tableQuery = new TableQuery(tag => new Maintainers.MaintainerTable(tag))

  object Service {

    def add(maintainer: Maintainer): Future[String] = create(maintainer).map(_.idString)

    def add(maintainers: Seq[Maintainer]): Future[Int] = create(maintainers)

    def update(maintainer: Maintainer): Future[String] = updateById(maintainer).map(_ => maintainer.getIDString)

    def get(id: String): Future[Option[Maintainer]] = getById(utilities.Secrets.base64URLDecode(id))

    def get(id: MaintainerID): Future[Option[Maintainer]] = getById(id.getBytes)

    def tryGet(id: String): Future[Maintainer] = tryGetById(utilities.Secrets.base64URLDecode(id))

    def tryGet(id: MaintainerID): Future[Maintainer] = tryGetById(id.getBytes)

    def fetchAll: Future[Seq[Maintainer]] = getAll

    def delete(id: MaintainerID): Future[Int] = deleteById(id.getBytes)

  }

  object Utility {

    def superAuxiliary(maintainedClassificationID: ClassificationID, toID: IdentityID, maintainedMutables: Mutables): Future[String] = {
      val permissions = getPermissions(canAdd = true, canMutate = true, canBurn = true, canMint = true, canRemove = true, canRenumerate = true)
      Service.add(newMaintainer(identityID = toID, maintainedClassificationID = maintainedClassificationID, maintainedPropertyIDList = maintainedMutables.propertyList.getPropertyIDList, permissions = permissions))
    }

    def deputizeAuxiliary(fromID: IdentityID, toID: IdentityID, maintainedClassificationID: ClassificationID, maintainedProperties: PropertyList, canMintAsset: Boolean, canBurnAsset: Boolean, canRenumerateAsset: Boolean, canAddMaintainer: Boolean, canRemoveMaintainer: Boolean, canMutateMaintainer: Boolean): Future[Unit] = {
      val fromMaintainerID = schema.utilities.ID.getMaintainerID(
        classificationID = schema.document.Maintainer.MaintainerClassificationID,
        immutables = Immutables(PropertyList(Seq(
          MetaProperty(id = schema.constants.Properties.MaintainedClassificationIDProperty.id, data = IDData(maintainedClassificationID)),
          MetaProperty(id = schema.constants.Properties.IdentityIDProperty.id, data = IDData(fromID)),
        ))))
      val toMaintainerID = schema.utilities.ID.getMaintainerID(
        classificationID = schema.document.Maintainer.MaintainerClassificationID,
        immutables = Immutables(PropertyList(Seq(
          MetaProperty(id = schema.constants.Properties.MaintainedClassificationIDProperty.id, data = IDData(maintainedClassificationID)),
          MetaProperty(id = schema.constants.Properties.IdentityIDProperty.id, data = IDData(toID)),
        ))))
      val fromMaintainer = Service.tryGet(fromMaintainerID)
      val toMaintainer = Service.get(toMaintainerID)

      //def getRemoveMaintainedPropertyList(fromMaintainer: Maintainer) = fromMaintainer.getMutables.remove(maintainedProperties.getProperties)

      def addOrUpdate(fromMaintainer: Maintainer, toMaintainer: Option[Maintainer]) = {
        val permissions = getPermissions(canAdd = canAddMaintainer, canMutate = canMutateMaintainer, canBurn = canBurnAsset, canMint = canMintAsset, canRemove = canRemoveMaintainer, canRenumerate = canRenumerateAsset)
        if (toMaintainer.isEmpty) {
          Service.add(newMaintainer(identityID = toID, maintainedClassificationID = maintainedClassificationID, maintainedPropertyIDList = maintainedProperties.getPropertyIDList, permissions = permissions))
        } else {
          val updatedMaintainedProperties = toMaintainer.get.getMutables.propertyList
            .add(maintainedProperties.getProperties)
            .remove(fromMaintainer.getMutables.remove(maintainedProperties.getProperties).getProperties)
          Service.update(newMaintainer(identityID = toID, maintainedClassificationID = maintainedClassificationID, maintainedPropertyIDList = updatedMaintainedProperties.getPropertyIDList, permissions = permissions))
        }
      }

      for {
        fromMaintainer <- fromMaintainer
        toMaintainer <- toMaintainer
        _ <- addOrUpdate(fromMaintainer, toMaintainer)
      } yield ()
    }

    def revokeAuxiliary(fromID: IdentityID, toID: IdentityID, maintainedClassificationID: ClassificationID): Future[Unit] = {
      val toMaintainerID = schema.utilities.ID.getMaintainerID(classificationID = maintainedClassificationID, immutables = Immutables(PropertyList(Seq(
        MetaProperty(id = schema.constants.Properties.MaintainedClassificationIDProperty.id, data = IDData(maintainedClassificationID)),
        MetaProperty(id = schema.constants.Properties.IdentityIDProperty.id, data = IDData(toID)),
      ))))

      for {
        _ <- Service.delete(toMaintainerID)
      } yield ()
    }

    private def newMaintainer(identityID: IdentityID, maintainedClassificationID: ClassificationID, maintainedPropertyIDList: IDList, permissions: IDList): Maintainer = {
      val document = schema.document.Maintainer.getMaintainerDocument(identityID = identityID, maintainedPropertyIDList = maintainedPropertyIDList, maintainedClassificationID = maintainedClassificationID, permissions = permissions)
      val maintainerID = schema.utilities.ID.getMaintainerID(classificationID = schema.document.Maintainer.MaintainerClassificationID, immutables = document.immutables)
      Maintainer(id = maintainerID.getBytes, idString = maintainerID.asString, maintainedClassificationID = maintainedClassificationID.getBytes, immutables = document.immutables.getProtoBytes, mutables = document.mutables.getProtoBytes)
    }

    private def getPermissions(canAdd: Boolean, canMutate: Boolean, canBurn: Boolean, canMint: Boolean, canRemove: Boolean, canRenumerate: Boolean) = {
      var permissions: Seq[StringID] = Seq()
      if (canAdd) permissions = permissions :+ schema.constants.Properties.Add
      if (canMutate) permissions = permissions :+ schema.constants.Properties.Mutate
      if (canBurn) permissions = permissions :+ schema.constants.Properties.Burn
      if (canMint) permissions = permissions :+ schema.constants.Properties.Mint
      if (canRemove) permissions = permissions :+ schema.constants.Properties.Remove
      if (canRenumerate) permissions = permissions :+ schema.constants.Properties.Renumerate
      IDList(permissions)
    }

  }
}