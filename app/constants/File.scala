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

  object WorldCheck {
    val TRADER_WORLD_CHECK = "TRADER_WORLD_CHECK"
    val ORGANIZATION_WORLD_CHECK = "ORGANIZATION_WORLD_CHECK"
  }



  //Seq
  val TRADER_BACKGROUND_CHECK_DOCUMENT_TYPES: Seq[String] = Seq(WorldCheck.TRADER_WORLD_CHECK)
  val ORGANIZATION_BACKGROUND_CHECK_DOCUMENT_TYPES: Seq[String] = Seq(WorldCheck.ORGANIZATION_WORLD_CHECK)
}
