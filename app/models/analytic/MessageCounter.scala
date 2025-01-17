package models.analytic

import models.blockchain.Transaction
import models.traits._
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.H2Profile.api._

import javax.inject.{Inject, Singleton}
import scala.collection.MapView
import scala.collection.immutable.ListMap
import scala.concurrent.{ExecutionContext, Future}

case class MessageCounter(messageType: String, counter: Int, createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity[String] {
  def id: String = messageType
}

private[analytic] object MessageCounters {

  class MessageCounterTable(tag: Tag) extends Table[MessageCounter](tag, "MessageCounter") with ModelTable[String] {

    def * = (messageType, counter, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (MessageCounter.tupled, MessageCounter.unapply)

    def messageType = column[String]("messageType", O.PrimaryKey)

    def counter = column[Int]("counter")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")

    def id = messageType
  }

}

@Singleton
class MessageCounters @Inject()(
                                 protected val dbConfigProvider: DatabaseConfigProvider,
                                 utilitiesOperations: utilities.Operations,
                               )(implicit executionContext: ExecutionContext)
  extends GenericDaoImpl[MessageCounters.MessageCounterTable, MessageCounter, String]() {

  implicit val logger: Logger = Logger(this.getClass)

  implicit val module: String = constants.Module.ANALYTIC_MESSAGE_COUNTER

  val tableQuery = new TableQuery(tag => new MessageCounters.MessageCounterTable(tag))

  object Service {

    def insertOrUpdateMultiple(messageCounters: Seq[MessageCounter]): Future[Int] = upsertMultiple(messageCounters)

    def update(messageType: String, counter: Int): Future[Int] = customUpdate(tableQuery.filter(_.messageType === messageType).map(_.counter).update(counter))

    def getByMessageTypes(messageTypes: Seq[String]): Future[Seq[MessageCounter]] = filter(_.messageType.inSet(messageTypes))

    def fetchAll: Future[Seq[MessageCounter]] = getAll.map(_.sortBy(_.counter))
  }

  object Utility {

    def updateMessageCounter(transactions: Seq[Transaction]): Future[Unit] = {
      val updates = transactions.filter(_.status).flatMap(_.getMessageCounters).groupBy(_._1).view.mapValues(_.map(_._2).sum)
      val oldMessageCounters = Service.getByMessageTypes(updates.keys.toSeq.distinct)

      def update(oldMessageCounters: Seq[MessageCounter], addCounters: MapView[String, Int]) = {
        Service.insertOrUpdateMultiple(addCounters.map(addCounter => MessageCounter(messageType = addCounter._1, counter = oldMessageCounters.find(_.messageType == addCounter._1).fold(0)(_.counter) + addCounter._2)).toSeq)
      }

      for {
        oldMessageCounters <- oldMessageCounters
        _ <- update(oldMessageCounters = oldMessageCounters, addCounters = updates)
      } yield ()
    }

    def getMessagesStatistics: Future[ListMap[String, Double]] = {
      val all = Service.fetchAll.map(_.sortBy(_.counter).reverse)
      for {
        all <- all
      } yield ListMap(all.sortBy(_.counter).reverse.map(x => x.messageType -> x.counter.toDouble): _*)
    }

  }
}