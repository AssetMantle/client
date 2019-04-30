package controllers

import exceptions.BlockChainException
import javax.inject.Inject
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}

import scala.concurrent.ExecutionContext

class BlockExplorerController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionsGetBlockHeight: transactions.GetBlockHeight, transactionsGetTransactionHash: transactions.GetTransactionHash)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {
  def showAllBlocks: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.showAllBlocks())
  }

  def validatorsList: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.validatorsTable())
  }

  def blockHeight(blockHeight: Int): Action[AnyContent] = Action { implicit request =>
    try {
      Ok(views.html.component.blockchain.blockHeight(transactionsGetBlockHeight.Service.get(blockHeight)))
    } catch {
      case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
    }
  }

  def transactionHash(txHash: String): Action[AnyContent] = Action { implicit request =>
    try {
      Ok(views.html.component.blockchain.transactionHash(transactionsGetTransactionHash.Service.get(txHash)))
    } catch {
      case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
    }
  }
}
