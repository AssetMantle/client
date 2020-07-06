package models.blockchain

import java.sql.Timestamp

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.GetAccount
import queries.responses.AccountResponse.Response
import slick.jdbc.JdbcProfile
import utilities.MicroNumber

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Fiat(pegHash: String, ownerAddress: String, transactionID: String, transactionAmount: MicroNumber, redeemedAmount: MicroNumber, dirtyBit: Boolean, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Fiats @Inject()(
                       protected val databaseConfigProvider: DatabaseConfigProvider,
                       actorSystem: ActorSystem, getAccount: GetAccount,
                       configuration: Configuration,
                       blockchainAccounts: blockchain.Accounts,
                       masterTraders: master.Traders,
                       masterFiats: master.Fiats)(implicit executionContext: ExecutionContext) {

  def serialize(fiat: Fiat): FiatSerialized = FiatSerialized(pegHash = fiat.pegHash, ownerAddress = fiat.ownerAddress, transactionID = fiat.transactionID, transactionAmount = fiat.transactionAmount.toMicroString, redeemedAmount = fiat.redeemedAmount.toMicroString, dirtyBit = fiat.dirtyBit, createdBy = fiat.createdBy, createdOn = fiat.createdOn, createdOnTimeZone = fiat.createdOnTimeZone, updatedBy = fiat.updatedBy, updatedOn = fiat.updatedOn, updatedOnTimeZone = fiat.updatedOnTimeZone)

  case class FiatSerialized(pegHash: String, ownerAddress: String, transactionID: String, transactionAmount: String, redeemedAmount: String, dirtyBit: Boolean, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Fiat = Fiat(pegHash = pegHash, ownerAddress = ownerAddress, transactionID = transactionID, transactionAmount = new MicroNumber(BigInt(transactionAmount)), redeemedAmount = new MicroNumber(BigInt(redeemedAmount)), dirtyBit = dirtyBit, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

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

  private def add(fiatSerialized: FiatSerialized): Future[String] = db.run((fiatTable returning fiatTable.map(_.pegHash) += fiatSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def insertMultiple(fiatsSerialized: Seq[FiatSerialized]): Future[Seq[String]] = db.run((fiatTable returning fiatTable.map(_.pegHash) ++= fiatsSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateByPegHashAndOwnerAddress(fiatSerialized: FiatSerialized): Future[Int] = db.run(fiatTable.filter(_.pegHash === fiatSerialized.pegHash).filter(_.ownerAddress === fiatSerialized.ownerAddress).update(fiatSerialized).asTry).map {
    case Success(result) => if (result > 0) result else {
      val create = add(fiatSerialized)
      for {
        _ <- create
      } yield 1
      1
    }

    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def upsert(fiatSerialized: FiatSerialized): Future[Int] = db.run(fiatTable.insertOrUpdate(fiatSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByPegHashAndOwnerAddress(pegHash: String, ownerAddress: String): Future[Option[FiatSerialized]] = db.run(fiatTable.filter(_.pegHash === pegHash).filter(_.ownerAddress === ownerAddress).result.headOption)

  private def getFiatPegWalletByAddress(address: String): Future[Seq[FiatSerialized]] = db.run(fiatTable.filter(_.ownerAddress === address).result)

  private def getFiatPegWalletByAddresses(addresses: Seq[String]): Future[Seq[FiatSerialized]] = db.run(fiatTable.filter(_.ownerAddress inSet addresses).result)

  private def getFiatsByDirtyBit(dirtyBit: Boolean): Future[Seq[FiatSerialized]] = db.run(fiatTable.filter(_.dirtyBit === dirtyBit).result)

  private def updateDirtyBitByAddress(address: String, dirtyBit: Boolean): Future[Int] = db.run(fiatTable.filter(_.ownerAddress === address).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => if (result > 0) result else {
      val create = add(serialize(Fiat(pegHash = utilities.IDGenerator.hexadecimal, address, "", new MicroNumber(0), new MicroNumber(0), true)))
      for {
        _ <- create
      } yield 1
      1
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateOwnerAddressesByPegHashes(pegHashes: Seq[String], ownerAddress: String): Future[Int] = db.run(fiatTable.filter(_.pegHash.inSet(pegHashes)).map(_.ownerAddress).update(ownerAddress).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByPegHashAndAddress(pegHash: String, address: String): Future[Int] = db.run(fiatTable.filter(_.pegHash === pegHash).filter(_.ownerAddress === address).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByAddress(address: String): Future[Int] = db.run(fiatTable.filter(_.ownerAddress === address).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class FiatTable(tag: Tag) extends Table[FiatSerialized](tag, "Fiat_BC") {

    def * = (pegHash, ownerAddress, transactionID, transactionAmount, redeemedAmount, dirtyBit, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (FiatSerialized.tupled, FiatSerialized.unapply)

    def pegHash = column[String]("pegHash", O.PrimaryKey)

    def ownerAddress = column[String]("ownerAddress", O.PrimaryKey)

    def transactionID = column[String]("transactionID")

    def transactionAmount = column[String]("transactionAmount")

    def redeemedAmount = column[String]("redeemedAmount")

    def dirtyBit = column[Boolean]("dirtyBit")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(pegHash: String, ownerAddress: String, transactionID: String, transactionAmount: MicroNumber, redeemedAmount: MicroNumber, dirtyBit: Boolean): Future[String] = add(serialize(Fiat(pegHash, ownerAddress, transactionID, transactionAmount, redeemedAmount, dirtyBit)))

    def insertList(fiats: Seq[Fiat]): Future[Seq[String]] = insertMultiple(fiats.map(serialize(_)))

    def update(fiat: Fiat): Future[Int] = updateByPegHashAndOwnerAddress(serialize(fiat))

    def getFiatPegWallet(address: String): Future[Seq[Fiat]] = getFiatPegWalletByAddress(address).map(_.map(_.deserialize))

    def insertOrUpdate(pegHash: String, ownerAddress: String, transactionID: String, transactionAmount: MicroNumber, redeemedAmount: MicroNumber, dirtyBit: Boolean): Future[Int] = upsert(serialize(Fiat(pegHash = pegHash, ownerAddress, transactionID = transactionID, transactionAmount = transactionAmount, redeemedAmount = redeemedAmount, dirtyBit)))

    def updateOwnerAddresses(pegHashes: Seq[String], ownerAddress: String): Future[Int] = updateOwnerAddressesByPegHashes(pegHashes, ownerAddress)

    def deleteFiat(pegHash: String, address: String): Future[Int] = deleteByPegHashAndAddress(pegHash, address)

    def deleteFiatPegWallet(address: String): Future[Int] = deleteByAddress(address)

    def getDirtyFiats: Future[Seq[Fiat]] = getFiatsByDirtyBit(dirtyBit = true).map(_.map(_.deserialize))

    def markDirty(address: String): Future[Int] = updateDirtyBitByAddress(address, dirtyBit = true)

  }

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = {
      val dirtyFiats = Service.getDirtyFiats
      Thread.sleep(sleepTime)

      def updateAndSendCometMessage(dirtyFiats: Seq[Fiat]) = {
        Future.traverse(dirtyFiats)(dirtyFiat => {
          val accountResponse = getAccount.Service.get(dirtyFiat.ownerAddress)

          val oldFiatPegWallet = Service.getFiatPegWallet(dirtyFiat.ownerAddress)

          def updateAndDelete(accountResponse: Response, oldFiatPegWallet: Seq[Fiat]): Future[String] = {
            accountResponse.value.fiat_peg_wallet match {
              case Some(updatedFiatPegWallet) =>
                val deleteFiats = Future.traverse(oldFiatPegWallet.map(_.pegHash).diff(updatedFiatPegWallet.map(_.pegHash)))(pegHash => {
                  Service.deleteFiat(pegHash = pegHash, address = dirtyFiat.ownerAddress)
                })
                val updateFiats = Future.traverse(oldFiatPegWallet.map(_.pegHash).intersect(updatedFiatPegWallet.map(_.pegHash)).flatMap(pegHash => updatedFiatPegWallet.find(_.pegHash == pegHash)))(fiatPeg => {
                  Service.update(Fiat(pegHash = fiatPeg.pegHash, ownerAddress = dirtyFiat.ownerAddress, transactionID = fiatPeg.transactionID, transactionAmount = fiatPeg.transactionAmount, redeemedAmount = fiatPeg.redeemedAmount, dirtyBit = false))
                })
                val insertFiats = Future.traverse(updatedFiatPegWallet.map(_.pegHash).diff(oldFiatPegWallet.map(_.pegHash)).flatMap(pegHash => updatedFiatPegWallet.find(_.pegHash == pegHash)))(fiatPeg => {
                  Service.create(pegHash = fiatPeg.pegHash, ownerAddress = dirtyFiat.ownerAddress, transactionID = fiatPeg.transactionID, transactionAmount = fiatPeg.transactionAmount, redeemedAmount = fiatPeg.redeemedAmount, dirtyBit = false)
                })
                for {
                  _ <- deleteFiats
                  _ <- updateFiats
                  _ <- insertFiats
                } yield updatedFiatPegWallet.map(_.transactionAmount).sum.toRoundedUpString()
              case None =>
                for {
                  _ <- Service.deleteFiatPegWallet(dirtyFiat.ownerAddress)
                } yield 0.toString
            }
          }

          val accountID = blockchainAccounts.Service.tryGetUsername(dirtyFiat.ownerAddress)

          def insertOrUpdateMasterFiat(accountOwnerAddress: Response, oldFiatPegWallet: Seq[Fiat]): Future[Unit] = {
            val accountID = blockchainAccounts.Service.getUsername(dirtyFiat.ownerAddress)

            def upsertMasterFiat(accountID: Option[String]): Future[Unit] = {
              accountID match {
                case Some(traderAccountID) => {
                  val traderID = masterTraders.Service.tryGetID(traderAccountID)

                  def upsert(traderID: String) =
                    accountOwnerAddress.value.fiat_peg_wallet match {
                      case Some(updatedFiatPegWallet) => {
                        val updateTransactionAmountToZero = Future.traverse(oldFiatPegWallet.map(_.pegHash).diff(updatedFiatPegWallet.map(_.pegHash)).flatMap(pegHash => oldFiatPegWallet.find(_.pegHash == pegHash)))(fiatPeg => {
                          masterFiats.Service.updateTransactionAmount(traderID, fiatPeg.transactionID, new MicroNumber(0))
                        })
                        val updateFiats = Future.traverse(oldFiatPegWallet.map(_.pegHash).intersect(updatedFiatPegWallet.map(_.pegHash)).flatMap(pegHash => updatedFiatPegWallet.find(_.pegHash == pegHash)))(fiatPeg => {
                          masterFiats.Service.updateFiat(traderID, fiatPeg.transactionID, fiatPeg.transactionAmount, fiatPeg.redeemedAmount)
                        })
                        val insertFiats = Future.traverse(updatedFiatPegWallet.map(_.pegHash).diff(oldFiatPegWallet.map(_.pegHash)).flatMap(pegHash => updatedFiatPegWallet.find(_.pegHash == pegHash)))(fiatPeg => {
                          masterFiats.Service.insertOrUpdate(traderID, fiatPeg.transactionID, fiatPeg.transactionAmount, fiatPeg.redeemedAmount)
                        })
                        for {
                          _ <- updateTransactionAmountToZero
                          _ <- updateFiats
                          _ <- insertFiats
                        } yield ()
                      }
                      case None => {
                        val updateTransactionAmountToZero = masterFiats.Service.updateAllTransactionAmountsToZero(traderID)
                        for {
                          _ <- updateTransactionAmountToZero
                        } yield ()
                      }
                    }

                  for {
                    traderID <- traderID
                    _ <- upsert(traderID)
                  } yield Unit
                }
                case None => Future(Unit)
              }
            }

            for {
              accountID <- accountID
              _ <- upsertMasterFiat(accountID)
            } yield Unit
          }

          for {
            accountResponse <- accountResponse
            oldFiatPegWallet <- oldFiatPegWallet
            _ <- insertOrUpdateMasterFiat(accountResponse, oldFiatPegWallet)
            totalAmount <- updateAndDelete(accountResponse = accountResponse, oldFiatPegWallet = oldFiatPegWallet)
            accountID <- accountID
          } yield actors.Service.cometActor ! actors.Message.makeCometMessage(username = accountID, messageType = constants.Comet.FIAT, messageContent = actors.Message.Fiat(totalAmount))
        })
      }

      (for {
        dirtyFiats <- dirtyFiats
        _ <- updateAndSendCometMessage(dirtyFiats)
      } yield ()).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }(schedulerExecutionContext)
}