package constants

object View {
  val USER_TYPE_DETAILS = "USER_TYPE_DETAILS"
  val ORGANIZATION_BANK_ACCOUNT_DETAIL = "ORGANIZATION_BANK_ACCOUNT_DETAIL"
  val CONFIRM_BUYER_BID = "CONFIRM_BUYER_BID"
  val CONFIRM_SELLER_BID = "CONFIRM_SELLER_BID"
  val IDENTIFICATION_DETAILS = "IDENTIFICATION_DETAILS"
  val UPDATE_IDENTIFICATION_DETAILS = "UPDATE_IDENTIFICATION_DETAILS"
  val PENDING_RELATION_REQUESTS = "PENDING_RELATION_REQUESTS"

  //Pending
  val PENDING_VERIFY_ORGANIZATION_REQUESTS = "PENDING_VERIFY_ORGANIZATION_REQUESTS"
  val PENDING_SET_ACL = "PENDING_SET_ACL"
  val PENDING_ISSUE_ASSET_REQUESTS = "PENDING_ISSUE_ASSET_REQUESTS"
  val PENDING_ISSUE_FIAT_REQUESTS = "PENDING_ISSUE_FIAT_REQUESTS"
  val PENDING_RELEASE_ASSET = "PENDING_RELEASE_ASSET"
  val PENDING_BUYER_EXECUTE_ORDER = "PENDING_BUYER_EXECUTE_ORDER"
  val PENDING_SELLER_EXECUTE_ORDER = "PENDING_SELLER_EXECUTE_ORDER_LIST"
  val PENDING_SENT_TRADER_RELATION = "PENDING_SENT_TRADER_RELATION"
  val PENDING_RECEIVED_TRADER_RELATION = "PENDING_RECEIVED_TRADER_RELATION"

  //Requests
  val PENDING_VERIFY_ZONE_REQUESTS = "PENDING_VERIFY_ZONE_REQUESTS"
  val PENDING_FAUCET_REQUESTS = "PENDING_FAUCET_REQUESTS"
  val PENDING_TRADER_REQUESTS = "PENDING_TRADER_REQUESTS"
  val PENDING_REQUESTS = "PENDING_REQUESTS"

  //identification
  val ID_PROOF = "ID_PROOF"
  val PENDING_IDENTIFICATION_VERIFICATION = "PENDING_IDENTIFICATION_VERIFICATION"
  val IDENTIFICATION_REJECTED = "IDENTIFICATION_REJECTED"
  val NOT_SPECIFIED = "NOT_SPECIFIED"
  val CONTACT_DETAILS = "CONTACT_DETAILS"

  val TO_BE_FILLED = "TO_BE_FILLED"

  val INDEX_FINANCIERS_HEADER = "INDEX_FINANCIERS_HEADER"
  val INDEX_TRADERS_HEADER = "INDEX_TRADERS_HEADER"
  val INDEX_UNDERWRITERS_HEADER = "INDEX_UNDERWRITERS_HEADER"
  val INDEX_1 = "INDEX_1"
  val INDEX_2 = "INDEX_2"
  val INDEX_3 = "INDEX_3"
  val SELL = "SELL"
  val BUY = "BUY"
  val UNSET = "UNSET"

  val INDEX_FINANCIER_1 = "INDEX_FINANCIER_1"
  val INDEX_FINANCIER_2 = "INDEX_FINANCIER_2"
  val INDEX_TRADERS_1 = "INDEX_TRADERS_1"
  val INDEX_TRADERS_2 = "INDEX_TRADERS_2"
  val INDEX_UNDERWRITERS_1 = "INDEX_UNDERWRITERS_1"
  val INDEX_UNDERWRITERS_2 = "INDEX_UNDERWRITERS_2"

  val REGISTERED_ADDRESS = "REGISTERED_ADDRESS"
  val POSTAL_ADDRESS = "POSTAL_ADDRESS"

