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
class AssetController @Inject()(
                                 messagesControllerComponents: MessagesControllerComponents,
                                 transaction: utilities.Transaction,
                                 withLoginAction: WithLoginAction,
                                 withUnknownLoginAction: WithUnknownLoginAction,
                                 transactionsAssetDefine: transactions.blockchain.AssetDefine,
                                 blockchainTransactionAssetDefines: blockchainTransaction.AssetDefines,
                                 transactionsAssetMint: transactions.blockchain.AssetMint,
                                 blockchainTransactionAssetMints: blockchainTransaction.AssetMints,
                                 transactionsAssetMutate: transactions.blockchain.AssetMutate,
                                 blockchainTransactionAssetMutates: blockchainTransaction.AssetMutates,
                                 transactionsAssetBurn: transactions.blockchain.AssetBurn,
                                 blockchainTransactionAssetBurns: blockchainTransaction.AssetBurns,
                                 withUserLoginAction: WithUserLoginAction,
                                 withUsernameToken: WithUsernameToken,
                                 withoutLoginAction: WithoutLoginAction,
                                 withoutLoginActionAsync: WithoutLoginActionAsync
                               )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ASSET

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def getNumberOfFields(addField: Boolean, currentNumber: Int) = if (addField) currentNumber + 1 else currentNumber

  def defineForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.txForms.assetDefine())
  }

  def define: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.AssetDefine.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.txForms.assetDefine(formWithErrors)))
      },
      defineData => {
        if (defineData.addImmutableMetaField || defineData.addImmutableField || defineData.addMutableMetaField || defineData.addMutableField) {
          Future(PartialContent(views.html.component.blockchain.txForms.assetDefine(
            assetDefineForm = views.companion.blockchain.AssetDefine.form.fill(defineData.copy(addImmutableMetaField = false, addImmutableField = false, addMutableMetaField = false, addMutableField = false)),
            numImmutableMetaForms = getNumberOfFields(defineData.addImmutableMetaField, defineData.immutableMetaTraits.fold(0)(_.flatten.length)),
            numImmutableForms = getNumberOfFields(defineData.addImmutableField, defineData.immutableTraits.fold(0)(_.flatten.length)),
            numMutableMetaForms = getNumberOfFields(defineData.addMutableMetaField, defineData.mutableMetaTraits.fold(0)(_.flatten.length)),
            numMutableForms = getNumberOfFields(defineData.addMutableField, defineData.mutableTraits.fold(0)(_.flatten.length)))))
        } else {
          val ticketID = transaction.process[blockchainTransaction.AssetDefine, transactionsAssetDefine.Request](
            entity = blockchainTransaction.AssetDefine(from = defineData.from, fromID = defineData.fromID, immutableMetaTraits = MetaProperties(defineData.immutableMetaTraits.fold[Seq[MetaProperty]](Seq.empty)(_.flatten.map(_.toMetaProperty))), immutableTraits = Properties(defineData.immutableTraits.fold[Seq[Property]](Seq.empty)(_.flatten.map(_.toProperty))), mutableMetaTraits = MetaProperties(defineData.mutableMetaTraits.fold[Seq[MetaProperty]](Seq.empty)(_.flatten.map(_.toMetaProperty))), mutableTraits = Properties(defineData.mutableTraits.fold[Seq[Property]](Seq.empty)(_.flatten.map(_.toProperty))), gas = defineData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionAssetDefines.Service.create,
            request = transactionsAssetDefine.Request(transactionsAssetDefine.Message(transactionsAssetDefine.BaseReq(from = defineData.from, gas = defineData.gas), fromID = defineData.fromID, immutableMetaTraits = defineData.immutableMetaTraits.getOrElse(Seq.empty).flatten, immutableTraits = defineData.immutableTraits.getOrElse(Seq.empty).flatten, mutableMetaTraits = defineData.mutableMetaTraits.getOrElse(Seq.empty).flatten, mutableTraits = defineData.mutableTraits.getOrElse(Seq.empty).flatten)),
            action = transactionsAssetDefine.Service.post,
            onSuccess = blockchainTransactionAssetDefines.Utility.onSuccess,
            onFailure = blockchainTransactionAssetDefines.Utility.onFailure,
            updateTransactionHash = blockchainTransactionAssetDefines.Service.updateTransactionHash
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

  def mintForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.txForms.assetMint())
  }

  def mint: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.AssetMint.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.txForms.assetMint(formWithErrors)))
      },
      mintData => {
        if (mintData.addImmutableMetaField || mintData.addImmutableField || mintData.addMutableMetaField || mintData.addMutableField) {
          Future(PartialContent(views.html.component.blockchain.txForms.assetMint(
            assetMintForm = views.companion.blockchain.AssetMint.form.fill(mintData.copy(addImmutableMetaField = false, addImmutableField = false, addMutableMetaField = false, addMutableField = false)),
            numImmutableMetaForms = getNumberOfFields(mintData.addImmutableMetaField, mintData.immutableMetaProperties.fold(0)(_.flatten.length)),
            numImmutableForms = getNumberOfFields(mintData.addImmutableField, mintData.immutableProperties.fold(0)(_.flatten.length)),
            numMutableMetaForms = getNumberOfFields(mintData.addMutableMetaField, mintData.mutableMetaProperties.fold(0)(_.flatten.length)),
            numMutableForms = getNumberOfFields(mintData.addMutableField, mintData.mutableProperties.fold(0)(_.flatten.length)))))
        } else {
          val ticketID = transaction.process[blockchainTransaction.AssetMint, transactionsAssetMint.Request](
            entity = blockchainTransaction.AssetMint(from = mintData.from, fromID = mintData.fromID, toID = mintData.toID, classificationID = mintData.classificationID, immutableMetaTraits = MetaProperties(mintData.immutableMetaProperties.fold[Seq[MetaProperty]](Seq.empty)(_.flatten.map(_.toMetaProperty))), immutableTraits = Properties(mintData.immutableProperties.fold[Seq[Property]](Seq.empty)(_.flatten.map(_.toProperty))), mutableMetaTraits = MetaProperties(mintData.mutableMetaProperties.fold[Seq[MetaProperty]](Seq.empty)(_.flatten.map(_.toMetaProperty))), mutableTraits = Properties(mintData.mutableProperties.fold[Seq[Property]](Seq.empty)(_.flatten.map(_.toProperty))), gas = mintData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionAssetMints.Service.create,
            request = transactionsAssetMint.Request(transactionsAssetMint.Message(transactionsAssetMint.BaseReq(from = mintData.from, gas = mintData.gas), fromID = mintData.fromID, toID = mintData.toID, classificationID = mintData.classificationID, immutableMetaProperties = mintData.immutableMetaProperties.getOrElse(Seq.empty).flatten, immutableProperties = mintData.immutableProperties.getOrElse(Seq.empty).flatten, mutableMetaProperties = mintData.mutableMetaProperties.getOrElse(Seq.empty).flatten, mutableProperties = mintData.mutableProperties.getOrElse(Seq.empty).flatten)),
            action = transactionsAssetMint.Service.post,
            onSuccess = blockchainTransactionAssetMints.Utility.onSuccess,
            onFailure = blockchainTransactionAssetMints.Utility.onFailure,
            updateTransactionHash = blockchainTransactionAssetMints.Service.updateTransactionHash
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

  def mutateForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.txForms.assetMutate())
  }

  def mutate: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.AssetMutate.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.txForms.assetMutate(formWithErrors)))
      },
      mutateData => {
        if (mutateData.addMutableMetaField || mutateData.addMutableField) {
          Future(PartialContent(views.html.component.blockchain.txForms.assetMutate(
            assetMutateForm = views.companion.blockchain.AssetMutate.form.fill(mutateData.copy(addMutableMetaField = false, addMutableField = false)),
            numMutableMetaForms = getNumberOfFields(mutateData.addMutableMetaField, mutateData.mutableMetaProperties.fold(0)(_.flatten.length)),
            numMutableForms = getNumberOfFields(mutateData.addMutableField, mutateData.mutableProperties.fold(0)(_.flatten.length)))))
        } else {
          val ticketID = transaction.process[blockchainTransaction.AssetMutate, transactionsAssetMutate.Request](
            entity = blockchainTransaction.AssetMutate(from = mutateData.from, fromID = mutateData.fromID, assetID = mutateData.assetID, mutableMetaTraits = MetaProperties(mutateData.mutableMetaProperties.fold[Seq[MetaProperty]](Seq.empty)(_.flatten.map(_.toMetaProperty))), mutableTraits = Properties(mutateData.mutableProperties.fold[Seq[Property]](Seq.empty)(_.flatten.map(_.toProperty))), gas = mutateData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionAssetMutates.Service.create,
            request = transactionsAssetMutate.Request(transactionsAssetMutate.Message(transactionsAssetMutate.BaseReq(from = mutateData.from, gas = mutateData.gas), fromID = mutateData.fromID, assetID = mutateData.assetID, mutableMetaProperties = mutateData.mutableMetaProperties.getOrElse(Seq.empty).flatten, mutableProperties = mutateData.mutableProperties.getOrElse(Seq.empty).flatten)),
            action = transactionsAssetMutate.Service.post,
            onSuccess = blockchainTransactionAssetMutates.Utility.onSuccess,
            onFailure = blockchainTransactionAssetMutates.Utility.onFailure,
            updateTransactionHash = blockchainTransactionAssetMutates.Service.updateTransactionHash
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

  def burnForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.txForms.assetBurn())
  }

  def burn: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.AssetBurn.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.txForms.assetBurn(formWithErrors)))
      },
      burnData => {
        val ticketID = transaction.process[blockchainTransaction.AssetBurn, transactionsAssetBurn.Request](
          entity = blockchainTransaction.AssetBurn(from = burnData.from, fromID = burnData.fromID, assetID = burnData.assetID, gas = burnData.gas, ticketID = "", mode = transactionMode),
          blockchainTransactionCreate = blockchainTransactionAssetBurns.Service.create,
          request = transactionsAssetBurn.Request(transactionsAssetBurn.Message(transactionsAssetBurn.BaseReq(from = burnData.from, gas = burnData.gas), fromID = burnData.fromID, assetID = burnData.assetID)),
          action = transactionsAssetBurn.Service.post,
          onSuccess = blockchainTransactionAssetBurns.Utility.onSuccess,
          onFailure = blockchainTransactionAssetBurns.Utility.onFailure,
          updateTransactionHash = blockchainTransactionAssetBurns.Service.updateTransactionHash
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
