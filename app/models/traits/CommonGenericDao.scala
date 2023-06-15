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

abstract class CommonGenericDao[E, T <: Table[E]]()(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import dbConfig.profile.api._ // Important for upsert to not generate "merge"(MySQL) statements for insertOrUpdate. Postgres doesn't have "merge"

  implicit val logger: Logger

  implicit val module: String

  val tableQuery: TableQuery[T]

  def countTotal(): Future[Int] = db.run(tableQuery.length.result)

  def create(entity: E): Future[E] = db.run((tableQuery returning tableQuery += entity).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => new constants.Response.Failure(module + "_INSERT_FAILED").throwBaseException(psqlException)
    }
  }

  def create(entities: Seq[E]): Future[Int] = if (entities.nonEmpty) db.run((tableQuery ++= entities).asTry).map {
    case Success(result) => result.getOrElse(0)
    case Failure(exception) => exception match {
      case psqlException: PSQLException => new constants.Response.Failure(module + "_INSERT_FAILED").throwBaseException(psqlException)
    }
  } else Future(0)

  def customQuery[C](query: StreamingProfileAction[C, _, _]) = db.run(query)

  def customQuery[C](query: DBIOAction[C, _, _]): Future[C] = db.run(query.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => new constants.Response.Failure(module + "_UPDATE_FAILED").throwBaseException(psqlException)
      case noSuchElementException: NoSuchElementException => new constants.Response.Failure(module + "_NOT_FOUND").throwBaseException(noSuchElementException)
    }
  }

  def customSortWithPagination[C1 <: Rep[_]](sortExpr: T => C1)(offset: Int, limit: Int)(implicit ev: C1 => Ordered): Future[Seq[E]] = db.run(tableQuery.sortBy(sortExpr).drop(offset).take(limit).result)

  def customUpdate[C](updateQuery: DBIOAction[C, NoStream, Effect.Write]): Future[C] = db.run(updateQuery.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => new constants.Response.Failure(module + "_UPDATE_FAILED").throwBaseException(psqlException)
      case noSuchElementException: NoSuchElementException => new constants.Response.Failure(module + "_UPDATE_FAILED").throwBaseException(noSuchElementException)
    }
  }

  def deleteAll(): Future[Unit] = db.run(sqlu"""TRUNCATE TABLE ${tableQuery.baseTableRow.tableName} RESTART IDENTITY CASCADE""".asTry).map {
    case Success(result) => ()
    case Failure(exception) => exception match {
      case psqlException: PSQLException => new constants.Response.Failure(module + "_DELETE_ALL_FAILED").throwBaseException(psqlException)
    }
  }

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

  def getAll: Future[Seq[E]] = db.run(tableQuery.result)

  def sortWithPagination[C1 <: Rep[_]](sortExpr: T => Ordered)(offset: Int, limit: Int): Future[Seq[E]] = db.run(tableQuery.sorted(sortExpr).drop(offset).take(limit).result)

  def upsert(entity: E): Future[Int] = db.run(tableQuery.insertOrUpdate(entity).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => new constants.Response.Failure(module + "_UPSERT_FAILED").throwBaseException(psqlException)
    }
  }

  def upsertMultiple(entities: Seq[E]): Future[Int] = db.run(DBIO.sequence(entities.map(entity => tableQuery.insertOrUpdate(entity))).asTry).map {
    case Success(result) => result.sum
    case Failure(exception) => exception match {
      case psqlException: PSQLException => new constants.Response.Failure(module + "_UPSERT_FAILED").throwBaseException(psqlException)
    }
  }
}
