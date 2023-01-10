package models.master

import exceptions.BaseException
import models.Trait.Logged
import models.blockchain
import models.common.Serializable.{BaseProperty, Data}
import models.common.{DataValue, Serializable}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Property(entityID: String, entityType: String, name: String, value: Option[String], dataType: String, isMeta: Boolean, isMutable: Boolean, hashID: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {

  def toSerializableMetaProperty: Serializable.MetaProperty = Serializable.MetaProperty(id = name, metaFact = Serializable.MetaFact(Data(dataType = dataType, dataValue = value)))

  def toSerializableProperty: Serializable.Property = Serializable.Property(id = name, fact = Serializable.NewFact(factType = DataValue.getFactTypeFromDataType(dataType), dataValue = DataValue.getDataValue(dataType = dataType, dataValue = value)))

}

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

    def getAll(entityID: String, entityType: String): Future[Seq[Property]] = getAllByEntityIDAndEntityType(entityID = entityID, entityType = entityType)

    def insertMultiple(properties: Seq[Property]): Future[Seq[String]] = addMultiple(properties)

    def tryGet(entityID: String, entityType: String, name: String): Future[Property] = tryGetByEntityIDEntityTypeAndName(entityID = entityID, entityType = entityType, name = name)

    def updateValue(entityID: String, entityType: String, name: String, value: String): Future[Int] = updateValueByEntityIDEntityTypeAndName(entityID = entityID, entityType = entityType, name = name, value = value)

    def insertOrUpdate(property: Property): Future[Int] = upsert(property)

    def deleteAll(entityID: String, entityType: String): Future[Int] = deleteByEntityIDAndEntityType(entityID = entityID, entityType = entityType)

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

  }

}