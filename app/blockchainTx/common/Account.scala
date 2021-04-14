package blockchainTx.common

import play.api.Logger
import play.api.libs.json._
import queries.Abstract.{Account, PublicKey}
import play.api.libs.functional.syntax._

object Account {

  private implicit val module: String = constants.Module.ACCOUNT_RESPONSE

  private implicit val logger: Logger = Logger(this.getClass)

  case class SinglePublicKey(publicKeyType: String, value: String) extends PublicKey {
    val publicKeyValue: String = value
  }

  implicit val singlePublicKeyReads: Reads[SinglePublicKey] = (
    (JsPath \ "type").read[String] and
      (JsPath \ "value").read[String]
    ) (SinglePublicKey.apply _)

  implicit val singlePublicKeyWrites: Writes[SinglePublicKey] = (singlePublicKey: SinglePublicKey) => Json.obj(
    "type" -> singlePublicKey.publicKeyType,
    "value" -> singlePublicKey.value
  )

  case class MultiSigPublicKeyValue(threshold: String, pubkeys: Seq[SinglePublicKey])

  implicit val multiSigPublicKeyValueReads: Reads[MultiSigPublicKeyValue] = Json.reads[MultiSigPublicKeyValue]

  case class MultiSigPublicKey(publicKeyType: String, value: MultiSigPublicKeyValue) extends PublicKey {
    val publicKeyValue: String = value.pubkeys.map(_.value).toString
  }

  implicit val multiSigPublicKeyReads: Reads[MultiSigPublicKey] = Json.reads[MultiSigPublicKey]

  def publicKeyApply(publicKeyType: String, value: JsValue): PublicKey = publicKeyType match {
    case constants.Blockchain.PublicKey.SINGLE => SinglePublicKey(publicKeyType = publicKeyType, value = value.toString)
    case constants.Blockchain.PublicKey.MULTI_SIG => MultiSigPublicKey(publicKeyType, utilities.JSON.convertJsonStringToObject[MultiSigPublicKeyValue](value.toString))
  }

  implicit val publicKeyReads: Reads[PublicKey] = (
    (JsPath \ "type").read[String] and
      (JsPath \ "value").read[JsValue]
    ) (publicKeyApply _)

  case class BaseAccount(address: String, coins: Seq[Coin], public_key: Option[PublicKey], account_number: String, sequence: String) extends Account {
    val publicKeyValue: String = public_key.fold("")(_.publicKeyValue)
    val accountNumber: String = account_number
  }

  implicit val baseAccountReads: Reads[BaseAccount] = Json.reads[BaseAccount]

  case class ModuleAccount(BaseAccount: BaseAccount, name: String) extends Account {
    val address: String = BaseAccount.address
    val coins: Seq[Coin] = BaseAccount.coins
    val publicKeyValue: String = BaseAccount.publicKeyValue
    val accountNumber: String = BaseAccount.account_number
    val sequence: String = BaseAccount.sequence
  }

  implicit val moduleAccountReads: Reads[ModuleAccount] = Json.reads[ModuleAccount]

  case class DelayedVestingAccount(BaseAccount: BaseAccount, original_vesting: Seq[Coin], delegated_free: Seq[Coin], delegated_vesting: Seq[Coin], end_time: String)

  implicit val delayedVestingAccountReads: Reads[DelayedVestingAccount] = Json.reads[DelayedVestingAccount]

  case class BaseVestingAccount(BaseVestingAccount: DelayedVestingAccount) extends Account {
    val address: String = BaseVestingAccount.BaseAccount.address
    val coins: Seq[Coin] = BaseVestingAccount.BaseAccount.coins
    val publicKeyValue: String = BaseVestingAccount.BaseAccount.publicKeyValue
    val accountNumber: String = BaseVestingAccount.BaseAccount.account_number
    val sequence: String = BaseVestingAccount.BaseAccount.sequence
  }

  implicit val baseVestingAccountReads: Reads[BaseVestingAccount] = Json.reads[BaseVestingAccount]

  def resultApply(accountType: String, value: JsObject): Result = accountType match {
    case constants.Blockchain.Account.BASE => Result(utilities.JSON.convertJsonStringToObject[BaseAccount](value.toString))
    case constants.Blockchain.Account.DELAYED_VESTING => Result(utilities.JSON.convertJsonStringToObject[BaseVestingAccount](value.toString))
    case constants.Blockchain.Account.MODULE => Result(utilities.JSON.convertJsonStringToObject[ModuleAccount](value.toString))
  }

  case class Result(value: Account)

  implicit val resultReads: Reads[Result] = (
    (JsPath \ "type").read[String] and
      (JsPath \ "value").read[JsObject]
    ) (resultApply _)

}
