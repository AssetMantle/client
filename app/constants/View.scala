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

  //addIdentification
  val ID_PROOF = "ID_PROOF"
  val PENDING_IDENTIFICATION_VERIFICATION = "PENDING_IDENTIFICATION_VERIFICATION"
  val IDENTIFICATION_REJECTED = "IDENTIFICATION_REJECTED"
  val NOT_SPECIFIED = "NOT_SPECIFIED"
  val CONTACT_DETAILS = "CONTACT_DETAILS"
  val UPLOAD_IDENTIFICATION = "UPLOAD_IDENTIFICATION"

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
  val BUYER_TRADER_NAME = "BUYER_TRADER_NAME"
  val SELLER_TRADER_NAME = "SELLER_TRADER_NAME"
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
  val ASSET_TYPE = "ASSET_TYPE"
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
  val VIEW_DOCUMENTS = "VIEW_DOCUMENTS"
  val FINANCIALS = "FINANCIALS"
  val CHECKS = "CHECKS"
  val LIVE_TRADE_ROOM = "LIVE_TRADE_ROOM"
  val TRADE_ROOM_ID = "TRADE_ROOM_ID"
  val TRADE_TERMS = "TRADE_TERMS"
  val VIEW_TERMS = "VIEW_TERMS"
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
  val DOCUMENT_LIST = "DOCUMENT_LIST"
  val CREDIT_TERMS = "CREDIT_TERMS"
  val BILL_OF_EXCHANGE_REQUIRED = "BILL_OF_EXCHANGE_REQUIRED"
  val PRIMARY_DOCUMENTS = "PRIMARY_DOCUMENTS"
  val BILL_OF_EXCHANGE = "BILL_OF_EXCHANGE"
  val INVOICE = "INVOICE"
  val COO = "COO"
  val COA = "COA"
  val OTHER_DOCUMENTS = "OTHER_DOCUMENTS"
  val TRADE_LIST = "TRADE_LIST"
  val BUYER = "BUYER"
  val SELLER = "SELLER"
  val OPEN_SALES_QUOTES = "OPEN_SALES_QUOTES"
  val TRADE_ROOMS = "TRADE_ROOMS"
  val ACTIVE = "ACTIVE"
  val COMPLETED = "COMPLETED"
  val GO_TO_TRADE_ROOM = "GO_TO_TRADE_ROOM"
  val PAY = "PAY"
  val PAYABLE = "PAYABLE"
  val RECEIVABLE = "RECEIVABLE"
  val WALLET_BALANCE = "WALLET_BALANCE"
  val TOTAL_AMOUNT_DUE = "TOTAL_AMOUNT_DUE"
  val ADVANCE_PAID = "ADVANCE_PAID"
  val AMOUNT_PENDING = "AMOUNT_PENDING"
  val ORDER_ACTIONS = "ORDER_ACTIONS"
  val MODERATED_BUYER_EXECUTE_ORDER_DOCUMENT = "MODERATED_BUYER_EXECUTE_ORDER_DOCUMENT"
  val MODERATED_SELLER_EXECUTE_ORDER_DOCUMENT = "MODERATED_SELLER_EXECUTE_ORDER_DOCUMENT"
  val NEGOTIATION_FILE_NOT_FOUND = "NEGOTIATION_FILE_NOT_FOUND"

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
  val TAB_TITLE = "TAB_TITLE"
  val ACCOUNT = "ACCOUNT"
  val INDEX = "INDEX"
  val DASHBOARD = "DASHBOARD"
  val COMMIT = "COMMIT"
  val PROFILE = "PROFILE"
  val MARKET = "MARKET"
  val TRADES = "TRADES"
  val TRANSACTIONS = "TRANSACTIONS"
  val REQUEST = "REQUEST"
  val TEAM = "TEAM"
  val TRADER_ID = "TRADER_ID"
  val BUYER_TRADER_ID = "BUYER_TRADER_ID"
  val SELLER_TRADER_ID = "SELLER_TRADER_ID"
  val BUYER_ORGANIZATION_ID = "BUYER_ORGANIZATION_ID"
  val SELLER_ORGANIZATION_ID = "SELLER_ORGANIZATION_ID"
  val NAME = "NAME"
  val TRADERS = "TRADERS"
  val TRADER = "TRADER"
  val REJECTED_TRADER_REQUESTS = "REJECTED_TRADER_REQUESTS"
  val TRADER_REQUEST = "TRADER_REQUEST"
  val ZONE_STATUS = "ZONE_STATUS"
  val ORGANIZATION_STATUS = "ORGANIZATION_STATUS"
  val VIEW_REQUEST = "VIEW_REQUEST"
  val PLEASE_APPROVE_ALL_KYC_DOCUMENTS = "PLEASE_APPROVE_ALL_KYC_DOCUMENTS"

  val ORGANIZATIONS = "ORGANIZATIONS"
  val ORGANIZATION = "ORGANIZATION"
  val APPROVED = "APPROVED"
  val PENDING_ORGANIZATION_REQUESTS = "PENDING_ORGANIZATION_REQUESTS"
  val REJECTED_ORGANIZATION_REQUESTS = "REJECTED_ORGANIZATION_REQUESTS"
  val ORGANIZATION_UBO_DETAIL = "ORGANIZATION_UBO_DETAIL"
  val UBOS = "UBOS"
  val UBO = "UBO"
  val FILL_ORGANIZATION_DETAILS = "FILL_ORGANIZATION_DETAILS"
  val COMPLETE_ORGANIZATION_DETAILS_REQUEST = "COMPLETE_ORGANIZATION_DETAILS_REQUEST"
  val PERSON_NAME = "PERSON_NAME"
  val SHARE_PERCENTAGE = "SHARE_PERCENTAGE"
  val RELATIONSHIP = "RELATIONSHIP"
  val TITLE = "TITLE"
  val PENDING = "PENDING"
  val DELETE = "DELETE"
  val DOCUMENTS = "DOCUMENTS"
  val VERIFIED = "VERIFIED"
  val NOT_VERIFIED = "NOT_VERIFIED"
  val COUNTER_PARTIES = "COUNTER_PARTIES"
  val INVITED_LIST = "INVITED_LIST"
  val REFRESH = "REFRESH"
  val UPLOAD_HISTORY = "UPLOAD_HISTORY"
  val FINANCIAL_GRAPH = "FINANCIAL_GRAPH"
  val SELECT = "SELECT"
  val BACK = "BACK"
  val NEGOTIATION_REQUEST_ASSET_DETAILS_TOOLTIP = "NEGOTIATION_REQUEST_ASSET_DETAILS_TOOLTIP"
  val ISSUE_ASSET_PENDING = "ISSUE_ASSET_PENDING"
  val ASSET = "ASSET"
  val BUY_NEGOTIATION_LIST = "BUY_NEGOTIATION_LIST"
  val SELL_NEGOTIATION_LIST = "SELL_NEGOTIATION_LIST"
  val REJECTED_OR_FAILED_NEGOTIATION_LIST = "REJECTED_OR_FAILED_NEGOTIATION_LIST"
  val RECEIVED_NEGOTIATION_LIST = "RECEIVED_NEGOTIATION_LIST"
  val INCOMPLETE_NEGOTIATION_LIST = "INCOMPLETE_SALES_QUOTE_LIST"
  val SENT_NEGOTIATION_REQUEST_LIST = "SENT_NEGOTIATION_REQUEST_LIST"
  val REJECTED_NEGOTIATION_LIST = "REJECTED_NEGOTIATION_LIST"
  val REJECTED_RECEIVED_NEGOTIATION_LIST = "REJECTED_RECEIVED_NEGOTIATION_LIST"
  val REJECTED_SENT_NEGOTIATION_LIST = "REJECTED_SENT_NEGOTIATION_LIST"
  val FAILED_NEGOTIATION_LIST = "FAILED_NEGOTIATION_LIST"
  val ASSET_ID = "ASSET_ID"
  val MESSAGE_NOT_SENT = "MESSAGE_NOT_SENT"
  val COMMENT = "COMMENT"
  val ASSET_DETAILS = "ASSET_DETAILS"
  val SHIPPING_PERIOD = "SHIPPING_PERIOD"
  val YES = "YES"
  val NO = "NO"
  val SELECT_ASSET = "SELECT_ASSET"
  val SELECT_COUNTER_PARTY = "SELECT_COUNTER_PARTY"
  val ORGANIZATION_DECALRATIONS = "ORGANIZATION_DECALRATIONS"
  val ORGANIZATION_ID = "ORGANIZATION_ID"
  val ORGANIZATION_REQUEST = "ORGANIZATION_REQUEST"
  val VIEW = "VIEW"
  val APPROVE = "APPROVE"
  val ZONE_VIEW_ACCEPTED_TRADER_REQUESTS = "ZONE_VIEW_ACCEPTED_TRADER_REQUESTS"
  val ZONE_VIEW_PENDING_TRADER_REQUESTS = "ZONE_VIEW_PENDING_TRADER_REQUESTS"
  val ZONE_VIEW_REJECTED_TRADER_REQUESTS = "ZONE_VIEW_REJECTED_TRADER_REQUESTS"
  val ORGANIZATION_VIEW_ACCEPTED_TRADER_REQUESTS = "ORGANIZATION_VIEW_ACCEPTED_TRADER_REQUESTS"
  val ORGANIZATION_VIEW_PENDING_TRADER_REQUESTS = "ORGANIZATION_VIEW_PENDING_TRADER_REQUESTS"
  val ORGANIZATION_VIEW_REJECTED_TRADER_REQUESTS = "ORGANIZATION_VIEW_REJECTED_TRADER_REQUESTS"
  val ORGANIZATION_SUBSCRIPTION_DETAILS = "ORGANIZATION_SUBSCRIPTION_DETAILS"
  val TRADER_SUBSCRIPTION_DETAILS = "TRADER_SUBSCRIPTION_DETAILS"
  val ZONE_TRADE_STATISTICS = "ZONE_TRADE_STATISTICS"
  val ORGANIZATION_TRADE_STATISTICS = "ORGANIZATION_TRADE_STATISTICS"
  val TRADER_TRADE_STATISTICS = "TRADER_TRADE_STATISTICS"
  val FIAT_TRANSACTIONS="FIAT_TRANSACTIONS"
  val NO_COMMENTS = "NO_COMMENTS"
  val ACCEPTED_TRADER_RELATION_LIST = "ACCEPTED_TRADER_RELATION_LIST"
  val MNEMONIC_NOTE = "MNEMONIC_NOTE"
  val UPDATE_EMAIL_ADDRESS = "UPDATE_EMAIL_ADDRESS"
  val UPDATE_MOBILE_NUMBER = "UPDATE_MOBILE_NUMBER"
  val FOOTER_TEXT = "FOOTER_TEXT"
  val ASSET_OTHER_DETAILS = "ASSET_OTHER_DETAILS"
  val ADD = "ADD"
  val REGISTER = "REGISTER"
  val NO_TRADE_ACTIVITIES_FOUND = "NO_TRADE_ACTIVITIES_FOUND"
  val NO_ACTIVITIES = "NO_ACTIVITIES"
  val BANK_ACCOUNT = "BANK_ACCOUNT"
  val ACCEPT = "ACCEPT"
  val REJECT = "REJECT"
  val MODIFY = "MODIFY"
  val MESSAGES_NOT_FOUND="MESSAGES_NOT_FOUND"
  val USER_NAME_NOT_AVAILABLE="USER_NAME_NOT_AVAILABLE"
  val REPEAT_PASSWORD_MISMATCH="REPEAT_PASSWORD_MISMATCH"
  val INVOICE_NUMBER = "INVOICE_NUMBER"
  val INVOICE_DATE = "INVOICE_DATE"
  val CONTRACT_NUMBER = "CONTRACT_NUMBER"
  val CONTENT_NOT_FOUND = "CONTENT_NOT_FOUND"
  val ASSET_DOCUMENTS = "ASSET_DOCUMENTS"
  val NEGOTIATION_DOCUMENTS = "NEGOTIATION_DOCUMENTS"
  val CONTRACT_UPLOADED = "CONTRACT_UPLOADED"
  val FIAT_PROOF = "FIAT_PROOF"
  val DAYS = "DAYS"
  val AMOUNT_REQUESTED="AMOUNT_REQUESTED"
  val AMOUNT_RECEIVED="AMOUNT_RECEIVED"
}
