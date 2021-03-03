package models.kycCheck.worldcheck

import models.Trait.{Document, Logged}
import models.master.AccountKYC

import java.sql.Timestamp

case class WorldCheckKycFiles ((id: String, documentType: String, fileName: String, file: Option[Array[Byte]], status: Option[Boolean] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Document[AccountKYC] with Logged {

  def updateFileName(newFileName: String): AccountKYC = copy(fileName = newFileName)

  def updateFile(newFile: Option[Array[Byte]]): AccountKYC = copy(file = newFile)

  def updateStatus(status: Option[Boolean]): AccountKYC = copy(status = status)

}
