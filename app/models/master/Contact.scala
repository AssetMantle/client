package models.master


import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

case class Contact(id: String, mobileNumber: String, mobileNumberVerified: Boolean, emailAddress: String, emailAddressVerified: Boolean)

class Contacts @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val contactTable = TableQuery[ContactTable]

  private def add(contact: Contact): Future[String] = db.run(contactTable returning contactTable.map(_.id) += contact)

  private def findById(id: String): Future[Contact] = db.run(contactTable.filter(_.id === id).result.head)

  private def deleteById(id: String) = db.run(contactTable.filter(_.id === id).delete)

  private def update(contact: Contact): Future[Int] = db.run(contactTable.insertOrUpdate(contact))

  private[models] class ContactTable(tag: Tag) extends Table[Contact](tag, "Contact") {

    def * = (id, mobileNumber, mobileNumberVerified, emailAddress, emailAddressVerified) <> (Contact.tupled, Contact.unapply)

    def ? = (id.?, mobileNumber.?, mobileNumberVerified.?, emailAddress.?, emailAddressVerified.?).shaped.<>({ r => import r._; _1.map(_ => Contact.tupled((_1.get, _2.get, _3.get, _4.get, _5.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    def id = column[String]("id", O.PrimaryKey)

    def mobileNumber = column[String]("mobileNumber")

    def mobileNumberVerified = column[Boolean]("mobileNumberVerified")

    def emailAddress = column[String]("emailAddress")

    def emailAddressVerified = column[Boolean]("emailAddressVerified")

  }

  object Service {

    def updateEmailAndMobile(id: String, mobileNumber: String, emailAddress: String): Boolean = if (0 < Await.result(update(new Contact(id, mobileNumber, false, emailAddress, false)), 1.seconds)) true else false
  }

}

