package models.memberCheck

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import models.Trait.HistoryLogged
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.{ExecutionContext, Future}

case class VesselScanDecisionHistory(id: String, scanID: Int, resultID: Option[Int], status: Boolean, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None, deletedBy: String, deletedOn: Timestamp, deletedOnTimeZone: String) extends HistoryLogged

@Singleton
class VesselScanDecisionHistories @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.MEMBER_CHECK_VESSEL_SCAN_DECISION_HISTORY

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val vesselScanDecisionHistoryTable = TableQuery[VesselScanDecisionTable]

  private def findById(id: String): Future[Option[VesselScanDecisionHistory]] = db.run(vesselScanDecisionHistoryTable.filter(_.id === id).result.headOption)

  private[models] class VesselScanDecisionTable(tag: Tag) extends Table[VesselScanDecisionHistory](tag, "VesselScanDecision_History") {

    def * = (id, scanID, resultID.?, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?, deletedBy, deletedOn, deletedOnTimeZone) <> (VesselScanDecisionHistory.tupled, VesselScanDecisionHistory.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def scanID = column[Int]("scanID")

    def resultID = column[Int]("resultID")

    def status = column[Boolean]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

    def deletedBy = column[String]("deletedBy")

    def deletedOn = column[Timestamp]("deletedOn")

    def deletedOnTimeZone = column[String]("deletedOnTimeZone")
  }

  object Service {
    def get(id: String): Future[Option[VesselScanDecisionHistory]] = findById(id)
  }

}
