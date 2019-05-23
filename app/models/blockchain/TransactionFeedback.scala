package models.blockchain

import akka.actor.ActorSystem
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.GetTraderReputation
import queries.responses.TraderReputationResponse.TransactionFeedbackResponse
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class TransactionFeedback(address: String, sendAssetCounts: SendAssetCounts, sendFiatCounts: SendFiatCounts, ibcIssueAssetCounts: IBCIssueAssetCounts, ibcIssueFiatCounts: IBCIssueFiatCounts, buyerExecuteOrderCounts: BuyerExecuteOrderCounts, sellerExecuteOrderCounts: SellerExecuteOrderCounts, changeBuyerBidCounts: ChangeBuyerBidCounts, changeSellerBidCounts: ChangeSellerBidCounts, confirmBuyerBidCounts: ConfirmBuyerBidCounts, confirmSellerBidCounts: ConfirmSellerBidCounts, negotiationCounts: NegotiationCounts, dirtyBit: Boolean)

case class SendAssetCounts(sendAssetsPositiveTx: String, sendAssetsNegativeTx: String)

case class SendFiatCounts(sendFiatsPositiveTx: String, sendFiatsNegativeTx: String)

case class IBCIssueAssetCounts(ibcIssueAssetsPositiveTx: String, ibcIssueAssetsNegativeTx: String)

case class IBCIssueFiatCounts(ibcIssueFiatsPositiveTx: String, ibcIssueFiatsNegativeTx: String)

case class BuyerExecuteOrderCounts(buyerExecuteOrderPositiveTx: String, buyerExecuteOrderNegativeTx: String)

case class SellerExecuteOrderCounts(sellerExecuteOrderPositiveTx: String, sellerExecuteOrderNegativeTx: String)

case class ChangeBuyerBidCounts(changeBuyerBidPositiveTx: String, changeBuyerBidNegativeTx: String)

case class ChangeSellerBidCounts(changeSellerBidPositiveTx: String, changeSellerBidNegativeTx: String)

case class ConfirmBuyerBidCounts(confirmBuyerBidPositiveTx: String, confirmBuyerBidNegativeTx: String)

case class ConfirmSellerBidCounts(confirmSellerBidPositiveTx: String, confirmSellerBidNegativeTx: String)

case class NegotiationCounts(negotiationPositiveTx: String, negotiationNegativeTx: String)

