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

  private def logJsonError(jsonString: String, jsErrors: JsError*)(implicit module: String, logger: Logger): Unit = {
    jsErrors.foreach(jsError => logger.error(s"JSON_PARSE_ERROR: ${jsError.errors.zipWithIndex.map { case (x, index) => s"[${index}] ${x._1}: ${x._2.map(_.message).mkString(",")}" }.mkString("; ")}"))
    logger.error(jsonString)
  }

  def getResponseFromJson[T <: BaseResponse](response: Future[WSResponse])(implicit exec: ExecutionContext, logger: Logger, module: String, reads: Reads[T]): Future[T] = {
    response.map { response =>
      Json.fromJson[T](response.json) match {
        case JsSuccess(value: T, _: JsPath) => value
        case jsError: JsError => logJsonError(response.json.toString(), jsError)
          new Failure(response.json.toString()).throwBaseException()
      }
    }.recover {
      case jsonParseException: JsonParseException => constants.Response.JSON_PARSE_EXCEPTION.throwBaseException(jsonParseException)
      case jsonMappingException: JsonMappingException => constants.Response.NO_RESPONSE.throwBaseException(jsonMappingException)
      case nullPointerException: NullPointerException => logger.error(nullPointerException.getMessage, nullPointerException)
        logger.error("Check order of case class definitions")
        constants.Response.NULL_POINTER_EXCEPTION.throwBaseException()
      // Here we need to throw baseException before any general exception
      case baseException: BaseException => throw baseException
      case exception: Exception => logger.error(exception.getLocalizedMessage)
        constants.Response.GENERIC_JSON_EXCEPTION.throwBaseException(exception)
    }
  }

  def convertJsonStringToObject[T](jsonString: String)(implicit module: String, logger: Logger, reads: Reads[T]): T = {
    try {
      Json.fromJson[T](Json.parse(jsonString)) match {
        case JsSuccess(value: T, _: JsPath) => value
        case jsError: JsError => logJsonError(jsonString, jsError)
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
      // Here we need to throw baseException before any general exception
      case baseException: BaseException => throw baseException
      case exception: Exception => logger.error(exception.getLocalizedMessage)
        constants.Response.GENERIC_JSON_EXCEPTION.throwBaseException(exception)
    }
  }

  def getSuccessErrorResponseFromJson[Success, Error](wsResponse: Future[WSResponse])(implicit exec: ExecutionContext, logger: Logger, module: String, reads1: Reads[Success], reads2: Reads[Error]): Future[(Option[Success], Option[Error])] = {
    wsResponse.map { response =>
      Json.fromJson[Success](response.json) match {
        case JsSuccess(value: Success, _: JsPath) => (Option(value), None)
        case successJsError: JsError => {
          Json.fromJson[Error](response.json) match {
            case JsSuccess(value: Error, _: JsPath) => (None, Option(value))
            case errorJsError: JsError => logJsonError(response.json.toString(), successJsError, errorJsError)
              constants.Response.JSON_PARSE_EXCEPTION.throwBaseException()
          }
        }
      }
    }.recover {
      case jsonParseException: JsonParseException => logger.error(jsonParseException.getMessage, jsonParseException)
        constants.Response.JSON_PARSE_EXCEPTION.throwBaseException(jsonParseException)
      case jsonMappingException: JsonMappingException => logger.error(jsonMappingException.getMessage, jsonMappingException)
        constants.Response.JSON_MAPPING_EXCEPTION.throwBaseException(jsonMappingException)
      case nullPointerException: NullPointerException => logger.error(nullPointerException.getMessage, nullPointerException)
        logger.error("Check order of case class definitions")
        constants.Response.NULL_POINTER_EXCEPTION.throwBaseException(nullPointerException)
      case exception: Exception => logger.error(exception.getLocalizedMessage)
        constants.Response.GENERIC_JSON_EXCEPTION.throwBaseException(exception)
    }
  }
}