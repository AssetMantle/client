package models.blockchain

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class Account(address: String, coins: Int, publicKey: String, accountNumber: String, sequence: String)

class Accounts @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val accountTable = TableQuery[AccountTable]

  def add(account: Account): Future[String] = db.run(accountTable returning accountTable.map(_.address) += account)

  def findByAddress(address: String): Future[Account] = db.run(accountTable.filter(_.address === address).result.head)

  def deleteByAddress(address: String) = db.run(accountTable.filter(_.address === address).delete)

  private[models] class AccountTable(tag: Tag) extends Table[Account](tag, "Account") {

    def * = (address, coins, publicKey, accountNumber, sequence) <> (Account.tupled, Account.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def coins = column[Int]("coins")

    def publicKey = column[String]("publicKey")

    def accountNumber = column[String]("accountNumber")

    def sequence = column[String]("sequence")

    def ? = (address.?, coins.?, publicKey.?, accountNumber.?, sequence.?).shaped.<>({ r => import r._; _1.map(_ => Account.tupled((_1.get, _2.get, _3.get, _4.get, _5.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))


  }

}