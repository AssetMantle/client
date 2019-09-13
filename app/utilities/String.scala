package utilities

import play.api.routing.JavaScriptReverseRoute

object String {

  def getJsRouteString(route: JavaScriptReverseRoute, parameters: String*): String = if (route != null) {
    s"jsRoutes.${route.name}(${parameters.mkString(",")})"
  } else {
    "#"
  }

  def getJsRouteFunction(route: JavaScriptReverseRoute): String = if (route != null) {
    s"jsRoutes.${route.name}"
  } else {
    "#"
  }

  def nestedFormField(fieldName: String)(implicit prefix: String): String = Seq(prefix, fieldName).mkString(".")

}
