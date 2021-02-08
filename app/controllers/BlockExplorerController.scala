package controllers

import controllers.actions.{WithoutLoginAction, WithoutLoginActionAsync}
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class BlockExplorerController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                        queriesGetABCIInfo: queries.blockchain.GetABCIInfo,
                                        queriesStakingValidators: queries.GetStakingValidators,
                                        queriesGetBlockDetails: queries.GetBlockDetails,
                                        queriesGetTransactionHash: queries.GetTransactionHashResponse,
                                        withoutLoginAction: WithoutLoginAction,
                                        withoutLoginActionAsync: WithoutLoginActionAsync,
                                       )
                                       (implicit
                                        exec: ExecutionContext,
                                        configuration: Configuration
                                       ) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  def lastBlockHeight(): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
    val aBCIInfo = queriesGetABCIInfo.Service.get()
    (for {
      aBCIInfo <- aBCIInfo
    } yield Ok(aBCIInfo.result.response.last_block_height)
      ).recover {
      case _: BaseException => InternalServerError
    }
  }

  def blockDetails(minimumHeight: Int, maximumHeight: Int): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
    val blockDetails = queriesGetBlockDetails.Service.get(minimumHeight = minimumHeight, maximumHeight = maximumHeight)
    (for {
      blockDetails <- blockDetails
    } yield Ok(Json.toJson(blockDetails.result.block_metas).toString)
      ).recover {
      case _: BaseException => InternalServerError
    }
  }

  def stakingValidators(): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
    val stakingValidators = queriesStakingValidators.Service.get()
    (for {
      stakingValidators <- stakingValidators
    } yield Ok(Json.toJson(stakingValidators))
      ).recover {
      case _: BaseException => InternalServerError
    }
  }

  def transactionHash(txHash: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
    val transactionHash = queriesGetTransactionHash.Service.get(txHash)
    (for {
      transactionHash <- transactionHash
    } yield Ok(Json.toJson(transactionHash.json))
      ).recover {
      case _: BaseException => InternalServerError
    }
  }
}
