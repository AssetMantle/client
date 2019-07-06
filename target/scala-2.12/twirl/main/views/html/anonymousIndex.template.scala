
package views.html

import _root_.play.twirl.api.TwirlFeatureImports._
import _root_.play.twirl.api.TwirlHelperImports._
import _root_.play.twirl.api.Html
import _root_.play.twirl.api.JavaScript
import _root_.play.twirl.api.Txt
import _root_.play.twirl.api.Xml
import models._
import controllers._
import play.api.i18n._
import views.html._
import play.api.templates.PlayMagic._
import play.api.mvc._
import play.api.data._
/*1.2*/import controllers.actions.LoginState

object anonymousIndex extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template5[RequestHeader,MessagesProvider,Flash,play.api.Configuration,LoginState,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*2.2*/()(implicit requestHeader: RequestHeader, messagesProvider: MessagesProvider, flash: Flash, configuration: play.api.Configuration, loginState: LoginState = null):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {

def /*5.6*/center/*5.12*/:play.twirl.api.HtmlFormat.Appendable = {_display_(

Seq[Any](format.raw/*5.16*/("""
    """),format.raw/*6.5*/("""<div class="flexContainer">
        <div id="commonHome" class="box flexItem flexGrowth"></div>
        <button onclick="getForm(jsRoutes.controllers.SendCoinController.requestCoinsForm())">"""),_display_(/*8.96*/Messages(constants.Form.REQUEST_COIN)),format.raw/*8.133*/("""</button>
    </div>
        <script>
                componentResource('commonHome', jsRoutes.controllers.ComponentViewController.commonHome('"""),_display_(/*11.107*/loginState/*11.117*/.username),format.raw/*11.126*/("""'))
        </script>
""")))};
Seq[Any](format.raw/*3.1*/("""
"""),_display_(/*4.2*/common(Messages(constants.User.HOME))/*4.39*/(Html(""))/*4.49*/(center)/*4.57*/(Html(""))),format.raw/*4.67*/("""
    """))
      }
    }
  }

  def render(requestHeader:RequestHeader,messagesProvider:MessagesProvider,flash:Flash,configuration:play.api.Configuration,loginState:LoginState): play.twirl.api.HtmlFormat.Appendable = apply()(requestHeader,messagesProvider,flash,configuration,loginState)

  def f:(() => (RequestHeader,MessagesProvider,Flash,play.api.Configuration,LoginState) => play.twirl.api.HtmlFormat.Appendable) = () => (requestHeader,messagesProvider,flash,configuration,loginState) => apply()(requestHeader,messagesProvider,flash,configuration,loginState)

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  DATE: Sat Jul 06 18:17:19 IST 2019
                  SOURCE: /root/IdeaProjects/comdex/app/views/anonymousIndex.scala.html
                  HASH: 4c7033d29add8bd7f70256701af093ed9339f141
                  MATRIX: 432->1|847->40|1086->275|1100->281|1180->285|1211->290|1428->481|1486->518|1658->662|1678->672|1709->681|1770->202|1797->204|1842->241|1860->251|1876->259|1906->269
                  LINES: 17->1|22->2|26->5|26->5|28->5|29->6|31->8|31->8|34->11|34->11|34->11|37->3|38->4|38->4|38->4|38->4|38->4
                  -- GENERATED --
              */
          