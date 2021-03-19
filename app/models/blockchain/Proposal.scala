package models.blockchain

import exceptions.BaseException
import models.Abstract.ProposalContent
import models.Trait.Logged
import models.common.ProposalContents._
import models.common.Serializable.{Coin, FinalTallyResult}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.blockchain.GetProposal
import queries.responses.blockchain.ProposalResponse.{Response => ProposalResponse}
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Proposal(id: String, content: ProposalContent, proposalType: String, status: String, finalTallyResult: FinalTallyResult, submitTime: String, depositEndTime: String, totalDeposit: Seq[Coin], votingStartTime: String, votingEndTime: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Proposals @Inject()(
                           protected val databaseConfigProvider: DatabaseConfigProvider,
                           getProposal: GetProposal,
                           configuration: Configuration,
                           utilitiesOperations: utilities.Operations
                         )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_PROPOSAL

  import databaseConfig.profile.api._

  private[models] val proposalTable = TableQuery[ProposalTable]

  case class ProposalSerialized(id: String, content: String, proposalType: String, status: String, finalTallyResult: String, submitTime: String, depositEndTime: String, totalDeposit: String, votingStartTime: String, votingEndTime: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Proposal = Proposal(id = id, content = utilities.JSON.convertJsonStringToObject[ProposalContent](content), proposalType = proposalType, status = status, finalTallyResult = utilities.JSON.convertJsonStringToObject[FinalTallyResult](finalTallyResult), submitTime = submitTime, depositEndTime = depositEndTime, totalDeposit = utilities.JSON.convertJsonStringToObject[Seq[Coin]](totalDeposit), votingStartTime = votingStartTime, votingEndTime = votingEndTime, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(proposal: Proposal): ProposalSerialized = ProposalSerialized(id = proposal.id, content = Json.toJson(proposal.content).toString, proposalType = proposal.proposalType, status = proposal.status, finalTallyResult = Json.toJson(proposal.finalTallyResult).toString, submitTime = proposal.submitTime, depositEndTime = proposal.depositEndTime, totalDeposit = Json.toJson(proposal.totalDeposit).toString, votingStartTime = proposal.votingStartTime, votingEndTime = proposal.votingEndTime, createdBy = proposal.createdBy, createdOn = proposal.createdOn, createdOnTimeZone = proposal.createdOnTimeZone, updatedBy = proposal.updatedBy, updatedOn = proposal.updatedOn, updatedOnTimeZone = proposal.updatedOnTimeZone)

  private def add(proposal: Proposal): Future[String] = db.run((proposalTable returning proposalTable.map(_.id) += serialize(proposal)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(proposal: Proposal): Future[Int] = db.run(proposalTable.insertOrUpdate(serialize(proposal)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def tryGetByID(id: String): Future[ProposalSerialized] = db.run(proposalTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getByID(id: String): Future[Option[ProposalSerialized]] = db.run(proposalTable.filter(_.id === id).result.headOption)

  private[models] class ProposalTable(tag: Tag) extends Table[ProposalSerialized](tag, "Proposal_BC") {

    def * = (id, content, proposalType, status, finalTallyResult, submitTime, depositEndTime, totalDeposit, votingStartTime, votingEndTime, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (ProposalSerialized.tupled, ProposalSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def content = column[String]("content")

    def proposalType = column[String]("proposalType")

    def status = column[String]("status")

    def finalTallyResult = column[String]("finalTallyResult")

    def submitTime = column[String]("submitTime")

    def depositEndTime = column[String]("depositEndTime")

    def totalDeposit = column[String]("totalDeposit")

    def votingStartTime = column[String]("votingStartTime")

    def votingEndTime = column[String]("votingEndTime")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def tryGet(id: String): Future[Proposal] = tryGetByID(id).map(_.deserialize)

    def insertOrUpdate(proposal: Proposal): Future[Int] = upsert(proposal)

    def get(id: String): Future[Option[Proposal]] = getByID(id).map(_.map(_.deserialize))

  }

  object Utility {

    def insertOrUpdateProposal(id: String): Future[Unit] = {
      val proposalResponse = getProposal.Service.get(id)

      def upsert(proposalResponse: ProposalResponse) = Service.insertOrUpdate(proposalResponse.proposal.toSerializableProposal)

      (for {
        proposalResponse <- proposalResponse
        _ <- upsert(proposalResponse)
      } yield ()).recover {
        case _: BaseException => Future()
      }
    }
  }

}