package controllers

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class BlockExplorerController@Inject()(messagesControllerComponents: MessagesControllerComponents,
                                       queriesGetABCIINfo: queries.GetABCIInfo,
                                       queriesStakingValidators: queries.GetStakingValidators,
                                       queriesGetBlockDetails: queries.GetBlockDetails,
                                       queriesGetTransactionHash: queries.GetTransactionHashResponse
                                      )
                                      (implicit
                                       exec: ExecutionContext,
                                       configuration: Configuration
                                      ) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def lastBlockHeight(): Action[AnyContent] = Action { implicit request =>
    try {
      Ok(queriesGetABCIINfo.Service.get().result.response.last_block_height)
    } catch {
      case _: BaseException => InternalServerError
    }
  }

  def blockDetails(minimumHeight: Int, maximumHeight: Int): Action[AnyContent] = Action { implicit request =>
    try {
      Ok(Json.toJson(queriesGetBlockDetails.Service.get(minimumHeight = minimumHeight, maximumHeight = maximumHeight).result.block_metas).toString)
    } catch {
      case _: BaseException => InternalServerError
    }
  }

  def stakingValidators(): Action[AnyContent] = Action { implicit request =>
    try {
      Ok(Json.toJson(queriesStakingValidators.Service.get()))
    } catch {
      case _: BaseException => InternalServerError
    }
  }

  def transactionHash(txHash: String): Action[AnyContent] = Action { implicit request =>
    try {
      Ok(Json.toJson(queriesGetTransactionHash.Service.get(txHash).json))
    } catch {
      case _: BaseException => InternalServerError
    }
  }
}
