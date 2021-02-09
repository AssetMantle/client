package models.blockchain

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.DataValue.IDDataValue
import models.common.Serializable._
import models.common.TransactionMessages.{IdentityDefine, IdentityIssue, IdentityNub, IdentityProvision, IdentityUnprovision}
import models.master.{Identity => masterIdentity}
import models.{blockchain, blockchainTransaction, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import utilities.MicroNumber

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
                            blockchainMetas: Metas,
                            masterZones: master.Zones,
                            blockchainClassifications: Classifications,
                            blockchainMaintainers: Maintainers,
                            masterOrganizations: master.Organizations,
                            masterAccounts: master.Accounts,
                            blockchainAccounts: blockchain.Accounts,
                            masterClassifications: master.Classifications,
                            masterIdentities: master.Identities,
                            masterTraders: master.Traders,
                            transactionsMaintainerDeputize: transactions.blockchain.MaintainerDeputize,
                            blockchainTransactionMaintainerDeputizes: blockchainTransaction.MaintainerDeputizes,
                            masterProperties: master.Properties,
                            transaction: utilities.Transaction,
                            utilitiesOperations: utilities.Operations
                          )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.BLOCKCHAIN_IDENTITY

  import databaseConfig.profile.api._

  case class IdentityProvisionedSerialized(id: String, address: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None)

  case class IdentityUnprovisionedSerialized(id: String, address: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None)

  case class IdentityPropertiesSerialized(id: String, immutables: String, mutables: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None)

  def deserialize(identityPropertiesSerialized: IdentityPropertiesSerialized, identityProvisionedSerialized: Seq[IdentityProvisionedSerialized], identityUnprovisionedSerialized: Seq[IdentityUnprovisionedSerialized]): Identity = {
    val provisionedSerializedIDs = identityProvisionedSerialized.map(_.id).distinct
    val unprovisionedSerializedIDs = identityUnprovisionedSerialized.map(_.id).distinct

    if ((provisionedSerializedIDs.isEmpty || (provisionedSerializedIDs.length == 1 && provisionedSerializedIDs.head == identityPropertiesSerialized.id)) && (unprovisionedSerializedIDs.isEmpty || (unprovisionedSerializedIDs.length == 1 && unprovisionedSerializedIDs.head == identityPropertiesSerialized.id))) {
      Identity(id = identityPropertiesSerialized.id, provisionedAddressList = identityProvisionedSerialized.map(_.address), unprovisionedAddressList = identityUnprovisionedSerialized.map(_.address), immutables = utilities.JSON.convertJsonStringToObject[Immutables](identityPropertiesSerialized.immutables), mutables = utilities.JSON.convertJsonStringToObject[Mutables](identityPropertiesSerialized.mutables), createdBy = identityPropertiesSerialized.createdBy, createdOn = identityPropertiesSerialized.createdOn, createdOnTimeZone = identityPropertiesSerialized.createdOnTimeZone, updatedBy = None, updatedOn = None, updatedOnTimeZone = None)
    } else throw new BaseException(constants.Response.INVALID_IDENTITY)

  }

  def provisionAddressesSerialized(identity: Identity): Seq[IdentityProvisionedSerialized] = identity.provisionedAddressList.map(x => IdentityProvisionedSerialized(id = identity.id, address = x, createdBy = identity.createdBy, createdOn = identity.createdOn, createdOnTimeZone = identity.createdOnTimeZone, updatedBy = identity.updatedBy, updatedOn = identity.updatedOn, updatedOnTimeZone = identity.updatedOnTimeZone))

  def unprovisionAddressesSerialized(identity: Identity): Seq[IdentityUnprovisionedSerialized] = identity.unprovisionedAddressList.map(x => IdentityUnprovisionedSerialized(id = identity.id, address = x, createdBy = identity.createdBy, createdOn = identity.createdOn, createdOnTimeZone = identity.createdOnTimeZone, updatedBy = identity.updatedBy, updatedOn = identity.updatedOn, updatedOnTimeZone = identity.updatedOnTimeZone))

  def propertiesSerialized(identity: Identity): IdentityPropertiesSerialized = IdentityPropertiesSerialized(id = identity.id, immutables = Json.toJson(identity.immutables).toString, mutables = Json.toJson(identity.mutables).toString, createdBy = identity.createdBy, createdOn = identity.createdOn, createdOnTimeZone = identity.createdOnTimeZone, updatedBy = identity.updatedBy, updatedOn = identity.updatedOn, updatedOnTimeZone = identity.updatedOnTimeZone)

  private[models] val identityProvisionedTable = TableQuery[IdentityProvisionTable]

  private[models] val identityUnprovisionedTable = TableQuery[IdentityUnprovisionTable]

  private[models] val identityPropertiesTable = TableQuery[IdentityPropertiesTable]

  private def addProvisionedAddressByID(id: String, address: String): Future[String] = db.run((identityProvisionedTable returning identityProvisionedTable.map(_.id) += IdentityProvisionedSerialized(id = id, address = address)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.IDENTITY_PROVISIONED_ADDRESS_INSERT_FAILED, psqlException)
    }
  }

  private def addUnprovisionedAddressByID(id: String, address: String): Future[String] = db.run((identityUnprovisionedTable returning identityUnprovisionedTable.map(_.id) += IdentityUnprovisionedSerialized(id = id, address = address)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.IDENTITY_UNPROVISIONED_ADDRESS_INSERT_FAILED, psqlException)
    }
  }

  private def addMultipleProvisionedAddresses(identity: Identity): Future[Seq[String]] = db.run((identityProvisionedTable returning identityProvisionedTable.map(_.id) ++= provisionAddressesSerialized(identity)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.IDENTITY_PROVISIONED_ADDRESS_INSERT_FAILED, psqlException)
    }
  }

  private def addMultipleUnprovisionedAddresses(identity: Identity): Future[Seq[String]] = db.run((identityUnprovisionedTable returning identityUnprovisionedTable.map(_.id) ++= unprovisionAddressesSerialized(identity)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.IDENTITY_UNPROVISIONED_ADDRESS_INSERT_FAILED, psqlException)
    }
  }

  private def upsertProperty(identity: Identity): Future[Int] = db.run(identityPropertiesTable.insertOrUpdate(propertiesSerialized(identity)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.IDENTITY_UPSERT_FAILED, psqlException)
    }
  }

  private def tryGetPropertiesByID(id: String) = db.run(identityPropertiesTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.IDENTITY_NOT_FOUND, noSuchElementException)
    }
  }

  private def getPropertiesByID(id: String) = db.run(identityPropertiesTable.filter(_.id === id).result.headOption)

  private def checkExistsByID(id: String) = db.run(identityPropertiesTable.filter(_.id === id).exists.result)

  private def getAllIdentityIDsByProvisionedAddress(address: String) = db.run(identityProvisionedTable.filter(_.address === address).map(_.id).result)

  private def getAllIdentityIDsByUnprovisionedAddress(address: String) = db.run(identityUnprovisionedTable.filter(_.address === address).map(_.id).result)

  private def getAllProvisionedAddressByID(id: String) = db.run(identityProvisionedTable.filter(_.id === id).map(_.address).result)

  private def getAllUnprovisionedAddressByID(id: String) = db.run(identityUnprovisionedTable.filter(_.id === id).map(_.address).result)

  private def deleteAllProvisionedAddressesByID(id: String): Future[Int] = db.run(identityProvisionedTable.filter(_.id === id).delete)

  private def deleteAllUnprovisionedAddressesByID(id: String): Future[Int] = db.run(identityUnprovisionedTable.filter(_.id === id).delete)

  private def deletePropertiesByID(id: String): Future[Int] = db.run(identityPropertiesTable.filter(_.id === id).delete)

  private def deleteProvisionedAddressByIDAndAddress(id: String, address: String): Future[Int] = db.run(identityProvisionedTable.filter(x => x.id === id && x.address === address).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.IDENTITY_PROVISIONED_ADDRESS_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.IDENTITY_PROVISIONED_ADDRESS_DELETE_FAILED, noSuchElementException)
    }
  }

  private def deleteUnprovisionedAddressByIDAndAddress(id: String, address: String): Future[Int] = db.run(identityUnprovisionedTable.filter(x => x.id === id && x.address === address).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.IDENTITY_UNPROVISIONED_ADDRESS_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.IDENTITY_UNPROVISIONED_ADDRESS_DELETE_FAILED, noSuchElementException)
    }
  }

  private[models] class IdentityProvisionTable(tag: Tag) extends Table[IdentityProvisionedSerialized](tag, "IdentityProvisioned_BC") {

    def * = (id, address, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (IdentityProvisionedSerialized.tupled, IdentityProvisionedSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def address = column[String]("address", O.PrimaryKey)

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  private[models] class IdentityUnprovisionTable(tag: Tag) extends Table[IdentityUnprovisionedSerialized](tag, "IdentityUnprovisioned_BC") {

    def * = (id, address, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (IdentityUnprovisionedSerialized.tupled, IdentityUnprovisionedSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def address = column[String]("address", O.PrimaryKey)

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  private[models] class IdentityPropertiesTable(tag: Tag) extends Table[IdentityPropertiesSerialized](tag, "IdentityProperties_BC") {

    def * = (id, immutables, mutables, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (IdentityPropertiesSerialized.tupled, IdentityPropertiesSerialized.unapply)

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

    def create(identity: Identity): Future[String] = {
      val insertProperties = upsertProperty(identity)

      //Should be called after inserting properties due to FK constraint.
      def insertAddress() = {
        val insertProvisioned = addMultipleProvisionedAddresses(identity)
        val insertUnporvisioned = addMultipleUnprovisionedAddresses(identity)
        for {
          _ <- insertProvisioned
          _ <- insertUnporvisioned
        } yield ()
      }

      (for {
        _ <- insertProperties
        _ <- insertAddress()
      } yield identity.id
        ).recoverWith {
        case baseException: BaseException =>
          for {
            _ <- delete(identity.id)
          } yield throw baseException
      }
    }

    def tryGet(id: String): Future[Identity] = {
      val property = tryGetPropertiesByID(id)
      val provisionedAddresses = getAllProvisionAddresses(id)
      val unprovisionedAddresses = getAllUnprovisionAddresses(id)
      (for {
        property <- property
        provisionedAddresses <- provisionedAddresses
        unprovisionedAddresses <- unprovisionedAddresses
      } yield Identity(id = id, provisionedAddressList = provisionedAddresses, unprovisionedAddressList = unprovisionedAddresses, mutables = utilities.JSON.convertJsonStringToObject[Mutables](property.mutables), immutables = utilities.JSON.convertJsonStringToObject[Immutables](property.immutables), createdBy = property.createdBy, createdOn = property.createdOn, createdOnTimeZone = property.createdOnTimeZone)
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def get(id: String): Future[Option[Identity]] = {
      val property = getPropertiesByID(id)
      val provisionedAddresses = getAllProvisionAddresses(id)
      val unprovisionedAddresses = getAllUnprovisionAddresses(id)
      (for {
        property <- property
        provisionedAddresses <- provisionedAddresses
        unprovisionedAddresses <- unprovisionedAddresses
      } yield property.fold[Option[Identity]](None)(property => Option(Identity(id = id, provisionedAddressList = provisionedAddresses, unprovisionedAddressList = unprovisionedAddresses, mutables = utilities.JSON.convertJsonStringToObject[Mutables](property.mutables), immutables = utilities.JSON.convertJsonStringToObject[Immutables](property.immutables), createdBy = property.createdBy, createdOn = property.createdOn, createdOnTimeZone = property.createdOnTimeZone)))
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def insertMultiple(identities: Seq[Identity]): Future[Seq[String]] = {
      utilitiesOperations.traverse(identities) { identity =>
        (for {
          _ <- create(identity)
        } yield identity.id
          ).recover {
          case baseException: BaseException => throw baseException
        }
      }
    }

    def delete(id: String): Future[Int] = {
      val deleteProvisioned = deleteAllProvisionedAddressesByID(id)
      val deleteUnprovisioned = deleteAllUnprovisionedAddressesByID(id)

      //Should be called at last due to for FK constraint
      def deleteProperties() = deletePropertiesByID(id)

      for {
        _ <- deleteProvisioned
        _ <- deleteUnprovisioned
        _ <- deleteProperties()
      } yield 1
    }

    def getAllIDsByProvisioned(address: String): Future[Seq[String]] = getAllIdentityIDsByProvisionedAddress(address)

    def getAllIDsByUnprovisioned(address: String): Future[Seq[String]] = getAllIdentityIDsByUnprovisionedAddress(address)

    def checkExists(id: String): Future[Boolean] = checkExistsByID(id)

    def getAllProvisionAddresses(id: String): Future[Seq[String]] = getAllProvisionedAddressByID(id)

    def getAllUnprovisionAddresses(id: String): Future[Seq[String]] = getAllUnprovisionedAddressByID(id)

    def addProvisionAddress(id: String, address: String): Future[String] = addProvisionedAddressByID(id = id, address = address)

    def deleteProvisionAddress(id: String, address: String): Future[Int] = deleteProvisionedAddressByIDAndAddress(id = id, address = address)

    def addUnprovisionAddress(id: String, address: String): Future[String] = addUnprovisionedAddressByID(id = id, address = address)

    def deleteUnprovisionAddress(id: String, address: String): Future[Int] = deleteUnprovisionedAddressByIDAndAddress(id = id, address = address)
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

      val userType = identityDefine.immutableMetaTraits.metaPropertyList.find(x => x.id == constants.User.USER_TYPE).map(x => x.metaFact.data.value.asString)

      def masterOperations(classificationID: String) = {
        val insert = masterClassifications.Service.insertOrUpdate(id = classificationID, entityType = constants.Blockchain.Entity.IDENTITY_DEFINITION, maintainerID = identityDefine.fromID, label = userType, status = Option(true))
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
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onIssue(identityIssue: IdentityIssue): Future[Unit] = {
      val scrubbedImmutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(identityIssue.immutableMetaProperties.metaPropertyList)
      val scrubbedMutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(identityIssue.mutableMetaProperties.metaPropertyList)
      val userType = identityIssue.immutableMetaProperties.metaPropertyList.find(x => x.id == constants.User.USER_TYPE).map(x => x.metaFact.data.value.asString)

      def updateUser(identityID: String) = {
        userType match {
          case Some(userType)=>{
            userType match {
              case constants.User.ZONE => {
                val accountID: Future[String] = blockchainAccounts.Service.tryGetUsername(identityIssue.to)

                val orgIdentityClassificationID: Future[String] = masterClassifications.Service.tryGetClassificationID(identityIssue.fromID, constants.Blockchain.Entity.IDENTITY_DEFINITION, constants.User.ORGANIZATION)

                def verifyZone = masterZones.Service.verifyZone(identityID)

                def markUserTypeZone(accountID: String): Future[Int] = masterAccounts.Service.markUserTypeZone(accountID)

                def classificationProperties(classificationID: String) = masterProperties.Service.getAll(classificationID, constants.Blockchain.Entity.IDENTITY_DEFINITION)

                def broadcastTx(classificationID: String, classificationProperties: Seq[models.master.Property]) = transaction.process[blockchainTransaction.MaintainerDeputize, transactionsMaintainerDeputize.Request](
                  entity = blockchainTransaction.MaintainerDeputize(from = identityIssue.from, fromID = identityIssue.fromID, toID = identityID, classificationID = classificationID, maintainedTraits = classificationProperties.filter(_.isMutable).map(x => BaseProperty(x.dataType, x.name, x.value)), addMaintainer = true, mutateMaintainer = true, removeMaintainer = true, gas = MicroNumber(5), ticketID = "", mode = transactionMode),
                  blockchainTransactionCreate = blockchainTransactionMaintainerDeputizes.Service.create,
                  request = transactionsMaintainerDeputize.Request(transactionsMaintainerDeputize.Message(transactionsMaintainerDeputize.BaseReq(from = identityIssue.from, gas = MicroNumber(5)), fromID = identityIssue.fromID, toID = identityID, classificationID = classificationID, maintainedTraits = classificationProperties.filter(_.isMutable).map(x => BaseProperty(x.dataType, x.name, x.value)), addMaintainer = true, mutateMaintainer = true, removeMaintainer = true)),
                  action = transactionsMaintainerDeputize.Service.post,
                  onSuccess = blockchainTransactionMaintainerDeputizes.Utility.onSuccess,
                  onFailure = blockchainTransactionMaintainerDeputizes.Utility.onFailure,
                  updateTransactionHash = blockchainTransactionMaintainerDeputizes.Service.updateTransactionHash
                )

                for {
                  accountID <- accountID
                  orgIdentityClassificationID <- orgIdentityClassificationID
                  orgClassificationProperties <- classificationProperties(orgIdentityClassificationID)
                  _ <- broadcastTx(orgIdentityClassificationID, orgClassificationProperties)
                  _ <- verifyZone
                  _ <- markUserTypeZone(accountID)
                } yield ()
              }
              case constants.User.ORGANIZATION => {
                val accountID: Future[String] = blockchainAccounts.Service.tryGetUsername(identityIssue.to)

                val adminMaintainerID = masterIdentities.Service.tryGetIDByLabel(constants.Blockchain.Parameters.MAIN_NUB_ID)

                def markAccepted = masterOrganizations.Service.markAccepted(identityID)

                def markUserTypeOrganization(accountID: String): Future[Int] = masterAccounts.Service.markUserTypeOrganization(accountID)

                def adminAddress(adminMaintainerID: String) = Service.getAllProvisionAddresses(adminMaintainerID).map(_.headOption.getOrElse(throw new BaseException(constants.Response.IDENTITY_NOT_FOUND)))

                def traderIdentityClassificationID(maintainerID: String): Future[String] = masterClassifications.Service.tryGetClassificationID(maintainerID, constants.Blockchain.Entity.IDENTITY_DEFINITION, constants.User.TRADER)

                def classificationProperties(classificationID: String) = masterProperties.Service.getAll(classificationID, constants.Blockchain.Entity.IDENTITY_DEFINITION)

                def broadcastTx(adminMaintainerID: String, adminAddress: String, classificationID: String, classificationProperties: Seq[models.master.Property]) = transaction.process[blockchainTransaction.MaintainerDeputize, transactionsMaintainerDeputize.Request](
                  entity = blockchainTransaction.MaintainerDeputize(from = adminAddress, fromID = adminMaintainerID, toID = identityID, classificationID = classificationID, maintainedTraits = classificationProperties.filter(_.isMutable).map(x => BaseProperty(x.dataType, x.name, x.value)), addMaintainer = true, mutateMaintainer = true, removeMaintainer = true, gas = MicroNumber(5), ticketID = "", mode = transactionMode),
                  blockchainTransactionCreate = blockchainTransactionMaintainerDeputizes.Service.create,
                  request = transactionsMaintainerDeputize.Request(transactionsMaintainerDeputize.Message(transactionsMaintainerDeputize.BaseReq(from = adminAddress, gas = MicroNumber(5)), fromID = adminMaintainerID, toID = identityID, classificationID = classificationID, maintainedTraits = classificationProperties.filter(_.isMutable).map(x => BaseProperty(x.dataType, x.name, x.value)), addMaintainer = true, mutateMaintainer = true, removeMaintainer = true)),
                  action = transactionsMaintainerDeputize.Service.post,
                  onSuccess = blockchainTransactionMaintainerDeputizes.Utility.onSuccess,
                  onFailure = blockchainTransactionMaintainerDeputizes.Utility.onFailure,
                  updateTransactionHash = blockchainTransactionMaintainerDeputizes.Service.updateTransactionHash
                )

                for {
                  adminMaintainerID <- adminMaintainerID
                  adminAddress <- adminAddress(adminMaintainerID)
                  accountID <- accountID
                  traderIdentityClassificationID <- traderIdentityClassificationID(adminMaintainerID)
                  classificationProperties <- classificationProperties(traderIdentityClassificationID)
                  _ <- broadcastTx(adminMaintainerID, adminAddress, traderIdentityClassificationID, classificationProperties)
                  _ <- markAccepted
                  _ <- markUserTypeOrganization(accountID)
                } yield ()
              }
              case constants.User.TRADER => {
                def markAccepted: Future[Int] = masterTraders.Service.markAccepted(identityID)

                val accountID: Future[String] = blockchainAccounts.Service.tryGetUsername(identityIssue.to)

                def markUserTypeTrader(accountID: String): Future[Int] = masterAccounts.Service.markUserTypeTrader(accountID)

                for {
                  accountID <- accountID
                  _ <- markAccepted
                  _ <- markUserTypeTrader(accountID)
                } yield ()

              }
              case _ => throw new BaseException(constants.Response.USER_TYPE_DOES_NOT_EXIST)
            }
          }
          case None=> throw new BaseException(constants.Response.USER_TYPE_DOES_NOT_EXIST)
        }

      }

      def insertOrUpdate(scrubbedImmutableMetaProperties: Seq[Property], scrubbedMutableMetaProperties: Seq[Property]) = {
        val immutables = Immutables(Properties(scrubbedImmutableMetaProperties ++ identityIssue.immutableProperties.propertyList))
        val identityID = utilities.IDGenerator.getIdentityID(classificationID = identityIssue.classificationID, immutables = immutables)
        val upsert = Service.create(Identity(id = identityID, provisionedAddressList = Seq(identityIssue.to), unprovisionedAddressList = Seq.empty[String], mutables = Mutables(Properties(scrubbedMutableMetaProperties ++ identityIssue.mutableProperties.propertyList)), immutables = immutables))

        for {
          _ <- upsert
        } yield identityID
      }

      def masterOperations(identityID: String) = {
        val insert = masterIdentities.Service.insertOrUpdate(masterIdentity(id = identityID, label = userType, status = Option(true)))
        for {
          _ <- insert
        } yield ()
      }

      (for {
        scrubbedImmutableMetaProperties <- scrubbedImmutableMetaProperties
        scrubbedMutableMetaProperties <- scrubbedMutableMetaProperties
        identityID <- insertOrUpdate(scrubbedImmutableMetaProperties = scrubbedImmutableMetaProperties, scrubbedMutableMetaProperties = scrubbedMutableMetaProperties)
        _ <- updateUser(identityID)
        _ <- masterOperations(identityID)
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onProvision(identityProvision: IdentityProvision): Future[Unit] = {
      val add = Service.addProvisionAddress(id = identityProvision.identityID, address = identityProvision.to)
      (for {
        _ <- add
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onUnprovision(identityUnprovision: IdentityUnprovision): Future[Unit] = {
      val deleteProvision = Service.deleteProvisionAddress(id = identityUnprovision.identityID, address = identityUnprovision.to)
      val addUnprovision = Service.addUnprovisionAddress(id = identityUnprovision.identityID, address = identityUnprovision.to)
      (for {
        _ <- deleteProvision
        _ <- addUnprovision
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onNub(identityNub: IdentityNub): Future[Unit] = {
      val nubMetaProperty = getNubMetaProperty(identityNub.nubID)
      val nubProperty = blockchainMetas.Utility.auxiliaryScrub(Seq(nubMetaProperty))

      def defineAndUpsert(nubProperty: Property) = {
        val immutables = Immutables(Properties(Seq(nubProperty)))
        val mutables = Mutables(Properties(Seq()))
        val defineClassification = blockchainClassifications.Utility.auxiliaryDefine(Immutables(Properties(Seq(Property(constants.Blockchain.Properties.NubID, NewFact(constants.Blockchain.FactType.ID, IDDataValue("")))))), mutables)

        def getIdentityID(classificationID: String) = Future(utilities.IDGenerator.getIdentityID(classificationID = classificationID, immutables = immutables))

        def upsert(identityID: String) = Service.create(Identity(id = identityID, provisionedAddressList = Seq(identityNub.from), unprovisionedAddressList = Seq.empty[String], immutables = immutables, mutables = mutables))

        for {
          classificationID <- defineClassification
          identityID <- getIdentityID(classificationID)
          _ <- upsert(identityID)
        } yield (classificationID, identityID)
      }

      def masterOperations(classificationID: String, identityID: String) = {
        val insertIdentity = masterIdentities.Service.insertOrUpdate(masterIdentity(id = identityID, status = Option(true)))
        val insertClassification = masterClassifications.Service.insertOrUpdate(id = classificationID, entityType = constants.Blockchain.Entity.IDENTITY_DEFINITION, maintainerID = "", status = Option(true))

        for {
          _ <- insertIdentity
          _ <- insertClassification
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

    def getNubMetaProperty(nubID: String): MetaProperty = MetaProperty(id = constants.Blockchain.Properties.NubID, metaFact = MetaFact(Data(dataType = constants.Blockchain.DataType.ID_DATA, value = IDDataValue(nubID))))
  }

}