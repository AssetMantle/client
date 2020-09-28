package controllers

import constants.Response.Success
import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchainTransaction
import models.common.Serializable.{MetaProperties, MetaProperty, Properties, Property}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrderController @Inject()(
                                 messagesControllerComponents: MessagesControllerComponents,
                                 transaction: utilities.Transaction,
                                 withLoginAction: WithLoginAction,
                                 withUnknownLoginAction: WithUnknownLoginAction,
                                 transactionsOrderDefine: transactions.blockchain.OrderDefine,
                                 blockchainTransactionOrderDefines: blockchainTransaction.OrderDefines,
                                 transactionsOrderMake: transactions.blockchain.OrderMake,
                                 blockchainTransactionOrderMakes: blockchainTransaction.OrderMakes,
                                 transactionsOrderTake: transactions.blockchain.OrderTake,
                                 blockchainTransactionOrderTakes: blockchainTransaction.OrderTakes,
                                 transactionsOrderCancel: transactions.blockchain.OrderCancel,
                                 blockchainTransactionOrderCancels: blockchainTransaction.OrderCancels,
                                 withUserLoginAction: WithUserLoginAction,
                                 withUsernameToken: WithUsernameToken,
                                 withoutLoginAction: WithoutLoginAction,
                                 withoutLoginActionAsync: WithoutLoginActionAsync
                               )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ORDER

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def getNumberOfFields(addField: Boolean, currentNumber: Int) = if (addField) currentNumber + 1 else currentNumber

  def defineForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.txForms.orderDefine())
  }

  def define: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.OrderDefine.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.txForms.orderDefine(formWithErrors)))
      },
      defineData => {
        if (defineData.addImmutableMetaField || defineData.addImmutableField || defineData.addMutableMetaField || defineData.addMutableField) {
          Future(PartialContent(views.html.component.blockchain.txForms.orderDefine(
            orderDefineForm = views.companion.blockchain.OrderDefine.form.fill(defineData.copy(addImmutableMetaField = false, addImmutableField = false, addMutableMetaField = false, addMutableField = false)),
            numImmutableMetaForms = getNumberOfFields(defineData.addImmutableMetaField, defineData.immutableMetaTraits.fold(0)(_.flatten.length)),
            numImmutableForms = getNumberOfFields(defineData.addImmutableField, defineData.immutableTraits.fold(0)(_.flatten.length)),
            numMutableMetaForms = getNumberOfFields(defineData.addMutableMetaField, defineData.mutableMetaTraits.fold(0)(_.flatten.length)),
            numMutableForms = getNumberOfFields(defineData.addMutableField, defineData.mutableTraits.fold(0)(_.flatten.length)))))
        } else {
          val ticketID = transaction.process[blockchainTransaction.OrderDefine, transactionsOrderDefine.Request](
            entity = blockchainTransaction.OrderDefine(from = defineData.from, fromID = defineData.fromID, immutableMetaTraits = MetaProperties(defineData.immutableMetaTraits.fold[Seq[MetaProperty]](Seq.empty)(_.flatten.map(_.toMetaProperty))), immutableTraits = Properties(defineData.immutableTraits.fold[Seq[Property]](Seq.empty)(_.flatten.map(_.toProperty))), mutableMetaTraits = MetaProperties(defineData.mutableMetaTraits.fold[Seq[MetaProperty]](Seq.empty)(_.flatten.map(_.toMetaProperty))), mutableTraits = Properties(defineData.mutableTraits.fold[Seq[Property]](Seq.empty)(_.flatten.map(_.toProperty))), gas = defineData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionOrderDefines.Service.create,
            request = transactionsOrderDefine.Request(transactionsOrderDefine.Message(transactionsOrderDefine.BaseReq(from = defineData.from, gas = defineData.gas), fromID = defineData.fromID, immutableMetaTraits = defineData.immutableMetaTraits.getOrElse(Seq.empty).flatten, immutableTraits = defineData.immutableTraits.getOrElse(Seq.empty).flatten, mutableMetaTraits = defineData.mutableMetaTraits.getOrElse(Seq.empty).flatten, mutableTraits = defineData.mutableTraits.getOrElse(Seq.empty).flatten)),
            action = transactionsOrderDefine.Service.post,
            onSuccess = blockchainTransactionOrderDefines.Utility.onSuccess,
            onFailure = blockchainTransactionOrderDefines.Utility.onFailure,
            updateTransactionHash = blockchainTransactionOrderDefines.Service.updateTransactionHash
          )
          (for {
            ticketID <- ticketID
          } yield Ok(views.html.dashboard(successes = Seq(new Success(ticketID))))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
          }
        }
      }
    )
  }

  def makeForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.txForms.orderMake())
  }

  def make: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.OrderMake.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.txForms.orderMake(formWithErrors)))
      },
      makeData => {
        if (makeData.addImmutableMetaField || makeData.addImmutableField || makeData.addMutableMetaField || makeData.addMutableField) {
          Future(PartialContent(views.html.component.blockchain.txForms.orderMake(
            orderMakeForm = views.companion.blockchain.OrderMake.form.fill(makeData.copy(addImmutableMetaField = false, addImmutableField = false, addMutableMetaField = false, addMutableField = false)),
            numImmutableMetaForms = getNumberOfFields(makeData.addImmutableMetaField, makeData.immutableMetaProperties.fold(0)(_.flatten.length)),
            numImmutableForms = getNumberOfFields(makeData.addImmutableField, makeData.immutableProperties.fold(0)(_.flatten.length)),
            numMutableMetaForms = getNumberOfFields(makeData.addMutableMetaField, makeData.mutableMetaProperties.fold(0)(_.flatten.length)),
            numMutableForms = getNumberOfFields(makeData.addMutableField, makeData.mutableProperties.fold(0)(_.flatten.length)))))
        } else {
          val ticketID = transaction.process[blockchainTransaction.OrderMake, transactionsOrderMake.Request](
            entity = blockchainTransaction.OrderMake(from = makeData.from, fromID = makeData.fromID, classificationID = makeData.classificationID, makerOwnableID = makeData.makerOwnableID, takerOwnableID = makeData.takerOwnableID, makerOwnableSplit = makeData.makerOwnableSplit, expiresIn = makeData.expiresIn, immutableMetaTraits = MetaProperties(makeData.immutableMetaProperties.fold[Seq[MetaProperty]](Seq.empty)(_.flatten.map(_.toMetaProperty))), immutableTraits = Properties(makeData.immutableProperties.fold[Seq[Property]](Seq.empty)(_.flatten.map(_.toProperty))), mutableMetaTraits = MetaProperties(makeData.mutableMetaProperties.fold[Seq[MetaProperty]](Seq.empty)(_.flatten.map(_.toMetaProperty))), mutableTraits = Properties(makeData.mutableProperties.fold[Seq[Property]](Seq.empty)(_.flatten.map(_.toProperty))), gas = makeData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionOrderMakes.Service.create,
            request = transactionsOrderMake.Request(transactionsOrderMake.Message(transactionsOrderMake.BaseReq(from = makeData.from, gas = makeData.gas), fromID = makeData.fromID, classificationID = makeData.classificationID, makerOwnableID = makeData.makerOwnableID, takerOwnableID = makeData.takerOwnableID, expiresIn = makeData.expiresIn, makerOwnableSplit = makeData.makerOwnableSplit, immutableMetaProperties = makeData.immutableMetaProperties.getOrElse(Seq.empty).flatten, immutableProperties = makeData.immutableProperties.getOrElse(Seq.empty).flatten, mutableMetaProperties = makeData.mutableMetaProperties.getOrElse(Seq.empty).flatten, mutableProperties = makeData.mutableProperties.getOrElse(Seq.empty).flatten)),
            action = transactionsOrderMake.Service.post,
            onSuccess = blockchainTransactionOrderMakes.Utility.onSuccess,
            onFailure = blockchainTransactionOrderMakes.Utility.onFailure,
            updateTransactionHash = blockchainTransactionOrderMakes.Service.updateTransactionHash
          )
          (for {
            ticketID <- ticketID
          } yield Ok(views.html.dashboard(successes = Seq(new Success(ticketID))))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
          }
        }
      }
    )
  }

  def takeForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.txForms.orderTake())
  }

  def take: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.OrderTake.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.txForms.orderTake(formWithErrors)))
      },
      takeData => {
        val ticketID = transaction.process[blockchainTransaction.OrderTake, transactionsOrderTake.Request](
          entity = blockchainTransaction.OrderTake(from = takeData.from, fromID = takeData.fromID, orderID = takeData.orderID, takerOwnableSplit = takeData.takerOwnableSplit, gas = takeData.gas, ticketID = "", mode = transactionMode),
          blockchainTransactionCreate = blockchainTransactionOrderTakes.Service.create,
          request = transactionsOrderTake.Request(transactionsOrderTake.Message(transactionsOrderTake.BaseReq(from = takeData.from, gas = takeData.gas), fromID = takeData.fromID, orderID = takeData.orderID, takerOwnableSplit = takeData.takerOwnableSplit)),
          action = transactionsOrderTake.Service.post,
          onSuccess = blockchainTransactionOrderTakes.Utility.onSuccess,
          onFailure = blockchainTransactionOrderTakes.Utility.onFailure,
          updateTransactionHash = blockchainTransactionOrderTakes.Service.updateTransactionHash
        )
        (for {
          ticketID <- ticketID
        } yield Ok(views.html.dashboard(successes = Seq(new Success(ticketID))))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def cancelForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.txForms.orderCancel())
  }

  def cancel: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.OrderCancel.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.txForms.orderCancel(formWithErrors)))
      },
      cancelData => {
        val ticketID = transaction.process[blockchainTransaction.OrderCancel, transactionsOrderCancel.Request](
          entity = blockchainTransaction.OrderCancel(from = cancelData.from, fromID = cancelData.fromID, orderID = cancelData.orderID, gas = cancelData.gas, ticketID = "", mode = transactionMode),
          blockchainTransactionCreate = blockchainTransactionOrderCancels.Service.create,
          request = transactionsOrderCancel.Request(transactionsOrderCancel.Message(transactionsOrderCancel.BaseReq(from = cancelData.from, gas = cancelData.gas), fromID = cancelData.fromID, orderID = cancelData.orderID)),
          action = transactionsOrderCancel.Service.post,
          onSuccess = blockchainTransactionOrderCancels.Utility.onSuccess,
          onFailure = blockchainTransactionOrderCancels.Utility.onFailure,
          updateTransactionHash = blockchainTransactionOrderCancels.Service.updateTransactionHash
        )
        (for {
          ticketID <- ticketID
        } yield Ok(views.html.dashboard(successes = Seq(new Success(ticketID))))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
        }
      }
    )
  }

}
