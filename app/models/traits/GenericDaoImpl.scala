package models.traits

import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick._
import slick.jdbc.H2Profile.StreamingProfileAction
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcProfile
import slick.lifted.{CanBeQueryCondition, Ordered}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

abstract class GenericDaoImpl[
  T <: Table[E] with ModelTable[PK],
  E <: Entity[PK],
  PK: BaseColumnType](
                       databaseConfigProvider: DatabaseConfigProvider,
                       tableQuery: TableQuery[T],
                       implicit val executionContext: ExecutionContext,
                       implicit val module: String,
                       implicit val logger: Logger) { //extends GenericDao[T, E, PK] {

  private val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  private val db = databaseConfig.db

  import databaseConfig.profile.api._

  def countTotal(): Future[Int] = db.run(tableQuery.length.result)

  def create(entity: E): Future[PK] = db.run((tableQuery returning tableQuery.map(_.id) += entity).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => new constants.Response.Failure(module + "_INSERT_FAILED").throwBaseException(psqlException)
    }
  }

  def create(entities: Seq[E]): Future[Unit] = db.run((tableQuery ++= entities).asTry).map {
    case Success(result) => ()
    case Failure(exception) => exception match {
      case psqlException: PSQLException => new constants.Response.Failure(module + "_INSERT_FAILED").throwBaseException(psqlException)
    }
  }

  def customQuery[C](query: StreamingProfileAction[C, _, _]) = db.run(query)

  def customQuery[C](query: DBIOAction[C, _, _]): Future[C] = db.run(query.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => new constants.Response.Failure(module + "_NOT_FOUND").throwBaseException(noSuchElementException)
    }
  }

  def customUpdate[R](updateQuery: DBIOAction[R, NoStream, Effect.Write]): Future[R] = db.run(updateQuery.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => new constants.Response.Failure(module + "_INSERT_FAILED").throwBaseException(psqlException)
    }
  }

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

  def deleteAll(): Future[Unit] = db.run(sqlu"""TRUNCATE TABLE ${tableQuery.baseTableRow.tableName} RESTART IDENTITY CASCADE""".asTry).map {
    case Success(result) => ()
    case Failure(exception) => exception match {
      case psqlException: PSQLException => new constants.Response.Failure(module + "_DELETE_ALL_FAILED").throwBaseException(psqlException)
    }
  }

  def exists(id: PK): Future[Boolean] = db.run(tableQuery.filter(_.id === id).exists.result)

  def filter[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]): Future[Seq[E]] = db.run(tableQuery.filter(expr).result)

  def filterAndCount[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]): Future[Int] = db.run(tableQuery.filter(expr).size.result)

  def filterAndDelete[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]): Future[Int] = db.run(tableQuery.filter(expr).delete)

  def filterAndExists[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]): Future[Boolean] = db.run(tableQuery.filter(expr).exists.result)

  def filterHead[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]): Future[E] = db.run(tableQuery.filter(expr).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => new constants.Response.Failure(module + "_NOT_FOUND").throwBaseException(noSuchElementException)
    }
  }

  def filterAndSort[C1 <: Rep[_], C2 <: Rep[_]](filterExpr: T => C1)(sortExpr: T => Ordered)(implicit wt: CanBeQueryCondition[C1]): Future[Seq[E]] = db.run(tableQuery.filter(filterExpr).sorted(sortExpr).result)

  def filterAndSortHead[C1 <: Rep[_], C2 <: Rep[_]](filterExpr: T => C1)(sortExpr: T => Ordered)(implicit wt: CanBeQueryCondition[C1]): Future[E] = db.run(tableQuery.filter(filterExpr).sorted(sortExpr).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => new constants.Response.Failure(module + "_NOT_FOUND").throwBaseException(noSuchElementException)
    }
  }

  def filterAndSortWithPagination[C1 <: Rep[_], C2 <: Rep[_]](filterExpr: T => C1)(sortExpr: T => Ordered)(offset: Int, limit: Int)(implicit wt: CanBeQueryCondition[C1]): Future[Seq[E]] = db.run(tableQuery.filter(filterExpr).sorted(sortExpr).drop(offset).take(limit).result)

  def filterAndCustomSortWithPagination[C1 <: Rep[_], C2 <: Rep[_]](filterExpr: T => C1)(sortExpr: T => C2)(offset: Int, limit: Int)(implicit wt: CanBeQueryCondition[C1], ev: C2 => Ordered): Future[Seq[E]] = db.run(tableQuery.filter(filterExpr).sortBy(sortExpr).drop(offset).take(limit).result)

  def filterAndSortWithOrderHead[C1 <: Rep[_], C2 <: Rep[_]](filterExpr: T => C1)(sortExpr: T => Ordered)(implicit wt: CanBeQueryCondition[C1]): Future[E] = db.run(tableQuery.filter(filterExpr).sorted(sortExpr).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => new constants.Response.Failure(module + "_NOT_FOUND").throwBaseException(noSuchElementException)
    }
  }

  def getById(id: PK): Future[Option[E]] = db.run(tableQuery.filter(_.id === id).result.headOption)

  def getByIds(ids: Seq[PK]): Future[Seq[E]] = db.run(tableQuery.filter(_.id.inSet(ids)).result)

  def getAll: Future[Seq[E]] = db.run(tableQuery.result)

  def sortWithPagination[C1 <: Rep[_]](sortExpr: T => Ordered)(offset: Int, limit: Int): Future[Seq[E]] = db.run(tableQuery.sorted(sortExpr).drop(offset).take(limit).result)

  def customSortWithPagination[C1 <: Rep[_]](sortExpr: T => C1)(offset: Int, limit: Int)(implicit ev: C1 => Ordered): Future[Seq[E]] = db.run(tableQuery.sortBy(sortExpr).drop(offset).take(limit).result)

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

  def upsert(entity: E): Future[Unit] = db.run(tableQuery.insertOrUpdate(entity).asTry).map {
    case Success(result) => ()
    case Failure(exception) => exception match {
      case psqlException: PSQLException => new constants.Response.Failure(module + "_UPSERT_FAILED").throwBaseException(psqlException)
    }
  }

  def upsertMultiple(entities: Seq[E]): Future[Unit] = db.run(DBIO.sequence(entities.map(entity => tableQuery.insertOrUpdate(entity))).asTry).map {
    case Success(result) => ()
    case Failure(exception) => exception match {
      case psqlException: PSQLException => new constants.Response.Failure(module + "_UPSERT_FAILED").throwBaseException(psqlException)
    }
  }
}