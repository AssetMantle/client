package models.masterTransaction

import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import exceptions.BaseException
import org.postgresql.util.PSQLException

case class NameTransaction(username: String, email: Option[String], idString: String, isClaimed: Boolean)

@Singleton
class NameTransactions @Inject()(
                                  protected val databaseConfigProvider: DatabaseConfigProvider
                                )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)
  private implicit val module: String = constants.Module.BLOCKCHAIN_NAME_TRANSACTION

  import databaseConfig.profile.api._

  private[models] val nameTransactionTable = TableQuery[NameTransactionTable]

  case class NameTransactionSerialized(username: String, email: Option[String], idString: String, isClaimed: Boolean) {
    def deserialize: NameTransaction = NameTransaction(username = username, email = email, idString = idString, isClaimed = isClaimed)
  }

  def serialize(nameTransaction: NameTransaction): NameTransactionSerialized =
    NameTransactionSerialized(
      username = nameTransaction.username,
      email = nameTransaction.email,
      idString = nameTransaction.idString,
      isClaimed = nameTransaction.isClaimed
    )

  // Insert new NameTransaction
  def add(nameTransaction: NameTransaction): Future[String] = db.run((nameTransactionTable returning nameTransactionTable.map(_.idString) += serialize(nameTransaction)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.NAME_TRANSACTION_INSERT_FAILED, psqlException)
    }
  }

  // Upsert (insert or update) NameTransaction
  def upsert(nameTransaction: NameTransaction): Future[Int] = db.run(nameTransactionTable.insertOrUpdate(serialize(nameTransaction)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.NAME_TRANSACTION_UPSERT_FAILED, psqlException)
    }
  }

  // Fetch by idString
  private def tryGetByIdString(idString: String): Future[NameTransactionSerialized] = db.run(nameTransactionTable.filter(_.idString === idString).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NAME_TRANSACTION_NOT_FOUND, noSuchElementException)
    }
  }

  // Fetch by username
  private def tryGetByUsername(username: String): Future[NameTransactionSerialized] = db.run(nameTransactionTable.filter(_.username === username).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NAME_TRANSACTION_NOT_FOUND, noSuchElementException)
    }
  }

  // Get NameTransaction by idString
  def getByIdString(idString: String): Future[Option[NameTransactionSerialized]] = db.run(nameTransactionTable.filter(_.idString === idString).result.headOption)

  // Get NameTransaction by username
  def getByUsername(username: String): Future[Option[NameTransactionSerialized]] = db.run(nameTransactionTable.filter(_.username === username).result.headOption)

  private[models] class NameTransactionTable(tag: Tag) extends Table[NameTransactionSerialized](tag, "NameTransaction") {

    def * = (username, email, idString, isClaimed) <> (NameTransactionSerialized.tupled, NameTransactionSerialized.unapply)

    def username = column[String]("username")
    def email = column[Option[String]]("email")
    def idString = column[String]("idString", O.PrimaryKey)
    def isClaimed = column[Boolean]("isClaimed")
  }

  // Service to provide additional functionality
  object Service {
    def tryGet(idString: String): Future[NameTransaction] = tryGetByIdString(idString).map(_.deserialize)

    def tryFindByUsername(username: String): Future[NameTransaction] = tryGetByUsername(username).map(_.deserialize)

    def insertOrUpdate(nameTransaction: NameTransaction): Future[Int] = upsert(nameTransaction)

    def get(idString: String): Future[Option[NameTransaction]] = getByIdString(idString).map(_.map(_.deserialize))

    def findByUsername(username: String): Future[Option[NameTransaction]] = getByUsername(username).map(_.map(_.deserialize))
  }

}
