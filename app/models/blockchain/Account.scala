package models.blockchain

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.GetAccount
import queries.responses.AccountResponse.Response
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Account(address: String, username: String, coins: String = "", publicKey: String, accountNumber: String = "", sequence: String = "", dirtyBit: Boolean)

@Singleton
class Accounts @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, actorSystem: ActorSystem, getAccount: GetAccount, implicit val utilitiesNotification: utilities.Notification)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ACCOUNT

  import databaseConfig.profile.api._

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  private[models] val accountTable = TableQuery[AccountTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private val denominationOfGasToken = configuration.get[String]("blockchain.denom.gas")

  private def add(account: Account): Future[String] = db.run((accountTable returning accountTable.map(_.address) += account).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def tryGetAddressByUsername(username: String): Future[String] = db.run(accountTable.filter(_.username === username).map(_.address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def tryGetUsernameByAddress(address: String): Future[String] = db.run(accountTable.filter(_.address === address).map(_.username).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getUsernameByAddress(address: String): Future[Option[String]] = db.run(accountTable.filter(_.address === address).map(_.username).result.headOption)

  private def updateDirtyBitByAddress(address: String, dirtyBit: Boolean): Future[Int] = db.run(accountTable.filter(_.address === address).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAddressesByDirtyBit(dirtyBit: Boolean): Future[Seq[String]] = db.run(accountTable.filter(_.dirtyBit === dirtyBit).map(_.address).result)

  private def updateAccountNumberSequenceCoinsAndDirtyBitByAddress(address: String, accountNumber: String, sequence: String, coins: String, dirtyBit: Boolean): Future[Int] = db.run(accountTable.filter(_.address === address).map(x => (x.accountNumber, x.sequence, x.coins, x.dirtyBit)).update((accountNumber, sequence, coins, dirtyBit)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByAddress(address: String): Future[Account] = db.run(accountTable.filter(_.address === address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getCoinsByAddress(address: String): Future[String] = db.run(accountTable.filter(_.address === address).map(_.coins).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByAddress(address: String): Future[Int] = db.run(accountTable.filter(_.address === address).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def checkAccountExistsByUsername(username: String): Future[Boolean] = db.run(accountTable.filter(_.username === username).exists.result)

  private[models] class AccountTable(tag: Tag) extends Table[Account](tag, "Account_BC") {

    def * = (address, username, coins, publicKey, accountNumber, sequence, dirtyBit) <> (Account.tupled, Account.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def username = column[String]("username")

    def coins = column[String]("coins")

    def publicKey = column[String]("publicKey")

    def accountNumber = column[String]("accountNumber")

    def sequence = column[String]("sequence")

    def dirtyBit = column[Boolean]("dirtyBit")
  }

  object Service {

    def create(address: String, username: String, pubkey: String): Future[String] = add(Account(address = address, username = username, publicKey = pubkey, dirtyBit = false))

    def refreshDirty(address: String, accountNumber: String, sequence: String, coins: String): Future[Int] = updateAccountNumberSequenceCoinsAndDirtyBitByAddress(address, accountNumber, sequence, coins, dirtyBit = false)

    def get(address: String): Future[Account] = findByAddress(address)

    def getCoins(address: String): Future[String] = getCoinsByAddress(address)

    def getDirtyAddresses: Future[Seq[String]] = getAddressesByDirtyBit(dirtyBit = true)

    def markDirty(address: String): Future[Int] = updateDirtyBitByAddress(address, dirtyBit = true)

    def tryGetAddress(username: String): Future[String] = tryGetAddressByUsername(username)

    def getUsername(address: String): Future[Option[String]] = getUsernameByAddress(address)

    def tryGetUsername(address: String): Future[String] = tryGetUsernameByAddress(address)

    def checkAccountExists(username: String): Future[Boolean] = checkAccountExistsByUsername(username)
  }

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = {
      val dirtyAddresses = Service.getDirtyAddresses
      Thread.sleep(sleepTime)

      def refreshDirtyAndSendCometMessage(dirtyAddresses: Seq[String]) = {
        Future.sequence {
          dirtyAddresses.map { dirtyAddress =>
            val responseAccount = getAccount.Service.get(dirtyAddress)
            val accountID = Service.tryGetUsername(dirtyAddress)

            def refreshDirty(responseAccount: Response): Future[Int] = Service.refreshDirty(responseAccount.value.address, responseAccount.value.account_number, responseAccount.value.sequence, responseAccount.value.coins.get.filter(_.denom == denominationOfGasToken).map(_.amount).headOption.getOrElse(""))

            for {
              responseAccount <- responseAccount
              _ <- refreshDirty(responseAccount)
              accountID <- accountID
            } yield actors.Service.cometActor ! actors.Message.makeCometMessage(username = accountID, messageType = constants.Comet.ACCOUNT, messageContent = actors.Message.Account())
          }
        }
      }

      (for {
        dirtyAddresses <- dirtyAddresses
        _ <- refreshDirtyAndSendCometMessage(dirtyAddresses)
      } yield ()).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }(schedulerExecutionContext)
}