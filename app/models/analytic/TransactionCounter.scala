package models.analytic

import exceptions.BaseException
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.collection.immutable.ListMap
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class TransactionCounter(epoch: Long, totalTxs: Int, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class TransactionCounters @Inject()(
                                     protected val databaseConfigProvider: DatabaseConfigProvider,
                                     utilitiesOperations: utilities.Operations,
                                   )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ANALYTIC_TRANSACTION_COUNTER

  import databaseConfig.profile.api._

  private[models] val transactionCounterTable = TableQuery[TransactionCounterTable]

  private def add(transactionCounter: TransactionCounter): Future[Long] = db.run((transactionCounterTable returning transactionCounterTable.map(_.epoch) += transactionCounter).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.WALLET_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(transactionCounter: TransactionCounter): Future[Int] = db.run(transactionCounterTable.insertOrUpdate(transactionCounter).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.WALLET_UPSERT_FAILED, psqlException)
    }
  }

  private def getByEpoch(startEpoch: Long, endEpoch: Long): Future[Seq[TransactionCounter]] = db.run(transactionCounterTable.filter(x => x.epoch >= startEpoch && x.epoch <= endEpoch).sortBy(_.epoch).result)

  private def getTotalTxsByEpoch(epoch: Long): Future[Option[Int]] = db.run(transactionCounterTable.filter(_.epoch < epoch).map(_.totalTxs).sum.result)

  private[models] class TransactionCounterTable(tag: Tag) extends Table[TransactionCounter](tag, "TransactionCounter") {

    def * = (epoch, totalTxs, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (TransactionCounter.tupled, TransactionCounter.unapply)

    def epoch = column[Long]("epoch", O.PrimaryKey)

    def totalTxs = column[Int]("totalTxs")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(epoch: Long, totalTxs: Int): Future[Long] = add(TransactionCounter(epoch = epoch, totalTxs = totalTxs))

    def getByStartAndEndEpoch(startEpoch: Long, endEpoch: Long): Future[Seq[TransactionCounter]] = getByEpoch(startEpoch = startEpoch, endEpoch = endEpoch)

    def getTotalTxsTill(epoch: Long): Future[Int] = getTotalTxsByEpoch(epoch).map(_.getOrElse(0))

  }

  object Utility {

    private var statisticsData: ListMap[String, Double] = ListMap[String, Double]()

    def addStatisticsData(epoch: Long, totalTxs: Int): Future[Long] = {
      val dateString = utilities.Date.epochToMMDDString(epoch)
      statisticsData = statisticsData.map { case (date, totalTxsTill) => if (date == dateString) dateString -> (totalTxsTill + totalTxs.toDouble) else date -> totalTxsTill }
      statisticsData = ListMap(statisticsData.toSeq.sortBy(_._1): _*)
      if (statisticsData.keys.size > 10) {
        statisticsData = statisticsData.takeRight(10)
      }
      Service.create(epoch, totalTxs)
    }

    def getTransactionStatisticsData(startEpoch: Long, endEpoch: Long): Future[ListMap[String, Double]] = {
      val data = if (statisticsData.keys.isEmpty) {
        val counter = Service.getByStartAndEndEpoch(startEpoch = startEpoch, endEpoch = endEpoch).map(_.map(x => (x.epoch, x.totalTxs)))
        for {
          counter <- counter
        } yield {
          statisticsData = ListMap(counter.groupBy[String](x => utilities.Date.epochToMMDDString(x._1)).view.mapValues(x => x.map(_._2).sum.toDouble).toSeq.sortBy(_._1): _*)
          statisticsData
        }
      } else Future(statisticsData)

      (for {
        data <- data
      } yield data.map { case (key, value) =>
        val dates = key.split("/")
        Seq(dates.last, dates.head).mkString("/") -> value
      }).recover {
        case baseException: BaseException => throw baseException
      }
    }
  }

}