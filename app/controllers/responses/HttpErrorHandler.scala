package controllers.responses

import javax.inject.{Singleton, _}
import play.api.http._

@Singleton
class HttpErrorHandler @Inject()(
                                  xmlHttpRequestHandler: XmlHttpRequestHandler,
                                  jsonHttpErrorHandler: JsonHttpErrorHandler,
                                ) extends PreferredMediaTypeHttpErrorHandler(
  "application/xml" -> xmlHttpRequestHandler,
  "application/json" -> jsonHttpErrorHandler,
)