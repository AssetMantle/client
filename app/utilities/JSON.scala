package utilities

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import constants.Response.Failure
import exceptions.BlockChainException
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.WSResponse

object JSON {

  def getResponseFromJson[T](response: WSResponse)(implicit logger: Logger, reads: Reads[T]): T = {
    try {
      logger.info(response.body)
      val responseFromJson: JsResult[T] = Json.fromJson[T](response.json)
      responseFromJson match {
        case JsSuccess(value: T, _: JsPath) => value
        case errors: JsError => logger.info(errors.toString)
          throw new BlockChainException(new Failure(response.body.toString, null))
//        case _: JsError =>
//          val errorResponseFromJson: JsResult[BlockModeErrorResponse] = Json.fromJson[BlockModeErrorResponse](response.json)
//          errorResponseFromJson match {
//            case JsSuccess(value: BlockModeErrorResponse, _) => throw new BlockChainException(new Failure(value.error, null))
//            case errors: JsError => logger.info(errors.toString)
//              throw new BlockChainException(new Failure(response.body.toString, null))
//          }
      }
    }
    catch {
      case jsonParseException: JsonParseException => logger.info(response.toString, jsonParseException)
        throw new BlockChainException(new Failure(response.body.toString, null))
      case jsonMappingException: JsonMappingException => logger.info(constants.Response.NO_RESPONSE.message, jsonMappingException)
        throw new BlockChainException(constants.Response.NO_RESPONSE)
    }
  }
}