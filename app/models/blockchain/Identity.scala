package models.blockchain

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.DataValue.IDDataValue
import models.common.Serializable._
import models.common.TransactionMessages.{IdentityDefine, IdentityIssue, IdentityNub, IdentityProvision, IdentityUnprovision}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.GetIdentity
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Identity(id: String, provisionedAddressList: Seq[String], unprovisionedAddressList: Seq[String], immutables: Immutables, mutables: Mutables, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {
  def getClassificationID: String = id.split(constants.RegularExpression.BLOCKCHAIN_ID_SEPARATOR)(0)

  def getHashID: String = id.split(constants.RegularExpression.BLOCKCHAIN_ID_SEPARATOR)(1)
}

@Singleton
class Identities @Inject()(
                            protected val databaseConfigProvider: DatabaseConfigProvider,
                            configuration: Configuration,
                            getIdentity: GetIdentity,
                            blockchainMetas: Metas,
                            blockchainClassifications: Classifications,
                            blockchainMaintainers: Maintainers,
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

  private def getAllIdentities = db.run(identityTable.result)

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
  }

  object Utility {

    private val chainID = configuration.get[String]("blockchain.main.chainID")

    def onDefine(identityDefine: IdentityDefine): Future[Unit] = {
      val scrubbedImmutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(identityDefine.immutableMetaTraits.metaPropertyList)
      val scrubbedMutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(identityDefine.mutableMetaTraits.metaPropertyList)

      def defineAndSuperAuxiliary(scrubbedImmutableMetaProperties: Seq[Property], scrubbedMutableMetaProperties: Seq[Property]) = {
        val mutables = Mutables(Properties(scrubbedMutableMetaProperties ++ identityDefine.mutableTraits.propertyList))
        val defineAuxiliary = blockchainClassifications.Utility.auxiliaryDefine(immutables = Immutables(Properties(scrubbedImmutableMetaProperties ++ identityDefine.immutableTraits.propertyList)), mutables = mutables)

        def superAuxiliary(classificationID: String) = blockchainMaintainers.Utility.auxiliarySuper(classificationID = classificationID, identityID = identityDefine.fromID, mutableTraits = mutables)

        for {
          classificationID <- defineAuxiliary
          _ <- superAuxiliary(classificationID = classificationID)
        } yield classificationID
      }

      (for {
        scrubbedImmutableMetaProperties <- scrubbedImmutableMetaProperties
        scrubbedMutableMetaProperties <- scrubbedMutableMetaProperties
        _ <- defineAndSuperAuxiliary(scrubbedImmutableMetaProperties = scrubbedImmutableMetaProperties, scrubbedMutableMetaProperties = scrubbedMutableMetaProperties)
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
        Service.insertOrUpdate(Identity(id = getID(classificationID = identityIssue.classificationID, immutables = immutables), provisionedAddressList = Seq(identityIssue.to), unprovisionedAddressList = Seq.empty[String], mutables = Mutables(Properties(scrubbedMutableMetaProperties ++ identityIssue.mutableProperties.propertyList)), immutables = immutables)
        )
      }

      (for {
        scrubbedImmutableMetaProperties <- scrubbedImmutableMetaProperties
        scrubbedMutableMetaProperties <- scrubbedMutableMetaProperties
        _ <- insertOrUpdate(scrubbedImmutableMetaProperties = scrubbedImmutableMetaProperties, scrubbedMutableMetaProperties = scrubbedMutableMetaProperties)
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
      val nubProperty = blockchainMetas.Utility.auxiliaryScrub(Seq(MetaProperty(id = constants.Blockchain.Properties.NubID, metaFact = MetaFact(Data(dataType = constants.Blockchain.DataType.ID_DATA, value = IDDataValue(identityNub.nubID))))))

      def defineAndUpsert(nubProperty: Property) = {
        val immutables = Immutables(Properties(Seq(nubProperty)))
        val mutables = Mutables(Properties(Seq()))
        //While giving Immutables to auxiliaryDefine, do not use NewFact(""), instead directly use Fact(""). This is because, in BC, NewMetaFact is used in Property NubID with "". So when hash is evaluated in auxiliaryDefine in BC, hash of "" is evaluated. Using NewFact will lead to computing hash of "" one more time compared to BC.
        val defineClassification = blockchainClassifications.Utility.auxiliaryDefine(Immutables(Properties(Seq(Property(constants.Blockchain.Properties.NubID, Fact(""))))), mutables)

        def upsert(classificationID: String) = Service.insertOrUpdate(Identity(id = getID(classificationID = classificationID, immutables = immutables), provisionedAddressList = Seq(identityNub.from), unprovisionedAddressList = Seq.empty[String], immutables = immutables, mutables = mutables))

        for {
          classificationID <- defineClassification
          _ <- upsert(classificationID)
        } yield ()
      }

      (for {
        nubProperty <- nubProperty
        _ <- defineAndUpsert(nubProperty.head)
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    private def getID(classificationID: String, immutables: Immutables) = Seq(classificationID, immutables.getHashID).mkString(constants.Blockchain.FirstOrderCompositeIDSeparator)

  }

}