  val TRADER_NAME = "TRADER_NAME"
  val ORGANIZATION_NAME = "ORGANIZATION_NAME"
  val ACCOUNT_ID = "ACCOUNT_ID"
  val STATUS = "STATUS"
  val TRADER_RELATION_LIST = "TRADER_RELATION_LIST"
  val ORGANIZATION_DOCUMENTS = "ORGANIZATION_DOCUMENTS"

  val ZONE_NAME = "ZONE_NAME"
  val TRADER_DOCUMENTS = "TRADER_DOCUMENTS"
  val NO_REQUESTS_FOUND = "NO_REQUESTS_FOUND"
  val COPY_TO_CLIPBOARD = "COPY_TO_CLIPBOARD"
  val USER_VIEW_PENDING_REQUESTS = "USER_VIEW_PENDING_REQUESTS"

  //salesQuote
  val SALES_QUOTE_LIST = "SALES_QUOTE_LIST"
  val COMMODITY_DETAILS = "COMMODITY_DETAILS"
  val SHIPPING_DETAILS = "SHIPPING_DETAILS"
  val PAYMENT_TERMS = "PAYMENT_TERMS"
  val SALES_QUOTE_DOCUMENTS = "DOCUMENTS"
  val ACTION = "ACTION"
  val INVITE = "INVITE"
  val COMPLETE = "COMPLETE"
  val SALES_QUOTE_ID = "SALES_QUOTE_ID"
  val ASSET_DESCRIPTION = "ASSET_DESCRIPTION"
  val COUNTER_PARTY_LIST = "COUNTER_PARTY_LIST"
  val COUNTER_PARTY = "COUNTER_PARTY"
  val COUNTER_PARTY_DETAILS = "COUNTER_PARTY_DETAILS"
  val ACCEPTED = "ACCEPTED"
  val REJECTED = "REJECTED"
  val INVITATION_SENT = "INVITATION_SENT"
  val INCOMPLETE = "INCOMPLETE"

  //TradeRoom
  val ADD_BALANCE = "ADD_BALANCE"
  val AMEND = "AMEND"
  val DOCS_VIEW = "DOCS_VIEW"
  val FINANCIALS = "FINANCIALS"
  val LIVE_TRADE_ROOM = "LIVE_TRADE_ROOM"
  val TRADE_ROOM_ID = "TRADE_ROOM_ID"
  val TRADE_TERMS = "TRADE_TERMS"
  val TERMS_VIEW = "TERMS_VIEW"
  val BUYER_ACCEPTANCE = "BUYER_ACCEPTANCE"
  val DESCRIPTION = "DESCRIPTION"
  val QUANTITY = "QUANTITY"
  val PRICE = "PRICE"
  val CONTRACT_PRICE = "CONTRACT_PRICE"
  val SHIPMENT_PERIOD = "SHIPMENT_PERIOD"
  val LOAD_PORT = "LOAD_PORT"
  val DISCHARGE_PORT = "DISCHARGE_PORT"
  val ADVANCE_PAYMENT = "ADVANCE_PAYMENT"
  val ADVANCE_PERCENTAGE = "ADVANCE_PERCENTAGE"
  val TENURE = "TENURE"
  val TENTATIVE_DATE = "TENTATIVE_DATE"
  val REFERENCE = "REFERENCE"
  val CREDIT = "CREDIT"
  val DOCUMENTS_CHECKLIST = "DOCUMENTS_CHECKLIST"
  val CREDIT_TERMS = "CREDIT_TERMS"
  val BILL_OF_EXCHANGE_REQUIRED = "BILL_OF_EXCHANGE_REQUIRED"
  val PRIMARY_DOCUMENTS = "PRIMARY_DOCUMENTS"
  val OBL = "OBL"
  val BILL_OF_EXCHANGE ="BILL_OF_EXCHANGE"
  val INVOICE ="INVOICE"
  val COO ="COO"
  val COA ="COA"
  val OTHER_DOCUMENTS ="OTHER_DOCUMENTS"
  val TRADE_LIST = "TRADE_LIST"
  val BUYER = "BUYER"
  val SELLER = "SELLER"
  val OPEN_SALES_QUOTES="OPEN_SALES_QUOTES"
  val TRADE_ROOMS="TRADE_ROOMS"
  val ACTIVE="ACTIVE"
  val COMPLETED="COMPLETED"
  val GO_TO_TRADE_ROOM="GO_TO_TRADE_ROOM"
  val PAY = "PAY"
  val PAYABLE= "PAYABLE"
  val RECEIVABLE = "RECEIVABLE"
  val WALLET_BALANCE = "WALLET_BALANCE"

