package models.blockchain

import exceptions.BaseException
import models.Abstract.ProposalContent
import models.Trait.Logged
import models.common.Parameters.{GovernanceParameter, MintingParameter}
import models.common.ProposalContents._
import models.common.Serializable.{Coin, FinalTallyResult}
import models.common.TransactionMessages.{Deposit, SubmitProposal}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.blockchain.GetProposal
import queries.responses.blockchain.ProposalResponse.{Response => ProposalResponse}
import queries.responses.common.Header
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Proposal(id: Int, content: ProposalContent, status: String, finalTallyResult: FinalTallyResult, submitTime: String, depositEndTime: String, totalDeposit: Seq[Coin], votingStartTime: String, votingEndTime: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {

  def addDeposit(depositCoins: Seq[Coin]): Proposal = Proposal(
    id = id, content = content, status = status, finalTallyResult = finalTallyResult, submitTime = submitTime, depositEndTime = depositEndTime,
    totalDeposit = utilities.Blockchain.addCoins(totalDeposit, depositCoins),
    votingStartTime = votingStartTime, votingEndTime = votingEndTime)

  def isTotalDepositGTEMinimum(minimumDeposit: Seq[Coin]): Boolean = {
    if (minimumDeposit.isEmpty) {
      return true
    }

    if (totalDeposit.isEmpty) {
      return false
    }

    minimumDeposit.foreach(minimumCoin => {
      if (!totalDeposit.find(_.denom == minimumCoin.denom).fold(false)(_.amount >= minimumCoin.amount)) {
        return false
      }
    })
    true
  }

  def activateVotingPeriod(currentTime: String, votingPeriod: Long): Proposal = Proposal(
    id = id, content = content,
    status = constants.Blockchain.Proposal.Status.VOTING_PERIOD,
    finalTallyResult = finalTallyResult, submitTime = submitTime, depositEndTime = depositEndTime,
    totalDeposit = totalDeposit,
    votingStartTime = currentTime,
    votingEndTime = utilities.Date.addTime(currentTime, votingPeriod)
  )

  def isPassed: Boolean = status match {
    case constants.Blockchain.Proposal.Status.PASSED => true
    case _ => false
  }

}

@Singleton
class Proposals @Inject()(
                           protected val databaseConfigProvider: DatabaseConfigProvider,
                           getProposal: GetProposal,
                           configuration: Configuration,
                           utilitiesOperations: utilities.Operations,
                           blockchainParameters: Parameters,
                           blockchainBalances: Balances,
                           blockchainValidators: Validators,
                           blockchainTokens: Tokens,
                         )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_PROPOSAL

  private val startingProposalID = configuration.get[Int]("blockchain.govStartingProposalID")

  import databaseConfig.profile.api._

  private[models] val proposalTable = TableQuery[ProposalTable]

  case class ProposalSerialized(id: Int, content: String, status: String, finalTallyResult: String, submitTime: String, depositEndTime: String, totalDeposit: String, votingStartTime: String, votingEndTime: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Proposal = Proposal(id = id, content = utilities.JSON.convertJsonStringToObject[ProposalContent](content), status = status, finalTallyResult = utilities.JSON.convertJsonStringToObject[FinalTallyResult](finalTallyResult), submitTime = submitTime, depositEndTime = depositEndTime, totalDeposit = utilities.JSON.convertJsonStringToObject[Seq[Coin]](totalDeposit), votingStartTime = votingStartTime, votingEndTime = votingEndTime, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(proposal: Proposal): ProposalSerialized = ProposalSerialized(id = proposal.id, content = Json.toJson(proposal.content).toString, status = proposal.status, finalTallyResult = Json.toJson(proposal.finalTallyResult).toString, submitTime = proposal.submitTime, depositEndTime = proposal.depositEndTime, totalDeposit = Json.toJson(proposal.totalDeposit).toString, votingStartTime = proposal.votingStartTime, votingEndTime = proposal.votingEndTime, createdBy = proposal.createdBy, createdOn = proposal.createdOn, createdOnTimeZone = proposal.createdOnTimeZone, updatedBy = proposal.updatedBy, updatedOn = proposal.updatedOn, updatedOnTimeZone = proposal.updatedOnTimeZone)

  private def add(proposal: Proposal): Future[Int] = db.run((proposalTable returning proposalTable.map(_.id) += serialize(proposal)).asTry).map {
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

  private def tryGetByID(id: Int): Future[ProposalSerialized] = db.run(proposalTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByID(id: Int): Future[Int] = db.run(proposalTable.filter(_.id === id).delete)

  private def getByID(id: Int): Future[Option[ProposalSerialized]] = db.run(proposalTable.filter(_.id === id).result.headOption)

  private def getMaxProposalID: Future[Int] = db.run(proposalTable.map(_.id).max.result.asTry).map {
    case Success(result) => result.getOrElse(0)
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def getAllProposals: Future[Seq[ProposalSerialized]] = db.run(proposalTable.result)

  private[models] class ProposalTable(tag: Tag) extends Table[ProposalSerialized](tag, "Proposal_BC") {

    def * = (id, content, status, finalTallyResult, submitTime, depositEndTime, totalDeposit, votingStartTime, votingEndTime, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (ProposalSerialized.tupled, ProposalSerialized.unapply)

    def id = column[Int]("id", O.PrimaryKey)

    def content = column[String]("content")

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

    def tryGet(id: Int): Future[Proposal] = tryGetByID(id).map(_.deserialize)

    def insertOrUpdate(proposal: Proposal): Future[Int] = upsert(proposal)

    def get(id: Int): Future[Option[Proposal]] = getByID(id).map(_.map(_.deserialize))

    def getLatestProposalID: Future[Int] = getMaxProposalID

    def getAllActiveProposals(time: String): Future[Seq[Proposal]] = getAllProposals.map(_.filter(x => x.votingEndTime != "" && utilities.Date.isMature(completionTimestamp = x.votingEndTime, currentTimeStamp = time)).map(_.deserialize))

    def getAllInactiveProposals(time: String): Future[Seq[Proposal]] = getAllProposals.map(_.filter(x => x.depositEndTime != "" && utilities.Date.isMature(completionTimestamp = x.depositEndTime, currentTimeStamp = time)).map(_.deserialize))

    def delete(id: Int): Future[Int] = deleteByID(id)

    def get(): Future[Seq[Proposal]] = getAllProposals.map(_.map(_.deserialize))

  }

  object Utility {

    def onSubmitProposal(submitProposal: SubmitProposal)(implicit header: Header): Future[Unit] = {
      val latestProposalID = Service.getLatestProposalID

      def upsert(latestProposalID: Int) = insertOrUpdateProposal(latestProposalID + startingProposalID)

      (for {
        latestProposalID <- latestProposalID
        _ <- upsert(latestProposalID)
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.SUBMIT_PROPOSAL + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def insertOrUpdateProposal(id: Int): Future[Proposal] = {
      val proposalResponse = getProposal.Service.get(id)

      def upsert(proposalResponse: ProposalResponse) = Service.insertOrUpdate(proposalResponse.proposal.toSerializableProposal)

      (for {
        proposalResponse <- proposalResponse
        _ <- upsert(proposalResponse)
      } yield proposalResponse.proposal.toSerializableProposal).recover {
        case baseException: BaseException => throw baseException
      }
    }

    // WARNING: Risky to use due to state difference
    //    def tally(finalTallyResult: FinalTallyResult): Future[(Boolean, Boolean)] = {
    //      val governanceParameter = blockchainParameters.Service.tryGetGovernanceParameter
    //      val totalVotingPower = blockchainValidators.Service.getTotalVotingPower.map(x => BigDecimal(x.toMicroString))
    //      val totalBondedTokens = blockchainTokens.Service.getTotalBondedAmount.map(x => BigDecimal(x.toMicroString))
    //
    //      def getResults(governanceParameter: GovernanceParameter, totalVotingPower: BigDecimal, totalBondedTokens: BigDecimal): (Boolean, Boolean) = {
    //        // Maintain this order
    //        if (totalBondedTokens == 0) {
    //          return (false, false)
    //        }
    //
    //        if ((totalVotingPower / totalBondedTokens) < governanceParameter.quorum) {
    //          return (false, true)
    //        }
    //
    //        if ((totalVotingPower - finalTallyResult.abstain) == 0) {
    //          return (false, false)
    //        }
    //
    //        if ((finalTallyResult.noWithVeto / totalVotingPower) > governanceParameter.vetoThreshold) {
    //          return (false, true)
    //        }
    //
    //        if ((finalTallyResult.yes / (totalVotingPower - finalTallyResult.abstain)) > governanceParameter.threshold) {
    //          (true, false)
    //        }
    //        else {
    //          (false, false)
    //        }
    //      }
    //
    //      (for {
    //        governanceParameter <- governanceParameter
    //        totalVotingPower <- totalVotingPower
    //        totalBondedTokens <- totalBondedTokens
    //      } yield getResults(governanceParameter = governanceParameter, totalVotingPower = totalVotingPower, totalBondedTokens = totalBondedTokens)).recover {
    //        case baseException: BaseException => throw baseException
    //      }
    //    }

  }

}