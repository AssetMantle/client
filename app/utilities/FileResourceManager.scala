package utilities

import javax.inject.Inject
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

class FileResourceManager @Inject()()(implicit exec: ExecutionContext, configuration: Configuration){

  private implicit val module: String = constants.Module.FILE_RESOURCE_MANAGER

  private val logger: Logger = Logger(this.getClass)

  private val uploadAccountKycBankDetailsPath = configuration.get[String]("upload.accountKYCsBankDetailsPath")

  private val uploadAccountKycIdentificationPath = configuration.get[String]("upload.accountKYCsIdentificationPath")

  private val uploadZoneKycBankDetailsPath = configuration.get[String]("upload.zoneKYCsBankDetailsPath")

  private val uploadZoneKycIdentificationPath = configuration.get[String]("upload.zoneKYCsIdentificationPath")

  private val uploadOrganizationKycBankDetailsPath = configuration.get[String]("upload.organizationKYCsBankDetailsPath")

  private val uploadOrganizationKycIdentificationPath = configuration.get[String]("upload.organizationKYCsIdentificationPath")

  def getAccountFilePath(documentType: String): String = {
    documentType match {
      case constants.File.BANK_DETAILS => uploadAccountKycBankDetailsPath
      case constants.File.IDENTIFICATION => uploadAccountKycIdentificationPath
      case _ => constants.File.UNKNOWN_TYPE
    }
  }

  def getZoneFilePath(documentType: String): String = {
    documentType match {
      case constants.File.BANK_DETAILS => uploadZoneKycBankDetailsPath
      case constants.File.IDENTIFICATION => uploadZoneKycIdentificationPath
      case _ => constants.File.UNKNOWN_TYPE
    }
  }

  def getOrganizationFilePath(documentType: String): String = {
    documentType match {
      case constants.File.BANK_DETAILS => uploadOrganizationKycBankDetailsPath
      case constants.File.IDENTIFICATION => uploadOrganizationKycIdentificationPath
      case _ => constants.File.UNKNOWN_TYPE
    }
  }
}
