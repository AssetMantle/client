package utilities

import scala.util.Random

object IDGenerator {

  def generateRequestID()(implicit module: String): String = module + Random.nextString(32 - module.length())

  def generateTicketID()(implicit module: String): String= module + Random.nextString(32 - module.length())


}