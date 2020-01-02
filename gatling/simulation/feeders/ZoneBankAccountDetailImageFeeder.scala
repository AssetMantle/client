package feeders

import java.awt.image.BufferedImage
import java.io.{ByteArrayOutputStream, File}
import java.nio.file.Files

import java.nio.file.Paths
import constants.Test
import javax.imageio.ImageIO

import scala.util.Random

object ZoneBankAccountDetailImageFeeder {

  val zoneBankAccountDetailImageFeed=imageCreator(Test.NUMBER_OF_USERS)

  def imageCreator(users: Int)={

    val imageFeed= new Array[Map[String, String]](users)
    for (id <- 0 until users) {
      val img = new BufferedImage(Random.nextInt(1000)+1000,Random.nextInt(1000)+1000, BufferedImage.TYPE_INT_RGB)
      val fileName="ZoneBankAccountDetailImage-"+Random.alphanumeric.take(6).mkString+".jpg"
      ImageIO.write(img, "jpg", new File("gatling/simulation/images/"+fileName))
      val length=(new File("gatling/simulation/images/"+fileName).length())
      imageFeed(id) = Map(Test.ZONE_BANK_DETAIL_FILENAME -> fileName, Test.ZONE_BANK_DETAIL_FILESIZE -> length.toString)
    }
    imageFeed
  }
}
