package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.BuyerExecuteOrder
import views.companion.blockchain.BuyerExecuteOrder

import scala.concurrent.ExecutionContext

class BuyerExecuteOrderController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionBuyerExecuteOrder: BuyerExecuteOrder)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def buyerExecuteOrderForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.buyerExecuteOrder(BuyerExecuteOrder.form))
  }

  def buyerExecuteOrder: Action[AnyContent] = Action { implicit request =>
    BuyerExecuteOrder.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.buyerExecuteOrder(formWithErrors))
      },
      buyerExecuteOrderData => {
        try {
          Ok(views.html.index(transactionBuyerExecuteOrder.Service.post(new transactionBuyerExecuteOrder.Request(buyerExecuteOrderData.from, buyerExecuteOrderData.password, buyerExecuteOrderData.buyerAddress, buyerExecuteOrderData.sellerAddress, buyerExecuteOrderData.fiatProofHash, buyerExecuteOrderData.pegHash, buyerExecuteOrderData.chainID, buyerExecuteOrderData.gas)).txHash))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))

        }
      })
  }
}
