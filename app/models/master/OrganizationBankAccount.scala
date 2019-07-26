package models.master

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class OrganizationBankAccount(id: String, accountHolder: String, bankName: String, nickName: String, country: String, swift: String, address: String, zipcode: String, status: String)

@Singleton
class OrganizationBankAccounts @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ORGANIZATION_BANK_ACCOUNT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val organizationBankAccountTable = TableQuery[OrganizationBankAccountTable]

  private def add(organizationBankAccount: OrganizationBankAccount): Future[String] = db.run(organizationBankAccountTable returning organizationBankAccountTable.map(_.id) += organizationBankAccount)

  private def findById(id: String): Future[OrganizationBankAccount] = db.run(organizationBankAccountTable.filter(_.id === id).result.head)

  private def deleteById(id: String) = db.run(organizationBankAccountTable.filter(_.id === id).delete)

  private[models] class OrganizationBankAccountTable(tag: Tag) extends Table[OrganizationBankAccount](tag, "OrganizationBankAccount") {

    def * = (id, accountHolder, bankName, nickName, country, swift, address, zipcode, status) <> (OrganizationBankAccount.tupled, OrganizationBankAccount.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def accountHolder = column[String]("accountHolder")

    def bankName = column[String]("bankName")

    def nickName = column[String]("nickName")

    def country = column[String]("country")

    def swift = column[String]("swift")

    def address = column[String]("address")

    def zipcode = column[String]("zipcode")

    def status = column[String]("status")

    def ? = (id.?, accountHolder.?, bankName.?, nickName.?, country.?, swift.?, address.?, zipcode.?, status.?).shaped.<>({ r => import r._; _1.map(_ => OrganizationBankAccount.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))


  }

}