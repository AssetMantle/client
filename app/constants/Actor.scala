package constants

import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.{Duration, DurationInt, FiniteDuration, SECONDS}

object Actor {
  val CONFIG = ConfigFactory.load("clustering/clustering.conf")

  val ACTOR_NOT_FOUND = "ACTOR_NOT_FOUND"
  val ACTOR_NOT_FOUND_FOR_SHUTDOWN = "ACTOR_NOT_FOUND_FOR_SHUTDOWN"

  val ACTOR_BLOCK = "ACTOR_BLOCK"
  val ACTOR_EMAIL = "ACTOR_EMAIL"
  val ACTOR_SMS = "ACTOR_SMS"
  val ACTOR_APP_WEB_SOCKET = "ACTOR_APP_WEB_SOCKET"
  val ACTOR_PUSH_NOTIFICATION = "ACTOR_PUSH_NOTIFICATION"

  val ACTOR_ASK_TIMEOUT = FiniteDuration(Duration(CONFIG.getString("custom-shard.timeout")).toSeconds, SECONDS)
  val NUMBER_OF_ENTITIES = CONFIG.getString("custom-shard.entities").toInt
  val NUMBER_OF_SHARDS = CONFIG.getString("custom-shard.shards").toInt

  object MessageType {
    val NEW_BLOCK = "NEW_BLOCK"
    val BLOCKCHAIN_CONNECTION_LOST = "BLOCKCHAIN_CONNECTION_LOST"
    val PRIVATE_MESSAGE = "PRIVATE_MESSAGE"
    val PING = "PING"
    val CHAT = "CHAT"
    val ASSET = "ASSET"
    val TEST = "TEST"
  }

}
