package utilities

import com.fasterxml.jackson.core.JsonParseException
import exceptions.BlockChainException
import play.api.Logger
import play.api.libs.ws.WSResponse

object JSON {
  def getJSONString(key: String)(implicit response: WSResponse, logger: Logger): String = {
    try {
      response.json(key).as[String]
    } catch {
      case jsonParseException: JsonParseException => logger.error(response.body.toString, jsonParseException)
        throw new BlockChainException(response.body.toString)
    }
  }
}