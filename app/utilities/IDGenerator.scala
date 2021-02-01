package utilities

import models.common.Serializable.{Immutables, Mutables}

import java.util.concurrent.atomic.AtomicInteger
import scala.util.Random

object IDGenerator {

  private val count = new AtomicInteger()

  def requestID(length: Int = 32)(implicit module: String): String = {
    val initials = module.split("_").foldLeft("")(_ + _.take(1))
    Seq("RQ", initials, (count.getAndIncrement() % 89999 + 10000).toString, (System.currentTimeMillis() % 89999999 + 10000000).toString, Random.alphanumeric.take(length - initials.length - 15).mkString).mkString("")
  }

  def ticketID()(implicit module: String): String = {
    val initials = module.split("_").foldLeft("")(_ + _.take(1))
    Seq("TX", initials, (count.getAndIncrement() % 89999 + 10000).toString, (System.currentTimeMillis() % 89999999 + 10000000).toString, Random.alphanumeric.take(32 - initials.length - 15).mkString).mkString("")
  }

  def hexadecimal: String = (-Math.abs(Random.nextLong)).toHexString.toUpperCase

  def getClassificationID(chainID: String, immutables: Immutables, mutables: Mutables): String = Seq(chainID, Hash.getHash(Hash.getHash(immutables.properties.propertyList.map(_.id): _*), Hash.getHash(mutables.properties.propertyList.map(_.id): _*), immutables.getHashID)).mkString(constants.Blockchain.IDSeparator)

  def getAssetID(classificationID: String, immutables: Immutables): String = Seq(classificationID, immutables.getHashID).mkString(constants.Blockchain.FirstOrderCompositeIDSeparator)

  def getIdentityID(classificationID: String, immutables: Immutables): String = Seq(classificationID, immutables.getHashID).mkString(constants.Blockchain.FirstOrderCompositeIDSeparator)

  def getOrderID(classificationID: String, makerOwnableID: String, takerOwnableID: String, makerID: String, immutables: Immutables): String = Seq(classificationID, makerOwnableID, takerOwnableID, makerID, immutables.getHashID).mkString(constants.Blockchain.SecondOrderCompositeIDSeparator)

  def getMaintainerID(classificationID: String, identityID: String): String = Seq(classificationID, identityID).mkString(constants.Blockchain.SecondOrderCompositeIDSeparator)
}