package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class WUSFTPFileTransaction(payerID: String, invoiceNumber: String, customerFirstName: String, customerLastName: String, customerEmailAddress: String, settlementDate: String, clientReceivedAmount: String, transactionType: String, productType: String, transactionReference: String)

@Singleton
class WUSFTPFileTransactions @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_WU_SFTP_FILE_TRANSACTION

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val wuSFTPFileTransactionTable = TableQuery[WUSFTPFileTransactionTable]

  private def add(wuSFTPFileTransaction: WUSFTPFileTransaction): Future[String] = db.run((wuSFTPFileTransactionTable returning wuSFTPFileTransactionTable.map(_.transactionReference) += wuSFTPFileTransaction).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(wuSFTPFileTransaction: WUSFTPFileTransaction): Future[Int] = db.run(wuSFTPFileTransactionTable.insertOrUpdate(wuSFTPFileTransaction).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(transactionReference: String): Future[WUSFTPFileTransaction] = db.run(wuSFTPFileTransactionTable.filter(_.transactionReference === transactionReference).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(transactionReference: String): Future[Int] = db.run(wuSFTPFileTransactionTable.filter(_.transactionReference === transactionReference).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class WUSFTPFileTransactionTable(tag: Tag) extends Table[WUSFTPFileTransaction](tag, "WUSFTPFileTransaction") {

    def * = (payerID, invoiceNumber, customerFirstName, customerLastName, customerEmailAddress, settlementDate, clientReceivedAmount, transactionType, productType, transactionReference) <> (WUSFTPFileTransaction.tupled, WUSFTPFileTransaction.unapply)

    def transactionReference = column[String]("transactionReference", O.PrimaryKey)

    def payerID = column[String]("payerID")

    def invoiceNumber = column[String]("invoiceNumber")

    def customerFirstName = column[String]("customerFirstName")

    def customerLastName = column[String]("customerLastName")

    def customerEmailAddress = column[String]("customerEmailAddress")

    def settlementDate = column[String]("settlementDate")

    def clientReceivedAmount = column[String]("clientReceivedAmount")

    def transactionType = column[String]("transactionType")

    def productType = column[String]("productType")

  }

  object Service {

    def create(wuSFTPFileTransaction: WUSFTPFileTransaction): Future[String] = add(wuSFTPFileTransaction)

    def get(id: String): Future[WUSFTPFileTransaction] = findById(id)
  }

}
