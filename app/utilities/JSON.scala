package utilities

import com.fasterxml.jackson.core.JsonParseException
import exceptions.BlockChainException
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.WSResponse

object JSON {
  def getBCStringResponse(key: String)(implicit response: WSResponse, logger: Logger): String = {
    try {
      response.json(key).as[String]
    } catch {
      case noSuchElementException: NoSuchElementException => logger.info(response.body.toString, noSuchElementException)
        try {
          response.json(constants.JSON.TICKET_ID).as[String]
        }
        catch {
          case noSuchElementException: NoSuchElementException => logger.info(response.body.toString, noSuchElementException)
            throw new BlockChainException(response.body.toString)
          case jsonParseException: JsonParseException => logger.error(response.body.toString, jsonParseException)
            throw new BlockChainException(response.body.toString)
        }
      case jsonParseException: JsonParseException => logger.error(response.body.toString, jsonParseException)
        throw new BlockChainException(response.body.toString)

    }
  }

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
    }
  }
}