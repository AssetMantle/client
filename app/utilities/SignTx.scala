package utilities

import java.nio.charset.StandardCharsets
import java.util.Base64

import org.bitcoinj.core.{ECKey, Sha256Hash, Utils}
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import blockchainTx.common.Account.SinglePublicKey
import transactions.request.Serializable.{SignMeta, Signature, StdSignMsg, StdTx, Tx}

object SignTx {

  def sign(tx: Tx, meta: SignMeta, ecKey: ECKey) = {
    val signature = createSignature(StdSignMsg(meta.account_number, meta.chain_id, tx.fee, tx.memo, tx.msg, meta.sequence), ecKey)
    StdTx(tx.msg, tx.fee, Seq(signature), tx.memo)
  }

  def createSignature(stdSignMsg: StdSignMsg, ecKey: ECKey) = Signature(ecdsaSign(canonicalizeJson(Json.toJson(stdSignMsg)).toString(), ecKey), SinglePublicKey(constants.Blockchain.PublicKey.SINGLE, Base64.getEncoder.encodeToString(ecKey.getPubKey)))

  def ecdsaSign(message: String, ecKey: ECKey) = {
    val ecdsaSignature = ecKey.sign(Sha256Hash.wrap(Sha256Hash.hash(message.getBytes(StandardCharsets.UTF_8))))
    Base64.getEncoder.encodeToString(Utils.bigIntegerToBytes(ecdsaSignature.r, 32) ++ Utils.bigIntegerToBytes(ecdsaSignature.s, 32))
  }

  def canonicalizeJson(js: JsValue): JsValue = js match {
    case JsObject(fields) => JsObject(fields.toSeq.sortBy(_._1).map { case (key, value) => (key, canonicalizeJson(value)) })
    case JsArray(array) => JsArray(array.map(e => canonicalizeJson(e)))
    case other => other
  }
}