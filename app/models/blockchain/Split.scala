package models.blockchain

import models.traits._
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import schema.id.base.{AssetID, IdentityID}
import slick.jdbc.H2Profile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class Split(ownerID: Array[Byte], ownableID: Array[Byte], protoOwnableID: Array[Byte], ownerIDString: String, ownableIDString: String, value: BigInt, createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging {

  def serialize: SplitSerialized = SplitSerialized(
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

private[blockchain] object Splits {

  class SplitTable(tag: Tag) extends Table[SplitSerialized](tag, "Split") with ModelTable2[Array[Byte], Array[Byte]] {

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
}

@Singleton
class Splits @Inject()(
                        utilitiesOperations: utilities.Operations,
                        protected val dbConfigProvider: DatabaseConfigProvider,
                      )(implicit val executionContext: ExecutionContext)
  extends GenericDaoImpl2[Splits.SplitTable, SplitSerialized, Array[Byte], Array[Byte]]() {

  implicit val module: String = constants.Module.BLOCKCHAIN_SPLIT

  implicit val logger: Logger = Logger(this.getClass)

  val tableQuery = new TableQuery(tag => new Splits.SplitTable(tag))

  object Service {
    def insertOrUpdate(split: Split): Future[Int] = upsert(split.serialize)

    def add(splits: Seq[Split]): Future[Int] = create(splits.map(_.serialize))

    def getByOwnerID(ownerId: IdentityID): Future[Seq[Split]] = filter(_.ownerID === ownerId.getBytes).map(_.map(_.deserialize))

    def getByOwnableID(ownableID: AssetID): Future[Seq[Split]] = filter(_.ownableID === ownableID.getBytes).map(_.map(_.deserialize))

    def getTotalSupply(ownableID: AssetID): Future[BigInt] = filter(_.ownableID === ownableID.getBytes).map(_.map(_.deserialize.value).sum)

    def getByOwnerIDAndOwnableID(ownerId: IdentityID, ownableID: AssetID): Future[Option[Split]] = filter(x => x.ownerID === ownerId.getBytes && x.ownableID === ownableID.getBytes).map(_.headOption.map(_.deserialize))

    def tryGetByOwnerIDAndOwnableID(ownerId: IdentityID, ownableID: AssetID): Future[Split] = tryGetById1AndId2(id1 = ownerId.getBytes, id2 = ownableID.getBytes).map(_.deserialize)

    def deleteByOwnerIDAndOwnableID(ownerId: IdentityID, ownableID: AssetID): Future[Int] = deleteById1AndId2(id1 = ownerId.getBytes, id2 = ownableID.getBytes)

  }

  object Utility {

    def mint(ownerID: IdentityID, assetID: AssetID, value: BigInt): Future[Unit] = addSplit(ownerId = ownerID, assetID = assetID, value = value)

    def burn(ownerID: IdentityID, assetID: AssetID, value: BigInt): Future[Unit] = subtractSplit(ownerId = ownerID, assetID = assetID, value = value)

    def renumerate(ownerID: IdentityID, assetID: AssetID, value: BigInt): Future[Unit] = {
      val totalSupply = Service.getTotalSupply(assetID)

      def update(totalSupply: BigInt) = if (totalSupply < value) addSplit(ownerId = ownerID, assetID = assetID, value = value - totalSupply)
      else if (totalSupply > value) subtractSplit(ownerId = ownerID, assetID = assetID, value = totalSupply - value)
      else Future()

      for {
        totalSupply <- totalSupply
        _ <- update(totalSupply)
      } yield ()
    }

    def transfer(fromID: IdentityID, toID: IdentityID, assetID: AssetID, value: BigInt): Future[Unit] = {
      val add = addSplit(ownerId = toID, assetID = assetID, value = value)

      def subtract = subtractSplit(ownerId = fromID, assetID = assetID, value = value)

      for {
        _ <- add
        _ <- subtract
      } yield ()
    }

    def addSplit(ownerId: IdentityID, assetID: AssetID, value: BigInt): Future[Unit] = {
      val ownedSplit = Service.getByOwnerIDAndOwnableID(ownerId = ownerId, ownableID = assetID)

      def addOrUpdate(ownedSplit: Option[Split]) = {
        val split = if (ownedSplit.isDefined) ownedSplit.get.copy(value = ownedSplit.get.value + value)
        else Split(ownerID = ownerId.getBytes, ownableID = assetID.getBytes, protoOwnableID = assetID.getProtoBytes, ownerIDString = ownerId.asString, ownableIDString = assetID.asString, value = value)
        Service.insertOrUpdate(split)
      }

      for {
        ownedSplit <- ownedSplit
        _ <- addOrUpdate(ownedSplit)
      } yield ()
    }

    def subtractSplit(ownerId: IdentityID, assetID: AssetID, value: BigInt): Future[Unit] = {
      val ownedSplit = Service.tryGetByOwnerIDAndOwnableID(ownerId = ownerId, ownableID = assetID)

      def deleteOrUpdate(ownedSplit: Split) = {
        val split = ownedSplit.copy(value = ownedSplit.value - value)
        if (split.value == 0) Service.deleteByOwnerIDAndOwnableID(ownerId = ownerId, ownableID = assetID)
        else Service.insertOrUpdate(split)
      }

      for {
        ownedSplit <- ownedSplit
        _ <- deleteOrUpdate(ownedSplit)
      } yield ()
    }

  }
}