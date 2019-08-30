package constants

import play.api.data.validation._
import views.companion.master.SignUp

object FormConstraint {
  //TODO: Error Response through Messages
  val signUpCheckConstraint: Constraint[SignUp.Data] = Constraint("constraints.signUpCheck")({ signUp: SignUp.Data =>
    val errors = {
      if (signUp.password != signUp.confirmPassword) Seq(ValidationError(constants.Response.PASSWORDS_DO_NOT_MATCH.message))
      else if (!signUp.usernameAvailable) Seq(ValidationError(constants.Response.USERNAME_UNAVAILABLE.message))
      else Nil
    }
    if (errors.isEmpty) Valid else Invalid(errors)
  })



}
