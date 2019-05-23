package utilities

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import exceptions.BlockChainException
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.WSResponse

object JSON {

  def getResponseFromJson[T](response: WSResponse)(implicit logger: Logger, reads: Reads[T]): T = {
    try {
      val responseFromJson: JsResult[T] = Json.fromJson[T](response.json)
      responseFromJson match {
        case JsSuccess(value: T, _: JsPath) => value
        case errors: JsError => logger.info(errors.toString)
          throw new BlockChainException(response.body.toString)
      }
    }
    catch {
      case jsonParseException: JsonParseException => logger.info(response.toString, jsonParseException)
        throw new BlockChainException(response.body.toString)
      case jsonMappingException: JsonMappingException => logger.info(constants.Response.NO_RESPONSE, jsonMappingException)
        throw new BlockChainException(constants.Error.NO_RESPONSE)
    }
  }
}