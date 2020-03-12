package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import java.sql.Date
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class TradeTerm(id: String, assetType:String, assetDescription: AssetDescription, assetQuantity: AssetQuantity, assetPrice:AssetPrice, shipmentPeriod: ShipmentPeriod, portOfLoading: PortOfLoading, portOfDischarge: PortOfDischarge, advancePayment: AdvancePayment, creditTerms:CreditTerms, billOfExchangeRequired:BillOfExchangeRequired, primaryDocuments: PrimaryDocuments)

case class AssetDescription(assetDescriptionValue: String, assetDescriptionStatus: Boolean)

case class AssetQuantity(assetQuantityValue: Int, assetQuantityStatus: Boolean)

case class AssetPrice(assetPriceValue: Int, assetPriceStatus: Boolean)

case class ShipmentPeriod(shipmentPeriodValue: Int, shipmentPeriodStatus: Boolean)

case class PortOfLoading(portOfLoadingValue: String, portOfLoadingStatus: Boolean)

case class PortOfDischarge(portOfDischargeValue: String, portOfDischargeStatus: Boolean)

case class AdvancePayment(advancePaymentValue: Boolean, advancePercentage:Option[Double],advancePaymentStatus: Boolean)

case class CreditTerms(creditTermsValue:Boolean, tenure:Option[Int], tentativeDate:Option[Date], refrence:Option[String], creditTermsStatus:Boolean)

case class BillOfExchangeRequired(billOfExchangeRequiredValue:Boolean, billOfExchangeRequiredStatus:Boolean)

case class PrimaryDocuments(obl: Boolean, invoice: Boolean, coo: Boolean, coa: Boolean, otherDocuments: String, primaryDocumentsStatus:Boolean)

