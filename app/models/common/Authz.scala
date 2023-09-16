package models.common

import com.cosmos.staking.v1beta1.AuthorizationType
import com.cosmos.staking.{v1beta1 => stakingTx}
import com.google.protobuf.{Any => protoAny}
import models.Abstract.Authorization
import models.common.Serializable.Coin
import utilities.Blockchain.Authz.ValidateResponse
import utilities.MicroNumber

import scala.jdk.CollectionConverters.{CollectionHasAsScala, IterableHasAsJava}

object Authz {

  //bank
  case class SendAuthorization(spendLimit: Seq[Coin]) extends Authorization {

    def getMsgTypeURL: String = schema.constants.Messages.SEND_COIN

    def validate(stdMsg: protoAny): ValidateResponse = {
      val (limitLeft, _) = utilities.Blockchain.subtractCoins(spendLimit, com.cosmos.bank.v1beta1.MsgSend.parseFrom(stdMsg.getValue).getAmountList.asScala.toSeq.map(x => Coin(x)))
      if (limitLeft.exists(_.isZero)) ValidateResponse(accept = true, delete = true, updated = None)
      else ValidateResponse(accept = true, delete = false, updated = Option(SendAuthorization(spendLimit = limitLeft)))
    }

    def toProto: com.cosmos.bank.v1beta1.SendAuthorization = com.cosmos.bank.v1beta1.SendAuthorization.newBuilder()
      .addAllSpendLimit(spendLimit.map(_.toProtoCoin).asJava)
      .build()

    def toAnyProto: protoAny = protoAny.newBuilder()
      .setTypeUrl(schema.constants.Authz.SEND_AUTHORIZATION)
      .setValue(this.toProto.toByteString)
      .build()

  }

  //authz
  case class GenericAuthorization(msg: String) extends Authorization {
    def getMsgTypeURL: String = msg

    def validate(stdMsg: protoAny): ValidateResponse = ValidateResponse(accept = true, delete = false, updated = None)

    def toProto: com.cosmos.authz.v1beta1.GenericAuthorization = com.cosmos.authz.v1beta1.GenericAuthorization.newBuilder()
      .setMsg(this.msg)
      .build()

    def toAnyProto: protoAny = protoAny.newBuilder()
      .setTypeUrl(schema.constants.Authz.GENERIC_AUTHORIZATION)
      .setValue(this.toProto.toByteString)
      .build()

  }

  //staking
  case class StakeAuthorizationValidators(address: Seq[String]) {

    def toProto: com.cosmos.staking.v1beta1.StakeAuthorization.Validators = com.cosmos.staking.v1beta1.StakeAuthorization.Validators.newBuilder().addAllAddress(this.address.asJava).build()

  }

  object StakeAuthorizationValidators {
    def fromProto(protoValidators: com.cosmos.staking.v1beta1.StakeAuthorization.Validators): StakeAuthorizationValidators = StakeAuthorizationValidators(protoValidators.getAddressList.asScala.toSeq)
  }

  case class StakeAuthorization(maxTokens: Coin, allowList: StakeAuthorizationValidators, denyList: StakeAuthorizationValidators, authorizationType: String) extends Authorization {
    def getMsgTypeURL: String = authorizationType match {
      case schema.constants.Authz.StakeAuthorization.AUTHORIZATION_TYPE_DELEGATE => schema.constants.Messages.DELEGATE
      case schema.constants.Authz.StakeAuthorization.AUTHORIZATION_TYPE_UNDELEGATE => schema.constants.Messages.UNDELEGATE
      case schema.constants.Authz.StakeAuthorization.AUTHORIZATION_TYPE_REDELEGATE => schema.constants.Messages.REDELEGATE
      case _ => com.cosmos.staking.v1beta1.AuthorizationType.AUTHORIZATION_TYPE_UNSPECIFIED.toString
    }

    def validate(stdMsg: protoAny): ValidateResponse = if (maxTokens.denom == "") {
      ValidateResponse(accept = true, delete = false, updated = Option(this.copy(maxTokens = Coin("", MicroNumber.zero))))
    } else {
      val amount: Coin = getMsgTypeURL match {
        case schema.constants.Messages.DELEGATE => Coin(stakingTx.MsgDelegate.parseFrom(stdMsg.getValue).getAmount)
        case schema.constants.Messages.UNDELEGATE => Coin(stakingTx.MsgUndelegate.parseFrom(stdMsg.getValue).getAmount)
        case schema.constants.Messages.REDELEGATE => Coin(stakingTx.MsgBeginRedelegate.parseFrom(stdMsg.getValue).getAmount)
      }
      val limitLeft = maxTokens.subtract(amount)
      if (limitLeft.isZero) ValidateResponse(accept = true, delete = true, updated = None)
      else ValidateResponse(accept = true, delete = false, updated = Option(this.copy(maxTokens = limitLeft)))
    }

    private def getProtoStakeAuthorizationType: AuthorizationType = this.getMsgTypeURL match {
      case schema.constants.Messages.DELEGATE => com.cosmos.staking.v1beta1.AuthorizationType.AUTHORIZATION_TYPE_DELEGATE
      case schema.constants.Messages.UNDELEGATE => com.cosmos.staking.v1beta1.AuthorizationType.AUTHORIZATION_TYPE_UNDELEGATE
      case schema.constants.Messages.REDELEGATE => com.cosmos.staking.v1beta1.AuthorizationType.AUTHORIZATION_TYPE_REDELEGATE
      case _ => com.cosmos.staking.v1beta1.AuthorizationType.AUTHORIZATION_TYPE_UNSPECIFIED
    }

    def toProto: com.cosmos.staking.v1beta1.StakeAuthorization = com.cosmos.staking.v1beta1.StakeAuthorization.newBuilder()
      .setAllowList(this.allowList.toProto)
      .setDenyList(this.denyList.toProto)
      .setMaxTokens(this.maxTokens.toProtoCoin)
      .setAuthorizationType(this.getProtoStakeAuthorizationType)
      .build()

    def toAnyProto: protoAny = protoAny.newBuilder()
      .setTypeUrl(schema.constants.Authz.STAKE_AUTHORIZATION)
      .setValue(this.toProto.toByteString)
      .build()

  }


}
