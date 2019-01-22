package models.master

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class Zone(id: String, secretHash: String, name: String, currency: String)

class Zones @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val zoneTable = TableQuery[ZoneTable]

  private def add(zone: Zone): Future[String] = db.run(zoneTable returning zoneTable.map(_.id) += zone)

  private def findById(id: String): Future[Zone] = db.run(zoneTable.filter(_.id === id).result.head)

  private def deleteById(id: String) = db.run(zoneTable.filter(_.id === id).delete)

  private[models] class ZoneTable(tag: Tag) extends Table[Zone](tag, "Zone") {

    def * = (id, secretHash, name, currency) <> (Zone.tupled, Zone.unapply)

    def ? = (id.?, secretHash.?, name.?, currency.?).shaped.<>({ r => import r._; _1.map(_ => Zone.tupled((_1.get, _2.get, _3.get, _4.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    def id = column[String]("id", O.PrimaryKey)

    def secretHash = column[String]("secretHash")

    def name = column[String]("name")

    def currency = column[String]("currency")


  }

}