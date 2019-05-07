package models.blockchain

import akka.actor.ActorSystem
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.master
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import queries.GetFeedback
import queries.responses.FeedbackResponse
import slick.jdbc.JdbcProfile
import utilities.PushNotifications

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

//case class Feedback(address: String, buyerExecuteOrderNegativeTx: Int, buyerExecuteOrderPositiveTx: Int, changeBuyerBidNegativeTx: Int, changeBuyerBidPositiveTx: Int, changeSellerBidNegativeTx: Int, changeSellerBidPositiveTx: Int, confirmBuyerBidNegativeTx: Int, confirmBuyerBidPositiveTx: Int, confirmSellerBidNegativeTx: Int, confirmSellerBidPositiveTx: Int, ibcIssueAssetsNegativeTx: Int, ibcIssueAssetsPositiveTx: Int, ibcIssueFiatsNegativeTx: Int, ibcIssueFiatsPositiveTx: Int, negotiationNegativeTx: Int, negotiationPositiveTx: Int, sellerExecuteOrderNegativeTx: Int, sellerExecuteOrderPositiveTx: Int, sendAssetsNegativeTx: Int, sendAssetsPositiveTx: Int, sendFiatsNegativeTx: Int, sendFiatsPositiveTx: Int, rating: Int, dirtyBit: Boolean)
case class Feedback(address: String, rating: Int, dirtyBit: Boolean)

