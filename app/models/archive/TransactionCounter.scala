package models.archive

import exceptions.BaseException
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class TransactionCounter(epoch: Long, totalTxs: Int)

@Singleton
class TransactionCounters @Inject()(
                                     protected val databaseConfigProvider: DatabaseConfigProvider,
                                     utilitiesOperations: utilities.Operations,
                                   )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ARCHIVE_TRANSACTION_COUNTER

  import databaseConfig.profile.api._

  private[models] val transactionCounterTable = TableQuery[TransactionCounterTable]

  private def add(transactionCounters: Seq[TransactionCounter]): Future[Seq[Long]] = db.run((transactionCounterTable returning transactionCounterTable.map(_.epoch) ++= transactionCounters).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.TRANSACTION_COUNTER_UPSERT_FAILED, psqlException)
    }
  }


  private[models] class TransactionCounterTable(tag: Tag) extends Table[TransactionCounter](tag, Option("archive"), "TransactionCounter") {

    def * = (epoch, totalTxs) <> (TransactionCounter.tupled, TransactionCounter.unapply)

    def epoch = column[Long]("epoch", O.PrimaryKey)

    def totalTxs = column[Int]("totalTxs")

  }

  object Service {

    def create(txs: Seq[TransactionCounter]) = add(txs)
  }

}