@Singleton
class TradeTerms @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_TRADE_TERM

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val tradeTermTable = TableQuery[TradeTermTable]

  private def add(tradeTerm: TradeTerm): Future[String] = db.run((tradeTermTable returning tradeTermTable.map(_.id) += tradeTerm).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(tradeTerm: TradeTerm): Future[Int] = db.run(tradeTermTable.insertOrUpdate(tradeTerm).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[TradeTerm] = db.run(tradeTermTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateAssetDescriptionStatusByID(id: String,assetDescriptionStatus:Boolean): Future[Int] = db.run(tradeTermTable.filter(_.id === id).map(_.assetDescriptionStatus).update(assetDescriptionStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateAssetQuantityStatusByID(id: String,assetQuantityStatus:Boolean): Future[Int] = db.run(tradeTermTable.filter(_.id === id).map(_.assetQuantityStatus).update(assetQuantityStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateAssetPriceStatusByID(id: String,assetPriceStatus:Boolean): Future[Int] = db.run(tradeTermTable.filter(_.id === id).map(_.assetPriceStatus).update(assetPriceStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateShipmentPeriodStatusByID(id: String,shipmentPeriodStatus:Boolean): Future[Int] = db.run(tradeTermTable.filter(_.id === id).map(_.shipmentPeriodStatus).update(shipmentPeriodStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updatePortOfLoadingStatusByID(id: String,portOfLoadingStatus:Boolean): Future[Int] = db.run(tradeTermTable.filter(_.id === id).map(_.portOfLoadingStatus).update(portOfLoadingStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updatePortOfDischargeStatusByID(id: String,portOfDischargeStatus:Boolean): Future[Int] = db.run(tradeTermTable.filter(_.id === id).map(_.portOfDischargeStatus).update(portOfDischargeStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateAdvancePaymentStatusByID(id: String,advancePaymentStatus:Boolean): Future[Int] = db.run(tradeTermTable.filter(_.id === id).map(_.advancePaymentStatus).update(advancePaymentStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateCreditTermsStatusByID(id: String,creditTermsStatus:Boolean): Future[Int] = db.run(tradeTermTable.filter(_.id === id).map(_.creditTermsStatus).update(creditTermsStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateBillOfExchangeRequiredStatusByID(id: String,billOfExchangeRequiredStatus:Boolean): Future[Int] = db.run(tradeTermTable.filter(_.id === id).map(_.billOfExchangeRequiredStatus).update(billOfExchangeRequiredStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updatePrimaryDocumentsStatusByID(id: String,primaryDocumentsStatus:Boolean): Future[Int] = db.run(tradeTermTable.filter(_.id === id).map(_.primaryDocumentsStatus).update(primaryDocumentsStatus).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String) = db.run(tradeTermTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class TradeTermTable(tag: Tag) extends Table[TradeTerm](tag, "TradeTerm") {

    def * = (id, assetType, (assetDescriptionValue, assetDescriptionStatus), (assetQuantityValue, assetQuantityStatus), (assetPriceValue, assetPriceStatus), (shipmentPeriodValue, shipmentPeriodStatus), (portOfLoadingValue, portOfLoadingStatus), (portOfDischargeValue, portOfDischargeStatus), (advancePaymentValue, advancePercentage, advancePaymentStatus), (creditTermsValue,tenure,tentativeDate,refrence, creditTermsStatus), (billOfExchangeRequiredValue, billOfExchangeRequiredStatus), (obl,invoice,coo,coa,otherDocuments, primaryDocumentsStatus)).shaped <> ( {
      case (id, assetType, assetDescription, assetQuantity, assetPrice, shipmentPeriod, portOfLoading, portOfDischarge, advancePayment, creditTerms, billOfExchangeRequired, primaryDocuments) => TradeTerm(id, assetType, AssetDescription.tupled.apply(assetDescription), AssetQuantity.tupled.apply(assetQuantity), AssetPrice.tupled.apply(assetPrice), ShipmentPeriod.tupled.apply(shipmentPeriod), PortOfLoading.tupled.apply(portOfLoading), PortOfDischarge.tupled.apply(portOfDischarge),AdvancePayment.tupled.apply(advancePayment), CreditTerms.tupled.apply(creditTerms), BillOfExchangeRequired.tupled.apply(billOfExchangeRequired), PrimaryDocuments.tupled.apply(primaryDocuments))
    }, { tradeTerm: TradeTerm =>
      def f1(assetDescription: AssetDescription) = AssetDescription.unapply(assetDescription).get

      def f2(assetQuantity: AssetQuantity) = AssetQuantity.unapply(assetQuantity).get

      def f3(assetPrice:AssetPrice) = AssetPrice.unapply(assetPrice).get

      def f4(shipmentPeriod: ShipmentPeriod) = ShipmentPeriod.unapply(shipmentPeriod).get

      def f5(portOfLoading: PortOfLoading) = PortOfLoading.unapply(portOfLoading).get

      def f6(portOfDischarge: PortOfDischarge) = PortOfDischarge.unapply(portOfDischarge).get

      def f7(advancePayment: AdvancePayment) = AdvancePayment.unapply(advancePayment).get

      def f8(creditTerms:CreditTerms) = CreditTerms.unapply(creditTerms).get

      def f9(billOfExchangeRequired:BillOfExchangeRequired) = BillOfExchangeRequired.unapply(billOfExchangeRequired).get

      def f10(primaryDocuments: PrimaryDocuments) = PrimaryDocuments.unapply(primaryDocuments).get

      Some((tradeTerm.id, tradeTerm.assetType, f1(tradeTerm.assetDescription), f2(tradeTerm.assetQuantity), f3(tradeTerm.assetPrice), f4(tradeTerm.shipmentPeriod), f5(tradeTerm.portOfLoading), f6(tradeTerm.portOfDischarge), f7(tradeTerm.advancePayment), f8(tradeTerm.creditTerms), f9(tradeTerm.billOfExchangeRequired), f10(tradeTerm.primaryDocuments) ))
    })

    def id = column[String]("id", O.PrimaryKey)

    def assetType = column[String]("assetType")

    def assetDescriptionValue = column[String]("assetDescriptionValue")

    def assetDescriptionStatus = column[Boolean]("assetDescriptionStatus")

    def assetQuantityValue = column[Int]("assetQuantityValue")

    def assetQuantityStatus = column[Boolean]("assetQuantityStatus")

    def assetPriceValue = column[Int]("assetPriceValue")

    def assetPriceStatus = column[Boolean]("assetPriceStatus")

    def shipmentPeriodValue = column[Int]("shipmentPeriodValue")

    def shipmentPeriodStatus = column[Boolean]("shipmentPeriodStatus")

    def portOfLoadingValue = column[String]("portOfLoadingValue")

    def portOfLoadingStatus = column[Boolean]("portOfLoadingStatus")

    def portOfDischargeValue = column[String]("portOfDischargeValue")

    def portOfDischargeStatus = column[Boolean]("portOfDischargeStatus")

    def advancePaymentValue = column[Boolean]("advancePaymentValue")

    def advancePercentage = column[Option[Double]]("advancePercentage")

    def advancePaymentStatus = column[Boolean]("advancePaymentStatus")

    def creditTermsValue = column[Boolean]("creditTermsValue")

    def tenure = column[Option[Int]]("tenure")

    def tentativeDate = column[Option[Date]]("tentativeDate")

    def refrence = column[Option[String]]("refrence")

    def creditTermsStatus = column[Boolean]("creditTermsStatus")

    def billOfExchangeRequiredValue = column[Boolean]("billOfExchangeRequiredValue")

    def billOfExchangeRequiredStatus = column[Boolean]("billOfExchangeRequiredStatus")

    def obl = column[Boolean]("obl")

    def invoice = column[Boolean]("invoice")

    def coo = column[Boolean]("coo")

    def coa = column[Boolean]("coa")

    def otherDocuments = column[String]("otherDocuments")

    def primaryDocumentsStatus = column[Boolean]("primaryDocumentsStatus")

  }

  object Service {

    def create(id: String, assetType:String,assetDescription: String, assetQuantity: Int, assetPrice: Int, shipmentPeriod: Int, portOfLoading: String, portOfDischarge: String, advancePaymentValue:Boolean,advancePercentage:Option[Double] , creditTermsValue:Boolean,tenure:Option[Int], tentativeDate:Option[Date], refrence:Option[String],  billOfExchangeRequired: Boolean, obl: Boolean, invoice: Boolean, coo: Boolean, coa: Boolean, otherDocuments: String): Future[String] = add(TradeTerm(id = id,assetType=assetType,AssetDescription(assetDescription,false), AssetQuantity(assetQuantity,false),AssetPrice(assetPrice,false), ShipmentPeriod(shipmentPeriod,false),PortOfLoading(portOfLoading,false),PortOfDischarge(portOfDischarge,false),AdvancePayment(advancePaymentValue,advancePercentage,false), CreditTerms(creditTermsValue, tenure,tentativeDate,refrence,false), BillOfExchangeRequired(billOfExchangeRequired,false), PrimaryDocuments(obl,invoice,coo,coa,otherDocuments,false)))

    def get(id: String) = findById(id)

    def updateAssetDescriptionStatus(id:String,assetDescriptionStatus:Boolean)=updateAssetDescriptionStatusByID(id,assetDescriptionStatus)

    def updateAssetQuantityStatus(id:String,assetQuantityStatus:Boolean)=updateAssetQuantityStatusByID(id,assetQuantityStatus)

    def updateAssetPriceStatus(id:String,assetPriceStatus:Boolean)=updateAssetPriceStatusByID(id,assetPriceStatus)

    def updateShipmentPeriodStatus(id:String, shipmentPeriodStatus:Boolean)=updateShipmentPeriodStatusByID(id,shipmentPeriodStatus)

    def updatePortOfLoadingStatus(id:String,portOfLoadingStatus:Boolean)=updatePortOfLoadingStatusByID(id,portOfLoadingStatus)

    def updatePortOfDischargeStatus(id:String,portOfDischargeStatus:Boolean)=updatePortOfDischargeStatusByID(id,portOfDischargeStatus)

    def updateAdvancePaymentStatus(id:String,advancePaymentStatus:Boolean)=updateAdvancePaymentStatusByID(id,advancePaymentStatus)

    def updateCreditTermsStatus(id:String,updateCreditTerms:Boolean)=updateCreditTermsStatusByID(id,updateCreditTerms)

    def updateBillOfExchangeRequiredStatus(id:String,billOfExchangeRequiredStatus:Boolean)=updateBillOfExchangeRequiredStatusByID(id,billOfExchangeRequiredStatus)

    def updatePrimaryDocumentsStatus(id:String,primaryDocumentsStatus:Boolean)=updatePrimaryDocumentsStatusByID(id,primaryDocumentsStatus)
  }

}