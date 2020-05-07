package models.blockchain

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.GetZone
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Zone(id: String, address: String, dirtyBit: Boolean)

@Singleton
class Zones @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, actorSystem: ActorSystem, getZone: GetZone, configuration: Configuration)(implicit executionContext: ExecutionContext) {

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
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateAddressAndDirtyBitByID(zoneID: String, address: String, dirtyBit: Boolean) = db.run(zoneTable.filter(_.id === zoneID).map(x => (x.address, x.dirtyBit)).update((address, dirtyBit)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getZonesByDirtyBit(dirtyBit: Boolean): Future[Seq[Zone]] = db.run(zoneTable.filter(_.dirtyBit === dirtyBit).result)

  private def findById(id: String): Future[Zone] = db.run(zoneTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateDirtyBitByID(zoneID: String, dirtyBit: Boolean) = db.run(zoneTable.filter(_.id === zoneID).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAddressById(id: String): Future[String] = db.run(zoneTable.filter(_.id === id).map(_.address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getIdByAddress(address: String): Future[String] = db.run(zoneTable.filter(_.address === address).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
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

  private[models] class ZoneTable(tag: Tag) extends Table[Zone](tag, "Zone_BC") {

    def * = (id, address, dirtyBit) <> (Zone.tupled, Zone.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def address = column[String]("address")

    def dirtyBit = column[Boolean]("dirtyBit")

    def ? = (id.?, address.?, dirtyBit.?).shaped.<>({ r => import r._; _1.map(_ => Zone.tupled((_1.get, _2.get, _3.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

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

            def refreshDirty(response: queries.responses.ZoneResponse.Response): Future[Int] = Service.refreshDirty(dirtyZone.id, response.body)

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