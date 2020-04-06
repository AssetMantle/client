package models.master

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Asset(id: String, ownerID: String, ticketID: Option[String] = None, pegHash: Option[String] = None, assetType: String, description: String, documentHash: String, quantity: Int, quantityUnit: String, price: Int, moderated: Boolean, shippingPeriod: Int, portOfLoading: String, portOfDischarge: String, status: String)

@Singleton
class Assets @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  private[models] val assetTable = TableQuery[AssetTable]

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ASSET

  import databaseConfig.profile.api._

  private def add(asset: Asset): Future[(String, String)] = db.run((assetTable returning assetTable.map(x => (x.id, x.documentHash)) += asset).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByID(id: String): Future[Asset] = db.run(assetTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findPegHashByID(id: String): Future[Option[String]] = db.run(assetTable.filter(_.id === id).map(_.pegHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: Option[String]): Future[Asset] = db.run(assetTable.filter(_.ticketID.? === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findStatusByID(id: String): Future[String] = db.run(assetTable.filter(_.id === id).map(_.status).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findAllByTraderID(ownerID: String): Future[Seq[Asset]] = db.run(assetTable.filter(_.ownerID === ownerID).result)

  private def findAllByTraderIDAndStatuses(ownerID: String, statuses: String*): Future[Seq[Asset]] = db.run(assetTable.filter(_.ownerID === ownerID).filter(_.status.inSet(statuses)).result)

  private def findAllByIDs(ids: Seq[String]): Future[Seq[Asset]] = db.run(assetTable.filter(_.id.inSet(ids)).result)

  private def findAllByOwnerIDsAndStatus(ownerIDs: Seq[String], status: String): Future[Seq[Asset]] = db.run(assetTable.filter(_.ownerID.inSet(ownerIDs)).filter(_.status === status).result)

  private def checkByIDAndStatus(id: String, status: String): Future[Boolean] = db.run(assetTable.filter(_.id === id).filter(_.status === status).exists.result)

  private def updateStatusByID(id: String, status: String): Future[Int] = db.run(assetTable.filter(_.id === id).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusByPegHash(pegHash: String, status: String): Future[Int] = db.run(assetTable.filter(_.pegHash === pegHash).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updatePegHashAndStatusByID(id: String, pegHash: Option[String], status: String): Future[Int] = db.run(assetTable.filter(_.id === id).map(x => (x.pegHash.?, x.status)).update((pegHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTicketIDByID(id: String, ticketID: Option[String]): Future[Int] = db.run(assetTable.filter(_.id === id).map(_.ticketID.?).update(ticketID).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def createDocumentHash(assetID: String, ownerID: String, assetType: String, description: String, quantity: Int, quantityUnit: String, price: Int, moderated: Boolean, shippingPeriod: Int, portOfLoading: String, portOfDischarge: String): String = utilities.String.sha256Sum(Seq(assetID, ownerID, assetType, description, quantity.toString(), quantity, price.toString(), moderated.toString(), shippingPeriod.toString(), portOfLoading, portOfDischarge).mkString(""))

  private[models] class AssetTable(tag: Tag) extends Table[Asset](tag, "Asset") {

    def * = (id, ownerID, ticketID.?, pegHash.?, assetType, description, documentHash, quantity, quantityUnit, price, moderated, shippingPeriod, portOfLoading, portOfDischarge, status) <> (Asset.tupled, Asset.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def ownerID = column[String]("ownerID")

    def ticketID = column[String]("ticketID")

    def pegHash = column[String]("pegHash")

    def assetType = column[String]("assetType")

    def description = column[String]("description")

    def documentHash = column[String]("documentHash")

    def quantity = column[Int]("quantity")

    def moderated = column[Boolean]("moderated")

    def quantityUnit = column[String]("quantityUnit")

    def price = column[Int]("price")

    def shippingPeriod = column[Int]("shippingPeriod")

    def portOfLoading = column[String]("portOfLoading")

    def portOfDischarge = column[String]("portOfDischarge")

    def status = column[String]("status")

  }

  object Service {

    def insertModeratedAssetAndGetIDAndDocumentHash(ownerID: String, assetType: String, description: String, quantity: Int, quantityUnit: String, price: Int, shippingPeriod: Int, portOfLoading: String, portOfDischarge: String): Future[(String, String)] = {
      val id = utilities.IDGenerator.requestID()
      val documentHash = createDocumentHash(assetID = id, ownerID = ownerID, assetType = assetType, description = description, quantity = quantity, quantityUnit = quantityUnit, price = price, moderated = true, shippingPeriod = shippingPeriod, portOfLoading = portOfLoading, portOfDischarge = portOfDischarge)
      add(Asset(id = id, ownerID = ownerID, assetType = assetType, description = description, documentHash = documentHash, quantity = quantity, quantityUnit = quantityUnit, price = price, moderated = true, shippingPeriod = shippingPeriod, portOfLoading = portOfLoading, portOfDischarge = portOfDischarge, status = constants.Status.Asset.REQUESTED_TO_ZONE))
    }

    def insertUnmoderatedAssetAndGetIDAndDocumentHash(ownerID: String, assetType: String, description: String, quantity: Int, quantityUnit: String, price: Int, shippingPeriod: Int, portOfLoading: String, portOfDischarge: String): Future[(String, String)] = {
      val id = utilities.IDGenerator.requestID()
      val documentHash = createDocumentHash(assetID = id, ownerID = ownerID, assetType = assetType, description = description, quantity = quantity, quantityUnit = quantityUnit, price = price, moderated = false, shippingPeriod = shippingPeriod, portOfLoading = portOfLoading, portOfDischarge = portOfDischarge)
      add(Asset(id = id, ownerID = ownerID, assetType = assetType, description = description, documentHash = documentHash, quantity = quantity, quantityUnit = quantityUnit, price = price, moderated = false, shippingPeriod = shippingPeriod, portOfLoading = portOfLoading, portOfDischarge = portOfDischarge, status = constants.Status.Asset.AWAITING_BLOCKCHAIN_RESPONSE))
    }

    def tryGet(id: String): Future[Asset] = findByID(id)

    def tryGetPegHash(id: String): Future[String] = findPegHashByID(id).map(_.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)))

    def tryGetByTicketID(ticketID: String): Future[Asset] = findByTicketID(Option(ticketID))

    def updateTicketID(id: String, ticketID: String): Future[Int] = updateTicketIDByID(id = id, ticketID = Option(ticketID))

    def getAllAssets(ownerID: String): Future[Seq[Asset]] = findAllByTraderID(ownerID)

    def getAllTradableAssets(ownerID: String): Future[Seq[Asset]] = findAllByTraderIDAndStatuses(ownerID = ownerID, constants.Status.Asset.REQUESTED_TO_ZONE, constants.Status.Asset.AWAITING_BLOCKCHAIN_RESPONSE, constants.Status.Asset.ISSUED, constants.Status.Asset.TRADE_COMPLETED)

    def getAllAssetsByID(ids: Seq[String]): Future[Seq[Asset]] = findAllByIDs(ids)

    def tryGetStatus(id: String): Future[String] = findStatusByID(id)

    def markIssuedByID(id: String, pegHash: String): Future[Int] = updatePegHashAndStatusByID(id = id, pegHash = Option(pegHash), status = constants.Status.Asset.ISSUED)

    def markRedeemedByPegHash(pegHash: String): Future[Int] = updateStatusByPegHash(pegHash = pegHash, status = constants.Status.Asset.REDEEMED)

    def markIssueAssetRejected(id: String): Future[Int] = updateStatusByID(id = id, status = constants.Status.Asset.ISSUE_ASSET_FAILED)

    def markTradeCompletedByPegHash(pegHash: String): Future[Int] = updateStatusByPegHash(pegHash = pegHash, status = constants.Status.Asset.TRADE_COMPLETED)

    def getPendingIssueAssetRequests(traderIDs: Seq[String]): Future[Seq[Asset]] = findAllByOwnerIDsAndStatus(ownerIDs = traderIDs, status = constants.Status.Asset.REQUESTED_TO_ZONE)

    def verifyAssetPendingRequestStatus(id: String): Future[Boolean] = checkByIDAndStatus(id = id, status = constants.Status.Asset.REQUESTED_TO_ZONE)

  }

}
