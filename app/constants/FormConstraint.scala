package constants

import play.api.data.validation._
import views.companion.master.{IssueAsset, SignUp, DocumentList, ChangePassword}

object FormConstraint {
  //TODO: Error Response through Messages
  val signUpConstraint: Constraint[SignUp.Data] = Constraint("constraints.signUp")({ signUpData: SignUp.Data =>
    val errors = {
      if (signUpData.password != signUpData.confirmPassword) Seq(ValidationError(constants.Response.PASSWORDS_DO_NOT_MATCH.message))
      else if (!signUpData.usernameAvailable) Seq(ValidationError(constants.Response.USERNAME_UNAVAILABLE.message))
      else Nil
    }
    if (errors.isEmpty) Valid else Invalid(errors)
  })

  val issueAssetConstraint: Constraint[IssueAsset.Data] = Constraint("constraints.issueAsset")({ issueAssetData: IssueAsset.Data =>
    val errors = {
      if (!issueAssetData.moderated && issueAssetData.password.isEmpty && issueAssetData.gas.isDefined) Seq(ValidationError(constants.Response.PASSWORD_NOT_GIVEN.message))
      else if (!issueAssetData.moderated && issueAssetData.password.isDefined && issueAssetData.gas.isEmpty) Seq(ValidationError(constants.Response.GAS_NOT_GIVEN.message))
      else Nil
    }
    if (errors.isEmpty) Valid else Invalid(errors)
  })

  val changePasswordConstraint: Constraint[ChangePassword.Data] = Constraint("constraints.changePassword")({ changePasswordData: ChangePassword.Data =>
    val errors = {
      if (changePasswordData.oldPassword == changePasswordData.newPassword) Seq(ValidationError(constants.Response.NEW_PASSWORD_SAME_AS_OLD_PASSWORD.message))
      else if (changePasswordData.newPassword != changePasswordData.confirmNewPassword) Seq(ValidationError(constants.Response.PASSWORDS_DO_NOT_MATCH.message))
      else Nil
    }
    if (errors.isEmpty) Valid else Invalid(errors)
  })
}
