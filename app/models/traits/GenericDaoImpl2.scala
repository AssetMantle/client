package models.traits

import org.postgresql.util.PSQLException
import slick.jdbc.H2Profile.api._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

abstract class GenericDaoImpl2[
  T <: Table[E] with ModelTable2[PK1, PK2],
  E <: Entity2[PK1, PK2],
  PK1: BaseColumnType,
  PK2: BaseColumnType]()(implicit executionContext: ExecutionContext) extends CommonGenericDao[E, T]() {

  import dbConfig.profile.api._ // Important for upsert to not generate "merge"(MySQL) statements for insertOrUpdate. Postgres doesn't have "merge"

  def deleteById1AndId2(id1: PK1, id2: PK2): Future[Int] = db.run(tableQuery.filter(x => x.id1 === id1 && x.id2 === id2).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => new constants.Response.Failure(module + "_DELETE_FAILED").throwBaseException(psqlException)
      case noSuchElementException: NoSuchElementException => new constants.Response.Failure(module + "_DELETE_FAILED").throwBaseException(noSuchElementException)
    }
  }

  def deleteMultipleById1(id1: PK1): Future[Int] = db.run(tableQuery.filter(_.id1 === id1).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => new constants.Response.Failure(module + "_DELETE_FAILED").throwBaseException(psqlException)
      case noSuchElementException: NoSuchElementException => new constants.Response.Failure(module + "_DELETE_FAILED").throwBaseException(noSuchElementException)
    }
  }

  def deleteMultipleById2(id2: PK2): Future[Int] = db.run(tableQuery.filter(_.id2 === id2).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => new constants.Response.Failure(module + "_DELETE_FAILED").throwBaseException(psqlException)
      case noSuchElementException: NoSuchElementException => new constants.Response.Failure(module + "_DELETE_FAILED").throwBaseException(noSuchElementException)
    }
  }

  def exists(id1: PK1, id2: PK2): Future[Boolean] = db.run(tableQuery.filter(x => x.id1 === id1 && x.id2 === id2).exists.result)

  def getById(id1: PK1, id2: PK2): Future[Option[E]] = db.run(tableQuery.filter(x => x.id1 === id1 && x.id2 === id2).result.headOption)

  def tryGetById1AndId2(id1: PK1, id2: PK2): Future[E] = db.run(tableQuery.filter(x => x.id1 === id1 && x.id2 === id2).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => new constants.Response.Failure(module + "_NOT_FOUND").throwBaseException(noSuchElementException)
    }
  }

  def updateById1AndId2(update: E): Future[Int] = db.run(tableQuery.filter(x => x.id1 === update.id1 && x.id2 === update.id2).update(update).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => new constants.Response.Failure(module + "_UPDATE_FAILED").throwBaseException(psqlException)
      case noSuchElementException: NoSuchElementException => new constants.Response.Failure(module + "_UPDATE_FAILED").throwBaseException(noSuchElementException)
    }
  }
}