package models.blockchain

import actors.{MainAccountActor, ShutdownActors}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, OverflowStrategy}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.master
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{JsValue, Json}
import play.api.{Configuration, Logger}
import queries.GetAccount
import slick.jdbc.JdbcProfile
import transactions.{AddKey, GetSeed}
import utilities.PushNotification

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Account(address: String, coins: String, publicKey: String, accountNumber: Int, sequence: Int, dirtyBit: Boolean)

case class AccountCometMessage(username: String, message: JsValue)

@Singleton
class Accounts @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, actorSystem: ActorSystem, shutdownActors: ShutdownActors, getSeed: GetSeed, addKey: AddKey, getAccount: GetAccount, masterAccounts: master.Accounts, implicit val pushNotification: PushNotification)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ACCOUNT

  private implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  import databaseConfig.profile.api._

  private val actorTimeout = configuration.get[Int]("akka.actors.timeout").seconds

  private val cometActorSleepTime = configuration.get[Long]("akka.actors.cometActorSleepTime")

  val mainAccountActor: ActorRef = actorSystem.actorOf(props = MainAccountActor.props(actorTimeout, actorSystem), name = constants.Module.ACTOR_MAIN_ACCOUNT)

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

  private def updateSequenceCoinsAndDirtyBitByAddress(address: String, sequence: Int, coins: String, dirtyBit: Boolean): Future[Int] = db.run(accountTable.filter(_.address === address).map(x => (x.sequence, x.coins, x.dirtyBit)).update((sequence, coins, dirtyBit)).asTry).map {
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

  private def deleteByAddress(address: String)= db.run(accountTable.filter(_.address === address).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class AccountTable(tag: Tag) extends Table[Account](tag, "Account_BC") {

    def * = (address, coins, publicKey, accountNumber, sequence, dirtyBit) <> (Account.tupled, Account.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def coins = column[String]("coins")

    def publicKey = column[String]("publicKey")

    def accountNumber = column[Int]("accountNumber")

    def sequence = column[Int]("sequence")

    def dirtyBit = column[Boolean]("dirtyBit")
  }

  object Service {

    def create(username: String, password: String): String = {
      val addKeyResponse = addKey.Service.post(addKey.Request(username, password))
      Await.result(add(Account(addKeyResponse.address, "0", addKeyResponse.pubkey, -1, 0, dirtyBit = false)), Duration.Inf)
    }

    def refreshDirty(address: String, sequence: Int, coins: String): Int = Await.result(updateSequenceCoinsAndDirtyBitByAddress(address, sequence, coins, dirtyBit = false), Duration.Inf)

    def get(address: String): Account = Await.result(findByAddress(address), Duration.Inf)

    def getCoins(address: String): String = Await.result(getCoinsByAddress(address), Duration.Inf)

    def getDirtyAddresses: Seq[String] = Await.result(getAddressesByDirtyBit(dirtyBit = true), Duration.Inf)

    def markDirty(address: String): Int = Await.result(updateDirtyBitByAddress(address, dirtyBit = true), Duration.Inf)

    def accountCometSource(username: String) = {
      shutdownActors.shutdown(constants.Module.ACTOR_MAIN_ACCOUNT, username)
      Thread.sleep(cometActorSleepTime)
      val (systemUserActor, source) = Source.actorRef[JsValue](0, OverflowStrategy.dropHead).preMaterialize()
      mainAccountActor ! actors.CreateAccountChildActorMessage(username = username, actorRef = systemUserActor)
      source
    }
  }

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = Future {
      try {
        val dirtyAddresses = Service.getDirtyAddresses
        Thread.sleep(sleepTime)
        for (dirtyAddress <- dirtyAddresses) {
          val responseAccount = getAccount.Service.get(dirtyAddress)
          Service.refreshDirty(responseAccount.value.address, responseAccount.value.sequence.toInt, responseAccount.value.coins.get.filter(_.denom == denominationOfGasToken).map(_.amount).head)
          mainAccountActor ! AccountCometMessage(username = masterAccounts.Service.getId(dirtyAddress), message = Json.toJson(constants.Comet.PING))
        }
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
      }
    }
  }

  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }
}