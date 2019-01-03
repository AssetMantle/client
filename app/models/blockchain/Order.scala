package models.blockchain

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class Order(id: String, fiatProofHash: String, awbProofHash: String, executed: Boolean)

class Orders @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val orderTable = TableQuery[OrderTable]

  def add(order: Order): Future[String] = db.run(orderTable returning orderTable.map(_.id) += order)

  def findById(id: String): Future[Order] = db.run(orderTable.filter(_.id === id).result.head)

  def deleteById(id: String) = db.run(orderTable.filter(_.id === id).delete)

  private[models] class OrderTable(tag: Tag) extends Table[Order](tag, "Order") {

    def * = (id, fiatProofHash, awbProofHash, executed) <> (Order.tupled, Order.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def fiatProofHash = column[String]("fiatProofHash")

    def awbProofHash = column[String]("awbProofHash")

    def executed = column[Boolean]("executed")

    def ? = (id.?, fiatProofHash.?, awbProofHash.?, executed.?).shaped.<>({ r => import r._; _1.map(_ => Order.tupled((_1.get, _2.get, _3.get, _4.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))


  }

}