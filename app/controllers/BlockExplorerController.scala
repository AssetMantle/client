package controllers

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class BlockExplorerController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                        queriesGetABCIInfo: queries.GetABCIInfo,
                                        queriesStakingValidators: queries.GetStakingValidators,
                                        queriesGetBlockDetails: queries.GetBlockDetails,
                                        queriesGetTransactionHash: queries.GetTransactionHashResponse
                                       )
                                       (implicit
                                        exec: ExecutionContext,
                                        configuration: Configuration
                                       ) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def lastBlockHeight(): Action[AnyContent] = Action.async { implicit request =>
    val aBCIInfo = queriesGetABCIInfo.Service.get()
    (for {
      aBCIInfo <- aBCIInfo
    } yield Ok(aBCIInfo.result.response.last_block_height)
      ).recover {
      case _: BaseException => InternalServerError
    }
  }

  def blockDetails(minimumHeight: Int, maximumHeight: Int): Action[AnyContent] = Action.async { implicit request =>
    val blockDetails = queriesGetBlockDetails.Service.get(minimumHeight = minimumHeight, maximumHeight = maximumHeight)
    (for {
      blockDetails <- blockDetails
    } yield Ok(Json.toJson(blockDetails.result.block_metas).toString)
      ).recover {
      case _: BaseException => InternalServerError
    }
  }

  def stakingValidators(): Action[AnyContent] = Action.async { implicit request =>
    val stakingValidators = queriesStakingValidators.Service.get()
    (for {
      stakingValidators <- stakingValidators
    } yield Ok(Json.toJson(stakingValidators))
      ).recover {
      case _: BaseException => InternalServerError
    }
  }

  def transactionHash(txHash: String): Action[AnyContent] = Action.async { implicit request =>
    val transactionHash = queriesGetTransactionHash.Service.get(txHash)
    (for {
      transactionHash <- transactionHash
    } yield Ok(Json.toJson(transactionHash.json))
      ).recover {
      case _: BaseException => InternalServerError
    }
  }
}
