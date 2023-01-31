package models.blockchain

import models.common.Serializable.Coin
import models.traits.{Entity2, GenericDaoImpl2, Logging, ModelTable2}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import schema.id.OwnableID
import schema.id.base.{CoinID, IdentityID, StringID}
import slick.jdbc.H2Profile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.CollectionHasAsScala

case class Split(ownerID: Array[Byte], ownableID: Array[Byte], value: BigDecimal, createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity2[Array[Byte], Array[Byte]] {

  def id1: Array[Byte] = this.ownerID

  def id2: Array[Byte] = this.ownableID

}

object Splits {

  implicit val module: String = constants.Module.BLOCKCHAIN_SPLIT

  implicit val logger: Logger = Logger(this.getClass)

  class DataTable(tag: Tag) extends Table[Split](tag, "Split") with ModelTable2[Array[Byte], Array[Byte]] {

    def * = (ownerID, ownableID, value, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (Split.tupled, Split.unapply)

    def ownerID = column[Array[Byte]]("ownerID", O.PrimaryKey)

    def ownableID = column[Array[Byte]]("ownableID", O.PrimaryKey)

    def value = column[BigDecimal]("value")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")

    def id1 = ownerID

    def id2 = ownableID

  }

  val TableQuery = new TableQuery(tag => new DataTable(tag))

}

@Singleton
class Splits @Inject()(
                        blockchainBalances: Balances,
                        utilitiesOperations: utilities.Operations,
                        protected val databaseConfigProvider: DatabaseConfigProvider
                      )(implicit override val executionContext: ExecutionContext)
  extends GenericDaoImpl2[Splits.DataTable, Split, Array[Byte], Array[Byte]](
    databaseConfigProvider,
    Splits.TableQuery,
    executionContext,
    Splits.module,
    Splits.logger
  ) {
  object Service {

    def add(splits: Seq[Split]): Future[Unit] = create(splits)

    def insertOrUpdate(split: Split): Future[Unit] = upsert(split)

    def getByOwnerID(ownerId: IdentityID): Future[Seq[Split]] = filter(_.ownerID === ownerId.getBytes)

    def getByOwnerIDAndOwnableID(ownerId: IdentityID, ownableID: OwnableID): Future[Option[Split]] = filter(x => x.ownerID === ownerId.getBytes && x.ownableID === ownableID.getBytes).map(_.headOption)

    def tryGetByOwnerIDAndOwnableID(ownerId: IdentityID, ownableID: OwnableID): Future[Split] = tryGetById1AndId2(id1 = ownerId.getBytes, id2 = ownableID.getBytes)

    def deleteByOwnerIDAndOwnableID(ownerId: IdentityID, ownableID: OwnableID): Future[Int] = deleteById1AndId2(id1 = ownerId.getBytes, id2 = ownableID.getBytes)

  }

  object Utility {

    def onSend(msg: com.splits.transactions.send.Message): Future[String] = {
      val add = addSplit(ownerId = IdentityID(msg.getToID), ownableID = OwnableID(msg.getOwnableID), value = BigDecimal(msg.getValue))
      val subtract = subtractSplit(ownerId = IdentityID(msg.getFromID), ownableID = OwnableID(msg.getOwnableID), value = BigDecimal(msg.getValue))
      for {
        _ <- add
        _ <- subtract
      } yield msg.getFrom
    }

    def onWrap(msg: com.splits.transactions.wrap.Message): Future[String] = {
      val updateBalance = blockchainBalances.Utility.insertOrUpdateBalance(msg.getFrom)
      val add = utilitiesOperations.traverse(msg.getCoinsList.asScala.toSeq.map(x => Coin(x))) { coin => addSplit(ownerId = IdentityID(msg.getFromID), ownableID = CoinID(StringID(coin.denom)), value = coin.amount.toMicroBigDecimal) }
      for {
        _ <- updateBalance
        _ <- add
      } yield msg.getFrom
    }

    def onUnwrap(msg: com.splits.transactions.unwrap.Message): Future[String] = {
      val updateBalance = blockchainBalances.Utility.insertOrUpdateBalance(msg.getFrom)
      val subtract = subtractSplit(ownerId = IdentityID(msg.getFromID), ownableID = OwnableID(msg.getOwnableID), value = BigDecimal(msg.getValue))
      for {
        _ <- updateBalance
        _ <- subtract
      } yield msg.getFrom
    }

    private def addSplit(ownerId: IdentityID, ownableID: OwnableID, value: BigDecimal) = {
      val ownedSplit = Service.getByOwnerIDAndOwnableID(ownerId = ownerId, ownableID = ownableID)

      def addOrUpdate(ownedSplit: Option[Split]) = {
        val split = if (ownedSplit.isDefined) {
          ownedSplit.get.copy(value = ownedSplit.get.value + value)
        } else Split(ownerID = ownerId.getBytes, ownableID = ownableID.getBytes, value = value)
        Service.insertOrUpdate(split)
      }

      for {
        ownedSplit <- ownedSplit
        _ <- addOrUpdate(ownedSplit)
      } yield ()
    }

    private def subtractSplit(ownerId: IdentityID, ownableID: OwnableID, value: BigDecimal) = {
      val ownedSplit = Service.tryGetByOwnerIDAndOwnableID(ownerId = ownerId, ownableID = ownableID)

      def deleteOrUpdate(ownedSplit: Split) = {
        val split = ownedSplit.copy(value = ownedSplit.value - value)
        if (split.value == 0) Service.deleteByOwnerIDAndOwnableID(ownerId = ownerId, ownableID = ownableID)
        else Service.insertOrUpdate(split)
      }

      for {
        ownedSplit <- ownedSplit
        _ <- deleteOrUpdate(ownedSplit)
      } yield ()
    }

  }
}