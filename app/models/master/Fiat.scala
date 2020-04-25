package models.master

import actors.{Create, MainActor, ShutdownActor}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl.Source
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.WesternUnion.FiatRequests
import models.{master, masterTransaction}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{JsValue, Json}
import play.api.{Configuration, Logger}
import queries.responses.AccountResponse
import queries.responses.AccountResponse.Response
import queries.{GetAccount, GetOrder}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Fiat(pegHash: String, ownerID: String, transactionID: String, transactionAmount: Int, redeemedAmount: Int, status: Option[Boolean])

@Singleton
class Fiats @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, actorsCreate: actors.Create, actorSystem: ActorSystem, shutdownActors: ShutdownActor, blockchainNegotiations: Negotiations, getAccount: GetAccount, masterTransactionIssueFiatRequests: FiatRequests, masterAccounts: master.Accounts, getOrder: GetOrder)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_FIAT

  import databaseConfig.profile.api._

  private[models] val fiatTable = TableQuery[FiatTable]

  private def add(fiat: Fiat): Future[String] = db.run((fiatTable returning fiatTable.map(_.pegHash) += fiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(fiat: Fiat): Future[Int] = db.run(fiatTable.insertOrUpdate(fiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByPegHashAndOwnerID(pegHash: String, ownerID: String): Future[Fiat] = db.run(fiatTable.filter(_.pegHash === pegHash).filter(_.ownerID === ownerID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTransactionAmountsByTransactionID(transactionID: String): Future[Option[Int]] = db.run(fiatTable.filter(_.transactionID === transactionID).map(_.transactionAmount).sum.result)

  private def getFiatPegWalletByOwnerID(ownerID: String): Future[Seq[Fiat]] = db.run(fiatTable.filter(_.ownerID === ownerID).result)

  private def getFiatPegWalletByOwnerIDs(ownerIDs: Seq[String]): Future[Seq[Fiat]] = db.run(fiatTable.filter(_.ownerID inSet ownerIDs).result)

  private[models] class FiatTable(tag: Tag) extends Table[Fiat](tag, "Fiat") {

    def * = (pegHash, ownerID, transactionID, transactionAmount, redeemedAmount, status.?) <> (Fiat.tupled, Fiat.unapply)

    def pegHash = column[String]("pegHash", O.PrimaryKey)

    def ownerID = column[String]("ownerID", O.PrimaryKey)

    def transactionID = column[String]("transactionID")

    def transactionAmount = column[Int]("transactionAmount")

    def redeemedAmount = column[Int]("redeemedAmount")

    def status = column[Boolean]("status")
  }

  object Service {

    def create(pegHash: String, ownerID: String, transactionID: String, transactionAmount: Int, redeemedAmount: Int): Future[String] = add(Fiat(pegHash, ownerID, transactionID, transactionAmount, redeemedAmount, status = None))

    def getRTCBAmountsByTransactionID(transactionID: String): Future[Option[Int]] = getTransactionAmountsByTransactionID(transactionID)

    def getFiatPegWallet(ownerID: String): Future[Seq[Fiat]] = getFiatPegWalletByOwnerID(ownerID)

    def getFiatPegWallet(ownerIDs: Seq[String]): Future[Seq[Fiat]] = getFiatPegWalletByOwnerIDs(ownerIDs)

    def insertOrUpdate(pegHash: String, ownerID: String, transactionID: String, transactionAmount: Int, redeemedAmount: Int, status: Option[Boolean]): Future[Int] = upsert(Fiat(pegHash = pegHash, ownerID, transactionID = transactionID, transactionAmount = transactionAmount, redeemedAmount = redeemedAmount, status = status))

  }
}