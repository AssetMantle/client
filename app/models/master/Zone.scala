package models.master

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Random

case class Zone(id: String, secretHash: String, name: String, currency: String, status: Option[Boolean])

class Zones @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val zoneTable = TableQuery[ZoneTable]

  private def add(zone: Zone): Future[String] = db.run(zoneTable returning zoneTable.map(_.id) += zone)

  private def findById(id: String): Future[Zone] = db.run(zoneTable.filter(_.id === id).result.head)

  private def deleteById(id: String) = db.run(zoneTable.filter(_.id === id).delete)

  private def verifyZoneOnID(id: String, status: Boolean) = db.run(zoneTable.filter(_.id === id).map(_.status.?).update(Option(status)))

  private[models] class ZoneTable(tag: Tag) extends Table[Zone](tag, "Zone") {

    def * = (id, secretHash, name, currency, status.?) <> (Zone.tupled, Zone.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def secretHash = column[String]("secretHash")

    def name = column[String]("name")

    def currency = column[String]("currency")

    def status = column[Boolean]("status")

  }

  object Service {

    def addZone(secretHash: String, name: String, currency: String): String = Await.result(add(Zone(Random.nextInt.toHexString.toUpperCase, secretHash, name, currency, null)), Duration.Inf)

    def getZone(id: String): Zone = Await.result(findById(id), Duration.Inf)

    def verifyZone(id: String, status: Boolean): Boolean = if (Await.result(verifyZoneOnID(id, status), Duration.Inf) == 1) true else false


  }

}