package models.kycCheck.worldcheck

import exceptions.BaseException
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class WorldCheckScreeningCheck(
    id: String,
    checkType: String,
    status: String,
    createdBy: Option[String] = None,
    createdOn: Option[Timestamp] = None,
    createdOnTimeZone: Option[String] = None,
    updatedBy: Option[String] = None,
    updatedOn: Option[Timestamp] = None,
    updatedOnTimeZone: Option[String] = None
) extends Logged

@Singleton
class WorldCheckScreeningChecks @Inject() (
    protected val databaseConfigProvider: DatabaseConfigProvider,
    configuration: Configuration
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.WORLD_CHECK_KYC_FILE

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val worldCheckScreeningTable =
    TableQuery[WorldCheckScreeningTable]

  private def add(screeningChecks: WorldCheckScreeningCheck): Future[String] =
    db.run(
      (worldCheckScreeningTable returning worldCheckScreeningTable
        .map(_.id) += screeningChecks).asTry
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

  private def findById(id: String): Future[Seq[WorldCheckScreeningCheck]] = db.run(worldCheckScreeningTable.filter(_.id === id).result)

  private[models] class WorldCheckScreeningTable(tag: Tag)
      extends Table[WorldCheckScreeningCheck](tag, "WorldCheckScreeningCheck") {

    def * =
      (
        id,
        checkType,
        status,
        createdBy.?,
        createdOn.?,
        createdOnTimeZone.?,
        updatedBy.?,
        updatedOn.?,
        updatedOnTimeZone.?
      ) <> (WorldCheckScreeningCheck.tupled, WorldCheckScreeningCheck.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def checkType = column[String]("checkType", O.PrimaryKey)

    def status = column[String]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def getScreeningCheck(id : String) : Future[Seq[WorldCheckScreeningCheck]] = findById(id = id)
  }
}
