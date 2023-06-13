package models.traits

import exceptions.BaseException
import org.postgresql.util.PSQLException
import play.api.Logger
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

abstract class TestGenericDaoImpl[
  T <: Table[E] with ModelTable[PK],
  E <: Entity[PK],
  PK: BaseColumnType](
                       tableQuery: TableQuery[T],
                       implicit val executionContext: ExecutionContext,
                       implicit val module: String,
                       implicit val logger: Logger) extends GenericDao[E, T](tableQuery) {

  private val databaseConfig = dbConfigProvider.get[JdbcProfile]

  private val db = databaseConfig.db

  import databaseConfig.profile.api._

  def deleteById(id: PK): Future[Int] = db.run(tableQuery.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(new constants.Response.Failure(module + "_DELETE_FAILED"), psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(new constants.Response.Failure(module + "_DELETE_FAILED"), noSuchElementException)
    }
  }

  def deleteByIds(ids: Seq[PK]): Future[Unit] = db.run(tableQuery.filter(_.id.inSet(ids)).delete.asTry).map {
    case Success(result) => ()
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(new constants.Response.Failure(module + "_DELETE_FAILED"), psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(new constants.Response.Failure(module + "_DELETE_FAILED"), noSuchElementException)
    }
  }

  def exists(id: PK): Future[Boolean] = db.run(tableQuery.filter(_.id === id).exists.result)

  def getById(id: PK): Future[Option[E]] = db.run(tableQuery.filter(_.id === id).result.headOption)

  def getByIds(ids: Seq[PK]): Future[Seq[E]] = db.run(tableQuery.filter(_.id.inSet(ids)).result)

  def tryGetById(id: PK): Future[E] = db.run(tableQuery.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(new constants.Response.Failure(module + "_NOT_FOUND"), noSuchElementException)
    }
  }

  def updateById(update: E): Future[Unit] = db.run(tableQuery.filter(_.id === update.id).update(update).asTry).map {
    case Success(result) => ()
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(new constants.Response.Failure(module + "_UPDATE_FAILED"), psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(new constants.Response.Failure(module + "_UPDATE_FAILED"), noSuchElementException)
    }
  }
}