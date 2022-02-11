package models.blockchain

import exceptions.BaseException
import models.Trait.Logged
import models.common.DataValue.IDDataValue
import models.common.Serializable._
import models.common.TransactionMessages.{IdentityDefine, IdentityIssue, IdentityNub}
import models.master
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import queries.responses.common.Header
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Identity(id: String, immutables: Immutables, mutables: Mutables, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {
  def getClassificationID: String = id.split(constants.RegularExpression.BLOCKCHAIN_FIRST_ORDER_COMPOSITE_ID_SEPARATOR)(0)

  def getHashID: String = id.split(constants.RegularExpression.BLOCKCHAIN_FIRST_ORDER_COMPOSITE_ID_SEPARATOR)(1)
}

@Singleton
class Identities @Inject()(
                            protected val databaseConfigProvider: DatabaseConfigProvider,
                            blockchainIdentityProvisions: IdentityProvisions,
                            blockchainIdentityUnprovisions: IdentityUnprovisions,
                            blockchainMetas: Metas,
                            blockchainClassifications: Classifications,
                            blockchainMaintainers: Maintainers,
                            masterClassifications: master.Classifications,
                            masterIdentityNubs: master.IdentityNubs,
                            masterProperties: master.Properties,
                            utilitiesOperations: utilities.Operations
                          )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_IDENTITY

  import databaseConfig.profile.api._

  case class IdentitySerialized(id: String, immutables: String, mutables: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) {
    def deserialize: Identity = Identity(id = this.id, immutables = utilities.JSON.convertJsonStringToObject[Immutables](this.immutables), mutables = utilities.JSON.convertJsonStringToObject[Mutables](this.mutables), createdBy = this.createdBy, createdOn = this.createdOn, createdOnTimeZone = this.createdOnTimeZone, updatedBy = this.updatedBy, updatedOn = this.updatedOn, updatedOnTimeZone = this.updatedOnTimeZone)
  }

  def serialize(identity: Identity): IdentitySerialized = IdentitySerialized(id = identity.id, immutables = Json.toJson(identity.immutables).toString, mutables = Json.toJson(identity.mutables).toString, createdBy = identity.createdBy, createdOn = identity.createdOn, createdOnTimeZone = identity.createdOnTimeZone, updatedBy = identity.updatedBy, updatedOn = identity.updatedOn, updatedOnTimeZone = identity.updatedOnTimeZone)

  private[models] val identityTable = TableQuery[IdentityTable]

  private def add(identity: Identity): Future[String] = db.run((identityTable returning identityTable.map(_.id) += serialize(identity)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.IDENTITY_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(identities: Seq[Identity]): Future[Seq[String]] = db.run((identityTable returning identityTable.map(_.id) ++= identities.map(x => serialize(x))).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.IDENTITY_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(identity: Identity): Future[Int] = db.run(identityTable.insertOrUpdate(serialize(identity)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.IDENTITY_UPSERT_FAILED, psqlException)
    }
  }

  private def tryGetByID(id: String) = db.run(identityTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.IDENTITY_NOT_FOUND, noSuchElementException)
    }
  }

  private def getByID(id: String) = db.run(identityTable.filter(_.id === id).result.headOption)

  private def checkExistsByID(id: String) = db.run(identityTable.filter(_.id === id).exists.result)

  private def deleteByID(id: String): Future[Int] = db.run(identityTable.filter(_.id === id).delete)

  private[models] class IdentityTable(tag: Tag) extends Table[IdentitySerialized](tag, "Identity_BC") {

    def * = (id, immutables, mutables, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (IdentitySerialized.tupled, IdentitySerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

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

    def insertOrUpdate(identity: Identity): Future[Int] = upsert(identity)

    def tryGet(id: String): Future[Identity] = tryGetByID(id).map(_.deserialize)

    def get(id: String): Future[Option[Identity]] = getByID(id).map(_.map(_.deserialize))

    def insertMultiple(identities: Seq[Identity]): Future[Seq[String]] = addMultiple(identities)

    def checkExists(id: String): Future[Boolean] = checkExistsByID(id)
  }

  object Utility {

    def onDefine(identityDefine: IdentityDefine)(implicit header: Header): Future[Unit] = {
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
        val insert = masterClassifications.Service.insertOrUpdate(id = classificationID, entityType = constants.Blockchain.Entity.IDENTITY_DEFINITION, maintainerID = identityDefine.fromID, status = Option(true))
        for {
          _ <- insert
        } yield ()
      }

      (for {
        scrubbedImmutableMetaProperties <- scrubbedImmutableMetaProperties
        scrubbedMutableMetaProperties <- scrubbedMutableMetaProperties
        classificationID <- defineAndSuperAuxiliary(scrubbedImmutableMetaProperties = scrubbedImmutableMetaProperties, scrubbedMutableMetaProperties = scrubbedMutableMetaProperties)
        _ <- masterOperations(classificationID)
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.IDENTITY_DEFINE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def onIssue(identityIssue: IdentityIssue)(implicit header: Header): Future[Unit] = {
      val scrubbedImmutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(identityIssue.immutableMetaProperties.metaPropertyList)
      val scrubbedMutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(identityIssue.mutableMetaProperties.metaPropertyList)

      def insertOrUpdate(scrubbedImmutableMetaProperties: Seq[Property], scrubbedMutableMetaProperties: Seq[Property]) = {
        val immutables = Immutables(Properties(scrubbedImmutableMetaProperties ++ identityIssue.immutableProperties.propertyList))
        val identityID = utilities.IDGenerator.getIdentityID(classificationID = identityIssue.classificationID, immutables = immutables)
        val addIdentityWithProvisionAddress = addIdentityWithProvisionAddresses(identity = Identity(id = identityID, mutables = Mutables(Properties(scrubbedMutableMetaProperties ++ identityIssue.mutableProperties.propertyList)), immutables = immutables), provisionedAddresses = Seq(identityIssue.to))

        for {
          _ <- addIdentityWithProvisionAddress
        } yield identityID
      }

      (for {
        scrubbedImmutableMetaProperties <- scrubbedImmutableMetaProperties
        scrubbedMutableMetaProperties <- scrubbedMutableMetaProperties
        identityID <- insertOrUpdate(scrubbedImmutableMetaProperties = scrubbedImmutableMetaProperties, scrubbedMutableMetaProperties = scrubbedMutableMetaProperties)
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.IDENTITY_ISSUE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def onNub(identityNub: IdentityNub)(implicit header: Header): Future[Unit] = {
      val nubProperty = blockchainMetas.Utility.auxiliaryScrub(Seq(getNubMetaProperty(identityNub.nubID)))

      def defineAndUpsert(nubProperty: Property) = {
        val immutables = Immutables(Properties(Seq(nubProperty)))
        val mutables = Mutables(Properties(Seq()))
        val defineClassification = blockchainClassifications.Utility.auxiliaryDefine(Immutables(Properties(Seq(Property(constants.Blockchain.Properties.NubID, NewFact(constants.Blockchain.FactType.ID, IDDataValue("")))))), mutables)

        def getIdentityID(classificationID: String) = Future(utilities.IDGenerator.getIdentityID(classificationID = classificationID, immutables = immutables))

        def addIdentityWithProvisionAddress(identityID: String) = addIdentityWithProvisionAddresses(identity = Identity(id = identityID, immutables = immutables, mutables = mutables), provisionedAddresses = Seq(identityNub.from))

        for {
          classificationID <- defineClassification
          identityID <- getIdentityID(classificationID)
          _ <- addIdentityWithProvisionAddress(identityID)
        } yield (classificationID, identityID)
      }

      def masterUpdates(identityID: String) = masterIdentityNubs.Utility.onIdentityNubTx(identityID = identityID, nubID = identityNub.nubID, creatorAddress = identityNub.from)

      (for {
        nubProperty <- nubProperty
        (classificationID, identityID) <- defineAndUpsert(nubProperty.head)
        _ <- masterUpdates(identityID)
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.IDENTITY_NUB + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def addIdentityWithProvisionAddresses(identity: Identity, provisionedAddresses: Seq[String]): Future[Unit] = {
      val upsert = Service.insertOrUpdate(Identity(id = identity.id, mutables = identity.mutables, immutables = identity.immutables))

      def addProvisions() = if (provisionedAddresses.nonEmpty) utilitiesOperations.traverse(provisionedAddresses) { address => blockchainIdentityProvisions.Service.addProvisionAddress(id = identity.id, address = address) } else Future(Seq())

      (for {
        _ <- upsert
        _ <- addProvisions()
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def getNubMetaProperty(nubID: String): MetaProperty = MetaProperty(id = constants.Blockchain.Properties.NubID, metaFact = MetaFact(Data(dataType = constants.Blockchain.DataType.ID_DATA, value = IDDataValue(nubID))))
  }

}