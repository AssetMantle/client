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

case class WallexCollectionAccountDetail(
    id: String,
    wallexId: String,
    accountId: String,
    createdBy: Option[String] = None,
    createdOn: Option[Timestamp] = None,
    createdOnTimeZone: Option[String] = None,
    updatedBy: Option[String] = None,
    updatedOn: Option[Timestamp] = None,
    updatedOnTimeZone: Option[String] = None
) extends Logged

@Singleton
class WallexCollectionAccountDetails @Inject() (
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.WALLEX_COLLECTION_ACCOUNT_DETAIL

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val wallexCollectionAccountDetailTable =
    TableQuery[WallexCollectionAccountDetailTable]

  private def add(
      wallexCollectionAccountDetail: WallexCollectionAccountDetail
  ): Future[String] =
    db.run(
        (wallexCollectionAccountDetailTable returning wallexCollectionAccountDetailTable
          .map(_.id) += wallexCollectionAccountDetail).asTry
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
      wallexCollectionAccountDetail: WallexCollectionAccountDetail
  ): Future[Int] =
    db.run(
        wallexCollectionAccountDetailTable
          .insertOrUpdate(wallexCollectionAccountDetail)
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

  private def findById(
      id: String
  ): Future[Option[WallexCollectionAccountDetail]] =
    db.run(
      wallexCollectionAccountDetailTable
        .filter(_.id === id)
        .result
        .headOption
    )

  private def findByAccountId(
      accountId: String
  ): Future[Option[WallexCollectionAccountDetail]] =
    db.run(
      wallexCollectionAccountDetailTable
        .filter(_.accountId === accountId)
        .result
        .headOption
    )

  private[models] class WallexCollectionAccountDetailTable(tag: Tag)
      extends Table[WallexCollectionAccountDetail](
        tag,
        "WallexCollectionAccountDetail"
      ) {

    override def * =
      (
        id,
        wallexId,
        accountId,
        createdBy.?,
        createdOn.?,
        createdOnTimeZone.?,
        updatedBy.?,
        updatedOn.?,
        updatedOnTimeZone.?
      ) <> (WallexCollectionAccountDetail.tupled, WallexCollectionAccountDetail.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def wallexId = column[String]("wallexId", O.PrimaryKey)

    def accountId = column[String]("accountId", O.PrimaryKey)

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
        wallexId: String,
        accountId: String
    ): Future[String] =
      add(
        WallexCollectionAccountDetail(
          id = id,
          wallexId = wallexId,
          accountId = accountId
        )
      )

    def insertOrUpdate(
        id: String,
        wallexId: String,
        accountId: String
    ): Future[String] =
      add(
        WallexCollectionAccountDetail(
          id = id,
          wallexId = wallexId,
          accountId = accountId
        )
      )

    def tryGet(id: String): Future[WallexCollectionAccountDetail] =
      findById(id).map { detail =>
        detail.getOrElse(
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
        )
      }

    def get(id: String): Future[Option[WallexCollectionAccountDetail]] =
      findById(id)

  def tryGetByAccountId(
      accountId: String
  ): Future[WallexCollectionAccountDetail] =
    findByAccountId(accountId).map { detail =>
      detail.getOrElse(
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      )
    }

  def getByAccountId(
      accountId: String
  ): Future[Option[WallexCollectionAccountDetail]] =
    findByAccountId(accountId)
}
}
