package constants

object File {
  val IMAGE = "IMAGES"
  val DOCUMENT = "DOCUMENT"
  val FILE = "FILE"

  val PDF = "pdf"
  val TXT = "txt"
  val JPG = "jpg"
  val PNG = "png"
  val JPEG = "jpeg"
  val DOC = "doc"
  val DOCX = "docx"

  val BANK_DETAILS = "BANK_DETAILS"
  val IDENTIFICATION = "IDENTIFICATION"
  val ADDRESS = "ADDRESS"
  val UNKNOWN_TYPE = "UNKNOWN_TYPE"
  val CONTRACT = "CONTRACT"
  val OBL = "OBL"
  val INVOICE = "INVOICE"
  val PACKING_LIST = "PACKING_LIST"
  val COO = "COO"
  val COA = "COA"
  val OTHER = "OTHER"

  val BUYER_CONTRACT = "BUYER_CONTRACT"
  val SELLER_CONTRACT = "SELLER_CONTRACT"
  val AWB_PROOF = "AWB_PROOF"
  val FIAT_PROOF = "FIAT_PROOF"

  val PROFILE_PICTURE = "PROFILE_PICTURE"

  val BANK_ACCOUNT_DETAIL = "BANK_ACCOUNT_DETAIL"
  val LATEST_AUDITED_FINANCIAL_REPORT = "LATEST_AUDITED_FINANCIAL_REPORT"
  val LAST_YEAR_AUDITED_FINANCIAL_REPORT = "LAST_YEAR_AUDITED_FINANCIAL_REPORT"
  val MANAGEMENT = "MANAGEMENT"
  val ACRA = "ACRA"
  val SHARE_STRUCTURE = "SHARE_STRUCTURE"
  val ADMIN_PROFILE_IDENTIFICATION = "ADMIN_PROFILE_IDENTIFICATION"

  val TRADER_ASSET_DOCUMENT_TYPES = Seq(CONTRACT, OBL, INVOICE, PACKING_LIST, COO, COA, OTHER)
  val TRADER_ASSET_DOCUMENT_TYPES_UPLOAD_PAGE = Seq(CONTRACT, PACKING_LIST, COO, COA, OTHER)
  val TRADER_ASSET_DOCUMENT_TYPES_UPLOAD_PAGE_NEXT = Seq(CONTRACT, PACKING_LIST, COO, COA)
  val ORGANIZATION_KYC_DOCUMENT_TYPES = Seq(BANK_ACCOUNT_DETAIL, LATEST_AUDITED_FINANCIAL_REPORT, LAST_YEAR_AUDITED_FINANCIAL_REPORT, MANAGEMENT, ACRA, SHARE_STRUCTURE, ADMIN_PROFILE_IDENTIFICATION)
}
