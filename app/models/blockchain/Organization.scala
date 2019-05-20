package models.blockchain

import akka.actor.ActorSystem
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.GetOrganization
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Organization(id: String, address: String, dirtyBit: Boolean)

@Singleton
class Organizations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, actorSystem: ActorSystem, getOrganization: GetOrganization)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ORGANIZATION

  import databaseConfig.profile.api._

  private[models] val organizationTable = TableQuery[OrganizationTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val sleepTime = configuration.get[Long]("blockchain.kafka.entityIterator.threadSleep")

  private def add(organization: Organization)(implicit executionContext: ExecutionContext): Future[String] = db.run((organizationTable returning organizationTable.map(_.id) += organization).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String)(implicit executionContext: ExecutionContext): Future[Organization] = db.run(organizationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAddressById(id: String)(implicit executionContext: ExecutionContext): Future[String] = db.run(organizationTable.filter(_.id === id).map(_.address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getIdByAddress(address: String)(implicit executionContext: ExecutionContext): Future[String] = db.run(organizationTable.filter(_.address === address).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getOrganizationsByDirtyBit(dirtyBit: Boolean): Future[Seq[Organization]] = db.run(organizationTable.filter(_.dirtyBit === dirtyBit).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        Nil
    }
  }

  private def updateAddressAndDirtyBitByID(organizationID: String, address: String, dirtyBit: Boolean): Future[Int] = db.run(organizationTable.filter(_.id === organizationID).map(x => (x.address, x.dirtyBit)).update((address, dirtyBit)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(organizationTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class OrganizationTable(tag: Tag) extends Table[Organization](tag, "Organization_BC") {

    def * = (id, address, dirtyBit) <> (Organization.tupled, Organization.unapply)

    def ? = (id.?, address.?, dirtyBit.?).shaped.<>({ r => import r._; _1.map(_ => Organization.tupled((_1.get, _2.get, _3.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    def id = column[String]("id", O.PrimaryKey)

    def dirtyBit = column[Boolean]("dirtyBit")

    def address = column[String]("address")

  }

  object Service {

    def addOrganization(id: String, address: String, dirtyBit: Boolean)(implicit executionContext: ExecutionContext): String = Await.result(add(Organization(id = id, address = address, dirtyBit = dirtyBit)), Duration.Inf)

    def getAddress(id: String)(implicit executionContext: ExecutionContext): String = Await.result(getAddressById(id), Duration.Inf)

    def getID(address: String)(implicit executionContext: ExecutionContext): String = Await.result(getIdByAddress(address), Duration.Inf)

    def getDirtyOrganizations(dirtyBit: Boolean): Seq[Organization] = Await.result(getOrganizationsByDirtyBit(dirtyBit), Duration.Inf)

    def updateAddressAndDirtyBit(id: String, address: String, dirtyBit: Boolean): Int = Await.result(updateAddressAndDirtyBitByID(id, address, dirtyBit), Duration.Inf)
  }

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = Future {
      val dirtyOrganizations = Service.getDirtyOrganizations(dirtyBit = true)
      Thread.sleep(sleepTime)
      for (dirtyOrganization <- dirtyOrganizations) {
        try {
          val responseAddress = getOrganization.Service.get(dirtyOrganization.id)
          Service.updateAddressAndDirtyBit(dirtyOrganization.id, responseAddress.address, dirtyBit = false)
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