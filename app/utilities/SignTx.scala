package utilities

import java.nio.charset.StandardCharsets
import java.util.Base64

import org.bitcoinj.core.{ECKey, Sha256Hash, Utils}
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import queries.responses.common.Account.SinglePublicKey
import models.common.Serializable.{SignMeta, Signature, StdSignMsg, StdTx, Tx}

object SignTx {

  def sign(tx: Tx, meta: SignMeta, key: Key) = {
    val signature = createSignature(StdSignMsg(meta.account_number, meta.chain_id, tx.fee, tx.memo, tx.msg, meta.sequence), key)
    StdTx(tx.msg, tx.fee, Seq(signature), tx.memo)
  }

  def createSignature(stdSignMsg: StdSignMsg, key: Key) = Signature(ecdsaSign(canonicalizeJson(Json.toJson(stdSignMsg)).toString(), key), SinglePublicKey(constants.Blockchain.PublicKey.SINGLE, Base64.getEncoder.encodeToString(key.publicKey)))

  def ecdsaSign(message: String, key: Key) = {
    val ecdsaSignature = ECKey.fromPrivateAndPrecalculatedPublic(key.privateKey, key.publicKey).sign(Sha256Hash.wrap(Sha256Hash.hash(message.getBytes(StandardCharsets.UTF_8))))
    Base64.getEncoder.encodeToString(Utils.bigIntegerToBytes(ecdsaSignature.r, 32) ++ Utils.bigIntegerToBytes(ecdsaSignature.s, 32))
  }

  def canonicalizeJson(js: JsValue): JsValue = js match {
    case JsObject(fields) => JsObject(fields.toSeq.sortBy(_._1).map { case (key, value) => (key, canonicalizeJson(value)) })
    case JsArray(array) => JsArray(array.map(e => canonicalizeJson(e)))
    case other => other
  }
}