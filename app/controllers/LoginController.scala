package controllers

import akka.actor.ActorSystem
import javax.inject.Inject
import models.{Account, Accounts}
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}

import scala.concurrent.ExecutionContext

class LoginController @Inject()(messagesControllerComponents: MessagesControllerComponents, actorSystem: ActorSystem, accounts: Accounts)(implicit exec: ExecutionContext) extends MessagesAbstractController(messagesControllerComponents) {

  def login = Action.async {
    accounts.add(Account("a", "b", 1, "d", "e", "s", "s")).map(x => Ok(s"$x"))
  }
}
