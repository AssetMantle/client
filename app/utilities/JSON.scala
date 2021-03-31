package utilities

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import constants.Response.Failure
import exceptions.BaseException
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.WSResponse
import transactions.Abstract.BaseResponse
import transactions.responses.blockchain.TransactionResponse.ErrorResponse

import scala.concurrent.{ExecutionContext, Future}

object JSON {

  def getResponseFromJson[T <: BaseResponse](response: Future[WSResponse])(implicit exec: ExecutionContext, logger: Logger, module: String, reads: Reads[T]): Future[T] = {
    response.map { response =>
      Json.fromJson[T](response.json) match {
        case JsSuccess(value: T, _: JsPath) => value
        case mainError: JsError => logger.error(mainError.toString)
          val errorResponse: ErrorResponse = Json.fromJson[ErrorResponse](response.json) match {
            case JsSuccess(value: ErrorResponse, _: JsPath) => value
            case error: JsError => logger.error(error.toString)
              logger.error(response.body)
              throw new BaseException(constants.Response.JSON_UNMARSHALLING_ERROR)
          }
          logger.error(errorResponse.error.getOrElse(errorResponse.message.getOrElse(constants.Response.JSON_PARSE_EXCEPTION.logMessage)))
          throw new BaseException(new Failure(errorResponse.error.getOrElse(errorResponse.message.getOrElse(constants.Response.JSON_PARSE_EXCEPTION.message)), null))
      }
    }.recover {
      case jsonParseException: JsonParseException => logger.error(jsonParseException.getMessage, jsonParseException)
        throw new BaseException(constants.Response.JSON_PARSE_EXCEPTION)
      case jsonMappingException: JsonMappingException => logger.error(jsonMappingException.getMessage, jsonMappingException)
        throw new BaseException(constants.Response.NO_RESPONSE)
    }
  }

  def convertJsonStringToObject[T](jsonString: String)(implicit module: String, logger: Logger, reads: Reads[T]): T = {
    try {
      Json.fromJson[T](Json.parse(jsonString)) match {
        case JsSuccess(value: T, _: JsPath) => value
        case errors: JsError => logger.error(errors.toString)
          logger.error(jsonString)
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