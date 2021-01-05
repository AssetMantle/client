package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.blockchain
import models.blockchain.Meta
import models.common.{DataValue, Serializable}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import views.companion.common.Property.{Data => companionPropertyData}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Property(entityID: String, entityType: String, name: String, value: Option[String], dataType: String, isMeta: Boolean, isRevealed: Boolean, isMutable: Boolean, hashID: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {

  def toSerializableMetaProperty: Serializable.MetaProperty = Serializable.MetaProperty(id = name, metaFact = Serializable.MetaFact(DataValue.getData(dataType = dataType, dataValue = value)))

  def toSerializableProperty: Serializable.Property = Serializable.Property(id = name, fact = Serializable.NewFact(factType = DataValue.getFactTypeFromDataType(dataType), dataValue = DataValue.getDataValue(dataType = dataType, dataValue = value)))

}

@Singleton
class Properties @Inject()(
                            configuration: Configuration,
                            blockchainClassifications: blockchain.Classifications,
                            blockchainMetas: blockchain.Metas,
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

  private def findEntityIDByNameValueAndEntityType(name: String, values: Seq[String], hashedValues: Seq[String], entityType: String) = db.run(propertyTable.filter(x => x.name === name && x.entityType === entityType && (x.value.inSet(values) || x.value.inSet(hashedValues))).map(_.entityID).result)

  private[models] class PropertyTable(tag: Tag) extends Table[Property](tag, "Property") {

    def * = (entityID, entityType, name, value.?, dataType, isMeta, isRevealed, isMutable, hashID, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Property.tupled, Property.unapply)

    def entityID = column[String]("entityID", O.PrimaryKey)

    def entityType = column[String]("entityType", O.PrimaryKey)

    def name = column[String]("name", O.PrimaryKey)

    def value = column[String]("value")

    def dataType = column[String]("dataType")

    def isMeta = column[Boolean]("isMeta")

    def isRevealed = column[Boolean]("isRevealed")

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

    def getAllPrivateOrderIDs(takerIDs: Seq[String]): Future[Seq[String]] = findEntityIDByNameValueAndEntityType(name = constants.Blockchain.Properties.TakerID, values = takerIDs, hashedValues = takerIDs.map(utilities.Hash.getHash(_)), entityType = constants.Blockchain.Entity.ORDER)

  }

  object Utilities {

    private val chainID = configuration.get[String]("blockchain.chainID")

    //Value of meta properties need not to be fetched from Meta as values already came from the user but exists does need to be checked
    def upsertProperties(entityType: String, entityID: String, immutableMetas: Seq[companionPropertyData], immutables: Seq[companionPropertyData], mutableMetas: Seq[companionPropertyData], mutables: Seq[companionPropertyData]): Future[String] = {
      val metas = blockchainMetas.Service.get(immutables.map(_.toProperty).map(_.fact.hash) ++ mutables.map(_.toProperty).map(_.fact.hash))

      def upsert(metas: Seq[Meta]) = {
        val upsertImmutableMetas = Future.traverse(immutableMetas)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.dataName, value = x.dataValue, dataType = x.dataType, isMeta = true, isRevealed = true, isMutable = false, hashID = DataValue.getHash(dataType = x.dataType, dataValue = x.dataValue.getOrElse("")))))
        val upsertImmutables = Future.traverse(immutables)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.dataName, value = x.dataValue, dataType = x.dataType, isMeta = false, isRevealed = metas.exists(_.id == x.toProperty.fact.hash), isMutable = false, hashID = DataValue.getHash(dataType = x.dataType, dataValue = x.dataValue.getOrElse("")))))
        val upsertMutableMetas = Future.traverse(mutableMetas)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.dataName, value = x.dataValue, dataType = x.dataType, isMeta = true, isRevealed = true, isMutable = true, hashID = DataValue.getHash(dataType = x.dataType, dataValue = x.dataValue.getOrElse("")))))
        val upsertMutables = Future.traverse(mutables)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.dataName, value = x.dataValue, dataType = x.dataType, isMeta = false, isRevealed = metas.exists(_.id == x.toProperty.fact.hash), isMutable = true, hashID = DataValue.getHash(dataType = x.dataType, dataValue = x.dataValue.getOrElse("")))))
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
      } yield entityID).recoverWith {
        case baseException: BaseException => Service.deleteAll(entityID = entityID, entityType = entityType)
          throw baseException
      }
    }

    //Value of meta properties need not to be fetched from Meta as values already came from the user but exists does need to be checked
    def updateProperties(entityType: String, entityID: String, mutableMetas: Seq[companionPropertyData], mutables: Seq[companionPropertyData]): Future[String] = {
      val metas = blockchainMetas.Service.get(mutables.map(_.toProperty).map(_.fact.hash))

      def update(metas: Seq[Meta]) = {
        val updateMutableMetas = Future.traverse(mutableMetas)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.dataName, value = x.dataValue, dataType = x.dataType, isMeta = true, isRevealed = true, isMutable = true, hashID = DataValue.getHash(dataType = x.dataType, dataValue = x.dataValue.getOrElse("")))))
        val updateMutables = Future.traverse(mutables)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.dataName, value = x.dataValue, dataType = x.dataType, isMeta = false, isRevealed = metas.exists(_.id == x.toProperty.fact.hash), isMutable = true, hashID = DataValue.getHash(dataType = x.dataType, dataValue = x.dataValue.getOrElse("")))))
        for {
          _ <- updateMutableMetas
          _ <- updateMutables
        } yield ()
      }

      (for {
        metas <- metas
        _ <- update(metas)
      } yield entityID).recoverWith {
        case baseException: BaseException => throw baseException
      }
    }

    def upsertProperties(entityType: String, entityID: String, immutableMetas: Serializable.MetaProperties, immutables: Serializable.Properties, mutableMetas: Serializable.MetaProperties, mutables: Serializable.Properties): Future[String] = {
      val metas = blockchainMetas.Service.get(immutables.propertyList.map(_.fact.hash) ++ mutables.propertyList.map(_.fact.hash))

      def upsert(metas: Seq[Meta]) = {
        val upsertImmutableMetas = Future.traverse(immutableMetas.metaPropertyList)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.id, value = Option(x.metaFact.data.value.asString), dataType = x.metaFact.data.dataType, isMeta = true, isRevealed = true, isMutable = false, hashID = x.metaFact.data.value.generateHash)))
        val upsertImmutables = Future.traverse(immutables.propertyList)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.id, value = metas.find(_.id == x.id).fold[Option[String]](None)(x => Option(x.dataValue)), dataType = DataValue.getDataTypeFromFactType(x.fact.factType), isMeta = false, isRevealed = metas.exists(_.id == x.id), isMutable = false, hashID = x.fact.hash)))
        val upsertMutableMetas = Future.traverse(mutableMetas.metaPropertyList)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.id, value = Option(x.metaFact.data.value.asString), dataType = x.metaFact.data.dataType, isMeta = true, isRevealed = true, isMutable = true, hashID = x.metaFact.data.value.generateHash)))
        val upsertMutables = Future.traverse(mutables.propertyList)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.id, value = metas.find(_.id == x.id).fold[Option[String]](None)(x => Option(x.dataValue)), dataType = DataValue.getDataTypeFromFactType(x.fact.factType), isMeta = false, isRevealed = metas.exists(_.id == x.id), isMutable = true, hashID = x.fact.hash)))

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

    def updateProperties(entityType: String, entityID: String, mutableMetas: Serializable.MetaProperties, mutables: Serializable.Properties): Future[String] = {
      val metas = blockchainMetas.Service.get(mutables.propertyList.map(_.fact.hash))

      def update(metas: Seq[Meta]) = {
        val updateMutableMetas = Future.traverse(mutableMetas.metaPropertyList)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.id, value = Option(x.metaFact.data.value.asString), dataType = x.metaFact.data.dataType, isMeta = true, isRevealed = true, isMutable = true, hashID = x.metaFact.data.value.generateHash)))
        val updateMutables = Future.traverse(mutables.propertyList)(x => Service.insertOrUpdate(Property(entityID = entityID, entityType = entityType, name = x.id, value = metas.find(_.id == x.id).fold[Option[String]](None)(x => Option(x.dataValue)), dataType = DataValue.getDataTypeFromFactType(x.fact.factType), isMeta = false, isRevealed = metas.exists(_.id == x.id), isMutable = true, hashID = x.fact.hash)))

        for {
          _ <- updateMutableMetas
          _ <- updateMutables
        } yield ()
      }

      (for {
        metas <- metas
        _ <- update(metas)
      } yield entityID).recover {
        case baseException: BaseException => throw baseException
      }
    }

  }

}