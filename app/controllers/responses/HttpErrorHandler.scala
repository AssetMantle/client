package controllers.responses

import javax.inject.{Singleton, _}
import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc.Results._
import play.api.mvc._
import play.api.routing.Router

import scala.concurrent._

import javax.inject._
import play.api.http._

@Singleton
class HttpErrorHandler @Inject()(
                                    xmlHttpRequestHandler: XmlHttpRequestHandler,
                                    jsonHttpErrorHandler: JsonHttpErrorHandler,
                                  ) extends PreferredMediaTypeHttpErrorHandler(
  "application/xml" -> xmlHttpRequestHandler,
  "application/json"        -> jsonHttpErrorHandler,
)