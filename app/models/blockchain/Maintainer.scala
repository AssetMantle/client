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

    def superAuxiliary(maintainedClassificationID: ClassificationID, toID: IdentityID, maintainedMutables: Mutables, permissionIDs: Seq[StringID]): Future[String] = {
      val updatedMutableIDList = IDList(maintainedMutables.remove(Seq(schema.constants.Properties.BondAmountProperty, schema.constants.Properties.AuthenticationProperty)).getProperties.map(_.getID))
      val document = schema.document.Maintainer.getMaintainerDocument(identityID = toID, maintainedClassificationID = maintainedClassificationID, maintainedPropertyIDList = updatedMutableIDList, permissions = IDList(schema.utilities.Permissions.getMaintainersPermissions(true, true, true)).add(permissionIDs))
      Service.add(newMaintainer(document, maintainedClassificationID))
    }

    def deputizeAuxiliary(fromID: IdentityID, toID: IdentityID, maintainedClassificationID: ClassificationID, maintainedProperties: PropertyList, permissionIDs: Seq[StringID], canAddMaintainer: Boolean, canRemoveMaintainer: Boolean, canMutateMaintainer: Boolean): Future[Unit] = {
      val fromMaintainerID = schema.utilities.ID.getMaintainerID(
        classificationID = schema.document.Maintainer.MaintainerClassificationID,
        immutables = Immutables(PropertyList(Seq(
          schema.constants.Properties.MaintainedClassificationIDProperty.mutate(IDData(maintainedClassificationID)),
          schema.constants.Properties.IdentityIDProperty.mutate(IDData(fromID))
        ))))
      val toMaintainerID = schema.utilities.ID.getMaintainerID(
        classificationID = schema.document.Maintainer.MaintainerClassificationID,
        immutables = Immutables(PropertyList(Seq(
          schema.constants.Properties.MaintainedClassificationIDProperty.mutate(IDData(maintainedClassificationID)),
          schema.constants.Properties.IdentityIDProperty.mutate(IDData(toID))
        ))))
      val fromMaintainer = Service.tryGet(fromMaintainerID)
      val toMaintainer = Service.get(toMaintainerID)

      //def getRemoveMaintainedPropertyList(fromMaintainer: Maintainer) = fromMaintainer.getMutables.remove(maintainedProperties.getProperties)

      def addOrUpdate(fromMaintainer: Maintainer, toMaintainer: Option[Maintainer]) = {
//        if (toMaintainer.isEmpty) {
//          Service.add(newMaintainer(identityID = toID, maintainedClassificationID = maintainedClassificationID, maintainedPropertyIDList = maintainedProperties.getPropertyIDList, permissions = permissions))
//        } else {
//          val updatedMaintainedProperties = toMaintainer.get.getMutables.propertyList
//            .add(maintainedProperties.getProperties)
//            .remove(fromMaintainer.getMutables.remove(maintainedProperties.getProperties).getProperties)
//          Service.update(newMaintainer(identityID = toID, maintainedClassificationID = maintainedClassificationID, maintainedPropertyIDList = updatedMaintainedProperties.getPropertyIDList, permissions = permissions))
//        }
        Future()
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

    private def newMaintainer(document: Document, maintainedClassificationID: ClassificationID): Maintainer = {
      val maintainerID = schema.utilities.ID.getMaintainerID(classificationID = schema.document.Maintainer.MaintainerClassificationID, immutables = document.immutables)
      Maintainer(id = maintainerID.getBytes, idString = maintainerID.asString, maintainedClassificationID = maintainedClassificationID.getBytes, immutables = document.immutables.getProtoBytes, mutables = document.mutables.getProtoBytes)
    }
  }
}