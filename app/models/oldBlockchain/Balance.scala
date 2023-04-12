package models.oldBlockchain

import models.common.Serializable.Coin
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class Balance(address: String, coins: Seq[Coin])

@Singleton
class Balances @Inject()(
                          @NamedDatabase("oldExplorer")
                          protected val databaseConfigProvider: DatabaseConfigProvider,
                        )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = "oldEXPLORER_BALANCE"

  import databaseConfig.profile.api._

  private[models] val balanceTable = TableQuery[BalanceTable]

  case class BalanceSerialized(address: String, coins: String) {
    def deserialize: Balance = Balance(address = address, coins = utilities.JSON.convertJsonStringToObject[Seq[Coin]](coins))
  }

  private def getByAddress(address: String): Future[Option[BalanceSerialized]] = db.run(balanceTable.filter(_.address === address).result.headOption)

  private[models] class BalanceTable(tag: Tag) extends Table[BalanceSerialized](tag, "Balance_BC") {

    def * = (address, coins) <> (BalanceSerialized.tupled, BalanceSerialized.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def coins = column[String]("coins")

  }

  object Service {

    def get(address: String): Future[Option[Balance]] = getByAddress(address).map(_.map(_.deserialize))

  }


}