package models.master

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class OrganizationKYC(id: String, documentType: String, status: Boolean, fileName: String, file: Array[Byte])

@Singleton
class OrganizationKYCs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ORGANIZATION_KYC

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val organizationKYCTable = TableQuery[OrganizationKYCTable]

  private def add(organizationKYC: OrganizationKYC): Future[String] = db.run(organizationKYCTable returning organizationKYCTable.map(_.id) += organizationKYC)

  private def findByIdDocumentType(id: String, documentType: String): Future[OrganizationKYC] = db.run(organizationKYCTable.filter(_.id === id).filter(_.documentType === documentType).result.head)

  private def deleteByIdDocumentType(id: String, documentType: String) = db.run(organizationKYCTable.filter(_.id === id).filter(_.documentType === documentType).delete)

  private[models] class OrganizationKYCTable(tag: Tag) extends Table[OrganizationKYC](tag, "OrganizationKYC") {

    def * = (id, documentType, status, fileName, file) <> (OrganizationKYC.tupled, OrganizationKYC.unapply)

    def ? = (id.?, documentType.?, status.?, fileName.?, file.?).shaped.<>({ r => import r._; _1.map(_ => OrganizationKYC.tupled((_1.get, _2.get, _3.get, _4.get, _5.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def status = column[Boolean]("status")

    def fileName = column[String]("fileName")

    def file = column[Array[Byte]]("file")


  }

}