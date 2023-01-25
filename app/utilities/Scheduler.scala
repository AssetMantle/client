package utilities

import akka.Done
import akka.actor.Cancellable
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

object Scheduler {

  private implicit val logger: Logger = Logger(this.getClass)

  private var signalReceived: Boolean = false

  private var shutdownCancellable: Option[Cancellable] = None

  def setShutdownCancellable(c: Cancellable): Unit = {
    shutdownCancellable = Option(c)
  }

  def getSignalReceived: Boolean = signalReceived

  def shutdownThread(): Unit = if (shutdownCancellable.isDefined) {
    if (shutdownCancellable.get.cancel()) logger.info("Successfully shutdown thread") else logger.error("Failed to shutdown thread")
  } else logger.error("Thread not found")

  def shutdownListener()(implicit executionContext: ExecutionContext): () => Future[Done] = () => {
    signalReceived = true
    Thread.sleep(10000)
    Future(Done.done())
  }

}
