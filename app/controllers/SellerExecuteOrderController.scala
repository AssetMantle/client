package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.SellerExecuteOrder
import views.companion.blockchain.SellerExecuteOrder

import scala.concurrent.ExecutionContext

class SellerExecuteOrderController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionSellerExecuteOrder: SellerExecuteOrder)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def sellerExecuteOrderForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.sellerExecuteOrder(SellerExecuteOrder.form))
  }

  def sellerExecuteOrder: Action[AnyContent] = Action { implicit request =>
    SellerExecuteOrder.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.sellerExecuteOrder(formWithErrors))
      },
      sellerExecuteOrderData => {
        try {
          Ok(views.html.index(transactionSellerExecuteOrder.Service.post(new transactionSellerExecuteOrder.Request(sellerExecuteOrderData.from, sellerExecuteOrderData.password, sellerExecuteOrderData.buyerAddress, sellerExecuteOrderData.sellerAddress, sellerExecuteOrderData.awbProofHash, sellerExecuteOrderData.pegHash, sellerExecuteOrderData.chainID, sellerExecuteOrderData.gas)).txHash))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))

        }
      })
  }
}
