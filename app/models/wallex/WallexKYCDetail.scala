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

case class WallexKYCDetail(
    id: String,
    orgId: String,
    wallexId: String,
    url: String,
    documentName: String,
    documentType: String,
    createdBy: Option[String] = None,
    createdOn: Option[Timestamp] = None,
    createdOnTimeZone: Option[String] = None,
    updatedBy: Option[String] = None,
    updatedOn: Option[Timestamp] = None,
    updatedOnTimeZone: Option[String] = None
) extends Logged

@Singleton
class WallexKYCDetails @Inject() (
    protected val databaseConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String =
    constants.Module.WALLEX_KYC_DETAIL

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val wallexKycDetailTable =
    TableQuery[WallexKYCDetailTable]

  private def add(
      wallexKYCDetail: WallexKYCDetail
  ): Future[String] =
    db.run(
        (wallexKycDetailTable returning wallexKycDetailTable
          .map(_.id) += wallexKYCDetail).asTry
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
      wallexKYCDetail: WallexKYCDetail
  ): Future[Int] =
    db.run(
        wallexKycDetailTable
          .insertOrUpdate(wallexKYCDetail)
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
  ): Future[Option[WallexKYCDetail]] =
    db.run(
      wallexKycDetailTable
        .filter(_.id === id)
        .result
        .headOption
    )

  private[models] class WallexKYCDetailTable(tag: Tag)
      extends Table[WallexKYCDetail](
        tag,
        "WallexKYCDetail"
      ) {

    override def * =
      (
        id,
        orgId,
        wallexId,
        url,
        documentName,
        documentType,
        createdBy.?,
        createdOn.?,
        createdOnTimeZone.?,
        updatedBy.?,
        updatedOn.?,
        updatedOnTimeZone.?
      ) <> (WallexKYCDetail.tupled, WallexKYCDetail.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def orgId = column[String]("orgId")

    def wallexId = column[String]("wallexId")

    def url = column[String]("url")

    def documentName = column[String]("documentName")

    def documentType = column[String]("documentType", O.PrimaryKey)

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
        orgId: String,
        wallexId: String,
        url: String,
        documentName: String,
        documentType: String
    ): Future[String] =
      add(
        WallexKYCDetail(
          id = id,
          orgId = orgId,
          wallexId = wallexId,
          url = url,
          documentName = documentName,
          documentType = documentType
        )
      )

    def insertOrUpdate(
        id: String,
        orgId: String,
        wallexId: String,
        url: String,
        documentName: String,
        documentType: String
    ): Future[Int] =
      upsert(
        WallexKYCDetail(
          id = id,
          orgId = orgId,
          wallexId = wallexId,
          url = url,
          documentName = documentName,
          documentType = documentType
        )
      )

    def tryGet(id: String): Future[WallexKYCDetail] =
      findById(id).map { detail =>
        detail.getOrElse(
          throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
        )
      }

    def get(id: String): Future[Option[WallexKYCDetail]] =
      findById(id)
  }
}
