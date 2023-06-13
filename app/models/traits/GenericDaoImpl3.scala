package models.traits

import exceptions.BaseException
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick._
import slick.jdbc.H2Profile.StreamingProfileAction
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcProfile
import slick.lifted.{CanBeQueryCondition, Ordered}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

abstract class GenericDaoImpl3[
  T <: Table[E] with ModelTable3[PK1, PK2, PK3],
  E <: Entity3[PK1, PK2, PK3],
  PK1: BaseColumnType,
  PK2: BaseColumnType,
  PK3: BaseColumnType](
                        databaseConfigProvider: DatabaseConfigProvider,
                        tableQuery: TableQuery[T],
                        implicit val executionContext: ExecutionContext,
                        implicit val module: String,
                        implicit val logger: Logger) { //extends GenericDao[T, E, PK] {

  private val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  private val db = databaseConfig.db

  import databaseConfig.profile.api._

  def count(): Future[Int] = db.run(tableQuery.length.result)

  def create(entity: E): Future[Unit] = db.run((tableQuery += entity).asTry).map {
    case Success(result) => ()
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(new constants.Response.Failure(module + "_INSERT_FAILED"), psqlException)
    }
  }

  def create(entities: Seq[E]): Future[Unit] = db.run((tableQuery ++= entities).asTry).map {
    case Success(result) => ()
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(new constants.Response.Failure(module + "_INSERT_FAILED"), psqlException)
    }
  }

  def customQuery[C](query: StreamingProfileAction[C, _, _]) = db.run(query)

  def customQuery[C](query: DBIOAction[C, _, _]): Future[C] = db.run(query.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(new constants.Response.Failure(module + "_NOT_FOUND"), noSuchElementException)
    }
  }

  def customUpdate[R](updateQuery: DBIOAction[R, NoStream, Effect.Write]): Future[R] = db.run(updateQuery.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(new constants.Response.Failure(module + "_INSERT_FAILED"), psqlException)
    }
  }

  def delete(id1: PK1, id2: PK2, id3: PK3): Future[Int] = db.run(tableQuery.filter(x => x.id1 === id1 && x.id2 === id2 && x.id3 === id3).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(new constants.Response.Failure(module + "_DELETE_FAILED"), psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(new constants.Response.Failure(module + "_DELETE_FAILED"), noSuchElementException)
    }
  }

  def deleteMultipleById1(id1: PK1): Future[Int] = db.run(tableQuery.filter(_.id1 === id1).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(new constants.Response.Failure(module + "_DELETE_FAILED"), psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(new constants.Response.Failure(module + "_DELETE_FAILED"), noSuchElementException)
    }
  }

  def deleteMultipleById2(id2: PK2): Future[Int] = db.run(tableQuery.filter(_.id2 === id2).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(new constants.Response.Failure(module + "_DELETE_FAILED"), psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(new constants.Response.Failure(module + "_DELETE_FAILED"), noSuchElementException)
    }
  }

  def deleteMultipleById3(id3: PK3): Future[Int] = db.run(tableQuery.filter(_.id3 === id3).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(new constants.Response.Failure(module + "_DELETE_FAILED"), psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(new constants.Response.Failure(module + "_DELETE_FAILED"), noSuchElementException)
    }
  }

  def deleteAll(): Future[Unit] = db.run(sqlu"""TRUNCATE TABLE ${tableQuery.baseTableRow.tableName} RESTART IDENTITY CASCADE""".asTry).map {
    case Success(result) => ()
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(new constants.Response.Failure(module + "_DELETE_ALL_FAILED"), psqlException)
    }
  }

  def exists(id1: PK1, id2: PK2, id3: PK3): Future[Boolean] = db.run(tableQuery.filter(x => x.id1 === id1 && x.id2 === id2 && x.id3 === id3).exists.result)

  def filter[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]): Future[Seq[E]] = db.run(tableQuery.filter(expr).result)

  def filterAndCount[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]): Future[Int] = db.run(tableQuery.filter(expr).size.result)

  def filterAndDelete[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]): Future[Int] = db.run(tableQuery.filter(expr).delete)

  def filterAndExists[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]): Future[Boolean] = db.run(tableQuery.filter(expr).exists.result)

  def filterHead[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]): Future[E] = db.run(tableQuery.filter(expr).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(new constants.Response.Failure(module + "_NOT_FOUND"), noSuchElementException)
    }
  }

  def filterAndSort[C1 <: Rep[_], C2 <: Rep[_]](filterExpr: T => C1)(sortExpr: T => Ordered)(implicit wt: CanBeQueryCondition[C1]): Future[Seq[E]] = db.run(tableQuery.filter(filterExpr).sorted(sortExpr).result)

  def filterAndSortHead[C1 <: Rep[_], C2 <: Rep[_]](filterExpr: T => C1)(sortExpr: T => Ordered)(implicit wt: CanBeQueryCondition[C1]): Future[E] = db.run(tableQuery.filter(filterExpr).sorted(sortExpr).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(new constants.Response.Failure(module + "_NOT_FOUND"), noSuchElementException)
    }
  }

  def filterAndSortWithPagination[C1 <: Rep[_], C2 <: Rep[_]](filterExpr: T => C1)(sortExpr: T => Ordered)(offset: Int, limit: Int)(implicit wt: CanBeQueryCondition[C1]): Future[Seq[E]] = db.run(tableQuery.filter(filterExpr).sorted(sortExpr).drop(offset).take(limit).result)

  def filterAndCustomSortWithPagination[C1 <: Rep[_], C2 <: Rep[_]](filterExpr: T => C1)(sortExpr: T => C2)(offset: Int, limit: Int)(implicit wt: CanBeQueryCondition[C1], ev: C2 => Ordered): Future[Seq[E]] = db.run(tableQuery.filter(filterExpr).sortBy(sortExpr).drop(offset).take(limit).result)

  def filterAndSortWithOrderHead[C1 <: Rep[_], C2 <: Rep[_]](filterExpr: T => C1)(sortExpr: T => Ordered)(implicit wt: CanBeQueryCondition[C1]): Future[E] = db.run(tableQuery.filter(filterExpr).sorted(sortExpr).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(new constants.Response.Failure(module + "_NOT_FOUND"), noSuchElementException)
    }
  }

  def getById1Id2Id3(id1: PK1, id2: PK2, id3: PK3): Future[Option[E]] = db.run(tableQuery.filter(x => x.id1 === id1 && x.id2 === id2 && x.id3 === id3).result.headOption)

  def getAll: Future[Seq[E]] = db.run(tableQuery.result)

  def sortWithPagination[C1 <: Rep[_]](sortExpr: T => Ordered)(offset: Int, limit: Int): Future[Seq[E]] = db.run(tableQuery.sorted(sortExpr).drop(offset).take(limit).result)

  def customSortWithPagination[C1 <: Rep[_]](sortExpr: T => C1)(offset: Int, limit: Int)(implicit ev: C1 => Ordered): Future[Seq[E]] = db.run(tableQuery.sortBy(sortExpr).drop(offset).take(limit).result)


  def tryGetById1Id2Id3(id1: PK1, id2: PK2, id3: PK3): Future[E] = db.run(tableQuery.filter(x => x.id1 === id1 && x.id2 === id2 && x.id3 === id3).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(new constants.Response.Failure(module + "_NOT_FOUND"), noSuchElementException)
    }
  }

  def updateById1Id2Id3(update: E): Future[Unit] = db.run(tableQuery.filter(x => x.id1 === update.id1 && x.id2 === update.id2 && x.id3 === update.id3).update(update).asTry).map {
    case Success(result) => ()
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(new constants.Response.Failure(module + "_UPDATE_FAILED"), psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(new constants.Response.Failure(module + "_UPDATE_FAILED"), noSuchElementException)
    }
  }

  def upsert(entity: E): Future[Unit] = db.run(tableQuery.insertOrUpdate(entity).asTry).map {
    case Success(result) => ()
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(new constants.Response.Failure(module + "_UPSERT_FAILED"), psqlException)
    }
  }

  def upsertMultiple(entities: Seq[E]): Future[Unit] = db.run(DBIO.sequence(entities.map(entity => tableQuery.insertOrUpdate(entity))).asTry).map {
    case Success(result) => ()
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(new constants.Response.Failure(module + "_UPSERT_FAILED"), psqlException)
    }
  }

}