package constants

object User {

  //common
  val UNAUTHORIZED_TRANSACTION  = "UNAUTHORIZED TRANSACTION"
  val COMDEX                    = "COMDEX"
  val HOME                      = "HOME"
  val USER_TYPE                 = "USER TYPE"
  val ADDRESS                   = "ADDRESS"
  val COINS                     = "COINS"
  val NAME                      = "NAME"

  //genesis
  val GENESIS                   = "GENESIS"
  val MAIN_ACCOUNT              = "main"

  //trader
  val TRADER                    = "TRADER"
  val NOT_TRADER                = "Address is not of a Trader!"

  //fiatsTable
  val PEGHASH                   = "PEGHASH"
  val TRANSACTION_ID            = "TRANSACTION ID"
  val TRANSACTION_AMOUNT        = "TRANSACTION AMOUNT"
  val REDEEMED_AMOUNT           = "REDEEMED AMOUNT"

  //assetsTable
  val DOCUMENT_HASH             = "DOCUMENT HASH"
  val ASSET_TYPE                = "ASSET TYPE"
  val ASSET_QUANTITY            = "ASSET QUANTITY"
  val ASSET_PRICE               = "ASSET PRICE"
  val ASSET_UNIT                = "ASSET UNIT"
  val QUANTITY_UNIT             = "QUANTITY UNIT"
  val LOCKED                    = "LOCKED"

  //user
  val USER                      = "USER"
  val ASSETS_OWNED              = "ASSETS OWNED"
  val FIATS_OWNED               = "FIATS OWNED"
  val FIATS_TOTAL_VALUE         = "FIATS TOTAL VALUE"

  //organization
  val ORGANIZATION              = "ORGANIZATION"
  val ORGANIZATION_ID           = "ORGANIZATION ID"
  val ORGANIZATION_NAME         = "ORGANIZATION NAME"
  val ORGANIZATION_USERNAME     = "ORGANIZATION USERNAME"
  val PHONE                     = "PHONE"
  val EMAIL                     = "EMAIL"
  val VERIFIED_STATUS           = "VERIFIED STATUS"

  //zone
  val ZONE                      = "ZONE"
  val ZONE_ID                   = "ZONE ID"
  val CURRENCY                  = "CURRENCY"

  //zone
  val UNKNOWN                   = "UNKNOWN"

  //unknownWithoutLogin
  val WITHOUT_LOGIN     = "WITHOUT_LOGIN"
  val REQUEST_COINS_AMOUNT          = 5

}
