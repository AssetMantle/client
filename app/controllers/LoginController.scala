package controllers

import akka.actor.ActorSystem
import javax.inject.Inject
import models.master
import models.blockchain
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}

import scala.concurrent.ExecutionContext

class LoginController @Inject()(messagesControllerComponents: MessagesControllerComponents, actorSystem: ActorSystem, accounts: master.Accounts, accounts_bc: blockchain.Accounts )(implicit exec: ExecutionContext) extends MessagesAbstractController(messagesControllerComponents) {

  def login = Action.async {
    accounts_bc.add(blockchain.Account("a",1,"b","c","d"))
    accounts.add(master.Account("a", "b", "a")).map(x => Ok(s"$x"))
  }
}
