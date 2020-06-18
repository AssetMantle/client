package controllers

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import constants.Response.Failure
import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.ACLAccounts
import models.{blockchain, master}
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsError, JsPath, JsSuccess, JsValue, Json, OWrites, Reads}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import queries.GetAccount
import queries.responses.MemberCheckCorporateScanResponse.{ScanEntity, ScanInputParam, ScanResult}
import transactions.responses.MemberCheckCorporateScanResponse._
import queries.responses.MemberCheckCorporateScanResponse._
import transactions.Abstract.BaseResponse
import transactions.responses.TransactionResponse._
import utilities.JSON.convertJsonStringToObject

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random
import transactions.AddKey

@Singleton
class LoopBackController @Inject()(
                                    messagesControllerComponents: MessagesControllerComponents,
                                    transactionsAddKey: transactions.AddKey,
                                    transactionsSendCoin: transactions.SendCoin,
                                  )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_VIEW

  private val mnemonicSampleElements = Seq("crush", "spin", "banana", "cushion", "danger", "lunar", "earn", "unique", "problem", "crack", "coral", "mirror", "battle", "wreck", "abandon", "clarify", "push", "evil", "embody", "insane", "gravity", "gain", "table", "kangaroo", "slim", "regular", "index", "buddy", "dad", "recycle", "suspect", "pair", "cram", "fold", "seven", "host", "palm", "lawsuit", "rocket", "region", "habit", "produce", "blossom", "mosquito", "daring", "twin", "isolate", "surround", "drip", "health", "stem", "sure", "coast", "breeze", "smart", "husband", "soup", "memory", "drill", "giggle", "ritual", "mechanic", "march", "potato", "until", "short", "animal", "only", "prison", "token", "illness", "subway", "pudding", "balance", "useless", "aspect", "view", "vital", "bamboo", "have", "release", "recipe", "subject", "envelope", "avoid", "duck", "host", "category", "mystery", "chapter", "card", "model", "diet", "mail", "unaware", "mistake")
  /*

    private def getResponseFromJSValue[T <: BaseResponse](jsValue: JsValue,reads: Reads[T])={
      try{
      Json.fromJson[T](jsValue) match {
        case JsSuccess(value: T, _: JsPath) => value
        case _: JsError =>
          val errorResponse: ErrorResponse = Json.fromJson[ErrorResponse](jsValue) match {
            case JsSuccess(value: ErrorResponse, _: JsPath) => value
            case error: JsError => logger.info(jsValue)
              throw new BaseException(new Failure(error.toString, null))
          }
          logger.info(errorResponse.error)
          throw new BaseException(new Failure(errorResponse.error, null))
      }
      }catch {
        case jsonParseException: JsonParseException => logger.info(jsonParseException.getMessage, jsonParseException)
          throw new BaseException(constants.Response.JSON_PARSE_EXCEPTION)
        case jsonMappingException: JsonMappingException => logger.info(jsonMappingException.getMessage, jsonMappingException)
          throw new BaseException(constants.Response.NO_RESPONSE)
      }
    }
  */
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  def kafkaDisabledResponse={
    transactionMode match {
      case constants.Transactions.BLOCK_MODE => Ok(Json.toJson(BlockResponse(height = Random.nextInt(99999).toString, txhash = Random.alphanumeric.filter(c => c.isDigit || c.isUpper).take(64).mkString, gas_wanted = "999999", gas_used = "888888", code = None)))
      case constants.Transactions.SYNC_MODE => Ok(Json.toJson(SyncResponse(height = Random.nextInt(99999).toString, txhash = Random.alphanumeric.filter(c => c.isDigit || c.isUpper).take(64).mkString)))
      case constants.Transactions.ASYNC_MODE => Ok(Json.toJson(AsyncResponse(height = Random.nextInt(99999).toString, txhash = Random.alphanumeric.filter(c => c.isDigit || c.isUpper).take(64).mkString)))
    }
  }

  def memberCheckCorporateScan = Action {
    Ok(Json.toJson(transactions.responses.MemberCheckCorporateScanResponse.Response(Random.alphanumeric.filter(_.isDigit).take(4).mkString.toInt, Random.alphanumeric.take(10).mkString, Random.alphanumeric.filter(_.isDigit).take(4).mkString.toInt, None)).toString())
  }

  def memberCheckCorporateScanInfo(request: String) = Action {
    val scanParam = ScanInputParam(Random.alphanumeric.take(10).mkString, Random.alphanumeric.take(10).mkString, Random.alphanumeric.take(10).mkString, Random.alphanumeric.take(10).mkString, Random.alphanumeric.take(10).mkString, Random.alphanumeric.take(10).mkString, true)
    val scanResult = ScanResult(request.toInt, Random.alphanumeric.take(10).mkString, Random.alphanumeric.filter(_.isDigit).take(4).mkString.toInt, None)
    Ok(Json.toJson(queries.responses.MemberCheckCorporateScanResponse.Response(scanParam, scanResult)).toString())
  }

  def sendEmail = Action {
    Ok
  }

  def sendSMS = Action {
    Ok
  }

  def mnemonic = Action {
    Ok(Random.shuffle(mnemonicSampleElements).take(24).mkString(" "))
  }

  def addKey = Action { implicit request =>

    implicit val requestReads = transactionsAddKey.requestReads
    implicit val responseReads = transactionsAddKey.responseWrites

    val addKey = request.body.asJson.map { requestBody =>
      convertJsonStringToObject[transactionsAddKey.Request](requestBody.toString())
    }.getOrElse(throw new BaseException(constants.Response.FAILURE))
    Ok(Json.toJson(transactionsAddKey.Response(addKey.name, "commit1" + Random.alphanumeric.filter(c => c.isDigit || c.isLower).take(38).mkString, "commitpub1addwnpepq" + Random.alphanumeric.filter(c => c.isDigit || c.isLower).take(58).mkString, addKey.seed)))
  }

  def sendCoin = Action { implicit request =>
    if (kafkaEnabled) {
      Ok(Json.toJson(KafkaResponse(ticketID = "SECO" + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
    } else {
      kafkaDisabledResponse
    }
  }

  def addZone = Action {
    if (kafkaEnabled) {
      Ok(Json.toJson(KafkaResponse(ticketID = "DEZO" + Random.alphanumeric.filter(_.isDigit).take(18).mkString)))
    } else {
      kafkaDisabledResponse
    }
  }

  def getResponse=Action{
    kafkaDisabledResponse
  }

  def getTxHashResponse=Action{
    Ok(Json.toJson(BlockResponse(height = Random.nextInt(99999).toString, txhash = Random.alphanumeric.filter(c => c.isDigit || c.isUpper).take(64).mkString, gas_wanted = "999999", gas_used = "888888", code = None)))
  }

  def account(address: String) = Action {
    Ok
  }
}
