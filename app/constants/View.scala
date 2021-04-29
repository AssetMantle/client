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
  val PENDING_ORGANIZATION_REQUEST = "PENDING_ORGANIZATION_REQUEST"
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
  val PENDING_TRADER_REQUEST = "PENDING_TRADER_REQUEST"
  val PENDING_REQUESTS = "PENDING_REQUESTS"
  val WALLEX_TRANSFER_REQUESTS = "WALLEX_TRANSFER_REQUESTS"
  val WALLEX_KYC_SCREENING_REQUESTS = "WALLEX_KYC_SCREENING_REQUESTS"

  //addIdentification
  val ID_PROOF = "ID_PROOF"
  val PROVIDE_ID_PROOF = "PROVIDE_ID_PROOF"
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
  val EMAIL_ADDRESS = "EMAIL_ADDRESS"
  val MOBILE_NUMBER = "MOBILE_NUMBER"
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
  val COUNTER_PARTY_TRADER = "COUNTER_PARTY_TRADER"
  val COUNTER_PARTY_ORGANIZATION = "COUNTER_PARTY_ORGANIZATION"
  val COUNTER_PARTY_DETAILS = "COUNTER_PARTY_DETAILS"
  val ACCEPTED = "ACCEPTED"
  val REJECTED = "REJECTED"
  val INVITATION_SENT = "INVITATION_SENT"
  val INCOMPLETE = "INCOMPLETE"


  //TradeRoom
  val ADD_BALANCE = "ADD_BALANCE"
  val REDEEM_BALANCE = "REDEEM_BALANCE"
  val SEND_FIAT = "SEND_FIAT"
  val AMEND = "AMEND"
  val VIEW_DOCUMENTS = "VIEW_DOCUMENTS"
  val FINANCIALS = "FINANCIALS"
  val CHECKS = "CHECKS"
  val TRADE_ROOM = "TRADE_ROOM"
  val LIVE_TRADE_ROOM = "LIVE_TRADE_ROOM"
  val TRADE_ROOM_ID = "TRADE_ROOM_ID"
  val TRADE_TERMS = "TRADE_TERMS"
  val VIEW_TERMS = "VIEW_TERMS"
  val BUYER_ACCEPTANCE = "BUYER_ACCEPTANCE"
  val DESCRIPTION = "DESCRIPTION"
  val QUANTITY = "QUANTITY"
  val PRICE = "PRICE"
  val PRICE_PER_UNIT = "PRICE_PER_UNIT"
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
  val FAILED = "FAILED"
  val GO_TO_TRADE_ROOM = "GO_TO_TRADE_ROOM"
  val PAY = "PAY"
  val PAYABLE = "PAYABLE"
  val RECEIVABLE = "RECEIVABLE"
  val WALLET_BALANCE = "WALLET_BALANCE"
  val TOTAL_AMOUNT = "TOTAL_AMOUNT"
  val ADVANCE_PAID = "ADVANCE_PAID"
  val AMOUNT_PENDING = "AMOUNT_PENDING"
  val ORDER_ACTIONS = "ORDER_ACTIONS"
  val MODERATED_BUYER_EXECUTE_ORDER_DOCUMENT = "MODERATED_BUYER_EXECUTE_ORDER_DOCUMENT"
  val MODERATED_SELLER_EXECUTE_ORDER_DOCUMENT = "MODERATED_SELLER_EXECUTE_ORDER_DOCUMENT"
  val NEGOTIATION_FILE_NOT_FOUND = "NEGOTIATION_FILE_NOT_FOUND"
  val CONFIRM_ALL_NEGOTIATION_TERMS_NOTE = "CONFIRM_ALL_NEGOTIATION_TERMS_NOTE"
  val UPDATE_CONTRACT_SIGNED_NOTE = "UPDATE_CONTRACT_SIGNED_NOTE"

  //ChatRoom
  val LOAD_MORE_CHATS = "LOAD_MORE_CHATS"
  val READ_BY = "READ_BY"
  val CHAT = "CHAT"

  //Transactions
  val REDEEM_FIAT_REQUESTS = "REDEEM_FIAT_REQUESTS"
  val PENDING_REDEEM_FIAT_REQUEST_LIST = "PENDING_REDEEM_FIAT_REQUEST_LIST"
  val COMPLETE_REDEEM_FIAT_REQUEST_LIST = "COMPLETE_REDEEM_FIAT_REQUEST_LIST"
  val FAILED_REDEEM_FIAT_REQUEST_LIST = "FAILED_REDEEM_FIAT_REQUEST_LIST"
  val SEND_FIAT_REQUESTS = "SEND_FIAT_REQUESTS"
  val PENDING_SEND_FIAT_REQUEST_LIST = "PENDING_SEND_FIAT_REQUEST_LIST"
  val COMPLETE_SEND_FIAT_REQUEST_LIST = "COMPLETE_SEND_FIAT_REQUEST_LIST"
  val FAILED_SEND_FIAT_REQUEST_LIST = "FAILED_SEND_FIAT_REQUEST_LIST"
  val RECEIVED_FIAT = "RECEIVED_FIAT"
  val UPDATE_TRADER_REQUEST = "UPDATE_TRADER_REQUEST"
  val UPDATE_ORGANIZATION_REQUEST = "UPDATE_ORGANIZATION_REQUEST"
  val REQUEST_COMPLETED = "REQUEST_COMPLETED"
  val ORGANIZATION_COMPLETE_ACCOUNT_KYC_NOTE = "ORGANIZATION_COMPLETE_ACCOUNT_KYC_NOTE"

  //Recent Activities
  val LOAD_MORE_ACTIVITIES = "LOAD_MORE_ACTIVITIES"
  val OPEN_TRADE_ROOM = "OPEN_TRADE_ROOM"

  val RECENT_ACTIVITY = "RECENT_ACTIVITY"
  val TRADE_ACTIVITY = "TRADE_ACTIVITY"
  val NO_TRADERS_FOUND = "NO_TRADERS_FOUND"
  val UNAVAILABLE = "UNAVAILABLE"
  val DETAILS = "DETAILS"
  val ORGANIZATION_DETAILS = "ORGANIZATION_DETAILS"
  val TRADER_DETAILS = "TRADER_DETAILS"
  val ZONE_DETAILS = "ZONE_DETAILS"
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
  val SELLER_ORGANIZATION = "SELLER_ORGANIZATION"
  val BUYER_ORGANIZATION = "BUYER_ORGANIZATION"
  val NAME = "NAME"
  val ABBREVIATION = "ABBREVIATION"
  val ID = "ID"
  val ORDER_ID = "ORDER_ID"
  val ESTABLISHMENT_DATE = "ESTABLISHMENT_DATE"
  val VERIFIED_STATUS = "VERIFIED_STATUS"
  val TRADERS = "TRADERS"
  val CURRENCY = "CURRENCY"
  val ZONE_ID = "ZONE_ID"
  val ADDRESS_LINE_1 = "ADDRESS_LINE_1"
  val ADDRESS_LINE_2 = "ADDRESS_LINE_2"
  val LANDMARK = "LANDMARK"
  val CITY = "CITY"
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
  val PERSON_FIRST_NAME = "PERSON_FIRST_NAME"
  val PERSON_LAST_NAME = "PERSON_LAST_NAME"
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
  val ISSUE_ASSET_PENDING = "ISSUE_ASSET_PENDING"
  val ASSET = "ASSET"
  val BUY_NEGOTIATION_LIST = "BUY_NEGOTIATION_LIST"
  val SELL_NEGOTIATION_LIST = "SELL_NEGOTIATION_LIST"
  val COMPLETED_NEGOTIATION_LIST = "COMPLETED_NEGOTIATION_LIST"
  val ACTIVE_NEGOTIATION_LIST = "ACTIVE_NEGOTIATION_LIST"
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
  val FIAT_TRANSACTIONS = "FIAT_TRANSACTIONS"
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
  val MESSAGES_NOT_FOUND = "MESSAGES_NOT_FOUND"
  val USER_NAME_NOT_AVAILABLE = "USER_NAME_NOT_AVAILABLE"
  val REPEAT_PASSWORD_MISMATCH = "REPEAT_PASSWORD_MISMATCH"
  val INVOICE_NUMBER = "INVOICE_NUMBER"
  val INVOICE_DATE = "INVOICE_DATE"
  val CONTRACT_NUMBER = "CONTRACT_NUMBER"
  val CONTENT_NOT_FOUND = "CONTENT_NOT_FOUND"
  val ASSET_DOCUMENTS = "ASSET_DOCUMENTS"
  val NEGOTIATION_DOCUMENTS = "NEGOTIATION_DOCUMENTS"
  val CONTRACT_UPLOADED = "CONTRACT_UPLOADED"
  val FIAT_PROOF = "FIAT_PROOF"
  val DAYS = "DAYS"
  val PER = "PER"
  val USD = "USD"
  val REVIEW_FORM_DECLARATION = "REVIEW_FORM_DECLARATION"
  val REGISTER_TO_ORGANIZATION_NOTE = "REGISTER_TO_ORGANIZATION_NOTE"
  val REGISTER_AS_ORGANIZATION_NOTE = "REGISTER_AS_ORGANIZATION_NOTE"
  val COMPLETE_ACCOUNT_KYC_NOTE = "COMPLETE_ACCOUNT_KYC_NOTE"
  val UPDATE_AND_VERIFY_CONTACT_NOTE = "UPDATE_AND_VERIFY_CONTACT_NOTE"

  val AMOUNT_REQUESTED = "AMOUNT_REQUESTED"
  val AMOUNT_RECEIVED = "AMOUNT_RECEIVED"
  val ORGANIZATION_BACKGROUND_CHECK = "ORGANIZATION_BACKGROUND_CHECK"
  val TRADER_BACKGROUND_CHECK = "TRADER_BACKGROUND_CHECK"
  val NEXT = "NEXT"
  val ASSETS = "ASSETS"
  val SUBMIT = "SUBMIT"
  val PROFILE_PICTURE = "PROFILE_PICTURE"
  val HOME = "HOME"
  val ZONE_KYC_FILES = "ZONE_KYC_FILES"
  val FIRST_NAME = "FIRST_NAME"
  val LAST_NAME = "LAST_NAME"
  val DATE_OF_BIRTH = "DATE_OF_BIRTH"
  val ID_NUMBER = "ID_NUMBER"
  val ID_TYPE = "ID_TYPE"
  val ORGANIZATION_KYC_FILES = "ORGANIZATION_KYC_FILES"
  val DELETE_UBO = "DELETE_UBO"
  val HEIGHT = "HEIGHT"
  val FEE = "FEE"
  val ASSET_QUANTITY = "ASSET_QUANTITY"
  val QUANTITY_UNIT = "QUANTITY_UNIT"
  val ASSET_PRICE = "ASSET_PRICE"
  val ASSET_PRICE_PER_UNIT = "ASSET_PRICE_PER_UNIT"
  val MODERATED = "MODERATED"
  val MODERATED_WARNING = "MODERATED_WARNING"
  val PORT_OF_LOADING = "PORT_OF_LOADING"
  val PORT_OF_DISCHARGE = "PORT_OF_DISCHARGE"
  val BILL_OF_LADING_NUMBER = "BILL_OF_LADING_NUMBER"
  val VESSEL_NAME = "VESSEL_NAME"
  val SHIPPER_NAME = "SHIPPER_NAME"
  val SHIPPER_ADDRESS = "SHIPPER_ADDRESS"
  val NOTIFY_PARTY_NAME = "NOTIFY_PARTY_NAME"
  val NOTIFY_PARTY_ADDRESS = "NOTIFY_PARTY_ADDRESS"
  val SHIPMENT_DATE = "SHIPMENT_DATE"
  val DELIVERY_TERM = "DELIVERY_TERM"
  val ACCOUNT_HOLDER_NAME = "ACCOUNT_HOLDER_NAME"
  val NICK_NAME = "NICK_NAME"
  val ACCOUNT_NUMBER = "ACCOUNT_NUMBER"
  val SWIFT_CODE = "SWIFT_CODE"
  val ADDRESS = "ADDRESS"
  val BANK_NAME = "BANK_NAME"
  val COUNTRY = "COUNTRY"
  val ZIP_CODE = "ZIP_CODE"
  val CONNECTION_ERROR = "CONNECTION_ERROR"
  val NEGOTIATION_ID = "NEGOTIATION_ID"
  val AMOUNT = "AMOUNT"
  val REJECT_ORGANIZATION_REQUEST = "REJECT_ORGANIZATION_REQUEST"
  val VERIFY_ZONE = "VERIFY_ZONE"
  val COMDEX = "COMDEX"
  val REQUEST_ID = "REQUEST_ID"
  val ONLY_SUPPLIER = "ONLY_SUPPLIER"
  val ONLY_BUYER = "ONLY_BUYER"
  val BOTH_PARTIES = "BOTH_PARTIES"
  val FAILURE = "FAILURE"
  val WARNING = "WARNING"
  val SUCCESS = "SUCCESS"
  val INFORMATION = "INFORMATION"
  val BLOCKCHAIN_ADDRESS = "BLOCKCHAIN_ADDRESS"
  val PEG_HASH = "PEG_HASH"
  val TRANSACTION_ID = "TRANSACTION_ID"
  val TRANSACTION_AMOUNT = "TRANSACTION_AMOUNT"
  val REDEEM_AMOUNT = "REDEEM_AMOUNT"
  val BUYER_ADDRESS = "BUYER_ADDRESS"
  val SELLER_ADDRESS = "SELLER_ADDRESS"
  val BROWSE = "BROWSE"
  val OR = "OR"
  val DROP_FILE = "DROP_FILE"
  val UPLOAD = "UPLOAD"
  val UPDATE = "UPDATE"
  val FOOTER_LOGO = "FOOTER_LOGO"
  val VIEW_QUOTE = "VIEW_QUOTE"
  val UNKNOWN = "UNKNOWN"

  //Member Check
  val SCAN_ID = "SCAN_ID"
  val RESULT_URL = "RESULT_URL"
  val MATCHED_NUMBER = "MATCHED_NUMBER"
  val DECISION_DETAIL = "DECISION_DETAIL"
  val CATEGORY = "CATEGORY"
  val PRIMARY_LOCATION = "PRIMARY_LOCATION"
  val MONITORING_STATUS = "MONITORING_STATUS"
  val TEXT = "TEXT"
  val MATCH_DECISION = "MATCH_DECISION"
  val ASSESSED_RISK = "ASSESSED_RISK"
  val NOT_RELEVANT = "NOT_RELEVANT"
  val RESULT_ID = "RESULT_ID"
  val UNIQUE_ID = "UNIQUE_ID"
  val MATCH_RATE = "MATCH_RATE"
  val CATEGORIES = "CATEGORIES"
  val SUBCATEGORY = "SUBCATEGORY"
  val PRIMARY_NAME = "PRIMARY_NAME"
  val PRIMARY_FIRST_NAME = "PRIMARY_FIRST_NAME"
  val PRIMARY_MIDDLE_NAME = "PRIMARY_MIDDLE_NAME"
  val PRIMARY_LAST_NAME = "PRIMARY_LAST_NAME"
  val GENERAL_INFO = "GENERAL_INFO"
  val FURTHER_INFORMATION = "FURTHER_INFORMATION"
  val XML_FURTHER_INFORMATION = "XML_FURTHER_INFORMATION"
  val ENTER_DATE = "ENTER_DATE"
  val LAST_REVIEWED = "LAST_REVIEWED"
  val DESCRIPTIONS = "DESCRIPTIONS"
  val NAME_DETAILS = "NAME_DETAILS"
  val ORIGINAL_SCRIPT_NAMES = "ORIGINAL_SCRIPT_NAMES"
  val LOCATIONS = "LOCATIONS"
  val COUNTRIES = "COUNTRIES"
  val OFFICIAL_LISTS = "OFFICIAL_LISTS"
  val ID_NUMBERS = "ID_NUMBERS"
  val SOURCES = "SOURCES"
  val LINKED_INDIVIDUALS = "LINKED_INDIVIDUALS"
  val LINKED_COMPANIES = "LINKED_COMPANIES"
  val DESCRIPTION_1 = "DESCRIPTION_1"
  val DESCRIPTION_2 = "DESCRIPTION_2"
  val DESCRIPTION_3 = "DESCRIPTION_3"
  val NAME_TYPE = "NAME_TYPE"
  val ENTITY_NAME = "ENTITY_NAME"
  val COUNTRY_TYPE = "COUNTRY_TYPE"
  val COUNTRY_VALUE = "COUNTRY_VALUE"
  val KEYWORD = "KEYWORD"
  val IS_CURRENT = "IS_CURRENT"
  val TYPE = "TYPE"
  val ID_NOTES = "ID_NOTES"
  val NUMBER = "NUMBER"
  val URL = "URL"
  val DATES = "DATES"
  val MIDDLE_NAME = "MIDDLE_NAME"
  val OTHER_CATEGORIES = "OTHER_CATEGORIES"
  val SUB_CATEGORIES = "SUB_CATEGORIES"
  val GENDER = "GENDER"
  val DECEASED = "DECEASED"
  val POSITION = "POSITION"
  val DECEASED_DATE = "DECEASED_DATE"
  val PLACE_OF_BIRTH = "PLACE_OF_BIRTH"
  val IMAGE = "IMAGE"
  val ROLES = "ROLES"
  val FROM = "FROM"
  val TO = "TO"
  val IMPORTANT_DATES = "IMPORTANT_DATES"
  val DATE_TYPE = "DATE_TYPE"
  val DATE_VALUE = "DATE_VALUE"
  val MEMBER_CHECK = "MEMBER_CHECK"
  val MEMBER_CHECK_CORPORATE_SCAN_RESPONSE = "MEMBER_CHECK_CORPORATE_SCAN_RESPONSE"
  val MEMBER_CHECK_CORPORATE_SCAN_RESULT = "MEMBER_CHECK_CORPORATE_SCAN_RESULT"
  val MEMBER_CHECK_SCAN_RESPONSE = "MEMBER_CHECK_CORPORATE_SCAN_RESULT"
  val MEMBER_CHECK_SCAN_RESULT = "MEMBER_CHECK_SCAN_RESULT"
  val MEMBER_CHECK_VESSEL_SCAN_RESPONSE = "MEMBER_CHECK_VESSEL_SCAN_RESPONSE"
  val MEMBER_CHECK_VESSEL_SCAN_RESULT = "MEMBER_CHECK_VESSEL_SCAN_RESULT"

  //docusign
  val PLEASE_SIGN_THIS_DOCUMENT = "PLEASE_SIGN_THIS_DOCUMENT"
  val SEND_FOR_SIGNATURE = "SEND_FOR_SIGNATURE"
  val E_SIGN = "E_SIGN"
  val AUTHORIZE_DOCUSIGN = "AUTHORIZE_DOCUSIGN"
  val SIGNED = "SIGNED"
  val DOCUMENT_SENT_FOR_SIGNING = "DOCUMENT_SENT_FOR_SIGNING"
  val DOCUMENT_SIGNED = "DOCUMENT_SIGNED"
  val UNEXPECTED_EVENT = "UNEXPECTED_EVENT"
  val CONTRACT_SIGNATURE_PENDING = "CONTRACT_SIGNATURE_PENDING"
  val SELECT_ZONE = "SELECT_ZONE"
  val MNEMONIC_SUCCESS_MESSAGE = "MNEMONIC_SUCCESS_MESSAGE"

  val LOGOUT_CONFIRMATION = "LOGOUT_CONFIRMATION"
  val UPLOAD_DOCUMENTS_LEGEND = "UPLOAD_DOCUMENTS_LEGEND"
  val UPLOAD_ASSET_DOCUMENTS_NOTE = "UPLOAD_ASSET_DOCUMENTS_NOTE"
  val FORGOT_PASSWORD_NOTE = "FORGOT_PASSWORD_NOTE"
  val UPLOAD_CONTRACT_NOTE = "UPLOAD_CONTRACT_NOTE"
  val EMAIL_OTP_NOTE = "EMAIL_OTP_NOTE"
  val MOBILE_OTP_NOTE = "MOBILE_OTP_NOTE"
  val REGISTER_TO_ORGANIZATION = "REGISTER_TO_ORGANIZATION"
  val REGISTER_AS_ORGANIZATION = "REGISTER_AS_ORGANIZATION"
  val COPY = "COPY"
  val DOCUMENTS_NOT_FOUND = "DOCUMENTS_NOT_FOUND"

  //wallex
  val WALLEX = "WALLEX"
  val ORGANIZATION_WALLEX_ACCOUNT_DETAIL ="ORGANIZATION_WALLEX_ACCOUNT_DETAIL"
  val WALLEX_ID ="WALLEX_ID"
  val UPLOAD_WALLEX_KYC ="UPLOAD_WALLEX_KYC"
  val WALLEX_PAYMENT_APP ="WALLEX_PAYMENT_APP"
  val SUPPORTING_DOC_REQUIRED ="SUPPORTING_DOC_REQUIRED"
  val CURRENCY_PAIR ="CURRENCY_PAIR"
  val BUY_CURRENCY = "BUY_CURRENCY"
  val SELL_CURRENCY ="SELL_CURRENCY"
  val BUY_AMOUNT = "BUY_AMOUNT"
  val SELL_AMOUNT = "SELL_AMOUNT"
  val FIXED_SIDE = "FIXED_SIDE"
  val RATE = "RATE"
  val PARTNER_RATE ="PARTNER_RATE"
  val PARTNER_BUY_AMOUNT = "PARTNER_BUY_AMOUNT"
  val PARTNER_SELL_AMOUNT = "PARTNER_SELL_AMOUNT"
  val PARTNER_PAYMENT_FEE = "PARTNER_PAYMENT_FEE"
  val EXPIRES_AT = "EXPIRES_AT"
  val QUOTE_ID ="QUOTE_ID"
  val CONVERSION_FEE = "CONVERSION_FEE"
  val PAYMENT_FEE = "PAYMENT_FEE"
  val TOTAL_FEE = "TOTAL_FEE"
  val PAYMENT_CHANNEL ="PAYMENT_CHANNEL"
  val BANK_CHARGE = "BANK_CHARGE"
  val QUOTE_RESPONSE = "QUOTE_RESPONSE"
  val ORGANIZATION_WALLEX_BENEFICIARY_DETAIL ="ORGANIZATION_WALLEX_BENEFICIARY_DETAIL"
  val BENEFICIARY_ID ="BENEFICIARY_ID"
  val COMPANY_NAME ="COMPANY_NAME"
  val ACCOUNT_TYPE ="ACCOUNT_TYPE"
  val ABA ="ABA"
  val WALLEX_TOTAL_AMOUNT= "WALLEX_TOTAL_AMOUNT"
  val WALLEX_WALLET_TRANSFER= "WALLEX_WALLET_TRANSFER"
  val SENDER_ACCOUNT_ID = "SENDER_ACCOUNT_ID"
  val RECEIVER_ACCOUNT_ID = "RECEIVER_ACCOUNT_ID"
  val PURPOSE_OF_TRANSFER = "PURPOSE_OF_TRANSFER"
  val REMARKS = "REMARKS"
  val CREATED_AT = "CREATED_AT"
  val PAY_TO_BENEFICIARY = "PAY_TO_BENEFICIARY"
  val COLLECTION_ACCOUNT = "COLLECTION_ACCOUNT"
  val ACCOUNT_NUMBER_TYPE = "ACCOUNT_NUMBER_TYPE"
  val WALLEX_KYC_DOCUMENTS = "WALLEX_KYC_DOCUMENTS"
  val SUBMIT_DOCUMENT = "SUBMIT_DOCUMENT"
  val SUBMIT_DOCUMENT_TERMS = "SUBMIT_DOCUMENT_TERMS"
  val ON_BEHALF_OF = "ON_BEHALF_OF"
  val DOCUMENT_TYPE = "DOCUMENT_TYPE"
  val WALLEX_ACCOUNT_CREATE = "WALLEX_ACCOUNT_CREATE"
  val WALLEX_POWERED_BY = "WALLEX_POWERED_BY"
  val WALLEX_LOGO = "WALLEX_LOGO"
  val WALLEX_COMPLIANCE_MESSAGE = "WALLEX_COMPLIANCE_MESSAGE"

}
