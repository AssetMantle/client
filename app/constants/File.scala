package constants

object File {

  val KEY_FILE = "file"

  val IMAGE = "IMAGES"
  val DOCUMENT = "DOCUMENT"
  val FILE = "FILE"

  //File Extensions
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

  object Account {
    val PROFILE_PICTURE = "PROFILE_PICTURE"
  }

  object AccountKYC {
    val IDENTIFICATION = "IDENTIFICATION"
  }

  object Asset {
    val BILL_OF_LADING = "BILL_OF_LADING"
    val COO = "COO"
    val COA = "COA"
  }

  object Negotiation {
    val BILL_OF_EXCHANGE = "BILL_OF_EXCHANGE"
    val INVOICE = "INVOICE"
    val INSURANCE = "INSURANCE"
    val CONTRACT = "CONTRACT"
    val FIAT_PROOF = "FIAT_PROOF"
    val OTHERS = "OTHERS"
  }

  object OrganizationKYC {
    val ACRA = "ACRA"
    val BOARD_RESOLUTION = "BOARD_RESOLUTION"
  }

  object WorldCheck {
    val TRADER_WORLD_CHECK = "TRADER_WORLD_CHECK"
    val ORGANIZATION_WORLD_CHECK = "ORGANIZATION_WORLD_CHECK"
  }

  object ZoneKYC {
    val BANK_ACCOUNT_DETAIL = "BANK_ACCOUNT_DETAIL"
    val IDENTIFICATION = "IDENTIFICATION"
  }

  object Wallex {
    val NATIONAL_IDENTITY = "national_identity"
    val COMPANY_PROOF = "company_proof"
    val COMPANY_PROOF_ADDRESS = "company_address_proof"
    val NRIC = "nric"
    val PASSPORT = "passport"
    val KTP = "ktp"
    val EMPLOYMENT_PASS = "employment_pass"
    val S_PASS = "s_pass"
    val WORK_PERMIT = "work_permit"
    val PHOTO = "photo"
    val BANK_STATEMENT = "bank_statement"
    val UTILITY_BILL = "utility_bill"
    val PHONE_BILL = "phone_bill"
    val TAX_BILL = "tax_bill"
    val FAMILY_CARD = "family_card"
    val IDENTITY_REPORT = "identity_report"
    val FDW = "fdw"


  }

  //Seq
  val ZONE_KYC_DOCUMENT_TYPES: Seq[String] = Seq(ZoneKYC.IDENTIFICATION, ZoneKYC.BANK_ACCOUNT_DETAIL)
  val ORGANIZATION_KYC_DOCUMENT_TYPES: Seq[String] = Seq(OrganizationKYC.ACRA, OrganizationKYC.BOARD_RESOLUTION)
  val TRADER_BACKGROUND_CHECK_DOCUMENT_TYPES: Seq[String] = Seq(WorldCheck.TRADER_WORLD_CHECK)
  val ORGANIZATION_BACKGROUND_CHECK_DOCUMENT_TYPES: Seq[String] = Seq(WorldCheck.ORGANIZATION_WORLD_CHECK)
  val ASSET_DOCUMENTS: Seq[String] = Seq(Asset.BILL_OF_LADING, Asset.COO, Asset.COA)
  val NEGOTIATION_DOCUMENTS: Seq[String] = Seq(Negotiation.BILL_OF_EXCHANGE, Negotiation.INVOICE, Negotiation.CONTRACT)
}
