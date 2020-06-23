package models.blockchain

import java.sql.Timestamp

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.GetZone
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Zone(id: String, address: String, dirtyBit: Boolean, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Zones @Inject()(
                       protected val databaseConfigProvider: DatabaseConfigProvider,
                       actorSystem: ActorSystem,
                       getZone: GetZone,
                       configuration: Configuration,
                     )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ZONE

  import databaseConfig.profile.api._

  private[models] val zoneTable = TableQuery[ZoneTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private def add(zone: Zone): Future[String] = db.run((zoneTable returning zoneTable.map(_.id) += zone).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateAddressAndDirtyBitByID(zoneID: String, address: String, dirtyBit: Boolean) = db.run(zoneTable.filter(_.id === zoneID).map(x => (x.address, x.dirtyBit)).update((address, dirtyBit)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getZonesByDirtyBit(dirtyBit: Boolean): Future[Seq[Zone]] = db.run(zoneTable.filter(_.dirtyBit === dirtyBit).result)

  private def findById(id: String): Future[Zone] = db.run(zoneTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateDirtyBitByID(zoneID: String, dirtyBit: Boolean) = db.run(zoneTable.filter(_.id === zoneID).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getAddressById(id: String): Future[String] = db.run(zoneTable.filter(_.id === id).map(_.address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getIdByAddress(address: String): Future[String] = db.run(zoneTable.filter(_.address === address).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteById(id: String) = db.run(zoneTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class ZoneTable(tag: Tag) extends Table[Zone](tag, "Zone_BC") {

    def * = (id, address, dirtyBit, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Zone.tupled, Zone.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def address = column[String]("address")

    def dirtyBit = column[Boolean]("dirtyBit")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(id: String, address: String, dirtyBit: Boolean): Future[String] = add(Zone(id = id, address = address, dirtyBit = dirtyBit))

    def getAddress(id: String): Future[String] = getAddressById(id)

    def getID(address: String): Future[String] = getIdByAddress(address)

    def getDirtyZones: Future[Seq[Zone]] = getZonesByDirtyBit(dirtyBit = true)

    def refreshDirty(zoneID: String, address: String): Future[Int] = updateAddressAndDirtyBitByID(zoneID, address, dirtyBit = false)

    def markDirty(zoneID: String): Future[Int] = updateDirtyBitByID(zoneID, dirtyBit = true)
  }

  object Utility {

    def dirtyEntityUpdater(): Future[Unit] = {
      val dirtyZones = Service.getDirtyZones
      Thread.sleep(sleepTime)

      def refreshDirtyZones(dirtyZones: Seq[Zone]): Future[Seq[Unit]] = {
        Future.sequence {
          dirtyZones.map { dirtyZone =>
            val response = getZone.Service.get(dirtyZone.id)

            def refreshDirty(response: queries.responses.ZoneResponse.Response): Future[Int] = {
              Service.refreshDirty(dirtyZone.id, response.body)
            }

            (for {
              response <- response
              _ <- refreshDirty(response)
            } yield {}
              ).recover {
              case baseException: BaseException => logger.error(baseException.failure.message, baseException)
            }
          }
        }
      }

      (for {
        dirtyZones <- dirtyZones
        _ <- refreshDirtyZones(dirtyZones)
      } yield ()).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }(schedulerExecutionContext)
}