package controllers.responses

import javax.inject.{Singleton, _}
import play.api._
import play.api.http.DefaultHttpErrorHandler
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
    Future.successful(
      utilities.XMLRestResponse.REQUEST_NOT_WELL_FORMED.result
    )
  }

}
