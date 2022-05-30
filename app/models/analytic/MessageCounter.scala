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

case class MessageCounter(messageType: String, counter: Int, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class MessageCounters @Inject()(
                                 protected val databaseConfigProvider: DatabaseConfigProvider,
                                 utilitiesOperations: utilities.Operations,
                               )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ANALYTIC_MESSAGE_COUNTER

  import databaseConfig.profile.api._

  private[models] val messageCounterTable = TableQuery[MessageCounterTable]

  private def add(messageCounter: MessageCounter): Future[String] = db.run((messageCounterTable returning messageCounterTable.map(_.messageType) += messageCounter).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.WALLET_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(messageCounters: Seq[MessageCounter]): Future[Seq[String]] = db.run((messageCounterTable returning messageCounterTable.map(_.messageType) ++= messageCounters).asTry).map {
    case Success(result) => messageCounters.map(_.messageType)
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.WALLET_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(messageCounter: MessageCounter): Future[Int] = db.run(messageCounterTable.insertOrUpdate(messageCounter).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.WALLET_UPSERT_FAILED, psqlException)
    }
  }

  private def updateByMessageType(messageType: String, counter: Int): Future[Int] = db.run(messageCounterTable.filter(_.messageType === messageType).map(_.counter).update(counter).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.WALLET_UPSERT_FAILED, psqlException)
    }
  }

  private def getAllByMessageTypes(messageTypes: Seq[String]): Future[Seq[MessageCounter]] = db.run(messageCounterTable.filter(_.messageType.inSet(messageTypes)).result)

  private def getAllCounters: Future[Seq[MessageCounter]] = db.run(messageCounterTable.sortBy(_.counter).result)

  private[models] class MessageCounterTable(tag: Tag) extends Table[MessageCounter](tag, "MessageCounter") {

    def * = (messageType, counter, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (MessageCounter.tupled, MessageCounter.unapply)

    def messageType = column[String]("messageType", O.PrimaryKey)

    def counter = column[Int]("counter")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(messageType: String, counter: Int): Future[String] = add(MessageCounter(messageType = messageType, counter = counter))

    def createMultiple(messageCounters: Seq[MessageCounter]): Future[Seq[String]] = addMultiple(messageCounters)

    def insertOrUpdate(messageType: String, counter: Int): Future[Int] = upsert(MessageCounter(messageType = messageType, counter = counter))

    def update(messageType: String, counter: Int): Future[Int] = updateByMessageType(messageType = messageType, counter = counter)

    def getByMessageTypes(messageTypes: Seq[String]): Future[Seq[MessageCounter]] = getAllByMessageTypes(messageTypes)

    def getAll: Future[Seq[MessageCounter]] = getAllCounters.map(_.reverse)
  }

  object Utility {

    def updateMessageCounter(updates: Map[String, Int]): Future[Unit] = {
      val messageCounters = Service.getByMessageTypes(updates.keys.toSeq)

      def update(messageCounters: Seq[MessageCounter]) = utilitiesOperations.traverse(updates.toSeq) { case (messageType, counter) =>
        Service.insertOrUpdate(messageType = messageType, counter = messageCounters.find(_.messageType == messageType).fold(0)(_.counter) + counter)
      }

      (for {
        messageCounters <- messageCounters
        _ <- update(messageCounters)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def getMessagesStatistics: Future[ListMap[String, Double]] = {
      val all = Service.getAll
      (for {
        all <- all
      } yield ListMap(all.map(x => x.messageType -> x.counter.toDouble): _*)
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

  }

}