@Singleton
class TransactionFeedbacks @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, actorSystem: ActorSystem, getTraderReputation: GetTraderReputation)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_FEEDBACK

  import databaseConfig.profile.api._

  private[models] val transactionFeedbackTable = TableQuery[TransactionFeedbackTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val sleepTime = configuration.get[Long]("blockchain.kafka.entityIterator.threadSleep")

  private def add(transactionFeedback: TransactionFeedback)(implicit executionContext: ExecutionContext): Future[String] = db.run((transactionFeedbackTable returning transactionFeedbackTable.map(_.address) += transactionFeedback).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(address: String)(implicit executionContext: ExecutionContext): Future[TransactionFeedback] = db.run(transactionFeedbackTable.filter(_.address === address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTransactionFeedbackByAddress(address: String, transactionFeedback: TransactionFeedback): Future[Int] = db.run(transactionFeedbackTable.filter(_.address === address).update(transactionFeedback).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateDirtyBitByAddress(address: String, dirtyBit: Boolean): Future[Int] = db.run(transactionFeedbackTable.filter(_.address === address).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTransactionFeedbacksByDirtyBit(dirtyBit: Boolean): Future[Seq[String]] = db.run(transactionFeedbackTable.filter(_.dirtyBit === dirtyBit).map(_.address).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        Nil
    }
  }

  private def deleteById(address: String)(implicit executionContext: ExecutionContext) = db.run(transactionFeedbackTable.filter(_.address === address).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class TransactionFeedbackTable(tag: Tag) extends Table[TransactionFeedback](tag, "TransactionFeedBack_BC") {

    def * = (address, (sendAssetsPositiveTx, sendAssetsNegativeTx), (sendFiatsPositiveTx, sendFiatsNegativeTx), (ibcIssueAssetsPositiveTx, ibcIssueAssetsNegativeTx), (ibcIssueFiatsPositiveTx, ibcIssueFiatsNegativeTx), (buyerExecuteOrderPositiveTx, buyerExecuteOrderNegativeTx), (sellerExecuteOrderPositiveTx, sellerExecuteOrderNegativeTx), (changeBuyerBidPositiveTx, changeBuyerBidNegativeTx), (changeSellerBidPositiveTx, changeSellerBidNegativeTx), (confirmBuyerBidPositiveTx, confirmBuyerBidNegativeTx), (confirmSellerBidPositiveTx, confirmSellerBidNegativeTx), (negotiationPositiveTx, negotiationNegativeTx), dirtyBit).shaped <> ( {
      case (address, sendAssetCounts, sendFiatCounts, ibcIssueAssetCounts, ibcIssueFiatCounts, buyerExecuteOrderCounts, sellerExecuteOrderCounts, changeBuyerBidCounts, changeSellerBidCounts, confirmBuyerBidCounts, confirmSellerBidCounts, negotiationCounts, dirtyBit) => TransactionFeedback(address, SendAssetCounts.tupled.apply(sendAssetCounts), SendFiatCounts.tupled.apply(sendFiatCounts), IBCIssueAssetCounts.tupled.apply(ibcIssueAssetCounts), IBCIssueFiatCounts.tupled.apply(ibcIssueFiatCounts), BuyerExecuteOrderCounts.tupled.apply(buyerExecuteOrderCounts), SellerExecuteOrderCounts.tupled.apply(sellerExecuteOrderCounts), ChangeBuyerBidCounts.tupled.apply(changeBuyerBidCounts), ChangeSellerBidCounts.tupled.apply(changeSellerBidCounts), ConfirmBuyerBidCounts.tupled.apply(confirmBuyerBidCounts), ConfirmSellerBidCounts.tupled.apply(confirmSellerBidCounts), NegotiationCounts.tupled.apply(negotiationCounts), dirtyBit)
    }, { transactionFeedback: TransactionFeedback =>
      def f1(sendAssetCounts: SendAssetCounts) = SendAssetCounts.unapply(sendAssetCounts).get

      def f2(sendFiatCounts: SendFiatCounts) = SendFiatCounts.unapply(sendFiatCounts).get

      def f3(ibcIssueAssetCounts: IBCIssueAssetCounts) = IBCIssueAssetCounts.unapply(ibcIssueAssetCounts).get

      def f4(ibcIssueFiatCounts: IBCIssueFiatCounts) = IBCIssueFiatCounts.unapply(ibcIssueFiatCounts).get

      def f5(buyerExecuteOrderCounts: BuyerExecuteOrderCounts) = BuyerExecuteOrderCounts.unapply(buyerExecuteOrderCounts).get

      def f6(sellerExecuteOrderCounts: SellerExecuteOrderCounts) = SellerExecuteOrderCounts.unapply(sellerExecuteOrderCounts).get

      def f7(changeBuyerBidCounts: ChangeBuyerBidCounts) = ChangeBuyerBidCounts.unapply(changeBuyerBidCounts).get

      def f8(changeSellerBidCounts: ChangeSellerBidCounts) = ChangeSellerBidCounts.unapply(changeSellerBidCounts).get

      def f9(confirmBuyerBidCounts: ConfirmBuyerBidCounts) = ConfirmBuyerBidCounts.unapply(confirmBuyerBidCounts).get

      def f10(confirmSellerBidCounts: ConfirmSellerBidCounts) = ConfirmSellerBidCounts.unapply(confirmSellerBidCounts).get

      def f11(negotiationCounts: NegotiationCounts) = NegotiationCounts.unapply(negotiationCounts).get

      Some((transactionFeedback.address, f1(transactionFeedback.sendAssetCounts), f2(transactionFeedback.sendFiatCounts), f3(transactionFeedback.ibcIssueAssetCounts), f4(transactionFeedback.ibcIssueFiatCounts), f5(transactionFeedback.buyerExecuteOrderCounts), f6(transactionFeedback.sellerExecuteOrderCounts), f7(transactionFeedback.changeBuyerBidCounts), f8(transactionFeedback.changeSellerBidCounts), f9(transactionFeedback.confirmBuyerBidCounts), f10(transactionFeedback.confirmSellerBidCounts), f11(transactionFeedback.negotiationCounts), transactionFeedback.dirtyBit))
    })

    def address = column[String]("address", O.PrimaryKey)

    def sendAssetsPositiveTx = column[String]("sendAssetsPositiveTx")

    def sendAssetsNegativeTx = column[String]("sendAssetsNegativeTx")

    def sendFiatsPositiveTx = column[String]("sendFiatsPositiveTx")

    def sendFiatsNegativeTx = column[String]("sendFiatsNegativeTx")

    def ibcIssueAssetsPositiveTx = column[String]("ibcIssueAssetsPositiveTx")

    def ibcIssueAssetsNegativeTx = column[String]("ibcIssueAssetsNegativeTx")

    def ibcIssueFiatsPositiveTx = column[String]("ibcIssueFiatsPositiveTx")

    def ibcIssueFiatsNegativeTx = column[String]("ibcIssueFiatsNegativeTx")

    def buyerExecuteOrderPositiveTx = column[String]("buyerExecuteOrderPositiveTx")

    def buyerExecuteOrderNegativeTx = column[String]("buyerExecuteOrderNegativeTx")

    def sellerExecuteOrderPositiveTx = column[String]("sellerExecuteOrderPositiveTx")

    def sellerExecuteOrderNegativeTx = column[String]("sellerExecuteOrderNegativeTx")

    def changeBuyerBidPositiveTx = column[String]("changeBuyerBidPositiveTx")

    def changeBuyerBidNegativeTx = column[String]("changeBuyerBidNegativeTx")

    def changeSellerBidPositiveTx = column[String]("changeSellerBidPositiveTx")

    def changeSellerBidNegativeTx = column[String]("changeSellerBidNegativeTx")

    def confirmBuyerBidPositiveTx = column[String]("confirmBuyerBidPositiveTx")

    def confirmBuyerBidNegativeTx = column[String]("confirmBuyerBidNegativeTx")

    def confirmSellerBidPositiveTx = column[String]("confirmSellerBidPositiveTx")

    def confirmSellerBidNegativeTx = column[String]("confirmSellerBidNegativeTx")

    def negotiationPositiveTx = column[String]("negotiationPositiveTx")

    def negotiationNegativeTx = column[String]("negotiationNegativeTx")

    def dirtyBit = column[Boolean]("dirtyBit")

  }

  object Service {

    def addTransactionFeedback(address: String, transactionFeedbackResponse: TransactionFeedbackResponse, dirtyBit: Boolean)(implicit executionContext: ExecutionContext): String = Await.result(add(TransactionFeedback(address = address, SendAssetCounts(transactionFeedbackResponse.sendAssetsPositiveTx, transactionFeedbackResponse.sendAssetsNegativeTx), SendFiatCounts(transactionFeedbackResponse.sendFiatsPositiveTx, transactionFeedbackResponse.sendFiatsNegativeTx), IBCIssueAssetCounts(transactionFeedbackResponse.ibcIssueAssetsPositiveTx, transactionFeedbackResponse.ibcIssueAssetsNegativeTx), IBCIssueFiatCounts(transactionFeedbackResponse.ibcIssueFiatsPositiveTx, transactionFeedbackResponse.ibcIssueFiatsNegativeTx), BuyerExecuteOrderCounts(transactionFeedbackResponse.buyerExecuteOrderPositiveTx, transactionFeedbackResponse.buyerExecuteOrderNegativeTx), SellerExecuteOrderCounts(transactionFeedbackResponse.sellerExecuteOrderPositiveTx, transactionFeedbackResponse.sellerExecuteOrderNegativeTx), ChangeBuyerBidCounts(transactionFeedbackResponse.changeBuyerBidPositiveTx, transactionFeedbackResponse.changeBuyerBidNegativeTx), ChangeSellerBidCounts(transactionFeedbackResponse.changeSellerBidPositiveTx, transactionFeedbackResponse.changeSellerBidNegativeTx), ConfirmBuyerBidCounts(transactionFeedbackResponse.confirmBuyerBidPositiveTx, transactionFeedbackResponse.confirmBuyerBidNegativeTx), ConfirmSellerBidCounts(transactionFeedbackResponse.confirmSellerBidPositiveTx, transactionFeedbackResponse.confirmSellerBidNegativeTx), NegotiationCounts(transactionFeedbackResponse.negotiationPositiveTx, transactionFeedbackResponse.negotiationNegativeTx), dirtyBit = dirtyBit)), Duration.Inf)

    def getAddress(address: String)(implicit executionContext: ExecutionContext): TransactionFeedback = Await.result(findById(address), Duration.Inf)

    def getDirtyAddresses(dirtyBit: Boolean): Seq[String] = Await.result(getTransactionFeedbacksByDirtyBit(dirtyBit), Duration.Inf)

    def updateTransactionFeedback(address: String, transactionFeedbackResponse: TransactionFeedbackResponse, dirtyBit: Boolean): Int = Await.result(updateTransactionFeedbackByAddress(address, TransactionFeedback(address = address, SendAssetCounts(transactionFeedbackResponse.sendAssetsPositiveTx, transactionFeedbackResponse.sendAssetsNegativeTx), SendFiatCounts(transactionFeedbackResponse.sendFiatsPositiveTx, transactionFeedbackResponse.sendFiatsNegativeTx), IBCIssueAssetCounts(transactionFeedbackResponse.ibcIssueAssetsPositiveTx, transactionFeedbackResponse.ibcIssueAssetsNegativeTx), IBCIssueFiatCounts(transactionFeedbackResponse.ibcIssueFiatsPositiveTx, transactionFeedbackResponse.ibcIssueFiatsNegativeTx), BuyerExecuteOrderCounts(transactionFeedbackResponse.buyerExecuteOrderPositiveTx, transactionFeedbackResponse.buyerExecuteOrderNegativeTx), SellerExecuteOrderCounts(transactionFeedbackResponse.sellerExecuteOrderPositiveTx, transactionFeedbackResponse.sellerExecuteOrderNegativeTx), ChangeBuyerBidCounts(transactionFeedbackResponse.changeBuyerBidPositiveTx, transactionFeedbackResponse.changeBuyerBidNegativeTx), ChangeSellerBidCounts(transactionFeedbackResponse.changeSellerBidPositiveTx, transactionFeedbackResponse.changeSellerBidNegativeTx), ConfirmBuyerBidCounts(transactionFeedbackResponse.confirmBuyerBidPositiveTx, transactionFeedbackResponse.confirmBuyerBidNegativeTx), ConfirmSellerBidCounts(transactionFeedbackResponse.confirmSellerBidPositiveTx, transactionFeedbackResponse.confirmSellerBidNegativeTx), NegotiationCounts(transactionFeedbackResponse.negotiationPositiveTx, transactionFeedbackResponse.negotiationNegativeTx), dirtyBit = dirtyBit)), Duration.Inf)

    def updateDirtyBit(address: String, dirtyBit: Boolean): Int = Await.result(updateDirtyBitByAddress(address, dirtyBit), Duration.Inf)
  }

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = Future {
      val dirtyAddresses = Service.getDirtyAddresses(dirtyBit = true)
      Thread.sleep(sleepTime)
      for (dirtyAddress <- dirtyAddresses) {
        try {
          val response = getTraderReputation.Service.get(dirtyAddress)
          Service.updateTransactionFeedback(response.value.address, response.value.transactionFeedbackResponse, dirtyBit = false)
        }
        catch {
          case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
          case baseException: BaseException => logger.error(constants.Response.BASE_EXCEPTION.message, baseException)
        }
      }
    }
  }


  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }
}