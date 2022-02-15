package models.common

import models.common.Serializable.{Data, Immutables, Mutables}
import utilities.Hash

object ID {

  case class ClassificationID(chainID: String, hashID: String) {
    def asString: String = Seq[String](this.chainID, this.hashID).mkString(constants.Blockchain.IDSeparator)
  }

  object ClassificationID {

    def apply(chainID: String, hashID: String): ClassificationID = new ClassificationID(chainID = chainID, hashID = hashID)

    def apply(id: String): ClassificationID = {
      val (chainID, hashID) = utilities.IDGenerator.getChainIDAndHashID(id)
      ClassificationID(chainID = chainID, hashID = hashID)
    }

    def apply(chainID: String, immutables: Immutables, mutables: Mutables): ClassificationID = ClassificationID(chainID = chainID, hashID = Hash.getHash(Hash.getHash(immutables.properties.propertyList.map(_.id): _*), Hash.getHash(mutables.properties.propertyList.map(_.id): _*), immutables.getHashID))

  }

  def getClassificationID(id: String): Option[ClassificationID] = try {
    Option(ClassificationID(id))
  } catch {
    case _: Exception => None
  }

  case class AssetID(classificationID: ClassificationID, hashID: String) {
    def asString: String = Seq[String](this.classificationID.asString, this.hashID).mkString(constants.Blockchain.FirstOrderCompositeIDSeparator)
  }

  object AssetID {

    def apply(classificationID: ClassificationID, hashID: String): AssetID = new AssetID(classificationID = classificationID, hashID = hashID)

    def apply(classificationID: String, hashID: String): AssetID = new AssetID(classificationID = ClassificationID(classificationID), hashID = hashID)

    def apply(id: String): AssetID = {
      val (classificationID, hashID) = utilities.IDGenerator.getClassificationIDAndHashID(id)
      AssetID(classificationID = classificationID, hashID = hashID)
    }

  }

  def getAssetID(id: String): Option[AssetID] = try {
    Option(AssetID(id))
  } catch {
    case _: Exception => None
  }

  case class IdentityID(classificationID: ClassificationID, hashID: String) {
    def asString: String = Seq[String](this.classificationID.asString, this.hashID).mkString(constants.Blockchain.FirstOrderCompositeIDSeparator)
  }

  object IdentityID {

    def apply(classificationID: ClassificationID, hashID: String): IdentityID = new IdentityID(classificationID = classificationID, hashID = hashID)

    def apply(classificationID: String, hashID: String): IdentityID = new IdentityID(classificationID = ClassificationID(classificationID), hashID = hashID)

    def apply(id: String): IdentityID = {
      val (classificationID, hashID) = utilities.IDGenerator.getClassificationIDAndHashID(id)
      IdentityID(classificationID = classificationID, hashID = hashID)
    }

  }

  def getIdentityID(id: String): Option[IdentityID] = try {
    Option(IdentityID(id))
  } catch {
    case _: Exception => None
  }

  case class MaintainerID(classificationID: ClassificationID, identityID: IdentityID) {
    def asString: String = Seq[String](this.classificationID.asString, this.identityID.asString).mkString(constants.Blockchain.SecondOrderCompositeIDSeparator)
  }

  object MaintainerID {

    def apply(classificationID: ClassificationID, identityID: IdentityID): MaintainerID = new MaintainerID(classificationID = classificationID, identityID = identityID)

    def apply(classificationID: String, identityID: String): MaintainerID = new MaintainerID(classificationID = ClassificationID(classificationID), identityID = IdentityID(identityID))

    def apply(id: String): MaintainerID = {
      val splitString = id.split(constants.RegularExpression.BLOCKCHAIN_SECOND_ORDER_COMPOSITE_ID_SEPARATOR)
      MaintainerID(classificationID = ClassificationID(splitString(0)), identityID = IdentityID(splitString(1)))
    }

  }

  def getMaintainerID(id: String): Option[MaintainerID] = try {
    Option(MaintainerID(id))
  } catch {
    case _: Exception => None
  }

  case class OrderID(classificationID: ClassificationID, makerOwnableID: String, takerOwnableID: String, rateID: String, creationID: String, makerID: IdentityID, hashID: String) {
    def asString: String = Seq[String](this.classificationID.asString, this.makerOwnableID, this.takerOwnableID, this.rateID, this.creationID, this.makerID.asString, this.hashID).mkString(constants.Blockchain.SecondOrderCompositeIDSeparator)
  }

  object OrderID {

    def apply(classificationID: ClassificationID, makerOwnableID: String, takerOwnableID: String, rateID: String, creationID: String, makerID: IdentityID, hashID: String): OrderID = new OrderID(classificationID = classificationID, makerOwnableID = makerOwnableID, takerOwnableID = takerOwnableID, rateID = rateID, creationID = creationID, makerID = makerID, hashID = hashID)

    def apply(classificationID: String, makerOwnableID: String, takerOwnableID: String, rateID: String, creationID: String, makerID: String, hashID: String): OrderID = new OrderID(classificationID = ClassificationID(classificationID), makerOwnableID = makerOwnableID, takerOwnableID = takerOwnableID, rateID = rateID, creationID = creationID, makerID = IdentityID(makerID), hashID = hashID)

    def apply(id: String): OrderID = {
      val (classificationID, makerOwnableID, takerOwnableID, rateID, creationID, makerID, hashID) = utilities.IDGenerator.getClassificationIDMakerOwnableTakerOwnableIDRateIDCreationIDMakerIDHashID(id)
      OrderID(classificationID = classificationID, makerOwnableID = makerOwnableID, takerOwnableID = takerOwnableID, rateID = rateID, creationID = creationID, makerID = makerID, hashID = hashID)
    }

  }

  def getOrderID(id: String): Option[OrderID] = try {
    Option(OrderID(id))
  } catch {
    case _: Exception => None
  }


  case class MetaID(typeID: String, hashID: String) {
    def asString: String = Seq[String](this.typeID, this.hashID).mkString(constants.Blockchain.FirstOrderCompositeIDSeparator)
  }

  object MetaID {

    def apply(typeID: String, hashID: String): MetaID = new MetaID(typeID = typeID, hashID = hashID)

    def apply(data: Data): MetaID = MetaID(typeID = data.dataType, hashID = data.value.generateHash)

    def apply(id: String): MetaID = {
      val splitString = id.split(constants.RegularExpression.BLOCKCHAIN_FIRST_ORDER_COMPOSITE_ID_SEPARATOR)
      MetaID(typeID = splitString(0), hashID = splitString(1))
    }

  }

  def getMetaID(id: String): Option[MetaID] = try {
    Option(MetaID(id))
  } catch {
    case _: Exception => None
  }

}
