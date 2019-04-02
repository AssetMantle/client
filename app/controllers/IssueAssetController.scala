package controllers

import controllers.actions.WithLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchain
import models.blockchainTransaction.IssueAssets
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.blockchain.IssueAsset
import views.companion.master

import scala.concurrent.ExecutionContext
import scala.util.Random

class IssueAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: models.master.Accounts, withLoginAction: WithLoginAction, blockchainAssets: blockchain.Assets, transactionIssueAsset: transactions.IssueAsset, issueAssets: IssueAssets)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def issueAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.issueAsset(master.IssueAsset.form))
  }

  def issueAsset: Action[AnyContent] = withLoginAction { implicit request =>
    master.IssueAsset.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.issueAsset(formWithErrors))
      },
      issueAssetData => {
        try {
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionIssueAsset.Service.kafkaPost(transactionIssueAsset.Request(from = request.session.get(constants.Security.USERNAME).get, to = issueAssetData.to, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas))
            issueAssets.Service.addIssueAssetKafka(from = request.session.get(constants.Security.USERNAME).get, to = issueAssetData.to, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            if (masterAccounts.Service.getUserTypeOnAddress(issueAssetData.to) == constants.User.TRADER) {
              val response = transactionIssueAsset.Service.post(transactionIssueAsset.Request(from = request.session.get(constants.Security.USERNAME).get, to = issueAssetData.to, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas))
              for (tag <- response.Tags) {
                if (tag.Key == constants.Response.KEY_ASSET) {
                  blockchainAssets.Service.addAsset(pegHash = tag.Value, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, ownerAddress = issueAssetData.to)
                }
              }
              Ok(views.html.index(success = response.TxHash))
            } else {
              Ok(views.html.index(failure = Messages(constants.User.NOT_TRADER)))
            }
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      }
    )
  }

  def blockchainIssueAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.issueAsset(IssueAsset.form))
  }

  def blockchainIssueAsset: Action[AnyContent] = Action { implicit request =>
    IssueAsset.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.issueAsset(formWithErrors))
      },
      issueAssetData => {
        try {
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionIssueAsset.Service.kafkaPost(transactionIssueAsset.Request(from = issueAssetData.from, to = issueAssetData.to, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas))
            issueAssets.Service.addIssueAssetKafka(from = issueAssetData.from, to = issueAssetData.to, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionIssueAsset.Service.post(transactionIssueAsset.Request(from = issueAssetData.from, to = issueAssetData.to, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas))
            issueAssets.Service.addIssueAsset(from = issueAssetData.from, to = issueAssetData.to, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, gas = issueAssetData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
            Ok(views.html.index(success = response.TxHash))
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      }
    )
  }
}
