package models.blockchain

import exceptions.BaseException
import models.Trait.Logged
import models.common.TransactionMessages
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import queries.responses.common.Header
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class IdentityUnprovision(id: String, address: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {
  def getClassificationID: String = id.split(constants.RegularExpression.BLOCKCHAIN_FIRST_ORDER_COMPOSITE_ID_SEPARATOR)(0)

  def getHashID: String = id.split(constants.RegularExpression.BLOCKCHAIN_FIRST_ORDER_COMPOSITE_ID_SEPARATOR)(1)
}

@Singleton
class IdentityUnprovisions @Inject()(
                                      protected val databaseConfigProvider: DatabaseConfigProvider,
                                    )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_IDENTITY_UNPROVISION

  import databaseConfig.profile.api._

  private[models] val identityUnprovisionTable = TableQuery[IdentityUnprovisionTable]

  private def addUnprovisionedAddressByID(identityUnprovision: IdentityUnprovision): Future[String] = db.run((identityUnprovisionTable returning identityUnprovisionTable.map(_.id) += identityUnprovision).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.IDENTITY_UNPROVISIONED_ADDRESS_INSERT_FAILED, psqlException)
    }
  }

  private def addMultipleUnprovisionedAddresses(identityUnprovisions: Seq[IdentityUnprovision]): Future[Seq[String]] = db.run((identityUnprovisionTable returning identityUnprovisionTable.map(_.id) ++= identityUnprovisions).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.IDENTITY_UNPROVISIONED_ADDRESS_INSERT_FAILED, psqlException)
    }
  }

  private def getAllIdentityIDsByUnprovisionedAddress(address: String) = db.run(identityUnprovisionTable.filter(_.address === address).map(_.id).result)

  private def getAllUnprovisionedAddressByID(id: String) = db.run(identityUnprovisionTable.filter(_.id === id).map(_.address).result)

  private def deleteAllUnprovisionedAddressesByID(id: String): Future[Int] = db.run(identityUnprovisionTable.filter(_.id === id).delete)

  private def deleteUnprovisionedAddressByIDAndAddress(id: String, address: String): Future[Int] = db.run(identityUnprovisionTable.filter(x => x.id === id && x.address === address).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.IDENTITY_UNPROVISIONED_ADDRESS_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.IDENTITY_UNPROVISIONED_ADDRESS_DELETE_FAILED, noSuchElementException)
    }
  }

  private[models] class IdentityUnprovisionTable(tag: Tag) extends Table[IdentityUnprovision](tag, "IdentityUnprovision_BC") {

    def * = (id, address, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (IdentityUnprovision.tupled, IdentityUnprovision.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def address = column[String]("address", O.PrimaryKey)

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def getAllIDsByUnprovisioned(address: String): Future[Seq[String]] = getAllIdentityIDsByUnprovisionedAddress(address)

    def getAllUnprovisionAddresses(id: String): Future[Seq[String]] = getAllUnprovisionedAddressByID(id)

    def addUnprovisionAddress(id: String, address: String): Future[String] = addUnprovisionedAddressByID(IdentityUnprovision(id = id, address = address))

    def deleteUnprovisionAddress(id: String, address: String): Future[Int] = deleteUnprovisionedAddressByIDAndAddress(id = id, address = address)
  }

  object Utility {

    def onUnprovision(identityUnprovision: TransactionMessages.IdentityUnprovision)(implicit header: Header): Future[Unit] = {
      val add = Service.addUnprovisionAddress(id = identityUnprovision.identityID, address = identityUnprovision.to)
      (for {
        _ <- add
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.IDENTITY_PROVISION + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

  }

}