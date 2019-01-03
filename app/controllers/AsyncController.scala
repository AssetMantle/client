package controllers

import akka.actor.ActorSystem
import javax.inject._
import models.blockchain._
import play.api.mvc.__
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}

/**
  * This controller creates an `Action` that demonstrates how to write
  * simple asynchronous code in a controller. It uses a timer to
  * asynchronously delay sending a response for 1 second.
  *
  * @param cc          standard controller components
  * @param actorSystem We need the `ActorSystem`'s `Scheduler` to
  *                    run code after a delay.
  * @param exec        We need an `ExecutionContext` to execute our
  *                    asynchronous code.  When rendering content, you should use Play's
  *                    default execution context, which is dependency injected.  If you are
  *                    using blocking operations, such as database or network access, then you should
  *                    use a different custom execution context that has a thread pool configured for
  *                    a blocking API.
  */
@Singleton
class AsyncController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem, accounts: Accounts, owners: Owners, fiats: Fiats)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  /**
    * Creates an Action that returns a plain text message after a delay
    * of 1 second.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/message`.
    */
  def message = Action.async {
    accounts.add(Account("bb", 12, "z3", "y3", "x3")).map(x => Ok(s"$x"))
    //getFutureMessage(1.second).map { msg => Ok(msg) }
  }

  def find = Action.async {

    accounts.findByAddress("b").map(x => Ok(s"$x"))
  }

  def delete = Action.async {

    accounts.deleteByAddress("c").map(x => Ok("Deleted"))

  }

  def addOwner = Action.async {

    owners.add(Owner("mm", "a", 123)).map(x => Ok(s"$x"))
  }

  def delOwner = Action.async {

    owners.deleteBypegHashOwnerAddress("mm", "a").map(x => Ok("Deleted"))
  }


  def addFiat = Action.async {

    fiats.add(Fiat("mm", "mck", 11, 11)).map(x => Ok(s"$x"))
  }



  private def getFutureMessage(delayTime: FiniteDuration): Future[String] = {
    val promise: Promise[String] = Promise[String]()
    actorSystem.scheduler.scheduleOnce(delayTime) {
      promise.success("Hi!")
    }(actorSystem.dispatcher) // run scheduled tasks using the actor system's dispatcher
    promise.future
  }

}
