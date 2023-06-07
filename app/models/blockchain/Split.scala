package models.blockchain

import com.assetmantle.modules.splits.{transactions => splitsTransactions}
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

case class Split(ownerID: Array[Byte], ownableID: Array[Byte], protoOwnableID: Array[Byte], ownerIDString: String, ownableIDString: String, value: BigInt, createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging {

  def serialize: Splits.SplitSerialized = Splits.SplitSerialized(
    ownerID = this.ownerID,
    ownableID = this.ownableID,
    protoOwnableID = this.protoOwnableID,
    ownerIDString = this.ownerIDString,
    ownableIDString = this.ownableIDString,
    value = BigDecimal(this.value),
    createdBy = this.createdBy,
    createdOnMillisEpoch = this.createdOnMillisEpoch,
    updatedBy = this.updatedBy,
    updatedOnMillisEpoch = this.updatedOnMillisEpoch)
}

object Splits {

  case class SplitSerialized(ownerID: Array[Byte], ownableID: Array[Byte], protoOwnableID: Array[Byte], ownerIDString: String, ownableIDString: String, value: BigDecimal, createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity2[Array[Byte], Array[Byte]] {

    def id1: Array[Byte] = this.ownerID

    def id2: Array[Byte] = this.ownableID

    def deserialize: Split = Split(
      ownerID = this.ownerID,
      ownableID = this.ownableID,
      protoOwnableID = this.protoOwnableID,
      ownerIDString = this.ownerIDString,
      ownableIDString = this.ownableIDString,
      value = this.value.toBigInt,
      createdBy = this.createdBy,
      createdOnMillisEpoch = this.createdOnMillisEpoch,
      updatedBy = this.updatedBy,
      updatedOnMillisEpoch = this.updatedOnMillisEpoch)

  }

  implicit val module: String = constants.Module.BLOCKCHAIN_SPLIT

  implicit val logger: Logger = Logger(this.getClass)

  class DataTable(tag: Tag) extends Table[SplitSerialized](tag, "Split") with ModelTable2[Array[Byte], Array[Byte]] {

    def * = (ownerID, ownableID, protoOwnableID, ownerIDString, ownableIDString, value, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (SplitSerialized.tupled, SplitSerialized.unapply)

    def ownerID = column[Array[Byte]]("ownerID", O.PrimaryKey)

    def ownableID = column[Array[Byte]]("ownableID", O.PrimaryKey)

    def protoOwnableID = column[Array[Byte]]("protoOwnableID")

    def ownerIDString = column[String]("ownerIDString")

    def ownableIDString = column[String]("ownableIDString")

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
  extends GenericDaoImpl2[Splits.DataTable, Splits.SplitSerialized, Array[Byte], Array[Byte]](
    databaseConfigProvider,
    Splits.TableQuery,
    executionContext,
    Splits.module,
    Splits.logger
  ) {
  object Service {
    def insertOrUpdate(split: Split): Future[Unit] = upsert(split.serialize)

    def add(splits: Seq[Split]): Future[Unit] = create(splits.map(_.serialize))

    def getByOwnerID(ownerId: IdentityID): Future[Seq[Split]] = filter(_.ownerID === ownerId.getBytes).map(_.map(_.deserialize))

    def getByOwnableID(ownableID: OwnableID): Future[Seq[Split]] = filter(_.ownableID === ownableID.getBytes).map(_.map(_.deserialize))

    def getTotalSupply(ownableID: OwnableID): Future[BigInt] = filter(_.ownableID === ownableID.getBytes).map(_.map(_.deserialize.value).sum)

    def getByOwnerIDAndOwnableID(ownerId: IdentityID, ownableID: OwnableID): Future[Option[Split]] = filter(x => x.ownerID === ownerId.getBytes && x.ownableID === ownableID.getBytes).map(_.headOption.map(_.deserialize))

    def tryGetByOwnerIDAndOwnableID(ownerId: IdentityID, ownableID: OwnableID): Future[Split] = tryGetById1AndId2(id1 = ownerId.getBytes, id2 = ownableID.getBytes).map(_.deserialize)

    def deleteByOwnerIDAndOwnableID(ownerId: IdentityID, ownableID: OwnableID): Future[Int] = deleteById1AndId2(id1 = ownerId.getBytes, id2 = ownableID.getBytes)

  }

  object Utility {

    def onSend(msg: splitsTransactions.send.Message): Future[String] = {
      val add = addSplit(ownerId = IdentityID(msg.getToID), ownableID = OwnableID(msg.getOwnableID), value = BigInt(msg.getValue))
      val subtract = subtractSplit(ownerId = IdentityID(msg.getFromID), ownableID = OwnableID(msg.getOwnableID), value = BigInt(msg.getValue))
      for {
        _ <- add
        _ <- subtract
      } yield msg.getFrom
    }

    def onWrap(msg: splitsTransactions.wrap.Message): Future[String] = {
      val updateBalance = blockchainBalances.Utility.insertOrUpdateBalance(msg.getFrom)
      val add = utilitiesOperations.traverse(msg.getCoinsList.asScala.toSeq.map(x => Coin(x))) { coin => addSplit(ownerId = IdentityID(msg.getFromID), ownableID = CoinID(StringID(coin.denom)), value = coin.amount.value) }
      for {
        _ <- updateBalance
        _ <- add
      } yield msg.getFrom
    }

    def onUnwrap(msg: splitsTransactions.unwrap.Message): Future[String] = {
      val updateBalance = blockchainBalances.Utility.insertOrUpdateBalance(msg.getFrom)
      val subtract = subtractSplit(ownerId = IdentityID(msg.getFromID), ownableID = OwnableID(msg.getOwnableID), value = BigInt(msg.getValue))
      for {
        _ <- updateBalance
        _ <- subtract
      } yield msg.getFrom
    }

    def mint(ownerID: IdentityID, ownableID: OwnableID, value: BigInt): Future[Unit] = addSplit(ownerId = ownerID, ownableID = ownableID, value = value)

    def burn(ownerID: IdentityID, ownableID: OwnableID, value: BigInt): Future[Unit] = subtractSplit(ownerId = ownerID, ownableID = ownableID, value = value)

    def renumerate(ownerID: IdentityID, ownableID: OwnableID, value: BigInt): Future[Unit] = {
      val totalSupply = Service.getTotalSupply(ownableID)

      def update(totalSupply: BigInt) = if (totalSupply < value) addSplit(ownerId = ownerID, ownableID = ownableID, value = value - totalSupply)
      else if (totalSupply > value) subtractSplit(ownerId = ownerID, ownableID = ownableID, value = totalSupply - value)
      else Future()

      for {
        totalSupply <- totalSupply
        _ <- update(totalSupply)
      } yield ()
    }

    def transfer(fromID: IdentityID, toID: IdentityID, ownableID: OwnableID, value: BigInt): Future[Unit] = {
      val add = addSplit(ownerId = toID, ownableID = ownableID, value = value)

      def subtract = subtractSplit(ownerId = fromID, ownableID = ownableID, value = value)

      for {
        _ <- add
        _ <- subtract
      } yield ()
    }

    private def addSplit(ownerId: IdentityID, ownableID: OwnableID, value: BigInt) = {
      val ownedSplit = Service.getByOwnerIDAndOwnableID(ownerId = ownerId, ownableID = ownableID)

      def addOrUpdate(ownedSplit: Option[Split]) = {
        val split = if (ownedSplit.isDefined) ownedSplit.get.copy(value = ownedSplit.get.value + value)
        else Split(ownerID = ownerId.getBytes, ownableID = ownableID.getBytes, protoOwnableID = ownableID.toAnyOwnableID.toByteArray, ownerIDString = ownerId.asString, ownableIDString = ownableID.asString, value = value)
        Service.insertOrUpdate(split)
      }

      for {
        ownedSplit <- ownedSplit
        _ <- addOrUpdate(ownedSplit)
      } yield ()
    }

    private def subtractSplit(ownerId: IdentityID, ownableID: OwnableID, value: BigInt) = {
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