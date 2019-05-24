package models.blockchain

import akka.actor.ActorSystem
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.GetZone
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Zone(id: String, address: String, dirtyBit: Boolean)

@Singleton
class Zones @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, actorSystem: ActorSystem, getZone: GetZone)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ZONE

  import databaseConfig.profile.api._

  private[models] val zoneTable = TableQuery[ZoneTable]

  private def add(zone: Zone)(implicit executionContext: ExecutionContext): Future[String] = db.run((zoneTable returning zoneTable.map(_.id) += zone).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def updateAddressAndDirtyBitByID(zoneID: String, address: String, dirtyBit: Boolean) = db.run(zoneTable.filter(_.id === zoneID).map(x => (x.address, x.dirtyBit)).update((address, dirtyBit)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateDirtyBitByID(zoneID: String, dirtyBit: Boolean) = db.run(zoneTable.filter(_.id === zoneID).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findById(id: String)(implicit executionContext: ExecutionContext): Future[Zone] = db.run(zoneTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAddressById(id: String)(implicit executionContext: ExecutionContext): Future[String] = db.run(zoneTable.filter(_.id === id).map(_.address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getIdByAddress(address: String)(implicit executionContext: ExecutionContext): Future[String] = db.run(zoneTable.filter(_.address === address).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getZonesByDirtyBit(dirtyBit: Boolean): Future[Seq[Zone]] = db.run(zoneTable.filter(_.dirtyBit === dirtyBit).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        Nil
    }
  }

  private def deleteById(id: String)(implicit executionContext: ExecutionContext) = db.run(zoneTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class ZoneTable(tag: Tag) extends Table[Zone](tag, "Zone_BC") {

    def * = (id, address, dirtyBit) <> (Zone.tupled, Zone.unapply)

    def ? = (id.?, address.?, dirtyBit.?).shaped.<>({ r => import r._; _1.map(_ => Zone.tupled((_1.get, _2.get, _3.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    def id = column[String]("id", O.PrimaryKey)

    def address = column[String]("address")

    def dirtyBit = column[Boolean]("dirtyBit")

  }

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  object Service {

    def addZone(id: String, address: String, dirtyBit: Boolean)(implicit executionContext: ExecutionContext): String = Await.result(add(Zone(id = id, address = address, dirtyBit = dirtyBit)), Duration.Inf)

    def getAddress(id: String)(implicit executionContext: ExecutionContext): String = Await.result(getAddressById(id), Duration.Inf)

    def getID(address: String)(implicit executionContext: ExecutionContext): String = Await.result(getIdByAddress(address), Duration.Inf)

    def getDirtyZones: Seq[Zone] = Await.result(getZonesByDirtyBit(dirtyBit = true), Duration.Inf)

    def refreshDirty(zoneID: String, address: String, dirtyBit: Boolean): Int = Await.result(updateAddressAndDirtyBitByID(zoneID, address, dirtyBit), Duration.Inf)

    def markDirty(zoneID: String): Int = Await.result(updateDirtyBitByID(zoneID, dirtyBit = true), Duration.Inf)
  }

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = Future {
      val dirtyZone = Service.getDirtyZones
      Thread.sleep(sleepTime)
      for (dirtyZone <- dirtyZone) {
        try {
          val responseAddress = getZone.Service.get(dirtyZone.id)
          Service.refreshDirty(dirtyZone.id, responseAddress.body, dirtyBit = false)
        }
        catch {
          case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
          case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
        }
      }
    }
  }


  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }
}