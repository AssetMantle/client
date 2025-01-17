package models.blockchain

import com.google.protobuf.{Any => protoAny}
import exceptions.BaseException
import models.Abstract.ProposalContent
import models.common.Serializable.{Coin, FinalTallyResult}
import models.traits.Logging
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.blockchain.GetProposal
import queries.responses.blockchain.ProposalResponse.{Response => ProposalResponse}
import slick.jdbc.JdbcProfile
import utilities.Date.RFC3339

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Proposal(id: Int, content: ProposalContent, status: String, finalTallyResult: FinalTallyResult, submitTime: RFC3339, depositEndTime: RFC3339, totalDeposit: Seq[Coin], votingStartTime: RFC3339, votingEndTime: RFC3339, createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging {

  def getProposalType: String = this.content.getType

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

  def activateVotingPeriod(currentTime: RFC3339, votingPeriod: Long): Proposal = Proposal(
    id = id, content = content,
    status = schema.constants.Proposal.Status.VOTING_PERIOD,
    finalTallyResult = finalTallyResult, submitTime = submitTime, depositEndTime = depositEndTime,
    totalDeposit = totalDeposit,
    votingStartTime = currentTime,
    votingEndTime = currentTime.addEpoch(votingPeriod)
  )

  def isPassed: Boolean = status == schema.constants.Proposal.Status.PASSED

}

@Singleton
class Proposals @Inject()(
                           protected val databaseConfigProvider: DatabaseConfigProvider,
                           getProposal: GetProposal,
                           configuration: Configuration,
                           utilitiesOperations: utilities.Operations,
                         )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_PROPOSAL

  private val startingProposalID = configuration.get[Int]("blockchain.govStartingProposalID")

  import databaseConfig.profile.api._

  private[models] val proposalTable = TableQuery[ProposalTable]

  case class ProposalSerialized(id: Int, content: Array[Byte], status: String, finalTallyResult: String, submitTime: Long, depositEndTime: Long, totalDeposit: String, votingStartTime: Long, votingEndTime: Long, createdBy: Option[String], createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) {
    def deserialize: Proposal = Proposal(id = id, content = ProposalContent(protoAny.parseFrom(this.content)), status = status, finalTallyResult = utilities.JSON.convertJsonStringToObject[FinalTallyResult](finalTallyResult), submitTime = RFC3339(submitTime), depositEndTime = RFC3339(depositEndTime), totalDeposit = utilities.JSON.convertJsonStringToObject[Seq[Coin]](totalDeposit), votingStartTime = RFC3339(votingStartTime), votingEndTime = RFC3339(votingEndTime), createdBy = createdBy, createdOnMillisEpoch = createdOnMillisEpoch, updatedBy = updatedBy, updatedOnMillisEpoch = updatedOnMillisEpoch)
  }

  def serialize(proposal: Proposal): ProposalSerialized = ProposalSerialized(id = proposal.id, content = proposal.content.toProto.toByteString.toByteArray, status = proposal.status, finalTallyResult = Json.toJson(proposal.finalTallyResult).toString, submitTime = proposal.submitTime.epoch, depositEndTime = proposal.depositEndTime.epoch, totalDeposit = Json.toJson(proposal.totalDeposit).toString, votingStartTime = proposal.votingStartTime.epoch, votingEndTime = proposal.votingEndTime.epoch, createdBy = proposal.createdBy, createdOnMillisEpoch = proposal.createdOnMillisEpoch, updatedBy = proposal.updatedBy, updatedOnMillisEpoch = proposal.updatedOnMillisEpoch)

  private def add(proposal: Proposal): Future[Int] = db.run((proposalTable returning proposalTable.map(_.id) += serialize(proposal)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PROPOSAL_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(proposal: Proposal): Future[Int] = db.run(proposalTable.insertOrUpdate(serialize(proposal)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PROPOSAL_UPSERT_FAILED, psqlException)
    }
  }

  private def tryGetByID(id: Int): Future[ProposalSerialized] = db.run(proposalTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.PROPOSAL_NOT_FOUND, noSuchElementException)
    }
  }

  private def getByID(id: Int): Future[Option[ProposalSerialized]] = db.run(proposalTable.filter(_.id === id).result.headOption)

  private def deleteByID(id: Int) = db.run(proposalTable.filter(_.id === id).delete)

  private def getAllProposals: Future[Seq[ProposalSerialized]] = db.run(proposalTable.sortBy(_.id.desc).result)

  private[models] class ProposalTable(tag: Tag) extends Table[ProposalSerialized](tag, "Proposal") {

    def * = (id, content, status, finalTallyResult, submitTime, depositEndTime, totalDeposit, votingStartTime, votingEndTime, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (ProposalSerialized.tupled, ProposalSerialized.unapply)

    def id = column[Int]("id", O.PrimaryKey)

    def content = column[Array[Byte]]("content")

    def status = column[String]("status")

    def finalTallyResult = column[String]("finalTallyResult")

    def submitTime = column[Long]("submitTime")

    def depositEndTime = column[Long]("depositEndTime")

    def totalDeposit = column[String]("totalDeposit")

    def votingStartTime = column[Long]("votingStartTime")

    def votingEndTime = column[Long]("votingEndTime")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")
  }

  object Service {

    def tryGet(id: Int): Future[Proposal] = tryGetByID(id).map(_.deserialize)

    def insertOrUpdate(proposal: Proposal): Future[Int] = upsert(proposal)

    def get(id: Int): Future[Option[Proposal]] = getByID(id).map(_.map(_.deserialize))

    def get(): Future[Seq[Proposal]] = getAllProposals.map(_.map(_.deserialize))

    def delete(id: Int): Future[Int] = deleteByID(id)

  }

  object Utility {
    def insertOrUpdateProposal(id: Int): Future[Proposal] = {
      val proposalResponse = getProposal.Service.get(id)

      def upsert(proposalResponse: ProposalResponse) = Service.insertOrUpdate(proposalResponse.proposal.toSerializableProposal)

      for {
        proposalResponse <- proposalResponse
        _ <- upsert(proposalResponse)
      } yield proposalResponse.proposal.toSerializableProposal
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
    //      for {
    //        governanceParameter <- governanceParameter
    //        totalVotingPower <- totalVotingPower
    //        totalBondedTokens <- totalBondedTokens
    //      } yield getResults(governanceParameter = governanceParameter, totalVotingPower = totalVotingPower, totalBondedTokens = totalBondedTokens)
    //    }

  }

}