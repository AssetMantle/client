package utilities

import java.util.concurrent.TimeUnit

import akka.util.Timeout
import javax.inject.{Inject, Singleton}
import models.masterTransaction.AccountTokens
import play.api.{Configuration, Logger}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ShutdownActorMessage()

@Singleton
class ShutdownActors @Inject()(masterAccounts: models.master.Accounts, accountTokens: AccountTokens)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val timeout: Timeout = Timeout(FiniteDuration(1, TimeUnit.SECONDS))

  private val sessionTokenTimeout = configuration.get[Long]("sessionToken.timeout")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_SHUTDOWN

  def sessionTimeoutShutdown(username: String): Unit = Future {
    while (true) {
      Thread.sleep(sessionTokenTimeout)
      if (!accountTokens.Service.verifySessionTokenTime(Option(username))){
        shutdown(username)
        return
      }
    }
  }

  def logOutShutdown(username: String): Future[Unit] = Future{ shutdown(username)}

  def shutdown(username: String): Unit = {
    Actor.system.actorSelection("/user/" + masterAccounts.Service.getAddress(username)).resolveOne().onComplete {
      case Success(actorRef) => actorRef ! ShutdownActorMessage()
      case Failure(ex) => logger.info(module + ": " + ex.getMessage)
    }
  }
}

