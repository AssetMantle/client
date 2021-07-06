package transactions.wallex

import play.api.libs.json.{Json, OWrites, Reads}
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class Notification @Inject()(
)(implicit
    configuration: Configuration,
    executionContext: ExecutionContext
) {

  private implicit val module: String =
    constants.Module.TRANSACTIONS_WALLEX_NOTIFICATION

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]
  implicit val requestReads: Reads[Request] = Json.reads[Request]
  case class Request(resourceId: String,resource: String,status: String) extends BaseRequest

}
