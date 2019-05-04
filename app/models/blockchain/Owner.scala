package models.blockchain

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Owner(pegHash: String, ownerAddress: String, amount: Int)

@Singleton
class Owners @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_OWNER

  import databaseConfig.profile.api._

  private[models] val ownerTable = TableQuery[OwnerTable]

  private def add(owner: Owner)(implicit executionContext: ExecutionContext): Future[String] = db.run((ownerTable returning ownerTable.map(_.pegHash) += owner).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def findByPegHashOwnerAddress(pegHash: String, ownerAddress: String)(implicit executionContext: ExecutionContext): Future[Owner] = db.run(ownerTable.filter(_.pegHash === pegHash).filter(_.ownerAddress === ownerAddress).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateOwnerAddressByPegHashPreviousOwnerAddress(pegHash: String, previousOwnerAddress: String, newOwnerAddress: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(ownerTable.filter(_.pegHash === pegHash).filter(_.ownerAddress === previousOwnerAddress).map(_.ownerAddress).update(newOwnerAddress).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def insertOrUpdate(owner: Owner)(implicit executionContext: ExecutionContext): Future[Int] = db.run(ownerTable.insertOrUpdate(owner).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def getOwnersByOwnerAddress(ownerAddress: String)(implicit executionContext: ExecutionContext): Future[Seq[Owner]] = db.run(ownerTable.filter(_.ownerAddress === ownerAddress).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.info(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        Nil
    }
  }

  private def deleteByPegHashOwnerAddress(pegHash: String, ownerAddress: String)(implicit executionContext: ExecutionContext) = db.run(ownerTable.filter(_.pegHash === pegHash).filter(_.ownerAddress === ownerAddress).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByPegHash(pegHash: String)(implicit executionContext: ExecutionContext) = db.run(ownerTable.filter(_.pegHash === pegHash).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }


  private[models] class OwnerTable(tag: Tag) extends Table[Owner](tag, "Owner_BC") {

    def * = (pegHash, ownerAddress, amount) <> (Owner.tupled, Owner.unapply)

    def pegHash = column[String]("pegHash", O.PrimaryKey)

    def ownerAddress = column[String]("ownerAddress", O.PrimaryKey)

    def amount = column[Int]("amount")

  }

  object Service {

    def addOwner(pegHash: String, ownerAddress: String, amount: Int)(implicit executionContext: ExecutionContext): String = Await.result(add(Owner(pegHash, ownerAddress, amount)), Duration.Inf)

    def getOwner(pegHash: String, ownerAddress: String)(implicit executionContext: ExecutionContext): Owner = Await.result(findByPegHashOwnerAddress(pegHash, ownerAddress), Duration.Inf)

    def getOwners(ownerAddress: String)(implicit executionContext: ExecutionContext): Seq[Owner] = Await.result(getOwnersByOwnerAddress(ownerAddress), Duration.Inf)

    def updateOwnerAddress(pegHash: String, previousOwnerAddress: String, newOwnerAddress: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateOwnerAddressByPegHashPreviousOwnerAddress(pegHash, previousOwnerAddress, newOwnerAddress), Duration.Inf)

    def deleteOwner(pegHash: String, ownerAddress: String)(implicit executionContext: ExecutionContext): Int = Await.result(deleteByPegHashOwnerAddress(pegHash, ownerAddress), Duration.Inf)

    def deleteOwners(pegHash: String)(implicit executionContext: ExecutionContext): Int = Await.result(deleteByPegHash(pegHash), Duration.Inf)

    def insertOrUpdateOwner(pegHash: String, ownerAddress: String, amount: Int)(implicit executionContext: ExecutionContext): Int = Await.result(insertOrUpdate(Owner(pegHash, ownerAddress, amount)), Duration.Inf)
  }
}