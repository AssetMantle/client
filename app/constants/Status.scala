package constants

object Status {

  object Account {
    val NO_CONTACT = "NO_CONTACT"
    val CONTACT_UNVERIFIED = "CONTACT_UNVERIFIED"
    val EMAIL_ADDRESS_UNVERIFIED = "EMAIL_ADDRESS_UNVERIFIED"
    val MOBILE_NUMBER_UNVERIFIED = "MOBILE_NUMBER_UNVERIFIED"
    val COMPLETE = "COMPLETE"
  }

  object TraderInvitation {
    val NO_CONTACT = "NO_CONTACT"
    val CONTACT_VERIFICATION_PENDING = "CONTACT_VERIFICATION_PENDING"
    val CONTACT_VERIFICATION_COMPLETE_NO_IDENTIFICATION = "CONTACT_VERIFICATION_COMPLETE_NO_IDENTIFICATION"
    val IDENTIFICATION_VERIFICATION_PENDING = "IDENTIFICATION_VERIFICATION_PENDING"
    val IDENTIFICATION_COMPLETE_DOCUMENT_UPLOAD_PENDING = "IDENTIFICATION_COMPLETE_DOCUMENT_UPLOAD_PENDING"
    val TRADER_ADDED_FOR_VERIFICATION = "TRADER_ADDED_FOR_VERIFICATION"
  }

  object Asset {
    val REQUESTED_TO_ZONE = "REQUESTED_TO_ZONE"
    val AWAITING_BLOCKCHAIN_RESPONSE = "AWAITING_BLOCKCHAIN_RESPONSE"

    val ISSUED = "ISSUED"

    val IN_ORDER = "IN_ORDER"
    val REDEEMED = "REDEEMED"

    val TRADE_COMPLETED = "TRADE_COMPLETED"

    val ISSUE_ASSET_FAILED = "ISSUE_ASSET_FAILED"
    val REJECTED_BY_ZONE = "REJECTED_BY_ZONE"
  }

  object Negotiation {
    val ISSUE_ASSET_PENDING = "ISSUE_ASSET_PENDING"
    val FORM_INCOMPLETE = "FORM_INCOMPLETE"

    val REQUEST_SENT = "REQUEST_SENT"

    val ISSUE_ASSET_FAILED = "ISSUE_ASSET_FAILED"
    val CONTRACT_UPLOADED = "CONTRACT_UPLOADED"
    val NEGOTIATION_STARTED = "NEGOTIATION_STARTED"
    val BUYER_CONFIRMED_ALL_NEGOTIATION_TERMS = "BUYER_CONFIRMED_ALL_NEGOTIATION_TERMS"
    val BUYER_CONFIRMED_PRICE_SELLER_PENDING = "BUYER_CONFIRMED_PRICE_SELLER_PENDING"
    val SELLER_CONFIRMED_PRICE_BUYER_PENDING = "SELLER_CONFIRMED_BID_BUYER_PENDING"
    val BOTH_PARTY_CONFIRMED_PRICE = "BOTH_PARTY_CONFIRMED_PRICE"
    val TIMED_OUT = "TIMED_OUT"
    val REJECTED = "REJECTED"
    val TRADE_COMPLETED = "TRADE_COMPLETED"
  }

  object TraderStatus {
    val NOT_SIGNED_UP = "NOT_SIGNED_UP"
    val SIGNED_UP = "SIGNED_UP"
    val ADD_TRADER_FORM_INCOMPLETE = "ADD_TRADER_FORM_INCOMPLETE"
    val REQUESTED = "REQUESTED"
    val REJECTED = "REJECTED"
    val APPROVED = "APPROVED"
  }

  object DocuSignEnvelopeStatus{
    val CREATED="created"
    val SENT="Send"
    val SIGNING_COMPLETE="signing_complete"
  }

}