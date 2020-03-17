package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class AddTraderRequest(id: String, accountID: String, name: String, emailAddress: String)

@Singleton
class AddTraderRequests @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_ADD_TRADER_REQUEST

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val addTraderRequestTable = TableQuery[AddTraderRequestTable]

  private def add(addTraderRequest: AddTraderRequest): Future[String] = db.run((addTraderRequestTable returning addTraderRequestTable.map(_.id) += addTraderRequest).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[AddTraderRequest] = db.run(addTraderRequestTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def upsert(addTraderRequest: AddTraderRequest) = db.run(addTraderRequestTable.insertOrUpdate(addTraderRequest).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def deleteById(id: String) = db.run(addTraderRequestTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAllTraderRequestsByAccountID(accountID:String)=db.run(addTraderRequestTable.filter(_.accountID === accountID).result)

  private def checkEmailPresent(email:String) = db.run(addTraderRequestTable.filter(_.emailAddress === email).exists.result)

  private[models] class AddTraderRequestTable(tag: Tag) extends Table[AddTraderRequest](tag, "AddTraderRequest") {

    def * = (id, accountID, name, emailAddress) <> (AddTraderRequest.tupled, AddTraderRequest.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def accountID = column[String]("accountID")

    def name = column[String]("name")

    def emailAddress = column[String]("emailAddress")

  }

  object Service {

    def create(accountID: String,name:String, emailAddress: String): Future[String] = add(AddTraderRequest(id = utilities.IDGenerator.requestID, accountID = accountID, name = name, emailAddress = emailAddress))

    def get(id: String): Future[AddTraderRequest] = findById(id)

    def getAllTraderRequests(accountID:String)=getAllTraderRequestsByAccountID(accountID).map(_.distinct)

    def emailPresent(email:String)= checkEmailPresent(email)
  }

}
