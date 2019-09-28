package utilities

import java.util.concurrent.atomic.AtomicInteger

import scala.util.Random

object IDGenerator {

  val count = new AtomicInteger()

  //TODO remove "\n" from ids
  def requestID()(implicit module: String): String = {
    val initials = module.split("_").foldLeft("")(_ + _.take(1))
    Seq("RQ", initials, (count.getAndIncrement() % 89999 + 10000).toString, (System.currentTimeMillis() % 89999999 + 10000000).toString, Random.nextString(32 - initials.length - 15)).mkString("")
  }

  def ticketID()(implicit module: String): String = {
    val initials = module.split("_").foldLeft("")(_ + _.take(1))
    Seq("TX", initials, (count.getAndIncrement() % 89999 + 10000).toString, (System.currentTimeMillis() % 89999999 + 10000000).toString, Random.nextString(32 - initials.length - 15)).mkString("")
  }

  def hexadecimal: String = (-Math.abs(Random.nextLong)).toHexString.toUpperCase
}