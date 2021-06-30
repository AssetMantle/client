package models.wallex

import exceptions.BaseException
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class CollectionAccount(
    id: String,
    accountID: String,
    createdBy: Option[String] = None,
    createdOn: Option[Timestamp] = None,
    createdOnTimeZone: Option[String] = None,
    updatedBy: Option[String] = None,
    updatedOn: Option[Timestamp] = None,
    updatedOnTimeZone: Option[String] = None
) extends Logged

@Singleton
class CollectionAccounts @Inject()(
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.WALLEX_COLLECTION_ACCOUNT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val collectionAccountTable =
    TableQuery[CollectionAccountTable]

  private def add(
      collectionAccount: CollectionAccount
  ): Future[String] =
    db.run(
        (collectionAccountTable returning collectionAccountTable
          .map(_.id) += collectionAccount).asTry
      )
      .map {
        case Success(result) => result
        case Failure(exception) =>
          exception match {
            case psqlException: PSQLException =>
              throw new BaseException(
                constants.Response.PSQL_EXCEPTION,
                psqlException
              )
          }
      }

  private def upsert(
      collectionAccount: CollectionAccount
  ): Future[Int] =
    db.run(
        collectionAccountTable
          .insertOrUpdate(collectionAccount)
          .asTry
      )
      .map {
        case Success(result) => result
        case Failure(exception) =>
          exception match {
            case psqlException: PSQLException =>
              throw new BaseException(
                constants.Response.PSQL_EXCEPTION,
                psqlException
              )
          }
      }

  private def findByID(
      id: String
  ): Future[Option[CollectionAccount]] =
    db.run(
      collectionAccountTable
        .filter(_.id === id)
        .result
        .headOption
    )

  private def findByAccountID(
      accountID: String
  ): Future[Option[CollectionAccount]] =
    db.run(
      collectionAccountTable
        .filter(_.accountID === accountID)
        .result
        .headOption
    )

  private[models] class CollectionAccountTable(tag: Tag)
      extends Table[CollectionAccount](
        tag,
        "CollectionAccount"
      ) {

    override def * =
      (
        id,
        accountID,
        createdBy.?,
        createdOn.?,
        createdOnTimeZone.?,
        updatedBy.?,
        updatedOn.?,
        updatedOnTimeZone.?
      ) <> (CollectionAccount.tupled, CollectionAccount.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def accountID = column[String]("accountID")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {
    def create(
        id: String,
        accountID: String
    ): Future[String] =
      add(
        CollectionAccount(
          id = id,
          accountID = accountID
        )
      )

    def insertOrUpdate(
        id: String,
        accountID: String
    ): Future[String] =
      add(
        CollectionAccount(
          id = id,
          accountID = accountID
        )
      )

    def tryGet(id: String): Future[CollectionAccount] =
      findByID(id).map { detail =>
        detail.getOrElse(
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
        )
      }

    def get(id: String): Future[Option[CollectionAccount]] =
      findByID(id)

  def tryGetByAccountID(
      accountID: String
  ): Future[CollectionAccount] =
    findByAccountID(accountID).map { detail =>
      detail.getOrElse(
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      )
    }

  def getByAccountID(
      accountID: String
  ): Future[Option[CollectionAccount]] =
    findByAccountID(accountID)
}
}
