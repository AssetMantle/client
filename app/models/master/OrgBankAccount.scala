package models.master

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class OrgBankAccount(id: String, accountHolder: String, bankName: String, nickName: String, country: String, swift: String, address: String, zipcode: String, status: String)

class OrgBankAccounts @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val orgBankAccountTable = TableQuery[OrgBankAccountTable]

  def add(orgBankAccount: OrgBankAccount): Future[String] = db.run(orgBankAccountTable returning orgBankAccountTable.map(_.id) += orgBankAccount)

  def findById(id: String): Future[OrgBankAccount] = db.run(orgBankAccountTable.filter(_.id === id).result.head)

  def deleteById(id: String) = db.run(orgBankAccountTable.filter(_.id === id).delete)

  private[models] class OrgBankAccountTable(tag: Tag) extends Table[OrgBankAccount](tag, "OrgBankAccount") {

    def * = (id, accountHolder, bankName, nickName, country, swift, address, zipcode, status) <> (OrgBankAccount.tupled, OrgBankAccount.unapply)

    def ? = (id.?, accountHolder.?, bankName.?, nickName.?, country.?, swift.?, address.?, zipcode.?, status.?).shaped.<>({ r => import r._; _1.map(_ => OrgBankAccount.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    def id = column[String]("id", O.PrimaryKey)

    def accountHolder = column[String]("accountHolder")

    def bankName = column[String]("bankName")

    def nickName = column[String]("nickName")

    def country = column[String]("country")

    def swift = column[String]("swift")

    def address = column[String]("address")

    def zipcode = column[String]("zipcode")

    def status = column[String]("status")


  }

}