  //ChatRoom
  val LOAD_MORE_CHATS = "LOAD_MORE_CHATS"
  val READ_BY = "READ_BY"
  val CHAT = "CHAT"

  //Recent Activities
  val LOAD_MORE_ACTIVITIES = "LOAD_MORE_ACTIVITIES"
  val OPEN_TRADE_ROOM = "OPEN_TRADE_ROOM"

  val RECENT_ACTIVITY = "RECENT_ACTIVITY"
  val NO_TRADERS_FOUND = "NO_TRADERS_FOUND"
  val UNAVAILABLE = "UNAVAILABLE"
  val DETAILS = "DETAILS"
  val ORGANIZATION_DETAILS = "ORGANIZATION_DETAILS"
  val BANK_ACCOUNT_NOT_FOUND = "BANK_ACCOUNT_NOT_FOUND"
  val REFRESH = "REFRESH"
  val ORGANIZATION_UBO_DETAIL = "ORGANIZATION_UBO_DETAIL"
  val UBOS = "UBOS"
  val UBO = "UBO"
  val FILL_ORGANIZATION_DETAILS = "FILL_ORGANIZATION_DETAILS"
  val COMPLETE_ORGANIZATION_DETAILS_REQUEST = "COMPLETE_ORGANIZATION_DETAILS_REQUEST"
  val PERSON_NAME = "PERSON_NAME"
  val SHARE_PERCENTAGE = "SHARE_PERCENTAGE"
  val RELATIONSHIP = "RELATIONSHIP"
  val TITLE = "TITLE"
  val ZONE_STATUS = "ZONE_STATUS"
  val ORGANIZATION_STATUS = "ORGANIZATION_STATUS"
  val PENDING = "PENDING"
  val DELETE = "DELETE"
  val DOCUMENTS = "DOCUMENTS"
  val SELECT = "SELECT"
  val BACK = "BACK"
  val NEGOTIATION_REQUEST_ASSET_DETAILS_TOOLTIP = "NEGOTIATION_REQUEST_ASSET_DETAILS_TOOLTIP"
  val ISSUE_ASSET_PENDING = "ISSUE_ASSET_PENDING"
  val ASSET = "ASSET"
  val ON_GOING_BUY_SALES_QUOTE_LIST = "ON_GOING_BUY_SALES_QUOTE_LIST"
  val ON_GOING_SELL_SALES_QUOTE_LIST = "ON_GOING_SELL_SALES_QUOTE_LIST"
  val REJECTED_OR_FAILED_SALES_QUOTE_LIST = "REJECTED_OR_FAILED_SALES_QUOTE_LIST"
  val NEW_INCOMING_BUY_SALES_QUOTE_LIST = "NEW_INCOMING_BUY_SALES_QUOTE_LIST"
  val INCOMPLETE_SALES_QUOTE_LIST = "INCOMPLETE_SALES_QUOTE_LIST"
  val PENDING_SENT_NEGOTIATION_REQUEST = "PENDING_SENT_NEGOTIATION_REQUEST"
  val ASSET_ID ="ASSET_ID"
  val MESSAGE_NOT_SENT_DUE_TO_SOME_ERROR = "MESSAGE_NOT_SENT_DUE_TO_SOME_ERROR"
}
