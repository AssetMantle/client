package models.blockchain

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.DataValue.IDDataValue
import models.common.Serializable._
import models.common.TransactionMessages.{IdentityDefine, IdentityIssue, IdentityNub, IdentityProvision, IdentityUnprovision}
import models.master
import models.master.{Classification => masterClassification, Identity => masterIdentity}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.GetIdentity
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Identity(id: String, provisionedAddressList: Seq[String], unprovisionedAddressList: Seq[String], immutables: Immutables, mutables: Mutables, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {
  def getClassificationID: String = id.split(constants.RegularExpression.BLOCKCHAIN_FIRST_ORDER_COMPOSITE_ID_SEPARATOR)(0)

  def getHashID: String = id.split(constants.RegularExpression.BLOCKCHAIN_FIRST_ORDER_COMPOSITE_ID_SEPARATOR)(1)
}

@Singleton
class Identities @Inject()(
                            protected val databaseConfigProvider: DatabaseConfigProvider,
                            configuration: Configuration,
                            getIdentity: GetIdentity,
                            blockchainMetas: Metas,
                            blockchainClassifications: Classifications,
                            blockchainMaintainers: Maintainers,
                            masterClassifications: master.Classifications,
                            masterIdentities: master.Identities,
                            masterProperties: master.Properties,
                          )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_IDENTITY

  import databaseConfig.profile.api._

  case class IdentitySerialized(id: String, provisionedAddressList: String, unprovisionedAddressList: String, immutables: String, mutables: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Identity = Identity(id = id, provisionedAddressList = utilities.JSON.convertJsonStringToObject[Seq[String]](provisionedAddressList), unprovisionedAddressList = utilities.JSON.convertJsonStringToObject[Seq[String]](unprovisionedAddressList), immutables = utilities.JSON.convertJsonStringToObject[Immutables](immutables), mutables = utilities.JSON.convertJsonStringToObject[Mutables](mutables), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(identity: Identity): IdentitySerialized = IdentitySerialized(id = identity.id, provisionedAddressList = Json.toJson(identity.provisionedAddressList).toString, unprovisionedAddressList = Json.toJson(identity.unprovisionedAddressList).toString, immutables = Json.toJson(identity.immutables).toString, mutables = Json.toJson(identity.mutables).toString, createdBy = identity.createdBy, createdOn = identity.createdOn, createdOnTimeZone = identity.createdOnTimeZone, updatedBy = identity.updatedBy, updatedOn = identity.updatedOn, updatedOnTimeZone = identity.updatedOnTimeZone)

  private[models] val identityTable = TableQuery[IdentityTable]

  private def add(identity: Identity): Future[String] = db.run((identityTable returning identityTable.map(_.id) += serialize(identity)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ASSET_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(identities: Seq[Identity]): Future[Seq[String]] = db.run((identityTable returning identityTable.map(_.id) ++= identities.map(x => serialize(x))).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ASSET_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(identity: Identity): Future[Int] = db.run(identityTable.insertOrUpdate(serialize(identity)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ASSET_UPSERT_FAILED, psqlException)
    }
  }

  private def tryGetByID(id: String) = db.run(identityTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ASSET_NOT_FOUND, noSuchElementException)
    }
  }

  private def getByID(id: String) = db.run(identityTable.filter(_.id === id).result.headOption)

  private def checkExistsByID(id: String) = db.run(identityTable.filter(_.id === id).exists.result)

  private def getAllIdentities = db.run(identityTable.result)

  private def getAllIdentityIDsByProvisionedAddress(address: String) = db.run(identityTable.filter(_.provisionedAddressList.like(s"""%$address%""")).map(_.id).result)

  private def getAllIdentitiesByProvisionedAddress(address: String) = db.run(identityTable.filter(_.provisionedAddressList.like(s"""%$address%""")).result)

  private def getAllIdentityIDsByUnprovisionedAddress(address: String) = db.run(identityTable.filter(_.unprovisionedAddressList.like(s"""%$address%""")).map(_.id).result)

  private def getAllIdentitiesByUnprovisionedAddress(address: String) = db.run(identityTable.filter(_.unprovisionedAddressList.like(s"""%$address%""")).result)

  private def getAllProvisionedAddressByID(id: String) = db.run(identityTable.filter(_.id === id).map(_.provisionedAddressList).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ASSET_NOT_FOUND, noSuchElementException)
    }
  }

  private def getAllUnprovisionedAddressByID(id: String) = db.run(identityTable.filter(_.id === id).map(_.unprovisionedAddressList).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ASSET_NOT_FOUND, noSuchElementException)
    }
  }

  private def deleteByID(id: String): Future[Int] = db.run(identityTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ASSET_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ASSET_DELETE_FAILED, noSuchElementException)
    }
  }

  private[models] class IdentityTable(tag: Tag) extends Table[IdentitySerialized](tag, "Identity_BC") {

    def * = (id, provisionedAddressList, unprovisionedAddressList, immutables, mutables, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (IdentitySerialized.tupled, IdentitySerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def provisionedAddressList = column[String]("provisionedAddressList")

    def unprovisionedAddressList = column[String]("unprovisionedAddressList")

    def immutables = column[String]("immutables")

    def mutables = column[String]("mutables")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(identity: Identity): Future[String] = add(identity)

    def tryGet(id: String): Future[Identity] = tryGetByID(id).map(_.deserialize)

    def get(id: String): Future[Option[Identity]] = getByID(id).map(_.map(_.deserialize))

    def getAll: Future[Seq[Identity]] = getAllIdentities.map(_.map(_.deserialize))

    def insertMultiple(identities: Seq[Identity]): Future[Seq[String]] = addMultiple(identities)

    def insertOrUpdate(identity: Identity): Future[Int] = upsert(identity)

    def delete(id: String): Future[Int] = deleteByID(id)

    def getAllIDsByProvisioned(address: String): Future[Seq[String]] = getAllIdentityIDsByProvisionedAddress(address)

    def getAllIDsByUnprovisioned(address: String): Future[Seq[String]] = getAllIdentityIDsByUnprovisionedAddress(address)

    def getAllByProvisioned(address: String): Future[Seq[Identity]] = getAllIdentitiesByProvisionedAddress(address).map(_.map(_.deserialize))

    def checkExists(id: String): Future[Boolean] = checkExistsByID(id)

    def getAllProvisionAddresses(id: String): Future[Seq[String]] = getAllProvisionedAddressByID(id).map(x => utilities.JSON.convertJsonStringToObject[Seq[String]](x))

    def getAllUnprovisionAddresses(id: String): Future[Seq[String]] = getAllUnprovisionedAddressByID(id).map(x => utilities.JSON.convertJsonStringToObject[Seq[String]](x))
  }

  object Utility {

    private val chainID = configuration.get[String]("blockchain.chainID")

    def onDefine(identityDefine: IdentityDefine): Future[Unit] = {
      val scrubbedImmutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(identityDefine.immutableMetaTraits.metaPropertyList)
      val scrubbedMutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(identityDefine.mutableMetaTraits.metaPropertyList)

      def defineAndSuperAuxiliary(scrubbedImmutableMetaProperties: Seq[Property], scrubbedMutableMetaProperties: Seq[Property]) = {
        val mutables = Mutables(Properties(scrubbedMutableMetaProperties ++ identityDefine.mutableTraits.propertyList))
        val defineAuxiliary = blockchainClassifications.Utility.auxiliaryDefine(immutables = Immutables(Properties(scrubbedImmutableMetaProperties ++ identityDefine.immutableTraits.propertyList)), mutables = mutables)

        def superAuxiliary(classificationID: String) = blockchainMaintainers.Utility.auxiliarySuper(classificationID = classificationID, identityID = identityDefine.fromID, mutableTraits = mutables)

        for {
          classificationID <- defineAuxiliary
          _ <- superAuxiliary(classificationID)
        } yield classificationID
      }

      def masterOperations(classificationID: String) = {
        val classification = masterClassifications.Service.get(classificationID)

        def insertProperties(classification: Option[masterClassification]) = if (classification.isEmpty) masterProperties.Utilities.upsertProperties(entityType = constants.Blockchain.Entity.IDENTITY_DEFINITION, entityID = classificationID, immutableMetas = identityDefine.immutableMetaTraits, immutables = identityDefine.immutableTraits, mutableMetas = identityDefine.mutableMetaTraits, mutables = identityDefine.mutableTraits) else Future("")

        def upsert(classification: Option[masterClassification]) = classification.fold(masterClassifications.Service.insertOrUpdate(id = classificationID, entityType = constants.Blockchain.Entity.IDENTITY_DEFINITION, fromID = identityDefine.fromID, label = None, status = Option(true)))(_ => masterClassifications.Service.markStatusSuccessful(id = classificationID, entityType = constants.Blockchain.Entity.IDENTITY_DEFINITION))

        for {
          classification <- classification
          _ <- upsert(classification)
          _ <- insertProperties(classification)
        } yield ()
      }

      (for {
        scrubbedImmutableMetaProperties <- scrubbedImmutableMetaProperties
        scrubbedMutableMetaProperties <- scrubbedMutableMetaProperties
        classificationID <- defineAndSuperAuxiliary(scrubbedImmutableMetaProperties = scrubbedImmutableMetaProperties, scrubbedMutableMetaProperties = scrubbedMutableMetaProperties)
        _ <- masterOperations(classificationID)
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onIssue(identityIssue: IdentityIssue): Future[Unit] = {
      val scrubbedImmutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(identityIssue.immutableMetaProperties.metaPropertyList)
      val scrubbedMutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(identityIssue.mutableMetaProperties.metaPropertyList)

      def insertOrUpdate(scrubbedImmutableMetaProperties: Seq[Property], scrubbedMutableMetaProperties: Seq[Property]) = {
        val immutables = Immutables(Properties(scrubbedImmutableMetaProperties ++ identityIssue.immutableProperties.propertyList))
        val identityID = getID(classificationID = identityIssue.classificationID, immutables = immutables)
        val upsert = Service.insertOrUpdate(Identity(id = identityID, provisionedAddressList = Seq(identityIssue.to), unprovisionedAddressList = Seq.empty[String], mutables = Mutables(Properties(scrubbedMutableMetaProperties ++ identityIssue.mutableProperties.propertyList)), immutables = immutables))

        for {
          _ <- upsert
        } yield identityID
      }

      def masterOperations(identityID: String) = {
        val identity = masterIdentities.Service.get(identityID)

        def insertProperties(identity: Option[masterIdentity]) = if (identity.isEmpty) masterProperties.Utilities.upsertProperties(entityType = constants.Blockchain.Entity.IDENTITY, entityID = identityID, immutableMetas = identityIssue.immutableMetaProperties, immutables = identityIssue.immutableProperties, mutableMetas = identityIssue.mutableMetaProperties, mutables = identityIssue.mutableProperties) else Future("")

        def upsert(identity: Option[masterIdentity]) = identity.fold(masterIdentities.Service.insertOrUpdate(masterIdentity(id = identityID, label = None, status = Option(true))))(x => masterIdentities.Service.insertOrUpdate(x.copy(status = Option(true))))

        for {
          identity <- identity
          _ <- upsert(identity)
          _ <- insertProperties(identity)
        } yield ()
      }

      (for {
        scrubbedImmutableMetaProperties <- scrubbedImmutableMetaProperties
        scrubbedMutableMetaProperties <- scrubbedMutableMetaProperties
        identityID <- insertOrUpdate(scrubbedImmutableMetaProperties = scrubbedImmutableMetaProperties, scrubbedMutableMetaProperties = scrubbedMutableMetaProperties)
        _ <- masterOperations(identityID)
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onProvision(identityProvision: IdentityProvision): Future[Unit] = {
      val oldIdentity = Service.tryGet(identityProvision.identityID)

      def update(oldIdentity: Identity) = Service.insertOrUpdate(oldIdentity.copy(provisionedAddressList = oldIdentity.provisionedAddressList :+ identityProvision.to))

      (for {
        oldIdentity <- oldIdentity
        _ <- update(oldIdentity)
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onUnprovision(identityUnprovision: IdentityUnprovision): Future[Unit] = {
      val oldIdentity = Service.tryGet(identityUnprovision.identityID)

      def update(oldIdentity: Identity) = Service.insertOrUpdate(oldIdentity.copy(provisionedAddressList = oldIdentity.provisionedAddressList.filterNot(_ == identityUnprovision.to), unprovisionedAddressList = oldIdentity.unprovisionedAddressList :+ identityUnprovision.to))

      (for {
        oldIdentity <- oldIdentity
        _ <- update(oldIdentity)
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onNub(identityNub: IdentityNub): Future[Unit] = {
      val nubMetaProperty = MetaProperty(id = constants.Blockchain.Properties.NubID, metaFact = MetaFact(Data(dataType = constants.Blockchain.DataType.ID_DATA, value = IDDataValue(identityNub.nubID))))
      val nubProperty = blockchainMetas.Utility.auxiliaryScrub(Seq(nubMetaProperty))

      def defineAndUpsert(nubProperty: Property) = {
        val immutables = Immutables(Properties(Seq(nubProperty)))
        val mutables = Mutables(Properties(Seq()))
        val defineClassification = blockchainClassifications.Utility.auxiliaryDefine(Immutables(Properties(Seq(Property(constants.Blockchain.Properties.NubID, NewFact(constants.Blockchain.FactType.ID, IDDataValue("")))))), mutables)

        def getIdentityID(classificationID: String) = Future(getID(classificationID = classificationID, immutables = immutables))

        def upsert(identityID: String) = Service.insertOrUpdate(Identity(id = identityID, provisionedAddressList = Seq(identityNub.from), unprovisionedAddressList = Seq.empty[String], immutables = immutables, mutables = mutables))

        for {
          classificationID <- defineClassification
          identityID <- getIdentityID(classificationID)
          _ <- upsert(identityID)
        } yield (classificationID, identityID)
      }

      def masterOperations(classificationID: String, identityID: String) = {
        val identity = masterIdentities.Service.get(identityID)
        val classification = masterClassifications.Service.get(classificationID)

        def insertIdentityProperties(identity: Option[masterIdentity]) = if (identity.isEmpty) masterProperties.Utilities.upsertProperties(entityType = constants.Blockchain.Entity.IDENTITY, entityID = identityID, immutableMetas = MetaProperties(Seq(nubMetaProperty)), immutables = Properties(Seq.empty), mutableMetas = MetaProperties(Seq.empty), mutables = Properties(Seq.empty)) else Future("")

        def upsertIdentity(identity: Option[masterIdentity]) = identity.fold(masterIdentities.Service.insertOrUpdate(masterIdentity(id = identityID, label = None, status = Option(true))))(x => masterIdentities.Service.insertOrUpdate(x.copy(status = Option(true))))

        def insertClassificationProperties(classification: Option[masterClassification]) = if (classification.isEmpty) masterProperties.Utilities.upsertProperties(entityType = constants.Blockchain.Entity.IDENTITY_DEFINITION, entityID = classificationID, immutableMetas = MetaProperties(Seq(nubMetaProperty)), immutables = Properties(Seq.empty), mutableMetas = MetaProperties(Seq.empty), mutables = Properties(Seq.empty)) else Future("")

        def upsertClassification(classification: Option[masterClassification]) = classification.fold(masterClassifications.Service.insertOrUpdate(id = classificationID, entityType = constants.Blockchain.Entity.IDENTITY_DEFINITION, fromID = identityID, label = None, status = Option(true)))(x => masterClassifications.Service.markStatusSuccessful(id = classificationID, entityType = constants.Blockchain.Entity.IDENTITY_DEFINITION))

        for {
          identity <- identity
          classification <- classification
          _ <- upsertIdentity(identity)
          _ <- insertIdentityProperties(identity)
          _ <- upsertClassification(classification)
          _ <- insertClassificationProperties(classification)
        } yield ()
      }

      (for {
        nubProperty <- nubProperty
        (classificationID, identityID) <- defineAndUpsert(nubProperty.head)
        _ <- masterOperations(classificationID = classificationID, identityID = identityID)
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def getID(classificationID: String, immutables: Immutables): String = Seq(classificationID, immutables.getHashID).mkString(constants.Blockchain.FirstOrderCompositeIDSeparator)

  }

}