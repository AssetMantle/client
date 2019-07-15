package utilities

import play.api.routing.JavaScriptReverseRoute

object String {
  def getJsRouteString(route: JavaScriptReverseRoute): String = if (route != null) {
    s"jsRoutes.${route.name}()"
  } else {
    "#"
  }
}
