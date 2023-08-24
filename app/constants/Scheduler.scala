package constants

import akka.actor.Cancellable
import play.api.Logger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, FiniteDuration, SECONDS}

abstract class Scheduler extends Runnable {

  val name: String
  val initialDelay: FiniteDuration
  val fixedDelay: FiniteDuration

  def runner(): Unit

  final def run(): Unit = if (!utilities.Scheduler.getSignalReceived) this.runner()
  else utilities.Scheduler.shutdownThread(this.name)

  def start()(implicit schedulerExecutionContext: ExecutionContext, logger: Logger): Cancellable = {
    logger.info("Starting thread: " + this.name)
    actors.Service.actorSystem.scheduler.scheduleWithFixedDelay(initialDelay = initialDelay, delay = fixedDelay)(this)(schedulerExecutionContext)
  }
}

object Scheduler {

  def HalfDay: FiniteDuration = Duration.create(43200, SECONDS)

  def OneHour: FiniteDuration = Duration.create(3600, SECONDS)

  def HalfHour: FiniteDuration = Duration.create(1800, SECONDS)

  def QuarterHour: FiniteDuration = Duration.create(900, SECONDS)

  def FiveMinutes: FiniteDuration = Duration.create(300, SECONDS)
}
