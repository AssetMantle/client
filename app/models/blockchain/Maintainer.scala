package models.blockchain

import models.traits.{Entity, GenericDaoImpl, Logging, ModelTable}
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

case class Maintainer(id: Array[Byte], idString: String, classificationID: Array[Byte], immutables: Array[Byte], mutables: Array[Byte], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity[Array[Byte]] {

  def getIDString: String = utilities.Secrets.base64URLEncoder(this.id)

  def getID: MaintainerID = MaintainerID(HashID(this.id))

  def getClassificationIDString: String = utilities.Secrets.base64URLEncoder(this.classificationID)

  def getClassificationID: ClassificationID = ClassificationID(this.classificationID)

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

  def maintainsProperty(id: PropertyID): Boolean = this.getMaintainedProperties.dataList.exists(_.getBytes.sameElements(IDData(id).getBytes))

  def canAdd: Boolean = this.getPermissions.dataList.exists(_.getBytes.sameElements(IDData(schema.constants.Properties.Add).getBytes))

  def canMutate: Boolean = this.getPermissions.dataList.exists(_.getBytes.sameElements(IDData(schema.constants.Properties.Mutate).getBytes))

  def canBurn: Boolean = this.getPermissions.dataList.exists(_.getBytes.sameElements(IDData(schema.constants.Properties.Burn).getBytes))

  def canMint: Boolean = this.getPermissions.dataList.exists(_.getBytes.sameElements(IDData(schema.constants.Properties.Mint).getBytes))

  def canRemove: Boolean = this.getPermissions.dataList.exists(_.getBytes.sameElements(IDData(schema.constants.Properties.Remove).getBytes))

  def canRenumerate: Boolean = this.getPermissions.dataList.exists(_.getBytes.sameElements(IDData(schema.constants.Properties.Renumerate).getBytes))
}

object Maintainers {

  implicit val module: String = constants.Module.BLOCKCHAIN_MAINTAINER

  implicit val logger: Logger = Logger(this.getClass)

  class DataTable(tag: Tag) extends Table[Maintainer](tag, "Maintainer") with ModelTable[Array[Byte]] {

    def * = (id, idString, classificationID, immutables, mutables, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (Maintainer.tupled, Maintainer.unapply)

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

    def add(maintainers: Seq[Maintainer]): Future[Unit] = create(maintainers)

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

    def deputize(fromID: IdentityID, toID: IdentityID, maintainedClassificationID: ClassificationID, maintainedProperties: PropertyList, canMintAsset: Boolean, canBurnAsset: Boolean, canRenumerateAsset: Boolean, canAddMaintainer: Boolean, canRemoveMaintainer: Boolean, canMutateMaintainer: Boolean): Future[Unit] = {
      val fromMaintainerID = utilities.ID.getMaintainerID(
        classificationID = schema.constants.ID.MaintainerClassificationID,
        immutables = Immutables(PropertyList(Seq(
          MetaProperty(id = schema.constants.Properties.MaintainedClassificationIDProperty.id, data = IDData(maintainedClassificationID)),
          MetaProperty(id = schema.constants.Properties.IdentityIDProperty.id, data = IDData(fromID)),
        ))))
      val toMaintainerID = utilities.ID.getMaintainerID(
        classificationID = schema.constants.ID.MaintainerClassificationID,
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

    def revoke(fromID: IdentityID, toID: IdentityID, maintainedClassificationID: ClassificationID): Future[Unit] = {
      val toMaintainerID = utilities.ID.getMaintainerID(classificationID = maintainedClassificationID, immutables = Immutables(PropertyList(Seq(
        MetaProperty(id = schema.constants.Properties.MaintainedClassificationIDProperty.id, data = IDData(maintainedClassificationID)),
        MetaProperty(id = schema.constants.Properties.IdentityIDProperty.id, data = IDData(toID)),
      ))))

      for {
        _ <- Service.delete(toMaintainerID)
      } yield ()
    }

    private def newMaintainer(identityID: IdentityID, maintainedClassificationID: ClassificationID, maintainedPropertyIDList: IDList, permissions: IDList): Maintainer = {
      val immutables = Immutables(PropertyList(Seq(
        MetaProperty(id = schema.constants.Properties.MaintainedClassificationIDProperty.id, data = IDData(maintainedClassificationID)),
        MetaProperty(id = schema.constants.Properties.IdentityIDProperty.id, data = IDData(identityID)),
      )))
      val mutables = Mutables(PropertyList(Seq(
        MetaProperty(id = schema.constants.Properties.MaintainedPropertiesProperty.id, data = ListData(maintainedPropertyIDList.idList.map(x => IDData(x)))),
        MetaProperty(id = schema.constants.Properties.PermissionsProperty.id, data = ListData(permissions.idList.map(x => IDData(x)))),
      )))
      val maintainerID = utilities.ID.getMaintainerID(classificationID = schema.constants.ID.MaintainerClassificationID, immutables = immutables)
      Maintainer(id = maintainerID.getBytes, idString = maintainerID.asString, classificationID = maintainedClassificationID.getBytes, immutables = immutables.getProtoBytes, mutables = mutables.getProtoBytes)
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