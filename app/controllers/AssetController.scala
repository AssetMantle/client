package controllers

import controllers.actions.{LoginState, WithLoginAction, WithTraderLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.{Asset, Trader}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AssetController @Inject()(
                                 blockchainAccounts: blockchain.Accounts,
                                 blockchainAclAccounts: blockchain.ACLAccounts,
                                 blockchainAclHashes: blockchain.ACLHashes,
                                 blockchainAssets: blockchain.Assets,
                                 blockchainNegotiations: blockchain.Negotiations,
                                 blockchainOrders: blockchain.Orders,
                                 blockchainOrganizations: blockchain.Organizations,
                                 blockchainZones: blockchain.Zones,
                                 blockchainTransactionIssueAssets: blockchainTransaction.IssueAssets,
                                 masterAccounts: master.Accounts,
                                 masterAccountKYCs: master.AccountKYCs,
                                 masterIdentifications: master.Identifications,
                                 masterOrganizations: master.Organizations,
                                 masterTraders: master.Traders,
                                 masterTradeRelations: master.TraderRelations,
                                 masterZones: master.Zones,
                                 masterAssets: master.Assets,
                                 messagesControllerComponents: MessagesControllerComponents,
                                 withTraderLoginAction: WithTraderLoginAction,
                                 withLoginAction: WithLoginAction,
                                 withUsernameToken: WithUsernameToken,
                                 transaction: utilities.Transaction,
                                 transactionsIssueAsset: transactions.IssueAsset,
                                 utilitiesNotification: utilities.Notification,
                               )
                               (implicit
                                executionContext: ExecutionContext,
                                configuration: Configuration
                               ) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_ASSET

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  def issueForm(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.issueAsset())
  }

  def issue(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.IssueAsset.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.issueAsset(formWithErrors)))
        },
        issueAssetData => {
          (loginState.acl match {
            case Some(acl) => {
              if (acl.issueAsset) {
                val traderID = masterTraders.Service.tryGetID(loginState.username)

                def getResult(traderID: String): Future[Result] = {

                  def getAllTradableAssets(traderID: String): Future[Seq[Asset]] = masterAssets.Service.getAllTradableAssets(traderID)

                  def getCounterPartyList(traderID: String): Future[Seq[String]] = masterTradeRelations.Service.getAllCounterParties(traderID)

                  def getCounterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

                  if (issueAssetData.moderated) {
                    val addModeratedAsset = masterAssets.Service.addModerated(ownerID = traderID, assetType = issueAssetData.assetType, description = issueAssetData.description, quantity = issueAssetData.quantity, quantityUnit = issueAssetData.quantityUnit, price = issueAssetData.price, shippingPeriod = issueAssetData.shippingPeriod, portOfLoading = issueAssetData.portOfLoading, portOfDischarge = issueAssetData.portOfDischarge)

                    for {
                      _ <- addModeratedAsset
                      tradableAssets <- getAllTradableAssets(traderID)
                      counterPartyList <- getCounterPartyList(traderID)
                      counterPartyTraders <- getCounterPartyTraders(counterPartyList)
                      result <- withUsernameToken.PartialContent(views.html.component.master.negotiationRequest(tradableAssets = tradableAssets, counterPartyTraders = counterPartyTraders))
                    } yield result
                  } else {
                    val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = issueAssetData.password.getOrElse(""))

                    def issueAssetAndGetResult(validateUsernamePassword: Boolean): Future[Result] = if (validateUsernamePassword) {
                      val addUnmoderatedAsset = masterAssets.Service.addUnmoderated(ownerID = traderID, assetType = issueAssetData.assetType, description = issueAssetData.description, quantity = issueAssetData.quantity, quantityUnit = issueAssetData.quantityUnit, price = issueAssetData.price, shippingPeriod = issueAssetData.shippingPeriod, portOfLoading = issueAssetData.portOfLoading, portOfDischarge = issueAssetData.portOfDischarge)

                      def sendTransaction(documentHash: String): Future[String] = transaction.process[blockchainTransaction.IssueAsset, transactionsIssueAsset.Request](
                        entity = blockchainTransaction.IssueAsset(from = loginState.address, to = loginState.address, documentHash = documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.price, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.quantity, moderated = false, takerAddress = None, gas = issueAssetData.gas.getOrElse(throw new BaseException(constants.Response.GAS_NOT_GIVEN)), ticketID = "", mode = transactionMode),
                        blockchainTransactionCreate = blockchainTransactionIssueAssets.Service.create,
                        request = transactionsIssueAsset.Request(transactionsIssueAsset.BaseReq(from = loginState.address, gas = issueAssetData.gas.getOrElse(throw new BaseException(constants.Response.GAS_NOT_GIVEN)).toString), to = loginState.address, password = issueAssetData.password.getOrElse(throw new BaseException(constants.Response.PASSWORD_NOT_GIVEN)), documentHash = documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.price.toString, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.quantity.toString, moderated = false, takerAddress = "", mode = transactionMode),
                        action = transactionsIssueAsset.Service.post,
                        onSuccess = blockchainTransactionIssueAssets.Utility.onSuccess,
                        onFailure = blockchainTransactionIssueAssets.Utility.onFailure,
                        updateTransactionHash = blockchainTransactionIssueAssets.Service.updateTransactionHash
                      )

                      for {
                        documentHash <- addUnmoderatedAsset
                        ticketID <- sendTransaction(documentHash)
                        tradableAssets <- getAllTradableAssets(traderID)
                        counterPartyList <- getCounterPartyList(traderID)
                        counterPartyTraders <- getCounterPartyTraders(counterPartyList)
                        result <- withUsernameToken.PartialContent(views.html.component.master.negotiationRequest(tradableAssets = tradableAssets, counterPartyTraders = counterPartyTraders))
                      } yield result
                    }
                    else {
                      Future(BadRequest(views.html.component.master.issueAsset(views.companion.master.IssueAsset.form.fill(issueAssetData).withGlobalError(constants.Response.INCORRECT_PASSWORD.message))))
                    }

                    for {
                      validateUsernamePassword <- validateUsernamePassword
                      result <- issueAssetAndGetResult(validateUsernamePassword)
                    } yield result
                  }
                }

                for {
                  traderID <- traderID
                  result <- getResult(traderID)
                } yield result
              } else {
                throw new BaseException(constants.Response.UNAUTHORIZED)
              }
            }
            case None => {
              throw new BaseException(constants.Response.UNAUTHORIZED)
            }
          }).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

}
