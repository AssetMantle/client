
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
/*1.2*/import views.html.component.blockchain.{blockExplorer, transactionExplorer, transactionHash, allBlocksTable, validatorsTable, blockHeight, validators, lastBlockHeight, averageBlockTime, search, blockTimeGraph}
/*2.2*/import controllers.actions.LoginState

object market extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template5[RequestHeader,MessagesProvider,Flash,play.api.Configuration,LoginState,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*3.2*/()(implicit requestHeader: RequestHeader, messagesProvider: MessagesProvider, flash: Flash, configuration: play.api.Configuration, loginState: LoginState = null):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {

def /*8.2*/center/*8.8*/:play.twirl.api.HtmlFormat.Appendable = {_display_(

Seq[Any](format.raw/*8.12*/("""
   """),format.raw/*9.4*/("""<div class="flexContainer">

        <div class="flexGrowth box leftText">
            <div>
                <h2 class="showHide toggleElement centerText">"""),_display_(/*13.64*/Messages(constants.Form.ASSETS)),format.raw/*13.95*/(""" """),format.raw/*13.96*/("""</h2>
            </div>
            <div id="availableAssetListWithLogin" class="flexContainer">
            </div>
        </div>

    </div>

    <script>
        componentResource('availableAssetListWithLogin', jsRoutes.controllers.ComponentViewController.availableAssetListWithLogin('"""),_display_(/*22.133*/loginState/*22.143*/.username),format.raw/*22.152*/("""'));
    </script>
""")))};
Seq[Any](format.raw/*4.1*/("""
"""),_display_(/*5.2*/common(Messages(constants.Form.INDEX))/*5.40*/(Html(""))/*5.50*/(center)/*5.58*/(Html(""))),format.raw/*5.68*/("""


"""),format.raw/*24.2*/("""
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
                  DATE: Sat Jul 06 14:53:10 IST 2019
                  SOURCE: /root/IdeaProjects/comdex/app/views/market.scala.html
                  HASH: 3eea1756e60410e4ddaec7833818579f252d6874
                  MATRIX: 432->1|649->212|1056->251|1295->485|1308->491|1388->495|1418->499|1601->655|1653->686|1682->687|2000->977|2020->987|2051->996|2109->413|2136->415|2182->453|2200->463|2216->471|2246->481|2276->1016
                  LINES: 17->1|18->2|23->3|27->8|27->8|29->8|30->9|34->13|34->13|34->13|43->22|43->22|43->22|46->4|47->5|47->5|47->5|47->5|47->5|50->24
                  -- GENERATED --
              */
          