@Singleton
class Feedbacks @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, getFeedback: GetFeedback, actorSystem: ActorSystem, implicit val pushNotifications: PushNotifications)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_FEEDBACK

  import databaseConfig.profile.api._

  private[models] val feedbackTable = TableQuery[FeedbackTable]

  private def add(feedback: Feedback)(implicit executionContext: ExecutionContext): Future[String] = db.run((feedbackTable returning feedbackTable.map(_.address) += feedback).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def insertOrUpdate(feedback: Feedback)(implicit executionContext: ExecutionContext): Future[Int] = db.run(feedbackTable.insertOrUpdate(feedback).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def findByAddress(address: String)(implicit executionContext: ExecutionContext): Future[Feedback] = db.run(feedbackTable.filter(_.address === address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getRatingByAddress(address: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(feedbackTable.filter(_.address === address).map(_.rating).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getFeedbacksByDirtyBit(dirtyBit: Boolean)(implicit executionContext: ExecutionContext): Future[Seq[Feedback]] = db.run(feedbackTable.filter(_.dirtyBit === dirtyBit).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        Nil
    }
  }

  private def updateDirtyBitByAddress(address: String, dirtyBit: Boolean)(implicit executionContext: ExecutionContext): Future[Int] = db.run(feedbackTable.filter(_.address === address).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def delete(address: String)(implicit executionContext: ExecutionContext) = db.run(feedbackTable.filter(_.address === address).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class FeedbackTable(tag: Tag) extends Table[Feedback](tag, "Feedback_BC") {

//    def * = (address, buyerExecuteOrderNegativeTx, buyerExecuteOrderPositiveTx, changeBuyerBidNegativeTx, changeBuyerBidPositiveTx, changeSellerBidNegativeTx, changeSellerBidPositiveTx, confirmBuyerBidNegativeTx, confirmBuyerBidPositiveTx, confirmSellerBidNegativeTx, confirmSellerBidPositiveTx, ibcIssueAssetsNegativeTx, ibcIssueAssetsPositiveTx, ibcIssueFiatsNegativeTx, ibcIssueFiatsPositiveTx, negotiationNegativeTx, negotiationPositiveTx, sellerExecuteOrderNegativeTx, sellerExecuteOrderPositiveTx, sendAssetsNegativeTx, sendAssetsPositiveTx, sendFiatsNegativeTx, sendFiatsPositiveTx, rating, dirtyBit) <> (Feedback.tupled, Feedback.unapply)

    def * = (address, rating, dirtyBit) <> (Feedback.tupled, Feedback.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def rating = column[Int]("rating")

    def dirtyBit = column[Boolean]("dirtyBit")

//    def buyerExecuteOrderNegativeTx = column[Int]("buyerExecuteOrderNegativeTx")
//
//    def buyerExecuteOrderPositiveTx = column[Int]("buyerExecuteOrderPositiveTx")
//
//    def changeBuyerBidNegativeTx = column[Int]("changeBuyerBidNegativeTx")
//
//    def changeBuyerBidPositiveTx = column[Int]("changeBuyerBidPositiveTx")
//
//    def changeSellerBidNegativeTx = column[Int]("changeSellerBidNegativeTx")
//
//    def changeSellerBidPositiveTx = column[Int]("changeSellerBidPositiveTx")
//
//    def confirmBuyerBidNegativeTx = column[Int]("confirmBuyerBidNegativeTx")
//
//    def confirmBuyerBidPositiveTx = column[Int]("confirmBuyerBidPositiveTx")
//
//    def confirmSellerBidNegativeTx = column[Int]("confirmSellerBidNegativeTx")
//
//    def confirmSellerBidPositiveTx = column[Int]("confirmSellerBidPositiveTx")
//
//    def ibcIssueAssetsNegativeTx = column[Int]("ibcIssueAssetsNegativeTx")
//
//    def ibcIssueAssetsPositiveTx = column[Int]("ibcIssueAssetsPositiveTx")
//
//    def ibcIssueFiatsNegativeTx = column[Int]("ibcIssueFiatsNegativeTx")
//
//    def ibcIssueFiatsPositiveTx = column[Int]("ibcIssueFiatsPositiveTx")
//
//    def negotiationNegativeTx = column[Int]("negotiationNegativeTx")
//
//    def negotiationPositiveTx = column[Int]("negotiationPositiveTx")
//
//    def sellerExecuteOrderNegativeTx = column[Int]("sellerExecuteOrderNegativeTx")
//
//    def sellerExecuteOrderPositiveTx = column[Int]("sellerExecuteOrderPositiveTx")
//
//    def sendAssetsNegativeTx = column[Int]("sendAssetsNegativeTx")
//
//    def sendAssetsPositiveTx = column[Int]("sendAssetsPositiveTx")
//
//    def sendFiatsNegativeTx = column[Int]("sendFiatsNegativeTx")
//
//    def sendFiatsPositiveTx = column[Int]("sendFiatsPositiveTx")

  }

  object Service {

    def addFeedback(address: String, rating: Int, dirtyBit: Boolean): String = Await.result(add(Feedback(address, rating, dirtyBit)), Duration.Inf)

    def update(address: String, rating: Int, dirtyBit: Boolean): Int = Await.result(insertOrUpdate(Feedback(address, rating, dirtyBit)), Duration.Inf)

    def getFeedback(address: String): Feedback = Await.result(findByAddress(address), Duration.Inf)

    def getRating(address: String): Int = Await.result(getRatingByAddress(address), Duration.Inf)

    def getDirtyFeedbacks(dirtyBit: Boolean): Seq[Feedback] = Await.result(getFeedbacksByDirtyBit(dirtyBit), Duration.Inf)

    def updateDirtyBit(address: String, dirtyBit: Boolean): Int = Await.result(updateDirtyBitByAddress(address, dirtyBit), Duration.Inf)

  }

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val sleepTime = configuration.get[Long]("blockchain.kafka.entityIterator.threadSleep")

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = Future {
      val dirtyFeedbacks = Service.getDirtyFeedbacks(true)
      Thread.sleep(sleepTime)
      for (dirtyFeedback <- dirtyFeedbacks) {
        try {
          val feedback = getFeedback.Service.get(dirtyFeedback.address).applyToBlockchainFeedback()
          Service.update(feedback.address, feedback.rating, feedback.dirtyBit)
        }
        catch {
          case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
          case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
        }
      }
    }
  }

  //Scheduler- iterates feedbacks with dirty tags
  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }


}
