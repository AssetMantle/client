package utilities

import controllers.routes
import play.api.mvc.Call

object PublicResource {

  def getVersionedRoute(file: String): Call = routes.PublicResourceController.versioned(file, constants.AppConfig.AppVersion)

}
