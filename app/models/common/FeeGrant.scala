package models.common

import com.google.protobuf.{Any => protoAny, Duration => protoDuration, Timestamp => protoTimestamp}
import com.cosmos.feegrant.{v1beta1 => protoFeeGrant}
import models.Abstract.FeeAllowance
import models.common.Serializable.Coin
import utilities.Date.RFC3339

import scala.jdk.CollectionConverters.{CollectionHasAsScala, IterableHasAsJava}

object FeeGrant {
  case class BasicAllowance(spendLimit: Seq[Coin], expiration: Long) extends FeeAllowance {

    def deleteAndUpdate(blockTime: RFC3339, fees: Seq[Coin]): (Boolean, FeeAllowance) = {
      if (this.expiration < blockTime.epoch) (true, this)
      else if (spendLimit.nonEmpty) {
        val (left, _) = utilities.Blockchain.subtractCoins(spendLimit, fees)
        (left.exists(_.isZero), this.copy(spendLimit = left))
      } else (false, this)
    }

    def toProtoBasicAllowance: protoFeeGrant.BasicAllowance = if (this.expiration != 0) {
      protoFeeGrant.BasicAllowance.newBuilder()
        .addAllSpendLimit(this.spendLimit.map(_.toProtoCoin).asJava)
        .setExpiration(protoTimestamp.newBuilder().setSeconds(this.expiration))
        .build()
    } else {
      protoFeeGrant.BasicAllowance.newBuilder()
        .addAllSpendLimit(this.spendLimit.map(_.toProtoCoin).asJava)
        .build()
    }

    def toProto: protoAny = {
      val protoFeeGrantValue = this.toProtoBasicAllowance
      protoAny.newBuilder()
        .setTypeUrl(constants.Blockchain.FeeGrant.BASIC_ALLOWANCE)
        .setValue(protoFeeGrantValue.toByteString)
        .build()
    }

  }

  object BasicAllowance {
    def apply(protoBasicAllowance: protoFeeGrant.BasicAllowance): BasicAllowance = BasicAllowance(spendLimit = protoBasicAllowance.getSpendLimitList.asScala.toSeq.map(x => Coin(x)), expiration = protoBasicAllowance.getExpiration.getSeconds)
  }

  case class PeriodicAllowance(basicAllowance: BasicAllowance, period: Long, periodSpendLimit: Seq[Coin], periodCanSpend: Seq[Coin], periodReset: Long) extends FeeAllowance {

    def deleteAndUpdate(blockTime: RFC3339, fees: Seq[Coin]): (Boolean, FeeAllowance) = {
      if (this.basicAllowance.expiration < blockTime.epoch) (true, this)
      else {
        val (resetPeriodCanSpend, updatedPeriodReset) = if (!(blockTime.epoch < this.periodReset)) {
          val (_, isNeg) = utilities.Blockchain.subtractCoins(fromCoins = this.basicAllowance.spendLimit, amount = this.periodSpendLimit)
          val resetPeriodCanSpend = if (isNeg && this.basicAllowance.spendLimit.nonEmpty) this.basicAllowance.spendLimit else this.periodSpendLimit
          val updatedPeriodReset = {
            val addPeriod = this.periodReset + this.period
            if (blockTime.epoch > addPeriod) (blockTime.epoch + this.period) else addPeriod
          }
          (resetPeriodCanSpend, updatedPeriodReset)
        } else (this.periodCanSpend, this.periodReset)
        val (updatedPeriodCanSpend, _) = utilities.Blockchain.subtractCoins(fromCoins = resetPeriodCanSpend, amount = fees)
        if (this.basicAllowance.spendLimit.nonEmpty) {
          val (updatedBasicSpendLimit, _) = utilities.Blockchain.subtractCoins(fromCoins = this.basicAllowance.spendLimit, amount = fees)
          (updatedBasicSpendLimit.exists(_.isZero), this.copy(basicAllowance = this.basicAllowance.copy(spendLimit = updatedBasicSpendLimit), periodCanSpend = updatedPeriodCanSpend, periodReset = updatedPeriodReset))
        } else (false, this.copy(periodCanSpend = updatedPeriodCanSpend, periodReset = updatedPeriodReset))
      }
    }

    def toProto: protoAny = {
      val protoFeeGrantValue = protoFeeGrant.PeriodicAllowance.newBuilder()
        .setBasic(this.basicAllowance.toProtoBasicAllowance)
        .setPeriodReset(protoTimestamp.newBuilder().setSeconds(this.periodReset).build())
        .addAllPeriodCanSpend(this.periodCanSpend.map(_.toProtoCoin).asJava)
        .addAllPeriodSpendLimit(this.periodSpendLimit.map(_.toProtoCoin).asJava)
        .setPeriod(protoDuration.newBuilder().setSeconds(this.period))
        .build()
      protoAny.newBuilder()
        .setTypeUrl(constants.Blockchain.FeeGrant.PERIODIC_ALLOWANCE)
        .setValue(protoFeeGrantValue.toByteString)
        .build()
    }
  }
  case class AllowedMsgAllowance(allowance: FeeAllowance, allowedMessages: Seq[String]) extends FeeAllowance {
    def deleteAndUpdate(blockTime: RFC3339, fees: Seq[Coin]): (Boolean, FeeAllowance) = this.allowance.deleteAndUpdate(blockTime, fees)

    def toProto: protoAny = {
      val protoFeeGrantValue = protoFeeGrant.AllowedMsgAllowance.newBuilder()
        .addAllAllowedMessages(this.allowedMessages.asJava)
        .setAllowance(this.allowance.toProto)
        .build()
      protoAny.newBuilder()
        .setTypeUrl(constants.Blockchain.FeeGrant.ALLOWED_MSG_ALLOWANCE)
        .setValue(protoFeeGrantValue.toByteString)
        .build()
    }

  }

}
