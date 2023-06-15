package models.blockchain

import com.cosmos.gov.{v1beta1 => govTx}
import exceptions.BaseException
import models.common.ProposalContents._
import models.common.Serializable.Coin
import models.traits.Logging
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.blockchain.GetProposalDeposit
import queries.responses.common.Header
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.{Failure, Success}

case class ProposalDeposit(proposalID: Int, depositor: String, amount: Seq[Coin], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging

@Singleton
class ProposalDeposits @Inject()(
                                  protected val databaseConfigProvider: DatabaseConfigProvider,
                                  getProposalDeposit: GetProposalDeposit,
                                  configuration: Configuration,
                                  utilitiesOperations: utilities.Operations,
                                  blockchainBalances: Balances,
                                  blockchainProposals: Proposals,
                                  blockchainParameters: Parameters,
                                )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_PROPOSAL_DEPOSIT

  import databaseConfig.profile.api._

  private[models] val proposalTable = TableQuery[ProposalDepositTable]

  case class ProposalDepositSerialized(proposalID: Int, depositor: String, amount: String, createdBy: Option[String], createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) {
    def deserialize: ProposalDeposit = ProposalDeposit(proposalID = proposalID, depositor = depositor, amount = utilities.JSON.convertJsonStringToObject[Seq[Coin]](amount), createdBy = createdBy, createdOnMillisEpoch = createdOnMillisEpoch, updatedBy = updatedBy, updatedOnMillisEpoch = updatedOnMillisEpoch)
  }

  def serialize(proposalDeposit: ProposalDeposit): ProposalDepositSerialized = ProposalDepositSerialized(proposalID = proposalDeposit.proposalID, depositor = proposalDeposit.depositor, amount = Json.toJson(proposalDeposit.amount).toString, createdBy = proposalDeposit.createdBy, createdOnMillisEpoch = proposalDeposit.createdOnMillisEpoch, updatedBy = proposalDeposit.updatedBy, updatedOnMillisEpoch = proposalDeposit.updatedOnMillisEpoch)

  private def add(proposalDeposit: ProposalDeposit): Future[Int] = db.run((proposalTable returning proposalTable.map(_.proposalID) += serialize(proposalDeposit)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(proposalDeposit: ProposalDeposit): Future[Int] = db.run(proposalTable.insertOrUpdate(serialize(proposalDeposit)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def tryGetByID(proposalID: Int): Future[ProposalDepositSerialized] = db.run(proposalTable.filter(_.proposalID === proposalID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getByIDAndDepositor(proposalID: Int, depositor: String): Future[Option[ProposalDepositSerialized]] = db.run(proposalTable.filter(x => x.proposalID === proposalID && x.depositor === depositor).result.headOption)

  private def getByID(proposalID: Int): Future[Seq[ProposalDepositSerialized]] = db.run(proposalTable.filter(_.proposalID === proposalID).result)

  private def deleteByID(proposalID: Int): Future[Int] = db.run(proposalTable.filter(_.proposalID === proposalID).delete)

  private[models] class ProposalDepositTable(tag: Tag) extends Table[ProposalDepositSerialized](tag, "ProposalDeposit") {

    def * = (proposalID, depositor, amount, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (ProposalDepositSerialized.tupled, ProposalDepositSerialized.unapply)

    def proposalID = column[Int]("proposalID", O.PrimaryKey)

    def depositor = column[String]("depositor", O.PrimaryKey)

    def amount = column[String]("amount")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")
  }

  object Service {

    def tryGet(proposalID: Int): Future[ProposalDeposit] = tryGetByID(proposalID).map(_.deserialize)

    def insertOrUpdate(proposalDeposit: ProposalDeposit): Future[Int] = upsert(proposalDeposit)

    def get(proposalID: Int, depositor: String): Future[Option[ProposalDeposit]] = getByIDAndDepositor(proposalID = proposalID, depositor = depositor).map(_.map(_.deserialize))

    def getByProposalID(proposalID: Int): Future[Seq[ProposalDeposit]] = getByID(proposalID).map(_.map(_.deserialize))

    def deleteByProposalID(proposalID: Int): Future[Int] = deleteByID(proposalID)

  }

  object Utility {

    def onDeposit(deposit: govTx.MsgDeposit)(implicit header: Header): Future[String] = {
      val updateProposal = blockchainProposals.Utility.insertOrUpdateProposal(deposit.getProposalId.toInt)
      val updateAccountBalance = blockchainBalances.Utility.insertOrUpdateBalance(deposit.getDepositor)
      val proposalDeposit = Service.get(deposit.getProposalId.toInt, deposit.getDepositor)

      def upsertDeposit(proposalDeposit: Option[ProposalDeposit]) = proposalDeposit.fold(Service.insertOrUpdate(ProposalDeposit(proposalID = deposit.getProposalId.toInt, depositor = deposit.getDepositor, amount = deposit.getAmountList.asScala.toSeq.map(x => Coin(x)))))(x => Service.insertOrUpdate(x.copy(amount = utilities.Blockchain.addCoins(oldCoins = x.amount, add = deposit.getAmountList.asScala.toSeq.map(x => Coin(x))))))

      (for {
        _ <- updateProposal
        proposalDeposit <- proposalDeposit
        _ <- updateAccountBalance
        _ <- upsertDeposit(proposalDeposit)
      } yield deposit.getDepositor).recover {
        case _: BaseException => logger.error(schema.constants.Messages.DEPOSIT + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          deposit.getDepositor
      }
    }

    def onNewProposalEvent(proposalID: Int, proposer: String): Future[Unit] = {
      val depositor = getProposalDeposit.Service.get(id = proposalID.toString, address = proposer).map(_.deposit.toSerializableProposalDeposit)

      def addDepositor(proposalDeposit: ProposalDeposit) = if (proposalDeposit.amount.nonEmpty) Service.insertOrUpdate(proposalDeposit) else Future(0)

      for {
        proposalDeposit <- depositor
        _ <- addDepositor(proposalDeposit)
      } yield ()
    }

    def onInactiveProposalEvent(proposalID: Int): Future[Unit] = {
      val deleteDeposits = Service.deleteByProposalID(proposalID)
      val deleteProposal = blockchainProposals.Service.delete(proposalID)

      for {
        _ <- deleteDeposits
        _ <- deleteProposal
      } yield ()
    }

    def onActiveProposalEvent(proposalID: Int): Future[Unit] = {
      val updateProposal = blockchainProposals.Utility.insertOrUpdateProposal(proposalID)
      val deposits = Service.getByProposalID(proposalID)

      def deleteDeposits() = Service.deleteByProposalID(proposalID)

      def updateBalances(deposits: Seq[ProposalDeposit]) = {
        logger.info("Processing depositor balances. Might take some time, depending on BC node. Please do not shutdown.")
        utilitiesOperations.traverse(deposits)(deposit => blockchainBalances.Utility.insertOrUpdateBalance(deposit.depositor))
      }

      def actOnProposal(proposal: Proposal): Future[Unit] = if (proposal.isPassed) {
        proposal.content.toProto.getTypeUrl match {
          case constants.Blockchain.Proposal.PARAMETER_CHANGE => blockchainParameters.Utility.onParameterChange(proposal.content.asInstanceOf[ParameterChange])
          case constants.Blockchain.Proposal.COMMUNITY_POOL_SPEND => blockchainBalances.Utility.insertOrUpdateBalance(proposal.content.asInstanceOf[CommunityPoolSpend].recipient)
          case constants.Blockchain.Proposal.SOFTWARE_UPGRADE => Future()
          case constants.Blockchain.Proposal.CANCEL_SOFTWARE_UPGRADE => Future()
          case constants.Blockchain.Proposal.TEXT => Future()
          case _ => Future()
        }
      } else Future()

      for {
        proposal <- updateProposal
        _ <- actOnProposal(proposal)
        deposits <- deposits
        _ <- updateBalances(deposits)
        _ <- deleteDeposits()
      } yield ()
    }
  }

}