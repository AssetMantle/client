package controllers.responses

import constants.Response
import javax.inject.{Singleton, _}
import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc.Results._
import play.api.mvc._
import play.api.routing.Router

import scala.concurrent._

@Singleton
class XmlHttpRequestHandler @Inject()(
                                       environment: Environment,
                                       configuration: Configuration,
                                       sourceMapper: OptionalSourceMapper,
                                       router: Provider[Router]
                                     ) extends DefaultHttpErrorHandler(environment, configuration, sourceMapper, router) {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    println(Response.REQUEST_NOT_WELL_FORMED)
    Future.successful(
            Response.REQUEST_NOT_WELL_FORMED.result
    )
  }

}
