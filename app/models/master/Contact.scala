package models.master


import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Contact(id: String, mobileNumber: String, mobileNumberVerified: Boolean, emailAddress: String, emailAddressVerified: Boolean)

@Singleton
class Contacts @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  private implicit val module: String = constants.Module.MASTER_CONTACT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private val logger: Logger = Logger(this.getClass)

  private[models] val contactTable = TableQuery[ContactTable]

  private def add(contact: Contact): Future[String] = db.run(contactTable returning contactTable.map(_.id) += contact)

  private def findById(id: String): Future[Contact] = db.run(contactTable.filter(_.id === id).result.head)

  private def findEmailAddressById(id: String)(implicit executionContext: ExecutionContext): Future[String] = db.run(contactTable.filter(_.id === id).map(_.emailAddress).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findMobileNumberById(id: String)(implicit executionContext: ExecutionContext): Future[String] = db.run(contactTable.filter(_.id === id).map(_.mobileNumber).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String) = db.run(contactTable.filter(_.id === id).delete)

  private def update(contact: Contact): Future[Int] = db.run(contactTable.insertOrUpdate(contact))

  private def verifyMobileNumberOnId(id: String): Future[Int] = db.run(contactTable.filter(_.id === id).map(_.mobileNumberVerified).update(true))

  private def verifyEmailAddressOnId(id: String): Future[Int] = db.run(contactTable.filter(_.id === id).map(_.emailAddressVerified).update(true))

  private[models] class ContactTable(tag: Tag) extends Table[Contact](tag, "Contact") {

    def * = (id, mobileNumber, mobileNumberVerified, emailAddress, emailAddressVerified) <> (Contact.tupled, Contact.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def mobileNumber = column[String]("mobileNumber")

    def mobileNumberVerified = column[Boolean]("mobileNumberVerified")

    def emailAddress = column[String]("emailAddress")

    def emailAddressVerified = column[Boolean]("emailAddressVerified")

    def ? = (id.?, mobileNumber.?, mobileNumberVerified.?, emailAddress.?, emailAddressVerified.?).shaped.<>({ r => import r._; _1.map(_ => Contact.tupled((_1.get, _2.get, _3.get, _4.get, _5.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

  }

  object Service {

    def updateEmailAndMobile(id: String, mobileNumber: String, emailAddress: String): Boolean = if (0 < Await.result(update(new Contact(id, mobileNumber, false, emailAddress, false)), Duration.Inf)) true else false

    def verifyMobileNumber(id: String): Int = Await.result(verifyMobileNumberOnId(id), Duration.Inf)

    def verifyEmailAddress(id: String): Int = Await.result(verifyEmailAddressOnId(id), Duration.Inf)

    def findEmailAddress(id: String)(implicit executionContext: ExecutionContext): String = Await.result(findEmailAddressById(id), Duration.Inf)

    def findMobileNumber(id: String)(implicit executionContext: ExecutionContext): String = Await.result(findMobileNumberById(id), Duration.Inf)

  }

}

