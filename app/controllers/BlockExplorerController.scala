package controllers

import exceptions.BlockChainException
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class BlockExplorerController@Inject()(messagesControllerComponents: MessagesControllerComponents, queriesGetABCIINfo: queries.GetABCIInfo, queriesStakingValidators: queries.GetStakingValidators, queriesGetBlockDetails: queries.GetBlockDetails)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def lastBlockHeight(): Action[AnyContent] = Action { implicit request =>
    try {
      Ok(queriesGetABCIINfo.Service.get().result.response.last_block_height)
    } catch {
      case _: BlockChainException => InternalServerError
    }
  }

  def blockDetails(minimumHeight: Int, maximumHeight: Int): Action[AnyContent] = Action { implicit request =>
    try {
      Ok(Json.toJson(queriesGetBlockDetails.Service.get(minimumHeight = minimumHeight, maximumHeight = maximumHeight).result.block_metas).toString)
    } catch {
      case _: BlockChainException => InternalServerError
    }
  }

  def stakingValidators(): Action[AnyContent] = Action { implicit request =>
    try {
      Ok(Json.toJson(queriesStakingValidators.Service.get()))
    } catch {
      case _: BlockChainException => InternalServerError
    }
  }
}
