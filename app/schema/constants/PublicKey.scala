package schema.constants

object PublicKey {
  val MULTI_SIG = "/cosmos.crypto.multisig.LegacyAminoPubKey"
  val SINGLE_SECP256K1 = "/cosmos.crypto.secp256k1.PubKey"
  val SINGLE_SECP256R1 = "/cosmos.crypto.secp256r1.PubKey"
  val VALIDATOR = "/cosmos.crypto.ed25519.PubKey"
}
