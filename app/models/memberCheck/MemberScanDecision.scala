package models.memberCheck

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class MemberScanDecision(id: String, scanID: Int, resultID: Option[Int], status: Boolean, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class MemberScanDecisions @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.MEMBER_CHECK_MEMBER_SCAN_DECISION

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val memberScanDecisionTable = TableQuery[MemberScanDecisionTable]

  private def add(memberScanDecision: MemberScanDecision): Future[String] = db.run((memberScanDecisionTable returning memberScanDecisionTable.map(_.id) += memberScanDecision).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(memberScanDecision: MemberScanDecision): Future[Int] = db.run(memberScanDecisionTable.insertOrUpdate(memberScanDecision).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findById(id: String): Future[Option[MemberScanDecision]] = db.run(memberScanDecisionTable.filter(_.id === id).result.headOption)

  private def findScanIDByID(id: String): Future[Option[Int]] = db.run(memberScanDecisionTable.filter(_.id === id).map(_.scanID).result.headOption)

  private def deleteById(id: String): Future[Int] = db.run(memberScanDecisionTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class MemberScanDecisionTable(tag: Tag) extends Table[MemberScanDecision](tag, "MemberScanDecision") {

    def * = (id, scanID, resultID.?, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (MemberScanDecision.tupled, MemberScanDecision.unapply)

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

  }

  object Service {

    def create(uboID: String, scanID: Int, resultID: Option[Int], status: Boolean): Future[String] = add(MemberScanDecision(uboID, scanID, resultID, status))

    def insertOrUpdate(uboID: String, scanID: Int, resultID: Option[Int], status: Boolean): Future[Int] = upsert(MemberScanDecision(uboID, scanID, resultID, status))

    def get(id: String): Future[Option[MemberScanDecision]] = findById(id)

    def getScanID(uboID: String): Future[Option[Int]] = findScanIDByID(uboID)
  }

}
