package models.blockchain

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl.Source
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.westernUnion.FiatRequests
import models.{master, masterTransaction}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{JsValue, Json}
import play.api.{Configuration, Logger}
import queries.responses.AccountResponse
import queries.responses.AccountResponse.Response
import queries.{GetAccount, GetOrder}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Fiat(pegHash: String, ownerAddress: String, transactionID: String, transactionAmount: String, redeemedAmount: String, dirtyBit: Boolean)

@Singleton
class Fiats @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider,
                      actorSystem: ActorSystem, getAccount: GetAccount,
                      masterAccounts: master.Accounts, masterTraders: master.Traders,
                      masterFiats: master.Fiats)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_FIAT

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val fiatTable = TableQuery[FiatTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private def add(fiat: Fiat): Future[String] = db.run((fiatTable returning fiatTable.map(_.pegHash) += fiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def insertMultiple(fiats: Seq[Fiat]): Future[Seq[String]] = db.run((fiatTable returning fiatTable.map(_.pegHash) ++= fiats).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(fiat: Fiat): Future[Int] = db.run(fiatTable.insertOrUpdate(fiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByPegHashAndOwnerAddress(pegHash: String, ownerAddress: String): Future[Fiat] = db.run(fiatTable.filter(_.pegHash === pegHash).filter(_.ownerAddress === ownerAddress).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getFiatPegWalletByAddress(address: String): Future[Seq[Fiat]] = db.run(fiatTable.filter(_.ownerAddress === address).result)

  private def getFiatPegWalletByAddresses(addresses: Seq[String]): Future[Seq[Fiat]] = db.run(fiatTable.filter(_.ownerAddress inSet addresses).result)

  private def getFiatsByDirtyBit(dirtyBit: Boolean): Future[Seq[Fiat]] = db.run(fiatTable.filter(_.dirtyBit === dirtyBit).result)

  private def updateDirtyBitByAddress(address: String, dirtyBit: Boolean): Future[Int] = db.run(fiatTable.filter(_.ownerAddress === address).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateOwnerAddressesByPegHashes(pegHashes: Seq[String], ownerAddress: String): Future[Int] = db.run(fiatTable.filter(_.pegHash.inSet(pegHashes)).map(_.ownerAddress).update(ownerAddress).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByPegHashAndAddress(pegHash: String, address: String): Future[Int] = db.run(fiatTable.filter(_.pegHash === pegHash).filter(_.ownerAddress === address).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByAddress(address: String): Future[Int] = db.run(fiatTable.filter(_.ownerAddress === address).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class FiatTable(tag: Tag) extends Table[Fiat](tag, "Fiat_BC") {

    def * = (pegHash, ownerAddress, transactionID, transactionAmount, redeemedAmount, dirtyBit) <> (Fiat.tupled, Fiat.unapply)

    def pegHash = column[String]("pegHash", O.PrimaryKey)

    def ownerAddress = column[String]("ownerAddress", O.PrimaryKey)

    def transactionID = column[String]("transactionID")

    def transactionAmount = column[String]("transactionAmount")

    def redeemedAmount = column[String]("redeemedAmount")

    def dirtyBit = column[Boolean]("dirtyBit")
  }

  object Service {

    def create(pegHash: String, ownerAddress: String, transactionID: String, transactionAmount: String, redeemedAmount: String, dirtyBit: Boolean): Future[String] = add(Fiat(pegHash, ownerAddress, transactionID, transactionAmount, redeemedAmount, dirtyBit))

    def insertList(fiats: Seq[Fiat]): Future[Seq[String]] = insertMultiple(fiats)

    def getFiatPegWallet(address: String): Future[Seq[Fiat]] = getFiatPegWalletByAddress(address)

    def insertOrUpdate(pegHash: String, ownerAddress: String, transactionID: String, transactionAmount: String, redeemedAmount: String, dirtyBit: Boolean): Future[Int] = upsert(Fiat(pegHash = pegHash, ownerAddress, transactionID = transactionID, transactionAmount = transactionAmount, redeemedAmount = redeemedAmount, dirtyBit))

    def updateOwnerAddresses(pegHashes: Seq[String], ownerAddress: String): Future[Int] = updateOwnerAddressesByPegHashes(pegHashes, ownerAddress)

    def deleteFiat(pegHash: String, address: String): Future[Int] = deleteByPegHashAndAddress(pegHash, address)

    def deleteFiatPegWallet(address: String): Future[Int] = deleteByAddress(address)

    def getDirtyFiats: Future[Seq[Fiat]] = getFiatsByDirtyBit(dirtyBit = true)

    def markDirty(address: String): Future[Int] = updateDirtyBitByAddress(address, dirtyBit = true)
  }

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = {
      val dirtyFiats = Service.getDirtyFiats
      Thread.sleep(sleepTime)

      def insertOrUpdateAndSendCometMessage(dirtyFiats: Seq[Fiat]) = {
        Future.sequence {
          dirtyFiats.map { dirtyFiat =>
            val accountOwnerAddress = getAccount.Service.get(dirtyFiat.ownerAddress)

            def insertOrUpdate(accountOwnerAddress: Response): Future[Seq[AccountResponse.Fiat]] = {
              val fiatPegWallet = accountOwnerAddress.value.fiatPegWallet.getOrElse(throw new BaseException(constants.Response.NO_RESPONSE))
              val upsert = Future.sequence(fiatPegWallet.map { fiatPeg => if (fiatPegWallet.map(_.pegHash) contains dirtyFiat.pegHash) Service.insertOrUpdate(fiatPeg.pegHash, dirtyFiat.ownerAddress, fiatPeg.transactionID, fiatPeg.transactionAmount, fiatPeg.redeemedAmount, dirtyBit = false) else Service.deleteFiat(dirtyFiat.pegHash, dirtyFiat.ownerAddress) })
              for {
                _ <- upsert
              } yield fiatPegWallet
            }

            val accountID = masterAccounts.Service.tryGetId(dirtyFiat.ownerAddress)

            def insertOrUpdateMasterFiat(accountOwnerAddress: Response): Future[Unit] = {
              val accountID = masterAccounts.Service.getId(dirtyFiat.ownerAddress)

              def upsertMasterFiat(accountID: Option[String]): Future[Unit] = {
                accountID match {
                  case Some(traderAccountID) => {
                    val traderID = masterTraders.Service.tryGetID(traderAccountID)
                    def upsert(traderID: String) =
                    accountOwnerAddress.value.fiatPegWallet match {
                      case Some(wallet) => Future.sequence(wallet.map { fiatPeg =>
                        if (wallet.map(_.pegHash) contains dirtyFiat.pegHash) {
                          masterFiats.Service.insertOrUpdate(traderID, fiatPeg.transactionID, fiatPeg.transactionAmount.toInt, fiatPeg.redeemedAmount.toInt)
                        } else {masterFiats.Service.insertOrUpdate(traderID, dirtyFiat.transactionID, 0, dirtyFiat.redeemedAmount.toInt)}
                      })
                      case None => masterFiats.Service.updateAllTransactionAmounts(traderID)
                    }
                    for {
                      traderID <- traderID
                      _ <- upsert(traderID)
                    } yield println(accountOwnerAddress.value.fiatPegWallet)
                  }
                  case None => Future(Unit)
                }
              }
              for{
                accountID <- accountID
                _ <- upsertMasterFiat(accountID)
              } yield Unit
            }

            (for {
              accountOwnerAddress <- accountOwnerAddress
              fiatPegWallet <- insertOrUpdate(accountOwnerAddress)
              _ <- insertOrUpdateMasterFiat(accountOwnerAddress)
              accountID <- accountID
            } yield actors.Service.cometActor ! actors.Message.makeCometMessage(username = accountID, messageType = constants.Comet.FIAT, messageContent = actors.Message.Fiat(fiatPegWallet.map(_.transactionAmount.toInt).sum.toString))
              ).recover {
              case baseException: BaseException => logger.info(baseException.failure.message, baseException)
                if (baseException.failure == constants.Response.NO_RESPONSE) {
                  val deleteFiatPegWallet = Service.deleteFiatPegWallet(dirtyFiat.ownerAddress)
                  val id = masterAccounts.Service.tryGetId(dirtyFiat.ownerAddress)
                  for {
                    _ <- deleteFiatPegWallet
                    id <- id
                  } yield actors.Service.cometActor ! actors.Message.makeCometMessage(username = id, messageType = constants.Comet.FIAT, messageContent = actors.Message.Fiat("0"))
                }
            }
          }
        }
      }

      (for {
        dirtyFiats <- dirtyFiats
        _ <- insertOrUpdateAndSendCometMessage(dirtyFiats)
      } yield ()).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }(schedulerExecutionContext)
}