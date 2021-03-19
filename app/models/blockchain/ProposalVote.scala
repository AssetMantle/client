package models.blockchain

import exceptions.BaseException
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.blockchain.GetProposalVote
import queries.responses.blockchain.ProposalVoteResponse.{Response => ProposalVoteResponse}
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ProposalVote(proposalID: String, voter: String, option: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class ProposalVotes @Inject()(
                               protected val databaseConfigProvider: DatabaseConfigProvider,
                               getProposalVote: GetProposalVote,
                               configuration: Configuration,
                               utilitiesOperations: utilities.Operations
                             )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_PROPOSAL_VOTE

  import databaseConfig.profile.api._

  private[models] val proposalVoteTable = TableQuery[ProposalVoteTable]

  private def add(proposalVote: ProposalVote): Future[String] = db.run((proposalVoteTable returning proposalVoteTable.map(_.proposalID) += proposalVote).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(proposalVote: ProposalVote): Future[Int] = db.run(proposalVoteTable.insertOrUpdate(proposalVote).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def tryGetByID(proposalID: String): Future[ProposalVote] = db.run(proposalVoteTable.filter(_.proposalID === proposalID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getByID(proposalID: String): Future[Option[ProposalVote]] = db.run(proposalVoteTable.filter(_.proposalID === proposalID).result.headOption)

  private[models] class ProposalVoteTable(tag: Tag) extends Table[ProposalVote](tag, "ProposalVote_BC") {

    def * = (proposalID, voter, option, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (ProposalVote.tupled, ProposalVote.unapply)

    def proposalID = column[String]("proposalID", O.PrimaryKey)

    def voter = column[String]("voter")

    def option = column[String]("option")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def tryGet(proposalID: String): Future[ProposalVote] = tryGetByID(proposalID)

    def insertOrUpdate(proposalVote: ProposalVote): Future[Int] = upsert(proposalVote)

    def get(proposalID: String): Future[Option[ProposalVote]] = getByID(proposalID)
  }

  object Utility {

    def insertOrUpdateProposal(proposalID: String, address: String): Future[Unit] = {
      val proposalVoteResponse = getProposalVote.Service.get(id = proposalID, address = address)

      def upsert(proposalVoteResponse: ProposalVoteResponse) = Service.insertOrUpdate(proposalVoteResponse.vote.toSerializableProposalVote)

      (for {
        proposalVoteResponse <- proposalVoteResponse
        _ <- upsert(proposalVoteResponse)
      } yield ()).recover {
        case _: BaseException => Future()
      }
    }
  }

}