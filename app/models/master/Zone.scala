package models.master

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import models.common.Serializable._
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Zone(id: String, accountID: String, name: String, currency: String, address: Address, completionStatus: Boolean = false, verificationStatus: Option[Boolean] = None)

@Singleton
class Zones @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  case class ZoneSerialized(id: String, accountID: String, name: String, currency: String, address: String, completionStatus: Boolean, verificationStatus: Option[Boolean]) {

    def deserialize: Zone = Zone(id = id, accountID = accountID, name = name, currency = currency, address = utilities.JSON.convertJsonStringToObject[Address](address), completionStatus = completionStatus, verificationStatus = verificationStatus)

  }

  private def serialize(zone: Zone): ZoneSerialized = ZoneSerialized(id = zone.id, accountID = zone.accountID, name = zone.name, currency = zone.currency, address = Json.toJson(zone.address).toString, completionStatus = zone.completionStatus, verificationStatus = zone.verificationStatus)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ZONE

  import databaseConfig.profile.api._

  private[models] val zoneTable = TableQuery[ZoneTable]

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

  private def findByAccountID(accountID: String): Future[ZoneSerialized] = db.run(zoneTable.filter(_.accountID === accountID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getIDByAccountID(accountID: String): Future[Option[String]] = db.run(zoneTable.filter(_.accountID === accountID).map(_.id.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        None
    }
  }

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

    def create(accountID: String, name: String, currency: String, address: Address): String = Await.result(add(serialize(Zone(id = utilities.IDGenerator.hexadecimal, accountID = accountID, name = name, currency = currency, address = address))), Duration.Inf)

    def insertOrUpdate(accountID: String, name: String, currency: String, address: Address): String = {
      val id = Await.result(getIDByAccountID(accountID), Duration.Inf).getOrElse(utilities.IDGenerator.hexadecimal)
      Await.result(upsert(serialize(Zone(id = id, accountID = accountID, name = name, currency = currency, address = address))), Duration.Inf)
      id
    }

    def get(id: String): Zone = Await.result(findById(id), Duration.Inf).deserialize

    def getID(accountID: String): String = Await.result(getIDByAccountID(accountID), Duration.Inf).getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION))

    def getByAccountID(accountID: String): Zone = Await.result(findByAccountID(accountID), Duration.Inf).deserialize

    def getAllVerified: Seq[Zone] = Await.result(getZonesByCompletionStatusVerificationStatus(completionStatus = true, verificationStatus = Option(true)), Duration.Inf).map(_.deserialize)

    def verifyZone(id: String): Int = Await.result(updateVerificationStatusOnID(id, Option(true)), Duration.Inf)

    def rejectZone(id: String): Int = Await.result(updateVerificationStatusOnID(id, Option(false)), Duration.Inf)

    def getAccountId(id: String): String = Await.result(getAccountIdById(id), Duration.Inf)

    def markZoneFormCompleted(id: String): Int = Await.result(updateCompletionStatusOnID(id = id, completionStatus = true), Duration.Inf)

    def getVerifyZoneRequests: Seq[Zone] = Await.result(getZonesByCompletionStatusVerificationStatus(completionStatus = true, verificationStatus = null), Duration.Inf).map(_.deserialize)

    def getVerificationStatus(id: String): Boolean = Await.result(getVerificationStatusByID(id), Duration.Inf).getOrElse(false)

  }

}