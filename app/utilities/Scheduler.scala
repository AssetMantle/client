package utilities

import akka.Done
import akka.actor.Cancellable
import constants.Scheduler
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

object Scheduler {

  private implicit val logger: Logger = Logger(this.getClass)

  private var signalReceived: Boolean = false

  private val SchedulersCancellable = collection.mutable.Map[String, Cancellable]()

  def getSignalReceived: Boolean = signalReceived

  def shutdownThread(name: String): Unit = {
    val cancellable = SchedulersCancellable.get(name)
    if (cancellable.isDefined) {
      val keysList = SchedulersCancellable.keys.toSeq.sorted
      if (cancellable.get.cancel()) logger.info(s"Successfully shutdown thread (${keysList.indexOf(name) + 1}/${keysList.length}): $name") else logger.error("Failed to shutdown thread: " + name)
    } else logger.error("Thread not found: " + name)
  }

  def shutdownListener()(implicit executionContext: ExecutionContext): () => Future[Done] = () => {
    signalReceived = true
    Thread.sleep(20000)
    Future(Done.done())
  }

  def startSchedulers(schedulers: Scheduler*)(implicit executionContext: ExecutionContext): Unit = schedulers.sortBy(_.name).foreach(x => SchedulersCancellable += (x.name -> x.start()))

}
