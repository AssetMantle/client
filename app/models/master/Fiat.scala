package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Fiat(ownerID: String, transactionID: String, transactionAmount: Int, amountRedeemed: Int, status: Option[Boolean], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Fiats @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_FIAT

  import databaseConfig.profile.api._

  private[models] val fiatTable = TableQuery[FiatTable]

  private def add(fiat: Fiat): Future[String] = db.run((fiatTable returning fiatTable.map(_.transactionID) += fiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def update(fiat: Fiat): Future[Int] = db.run(fiatTable.filter(_.ownerID === fiat.ownerID).filter(_.transactionID === fiat.transactionID).map(x => (x.transactionAmount, x.amountRedeemed)).update((fiat.transactionAmount, fiat.amountRedeemed)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def upsert(fiat: Fiat): Future[Int] = db.run(fiatTable.insertOrUpdate(fiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByPegHashAndOwnerID(transactionID: String, ownerID: String): Future[Fiat] = db.run(fiatTable.filter(_.transactionID === transactionID).filter(_.ownerID === ownerID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatus(ownerID: String, transactionID: String, status: Boolean): Future[Int] = db.run(fiatTable.filter(_.transactionID === transactionID).filter(_.ownerID === ownerID).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateAmountByOwnerIDAndTransactionID(ownerID: String, transactionID: String, transactionAmount: Int): Future[Int] = db.run(fiatTable.filter(_.transactionID === transactionID).filter(_.ownerID === ownerID).map(_.transactionAmount).update(transactionAmount).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateTransactionAmountByOwnerID(ownerID: String, transactionAmount: Int): Future[Int] = db.run(fiatTable.filter(_.ownerID === ownerID).map(_.transactionAmount).update(transactionAmount).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getTransactionAmountsByTransactionID(transactionID: String): Future[Option[Int]] = db.run(fiatTable.filter(_.transactionID === transactionID).map(_.transactionAmount).sum.result)

  private def getFiatPegWalletByOwnerID(ownerID: String): Future[Seq[Fiat]] = db.run(fiatTable.filter(_.ownerID === ownerID).result)

  private def getFiatPegWalletByOwnerIDs(ownerIDs: Seq[String]): Future[Seq[Fiat]] = db.run(fiatTable.filter(_.ownerID inSet ownerIDs).result)

  private[models] class FiatTable(tag: Tag) extends Table[Fiat](tag, "Fiat") {

    def * = (ownerID, transactionID, transactionAmount, amountRedeemed, status.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Fiat.tupled, Fiat.unapply)

    def ownerID = column[String]("ownerID", O.PrimaryKey)

    def transactionID = column[String]("transactionID", O.PrimaryKey)

    def transactionAmount = column[Int]("transactionAmount")

    def amountRedeemed = column[Int]("amountRedeemed")

    def status = column[Boolean]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(ownerID: String, transactionID: String, transactionAmount: Int, amountRedeemed: Int): Future[String] = add(Fiat(ownerID, transactionID, transactionAmount, amountRedeemed, status = None))

    def updateFiat(ownerID: String, transactionID: String, transactionAmount: Int, amountRedeemed: Int): Future[Int] = update(Fiat(ownerID, transactionID, transactionAmount, amountRedeemed, status = Some(true)))

    def insertOrUpdate(ownerID: String, transactionID: String, transactionAmount: Int, amountRedeemed: Int): Future[Int] = upsert(Fiat(ownerID, transactionID, transactionAmount, amountRedeemed, status = Some(true)))

    def updateAllTransactionAmountsToZero(ownerID: String): Future[Int] = updateTransactionAmountByOwnerID(ownerID, 0)

    def getRTCBAmountsByTransactionID(transactionID: String): Future[Option[Int]] = getTransactionAmountsByTransactionID(transactionID)

    def getFiatPegWallet(ownerID: String): Future[Seq[Fiat]] = getFiatPegWalletByOwnerID(ownerID)

    def getFiatPegWallet(ownerIDs: Seq[String]): Future[Seq[Fiat]] = getFiatPegWalletByOwnerIDs(ownerIDs)

    def markSuccess(ownerID: String, transactionID: String): Future[Int] = updateStatus(ownerID, transactionID, status = true)

    def markFailure(ownerID: String, transactionID: String): Future[Int] = updateStatus(ownerID, transactionID, status = false)

    def updateTransactionAmount(ownerID: String, transactionID: String, transactionAmount: Int): Future[Int] = updateAmountByOwnerIDAndTransactionID(ownerID = ownerID, transactionID = transactionID, transactionAmount = transactionAmount)

    def insertOrUpdate(ownerID: String, transactionID: String, transactionAmount: Int, amountRedeemed: Int, status: Option[Boolean]): Future[Int] = upsert(Fiat(ownerID, transactionID = transactionID, transactionAmount = transactionAmount, amountRedeemed = amountRedeemed, status = status))

  }

}