package constants

import constants.File.INSURANCE

object File {

  val KEY_FILE = "file"

  val IMAGE = "IMAGES"
  val DOCUMENT = "DOCUMENT"
  val FILE = "FILE"

  val PDF = "pdf"
  val TXT = "txt"
  val JPG = "JPG"
  val PNG = "PNG"
  val JPEG = "JPEG"
  val JPG_LOWER_CASE = "jpg"
  val PNG_LOWER_CASE = "png"
  val JPEG_LOWER_CASE = "jpeg"
  val DOC = "doc"
  val DOCX = "docx"

  val IDENTIFICATION = "IDENTIFICATION"
  val UNKNOWN_TYPE = "UNKNOWN_TYPE"
  val CONTRACT = "CONTRACT"
  val OBL = "OBL"
  val INVOICE = "INVOICE"
  val INSURANCE = "INSURANCE"
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
  val ORGANIZATION_AGREEMENT = "ORGANIZATION_AGREEMENT"
  val INCORPORATION_DOCUMENT = "INCORPORATION_DOCUMENT"
  val TRADER_AGREEMENT = "TRADER_AGREEMENT"
  val EMPLOYMENT_PROOF = "EMPLOYMENT_PROOF"

  val TRADER_IDENTIFICATION = "TRADER_IDENTIFICATION"

  val TRADER_WORLD_CHECK = "TRADER_WORLD_CHECK"
  val ORGANIZATION_WORLD_CHECK = "ORGANIZATION_WORLD_CHECK"

  val TRADER_ASSET_DOCUMENT_TYPES: Seq[String] = Seq(OBL, PACKING_LIST, COO, COA, OTHER)
  val TRADER_ASSET_DOCUMENT_TYPES_UPLOAD_PAGE: Seq[String] = Seq(CONTRACT, PACKING_LIST, COO, COA, OTHER)
  val TRADER_ASSET_DOCUMENT_TYPES_UPLOAD_PAGE_NEXT: Seq[String] = Seq(CONTRACT, PACKING_LIST, COO, COA)
  val TRADER_NEGOTIATION_DOCUMENTS: Seq[String] = Seq(INVOICE, INSURANCE, OTHER)

  //Seq
  val ZONE_KYC_DOCUMENT_TYPES: Seq[String] = Seq(IDENTIFICATION, BANK_ACCOUNT_DETAIL)
  val ORGANIZATION_KYC_DOCUMENT_TYPES: Seq[String] = Seq(ACRA, INCORPORATION_DOCUMENT)
  val TRADER_KYC_DOCUMENT_TYPES: Seq[String] = Seq(EMPLOYMENT_PROOF)
  val TRADER_BACKGROUND_CHECK_DOCUMENT_TYPES: Seq[String] = Seq(TRADER_WORLD_CHECK)
  val ORGANIZATION_BACKGROUND_CHECK_DOCUMENT_TYPES: Seq[String] = Seq(ORGANIZATION_WORLD_CHECK)
}
