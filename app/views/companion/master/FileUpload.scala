package views.companion.master

import play.api.data.Forms._
import play.api.data._

object FileUpload {
  def form = Form(
    mapping(
      "resumableChunkNumber" -> number,
      "resumableChunkSize" -> number,
      "resumableTotalSize" -> longNumber,
      "resumableIdentifier" -> nonEmptyText,
      "resumableFilename" -> nonEmptyText
    )(FileUploadInfo.apply)(FileUploadInfo.unapply))

  case class FileUploadInfo(resumableChunkNumber: Int, resumableChunkSize: Int, resumableTotalSize: Long, resumableIdentifier: String, resumableFilename: String) {
    def totalChunks: Double = Math.ceil(resumableTotalSize.toDouble / resumableChunkSize.toDouble)
  }

}