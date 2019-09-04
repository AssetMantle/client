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

object JSON {

  def getResponseFromJson[T <: BaseResponse](response: WSResponse)(implicit logger: Logger, reads: Reads[T]): T = {
    try {
      val responseFromJson: JsResult[T] = Json.fromJson[T](response.json)
      responseFromJson match {
        case JsSuccess(value: T, _: JsPath) => value
        case errors: JsError => logger.info(errors.toString)
          throw new BlockChainException(new Failure(response.body.toString, null))
      }
    }
    catch {
      case jsonParseException: JsonParseException => logger.info(jsonParseException.getMessage, jsonParseException)
        throw new BlockChainException(new Failure(jsonParseException.getMessage, null))
      case jsonMappingException: JsonMappingException => logger.info(jsonMappingException.getMessage, jsonMappingException)
        throw new BlockChainException(constants.Response.NO_RESPONSE)
    }
  }
  
  def getInstance[T <: BaseCaseClass](jsonString: String)(implicit module: String, logger: Logger, reads: Reads[T]): T = {
    try {
      val responseFromJson: JsResult[T] = Json.fromJson[T](Json.parse(jsonString))
      responseFromJson match {
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