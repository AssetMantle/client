package models.blockchain

import java.sql.Timestamp
import java.time.Duration

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.responses.common.{Header => BlockHeader}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class AverageBlockTime(id: String, height: Int, value: Long, time: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class AverageBlockTimes @Inject()(
                                   protected val databaseConfigProvider: DatabaseConfigProvider,
                                   configuration: Configuration
                                 )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_AVERAGE_BLOCK_TIME

  private val AVERAGE_BLOCK_TIME: String = "AVERAGE_BLOCK_TIME"

  import databaseConfig.profile.api._

  private[models] val averageBlockTimeTable = TableQuery[AverageBlockTimeTable]

  private def upsert(averageBlockTime: AverageBlockTime): Future[Int] = db.run(averageBlockTimeTable.insertOrUpdate(averageBlockTime).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def tryGet: Future[AverageBlockTime] = db.run(averageBlockTimeTable.filter(_.id === AVERAGE_BLOCK_TIME).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.BLOCK_NOT_FOUND, noSuchElementException)
    }
  }

  private def tryGetValue: Future[Long] = db.run(averageBlockTimeTable.filter(_.id === AVERAGE_BLOCK_TIME).map(_.value).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.BLOCK_NOT_FOUND, noSuchElementException)
    }
  }

  private[models] class AverageBlockTimeTable(tag: Tag) extends Table[AverageBlockTime](tag, "AverageBlockTime") {

    def * = (id, height, value, time, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (AverageBlockTime.tupled, AverageBlockTime.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def height = column[Int]("height")

    def value = column[Long]("value")

    def time = column[String]("time")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def get: Future[Double] = tryGetValue.map(x => x.toDouble / 1000)

    def set(blockHeader: BlockHeader): Future[Double] = {
      if (blockHeader.height == 1) {
        for {
          _ <- upsert(AverageBlockTime(AVERAGE_BLOCK_TIME, 1, 0, blockHeader.time))
        } yield 0.0
      } else {

        def calculateAvgTime(previousAvgBlockTime: AverageBlockTime): Long = {
          val difference = Duration.between(utilities.Date.bcTimestampToZonedDateTime(previousAvgBlockTime.time), utilities.Date.bcTimestampToZonedDateTime(blockHeader.time)).toMillis
          ((previousAvgBlockTime.value * (previousAvgBlockTime.height - 1)) + difference) / previousAvgBlockTime.height
        }

        val average = {
          for {
            previousAvgBlockTime <- tryGet
          } yield calculateAvgTime(previousAvgBlockTime)
        }

        def update(average: Long): Future[Int] = upsert(AverageBlockTime(AVERAGE_BLOCK_TIME, blockHeader.height, average, blockHeader.time))

        (for {
          average <- average
          _ <- update(average)
        } yield average.toDouble / 1000
          ).recover {
          case baseException: BaseException => throw baseException
        }
      }
    }
  }

}