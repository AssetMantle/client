package utilities

import play.api.routing.JavaScriptReverseRoute

object String {
  def getJsRouteString(route: JavaScriptReverseRoute): String = if (route != null) {
    constants.JavaScript.JS_ROUTE_TAKE1s.format(route.name)
  } else {
    "#"
  }
}
