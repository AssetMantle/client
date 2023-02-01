package models.analytic

import exceptions.BaseException
import models.traits.Logging
import models.blockchain.Transaction
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.collection.MapView
import scala.collection.immutable.ListMap
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class MessageCounter(messageType: String, counter: Int, createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging

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

  private def upsertMultiple(messageCounters: Seq[MessageCounter]) = db.run(DBIO.sequence(messageCounters.map(messageCounter => messageCounterTable.insertOrUpdate(messageCounter))).asTry).map {
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

    def * = (messageType, counter, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (MessageCounter.tupled, MessageCounter.unapply)

    def messageType = column[String]("messageType", O.PrimaryKey)

    def counter = column[Int]("counter")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")
  }

  object Service {

    def create(messageType: String, counter: Int): Future[String] = add(MessageCounter(messageType = messageType, counter = counter))

    def createMultiple(messageCounters: Seq[MessageCounter]): Future[Seq[String]] = addMultiple(messageCounters)

    def insertOrUpdate(messageType: String, counter: Int): Future[Int] = upsert(MessageCounter(messageType = messageType, counter = counter))

    def insertOrUpdateMultiple(messageCounters: Seq[MessageCounter]): Future[Seq[Int]] = upsertMultiple(messageCounters)

    def update(messageType: String, counter: Int): Future[Int] = updateByMessageType(messageType = messageType, counter = counter)

    def getByMessageTypes(messageTypes: Seq[String]): Future[Seq[MessageCounter]] = getAllByMessageTypes(messageTypes)

    def getAll: Future[Seq[MessageCounter]] = getAllCounters.map(_.reverse)
  }

  object Utility {

    def updateMessageCounter(transactions: Seq[Transaction]): Future[Unit] = {
      val updates = transactions.filter(_.status).flatMap(_.getMessageCounters).groupBy(_._1).view.mapValues(_.map(_._2).sum)
      val oldMessageCounters = Service.getByMessageTypes(updates.keys.toSeq.distinct)

      def update(oldMessageCounters: Seq[MessageCounter], addCounters: MapView[String, Int]) = {
        Service.insertOrUpdateMultiple(addCounters.map(addCounter => MessageCounter(messageType = addCounter._1, counter = oldMessageCounters.find(_.messageType == addCounter._1).fold(0)(_.counter) + addCounter._2)).toSeq)
      }

      (for {
        oldMessageCounters <- oldMessageCounters
        _ <- update(oldMessageCounters = oldMessageCounters, addCounters = updates)
      } yield ()).recover {
        case baseException: BaseException => {
          println("here")
          val a = true
          throw baseException
        }
        case exception: Exception => {
          println("here")
          val a = true
          throw exception
        }
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