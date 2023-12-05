package queries.responses.blockchain.params

import models.Abstract.Parameter
import models.common.Parameters.SlashingParameter
import play.api.libs.json.{Json, Reads}


object SlashingResponse {

  case class Params(signed_blocks_window: String, min_signed_per_window: String, downtime_jail_duration: String, slash_fraction_double_sign: String, slash_fraction_downtime: String) {
    def toParameter: Parameter = SlashingParameter(signedBlocksWindow = signed_blocks_window.toInt, minSignedPerWindow = BigDecimal(min_signed_per_window), downtimeJailDuration = downtime_jail_duration.split("s")(0).toLong, slashFractionDoubleSign = BigDecimal(slash_fraction_double_sign), slashFractionDowntime = BigDecimal(slash_fraction_downtime))
  }

  implicit val paramsReads: Reads[Params] = Json.reads[Params]

  case class Response(params: Params)

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
