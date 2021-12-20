package utilities

object Configuration {
  case class TokenTicker(denom: String, normalizedDenom: String, ticker: String)

  case class OtherApp(url: String, name: String)

  case class IBCRealName(denomHash: String, denomName: String)

}
