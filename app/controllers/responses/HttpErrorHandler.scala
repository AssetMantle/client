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
                                  httpErrorHandler: DefaultHttpErrorHandler,
                                ) extends PreferredMediaTypeHttpErrorHandler(
  "application/json" -> jsonHttpErrorHandler,
  "application/x-www-form-urlencoded" -> httpErrorHandler,
  "multipart/form-data" -> httpErrorHandler,
  "text/plain" -> httpErrorHandler,
  "application/xml" -> xmlHttpRequestHandler,

)