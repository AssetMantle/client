package models.master

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Serializable._
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Zone(id: String, accountID: String, name: String, currency: String, address: Address, completionStatus: Boolean = false, verificationStatus: Option[Boolean] = None)

@Singleton
class Zones @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {
  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db
  private[models] val zoneTable = TableQuery[ZoneTable]

  private def serialize(zone: Zone): ZoneSerialized = ZoneSerialized(id = zone.id, accountID = zone.accountID, name = zone.name, currency = zone.currency, address = Json.toJson(zone.address).toString, completionStatus = zone.completionStatus, verificationStatus = zone.verificationStatus)

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ZONE

  import databaseConfig.profile.api._

  private def add(zoneSerialized: ZoneSerialized): Future[String] = db.run((zoneTable returning zoneTable.map(_.id) += zoneSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(zoneSerialized: ZoneSerialized): Future[Int] = db.run(zoneTable.insertOrUpdate(zoneSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[ZoneSerialized] = db.run(zoneTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findOrNoneByID(id: String): Future[Option[ZoneSerialized]] = db.run(zoneTable.filter(_.id === id).result.headOption)

  private def findByAccountID(accountID: String): Future[ZoneSerialized] = db.run(zoneTable.filter(_.accountID === accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getIDByAccountID(accountID: String): Future[Option[String]] = db.run(zoneTable.filter(_.accountID === accountID).map(_.id).result.headOption)

  private def deleteById(id: String) = db.run(zoneTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAccountIdById(id: String): Future[String] = db.run(zoneTable.filter(_.id === id).map(_.accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getZonesByCompletionStatusVerificationStatus(completionStatus: Boolean, verificationStatus: Option[Boolean]): Future[Seq[ZoneSerialized]] = db.run(zoneTable.filter(_.completionStatus === completionStatus).filter(_.verificationStatus.? === verificationStatus).result)

  private def updateVerificationStatusOnID(id: String, verificationStatus: Option[Boolean]) = db.run(zoneTable.filter(_.id === id).map(_.verificationStatus.?).update(verificationStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateCompletionStatusOnID(id: String, completionStatus: Boolean) = db.run(zoneTable.filter(_.id === id).map(_.completionStatus).update(completionStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getVerificationStatusByID(id: String): Future[Option[Boolean]] = db.run(zoneTable.filter(_.id === id).map(_.verificationStatus.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  case class ZoneSerialized(id: String, accountID: String, name: String, currency: String, address: String, completionStatus: Boolean, verificationStatus: Option[Boolean]) {

    def deserialize: Zone = Zone(id = id, accountID = accountID, name = name, currency = currency, address = utilities.JSON.convertJsonStringToObject[Address](address), completionStatus = completionStatus, verificationStatus = verificationStatus)

  }

  private[models] class ZoneTable(tag: Tag) extends Table[ZoneSerialized](tag, "Zone") {

    def * = (id, accountID, name, currency, address, completionStatus, verificationStatus.?) <> (ZoneSerialized.tupled, ZoneSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def accountID = column[String]("accountID")

    def name = column[String]("name")

    def currency = column[String]("currency")

    def address = column[String]("address")

    def completionStatus = column[Boolean]("completionStatus")

    def verificationStatus = column[Boolean]("verificationStatus")

  }

  object Service {

    def create(accountID: String, name: String, currency: String, address: Address): Future[String] = add(serialize(Zone(id = utilities.IDGenerator.hexadecimal, accountID = accountID, name = name, currency = currency, address = address)))

    def insertOrUpdate(accountID: String, name: String, currency: String, address: Address): Future[String] = {
      val id = getIDByAccountID(accountID).map(_.getOrElse(utilities.IDGenerator.hexadecimal))

      def upsertZone(id: String) = upsert(serialize(Zone(id = id, accountID = accountID, name = name, currency = currency, address = address)))

      for {
        id <- id
        _ <- upsertZone(id)
      } yield id
    }

    def get(id: String): Future[Zone] = findById(id).map {
      _.deserialize
    }

    def getOrNone(id: String): Future[Option[Zone]] = findOrNoneByID(id).map(_.map(_.deserialize))

    def tryGetID(accountID: String): Future[String] = getIDByAccountID(accountID).map(_.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)))

    def getByAccountID(accountID: String): Future[Zone] = findByAccountID(accountID).map(_.deserialize)

    def getAllVerified: Future[Seq[Zone]] = getZonesByCompletionStatusVerificationStatus(completionStatus = true, verificationStatus = Option(true)).map(_.map(_.deserialize))

    def verifyZone(id: String): Future[Int] = updateVerificationStatusOnID(id, Option(true))

    def rejectZone(id: String): Future[Int] = updateVerificationStatusOnID(id, Option(false))

    def getAccountId(id: String): Future[String] = getAccountIdById(id)

    def markZoneFormCompleted(id: String): Future[Int] = updateCompletionStatusOnID(id = id, completionStatus = true)

    def getVerifyZoneRequests: Future[Seq[Zone]] = getZonesByCompletionStatusVerificationStatus(completionStatus = true, verificationStatus = null).map(_.map(_.deserialize))

    def getVerificationStatus(id: String): Future[Boolean] = getVerificationStatusByID(id).map { status => status.getOrElse(false) }

  }

}