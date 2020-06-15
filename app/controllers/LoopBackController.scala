package controllers

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.ACLAccounts
import models.{blockchain, master}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import queries.GetAccount
import queries.responses.MemberCheckCorporateScanResponse.{ScanEntity, ScanInputParam, ScanResult}
import transactions.responses.MemberCheckCorporateScanResponse._
import queries.responses.MemberCheckCorporateScanResponse._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random


@Singleton
class LoopBackController @Inject()(
                                    messagesControllerComponents: MessagesControllerComponents,
                                  )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_VIEW

  def memberCheckCorporateScan = Action {
    Ok(Json.toJson(transactions.responses.MemberCheckCorporateScanResponse.Response(Random.alphanumeric.filter(_.isDigit).take(4).mkString.toInt, Random.alphanumeric.take(10).mkString, Random.alphanumeric.filter(_.isDigit).take(4).mkString.toInt, None)).toString())
  }

  def memberCheckCorporateScanInfo(request:String) = Action {
    val scanParam = ScanInputParam(Random.alphanumeric.take(10).mkString, Random.alphanumeric.take(10).mkString, Random.alphanumeric.take(10).mkString, Random.alphanumeric.take(10).mkString, Random.alphanumeric.take(10).mkString, Random.alphanumeric.take(10).mkString, true)
    val scanResult = ScanResult(request.toInt, Random.alphanumeric.take(10).mkString, Random.alphanumeric.filter(_.isDigit).take(4).mkString.toInt, None)
    Ok(Json.toJson(queries.responses.MemberCheckCorporateScanResponse.Response(scanParam, scanResult)).toString())
  }

  def sendEmail=Action{
    Ok
  }

  def sendSMS=Action{
    Ok
  }

}
