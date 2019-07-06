
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

object genesisIndex extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template5[RequestHeader,MessagesProvider,Flash,play.api.Configuration,LoginState,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*2.2*/()(implicit requestHeader: RequestHeader, messagesProvider: MessagesProvider, flash: Flash, configuration: play.api.Configuration, loginState: LoginState = null):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {

def /*5.2*/center/*5.8*/:play.twirl.api.HtmlFormat.Appendable = {_display_(

Seq[Any](format.raw/*5.12*/("""
    """),format.raw/*6.5*/("""<div class="flexContainer">
        <div id="commonHome" class="box flexItem flexGrowth"></div>
    <div class="centerText">
        <button onclick="getForm(jsRoutes.controllers.AddZoneController.viewPendingVerifyZoneRequests())">"""),_display_(/*9.108*/Messages(constants.Form.PENDING_VERIFY_ZONE_REQUESTS)),format.raw/*9.161*/("""</button>
        <button onclick="getForm(jsRoutes.controllers.SendCoinController.viewPendingFaucetRequests())">"""),_display_(/*10.105*/Messages(constants.Form.APPROVE_FAUCET_REQUEST)),format.raw/*10.152*/("""</button>
    </div>
    </div>
    <script>
        componentResource('commonHome', jsRoutes.controllers.ComponentViewController.commonHome('"""),_display_(/*14.99*/loginState/*14.109*/.username),format.raw/*14.118*/("""'))
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
                  SOURCE: /root/IdeaProjects/comdex/app/views/genesisIndex.scala.html
                  HASH: 1cf2bf9ce7659aca91c87a634f14b75220702124
                  MATRIX: 432->1|845->40|1084->271|1097->277|1177->281|1208->286|1467->518|1541->571|1683->685|1752->732|1922->875|1942->885|1973->894|2030->202|2057->204|2102->241|2120->251|2136->259|2166->269
                  LINES: 17->1|22->2|26->5|26->5|28->5|29->6|32->9|32->9|33->10|33->10|37->14|37->14|37->14|40->3|41->4|41->4|41->4|41->4|41->4
                  -- GENERATED --
              */
          