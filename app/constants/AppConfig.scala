package constants

import com.typesafe.config.ConfigFactory
import play.api.Configuration

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object AppConfig {

  case class TokenTicker(denom: String, normalizedDenom: String, ticker: String)

  case class OtherApp(url: String, name: String)

  case class IBCDenom(hash: String, name: String)

  val configuration: Configuration = Configuration(ConfigFactory.load())

  implicit val otherApps: Seq[OtherApp] = configuration.get[Seq[Configuration]]("webApp.otherApps").map { otherApp =>
    OtherApp(url = otherApp.get[String]("url"), name = otherApp.get[String]("name"))
  }

  implicit val tokenTickers: Seq[constants.AppConfig.TokenTicker] = configuration.get[Seq[Configuration]]("blockchain.token.tickers").map { tokenTicker =>
    constants.AppConfig.TokenTicker(denom = tokenTicker.get[String]("denom"), normalizedDenom = tokenTicker.get[String]("normalizedDenom"), ticker = tokenTicker.get[String]("ticker"))
  }

  val CacheDuration: FiniteDuration = configuration.get[Int]("webApp.cacheDuration").milliseconds

  val AppVersion: String = configuration.get[String]("webApp.version")

  object Telegram {
    val BotToken: String = configuration.get[String]("telegram.botToken")
    val ChatId: String = configuration.get[String]("telegram.chatId")
  }

  val ExplorerInitialDelay: FiniteDuration = configuration.get[Int]("blockchain.explorer.initialDelay").millis

  val ExplorerFixedDelay: FiniteDuration = configuration.get[Int]("blockchain.explorer.fixedDelay").millis


}
