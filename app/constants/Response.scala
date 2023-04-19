package constants

import exceptions.BaseException
import play.api.Logger

object Response {

  lazy val PREFIX = "RESPONSE."
  lazy val FAILURE_PREFIX = "FAILURE."
  lazy val WARNING_PREFIX = "WARNING."
  lazy val SUCCESS_PREFIX = "SUCCESS."
  lazy val INFO_PREFIX = "INFO."
  lazy val LOG_PREFIX = "LOG."
  val NULL_POINTER_EXCEPTION = new Failure("NULL_POINTER_EXCEPTION")
  val INVALID_FILE_PATH_EXCEPTION = new Failure("INVALID_FILE_PATH_EXCEPTION")
  val FILE_SECURITY_EXCEPTION = new Failure("FILE_SECURITY_EXCEPTION")
  val GENERIC_EXCEPTION = new Failure("GENERIC_EXCEPTION")
  val GENERIC_JSON_EXCEPTION = new Failure("GENERIC_JSON_EXCEPTION")
  val I_O_EXCEPTION = new Failure("I_O_EXCEPTION")
  val FILE_NOT_FOUND_EXCEPTION = new Failure("FILE_NOT_FOUND_EXCEPTION")
  val FILE_ILLEGAL_ARGUMENT_EXCEPTION = new Failure("FILE_ILLEGAL_ARGUMENT_EXCEPTION")
  val CLASS_CAST_EXCEPTION = new Failure("CLASS_CAST_EXCEPTION")
  val FILE_UNSUPPORTED_OPERATION_EXCEPTION = new Failure("FILE_UNSUPPORTED_OPERATION_EXCEPTION")


  //Failure- for telling that something failed
  val FAILURE = new Failure("FAILURE")
  val NOT_SUPPORTED = new Failure("NOT_SUPPORTED")
  val NO_SUCH_ELEMENT_EXCEPTION = new Failure("NO_SUCH_ELEMENT_EXCEPTION")
  val NO_SUCH_FILE_EXCEPTION = new Failure("NO_SUCH_FILE_EXCEPTION")
  val NO_SUCH_DOCUMENT_TYPE_EXCEPTION = new Failure("NO_SUCH_DOCUMENT_TYPE_EXCEPTION")
  val PSQL_EXCEPTION = new Failure("PSQL_EXCEPTION")
  val JSON_PARSE_EXCEPTION = new Failure("JSON_PARSE_EXCEPTION")
  val JSON_MAPPING_EXCEPTION = new Failure("JSON_MAPPING_EXCEPTION")
  val CONNECT_EXCEPTION = new Failure("CONNECT_EXCEPTION")
  val ILLEGAL_STATE_EXCEPTION = new Failure("ILLEGAL_STATE_EXCEPTION")
  val NO_RESPONSE = new Failure("NO_RESPONSE")
  val INVALID_PAGE_NUMBER = new Failure("INVALID_PAGE_NUMBER")
  val INVALID_BASE64_ENCODING = new Failure("INVALID_BASE64_ENCODING")
  val NO_SUCH_PUBLIC_KEY_TYPE = new Failure("NO_SUCH_PUBLIC_KEY_TYPE")
  val NO_SUCH_PROPOSAL_CONTENT_TYPE = new Failure("NO_SUCH_PROPOSAL_CONTENT_TYPE")
  val INVALID_ACCOUNT_ADDRESS = new Failure("INVALID_ACCOUNT_ADDRESS")
  val INVALID_OPERATOR_ADDRESS = new Failure("INVALID_OPERATOR_ADDRESS")
  val INVALID_BECH32_ADDRESS = new Failure("INVALID_BECH32_ADDRESS")

