package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import utilities.MicroNumber

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Fiat(ownerID: String, transactionID: String, transactionAmount: MicroNumber, amountRedeemed: MicroNumber, status: Option[Boolean], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Fiats @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  def serialize(fiat: Fiat): FiatSerialized = FiatSerialized(ownerID = fiat.ownerID, transactionID = fiat.transactionID, transactionAmount = fiat.transactionAmount.toMicroString, amountRedeemed = fiat.amountRedeemed.toMicroString, status = fiat.status, createdBy = fiat.createdBy, createdOn = fiat.createdOn, createdOnTimeZone = fiat.createdOnTimeZone, updatedBy = fiat.updatedBy, updatedOn = fiat.updatedOn, updatedOnTimeZone = fiat.updatedOnTimeZone)

  case class FiatSerialized(ownerID: String, transactionID: String, transactionAmount: String, amountRedeemed: String, status: Option[Boolean], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) {
    def deserialize: Fiat = Fiat(ownerID = ownerID, transactionID = transactionID, transactionAmount = new MicroNumber(BigInt(transactionAmount)), amountRedeemed = new MicroNumber(BigInt(amountRedeemed)), status = status, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_FIAT

  import databaseConfig.profile.api._

  private[models] val fiatTable = TableQuery[FiatTable]

  private def add(fiat: FiatSerialized): Future[String] = db.run((fiatTable returning fiatTable.map(_.transactionID) += fiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def update(fiat: FiatSerialized): Future[Int] = db.run(fiatTable.filter(_.ownerID === fiat.ownerID).filter(_.transactionID === fiat.transactionID).map(x => (x.transactionAmount, x.amountRedeemed)).update((fiat.transactionAmount, fiat.amountRedeemed)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def upsert(fiat: FiatSerialized): Future[Int] = db.run(fiatTable.insertOrUpdate(fiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByPegHashAndOwnerID(transactionID: String, ownerID: String): Future[FiatSerialized] = db.run(fiatTable.filter(_.transactionID === transactionID).filter(_.ownerID === ownerID).result.head.asTry).map {
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

  private def updateAmountByOwnerIDAndTransactionID(ownerID: String, transactionID: String, transactionAmount: MicroNumber): Future[Int] = db.run(fiatTable.filter(_.transactionID === transactionID).filter(_.ownerID === ownerID).map(_.transactionAmount).update(transactionAmount.toMicroString).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateTransactionAmountByOwnerID(ownerID: String, transactionAmount: MicroNumber): Future[Int] = db.run(fiatTable.filter(_.ownerID === ownerID).map(_.transactionAmount).update(transactionAmount.toMicroString).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getFiatsByOwnerIDAndStatus(ownerID: String, status: Option[Boolean]): Future[Seq[FiatSerialized]] = db.run(fiatTable.filter(_.ownerID === ownerID).filter(_.status.? === status).result)

  private def getFiatsByOwnerIDsAndStatus(ownerIDs: Seq[String], status: Option[Boolean]): Future[Seq[FiatSerialized]] = db.run(fiatTable.filter(_.ownerID inSet ownerIDs).filter(_.status.? === status).result)

  private[models] class FiatTable(tag: Tag) extends Table[FiatSerialized](tag, "Fiat") {

    def * = (ownerID, transactionID, transactionAmount, amountRedeemed, status.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (FiatSerialized.tupled, FiatSerialized.unapply)

    def ownerID = column[String]("ownerID", O.PrimaryKey)

    def transactionID = column[String]("transactionID", O.PrimaryKey)

    def transactionAmount = column[String]("transactionAmount")

    def amountRedeemed = column[String]("amountRedeemed")

    def status = column[Boolean]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(ownerID: String, transactionID: String, transactionAmount: MicroNumber, amountRedeemed: MicroNumber): Future[String] = add(serialize(Fiat(ownerID, transactionID, transactionAmount, amountRedeemed, status = None)))

    def updateFiat(ownerID: String, transactionID: String, transactionAmount: MicroNumber, amountRedeemed: MicroNumber): Future[Int] = update(serialize(Fiat(ownerID, transactionID, transactionAmount, amountRedeemed, status = Some(true))))

    def insertOrUpdate(ownerID: String, transactionID: String, transactionAmount: MicroNumber, amountRedeemed: MicroNumber): Future[Int] = upsert(serialize(Fiat(ownerID, transactionID, transactionAmount, amountRedeemed, status = Some(true))))

    def updateAllTransactionAmountsToZero(ownerID: String): Future[Int] = updateTransactionAmountByOwnerID(ownerID, 0.0)

    def getFiatPegWallet(ownerID: String): Future[Seq[Fiat]] = getFiatsByOwnerIDAndStatus(ownerID, Option(true)).map(_.map(_.deserialize))

    def getFiatPegWallet(ownerIDs: Seq[String]): Future[Seq[Fiat]] = getFiatsByOwnerIDsAndStatus(ownerIDs, Option(true)).map(_.map(_.deserialize))

    def markSuccess(ownerID: String, transactionID: String): Future[Int] = updateStatus(ownerID, transactionID, status = true)

    def markFailure(ownerID: String, transactionID: String): Future[Int] = updateStatus(ownerID, transactionID, status = false)

    def updateTransactionAmount(ownerID: String, transactionID: String, transactionAmount: MicroNumber): Future[Int] = updateAmountByOwnerIDAndTransactionID(ownerID = ownerID, transactionID = transactionID, transactionAmount = transactionAmount)

    def insertOrUpdate(ownerID: String, transactionID: String, transactionAmount: MicroNumber, amountRedeemed: MicroNumber, status: Option[Boolean]): Future[Int] = upsert(serialize(Fiat(ownerID, transactionID = transactionID, transactionAmount = transactionAmount, amountRedeemed = amountRedeemed, status = status)))

  }

}