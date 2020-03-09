package utilities

import play.api.routing.JavaScriptReverseRoute
import java.security.MessageDigest
import java.math.BigInteger
import java.net.URLEncoder


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

  def sha256Sum(text: String) : String = java.lang.String.format("%064x", new BigInteger(1, MessageDigest.getInstance("SHA-256").digest(text.getBytes("UTF-8"))))

  def queryURLGenerator(baseURL: String, parameters: Map[String, Seq[String]]): String = {
    baseURL + Option(parameters)
      .filterNot(_.isEmpty)
      .map { params =>
        (if (baseURL.contains("?")) "&" else "?") + params.toSeq
          .flatMap { pair =>
            pair._2.map(value => pair._1 + "=" + URLEncoder.encode(value.toString, "utf-8"))
          }
          .mkString("&")
      }
      .getOrElse("")
  }
}
