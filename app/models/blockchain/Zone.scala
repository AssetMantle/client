package models.blockchain

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class Zone(id: String, address: String)

class Zones @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val zoneTable = TableQuery[ZoneTable]

  def add(zone: Zone): Future[String] = db.run(zoneTable returning zoneTable.map(_.id) += zone)

  def findById(id: String): Future[Zone] = db.run(zoneTable.filter(_.id === id).result.head)

  def deleteById(id: String) = db.run(zoneTable.filter(_.id === id).delete)

  private[models] class ZoneTable(tag: Tag) extends Table[Zone](tag, "Zone_BC") {

    def * = (id, address) <> (Zone.tupled, Zone.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def address = column[String]("address")

    def ? = (id.?, address.?).shaped.<>({ r => import r._; _1.map(_ => Zone.tupled((_1.get, _2.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

  }

}