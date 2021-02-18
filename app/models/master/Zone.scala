package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Serializable._
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Zone(id: String, accountID: String, name: String, currency: String, address: Address, completionStatus: Boolean = false, verificationStatus: Option[Boolean] = None, deputizeStatus: Boolean = false, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Zones @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private[models] val zoneTable = TableQuery[ZoneTable]

  private def serialize(zone: Zone): ZoneSerialized = ZoneSerialized(id = zone.id, accountID = zone.accountID, name = zone.name, currency = zone.currency, address = Json.toJson(zone.address).toString, completionStatus = zone.completionStatus, verificationStatus = zone.verificationStatus, deputizeStatus = zone.deputizeStatus, createdBy = zone.createdBy, createdOn = zone.createdOn, createdOnTimeZone = zone.createdOnTimeZone, updatedBy = zone.updatedBy, updatedOn = zone.updatedOn, updatedOnTimeZone = zone.updatedOnTimeZone)

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ZONE

  import databaseConfig.profile.api._

  private def add(zoneSerialized: ZoneSerialized): Future[String] = db.run((zoneTable returning zoneTable.map(_.id) += zoneSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(zoneSerialized: ZoneSerialized): Future[Int] = db.run(zoneTable.insertOrUpdate(zoneSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def tryGetByID(id: String): Future[ZoneSerialized] = db.run(zoneTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getByID(id: String): Future[Option[ZoneSerialized]] = db.run(zoneTable.filter(_.id === id).result.headOption)

  private def findByAccountID(accountID: String): Future[ZoneSerialized] = db.run(zoneTable.filter(_.accountID === accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def tryGetIDByAccountID(accountID: String): Future[Option[String]] = db.run(zoneTable.filter(_.accountID === accountID).map(_.id).result.headOption)

  private def deleteById(id: String) = db.run(zoneTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def tryGetAccountIDByID(id: String): Future[String] = db.run(zoneTable.filter(_.id === id).map(_.accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getZonesByCompletionStatusVerificationStatus(completionStatus: Boolean, verificationStatus: Option[Boolean]): Future[Seq[ZoneSerialized]] = db.run(zoneTable.filter(_.completionStatus === completionStatus).filter(_.verificationStatus.? === verificationStatus).sortBy(x => x.updatedOn.ifNull(x.createdOn).desc).result)

  private def updateVerificationStatusOnID(id: String, verificationStatus: Option[Boolean]) = db.run(zoneTable.filter(_.id === id).map(_.verificationStatus.?).update(verificationStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateCompletionStatusOnID(id: String, completionStatus: Boolean) = db.run(zoneTable.filter(_.id === id).map(_.completionStatus).update(completionStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getVerificationStatusByID(id: String): Future[Option[Boolean]] = db.run(zoneTable.filter(_.id === id).map(_.verificationStatus.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  case class ZoneSerialized(id: String, accountID: String, name: String, currency: String, address: String, completionStatus: Boolean, verificationStatus: Option[Boolean], deputizeStatus: Boolean, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {

    def deserialize: Zone = Zone(id = id, accountID = accountID, name = name, currency = currency, address = utilities.JSON.convertJsonStringToObject[Address](address), completionStatus = completionStatus, verificationStatus = verificationStatus, deputizeStatus = deputizeStatus, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)

  }

  private[models] class ZoneTable(tag: Tag) extends Table[ZoneSerialized](tag, "Zone") {

    def * = (id, accountID, name, currency, address, completionStatus, verificationStatus.?, deputizeStatus, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (ZoneSerialized.tupled, ZoneSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def accountID = column[String]("accountID")

    def name = column[String]("name")

    def currency = column[String]("currency")

    def address = column[String]("address")

    def completionStatus = column[Boolean]("completionStatus")

    def verificationStatus = column[Boolean]("verificationStatus")

    def deputizeStatus = column[Boolean]("deputizeStatus")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(accountID: String, name: String, currency: String, address: Address): Future[String] = add(serialize(Zone(id = utilities.IDGenerator.hexadecimal, accountID = accountID, name = name, currency = currency, address = address)))

    def insertOrUpdate(id: String, accountID: String, name: String, currency: String, address: Address): Future[Int] = upsert(serialize(Zone(id = id, accountID = accountID, name = name, currency = currency, address = address)))

    def tryGet(id: String): Future[Zone] = tryGetByID(id).map(_.deserialize)

    def get(id: String): Future[Option[Zone]] = getByID(id).map(_.map(_.deserialize))

    def tryGetID(accountID: String): Future[String] = tryGetIDByAccountID(accountID).map(_.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)))

    def getByAccountID(accountID: String): Future[Zone] = findByAccountID(accountID).map(_.deserialize)

    def getAllVerified: Future[Seq[Zone]] = getZonesByCompletionStatusVerificationStatus(completionStatus = true, verificationStatus = Option(true)).map(_.map(_.deserialize))

    def verifyZone(id: String): Future[Int] = updateVerificationStatusOnID(id, Option(true))

    def rejectZone(id: String): Future[Int] = updateVerificationStatusOnID(id, Option(false))

    def tryGetAccountID(id: String): Future[String] = tryGetAccountIDByID(id)

    def markZoneFormCompleted(id: String): Future[Int] = updateCompletionStatusOnID(id = id, completionStatus = true)

    def getVerifyZoneRequests: Future[Seq[Zone]] = getZonesByCompletionStatusVerificationStatus(completionStatus = true, verificationStatus = null).map(_.map(_.deserialize))

    def getVerificationStatus(id: String): Future[Boolean] = getVerificationStatusByID(id).map { status => status.getOrElse(false) }

  }

}