package models.Abstract

abstract class PublicKey {
  val publicKeyType: String
  val value: String

  def isValidatorKey: Boolean = publicKeyType == constants.Blockchain.PublicKey.VALIDATOR

  def getAccountAddress: String = utilities.Bech32.convertAccountPublicKeyToAccountAddress(value)
}