  val INDEX_OUT_OF_BOUND = new Failure("INDEX_OUT_OF_BOUND")
  val JSON_UNMARSHALLING_ERROR = new Failure("JSON_UNMARSHALLING_ERROR")
  val DATE_FORMAT_ERROR = new Failure("DATE_FORMAT_ERROR")
  val ACCOUNT_TYPE_NOT_FOUND = new Failure("ACCOUNT_TYPE_NOT_FOUND")
  val ACCOUNT_NOT_FOUND = new Failure("ACCOUNT_NOT_FOUND")
  val ACCOUNT_UPSERT_FAILED = new Failure("ACCOUNT_UPSERT_FAILED")
  val ACCOUNT_INSERT_FAILED = new Failure("ACCOUNT_INSERT_FAILED")
  val BALANCE_NOT_FOUND = new Failure("BALANCE_NOT_FOUND")
  val BALANCE_UPSERT_FAILED = new Failure("BALANCE_UPSERT_FAILED")
  val BALANCE_INSERT_FAILED = new Failure("BALANCE_INSERT_FAILED")
  val TRANSACTION_TYPE_NOT_FOUND = new Failure("TRANSACTION_TYPE_NOT_FOUND")
  val TRANSACTION_STRUCTURE_CHANGED = new Failure("TRANSACTION_STRUCTURE_CHANGED")
  val NUMBER_FORMAT_EXCEPTION = new Failure("NUMBER_FORMAT_EXCEPTION")
  val PRECISION_MORE_THAN_REQUIRED = new Failure("PRECISION_MORE_THAN_REQUIRED")
  val CONTENT_CONVERSION_ERROR = new Failure("CONTENT_CONVERSION_ERROR")
  val CRYPTO_TOKEN_INSERT_FAILED = new Failure("CRYPTO_TOKEN_INSERT_FAILED")
  val CRYPTO_TOKEN_UPDATE_FAILED = new Failure("CRYPTO_TOKEN_UPDATE_FAILED")
  val CRYPTO_TOKEN_UPSERT_FAILED = new Failure("CRYPTO_TOKEN_UPSERT_FAILED")
  val CRYPTO_TOKEN_NOT_FOUND = new Failure("CRYPTO_TOKEN_NOT_FOUND")
  val DOCUMENT_UPLOAD_FAILED = new Failure("DOCUMENT_UPLOAD_FAILED")
  val DOCUMENT_UPDATE_FAILED = new Failure("DOCUMENT_UPDATE_FAILED")
  val TRANSACTION_INSERT_FAILED = new Failure("TRANSACTION_INSERT_FAILED")
  val TRANSACTION_UPSERT_FAILED = new Failure("TRANSACTION_UPSERT_FAILED")
  val DELEGATION_INSERT_FAILED = new Failure("DELEGATION_INSERT_FAILED")
  val DELEGATION_UPSERT_FAILED = new Failure("DELEGATION_UPSERT_FAILED")
  val DELEGATION_UPDATE_FAILED = new Failure("DELEGATION_UPDATE_FAILED")
  val DELEGATION_DELETE_FAILED = new Failure("DELEGATION_DELETE_FAILED")
  val DELEGATION_NOT_FOUND = new Failure("DELEGATION_NOT_FOUND")
  val STAKING_TOKEN_NOT_FOUND = new Failure("STAKING_TOKEN_NOT_FOUND")
  val TRANSACTION_COUNTER_INSERT_FAILED = new Failure("TRANSACTION_COUNTER_INSERT_FAILED")
  val TRANSACTION_COUNTER_UPSERT_FAILED = new Failure("TRANSACTION_COUNTER_UPSERT_FAILED")
  val REDELEGATION_NOT_FOUND = new Failure("REDELEGATION_NOT_FOUND")
  val REDELEGATION_INSERT_FAILED = new Failure("REDELEGATION_INSERT_FAILED")
  val REDELEGATION_UPSERT_FAILED = new Failure("REDELEGATION_UPSERT_FAILED")
  val REDELEGATION_UPDATE_FAILED = new Failure("REDELEGATION_UPDATE_FAILED")
  val REDELEGATION_DELETE_FAILED = new Failure("REDELEGATION_DELETE_FAILED")
  val REDELEGATION_RESPONSE_NOT_FOUND = new Failure("REDELEGATION_RESPONSE_NOT_FOUND")
  val UNDELEGATION_INSERT_FAILED = new Failure("UNDELEGATION_INSERT_FAILED")
  val UNDELEGATION_UPSERT_FAILED = new Failure("UNDELEGATION_UPSERT_FAILED")
  val UNDELEGATION_UPDATE_FAILED = new Failure("UNDELEGATION_UPDATE_FAILED")
  val UNDELEGATION_DELETE_FAILED = new Failure("UNDELEGATION_DELETE_FAILED")
  val UNDELEGATION_NOT_FOUND = new Failure("UNDELEGATION_NOT_FOUND")
  val TIME_NOT_FOUND = new Failure("TIME_NOT_FOUND")
  val SEARCH_QUERY_NOT_FOUND = new Failure("SEARCH_QUERY_NOT_FOUND")
  val SIGNING_INFO_INSERT_FAILED = new Failure("SIGNING_INFO_INSERT_FAILED")
  val SIGNING_INFO_UPSERT_FAILED = new Failure("SIGNING_INFO_UPSERT_FAILED")
  val SIGNING_INFO_UPDATE_FAILED = new Failure("SIGNING_INFO_UPDATE_FAILED")
  val SIGNING_INFO_DELETE_FAILED = new Failure("SIGNING_INFO_DELETE_FAILED")
  val SIGNING_INFO_NOT_FOUND = new Failure("SIGNING_INFO_NOT_FOUND")
  val KEY_BASE_ACCOUNT_INSERT_FAILED = new Failure("KEY_BASE_ACCOUNT_INSERT_FAILED")
  val KEY_BASE_ACCOUNT_UPDATE_FAILED = new Failure("KEY_BASE_ACCOUNT_UPDATE_FAILED")
  val KEY_BASE_ACCOUNTS_UPDATE_FAILED = new Failure("KEY_BASE_ACCOUNTS_UPDATE_FAILED")
  val KEY_BASE_ACCOUNT_UPSERT_FAILED = new Failure("KEY_BASE_ACCOUNT_UPSERT_FAILED")
  val KEY_BASE_ACCOUNT_NOT_FOUND = new Failure("KEY_BASE_ACCOUNT_NOT_FOUND")
  val BLOCK_INSERT_FAILED = new Failure("BLOCK_INSERT_FAILED")
  val BLOCK_UPSERT_FAILED = new Failure("BLOCK_UPSERT_FAILED")
  val BLOCK_NOT_FOUND = new Failure("BLOCK_NOT_FOUND")
  val TRANSACTION_NOT_FOUND = new Failure("TRANSACTION_NOT_FOUND")
  val VALIDATOR_NOT_FOUND = new Failure("VALIDATOR_NOT_FOUND")
  val DATA_TYPE_NOT_FOUND = new Failure("DATA_TYPE_NOT_FOUND")
  val TRANSACTION_PROCESSING_FAILED = new Failure("TRANSACTION_PROCESSING_FAILED")
  val BLOCK_QUERY_FAILED = new Failure("BLOCK_QUERY_FAILED")
  val TRANSACTION_BY_HEIGHT_QUERY_FAILED = new Failure("TRANSACTION_BY_HEIGHT_QUERY_FAILED")
  val TRANSACTION_BY_HASH_QUERY_FAILED = new Failure("TRANSACTION_BY_HASH_QUERY_FAILED")
  val INVALID_DATA_VALUE = new Failure("INVALID_DATA_VALUE")
  val INVALID_DATA_TYPE = new Failure("INVALID_DATA_TYPE")
  val INVALID_MNEMONICS = new Failure("INVALID_MNEMONICS")
  val KEY_GENERATION_FAILED = new Failure("KEY_GENERATION_FAILED")
  val INVALID_VALIDATOR_OR_DELEGATION_OR_SHARES = new Failure("INVALID_VALIDATOR_OR_DELEGATION_OR_SHARES")
  val TENDERMINT_EVIDENCE_NOT_FOUND = new Failure("TENDERMINT_EVIDENCE_NOT_FOUND")
  val UNKNOWN_TENDERMINT_EVIDENCE_TYPE = new Failure("UNKNOWN_TENDERMINT_EVIDENCE_TYPE")
  val SLASHING_EVENT_ADDRESS_NOT_FOUND = new Failure("SLASHING_EVENT_ADDRESS_NOT_FOUND")
  val SLASHING_EVENT_REASON_ATTRIBUTE_VALUE_NOT_FOUND = new Failure("SLASHING_EVENT_REASON_ATTRIBUTE_VALUE_NOT_FOUND")
  val EVENT_PROPOSAL_ID_NOT_FOUND = new Failure("EVENT_PROPOSAL_ID_NOT_FOUND")
  val IBC_BALANCE_UPDATE_FAILED = new Failure("IBC_BALANCE_UPDATE_FAILED")
  val INVALID_UNBONDING_COMPLETION_EVENT = new Failure("INVALID_UNBONDING_COMPLETION_EVENT")
  val INVALID_REDELEGATION_COMPLETION_EVENT = new Failure("INVALID_REDELEGATION_COMPLETION_EVENT")
  val LIVENESS_EVENT_CONSENSUS_ADDRESS_NOT_FOUND: Failure = new Failure("LIVENESS_EVENT_CONSENSUS_ADDRESS_NOT_FOUND")
  val BLOCKCHAIN_CONNECTION_LOST = new Failure("BLOCKCHAIN_CONNECTION_LOST")
  val INVALID_SIGNATURE = new Failure("INVALID_SIGNATURE")

