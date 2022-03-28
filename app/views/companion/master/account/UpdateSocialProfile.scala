package views.companion.master.account

import models.common.Serializable.SocialProfile
import play.api.data.Form
import play.api.data.Forms.mapping

object UpdateSocialProfile {

  val form: Form[Data] = Form(
    mapping(
      constants.FormField.PLATFORM.name -> constants.FormField.PLATFORM.field,
      constants.FormField.USERNAME.name -> constants.FormField.USERNAME.field,
      constants.FormField.URL.name -> constants.FormField.URL.field,
    )(Data.apply)(Data.unapply).verifying(constants.FormConstraint.updateSocialProfileConstraint))

  case class Data(platform: String, username: String, url: String) {
    def toSocialProfile: SocialProfile = SocialProfile(platform = platform, username = username, url = url)
  }

}
