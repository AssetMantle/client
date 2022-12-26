package models.blockchain

import exceptions.BaseException
import models.Trait.Logged
import models.common.BaseData.IDData
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

case class Identity(id: IdentityID, immutables: Immutables, mutables: Mutables, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Identities @Inject()(
                            protected val databaseConfigProvider: DatabaseConfigProvider,
                            blockchainIdentityProvisions: IdentityProvisions,
                            blockchainIdentityUnprovisions: IdentityUnprovisions,
                            blockchainMetas: Metas,
                            blockchainClassifications: Classifications,
                            blockchainMaintainers: Maintainers,
                            masterClassifications: master.Classifications,
                            utilitiesOperations: utilities.Operations
                          )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_IDENTITY

  import databaseConfig.profile.api._

  case class IdentitySerialized(classificationID: String, hashID: String, immutables: String, mutables: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) {
    def deserialize: Identity = Identity(id = IdentityID(classificationID = this.classificationID, hashID = this.hashID), immutables = utilities.JSON.convertJsonStringToObject[Immutables](this.immutables), mutables = utilities.JSON.convertJsonStringToObject[Mutables](this.mutables), createdBy = this.createdBy, createdOn = this.createdOn, createdOnTimeZone = this.createdOnTimeZone, updatedBy = this.updatedBy, updatedOn = this.updatedOn, updatedOnTimeZone = this.updatedOnTimeZone)
  }

  def serialize(identity: Identity): IdentitySerialized = IdentitySerialized(classificationID = identity.id.classificationID.asString, hashID = identity.id.hashID, immutables = Json.toJson(identity.immutables).toString, mutables = Json.toJson(identity.mutables).toString, createdBy = identity.createdBy, createdOn = identity.createdOn, createdOnTimeZone = identity.createdOnTimeZone, updatedBy = identity.updatedBy, updatedOn = identity.updatedOn, updatedOnTimeZone = identity.updatedOnTimeZone)

  private[models] val identityTable = TableQuery[IdentityTable]

  private def add(identity: Identity): Future[String] = db.run((identityTable returning identityTable.map(_.hashID) += serialize(identity)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.IDENTITY_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(identities: Seq[Identity]): Future[Seq[String]] = db.run((identityTable returning identityTable.map(_.hashID) ++= identities.map(x => serialize(x))).asTry).map {
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

  private def tryGetByID(classificationID: String, hashID: String) = db.run(identityTable.filter(x => x.classificationID === classificationID && x.hashID === hashID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.IDENTITY_NOT_FOUND, noSuchElementException)
    }
  }

  private def getByID(classificationID: String, hashID: String) = db.run(identityTable.filter(x => x.classificationID === classificationID && x.hashID === hashID).result.headOption)

  private def checkExistsByID(classificationID: String, hashID: String) = db.run(identityTable.filter(x => x.classificationID === classificationID && x.hashID === hashID).exists.result)

  private def deleteByID(classificationID: String, hashID: String): Future[Int] = db.run(identityTable.filter(x => x.classificationID === classificationID && x.hashID === hashID).delete)

  private[models] class IdentityTable(tag: Tag) extends Table[IdentitySerialized](tag, "Identity_BC") {

    def * = (classificationID, hashID, immutables, mutables, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (IdentitySerialized.tupled, IdentitySerialized.unapply)

    def classificationID = column[String]("classificationID", O.PrimaryKey)

    def hashID = column[String]("hashID", O.PrimaryKey)

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

    def tryGet(id: IdentityID): Future[Identity] = tryGetByID(classificationID = id.classificationID.asString, hashID = id.hashID).map(_.deserialize)

    def get(id: IdentityID): Future[Option[Identity]] = getByID(classificationID = id.classificationID.asString, hashID = id.hashID).map(_.map(_.deserialize))

    def insertMultiple(identities: Seq[Identity]): Future[Seq[String]] = addMultiple(identities)

    def checkExists(id: IdentityID): Future[Boolean] = checkExistsByID(classificationID = id.classificationID.asString, hashID = id.hashID)
  }

  object Utility {

    def onDefine(identityDefine: IdentityDefine)(implicit header: Header): Future[Unit] = {
      val scrubbedImmutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(identityDefine.immutableMetaTraits.metaPropertyList)
      val scrubbedMutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(identityDefine.mutableMetaTraits.metaPropertyList)

      def defineAndSuperAuxiliary(scrubbedImmutableMetaProperties: Seq[Property], scrubbedMutableMetaProperties: Seq[Property]) = {
        val mutables = Mutables(Properties(scrubbedMutableMetaProperties ++ identityDefine.mutableTraits.propertyList))
        val defineAuxiliary = blockchainClassifications.Utility.auxiliaryDefine(immutables = Immutables(Properties(scrubbedImmutableMetaProperties ++ identityDefine.immutableTraits.propertyList)), mutables = mutables)

        def superAuxiliary(classificationID: ClassificationID) = blockchainMaintainers.Utility.auxiliarySuper(classificationID = classificationID, identityID = IdentityID(identityDefine.fromID), mutableTraits = mutables)

        for {
          classificationID <- defineAuxiliary
          _ <- superAuxiliary(classificationID)
        } yield classificationID
      }

      def masterOperations(classificationID: ClassificationID) = {
        val insert = masterClassifications.Service.insertOrUpdate(id = classificationID.asString, entityType = constants.Blockchain.Entity.IDENTITY_DEFINITION, maintainerID = identityDefine.fromID, status = Option(true))
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
        val identityID = IdentityID(classificationID = identityIssue.classificationID, hashID = immutables.getHashID)
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
      val nubProperty = blockchainMetas.Utility.auxiliaryScrub(Seq(utilities.Blockchain.getNubMetaProperty(identityNub.nubID)))

      def defineAndUpsert(nubProperty: Property): Future[Identity] = {
        val defineClassification = blockchainClassifications.Utility.auxiliaryDefine(Immutables(Properties(Seq(Property(constants.Blockchain.Properties.NubID, NewFact(constants.Blockchain.FactType.ID, IDData("")))))), Mutables(Properties(Seq())))

        def addIdentityWithProvisionAddress() = addIdentityWithProvisionAddresses(identity = utilities.Blockchain.getNubIdentity(identityNub.nubID), provisionedAddresses = Seq(identityNub.from))

        for {
          classificationID <- defineClassification
          identity <- addIdentityWithProvisionAddress()
        } yield identity
      }

      (for {
        nubProperty <- nubProperty
        identity <- defineAndUpsert(nubProperty.head)
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.IDENTITY_NUB + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def addIdentityWithProvisionAddresses(identity: Identity, provisionedAddresses: Seq[String]): Future[Identity] = {
      val upsert = Service.insertOrUpdate(Identity(id = identity.id, mutables = identity.mutables, immutables = identity.immutables))

      def addProvisions() = if (provisionedAddresses.nonEmpty) utilitiesOperations.traverse(provisionedAddresses) { address => blockchainIdentityProvisions.Service.addProvisionAddress(id = identity.id.asString, address = address) } else Future(Seq())

      (for {
        _ <- upsert
        _ <- addProvisions()
      } yield identity
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }
  }

}