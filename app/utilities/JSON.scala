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
          logger.error(error)
          throw new BaseException(new Failure(response.json.toString()))
      }
    }.recover {
      case jsonParseException: JsonParseException => constants.Response.JSON_PARSE_EXCEPTION.throwBaseException(jsonParseException)
      case jsonMappingException: JsonMappingException => constants.Response.NO_RESPONSE.throwBaseException(jsonMappingException)
      case nullPointerException: NullPointerException => logger.error(nullPointerException.getMessage, nullPointerException)
        logger.error("Check order of case class definitions")
        constants.Response.NULL_POINTER_EXCEPTION.throwBaseException()
      case baseException: BaseException => throw baseException
      case exception: Exception => logger.error(exception.getLocalizedMessage)
        constants.Response.GENERIC_JSON_EXCEPTION.throwBaseException(exception)
    }
  }

  def convertJsonStringToObject[T](jsonString: String)(implicit module: String, logger: Logger, reads: Reads[T]): T = {
    try {
      Json.fromJson[T](Json.parse(jsonString)) match {
        case JsSuccess(value: T, _: JsPath) => value
        case errors: JsError =>
          logger.error(errors.errors.map(_.toString()).mkString(", "))
          logger.error(jsonString)
          constants.Response.JSON_PARSE_EXCEPTION.throwBaseException()
      }
    } catch {
      case jsonParseException: JsonParseException => logger.error(jsonParseException.getMessage, jsonParseException)
        constants.Response.JSON_PARSE_EXCEPTION.throwBaseException(jsonParseException)
      case jsonMappingException: JsonMappingException => logger.error(jsonMappingException.getMessage, jsonMappingException)
        constants.Response.NO_RESPONSE.throwBaseException(jsonMappingException)
      case nullPointerException: NullPointerException => logger.error(nullPointerException.getMessage, nullPointerException)
        logger.error("Check order of case class definitions")
        constants.Response.NULL_POINTER_EXCEPTION.throwBaseException()
      case baseException: BaseException => throw baseException
      case exception: Exception => logger.error(exception.getLocalizedMessage)
        constants.Response.GENERIC_JSON_EXCEPTION.throwBaseException(exception)
    }
  }
}