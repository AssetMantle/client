package models.master

import exceptions.BaseException
import models.Trait.Logged
import models.blockchain
import models.blockchain.Meta
import models.common.Serializable.{AssetOtherDetails, BaseProperty}
import models.common.{DataValue, Serializable}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import utilities.MicroNumber

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class Property(entityID: String, entityType: String, name: String, value: Option[String], dataType: String, isMeta: Boolean, isMutable: Boolean, hashID: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {

  def toSerializableMetaProperty: Serializable.MetaProperty = Serializable.MetaProperty(id = name, metaFact = Serializable.MetaFact(DataValue.getData(dataType = dataType, dataValue = value)))

  def toSerializableProperty: Serializable.Property = Serializable.Property(id = name, fact = Serializable.NewFact(factType = DataValue.getFactTypeFromDataType(dataType), dataValue = DataValue.getDataValue(dataType = dataType, dataValue = value)))

}

case class AssetProperty(id: String, assetType: String, description: String, quantity: MicroNumber, quantityUnit: String, price: MicroNumber, moderated: Boolean, takerID: Option[String] = None, shippingPeriod: Int, portOfLoading: String, portOfDischarge: String)

@Singleton
class Properties @Inject()(
                            configuration: Configuration,
                            blockchainClassifications: blockchain.Classifications,
                            blockchainMetas: blockchain.Metas,
                            utilitiesOperations: utilities.Operations,
                            protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_PROPERTY

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val propertyTable = TableQuery[PropertyTable]

  private def add(property: Property): Future[String] = db.run((propertyTable returning propertyTable.map(_.entityID) += property).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def addMultiple(properties: Seq[Property]): Future[Seq[String]] = db.run((propertyTable returning propertyTable.map(_.entityID) ++= properties).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(property: Property): Future[Int] = db.run(propertyTable.insertOrUpdate(property).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def tryGetByEntityIDEntityTypeAndName(entityID: String, entityType: String, name: String) = db.run(propertyTable.filter(x => x.entityID === entityID && x.entityType === entityType && x.name === name).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateValueByEntityIDEntityTypeAndName(entityID: String, entityType: String, name: String, value: String): Future[Int] = db.run(propertyTable.filter(x => x.entityID === entityID && x.entityType === entityType && x.name === name).map(_.value).update(value).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def getAllByEntityIDAndEntityType(entityID: String, entityType: String): Future[Seq[Property]] = db.run(propertyTable.filter(x => x.entityID === entityID && x.entityType === entityType).result)

  private def getAllByEntityIDsAndEntityType(entityIDs: Seq[String], entityType: String): Future[Seq[Property]] = db.run(propertyTable.filter(x => x.entityID.inSet(entityIDs) && x.entityType === entityType).result)

  private def deleteByEntityIDAndEntityType(entityID: String, entityType: String) = db.run(propertyTable.filter(x => x.entityID === entityID && x.entityType === entityType).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class PropertyTable(tag: Tag) extends Table[Property](tag, "Property") {

    def * = (entityID, entityType, name, value.?, dataType, isMeta, isMutable, hashID, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Property.tupled, Property.unapply)

    def entityID = column[String]("entityID", O.PrimaryKey)

    def entityType = column[String]("entityType", O.PrimaryKey)

    def name = column[String]("name", O.PrimaryKey)

    def value = column[String]("value")

    def dataType = column[String]("dataType")

    def isMeta = column[Boolean]("isMeta")

    def isMutable = column[Boolean]("isMutable")

    def hashID = column[String]("hashID")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(property: Property): Future[String] = add(property)

    def getAll(entityID: String, entityType: String) = getAllByEntityIDAndEntityType(entityID = entityID, entityType = entityType)

    //def getAllInMapForm(entityID: String, entityType: String) = getAllByEntityIDAndEntityType(entityID = entityID, entityType = entityType).map(x=> x.map(a => a.name -> a.value).toMap)

    def insertMultiple(properties: Seq[Property]): Future[Seq[String]] = addMultiple(properties)

    def tryGet(entityID: String, entityType: String, name: String): Future[Property] = tryGetByEntityIDEntityTypeAndName(entityID = entityID, entityType = entityType, name = name)

    def updateValue(entityID: String, entityType: String, name: String, value: String): Future[Int] = updateValueByEntityIDEntityTypeAndName(entityID = entityID, entityType = entityType, name = name, value = value)

    def insertOrUpdate(property: Property): Future[Int] = upsert(property)

    def deleteAll(entityID: String, entityType: String): Future[Int] = deleteByEntityIDAndEntityType(entityID = entityID, entityType = entityType)

    def getAssetProperty(assetID:String) = getAllByEntityIDAndEntityType(entityID = assetID, entityType = constants.Blockchain.Entity.ASSET).map{ properties=>
      AssetProperty(
        id = assetID,
        assetType = properties.find(_.name == constants.Property.ASSET_TYPE.dataName).flatMap(_.value).getOrElse(throw new BaseException(constants.Response.FAILURE)),
        description = properties.find(_.name == constants.Property.ASSET_DESCRIPTION.dataName).flatMap(_.value).getOrElse(throw new BaseException(constants.Response.FAILURE)),
        quantity = Try(MicroNumber(BigInt(properties.find(_.name == constants.Property.QUANTITY.dataName).flatMap(_.value).getOrElse("")))).getOrElse(throw new BaseException(constants.Response.FAILURE)),
        quantityUnit = properties.find(_.name == constants.Property.QUANTITY_UNIT.dataName).flatMap(_.value).getOrElse(throw new BaseException(constants.Response.FAILURE)),
        price = Try(MicroNumber(BigInt(properties.find(_.name == constants.Property.PRICE.dataName).flatMap(_.value).getOrElse("")))).getOrElse(throw new BaseException(constants.Response.FAILURE)),
        moderated = Try(properties.find(_.name == constants.Property.MODERATED.dataName).flatMap(_.value).getOrElse("").toBoolean).getOrElse(throw new BaseException(constants.Response.FAILURE)),
        takerID = properties.find(_.name == constants.Property.TAKER_ID.dataName).getOrElse(throw new BaseException(constants.Response.FAILURE)).value,
        shippingPeriod = Try(properties.find(_.name == constants.Property.SHIPPING_PERIOD.dataName).flatMap(_.value).getOrElse("").toInt).getOrElse(throw new BaseException(constants.Response.FAILURE)),
        portOfLoading = properties.find(_.name == constants.Property.PORT_OF_LOADING.dataName).flatMap(_.value).getOrElse(throw new BaseException(constants.Response.FAILURE)),
        portOfDischarge = properties.find(_.name == constants.Property.PORT_OF_DISCHARGE.dataName).flatMap(_.value).getOrElse(throw new BaseException(constants.Response.FAILURE)),
      )
    }

    def getAssetPropertyList(assetIDs: Seq[String]) = getAllByEntityIDsAndEntityType(entityIDs = assetIDs, entityType = constants.Blockchain.Entity.ASSET).map{properties=>
      assetIDs.map{assetID=>
        val assetProperties = properties.filter(_.entityID == assetID)
        AssetProperty(
          id = assetID,
          assetType = assetProperties.find(_.name == constants.Property.ASSET_TYPE.dataName).flatMap(_.value).getOrElse(throw new BaseException(constants.Response.FAILURE)),
          description = assetProperties.find(_.name == constants.Property.ASSET_DESCRIPTION.dataName).flatMap(_.value).getOrElse(throw new BaseException(constants.Response.FAILURE)),
          quantity = Try(MicroNumber(BigInt(assetProperties.find(_.name == constants.Property.QUANTITY.dataName).flatMap(_.value).getOrElse("")))).getOrElse(throw new BaseException(constants.Response.FAILURE)),
          quantityUnit = assetProperties.find(_.name == constants.Property.QUANTITY_UNIT.dataName).flatMap(_.value).getOrElse(throw new BaseException(constants.Response.FAILURE)),
          price = Try(MicroNumber(BigInt(assetProperties.find(_.name == constants.Property.PRICE.dataName).flatMap(_.value).getOrElse("")))).getOrElse(throw new BaseException(constants.Response.FAILURE)),
          moderated = Try(assetProperties.find(_.name == constants.Property.MODERATED.dataName).flatMap(_.value).getOrElse("").toBoolean).getOrElse(throw new BaseException(constants.Response.FAILURE)),
          takerID = assetProperties.find(_.name == constants.Property.TAKER_ID.dataName).getOrElse(throw new BaseException(constants.Response.FAILURE)).value,
          shippingPeriod = Try(assetProperties.find(_.name == constants.Property.SHIPPING_PERIOD.dataName).flatMap(_.value).getOrElse("").toInt).getOrElse(throw new BaseException(constants.Response.FAILURE)),
          portOfLoading = assetProperties.find(_.name == constants.Property.PORT_OF_LOADING.dataName).flatMap(_.value).getOrElse(throw new BaseException(constants.Response.FAILURE)),
          portOfDischarge = assetProperties.find(_.name == constants.Property.PORT_OF_DISCHARGE.dataName).flatMap(_.value).getOrElse(throw new BaseException(constants.Response.FAILURE)),
        )
      }

    }

    def getPropertyMap(assetID:String): Future[Map[String,Option[String]]] = getAllByEntityIDAndEntityType(entityID = assetID, entityType = constants.Blockchain.Entity.ASSET).map(x=> x.map(a => a.name -> a.value).toMap)

    def getPropertyListMap(assetIDs:Seq[String]): Future[Map[String, Map[String,Option[String]]]] = getAllByEntityIDsAndEntityType(assetIDs, constants.Blockchain.Entity.ASSET).map(assetProperties=> assetIDs.map(assetID=> assetID -> assetProperties.filter(_.entityID == assetID).map(property=> property.name-> property.value).toMap).toMap)

  }

  object Utilities {

    def upsertProperties(entityType: String, entityID: String, immutableMetas: Seq[BaseProperty], immutables: Seq[BaseProperty], mutableMetas: Seq[BaseProperty], mutables: Seq[BaseProperty]): Future[String] = {
      val upsertImmutableMetas = utilitiesOperations.traverse(immutableMetas)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.dataName, value = x.dataValue, dataType = x.dataType, isMeta = true, isMutable = false, hashID = DataValue.getHash(DataValue.getDataValue(dataType = x.dataType, dataValue = x.dataValue)))))
      val upsertImmutables = utilitiesOperations.traverse(immutables)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.dataName, value = x.dataValue, dataType = x.dataType, isMeta = false, isMutable = false, hashID = DataValue.getHash(DataValue.getDataValue(dataType = x.dataType, dataValue = x.dataValue)))))
      val upsertMutableMetas = utilitiesOperations.traverse(mutableMetas)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.dataName, value = x.dataValue, dataType = x.dataType, isMeta = true, isMutable = true, hashID = DataValue.getHash(DataValue.getDataValue(dataType = x.dataType, dataValue = x.dataValue)))))
      val upsertMutables = utilitiesOperations.traverse(mutables)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.dataName, value = x.dataValue, dataType = x.dataType, isMeta = false, isMutable = true, hashID = DataValue.getHash(DataValue.getDataValue(dataType = x.dataType, dataValue = x.dataValue)))))
      (for {
        _ <- upsertImmutableMetas
        _ <- upsertImmutables
        _ <- upsertMutableMetas
        _ <- upsertMutables
      } yield entityID).recoverWith {
        case baseException: BaseException => Service.deleteAll(entityID = entityID, entityType = entityType)
          throw baseException
      }
    }

    def updateProperties(entityType: String, entityID: String, mutableMetas: Seq[BaseProperty], mutables: Seq[BaseProperty]): Future[String] = {
      val updateMutableMetas = utilitiesOperations.traverse(mutableMetas)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.dataName, value = x.dataValue, dataType = x.dataType, isMeta = true, isMutable = true, hashID = DataValue.getHash(DataValue.getDataValue(dataType = x.dataType, dataValue = x.dataValue)))))
      val updateMutables = utilitiesOperations.traverse(mutables)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.dataName, value = x.dataValue, dataType = x.dataType, isMeta = false, isMutable = true, hashID = DataValue.getHash(DataValue.getDataValue(dataType = x.dataType, dataValue = x.dataValue)))))
      (for {
        _ <- updateMutableMetas
        _ <- updateMutables
      } yield entityID).recoverWith {
        case baseException: BaseException => throw baseException
      }
    }

    //TODO After correction of multiple properties with same name in BC, utilitiesOperations.traverse can be written to Future.traverse
    def upsertProperties(entityType: String, entityID: String, immutableMetas: Serializable.MetaProperties, immutables: Serializable.Properties, mutableMetas: Serializable.MetaProperties, mutables: Serializable.Properties): Future[String] = {
      val metas = blockchainMetas.Service.get(immutables.propertyList.map(_.fact.hash) ++ mutables.propertyList.map(_.fact.hash))

      def upsert(metas: Seq[Meta]) = {
        val upsertImmutableMetas = utilitiesOperations.traverse(immutableMetas.metaPropertyList)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.id, value = Option(x.metaFact.data.value.asString), dataType = x.metaFact.data.dataType, isMeta = true, isMutable = false, hashID = x.metaFact.data.value.generateHash)))
        val upsertImmutables = utilitiesOperations.traverse(immutables.propertyList)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.id, value = metas.find(_.id == x.id).fold[Option[String]](None)(x => Option(x.dataValue)), dataType = DataValue.getDataTypeFromFactType(x.fact.factType), isMeta = false, isMutable = false, hashID = x.fact.hash)))
        val upsertMutableMetas = utilitiesOperations.traverse(mutableMetas.metaPropertyList)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.id, value = Option(x.metaFact.data.value.asString), dataType = x.metaFact.data.dataType, isMeta = true, isMutable = true, hashID = x.metaFact.data.value.generateHash)))
        val upsertMutables = utilitiesOperations.traverse(mutables.propertyList)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.id, value = metas.find(_.id == x.id).fold[Option[String]](None)(x => Option(x.dataValue)), dataType = DataValue.getDataTypeFromFactType(x.fact.factType), isMeta = false, isMutable = true, hashID = x.fact.hash)))

        for {
          _ <- upsertImmutableMetas
          _ <- upsertImmutables
          _ <- upsertMutableMetas
          _ <- upsertMutables
        } yield ()
      }

      (for {
        metas <- metas
        _ <- upsert(metas)
      } yield entityID).recover {
        case baseException: BaseException => throw baseException
      }
    }

    //TODO After correction of multiple properties with same name in BC, utilitiesOperations.traverse can be written to Future.traverse
    def updateProperties(entityType: String, entityID: String, mutableMetas: Serializable.MetaProperties, mutables: Serializable.Properties): Future[String] = {
      val metas = blockchainMetas.Service.get(mutables.propertyList.map(_.fact.hash))
      val oldProperties = Service.getAll(entityID = entityID, entityType = entityType)

      def update(metas: Seq[Meta], oldProperties: Seq[Property]) = {
        val updateMutableMetas = utilitiesOperations.traverse(mutableMetas.metaPropertyList)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.id, value = Option(x.metaFact.data.value.asString), dataType = x.metaFact.data.dataType, isMeta = true, isMutable = true, hashID = x.metaFact.data.value.generateHash)))

        val updateMutables = utilitiesOperations.traverse(mutables.propertyList) { newMutables =>
          oldProperties.find(_.name == newMutables.id).fold {
            Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = newMutables.id, value = metas.find(_.id == newMutables.id).fold[Option[String]](None)(x => Option(x.dataValue)), dataType = DataValue.getDataTypeFromFactType(newMutables.fact.factType), isMeta = false, isMutable = true, hashID = newMutables.fact.hash))
          } { oldProperty =>
            if (oldProperty.hashID != newMutables.fact.hash) Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = newMutables.id, value = metas.find(_.id == newMutables.id).fold[Option[String]](None)(x => Option(x.dataValue)), dataType = DataValue.getDataTypeFromFactType(newMutables.fact.factType), isMeta = false, isMutable = true, hashID = newMutables.fact.hash))
            else Future(0)
          }
        }

        for {
          _ <- updateMutableMetas
          _ <- updateMutables
        } yield ()
      }

      (for {
        metas <- metas
        oldProperties <- oldProperties
        _ <- update(metas, oldProperties)
      } yield entityID).recover {
        case baseException: BaseException => throw baseException
      }
    }

  }

}