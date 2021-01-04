package constants

import models.common.DataValue
import play.api.data.validation._
import views.companion.blockchain._
import views.companion.master.{ChangePassword, SignUp}

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

  val changePasswordConstraint: Constraint[ChangePassword.Data] = Constraint("constraints.changePassword")({ changePasswordData: ChangePassword.Data =>
    val errors = {
      if (changePasswordData.oldPassword == changePasswordData.newPassword) Seq(ValidationError(constants.Response.NEW_PASSWORD_SAME_AS_OLD_PASSWORD.message))
      else if (changePasswordData.newPassword != changePasswordData.confirmNewPassword) Seq(ValidationError(constants.Response.PASSWORDS_DO_NOT_MATCH.message))
      else Nil
    }
    if (errors.isEmpty) Valid else Invalid(errors)
  })

  val identityDefine: Constraint[IdentityDefine.Data] = Constraint("constraints.identityDefine")({ identityDefineData: IdentityDefine.Data =>
    val allTraits = identityDefineData.immutableMetaTraits.getOrElse(Seq.empty).flatten ++ identityDefineData.immutableTraits.getOrElse(Seq.empty).flatten ++ identityDefineData.mutableMetaTraits.getOrElse(Seq.empty).flatten ++ identityDefineData.mutableTraits.getOrElse(Seq.empty).flatten
    val errors = {
      if ((!identityDefineData.addImmutableMetaField && !identityDefineData.addImmutableField && !identityDefineData.addMutableMetaField && !identityDefineData.addMutableField) && identityDefineData.password.isEmpty) Seq(ValidationError(constants.Response.PASSWORD_NOT_GIVEN.message))
      else if (allTraits.length > constants.Blockchain.MaxTraits) Seq(ValidationError(constants.Response.MAXIMUM_NUMBER_OF_TRAITS_LIMIT_REACHED.message))
      else if (!allTraits.forall(x => DataValue.verifyDataType(dataType = x.dataType, dataValue = x.dataValue))) Seq(ValidationError(constants.Response.DATA_TYPE_AND_DATA_VALUE_MISMATCH.message))
      else if (allTraits.map(_.dataName).distinct.length != allTraits.map(_.dataName).length) Seq(ValidationError(constants.Response.DATA_NAME_REPEATED.message))
      else Nil
    }
    if (errors.isEmpty) Valid else Invalid(errors)
  })

  val identityIssue: Constraint[IdentityIssue.Data] = Constraint("constraints.identityIssue")({ identityIssueData: IdentityIssue.Data =>
//    val allProperties = identityIssueData.immutableMetaProperties.getOrElse(Seq.empty).flatten ++ identityIssueData.immutableProperties.getOrElse(Seq.empty).flatten ++ identityIssueData.mutableMetaProperties.getOrElse(Seq.empty).flatten ++ identityIssueData.mutableProperties.getOrElse(Seq.empty).flatten
    val errors = {
//      if ((!identityIssueData.addImmutableMetaField && !identityIssueData.addImmutableField && !identityIssueData.addMutableMetaField && !identityIssueData.addMutableField) && identityIssueData.password.isEmpty) Seq(ValidationError(constants.Response.PASSWORD_NOT_GIVEN.message))
//      else if (allProperties.length > constants.Blockchain.MaxTraits) Seq(ValidationError(constants.Response.MAXIMUM_NUMBER_OF_TRAITS_LIMIT_REACHED.message))
//      else if (!allProperties.forall(x => DataValue.verifyDataType(dataType = x.dataType, dataValue = x.dataValue))) Seq(ValidationError(constants.Response.DATA_TYPE_AND_DATA_VALUE_MISMATCH.message))
//      else if (allProperties.map(_.dataName).distinct.length != allProperties.map(_.dataName).length) Seq(ValidationError(constants.Response.DATA_NAME_REPEATED.message))
//      else Nil
      Nil
    }
    if (errors.isEmpty) Valid else Invalid(errors)
  })

  val maintainerDeputize: Constraint[MaintainerDeputize.Data] = Constraint("constraints.maintainerDeputize")({ maintainerDeputizeData: MaintainerDeputize.Data =>
    val errors = {
      if (!maintainerDeputizeData.addMaintainedTraits && maintainerDeputizeData.password.isEmpty) Seq(ValidationError(constants.Response.PASSWORD_NOT_GIVEN.message))
      else if (maintainerDeputizeData.maintainedTraits.getOrElse(Seq.empty).flatten.length > constants.Blockchain.MaxTraits) Seq(ValidationError(constants.Response.MAXIMUM_NUMBER_OF_TRAITS_LIMIT_REACHED.message))
      else Nil
    }
    if (errors.isEmpty) Valid else Invalid(errors)
  })

  val assetDefine: Constraint[AssetDefine.Data] = Constraint("constraints.identityDefine")({ assetDefineData: AssetDefine.Data =>
    val allTraits = assetDefineData.immutableMetaTraits.getOrElse(Seq.empty).flatten ++ assetDefineData.immutableTraits.getOrElse(Seq.empty).flatten ++ assetDefineData.mutableMetaTraits.getOrElse(Seq.empty).flatten ++ assetDefineData.mutableTraits.getOrElse(Seq.empty).flatten
    val errors = {
      if ((!assetDefineData.addImmutableMetaField && !assetDefineData.addImmutableField && !assetDefineData.addMutableMetaField && !assetDefineData.addMutableField) && assetDefineData.password.isEmpty) Seq(ValidationError(constants.Response.PASSWORD_NOT_GIVEN.message))
      else if (allTraits.length > constants.Blockchain.MaxTraits) Seq(ValidationError(constants.Response.MAXIMUM_NUMBER_OF_TRAITS_LIMIT_REACHED.message))
      else if (!allTraits.forall(x => DataValue.verifyDataType(dataType = x.dataType, dataValue = x.dataValue))) Seq(ValidationError(constants.Response.DATA_TYPE_AND_DATA_VALUE_MISMATCH.message))
      else if (allTraits.map(_.dataName).distinct.length != allTraits.map(_.dataName).length) Seq(ValidationError(constants.Response.DATA_NAME_REPEATED.message))
      else Nil
    }
    if (errors.isEmpty) Valid else Invalid(errors)
  })

  val assetMint: Constraint[AssetMint.Data] = Constraint("constraints.assetMint")({ assetMintData: AssetMint.Data =>
    val allProperties = assetMintData.immutableMetaProperties.getOrElse(Seq.empty).flatten ++ assetMintData.immutableProperties.getOrElse(Seq.empty).flatten ++ assetMintData.mutableMetaProperties.getOrElse(Seq.empty).flatten ++ assetMintData.mutableProperties.getOrElse(Seq.empty).flatten
    val errors = {
      if ((!assetMintData.addImmutableMetaField && !assetMintData.addImmutableField && !assetMintData.addMutableMetaField && !assetMintData.addMutableField) && assetMintData.password.isEmpty) Seq(ValidationError(constants.Response.PASSWORD_NOT_GIVEN.message))
      else if (allProperties.length > constants.Blockchain.MaxTraits) Seq(ValidationError(constants.Response.MAXIMUM_NUMBER_OF_TRAITS_LIMIT_REACHED.message))
      else if (!allProperties.forall(x => DataValue.verifyDataType(dataType = x.dataType, dataValue = x.dataValue))) Seq(ValidationError(constants.Response.DATA_TYPE_AND_DATA_VALUE_MISMATCH.message))
      else if (allProperties.map(_.dataName).distinct.length != allProperties.map(_.dataName).length) Seq(ValidationError(constants.Response.DATA_NAME_REPEATED.message))
      else Nil
    }
    if (errors.isEmpty) Valid else Invalid(errors)
  })

  val assetMutate: Constraint[AssetMutate.Data] = Constraint("constraints.assetMutate")({ assetMutateData: AssetMutate.Data =>
    val allProperties = assetMutateData.mutableMetaProperties.getOrElse(Seq.empty).flatten ++ assetMutateData.mutableProperties.getOrElse(Seq.empty).flatten
    val errors = {
      if ((!assetMutateData.addMutableMetaField && !assetMutateData.addMutableField) && assetMutateData.password.isEmpty) Seq(ValidationError(constants.Response.PASSWORD_NOT_GIVEN.message))
      else if (allProperties.length > constants.Blockchain.MaxTraits) Seq(ValidationError(constants.Response.MAXIMUM_NUMBER_OF_TRAITS_LIMIT_REACHED.message))
      else if (!allProperties.forall(x => DataValue.verifyDataType(dataType = x.dataType, dataValue = x.dataValue))) Seq(ValidationError(constants.Response.DATA_TYPE_AND_DATA_VALUE_MISMATCH.message))
      else if (allProperties.map(_.dataName).distinct.length != allProperties.map(_.dataName).length) Seq(ValidationError(constants.Response.DATA_NAME_REPEATED.message))
      else Nil
    }
    if (errors.isEmpty) Valid else Invalid(errors)
  })

  val orderDefine: Constraint[OrderDefine.Data] = Constraint("constraints.identityDefine")({ orderDefineData: OrderDefine.Data =>
    val allTraits = orderDefineData.immutableMetaTraits.getOrElse(Seq.empty).flatten ++ orderDefineData.immutableTraits.getOrElse(Seq.empty).flatten ++ orderDefineData.mutableMetaTraits.getOrElse(Seq.empty).flatten ++ orderDefineData.mutableTraits.getOrElse(Seq.empty).flatten
    val errors = {
      if ((!orderDefineData.addImmutableMetaField && !orderDefineData.addImmutableField && !orderDefineData.addMutableMetaField && !orderDefineData.addMutableField) && orderDefineData.password.isEmpty) Seq(ValidationError(constants.Response.PASSWORD_NOT_GIVEN.message))
      else if (allTraits.length > constants.Blockchain.MaxTraits) Seq(ValidationError(constants.Response.MAXIMUM_NUMBER_OF_TRAITS_LIMIT_REACHED.message))
      else if (!allTraits.forall(x => DataValue.verifyDataType(dataType = x.dataType, dataValue = x.dataValue))) Seq(ValidationError(constants.Response.DATA_TYPE_AND_DATA_VALUE_MISMATCH.message))
      else if (allTraits.map(_.dataName).distinct.length != allTraits.map(_.dataName).length) Seq(ValidationError(constants.Response.DATA_NAME_REPEATED.message))
      else Nil
    }
    if (errors.isEmpty) Valid else Invalid(errors)
  })

  val orderMake: Constraint[OrderMake.Data] = Constraint("constraints.orderMake")({ orderMakeData: OrderMake.Data =>
    val allProperties = orderMakeData.immutableMetaProperties.getOrElse(Seq.empty).flatten ++ orderMakeData.immutableProperties.getOrElse(Seq.empty).flatten ++ orderMakeData.mutableMetaProperties.getOrElse(Seq.empty).flatten ++ orderMakeData.mutableProperties.getOrElse(Seq.empty).flatten
    val errors = {
      if ((!orderMakeData.addImmutableMetaField && !orderMakeData.addImmutableField && !orderMakeData.addMutableMetaField && !orderMakeData.addMutableField) && orderMakeData.password.isEmpty) Seq(ValidationError(constants.Response.PASSWORD_NOT_GIVEN.message))
      else if (allProperties.length > constants.Blockchain.MaxTraits) Seq(ValidationError(constants.Response.MAXIMUM_NUMBER_OF_TRAITS_LIMIT_REACHED.message))
      else if (!allProperties.forall(x => DataValue.verifyDataType(dataType = x.dataType, dataValue = x.dataValue))) Seq(ValidationError(constants.Response.DATA_TYPE_AND_DATA_VALUE_MISMATCH.message))
      else if (allProperties.map(_.dataName).distinct.length != allProperties.map(_.dataName).length) Seq(ValidationError(constants.Response.DATA_NAME_REPEATED.message))
      else Nil
    }
    if (errors.isEmpty) Valid else Invalid(errors)
  })

}
