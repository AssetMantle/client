package models.blockchain

import java.sql.Timestamp

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.GetAccount
import queries.responses.AccountResponse.Response
import slick.jdbc.JdbcProfile
import utilities.MicroNumber

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Account(address: String, username: String, coins: MicroNumber = new MicroNumber(0), publicKey: String, accountNumber: String = "", sequence: String = "", dirtyBit: Boolean, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Accounts @Inject()(
                          protected val databaseConfigProvider: DatabaseConfigProvider,
                          actorSystem: ActorSystem,
                          getAccount: GetAccount,
                          configuration: Configuration
                        )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ACCOUNT

  import databaseConfig.profile.api._

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  case class AccountSerialized(address: String, username: String, coins: String, publicKey: String, accountNumber: String, sequence: String, dirtyBit: Boolean, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Account = Account(address = address, username = username, coins = new MicroNumber(BigInt(coins)), publicKey = publicKey, accountNumber = accountNumber, sequence = sequence, dirtyBit = dirtyBit, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(account: Account): AccountSerialized = AccountSerialized(address = account.address, username = account.username, coins = account.coins.toMicroString, publicKey = account.publicKey, accountNumber = account.accountNumber, sequence = account.sequence, dirtyBit = account.dirtyBit, createdBy = account.createdBy, createdOn = account.createdOn, createdOnTimeZone = account.createdOnTimeZone, updatedBy = account.updatedBy, updatedOn = account.updatedOn, updatedOnTimeZone = account.updatedOnTimeZone)

  private[models] val accountTable = TableQuery[AccountTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private val denom = configuration.get[String]("blockchain.denom")

  private def add(account: Account): Future[String] = db.run((accountTable returning accountTable.map(_.address) += serialize(account)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def tryGetAddressByUsername(username: String): Future[String] = db.run(accountTable.filter(_.username === username).map(_.address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def tryGetUsernameByAddress(address: String): Future[String] = db.run(accountTable.filter(_.address === address).map(_.username).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getUsernameByAddress(address: String): Future[Option[String]] = db.run(accountTable.filter(_.address === address).map(_.username).result.headOption)

  private def updateDirtyBitByAddress(address: String, dirtyBit: Boolean): Future[Int] = db.run(accountTable.filter(_.address === address).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getAddressesByDirtyBit(dirtyBit: Boolean): Future[Seq[String]] = db.run(accountTable.filter(_.dirtyBit === dirtyBit).map(_.address).result)

  private def updateAccountNumberSequenceCoinsAndDirtyBitByAddress(address: String, accountNumber: String, sequence: String, coins: MicroNumber, dirtyBit: Boolean): Future[Int] = db.run(accountTable.filter(_.address === address).map(x => (x.accountNumber, x.sequence, x.coins, x.dirtyBit)).update((accountNumber, sequence, coins.toMicroString, dirtyBit)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findByAddress(address: String): Future[AccountSerialized] = db.run(accountTable.filter(_.address === address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getCoinsByAddress(address: String): Future[String] = db.run(accountTable.filter(_.address === address).map(_.coins).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByAddress(address: String): Future[Int] = db.run(accountTable.filter(_.address === address).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def checkAccountExistsByUsername(username: String): Future[Boolean] = db.run(accountTable.filter(_.username === username).exists.result)

  private[models] class AccountTable(tag: Tag) extends Table[AccountSerialized](tag, "Account_BC") {

    def * = (address, username, coins, publicKey, accountNumber, sequence, dirtyBit, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (AccountSerialized.tupled, AccountSerialized.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def username = column[String]("username")

    def coins = column[String]("coins")

    def publicKey = column[String]("publicKey")

    def accountNumber = column[String]("accountNumber")

    def sequence = column[String]("sequence")

    def dirtyBit = column[Boolean]("dirtyBit")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(address: String, username: String, pubkey: String): Future[String] = add(Account(address = address, username = username, publicKey = pubkey, dirtyBit = false))

    def refreshDirty(address: String, accountNumber: String, sequence: String, coins: MicroNumber): Future[Int] = updateAccountNumberSequenceCoinsAndDirtyBitByAddress(address, accountNumber, sequence, coins, dirtyBit = false)

    def get(address: String): Future[Account] = findByAddress(address).map(_.deserialize)

    def getCoins(address: String): Future[MicroNumber] = getCoinsByAddress(address).map(new MicroNumber(_))

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

            def refreshDirty(responseAccount: Response): Future[Int] = Service.refreshDirty(responseAccount.value.address, responseAccount.value.account_number, responseAccount.value.sequence, responseAccount.value.coins.get.filter(_.denom == denom).map(_.amount).headOption.getOrElse(""))

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

  val scheduledTask = new Runnable {
    override def run(): Unit = {
      try {
        Await.result(Utility.dirtyEntityUpdater(), Duration.Inf)
      } catch {
        case exception: Exception => logger.error(exception.getMessage, exception)
      }
    }
  }

  actorSystem.scheduler.scheduleWithFixedDelay(initialDelay = schedulerInitialDelay, delay = schedulerInterval)(scheduledTask)(schedulerExecutionContext)
}