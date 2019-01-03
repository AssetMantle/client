package models.master

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class ZoneKYC(id: String, documentType: String, status: Boolean, fileName: String, file: Array[Byte])

class ZoneKYCs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val zoneKYCTable = TableQuery[ZoneKYCTable]

  def add(zoneKYC: ZoneKYC): Future[String] = db.run(zoneKYCTable returning zoneKYCTable.map(_.id) += zoneKYC)

  def findByIdDocumentType(id: String, documentType: String): Future[ZoneKYC] = db.run(zoneKYCTable.filter(_.id === id).filter(_.documentType === documentType).result.head)

  def deleteByIdDocumentType(id: String, documentType: String) = db.run(zoneKYCTable.filter(_.id === id).filter(_.documentType === documentType).delete)

  private[models] class ZoneKYCTable(tag: Tag) extends Table[ZoneKYC](tag, "ZoneKYC") {

    def * = (id, documentType, status, fileName, file) <> (ZoneKYC.tupled, ZoneKYC.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def status = column[Boolean]("status")

    def fileName = column[String]("fileName")

    def file = column[Array[Byte]]("file")

    def ? = (id.?, documentType.?, status.?, fileName.?, file.?).shaped.<>({ r => import r._; _1.map(_ => ZoneKYC.tupled((_1.get, _2.get, _3.get, _4.get, _5.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))


  }

}