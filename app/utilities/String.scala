package utilities

import play.api.routing.JavaScriptReverseRoute
import java.security.MessageDigest
import java.math.BigInteger


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

  def sha256Hash(text: String) : String = java.lang.String.format("%064x", new BigInteger(1, MessageDigest.getInstance("SHA-256").digest(text.getBytes("UTF-8"))))
}
