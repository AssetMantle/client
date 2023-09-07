package models.blockchain

import com.cosmos.gov.{v1beta1 => govTx}
import exceptions.BaseException
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import queries.responses.common.Header
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.{Failure, Success}

case class ProposalVote(proposalID: Int, voter: String, option: String, weight: BigDecimal)

@Singleton
class ProposalVotes @Inject()(
                               protected val databaseConfigProvider: DatabaseConfigProvider,
                               blockchainValidators: Validators,
                               utilitiesOperations: utilities.Operations
                             )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_PROPOSAL_VOTE

  import databaseConfig.profile.api._

  private[models] val proposalVoteTable = TableQuery[ProposalVoteTable]

  private def create(proposalVote: ProposalVote): Future[Int] = db.run((proposalVoteTable returning proposalVoteTable.map(_.proposalID) += proposalVote).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(proposalVote: ProposalVote): Future[Int] = db.run((proposalVoteTable.insertOrUpdate(proposalVote)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def create(proposalVotes: Seq[ProposalVote]) = db.run((proposalVoteTable ++= proposalVotes).asTry).map {
    case Success(result) => ()
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def deleteByVoterAndProposal(address: String, proposalID: Int): Future[Int] = db.run(proposalVoteTable.filter(x => x.voter === address && x.proposalID === proposalID).delete)

  private def tryGetByID(proposalID: Int): Future[ProposalVote] = db.run(proposalVoteTable.filter(_.proposalID === proposalID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getByID(proposalID: Int): Future[Seq[ProposalVote]] = db.run(proposalVoteTable.filter(_.proposalID === proposalID).result)

  private def getByIDAndAddresses(proposalID: Int, addresses: Seq[String]): Future[Seq[ProposalVote]] = db.run(proposalVoteTable.filter(x => x.proposalID === proposalID && x.voter.inSet(addresses)).result)

  private[models] class ProposalVoteTable(tag: Tag) extends Table[ProposalVote](tag, "ProposalVote") {

    def * = (proposalID, voter, option, weight) <> (ProposalVote.tupled, ProposalVote.unapply)

    def proposalID = column[Int]("proposalID", O.PrimaryKey)

    def voter = column[String]("voter", O.PrimaryKey)

    def option = column[String]("option")

    def weight = column[BigDecimal]("weight")
  }

  object Service {

    def tryGet(proposalID: Int): Future[ProposalVote] = tryGetByID(proposalID)

    def add(proposalVote: ProposalVote): Future[Int] = create(proposalVote)

    def add(proposalVotes: Seq[ProposalVote]): Future[Unit] = create(proposalVotes)

    def getAllByID(proposalID: Int): Future[Seq[ProposalVote]] = getByID(proposalID)

    def getAllByIDAndAddresses(proposalID: Int, addresses: Seq[String]): Future[Seq[ProposalVote]] = getByIDAndAddresses(proposalID, addresses)

    def deleteAllVotesForProposal(address: String, proposalID: Int): Future[Int] = deleteByVoterAndProposal(address = address, proposalID = proposalID)

  }

  object Utility {

    def onVote(vote: govTx.MsgVote)(implicit header: Header): Future[String] = {
      val delete = Service.deleteAllVotesForProposal(address = vote.getVoter, proposalID = vote.getProposalId.toInt)
      val isValidator = blockchainValidators.Service.exists(utilities.Crypto.convertAccountAddressToOperatorAddress(vote.getVoter))

      def add(isValidator: Boolean) = if (isValidator) Service.add(ProposalVote(proposalID = vote.getProposalId.toInt, voter = vote.getVoter, option = vote.getOption.toString, weight = 1))
      else Future(0)

      (for {
        _ <- delete
        isValidator <- isValidator
        _ <- add(isValidator)
      } yield vote.getVoter).recover {
        case _: BaseException => logger.error(schema.constants.Messages.VOTE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          vote.getVoter
      }
    }

    def onWeightedVote(vote: govTx.MsgVoteWeighted)(implicit header: Header): Future[String] = {
      val delete = Service.deleteAllVotesForProposal(address = vote.getVoter, proposalID = vote.getProposalId.toInt)
      val isValidator = blockchainValidators.Service.exists(utilities.Crypto.convertAccountAddressToOperatorAddress(vote.getVoter))
      val proposalVotes = vote.getOptionsList.asScala.toSeq.map(x => ProposalVote(proposalID = vote.getProposalId.toInt, voter = vote.getVoter, option = x.getOption.toString, weight = BigDecimal(x.getWeight)))

      def add(isValidator: Boolean) = if (isValidator) Service.add(proposalVotes) else Future()

      (for {
        _ <- delete
        isValidator <- isValidator
        _ <- add(isValidator)
      } yield vote.getVoter).recover {
        case _: BaseException => logger.error(schema.constants.Messages.VOTE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          vote.getVoter
      }
    }
  }

}