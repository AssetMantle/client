package constants

import play.api.data.validation._
import views.companion.master.SignUp
import views.companion.master.ConfirmIssueAssetTransaction

object FormConstraint {
  
  //TODO: Error Response through Messages
  val signUpCheckConstraint: Constraint[SignUp.Data] = Constraint("constraints.signUpCheck")({ signUpData: SignUp.Data =>
    val errors = {
      if (signUpData.password != signUpData.confirmPassword) Seq(ValidationError(constants.Response.PASSWORDS_DO_NOT_MATCH.message))
      else if (!signUpData.usernameAvailable) Seq(ValidationError(constants.Response.USERNAME_UNAVAILABLE.message))
      else Nil
    }
    if (errors.isEmpty) Valid else Invalid(errors)
  })

  val issueAssetRequestCheckConstraint: Constraint[ConfirmIssueAssetTransaction.Data] = Constraint("constraints.issueAssetRequestCheck")({ confirmTransactionData: ConfirmIssueAssetTransaction.Data =>
    val errors = {
      if (confirmTransactionData.password.isEmpty && confirmTransactionData.gas.isDefined) Seq(ValidationError(constants.Response.PASSWORD_NOT_GIVEN.message))
      else if (confirmTransactionData.password.isDefined && confirmTransactionData.gas.isEmpty) Seq(ValidationError(constants.Response.GAS_NOT_GIVEN.message))
      else Nil
    }
    if (errors.isEmpty) Valid else Invalid(errors)
  })
}
