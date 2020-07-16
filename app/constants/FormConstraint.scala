package constants

import play.api.data.validation._
import views.companion.master.{IssueAsset, SignUp, PaymentTerms, ChangePassword, DocumentList, ForgotPassword}

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

  val forgotPasswordConstraint: Constraint[ForgotPassword.Data] = Constraint("constraints.forgotPassword")({ forgotPasswordData: ForgotPassword.Data =>
    val errors = {
      if (forgotPasswordData.newPassword != forgotPasswordData.confirmNewPassword) Seq(ValidationError(constants.Response.PASSWORDS_DO_NOT_MATCH.message))
      else Nil
    }
    if (errors.isEmpty) Valid else Invalid(errors)
  })

  val paymentTermsConstraint: Constraint[PaymentTerms.Data] = Constraint("constraints.paymentTerms")({ paymentTermsData: PaymentTerms.Data =>
    val errors = {
      if ((paymentTermsData.advancePercentage < 100.0 && paymentTermsData.credit.isEmpty) || (paymentTermsData.advancePercentage == 100.0 && paymentTermsData.credit.isDefined)) Seq(ValidationError(constants.Response.INVALID_PAYMENT_TERMS.message))
      else if (paymentTermsData.credit.isDefined && (paymentTermsData.credit.get.tenure.isDefined && paymentTermsData.credit.get.tentativeDate.isDefined)) Seq(ValidationError(constants.Response.TENURE_AND_TENTATIVE_DATE_BOTH_FOUND.message))
      else if (paymentTermsData.credit.isDefined &&  (paymentTermsData.credit.get.tenure.isDefined && paymentTermsData.credit.get.reference.isEmpty)) Seq(ValidationError(constants.Response.REFRENCE_REQUIRED_WITH_TENURE.message))
      else if (paymentTermsData.credit.isDefined &&  (paymentTermsData.credit.get.tentativeDate.isDefined && paymentTermsData.credit.get.reference.isDefined)) Seq(ValidationError(constants.Response.REFRENCE_NOT_REQUIRED.message))
      else Nil
    }
    if (errors.isEmpty) Valid else Invalid(errors)
  })

  val documentListConstraint: Constraint[DocumentList.Data] = Constraint("constraints.documentList")({ documentListData: DocumentList.Data =>
    val errors = {
      if (documentListData.documentListCompleted && documentListData.physicalDocumentsHandledVia.isEmpty) Seq(ValidationError(constants.Response.PHYSICAL_DOCUMENTS_HANDLED_VIA_REQUIRED.message))
      else Nil
    }
    if (errors.isEmpty) Valid else Invalid(errors)
  })

}
