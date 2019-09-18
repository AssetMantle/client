package utilities

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import constants.Response.Failure
import exceptions.BaseException
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.WSResponse
import transactions.Abstract.BaseResponse
import transactions.responses.TransactionResponse.ErrorResponse

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
object JSON {

  def getResponseFromJson[T <: BaseResponse](response: WSResponse)(implicit logger: Logger, module: String, reads: Reads[T]): T = {
    try {
      Json.fromJson[T](response.json) match {
        case JsSuccess(value: T, _: JsPath) => value
        case _: JsError =>
          val errorResponse: ErrorResponse = Json.fromJson[ErrorResponse](response.json) match {
            case JsSuccess(value: ErrorResponse, _: JsPath) => value
            case error: JsError => logger.info(response.body.toString)
              throw new BaseException(new Failure(error.toString, null))
          }
          logger.info(errorResponse.error)
          throw new BaseException(new Failure(errorResponse.error, null))
      }
    } catch {
      case jsonParseException: JsonParseException => logger.info(jsonParseException.getMessage, jsonParseException)
        throw new BaseException(constants.Response.JSON_PARSE_EXCEPTION)
      case jsonMappingException: JsonMappingException => logger.info(jsonMappingException.getMessage, jsonMappingException)
        throw new BaseException(constants.Response.NO_RESPONSE)
    }
  }


  def getResponseFromJsonAsync[T <: BaseResponse](response: Future[WSResponse])(implicit exec:ExecutionContext,logger: Logger, module: String, reads: Reads[T]): Future[T]={
    response.map{ res=>
      Json.fromJson[T](res.json) match{
        case JsSuccess(value: T, _: JsPath) => value
        case _: JsError =>
          val errorResponse: ErrorResponse = Json.fromJson[ErrorResponse](res.json) match {
            case JsSuccess(value: ErrorResponse, _: JsPath) => value
            case error: JsError => logger.info(res.body.toString)
              throw new BaseException(new Failure(error.toString, null))
          }
          logger.info(errorResponse.error)
          throw new BaseException(new Failure(errorResponse.error, null))
      }

    }

  }


def convertJsonStringToObject[T](jsonString: String)(implicit module: String, logger: Logger, reads: Reads[T]): T = {
  try {
  Json.fromJson[T](Json.parse(jsonString)) match {
  case JsSuccess(value: T, _: JsPath) => value
  case errors: JsError => logger.error(errors.toString)
  throw new BaseException(constants.Response.JSON_PARSE_EXCEPTION)
}
}
  catch {
  case jsonParseException: JsonParseException => logger.error(jsonParseException.getMessage, jsonParseException)
  throw new BaseException(constants.Response.JSON_PARSE_EXCEPTION)
  case jsonMappingException: JsonMappingException => logger.error(jsonMappingException.getMessage, jsonMappingException)
  throw new BaseException(constants.Response.JSON_MAPPING_EXCEPTION)
}
}
}