  val FILE_UPLOAD_ERROR = new Failure("FILE_UPLOAD_ERROR")
  val KEY_STORE_ERROR = new Failure("KEY_STORE_ERROR")
  val PROPOSAL_INSERT_FAILED = new Failure("PROPOSAL_INSERT_FAILED")
  val PROPOSAL_UPSERT_FAILED = new Failure("PROPOSAL_UPSERT_FAILED")
  val PROPOSAL_NOT_FOUND = new Failure("PROPOSAL_NOT_FOUND")
  val CRYPTO_TOKEN_TICKER_NOT_FOUND = new Failure("CRYPTO_TOKEN_TICKER_NOT_FOUND")
  val GRANT_AUTHORIZATION_RESPONSE_STRUCTURE_CHANGED = new Failure("GRANT_AUTHORIZATION_RESPONSE_STRUCTURE_CHANGED")
  val UNKNOWN_GRANT_AUTHORIZATION_RESPONSE_STRUCTURE = new Failure("UNKNOWN_GRANT_AUTHORIZATION_RESPONSE_STRUCTURE")
  val FEE_ALLOWANCE_RESPONSE_STRUCTURE_CHANGED = new Failure("FEE_ALLOWANCE_RESPONSE_STRUCTURE_CHANGED")
  val UNKNOWN_FEE_ALLOWANCE_RESPONSE_STRUCTURE = new Failure("UNKNOWN_FEE_ALLOWANCE_RESPONSE_STRUCTURE")
  val UNKNOWN_TRANSACTION_MESSAGE = new Failure("UNKNOWN_TRANSACTION_MESSAGE")
  val COIN_AMOUNT_NEGATIVE = new Failure("COIN_AMOUNT_NEGATIVE")
  val ARITHMETIC_OPERATION_ON_DIFFERENT_COIN = new Failure("ARITHMETIC_OPERATION_ON_DIFFERENT_COIN")
  val AUTHORIZATION_NOT_FOUND = new Failure("AUTHORIZATION_NOT_FOUND")
  val AUTHORIZATION_INSERT_FAILED = new Failure("AUTHORIZATION_INSERT_FAILED")
  val AUTHORIZATION_UPSERT_FAILED = new Failure("AUTHORIZATION_UPSERT_FAILED")
  val AUTHORIZATION_DELETE_FAILED = new Failure("AUTHORIZATION_DELETE_FAILED")
  val GRANT_AUTHORIZATION_NOT_FOUND = new Failure("GRANT_AUTHORIZATION_NOT_FOUND")
  val FEE_GRANT_INSERT_FAILED = new Failure("FEE_GRANT_INSERT_FAILED")
  val FEE_GRANT_UPSERT_FAILED = new Failure("FEE_GRANT_UPSERT_FAILED")
  val FEE_GRANT_DELETE_FAILED = new Failure("FEE_GRANT_DELETE_FAILED")
  val PARAMETER_INSERT_FAILED = new Failure("PARAMETER_INSERT_FAILED")
  val PARAMETER_UPSERT_FAILED = new Failure("PARAMETER_UPSERT_FAILED")
  val PARAMETER_DELETE_FAILED = new Failure("PARAMETER_DELETE_FAILED")
  val PARAMETER_NOT_FOUND = new Failure("PARAMETER_NOT_FOUND")
  val EMPTY_QUERY = new Failure("EMPTY_QUERY")

  class Failure(private val response: String) {
    val message: String = PREFIX + FAILURE_PREFIX + response
    val logMessage: String = LOG_PREFIX + response

    def throwBaseException(exception: Exception = null)(implicit module: String, logger: Logger) = throw new BaseException(this, exception)
  }

  class Warning(val response: String) {
    val message: String = PREFIX + WARNING_PREFIX + response
  }

  class Success(val response: String) {
    val message: String = PREFIX + SUCCESS_PREFIX + response
  }

  class Info(val response: String) {
    val message: String = PREFIX + INFO_PREFIX + response
  }

}
