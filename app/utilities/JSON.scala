package utilities

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import constants.Response.Failure
import exceptions.BaseException
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.WSResponse
import transactions.Abstract.BaseResponse

import scala.concurrent.{ExecutionContext, Future}

object JSON {

  def getResponseFromJson[T <: BaseResponse](response: Future[WSResponse])(implicit exec: ExecutionContext, logger: Logger, module: String, reads: Reads[T]): Future[T] = {
    response.map { response =>
      Json.fromJson[T](response.json) match {
        case JsSuccess(value: T, _: JsPath) => value
        case jsError: JsError =>
          val error = s"JSON_PARSE_ERROR: ${jsError.errors.zipWithIndex.map { case (x, index) => s"[${index}] ${x._1}: ${x._2.map(_.message).mkString(",")}" }.mkString("; ")}"
          logger.error(response.json.toString())
          throw new BaseException(new Failure(error))
      }
    }.recover {
      case jsonParseException: JsonParseException => throw new BaseException(constants.Response.JSON_PARSE_EXCEPTION, jsonParseException)
      case jsonMappingException: JsonMappingException => throw new BaseException(constants.Response.NO_RESPONSE, jsonMappingException)
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