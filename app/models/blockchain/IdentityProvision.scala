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

case class IdentityProvision(id: String, address: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {
  def getClassificationID: String = id.split(constants.RegularExpression.BLOCKCHAIN_FIRST_ORDER_COMPOSITE_ID_SEPARATOR)(0)

  def getHashID: String = id.split(constants.RegularExpression.BLOCKCHAIN_FIRST_ORDER_COMPOSITE_ID_SEPARATOR)(1)
}

@Singleton
class IdentityProvisions @Inject()(
                                    protected val databaseConfigProvider: DatabaseConfigProvider,
                                  )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_IDENTITY_PROVISION

  import databaseConfig.profile.api._

  private[models] val identityProvisionTable = TableQuery[IdentityProvisionTable]

  private def addProvisionedAddressByID(identityProvision: IdentityProvision): Future[String] = db.run((identityProvisionTable returning identityProvisionTable.map(_.id) += identityProvision).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.IDENTITY_PROVISIONED_ADDRESS_INSERT_FAILED, psqlException)
    }
  }

  private def addMultipleProvisionedAddresses(identityProvisions: Seq[IdentityProvision]): Future[Seq[String]] = db.run((identityProvisionTable returning identityProvisionTable.map(_.id) ++= identityProvisions).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.IDENTITY_PROVISIONED_ADDRESS_INSERT_FAILED, psqlException)
    }
  }

  private def getAllIdentityIDsByProvisionedAddress(address: String) = db.run(identityProvisionTable.filter(_.address === address).map(_.id).result)

  private def getAllProvisionedAddressByID(id: String) = db.run(identityProvisionTable.filter(_.id === id).map(_.address).result)

  private def deleteAllProvisionedAddressesByID(id: String): Future[Int] = db.run(identityProvisionTable.filter(_.id === id).delete)

  private def deleteProvisionedAddressByIDAndAddress(id: String, address: String): Future[Int] = db.run(identityProvisionTable.filter(x => x.id === id && x.address === address).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.IDENTITY_PROVISIONED_ADDRESS_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.IDENTITY_PROVISIONED_ADDRESS_DELETE_FAILED, noSuchElementException)
    }
  }

  private[models] class IdentityProvisionTable(tag: Tag) extends Table[IdentityProvision](tag, "IdentityProvision_BC") {

    def * = (id, address, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (IdentityProvision.tupled, IdentityProvision.unapply)

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

    def getAllIDsByProvisioned(address: String): Future[Seq[String]] = getAllIdentityIDsByProvisionedAddress(address)

    def getAllProvisionAddresses(id: String): Future[Seq[String]] = getAllProvisionedAddressByID(id)

    def addProvisionAddress(id: String, address: String): Future[String] = addProvisionedAddressByID(IdentityProvision(id = id, address = address))

    def deleteProvisionAddress(id: String, address: String): Future[Int] = deleteProvisionedAddressByIDAndAddress(id = id, address = address)
  }

  object Utility {

    def onProvision(identityProvision: TransactionMessages.IdentityProvision)(implicit header: Header): Future[Unit] = {
      val add = Service.addProvisionAddress(id = identityProvision.identityID, address = identityProvision.to)
      (for {
        _ <- add
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.IDENTITY_PROVISION + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

  }

}