package models.blockchain

import exceptions.BaseException
import models.Trait.Logged
import models.common.Serializable.Coin
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.blockchain.GetProposalDeposit
import queries.responses.blockchain.ProposalDepositResponse.{Response => ProposalDepositResponse}
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ProposalDeposit(proposalID: String, depositor: String, amount: Seq[Coin], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class ProposalDeposits @Inject()(
                                  protected val databaseConfigProvider: DatabaseConfigProvider,
                                  getProposalDeposit: GetProposalDeposit,
                                  configuration: Configuration,
                                  utilitiesOperations: utilities.Operations
                                )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_PROPOSAL_DEPOSIT

  import databaseConfig.profile.api._

  private[models] val proposalTable = TableQuery[ProposalDepositTable]

  case class ProposalDepositSerialized(proposalID: String, depositor: String, amount: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: ProposalDeposit = ProposalDeposit(proposalID = proposalID, depositor = depositor, amount = utilities.JSON.convertJsonStringToObject[Seq[Coin]](amount), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(proposalDeposit: ProposalDeposit): ProposalDepositSerialized = ProposalDepositSerialized(proposalID = proposalDeposit.proposalID, depositor = proposalDeposit.depositor, amount = Json.toJson(proposalDeposit.amount).toString, createdBy = proposalDeposit.createdBy, createdOn = proposalDeposit.createdOn, createdOnTimeZone = proposalDeposit.createdOnTimeZone, updatedBy = proposalDeposit.updatedBy, updatedOn = proposalDeposit.updatedOn, updatedOnTimeZone = proposalDeposit.updatedOnTimeZone)

  private def add(proposalDeposit: ProposalDeposit): Future[String] = db.run((proposalTable returning proposalTable.map(_.proposalID) += serialize(proposalDeposit)).asTry).map {
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

  private def tryGetByID(proposalID: String): Future[ProposalDepositSerialized] = db.run(proposalTable.filter(_.proposalID === proposalID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getByID(proposalID: String): Future[Option[ProposalDepositSerialized]] = db.run(proposalTable.filter(_.proposalID === proposalID).result.headOption)

  private[models] class ProposalDepositTable(tag: Tag) extends Table[ProposalDepositSerialized](tag, "ProposalDeposit_BC") {

    def * = (proposalID, depositor, amount, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (ProposalDepositSerialized.tupled, ProposalDepositSerialized.unapply)

    def proposalID = column[String]("proposalID", O.PrimaryKey)

    def depositor = column[String]("depositor")

    def amount = column[String]("amount")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def tryGet(proposalID: String): Future[ProposalDeposit] = tryGetByID(proposalID).map(_.deserialize)

    def insertOrUpdate(proposalDeposit: ProposalDeposit): Future[Int] = upsert(proposalDeposit)

    def get(proposalID: String): Future[Option[ProposalDeposit]] = getByID(proposalID).map(_.map(_.deserialize))

  }

  object Utility {

    def insertOrUpdateProposalDeposit(proposalID: String, address: String): Future[Unit] = {
      val proposalResponse = getProposalDeposit.Service.get(id = proposalID, address = address)

      def upsert(proposalResponse: ProposalDepositResponse) = Service.insertOrUpdate(proposalResponse.deposit.toSerializableProposalDeposit)

      (for {
        proposalResponse <- proposalResponse
        _ <- upsert(proposalResponse)
      } yield ()).recover {
        case _: BaseException => Future()
      }
    }
  }

}