package models.traits

import org.postgresql.util.PSQLException
import slick.jdbc.H2Profile.api._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

abstract class GenericDaoImpl[
  T <: Table[E] with ModelTable[PK],
  E <: Entity[PK],
  PK: BaseColumnType]()(implicit executionContext: ExecutionContext) extends CommonGenericDao[E, T]() {

  import dbConfig.profile.api._ // Important for upsert to not generate "merge"(MySQL) statements for insertOrUpdate. Postgres doesn't have "merge"

  def deleteById(id: PK): Future[Int] = db.run(tableQuery.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => new constants.Response.Failure(module + "_DELETE_FAILED").throwBaseException(psqlException)
      case noSuchElementException: NoSuchElementException => new constants.Response.Failure(module + "_DELETE_FAILED").throwBaseException(noSuchElementException)
    }
  }

  def deleteByIds(ids: Seq[PK]): Future[Unit] = db.run(tableQuery.filter(_.id.inSet(ids)).delete.asTry).map {
    case Success(result) => ()
    case Failure(exception) => exception match {
      case psqlException: PSQLException => new constants.Response.Failure(module + "_DELETE_FAILED").throwBaseException(psqlException)
      case noSuchElementException: NoSuchElementException => new constants.Response.Failure(module + "_DELETE_FAILED").throwBaseException(noSuchElementException)
    }
  }

  def exists(id: PK): Future[Boolean] = db.run(tableQuery.filter(_.id === id).exists.result)

  def getById(id: PK): Future[Option[E]] = db.run(tableQuery.filter(_.id === id).result.headOption)

  def getByIds(ids: Seq[PK]): Future[Seq[E]] = db.run(tableQuery.filter(_.id.inSet(ids)).result)

  def tryGetById(id: PK): Future[E] = db.run(tableQuery.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => new constants.Response.Failure(module + "_NOT_FOUND").throwBaseException(noSuchElementException)
    }
  }

  def updateById(update: E): Future[Unit] = db.run(tableQuery.filter(_.id === update.id).update(update).asTry).map {
    case Success(result) => ()
    case Failure(exception) => exception match {
      case psqlException: PSQLException => new constants.Response.Failure(module + "_UPDATE_FAILED").throwBaseException(psqlException)
      case noSuchElementException: NoSuchElementException => new constants.Response.Failure(module + "_UPDATE_FAILED").throwBaseException(noSuchElementException)
    }
  }
}