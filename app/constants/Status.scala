package constants

object Status {

  object Contact {
    val NO_MOBILE_NUMBER = "NO_MOBILE_NUMBER"
    val NO_EMAIL_ADDRESS = "NO_EMAIL_ADDRESS"
    val EMAIL_ADDRESS_UNVERIFIED = "EMAIL_ADDRESS_UNVERIFIED"
    val EMAIL_ADDRESS_VERIFIED = "EMAIL_ADDRESS_VERIFIED"
    val MOBILE_NUMBER_UNVERIFIED = "MOBILE_NUMBER_UNVERIFIED"
    val MOBILE_NUMBER_VERIFIED = "MOBILE_NUMBER_VERIFIED"
  }

  object TraderInvitation {
    val NO_CONTACT = "NO_CONTACT"
    val CONTACT_VERIFICATION_PENDING = "CONTACT_VERIFICATION_PENDING"
    val CONTACT_VERIFICATION_COMPLETE_NO_IDENTIFICATION = "CONTACT_VERIFICATION_COMPLETE_NO_IDENTIFICATION"
    val IDENTIFICATION_VERIFICATION_PENDING = "IDENTIFICATION_VERIFICATION_PENDING"
    val TRADER_ADDED_FOR_VERIFICATION = "TRADER_ADDED_FOR_VERIFICATION"
  }

  object Asset {
    val REQUESTED_TO_ZONE = "REQUESTED_TO_ZONE"
    val AWAITING_BLOCKCHAIN_RESPONSE = "AWAITING_BLOCKCHAIN_RESPONSE"

    val ISSUED = "ISSUED"

    val IN_ORDER = "IN_ORDER"
    val REDEEMED = "REDEEMED"

    val TRADED = "TRADED"

    val ISSUE_ASSET_FAILED = "ISSUE_ASSET_FAILED"
  }

  object IssueFiat {
    val REQUEST_INITIATED = "REQUEST_INITIATED"
    val PARTIALLY_PAID = "PARTIALLY_PAID"
    val FULLY_PAID = "FULLY_PAID"
    val OVER_PAID = "OVER_PAID"
  }

  object RedeemFiat {
    val AWAITING_BLOCKCHAIN_RESPONSE = "AWAITING_BLOCKCHAIN_RESPONSE"
    val BLOCKCHAIN_SUCCESS = "BLOCKCHAIN_SUCCESS"
    val BLOCKCHAIN_FAILURE = "BLOCKCHAIN_FAILURE"
    val REDEEMED = "REDEEMED"
  }

  object SendFiat {
    val AWAITING_BLOCKCHAIN_RESPONSE = "AWAITING_BLOCKCHAIN_RESPONSE"
    val BLOCKCHAIN_SUCCESS = "BLOCKCHAIN_SUCCESS"
    val BLOCKCHAIN_FAILURE = "BLOCKCHAIN_FAILURE"
    val SENT = "SENT"
  }

  object ReceiveFiat {
    val ORDER_COMPLETION_FIAT = "ORDER_COMPLETION_FIAT"
    val ORDER_REVERSED_FIAT = "ORDER_REVERSED_FIAT"
  }

  object Negotiation {
    val FORM_INCOMPLETE = "FORM_INCOMPLETE"
    val ISSUE_ASSET_PENDING = "ISSUE_ASSET_PENDING"
    val ISSUE_ASSET_FAILED = "ISSUE_ASSET_FAILED"
    val REQUEST_SENT = "REQUEST_SENT"
    val STARTED = "STARTED"
    val BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS = "BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS"
    val CONTRACT_SIGNED = "CONTRACT_SIGNED"
    val BUYER_CONFIRMED_SELLER_PENDING = "BUYER_CONFIRMED_SELLER_PENDING"
    val SELLER_CONFIRMED_BUYER_PENDING = "SELLER_CONFIRMED_BUYER_PENDING"
    val BOTH_PARTIES_CONFIRMED = "BOTH_PARTIES_CONFIRMED"
    val COMPLETED = "COMPLETED"
    val ASSET_ALREADY_TRADED = "ASSET_ALREADY_TRADED"
    val REJECTED = "REJECTED"
    val TIMED_OUT = "TIMED_OUT"
  }


}