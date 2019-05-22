package models.master

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class AccountKYC(id: String, documentType: String, status: Boolean, fileName: String, file: Array[Byte])

@Singleton
class AccountKYCs @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ACCOUNT_KYC

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val accountKYCTable = TableQuery[AccountKYCTable]

  private def add(accountKYC: AccountKYC): Future[String] = db.run(accountKYCTable returning accountKYCTable.map(_.id) += accountKYC)

  private def findByIdDocumentType(id: String, documentType: String): Future[AccountKYC] = db.run(accountKYCTable.filter(_.id === id).filter(_.documentType === documentType).result.head)

  private def deleteByIdDocumenttype(id: String, documentType: String) = db.run(accountKYCTable.filter(_.id === id).filter(_.documentType === documentType).delete)

  private[models] class AccountKYCTable(tag: Tag) extends Table[AccountKYC](tag, "AccountKYC") {

    def * = (id, documentType, status, fileName, file) <> (AccountKYC.tupled, AccountKYC.unapply)

    def ? = (id.?, documentType.?, status.?, fileName.?, file.?).shaped.<>({ r => import r._; _1.map(_ => AccountKYC.tupled((_1.get, _2.get, _3.get, _4.get, _5.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    def id = column[String]("id", O.PrimaryKey)

    def documentType = column[String]("documentType", O.PrimaryKey)

    def status = column[Boolean]("status")

    def fileName = column[String]("fileName")

    def file = column[Array[Byte]]("file")


  }

}