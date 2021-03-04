package models.kycCheck.worldcheck

import models.Trait.Logged
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

case class WorldCheckScreeningCheck(
    id: String,
    checkType: String,
    status: Option[Boolean] = None,
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

  private[models] class WorldCheckScreeningTable(tag: Tag)
      extends Table[WorldCheckScreeningCheck](tag, "WorldCheckKycFile") {

    def * =
      (
        id,
        checkType,
        status.?,
        createdBy.?,
        createdOn.?,
        createdOnTimeZone.?,
        updatedBy.?,
        updatedOn.?,
        updatedOnTimeZone.?
      ) <> (WorldCheckScreeningCheck.tupled, WorldCheckScreeningCheck.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def checkType = column[String]("checkType", O.PrimaryKey)

    def status = column[Boolean]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }
}
