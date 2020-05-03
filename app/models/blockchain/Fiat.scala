package models.blockchain

import java.sql.Timestamp

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl.Source
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Node
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

case class Fiat(pegHash: String, ownerAddress: String, transactionID: String, transactionAmount: String, redeemedAmount: String, dirtyBit: Boolean, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged[Fiat] {

  def createLog()(implicit node: Node): Fiat = copy(createdBy = Option(node.id), createdOn = Option(new Timestamp(System.currentTimeMillis())), createdOnTimeZone = Option(node.timeZone))

  def updateLog()(implicit node: Node): Fiat = copy(updatedBy = Option(node.id), updatedOn = Option(new Timestamp(System.currentTimeMillis())), updatedOnTimeZone = Option(node.timeZone))

}

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

  private implicit val node: Node = Node(id = configuration.get[String]("node.id"), timeZone = configuration.get[String]("node.timeZone"))

  private def add(fiat: Fiat): Future[String] = db.run((fiatTable returning fiatTable.map(_.pegHash) += fiat.createLog()).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def insertMultiple(fiats: Seq[Fiat]): Future[Seq[String]] = db.run((fiatTable returning fiatTable.map(_.pegHash) ++= fiats.map(_.createLog())).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateByPegHash(fiat: Fiat): Future[Int] = db.run(fiatTable.filter(_.pegHash === fiat.pegHash).filter(_.ownerAddress === fiat.ownerAddress).update(fiat.updateLog()).asTry).map {
    case Success(result) => if (result > 0) result else {
      val create = add(fiat)
      for {
        _ <- create
      } yield 1
      1
    }

    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def upsert(fiat: Fiat): Future[Int] = db.run(fiatTable.insertOrUpdate(fiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByPegHashAndOwnerAddress(pegHash: String, ownerAddress: String): Future[Option[Fiat]] = db.run(fiatTable.filter(_.pegHash === pegHash).filter(_.ownerAddress === ownerAddress).result.headOption)

  private def getFiatPegWalletByAddress(address: String): Future[Seq[Fiat]] = db.run(fiatTable.filter(_.ownerAddress === address).result)

  private def getFiatPegWalletByAddresses(addresses: Seq[String]): Future[Seq[Fiat]] = db.run(fiatTable.filter(_.ownerAddress inSet addresses).result)

  private def getFiatsByDirtyBit(dirtyBit: Boolean): Future[Seq[Fiat]] = db.run(fiatTable.filter(_.dirtyBit === dirtyBit).result)

  private def updateDirtyBitByAddress(address: String, dirtyBit: Boolean): Future[Int] = db.run(fiatTable.filter(_.ownerAddress === address).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => if (result > 0) result else {
      val create = add(Fiat(pegHash = utilities.IDGenerator.hexadecimal, address, "", "", "", true))
      for {
        _ <- create
      } yield 1
      1
    }
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

    def * = (pegHash, ownerAddress, transactionID, transactionAmount, redeemedAmount, dirtyBit, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Fiat.tupled, Fiat.unapply)

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

    def create(pegHash: String, ownerAddress: String, transactionID: String, transactionAmount: String, redeemedAmount: String, dirtyBit: Boolean): Future[String] = add(Fiat(pegHash, ownerAddress, transactionID, transactionAmount, redeemedAmount, dirtyBit))

    def insertList(fiats: Seq[Fiat]): Future[Seq[String]] = insertMultiple(fiats)

    def update(fiat: Fiat): Future[Int] = updateByPegHash(fiat)

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

      def updateAndSendCometMessage(dirtyFiats: Seq[Fiat]) = {
        Future.traverse(dirtyFiats)(dirtyFiat => {
          val accountResponse = getAccount.Service.get(dirtyFiat.ownerAddress)

          val oldFiatPegWallet = Service.getFiatPegWallet(dirtyFiat.ownerAddress)

          def updateAndDelete(accountResponse: Response, oldFiatPegWallet: Seq[Fiat]): Future[String] = {
            accountResponse.value.fiatPegWallet match {
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
                } yield updatedFiatPegWallet.map(_.transactionAmount.toInt).sum.toString
              case None =>
                for {
                  _ <- Service.deleteFiatPegWallet(dirtyFiat.ownerAddress)
                } yield 0.toString
            }
          }

          val accountID = masterAccounts.Service.tryGetId(dirtyFiat.ownerAddress)

          def insertOrUpdateMasterFiat(accountOwnerAddress: Response, oldFiatPegWallet: Seq[Fiat]): Future[Unit] = {
            val accountID = masterAccounts.Service.getId(dirtyFiat.ownerAddress)

            def upsertMasterFiat(accountID: Option[String]): Future[Unit] = {
              accountID match {
                case Some(traderAccountID) => {
                  val traderID = masterTraders.Service.tryGetID(traderAccountID)

                  def upsert(traderID: String) =
                    accountOwnerAddress.value.fiatPegWallet match {
                      case Some(updatedFiatPegWallet) => {
                        val updateTransactionAmountToZero = Future.traverse(oldFiatPegWallet.map(_.pegHash).diff(updatedFiatPegWallet.map(_.pegHash)).flatMap(pegHash => oldFiatPegWallet.find(_.pegHash == pegHash)))(fiatPeg => {
                          masterFiats.Service.updateTransactionAmount(traderID, fiatPeg.transactionID, 0)
                        })
                        val updateFiats = Future.traverse(oldFiatPegWallet.map(_.pegHash).intersect(updatedFiatPegWallet.map(_.pegHash)).flatMap(pegHash => updatedFiatPegWallet.find(_.pegHash == pegHash)))(fiatPeg => {
                          masterFiats.Service.updateFiat(traderID, fiatPeg.transactionID, fiatPeg.transactionAmount.toInt, fiatPeg.redeemedAmount.toInt)
                        })
                        val insertFiats = Future.traverse(updatedFiatPegWallet.map(_.pegHash).diff(oldFiatPegWallet.map(_.pegHash)).flatMap(pegHash => updatedFiatPegWallet.find(_.pegHash == pegHash)))(fiatPeg => {
                          masterFiats.Service.insertOrUpdate(traderID, fiatPeg.transactionID, fiatPeg.transactionAmount.toInt, fiatPeg.redeemedAmount.toInt)
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
            for{
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