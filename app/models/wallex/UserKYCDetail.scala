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

case class UserKYCDetail(
    id: String,
    documentType: String,
    organizationID: String,
    wallexID: String,
    url: String,
    documentName: String,
    createdBy: Option[String] = None,
    createdOn: Option[Timestamp] = None,
    createdOnTimeZone: Option[String] = None,
    updatedBy: Option[String] = None,
    updatedOn: Option[Timestamp] = None,
    updatedOnTimeZone: Option[String] = None
) extends Logged

@Singleton
class UserKYCDetails @Inject() (
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.WALLEX_KYC_DETAIL

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val userKycDetailTable =
    TableQuery[UserKYCDetailTable]

  private def add(
      userKYCDetail: UserKYCDetail
  ): Future[String] =
    db.run(
        (userKycDetailTable returning userKycDetailTable
          .map(_.id) += userKYCDetail).asTry
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
      userKYCDetail: UserKYCDetail
  ): Future[Int] =
    db.run(
        userKycDetailTable
          .insertOrUpdate(userKYCDetail)
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
  ): Future[Option[UserKYCDetail]] =
    db.run(
      userKycDetailTable
        .filter(_.id === id)
        .result
        .headOption
    )

  private[models] class UserKYCDetailTable(tag: Tag)
      extends Table[UserKYCDetail](
        tag,
        "UserKYCDetail"
      ) {

    override def * =
      (
        id,
        documentType,
        organizationID,
        wallexID,
        url,
        documentName,
        createdBy.?,
        createdOn.?,
        createdOnTimeZone.?,
        updatedBy.?,
        updatedOn.?,
        updatedOnTimeZone.?
      ) <> (UserKYCDetail.tupled, UserKYCDetail.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def organizationID = column[String]("organizationID", O.PrimaryKey)

    def wallexID = column[String]("wallexID")

    def url = column[String]("url")

    def documentName = column[String]("documentName")

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
        organizationID: String,
        wallexID: String,
        url: String,
        documentName: String,
        documentType: String
    ): Future[String] =
      add(
        UserKYCDetail(
          id = id,
          organizationID = organizationID,
          wallexID = wallexID,
          url = url,
          documentName = documentName,
          documentType = documentType
        )
      )

    def insertOrUpdate(
        id: String,
        organizationID: String,
        wallexID: String,
        url: String,
        documentName: String,
        documentType: String
    ): Future[Int] =
      upsert(
        UserKYCDetail(
          id = id,
          organizationID = organizationID,
          wallexID = wallexID,
          url = url,
          documentName = documentName,
          documentType = documentType
        )
      )

    def tryGet(id: String): Future[UserKYCDetail] =
      findById(id).map { detail =>
        detail.getOrElse(
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
        )
      }

    def get(id: String): Future[Option[UserKYCDetail]] =
      findById(id)
  }
}
