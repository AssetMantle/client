package models.blockchain

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.GetOrganization
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Organization(id: String, address: String, dirtyBit: Boolean)

@Singleton
class Organizations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, actorSystem: ActorSystem, getOrganization: GetOrganization)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ORGANIZATION

  import databaseConfig.profile.api._

  private[models] val organizationTable = TableQuery[OrganizationTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private def add(organization: Organization): Future[String] = db.run((organizationTable returning organizationTable.map(_.id) += organization).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getOrganizationsByDirtyBit(dirtyBit: Boolean): Future[Seq[Organization]] = db.run(organizationTable.filter(_.dirtyBit === dirtyBit).result)

  private def findById(id: String): Future[Organization] = db.run(organizationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAddressById(id: String): Future[String] = db.run(organizationTable.filter(_.id === id).map(_.address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getIdByAddress(address: String): Future[String] = db.run(organizationTable.filter(_.address === address).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateAddressAndDirtyBitByID(organizationID: String, address: String, dirtyBit: Boolean): Future[Int] = db.run(organizationTable.filter(_.id === organizationID).map(x => (x.address, x.dirtyBit)).update((address, dirtyBit)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String): Future[Int] = db.run(organizationTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class OrganizationTable(tag: Tag) extends Table[Organization](tag, "Organization_BC") {

    def * = (id, address, dirtyBit) <> (Organization.tupled, Organization.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def dirtyBit = column[Boolean]("dirtyBit")

    def address = column[String]("address")

    def ? = (id.?, address.?, dirtyBit.?).shaped.<>({ r => import r._; _1.map(_ => Organization.tupled((_1.get, _2.get, _3.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

  }

  object Service {

    def create(id: String, address: String, dirtyBit: Boolean): Future[String] = add(Organization(id = id, address = address, dirtyBit = dirtyBit))

    def getAddress(id: String): Future[String] = getAddressById(id)

    def getID(address: String): Future[String] = getIdByAddress(address)

    def getDirtyOrganizations: Future[Seq[Organization]] = getOrganizationsByDirtyBit(dirtyBit = true)

    def refreshDirty(id: String, address: String): Future[Int] = updateAddressAndDirtyBitByID(id, address, dirtyBit = false)
  }

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = {
      val dirtyOrganizations = Service.getDirtyOrganizations
      Thread.sleep(sleepTime)

      def refreshDirtyOrganizations(dirtyOrganizations: Seq[Organization]): Future[Seq[Any]] = {
        Future.sequence {
          dirtyOrganizations.map { dirtyOrganization =>
            val responseAddress = getOrganization.Service.get(dirtyOrganization.id)

            def refreshDirty(responseAddress: queries.responses.OrganizationResponse.Response): Future[Int] = Service.refreshDirty(dirtyOrganization.id, responseAddress.address)

            (for {
              responseAddress <- responseAddress
              _ <- refreshDirty(responseAddress)
            } yield Unit
              ).recover {
              case baseException: BaseException => logger.error(baseException.failure.message, baseException)
            }
          }
        }
      }

      (for {
        dirtyOrganizations <- dirtyOrganizations
        _ <- refreshDirtyOrganizations(dirtyOrganizations)
      } yield ()).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }(schedulerExecutionContext)
}