package models.Abstract

import com.google.protobuf.{Any => protoAny}
import com.cosmos.feegrant.{v1beta1 => protoFeeGrant}
import models.common.FeeGrant._
import models.common.Serializable.Coin
import utilities.Blockchain.{FeeGrant => utilitiesFeeGrant}
import utilities.Date.RFC3339

import scala.jdk.CollectionConverters.CollectionHasAsScala

abstract class FeeAllowance {
  def deleteAndUpdate(blockTime: RFC3339, fees: Seq[Coin]): (Boolean, FeeAllowance)

  def validate(blockTime: RFC3339, fees: Seq[Coin]): utilitiesFeeGrant.ValidateResponse = {
    val (delete, updatedAllowanceValue) = this.deleteAndUpdate(blockTime, fees)
    utilitiesFeeGrant.ValidateResponse(delete = delete, updated = updatedAllowanceValue)
  }

  def toProto: protoAny

}

object FeeAllowance {
  def apply(feeGrantAny: protoAny): FeeAllowance = feeGrantAny.getTypeUrl match {
    case constants.Blockchain.FeeGrant.BASIC_ALLOWANCE => {
      val protoBasicAllowance = protoFeeGrant.BasicAllowance.parseFrom(feeGrantAny.toByteString)
      BasicAllowance(spendLimit = protoBasicAllowance.getSpendLimitList.asScala.toSeq.map(x => Coin(x)), expiration = protoBasicAllowance.getExpiration.getSeconds)
    }
    case constants.Blockchain.FeeGrant.PERIODIC_ALLOWANCE => {
      val protoAllowance = protoFeeGrant.PeriodicAllowance.parseFrom(feeGrantAny.toByteString)
      PeriodicAllowance(basicAllowance = BasicAllowance(protoAllowance.getBasic), period = protoAllowance.getPeriod.getSeconds, periodSpendLimit = protoAllowance.getPeriodSpendLimitList.asScala.toSeq.map(x => Coin(x)), periodCanSpend = protoAllowance.getPeriodCanSpendList.asScala.toSeq.map(x => Coin(x)), periodReset = protoAllowance.getPeriodReset.getSeconds)
    }
    case constants.Blockchain.FeeGrant.ALLOWED_MSG_ALLOWANCE => {
      val protoAllowance = protoFeeGrant.AllowedMsgAllowance.parseFrom(feeGrantAny.toByteString)
      AllowedMsgAllowance(allowance = FeeAllowance(protoAllowance.getAllowance), allowedMessages = protoAllowance.getAllowedMessagesList.asScala.toSeq)
    }
  }
}