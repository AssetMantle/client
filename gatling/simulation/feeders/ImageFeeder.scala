package feeders

import java.awt.image.BufferedImage
import java.io.{ByteArrayOutputStream, File}
import java.nio.file.Files
import java.nio.file.Paths

//import com.swara.learn.markov.DiscreteMarkovChain
import java.awt.Color


//import io.gatling.core.
import constants.Test
import io.gatling.core.config.DnsConfiguration
import javax.imageio.ImageIO
import play.api.Configuration

import scala.util.Random

object ImageFeeder {

  val imageFeed=imageCreator(100)

  def imageCreator(users: Int)={

    val imageFeed= new Array[Map[String, String]](users)
    for (id <- 0 until users) {

      val height=Random.nextInt(500)+50
      val width=Random.nextInt(500)+50

     val img = new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB)
        for(w <- 0 to width-1){
          for(h <- 0 to height-1){
          img.setRGB(w, h, Random.nextInt(1000))
      }}

      val fileName="test"+Random.alphanumeric.take(6).mkString+".jpg"
     ImageIO.write(img, "jpg", new File(Test.IMAGE_FILE_FEED+fileName))
      val length=(new File(Test.IMAGE_FILE_FEED+fileName).length())
      imageFeed(id) = Map(Test.TEST_FILE_NAME -> fileName, Test.TEST_FILE_SIZE -> length.toString)
    }
      imageFeed
  }

}