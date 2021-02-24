package controllers

import constants.Response.Success
import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction, WithoutLoginAction, WithoutLoginActionAsync, _}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.ACL
import models.master.{Asset, Negotiation, Order}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, _}
import play.api.{Configuration, Logger}
import transactions.blockchain.{BuyerExecuteOrder, SellerExecuteOrder}
import utilities.MicroNumber
import views.companion.{blockchain => blockchainCompanion}
import views.html.component.blockchain.{txForms => blockchainForms}

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class OrderController @Inject()(
                                 blockchainAccounts: blockchain.Accounts,
                                 blockchainACLAccounts: blockchain.ACLAccounts,
                                 blockchainACLHashes: blockchain.ACLHashes,
                                 masterAssets: master.Assets,
                                 masterNegotiations: master.Negotiations,
                                 masterOrders: master.Orders,
                                 masterTraders: master.Traders,
                                 masterZones: master.Zones,
                                 masterAccounts: master.Accounts,
                                 masterTransactionAssetFiles: masterTransaction.AssetFiles,
                                 masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                 masterTransactionTradeActivities: masterTransaction.TradeActivities,
                                 messagesControllerComponents: MessagesControllerComponents,
                                 transaction: utilities.Transaction,
                                 transactionsBuyerExecuteOrder: BuyerExecuteOrder,
                                 transactionsSellerExecuteOrder: SellerExecuteOrder,
                                 utilitiesNotification: utilities.Notification,
                                 withTraderLoginAction: WithTraderLoginAction,
                                 withZoneLoginAction: WithZoneLoginAction,
                                 withLoginActionAsync: WithLoginActionAsync,
                                 withUnknownLoginAction: WithUnknownLoginAction,
                                 blockchainOrders: blockchain.Orders,
                                 blockchainClassifications: blockchain.Classifications,
                                 masterClassifications: master.Classifications,
                                 masterProperties: master.Properties,
                                 transactionsOrderDefine: transactions.blockchain.OrderDefine,
                                 blockchainTransactionOrderDefines: blockchainTransaction.OrderDefines,
                                 transactionsOrderMake: transactions.blockchain.OrderMake,
                                 blockchainTransactionOrderMakes: blockchainTransaction.OrderMakes,
                                 transactionsOrderTake: transactions.blockchain.OrderTake,
                                 blockchainTransactionOrderTakes: blockchainTransaction.OrderTakes,
                                 transactionsOrderCancel: transactions.blockchain.OrderCancel,
                                 blockchainTransactionOrderCancels: blockchainTransaction.OrderCancels,
                                 withUserLoginAction: WithUserLoginAction,
                                 blockchainIdentities: blockchain.Identities,
                                 withUsernameToken: WithUsernameToken,
                                 withGenesisLoginAction: WithGenesisLoginAction,
                                 withoutLoginAction: WithoutLoginAction,
                                 withoutLoginActionAsync: WithoutLoginActionAsync
                               )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ORDER

  private val stakingDenom = configuration.get[String]("blockchain.stakingDenom")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")
  //TODO fix the order controllers when the splits issue tx is defined
  def makeForm(negotiationID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.makeOrder(negotiationID = negotiationID))
  }

  def make: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.MakeOrder.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.makeOrder(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.NEGOTIATION_ID.name, ""))))
        },
        makeData => {

          val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = makeData.password)
          val negotiation = masterNegotiations.Service.tryGet(makeData.negotiationID)
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          def getCounterPartyAccountID(negotiation: Negotiation)= masterTraders.Service.tryGetAccountId(negotiation.sellerTraderID)
          def sendTransactionAndGetResult(verifyPassword: Boolean, negotiation: Negotiation, traderID: String, counterPartyAccountID:String) = {
            if (verifyPassword) {
              if (negotiation.status == constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED && traderID == negotiation.buyerTraderID) {

                val immutableMetas = Seq(constants.Property.ORDER_TYPE.getBaseProperty(constants.Blockchain.parameterValues.ORDER_TYPE))
                val immutables = Seq(constants.Property.FIAT_PROOF.getBaseProperty(makeData.fiatProof))
                val mutables = Seq(constants.Property.TAKER_ID.getBaseProperty(""),constants.Property.EXCHANGE_RATE.getBaseProperty("1"))
                val mutableMetas = Seq(constants.Property.BUYER_ACCOUNT_ID.getBaseProperty(loginState.username), constants.Property.SELLER_ACCOUNT_ID.getBaseProperty(counterPartyAccountID))

                val broadcastTx = transaction.process[blockchainTransaction.OrderMake, transactionsOrderMake.Request](
                  entity = blockchainTransaction.OrderMake(from = loginState.address, fromID = negotiation.buyerTraderID, classificationID = constants.Blockchain.Classification.ORDER, makerOwnableID = stakingDenom, takerOwnableID = negotiation.assetID, makerOwnableSplit = negotiation.price.doubleValue, expiresIn = constants.Blockchain.expiresIn, immutableMetaProperties = immutableMetas, immutableProperties = immutables, mutableMetaProperties = mutableMetas, mutableProperties = mutables, gas = makeData.gas, ticketID = "", mode = transactionMode),
                  blockchainTransactionCreate = blockchainTransactionOrderMakes.Service.create,
                  request = transactionsOrderMake.Request(transactionsOrderMake.Message(transactionsOrderMake.BaseReq(from = loginState.address, gas = makeData.gas), fromID = negotiation.buyerTraderID, classificationID = constants.Blockchain.Classification.ORDER, makerOwnableID = stakingDenom, takerOwnableID = negotiation.assetID, expiresIn = constants.Blockchain.expiresIn, makerOwnableSplit = negotiation.price.doubleValue, immutableMetaProperties = immutableMetas, immutableProperties = immutables, mutableMetaProperties = mutableMetas, mutableProperties = mutables)),
                  action = transactionsOrderMake.Service.post,
                  onSuccess = blockchainTransactionOrderMakes.Utility.onSuccess,
                  onFailure = blockchainTransactionOrderMakes.Utility.onFailure,
                  updateTransactionHash = blockchainTransactionOrderMakes.Service.updateTransactionHash)

                for {
                  ticketID <- broadcastTx
                  _ <- utilitiesNotification.send(loginState.username, constants.Notification.ORDER_MADE, ticketID)()
                  _ <- utilitiesNotification.send(counterPartyAccountID, constants.Notification.ORDER_MADE, ticketID)()
                  _ <- masterTransactionTradeActivities.Service.create(negotiation.id, constants.TradeActivity.ORDER_MADE, ticketID)
                  result <- withUsernameToken.Ok(views.html.account(successes = Seq(constants.Response.ORDER_CREATED)))
                } yield result

              } else {
                throw new BaseException(constants.Response.UNAUTHORIZED)
              }
            } else {
              Future(BadRequest(views.html.component.master.makeOrder(views.companion.master.MakeOrder.form.fill(views.companion.master.MakeOrder.Data(makeData.negotiationID, makeData.fiatProof, makeData.gas, makeData.password)), makeData.negotiationID)))
            }
          }


          for {
            verifyPassword <- verifyPassword
            negotiation <- negotiation
            traderID <- traderID
            counterPartyAccountID<-getCounterPartyAccountID(negotiation)
            result <- sendTransactionAndGetResult(verifyPassword, negotiation, traderID, counterPartyAccountID)
          } yield result
        })
  }


  def takeForm(orderID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.takeOrder(orderID = orderID))
  }

  def take: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.TakeOrder.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.takeOrder(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.ORDER_ID.name, ""))))
        },
        takeData => {
          val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = takeData.password)
          val negotiation = masterNegotiations.Service.tryGet(takeData.orderID)
          val order = masterOrders.Service.tryGet(takeData.orderID)
          val traderID = masterTraders.Service.tryGetID(loginState.username)

          def sendTransactionAndGetResult(verifyPassword: Boolean, order: Order, negotiation: Negotiation, traderID: String) = {
            if (verifyPassword) {
              if (order.status.isEmpty && traderID == negotiation.buyerTraderID) {

                val sendTx = transaction.process[blockchainTransaction.OrderTake, transactionsOrderTake.Request](
                  entity = blockchainTransaction.OrderTake(from = loginState.address, fromID = traderID, orderID = takeData.orderID, takerOwnableSplit = constants.Blockchain.SmallestDec, gas = takeData.gas, ticketID = "", mode = transactionMode),
                  blockchainTransactionCreate = blockchainTransactionOrderTakes.Service.create,
                  request = transactionsOrderTake.Request(transactionsOrderTake.Message(transactionsOrderTake.BaseReq(from = loginState.address, gas = takeData.gas), fromID = traderID, orderID = takeData.orderID, takerOwnableSplit = constants.Blockchain.SmallestDec)),
                  action = transactionsOrderTake.Service.post,
                  onSuccess = blockchainTransactionOrderTakes.Utility.onSuccess,
                  onFailure = blockchainTransactionOrderTakes.Utility.onFailure,
                  updateTransactionHash = blockchainTransactionOrderTakes.Service.updateTransactionHash
                )

                for {
                  _ <- sendTx
                  result <- withUsernameToken.Ok(views.html.tradeRoom(negotiationID = negotiation.id))
                } yield result
              } else {
                throw new BaseException(constants.Response.UNAUTHORIZED)
              }
            } else {
              Future(BadRequest(views.html.component.master.takeOrder(orderID = takeData.orderID)))
            }
          }

          for {
            verifyPassword <- verifyPassword
            negotiation <- negotiation
            traderID <- traderID
            order <- order
            result <- sendTransactionAndGetResult(verifyPassword, order, negotiation, traderID)
          } yield result
        })
  }

  private def getNumberOfFields(addField: Boolean, currentNumber: Int) = if (addField) currentNumber + 1 else currentNumber

  def defineForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(blockchainForms.orderDefine())
  }

  def define: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      blockchainCompanion.OrderDefine.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.orderDefine(formWithErrors)))
        },
        defineData => {
          if (defineData.addImmutableMetaField || defineData.addImmutableField || defineData.addMutableMetaField || defineData.addMutableField) {
            Future(PartialContent(blockchainForms.orderDefine(
              orderDefineForm = blockchainCompanion.OrderDefine.form.fill(defineData.copy(addImmutableMetaField = false, addImmutableField = false, addMutableMetaField = false, addMutableField = false)),
              numImmutableMetaForms = getNumberOfFields(defineData.addImmutableMetaField, defineData.immutableMetaTraits.fold(0)(_.flatten.length)),
              numImmutableForms = getNumberOfFields(defineData.addImmutableField, defineData.immutableTraits.fold(0)(_.flatten.length)),
              numMutableMetaForms = getNumberOfFields(defineData.addMutableMetaField, defineData.mutableMetaTraits.fold(0)(_.flatten.length)),
              numMutableForms = getNumberOfFields(defineData.addMutableField, defineData.mutableTraits.fold(0)(_.flatten.length)))))
          } else {
            val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = defineData.password.getOrElse(""))
            val immutableMetas = defineData.immutableMetaTraits.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val immutables = defineData.immutableTraits.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val mutableMetas = defineData.mutableMetaTraits.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val mutables = defineData.mutableTraits.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)

            def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
              val broadcastTx = transaction.process[blockchainTransaction.OrderDefine, transactionsOrderDefine.Request](
                entity = blockchainTransaction.OrderDefine(from = loginState.address, fromID = defineData.fromID, immutableMetaTraits = immutableMetas, immutableTraits = immutables, mutableMetaTraits = mutableMetas, mutableTraits = mutables, gas = defineData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionOrderDefines.Service.create,
                request = transactionsOrderDefine.Request(transactionsOrderDefine.Message(transactionsOrderDefine.BaseReq(from = loginState.address, gas = defineData.gas), fromID = defineData.fromID, immutableMetaTraits = immutableMetas, immutableTraits = immutables, mutableMetaTraits = mutableMetas, mutableTraits = mutables)),
                action = transactionsOrderDefine.Service.post,
                onSuccess = blockchainTransactionOrderDefines.Utility.onSuccess,
                onFailure = blockchainTransactionOrderDefines.Utility.onFailure,
                updateTransactionHash = blockchainTransactionOrderDefines.Service.updateTransactionHash)

              for {
                ticketID <- broadcastTx
                result <- withUsernameToken.Ok(views.html.order(successes = Seq(new Success(ticketID))))
              } yield result
            } else Future(BadRequest(blockchainForms.orderDefine(blockchainCompanion.OrderDefine.form.fill(defineData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message))))

            (for {
              verifyPassword <- verifyPassword
              result <- broadcastTxAndGetResult(verifyPassword)
            } yield result
              ).recover {
              case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
            }
          }
        }
      )
  }

  def blockchainMakeForm(classificationID: String): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val properties = masterProperties.Service.getAll(entityID = classificationID, entityType = constants.Blockchain.Entity.ORDER_DEFINITION)
      val maintainerIDs = masterClassifications.Service.getMaintainerIDs(classificationID)
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      (for {
        properties <- properties
        maintainerIDs <- maintainerIDs
        identityIDs <- identityIDs
      } yield {
        if (properties.nonEmpty && maintainerIDs.intersect(identityIDs).nonEmpty) {
          val immutableMetaProperties = Option(properties.filter(x => x.isMeta && !x.isMutable).map(x => Option(views.companion.common.Property.Data(dataType = x.dataType, dataName = x.name, dataValue = x.value))))
          val immutableProperties = Option(properties.filter(x => !x.isMeta && !x.isMutable).map(x => Option(views.companion.common.Property.Data(dataType = x.dataType, dataName = x.name, dataValue = x.value))))
          //Special Case need to remove expiry and makerOwnableSplit from Mutables Meta
          val mutableMetaProperties = Option(properties.filter(x => x.isMeta && x.isMutable && x.name != constants.Blockchain.Properties.Expiry && x.name != constants.Blockchain.Properties.MakerOwnableSplit).map(x => Option(views.companion.common.Property.Data(dataType = x.dataType, dataName = x.name, dataValue = x.value))))
          val mutableProperties = Option(properties.filter(x => !x.isMeta && x.isMutable).map(x => Option(views.companion.common.Property.Data(dataType = x.dataType, dataName = x.name, dataValue = x.value))))
          Ok(blockchainForms.orderMake(blockchainCompanion.OrderMake.form.fill(blockchainCompanion.OrderMake.Data(fromID = maintainerIDs.intersect(identityIDs).headOption.getOrElse(""), classificationID = classificationID, makerOwnableID = "", takerOwnableID = "", expiresIn = 0, makerOwnableSplit = 0.0, immutableMetaProperties = immutableMetaProperties, addImmutableMetaField = false, immutableProperties = immutableProperties, addImmutableField = false, mutableMetaProperties = mutableMetaProperties, addMutableMetaField = false, mutableProperties = mutableProperties, addMutableField = false, gas = MicroNumber.zero, password = None)), classificationID = classificationID, numImmutableMetaForms = immutableMetaProperties.fold(0)(_.length), numImmutableForms = immutableProperties.fold(0)(_.length), numMutableMetaForms = mutableMetaProperties.fold(0)(_.length), numMutableForms = mutableProperties.fold(0)(_.length)))
        } else {
          Ok(blockchainForms.orderMake(classificationID = classificationID))
        }
      }
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }


  def blockchainMake: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      blockchainCompanion.OrderMake.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.orderMake(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.CLASSIFICATION_ID.name, ""))))
        },
        makeData => {
          if (makeData.addImmutableMetaField || makeData.addImmutableField || makeData.addMutableMetaField || makeData.addMutableField) {
            Future(PartialContent(blockchainForms.orderMake(
              orderMakeForm = blockchainCompanion.OrderMake.form.fill(makeData.copy(addImmutableMetaField = false, addImmutableField = false, addMutableMetaField = false, addMutableField = false)),
              numImmutableMetaForms = getNumberOfFields(makeData.addImmutableMetaField, makeData.immutableMetaProperties.fold(0)(_.flatten.length)),
              classificationID = makeData.classificationID,
              numImmutableForms = getNumberOfFields(makeData.addImmutableField, makeData.immutableProperties.fold(0)(_.flatten.length)),
              numMutableMetaForms = getNumberOfFields(makeData.addMutableMetaField, makeData.mutableMetaProperties.fold(0)(_.flatten.length)),
              numMutableForms = getNumberOfFields(makeData.addMutableField, makeData.mutableProperties.fold(0)(_.flatten.length)))))
          } else {
            val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = makeData.password.getOrElse(""))
            val immutableMetas = makeData.immutableMetaProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val immutables = makeData.immutableProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val mutableMetas = makeData.mutableMetaProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val mutables = makeData.mutableProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)

            def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
              val broadcastTx = transaction.process[blockchainTransaction.OrderMake, transactionsOrderMake.Request](
                entity = blockchainTransaction.OrderMake(from = loginState.address, fromID = makeData.fromID, classificationID = makeData.classificationID, makerOwnableID = makeData.makerOwnableID, takerOwnableID = makeData.takerOwnableID, makerOwnableSplit = makeData.makerOwnableSplit, expiresIn = makeData.expiresIn, immutableMetaProperties = immutableMetas, immutableProperties = immutables, mutableMetaProperties = mutableMetas, mutableProperties = mutables, gas = makeData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionOrderMakes.Service.create,
                request = transactionsOrderMake.Request(transactionsOrderMake.Message(transactionsOrderMake.BaseReq(from = loginState.address, gas = makeData.gas), fromID = makeData.fromID, classificationID = makeData.classificationID, makerOwnableID = makeData.makerOwnableID, takerOwnableID = makeData.takerOwnableID, expiresIn = makeData.expiresIn, makerOwnableSplit = makeData.makerOwnableSplit, immutableMetaProperties = immutableMetas, immutableProperties = immutables, mutableMetaProperties = mutableMetas, mutableProperties = mutables)),
                action = transactionsOrderMake.Service.post,
                onSuccess = blockchainTransactionOrderMakes.Utility.onSuccess,
                onFailure = blockchainTransactionOrderMakes.Utility.onFailure,
                updateTransactionHash = blockchainTransactionOrderMakes.Service.updateTransactionHash)

              for {
                ticketID <- broadcastTx
                result <- withUsernameToken.Ok(views.html.order(successes = Seq(new Success(ticketID))))
              } yield result
            } else Future(BadRequest(blockchainForms.orderMake(blockchainCompanion.OrderMake.form.fill(makeData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), makeData.classificationID)))

            (for {
              verifyPassword <- verifyPassword
              result <- broadcastTxAndGetResult(verifyPassword)
            } yield result
              ).recover {
              case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
            }
          }
        }
      )
  }

  def blockchainTakeForm(orderID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(blockchainForms.orderTake(orderID = orderID))
  }

  def blockchainTake: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      blockchainCompanion.OrderTake.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.orderTake(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.ORDER_ID.name, ""))))
        },
        takeData => {
          val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = takeData.password)

          def broadcastTx = transaction.process[blockchainTransaction.OrderTake, transactionsOrderTake.Request](
            entity = blockchainTransaction.OrderTake(from = loginState.address, fromID = takeData.fromID, orderID = takeData.orderID, takerOwnableSplit = takeData.takerOwnableSplit, gas = takeData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionOrderTakes.Service.create,
            request = transactionsOrderTake.Request(transactionsOrderTake.Message(transactionsOrderTake.BaseReq(from = loginState.address, gas = takeData.gas), fromID = takeData.fromID, orderID = takeData.orderID, takerOwnableSplit = takeData.takerOwnableSplit)),
            action = transactionsOrderTake.Service.post,
            onSuccess = blockchainTransactionOrderTakes.Utility.onSuccess,
            onFailure = blockchainTransactionOrderTakes.Utility.onFailure,
            updateTransactionHash = blockchainTransactionOrderTakes.Service.updateTransactionHash
          )

          def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
            for {
              ticketID <- broadcastTx
              result <- withUsernameToken.Ok(views.html.order(successes = Seq(new Success(ticketID))))
            } yield result
          } else Future(BadRequest(blockchainForms.orderTake(blockchainCompanion.OrderTake.form.fill(takeData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), takeData.orderID)))

          (for {
            verifyPassword <- verifyPassword
            result <- broadcastTxAndGetResult(verifyPassword)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def cancelForm(orderID: String, makerID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(blockchainForms.orderCancel(orderID = orderID, makerID = makerID))
  }

  def cancel: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      blockchainCompanion.OrderCancel.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.orderCancel(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.ORDER_ID.name, ""), formWithErrors.data.getOrElse(constants.FormField.FROM_ID.name, ""))))
        },
        cancelData => {
          val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = cancelData.password)

          def broadcastTx = transaction.process[blockchainTransaction.OrderCancel, transactionsOrderCancel.Request](
            entity = blockchainTransaction.OrderCancel(from = loginState.address, fromID = cancelData.fromID, orderID = cancelData.orderID, gas = cancelData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionOrderCancels.Service.create,
            request = transactionsOrderCancel.Request(transactionsOrderCancel.Message(transactionsOrderCancel.BaseReq(from = loginState.address, gas = cancelData.gas), fromID = cancelData.fromID, orderID = cancelData.orderID)),
            action = transactionsOrderCancel.Service.post,
            onSuccess = blockchainTransactionOrderCancels.Utility.onSuccess,
            onFailure = blockchainTransactionOrderCancels.Utility.onFailure,
            updateTransactionHash = blockchainTransactionOrderCancels.Service.updateTransactionHash
          )

          def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
            for {
              ticketID <- broadcastTx
              result <- withUsernameToken.Ok(views.html.order(successes = Seq(new Success(ticketID))))
            } yield result
          } else Future(BadRequest(blockchainForms.orderCancel(blockchainCompanion.OrderCancel.form.fill(cancelData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), cancelData.orderID, cancelData.fromID)))

          (for {
            verifyPassword <- verifyPassword
            result <- broadcastTxAndGetResult(verifyPassword)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
          }
        }
      )
  }

}
