package utilities

import play.api.routing.JavaScriptReverseRoute

object String {
  def getJsRouteString(route: Option[JavaScriptReverseRoute]): String = {
    route match {
      case Some(x) => constants.JavaScript.JS_ROUTE_TAKE1s.format(x.name)
      case None => "#"

    }
  }
}
