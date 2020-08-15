package models.blockchain

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Serializable._
import models.common.TransactionMessages.{IdentityIssue, IdentityProvision, IdentityUnprovision}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.GetIdentity
import queries.responses.IdentityResponse.{Response => IdentityResponse}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Identity(id: String, provisionedAddressList: Seq[String], unprovisionedAddressList: Seq[String], immutables: Immutables, mutables: Mutables, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Identities @Inject()(
                            protected val databaseConfigProvider: DatabaseConfigProvider,
                            configuration: Configuration,
                            getIdentity: GetIdentity,
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

    def onIssue(identityIssue: IdentityIssue): Future[Unit] = {
      val hashID = Immutables(identityIssue.properties).getHashID
      val identityResponse = getIdentity.Service.get(getID(chainID = chainID, maintainersID = identityIssue.maintainersID, classificationID = identityIssue.classificationID, hashID = hashID))

      def insertOrUpdate(identityResponse: IdentityResponse) = identityResponse.result.value.identities.value.list.find(x => x.value.id.value.chainID.value.idString == chainID && x.value.id.value.maintainersID.value.idString == identityIssue.maintainersID && x.value.id.value.classificationID.value.idString == identityIssue.classificationID && x.value.id.value.hashID.value.idString == hashID).fold(throw new BaseException(constants.Response.IDENTITY_NOT_FOUND)) { identity =>
        val identityID = getID(chainID = identity.value.id.value.chainID.value.idString, maintainersID = identity.value.id.value.maintainersID.value.idString, classificationID = identity.value.id.value.classificationID.value.idString, hashID = identity.value.id.value.hashID.value.idString)
        Service.insertOrUpdate(Identity(id = identityID, provisionedAddressList = identity.value.provisionedAddressList.fold(Seq.empty[String])(x => x), unprovisionedAddressList = identity.value.unprovisionedAddressList.fold(Seq.empty[String])(x => x), mutables = identity.value.mutables.toMutables, immutables = identity.value.immutables.toImmutables))
      }

      (for {
        identityResponse <- identityResponse
        _ <- insertOrUpdate(identityResponse)
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def onProvision(identityProvision: IdentityProvision): Future[Unit] = {
      val identityResponse = getIdentity.Service.get(identityProvision.identityID)
      val (chainID, maintainersID, classificationID, hashID) = getFeatures(identityProvision.identityID)

      def insertOrUpdate(identityResponse: IdentityResponse) = identityResponse.result.value.identities.value.list.find(x => x.value.id.value.chainID.value.idString == chainID && x.value.id.value.maintainersID.value.idString == maintainersID && x.value.id.value.classificationID.value.idString == classificationID && x.value.id.value.hashID.value.idString == hashID).fold(throw new BaseException(constants.Response.IDENTITY_NOT_FOUND)) { identity =>
        Service.insertOrUpdate(Identity(id = identityProvision.identityID, provisionedAddressList = identity.value.provisionedAddressList.fold(Seq.empty[String])(x => x), unprovisionedAddressList = identity.value.unprovisionedAddressList.fold(Seq.empty[String])(x => x), mutables = identity.value.mutables.toMutables, immutables = identity.value.immutables.toImmutables))
      }

      (for {
        identityResponse <- identityResponse
        _ <- insertOrUpdate(identityResponse)
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def onUnprovision(identityUnprovision: IdentityUnprovision): Future[Unit] = {
      val identityResponse = getIdentity.Service.get(identityUnprovision.identityID)
      val (chainID, maintainersID, classificationID, hashID) = getFeatures(identityUnprovision.identityID)

      def insertOrUpdate(identityResponse: IdentityResponse) = identityResponse.result.value.identities.value.list.find(x => x.value.id.value.chainID.value.idString == chainID && x.value.id.value.maintainersID.value.idString == maintainersID && x.value.id.value.classificationID.value.idString == classificationID && x.value.id.value.hashID.value.idString == hashID).fold(throw new BaseException(constants.Response.IDENTITY_NOT_FOUND)) { identity =>
        Service.insertOrUpdate(Identity(id = identityUnprovision.identityID, provisionedAddressList = identity.value.provisionedAddressList.fold(Seq.empty[String])(x => x), unprovisionedAddressList = identity.value.unprovisionedAddressList.fold(Seq.empty[String])(x => x), mutables = identity.value.mutables.toMutables, immutables = identity.value.immutables.toImmutables))
      }

      (for {
        identityResponse <- identityResponse
        _ <- insertOrUpdate(identityResponse)
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    private def getID(chainID: String, maintainersID: String, classificationID: String, hashID: String) = Seq(chainID, maintainersID, classificationID, hashID).mkString(constants.Blockchain.IDSeparator)

    private def getFeatures(id: String): (String, String, String, String) = {
      val idList = id.split(constants.RegularExpression.BLOCKCHAIN_ID_SEPARATOR)
      if (idList.length == 4) (idList(0), idList(1), idList(2), idList(3)) else ("", "", "", "")
    }
  }

}