package utilities

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import constants.Response.Failure
import exceptions.{BaseException, BlockChainException}
import models.Abstract.BaseCaseClass
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.WSResponse
import transactions.Abstract.BaseResponse
import transactions.responses.TransactionResponse.ErrorResponse

object JSON {

  def getResponseFromJson[T <: BaseResponse](response: WSResponse)(implicit logger: Logger, reads: Reads[T]): T = {
    try {
      Json.fromJson[T](response.json) match {
        case JsSuccess(value: T, _: JsPath) => value
        case _: JsError =>
          val errorResponse: ErrorResponse = Json.fromJson[ErrorResponse](response.json) match {
            case JsSuccess(value: ErrorResponse, _: JsPath) => value
            case error: JsError => logger.info(response.body.toString)
              throw new BlockChainException(new Failure(error.toString, null))
          }
          logger.info(errorResponse.error)
          throw new BlockChainException(new Failure(errorResponse.error, null))
      }
    } catch {
      case jsonParseException: JsonParseException => logger.info(jsonParseException.getMessage, jsonParseException)
        throw new BlockChainException(new Failure(jsonParseException.getMessage, null))
      case jsonMappingException: JsonMappingException => logger.info(jsonMappingException.getMessage, jsonMappingException)
        throw new BlockChainException(constants.Response.NO_RESPONSE)
      case blockChainException: BlockChainException => logger.info(blockChainException.failure.message, blockChainException)
        throw new BlockChainException(blockChainException.failure)
    }
  }

  def getInstance[T <: BaseCaseClass](jsonString: String)(implicit module: String, logger: Logger, reads: Reads[T]): T = {
    try {
      Json.fromJson[T](Json.parse(jsonString)) match {
        case JsSuccess(value: T, _: JsPath) => value
        case errors: JsError => logger.info(errors.toString)
          throw new BaseException(new Failure(jsonString, null))
      }
    }
    catch {
      case jsonParseException: JsonParseException => logger.info(jsonParseException.getMessage, jsonParseException)
        throw new BaseException(new Failure(jsonParseException.getMessage, null))
      case jsonMappingException: JsonMappingException => logger.info(jsonMappingException.getMessage, jsonMappingException)
        throw new BaseException(new Failure(jsonMappingException.getMessage, null))
    }
  }
}