package models.blockchain

import akka.actor.ActorSystem
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.master
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import transactions.{AddKey, GetSeed}

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Account(address: String, coins: Int, publicKey: String, accountNumber: Int, sequence: Int, dirtyBit: Boolean)

@Singleton
class Accounts @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, getSeed: GetSeed, addKey: AddKey, getAccount: GetAccount, masterAccounts: master.Accounts, actorSystem: ActorSystem, implicit val pushNotifications: PushNotifications)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ACCOUNT

  import databaseConfig.profile.api._

  private[models] val accountTable = TableQuery[AccountTable]

  private def add(account: Account)(implicit executionContext: ExecutionContext): Future[String] = db.run((accountTable returning accountTable.map(_.address) += account).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def updateSequenceByAddress(address: String, sequence: Int)(implicit executionContext: ExecutionContext): Future[Int] = db.run(accountTable.filter(_.address === address).map(_.sequence).update(sequence).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateDirtyBitByAddress(address: String, dirtyBit: Boolean): Future[Int] = db.run(accountTable.filter(_.address === address).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateCoinsByAddress(address: String, coins: Int)(implicit executionContext: ExecutionContext): Future[Int] = db.run(accountTable.filter(_.address === address).map(_.coins).update(coins).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateSequenceAndCoinsByAddress(address: String, sequence: Int, coins: Int)(implicit executionContext: ExecutionContext): Future[Int] = db.run(accountTable.filter(_.address === address).map(x => (x.sequence, x.coins)).update((sequence, coins)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateSequenceCoinsAndDirtyBitByAddress(address: String, sequence: Int, coins: Int, dirtyBit: Boolean): Future[Int] = db.run(accountTable.filter(_.address === address).map(x => (x.sequence, x.coins, x.dirtyBit)).update((sequence, coins, dirtyBit)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByAddress(address: String)(implicit executionContext: ExecutionContext): Future[Account] = db.run(accountTable.filter(_.address === address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getCoinsByAddress(address: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(accountTable.filter(_.address === address).map(_.coins).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAccountsByDirtyBit(dirtyBit: Boolean): Future[Seq[Account]] = db.run(accountTable.filter(_.dirtyBit === dirtyBit).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getSequenceByAddress(address: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(accountTable.filter(_.address === address).map(_.sequence).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByAddress(address: String)(implicit executionContext: ExecutionContext) = db.run(accountTable.filter(_.address === address).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class AccountTable(tag: Tag) extends Table[Account](tag, "Account_BC") {

    def * = (address, coins, publicKey, accountNumber, sequence, dirtyBit) <> (Account.tupled, Account.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def coins = column[Int]("coins")

    def publicKey = column[String]("publicKey")

    def accountNumber = column[Int]("accountNumber")

    def sequence = column[Int]("sequence")

    def dirtyBit = column[Boolean]("dirtyBit")
  }

  if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
    actorSystem.scheduler.schedule(initialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds, interval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").second) {
      Utility.Iterator()
    }
  }

  object Service {

    def addAccount(username: String, password: String)(implicit executionContext: ExecutionContext): String = {
      val addKeyResponse = addKey.Service.post(addKey.Request(username, password, getSeed.Service.get().body))
      Await.result(add(Account(addKeyResponse.address, 0, addKeyResponse.pub_key, -1, 0, dirtyBit = false)), Duration.Inf)
    }

    def getSequence(address: String)(implicit executionContext: ExecutionContext): Int = Await.result(getSequenceByAddress(address), Duration.Inf)

    def updateSequence(address: String, sequence: Int)(implicit executionContext: ExecutionContext): Int = Await.result(updateSequenceByAddress(address, sequence), Duration.Inf)

    def updateCoins(address: String, coins: Int)(implicit executionContext: ExecutionContext): Int = Await.result(updateCoinsByAddress(address, coins), Duration.Inf)

    def updateSequenceAndCoins(address: String, sequence: Int, coins: Int)(implicit executionContext: ExecutionContext): Int = Await.result(updateSequenceAndCoinsByAddress(address, sequence, coins), Duration.Inf)

    def updateSequenceCoinsAndDirtyBit(address: String, sequence: Int, coins: Int, dirtyBit: Boolean): Int = Await.result(updateSequenceCoinsAndDirtyBitByAddress(address, sequence, coins, dirtyBit), Duration.Inf)

    def getAccount(address: String)(implicit executionContext: ExecutionContext): Account = Await.result(findByAddress(address), Duration.Inf)

    def getCoins(address: String)(implicit executionContext: ExecutionContext): Int = Await.result(getCoinsByAddress(address), Duration.Inf)

    def getDirtyBits(dirtyBit: Boolean): Seq[Account] = Await.result(getAccountsByDirtyBit(dirtyBit), Duration.Inf)

    def updateDirtyBit(address: String, dirtyBit: Boolean): Int = Await.result(updateDirtyBitByAddress(address, dirtyBit), Duration.Inf)
  }

  object Utility {
    def Iterator(): Unit = {
      try {
        val dirtyAccounts = Service.getDirtyBits(dirtyBit = true)
        Thread.sleep(configuration.get[Long]("blockchain.kafka.entityIterator.threadSleep"))
        for (dirtyAccount <- dirtyAccounts) {
          val responseAccount = getAccount.Service.get(dirtyAccount.address)
          Service.updateSequenceCoinsAndDirtyBit(responseAccount.value.address, responseAccount.value.sequence.toInt, responseAccount.value.coins.get.filter(_.denom == configuration.get[String]("blockchain.denom.gas")).map(_.amount.toInt).sum, dirtyBit = false)
        }
      } catch {
        case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
        case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
      }
    }
  }

}