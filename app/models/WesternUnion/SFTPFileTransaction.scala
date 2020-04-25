package models.WesternUnion

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SFTPFileTransaction(payerID: String, invoiceNumber: String, customerFirstName: String, customerLastName: String, customerEmailAddress: String, settlementDate: String, clientReceivedAmount: String, transactionType: String, productType: String, transactionReference: String)

@Singleton
class SFTPFileTransactions @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.WESTERN_UNION_SFTP_FILE_TRANSACTION

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val sftpFileTransactionTable = TableQuery[SFTPFileTransactionTable]

  private def add(sftpFileTransaction: SFTPFileTransaction): Future[String] = db.run((sftpFileTransactionTable returning sftpFileTransactionTable.map(_.transactionReference) += sftpFileTransaction).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(wuSFTPFileTransaction: SFTPFileTransaction): Future[Int] = db.run(sftpFileTransactionTable.insertOrUpdate(wuSFTPFileTransaction).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(transactionReference: String): Future[SFTPFileTransaction] = db.run(sftpFileTransactionTable.filter(_.transactionReference === transactionReference).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(transactionReference: String): Future[Int] = db.run(sftpFileTransactionTable.filter(_.transactionReference === transactionReference).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class SFTPFileTransactionTable(tag: Tag) extends Table[SFTPFileTransaction](tag, "SFTPFileTransaction") {

    def * = (payerID, invoiceNumber, customerFirstName, customerLastName, customerEmailAddress, settlementDate, clientReceivedAmount, transactionType, productType, transactionReference) <> (SFTPFileTransaction.tupled, SFTPFileTransaction.unapply)

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

    def create(sftpFileTransaction: SFTPFileTransaction): Future[String] = add(sftpFileTransaction)

    def get(id: String): Future[SFTPFileTransaction] = findById(id)
  }

}
