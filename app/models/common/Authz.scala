package models.common

import com.google.protobuf.{Any => protoAny}
import com.cosmos.staking.{v1beta1 => stakingTx}
import models.Abstract.Authorization
import models.common.Serializable.Coin
import utilities.Blockchain.Authz.ValidateResponse
import utilities.MicroNumber

import scala.jdk.CollectionConverters.{CollectionHasAsScala, IterableHasAsJava}

object Authz {

  //bank
  case class SendAuthorization(spendLimit: Seq[Coin]) extends Authorization {

    def getMsgTypeURL: String = constants.Blockchain.TransactionMessage.SEND_COIN

    def validate(stdMsg: protoAny): ValidateResponse = {
      val (limitLeft, _) = utilities.Blockchain.subtractCoins(spendLimit, com.cosmos.bank.v1beta1.MsgSend.parseFrom(stdMsg.getValue).getAmountList.asScala.toSeq.map(x => Coin(x)))
      if (limitLeft.exists(_.isZero)) ValidateResponse(accept = true, delete = true, updated = None)
      else ValidateResponse(accept = true, delete = false, updated = Option(SendAuthorization(spendLimit = limitLeft)))
    }

    def toProto: protoAny = protoAny.newBuilder()
      .setTypeUrl(constants.Blockchain.Authz.SEND_AUTHORIZATION)
      .setValue(com.cosmos.bank.v1beta1.SendAuthorization.newBuilder()
        .addAllSpendLimit(spendLimit.map(_.toProtoCoin).asJava)
        .build()
        .toByteString
      ).build()

  }

  //authz
  case class GenericAuthorization(msg: String) extends Authorization {
    def getMsgTypeURL: String = msg

    def validate(stdMsg: protoAny): ValidateResponse = ValidateResponse(accept = true, delete = false, updated = None)

    def toProto: protoAny = protoAny.newBuilder()
      .setTypeUrl(constants.Blockchain.Authz.GENERIC_AUTHORIZATION)
      .setValue(com.cosmos.authz.v1beta1.GenericAuthorization.newBuilder()
        .setMsg(this.msg)
        .build()
        .toByteString
      ).build()

  }

  //staking
  case class StakeAuthorizationValidators(address: Seq[String])

  object StakeAuthorizationValidators {
    def fromProtoAny(protoValidators: com.cosmos.staking.v1beta1.StakeAuthorization.Validators): StakeAuthorizationValidators = StakeAuthorizationValidators(protoValidators.getAddressList.asScala.toSeq)
  }

  case class StakeAuthorization(maxTokens: Coin, allowList: StakeAuthorizationValidators, denyList: StakeAuthorizationValidators, authorizationType: String) extends Authorization {
    def getMsgTypeURL: String = authorizationType match {
      case constants.Blockchain.Authz.StakeAuthorization.AUTHORIZATION_TYPE_DELEGATE => constants.Blockchain.TransactionMessage.DELEGATE
      case constants.Blockchain.Authz.StakeAuthorization.AUTHORIZATION_TYPE_UNDELEGATE => constants.Blockchain.TransactionMessage.UNDELEGATE
      case constants.Blockchain.Authz.StakeAuthorization.AUTHORIZATION_TYPE_REDELEGATE => constants.Blockchain.TransactionMessage.REDELEGATE
      case _ => com.cosmos.staking.v1beta1.AuthorizationType.AUTHORIZATION_TYPE_UNSPECIFIED.toString
    }

    def validate(stdMsg: protoAny): ValidateResponse = if (maxTokens.denom == "") {
      ValidateResponse(accept = true, delete = false, updated = Option(this.copy(maxTokens = Coin("", MicroNumber.zero))))
    } else {
      val amount: Coin = getMsgTypeURL match {
        case constants.Blockchain.TransactionMessage.DELEGATE => Coin(stakingTx.MsgDelegate.parseFrom(stdMsg.getValue).getAmount)
        case constants.Blockchain.TransactionMessage.UNDELEGATE => Coin(stakingTx.MsgUndelegate.parseFrom(stdMsg.getValue).getAmount)
        case constants.Blockchain.TransactionMessage.REDELEGATE => Coin(stakingTx.MsgBeginRedelegate.parseFrom(stdMsg.getValue).getAmount)
      }
      val limitLeft = maxTokens.subtract(amount)
      if (limitLeft.isZero) ValidateResponse(accept = true, delete = true, updated = None)
      else ValidateResponse(accept = true, delete = false, updated = Option(this.copy(maxTokens = limitLeft)))
    }

    def toProto: protoAny = protoAny.newBuilder()
      .setTypeUrl(constants.Blockchain.Authz.STAKE_AUTHORIZATION)
      .setValue(com.cosmos.staking.v1beta1.StakeAuthorization.newBuilder()
        .setMaxTokens(this.maxTokens.toProtoCoin)
        .setAuthorizationType(com.cosmos.staking.v1beta1.AuthorizationType.AUTHORIZATION_TYPE_UNSPECIFIED)
        .build()
        .toByteString
      ).build()

  }


}
