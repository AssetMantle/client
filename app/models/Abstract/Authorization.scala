package models.Abstract

import com.google.protobuf.{Any => protoAny}
import models.common.Authz._
import models.common.Serializable.Coin
import utilities.Blockchain.Authz.ValidateResponse

import scala.jdk.CollectionConverters.CollectionHasAsScala

abstract class Authorization {
  def getMsgTypeURL: String

  def validate(stdMsg: protoAny): ValidateResponse

  def toProto: protoAny

}

object Authorization {
  def apply(authzAny: protoAny): Authorization = authzAny.getTypeUrl match {
    case constants.Blockchain.Authz.SEND_AUTHORIZATION => {
      val protoSend = cosmos.bank.v1beta1.Authz.SendAuthorization.parseFrom(authzAny.getValue)
      SendAuthorization(spendLimit = protoSend.getSpendLimitList.asScala.toSeq.map(x => Coin(x)))
    }
    case constants.Blockchain.Authz.GENERIC_AUTHORIZATION => {
      GenericAuthorization(cosmos.authz.v1beta1.Authz.GenericAuthorization.parseFrom(authzAny.getValue).getMsg)
    }
    case constants.Blockchain.Authz.STAKE_AUTHORIZATION => {
      val protoStakeAuthorization = cosmos.staking.v1beta1.Authz.StakeAuthorization.parseFrom(authzAny.getValue)
      StakeAuthorization(
        maxTokens = Coin(protoStakeAuthorization.getMaxTokens),
        allowList = StakeAuthorizationValidators(protoStakeAuthorization.getAllowList.getAddressList.asScala.toSeq),
        denyList = StakeAuthorizationValidators(protoStakeAuthorization.getDenyList.getAddressList.asScala.toSeq),
        authorizationType = protoStakeAuthorization.getAuthorizationType.toString)
    }
  }
}