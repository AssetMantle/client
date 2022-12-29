package models.blockchain

import cosmos.gov.v1beta1.{Tx => govTx}
import exceptions.BaseException
import models.Trait.Logging
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.blockchain.GetProposalVote
import queries.responses.blockchain.ProposalVoteResponse.{Response => ProposalVoteResponse}
import queries.responses.common.Header
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ProposalVote(proposalID: Int, voter: String, option: String, createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging

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

  private def add(proposalVote: ProposalVote): Future[Int] = db.run((proposalVoteTable returning proposalVoteTable.map(_.proposalID) += proposalVote).asTry).map {
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

  private def tryGetByID(proposalID: Int): Future[ProposalVote] = db.run(proposalVoteTable.filter(_.proposalID === proposalID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getByID(proposalID: Int): Future[Seq[ProposalVote]] = db.run(proposalVoteTable.filter(_.proposalID === proposalID).result)

  private[models] class ProposalVoteTable(tag: Tag) extends Table[ProposalVote](tag, "ProposalVote") {

    def * = (proposalID, voter, option, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (ProposalVote.tupled, ProposalVote.unapply)

    def proposalID = column[Int]("proposalID", O.PrimaryKey)

    def voter = column[String]("voter", O.PrimaryKey)

    def option = column[String]("option")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")
  }

  object Service {

    def tryGet(proposalID: Int): Future[ProposalVote] = tryGetByID(proposalID)

    def insertOrUpdate(proposalVote: ProposalVote): Future[Int] = upsert(proposalVote)

    def getAllByID(proposalID: Int): Future[Seq[ProposalVote]] = getByID(proposalID)
  }

  object Utility {

    def onVote(vote: govTx.MsgVote)(implicit header: Header): Future[Unit] = {
      val upsert = Service.insertOrUpdate(ProposalVote(proposalID = vote.getProposalId.toInt, voter = vote.getVoter, option = vote.getOption.toString))

      (for {
        _ <- upsert
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.VOTE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def insertOrUpdateProposal(proposalID: String, address: String): Future[Unit] = {
      val proposalVoteResponse = getProposalVote.Service.get(id = proposalID, address = address)

      def upsert(proposalVoteResponse: ProposalVoteResponse) = Service.insertOrUpdate(proposalVoteResponse.vote.toSerializableProposalVote)

      (for {
        proposalVoteResponse <- proposalVoteResponse
        _ <- upsert(proposalVoteResponse)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }
  }

}