package controllers.actions

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.ACL
import models.{blockchain, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.routing.Router
import play.api.{Configuration, Logger}
import controllers.logging.WithActionAsyncLoggingFilter
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WithTraderLoginAction @Inject()(messagesControllerComponents: MessagesControllerComponents, withActionAsyncLoggingFilter: WithActionAsyncLoggingFilter,blockchainAccounts: blockchain.Accounts, masterAccounts: master.Accounts, blockchainACLHashes: blockchain.ACLHashes, blockchainACLAccounts: blockchain.ACLAccounts, masterTransactionSessionTokens: masterTransaction.SessionTokens)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.ACTIONS_WITH_TRADER_LOGIN_ACTION

  def authenticated(f: ⇒ LoginState => Request[AnyContent] => Future[Result])(implicit logger: Logger): Action[AnyContent] = {
    withActionAsyncLoggingFilter.next { implicit request ⇒
     // logger.info("host:"+request.host+"-requested-path-"+request.path+"-method-"+request.method+"-requestedBY-"+request.session.get(constants.Security.USERNAME))
      //println("remote Address"+request.remoteAddress)
      val handlerDef= request.attrs(Router.Attrs.HandlerDef)
     // println(handlerDef.controller+"."+handlerDef.method)

      val username = Future(request.session.get(constants.Security.USERNAME).getOrElse(throw new BaseException(constants.Response.USERNAME_NOT_FOUND)))
      val sessionToken = Future(request.session.get(constants.Security.TOKEN).getOrElse(throw new BaseException(constants.Response.TOKEN_NOT_FOUND)))

      def verifySessionTokenAndUserType(username: String, sessionToken: String): Future[String] = {
        val sessionTokenVerify = masterTransactionSessionTokens.Service.tryVerifyingSessionToken(username, sessionToken)
        val tokenTimeVerify = masterTransactionSessionTokens.Service.tryVerifyingSessionTokenTime(username)
        val verifyUserType = masterAccounts.Service.tryVerifyingUserType(username, constants.User.TRADER)
        val address = blockchainAccounts.Service.tryGetAddress(username)
        for {
          _ <- sessionTokenVerify
          _ <- tokenTimeVerify
          _ <- verifyUserType
          address <- address
        } yield address
      }

      def getLoginState(username: String, address: String): Future[LoginState] = {
        val aclHash = blockchainACLAccounts.Service.tryGetACLHash(address)

        def acl(aclHash: String): Future[ACL] = blockchainACLHashes.Service.tryGetACL(aclHash)

        for {
          aclHash <- aclHash
          acl <- acl(aclHash)
        } yield LoginState(username, constants.User.TRADER, address, Option(acl))
      }

      def result(loginState: LoginState): Future[Result] = f(loginState)(request)

      (for {
        username <- username
        sessionToken <- sessionToken
        address <- verifySessionTokenAndUserType(username, sessionToken)
        loginState <- getLoginState(username, address)
        result <- result(loginState)
      } yield result).recover {
        case baseException: BaseException => logger.info(baseException.failure.message, baseException)
          Results.Unauthorized(views.html.index(failures = Seq(baseException.failure)))
      }
    }
  }
}
