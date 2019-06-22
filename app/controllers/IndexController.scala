package controllers

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import akka.stream.scaladsl.Source
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.http.ContentTypes
import play.api.i18n.I18nSupport
import play.api.libs.Comet
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}

import scala.concurrent.duration._

@Singleton
class IndexController @Inject()(messagesControllerComponents: MessagesControllerComponents)(implicit configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def index: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.index())
  }

  def indexComet() = Action {
    Ok(views.html.scalacomet())
  }

  def streamClock() = Action {
    Ok.chunked(stringSource via Comet.string("parent.clockChanged")).as(ContentTypes.HTML)
  }

  def stringSource: Source[String, _] = {
    val df: DateTimeFormatter = DateTimeFormatter.ofPattern("HH mm ss")
    val tickSource = Source.tick(0 millis, 100 millis, "TICK")
    val s = tickSource.map((tick) => df.format(ZonedDateTime.now()))
    s
  }
}
