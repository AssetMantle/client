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

  def getChainIDAndHashID(id: String): (String, String) = {
    val splitString = id.split(constants.RegularExpression.BLOCKCHAIN_ID_SEPARATOR)
    (splitString(0), splitString(1))
  }

  def getClassificationIDAndHashID(id: String): (String, String) = {
    val splitString = id.split(constants.RegularExpression.BLOCKCHAIN_FIRST_ORDER_COMPOSITE_ID_SEPARATOR)
    (splitString(0), splitString(1))
  }

  def getClassificationIDMakerOwnableTakerOwnableIDRateIDCreationIDMakerIDHashID(id: String): (String, String, String, String, String, String, String) = {
    val splitString = id.split(constants.RegularExpression.BLOCKCHAIN_SECOND_ORDER_COMPOSITE_ID_SEPARATOR)
    (splitString(0), splitString(1), splitString(2), splitString(3), splitString(4), splitString(5), splitString(6))
  }

  def getClassificationID(chainID: String, immutables: Immutables, mutables: Mutables): String = Seq(chainID,
    Secrets.getBlockchainHash(utilities.Secrets.getBlockchainHash(immutables.properties.propertyList.map(_.id): _*), utilities.Secrets.getBlockchainHash(mutables.properties.propertyList.map(_.id): _*), immutables.getHashID)).mkString(constants.Blockchain.IDSeparator)

  def getMaintainerID(classificationID: String, identityID: String): String = Seq(classificationID, identityID).mkString(constants.Blockchain.SecondOrderCompositeIDSeparator)
}