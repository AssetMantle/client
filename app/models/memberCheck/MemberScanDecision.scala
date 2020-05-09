package models.memberCheck

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Node
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class MemberScanDecision(id: String, organizationID: String, firstName: String, lastName: String, scanID: Int, resultID: Option[Int], status: Boolean, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged[MemberScanDecision] {

  def createLog()(implicit node: Node): MemberScanDecision = copy(createdBy = Option(node.id), createdOn = Option(new Timestamp(System.currentTimeMillis())), createdOnTimeZone = Option(node.timeZone))

  def updateLog()(implicit node: Node): MemberScanDecision = copy(updatedBy = Option(node.id), updatedOn = Option(new Timestamp(System.currentTimeMillis())), updatedOnTimeZone = Option(node.timeZone))

}

@Singleton
class MemberScanDecisions @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.MEMBER_CHECK_MEMBER_SCAN_DECISION

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private implicit val node: Node = Node(id = configuration.get[String]("node.id"), timeZone = configuration.get[String]("node.timeZone"))

  private[models] val memberScanDecisionTable = TableQuery[MemberScanDecisionTable]

  private def add(memberScanDecision: MemberScanDecision): Future[String] = db.run((memberScanDecisionTable returning memberScanDecisionTable.map(_.id) += memberScanDecision.createLog()).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(memberScanDecision: MemberScanDecision): Future[Int] = db.run(memberScanDecisionTable.insertOrUpdate(memberScanDecision.updateLog()).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[Option[MemberScanDecision]] = db.run(memberScanDecisionTable.filter(_.id === id).result.headOption)

  private def findScanIDByFirstNameAndLastName(firstName: String, lastName: String): Future[Option[Int]] = db.run(memberScanDecisionTable.filter(_.firstName === firstName).filter(_.lastName === lastName).map(_.scanID).result.headOption)

  private def deleteById(id: String): Future[Int] = db.run(memberScanDecisionTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class MemberScanDecisionTable(tag: Tag) extends Table[MemberScanDecision](tag, "MemberScanDecision") {

    def * = (id, organizationID, firstName, lastName, scanID, resultID.?, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (MemberScanDecision.tupled, MemberScanDecision.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def organizationID = column[String]("organizationID")

    def firstName = column[String]("firstName")

    def lastName = column[String]("lastName")

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

    def create(organizationID: String, firstName: String, lastName: String, scanID: Int, resultID: Option[Int], status: Boolean): Future[String] = add(MemberScanDecision(utilities.IDGenerator.requestID(), organizationID, firstName, lastName, scanID, resultID, status))

    def get(id: String): Future[Option[MemberScanDecision]] = findById(id)

    def getScanID(firstName: String, lastName: String): Future[Option[Int]] = findScanIDByFirstNameAndLastName(firstName, lastName)
  }

}
