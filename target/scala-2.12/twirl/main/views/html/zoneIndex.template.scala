
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
/*1.2*/import models.master.Zone
/*2.2*/import controllers.actions.LoginState

object zoneIndex extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template6[Zone,RequestHeader,MessagesProvider,Flash,play.api.Configuration,LoginState,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*3.2*/(zone: Zone)(implicit requestHeader: RequestHeader, messagesProvider: MessagesProvider, flash: Flash, configuration: play.api.Configuration, loginState: LoginState = null):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {

def /*7.2*/center/*7.8*/:play.twirl.api.HtmlFormat.Appendable = {_display_(

Seq[Any](format.raw/*7.12*/("""
    """),format.raw/*8.5*/("""<div class="flexContainer">
        <div id="commonHome" class="box flexItem flexGrowth"></div>
        <div class="flexItem box leftAlign flexGrowth">
            <div>
                <h2 class="showHide toggleElement centerText">"""),_display_(/*12.64*/Messages(constants.Form.ZONE)),format.raw/*12.93*/(""":</h2>
            </div>
            <div id="zone">
            </div>
        </div>

    </div>
    <div class="centerText">
        <button onclick="getForm(jsRoutes.controllers.AddOrganizationController.viewPendingVerifyOrganizationRequests())">"""),_display_(/*20.124*/Messages(constants.Form.PENDING_VERIFY_ORGANIZATION_REQUESTS)),format.raw/*20.185*/("""</button>
        <button onclick="getForm(jsRoutes.controllers.BuyerExecuteOrderController.buyerExecuteOrderForm())">"""),_display_(/*21.110*/Messages(constants.Form.BUYER_EXECUTE_ORDER)),format.raw/*21.154*/("""</button>
        <button onclick="getForm(jsRoutes.controllers.IssueAssetController.viewPendingIssueAssetRequests())">"""),_display_(/*22.111*/Messages(constants.Form.PENDING_ISSUE_ASSET_REQUESTS)),format.raw/*22.164*/("""</button>
        <button onclick="getForm(jsRoutes.controllers.IssueFiatController.viewPendingIssueFiatRequests())">"""),_display_(/*23.109*/Messages(constants.Form.PENDING_ISSUE_FIAT_REQUESTS)),format.raw/*23.161*/("""</button>
        <button onclick="getForm(jsRoutes.controllers.ReleaseAssetController.releaseAssetForm())">"""),_display_(/*24.100*/Messages(constants.Form.RELEASE_ASSET)),format.raw/*24.138*/("""</button>
        <button onclick="getForm(jsRoutes.controllers.SellerExecuteOrderController.sellerExecuteOrderForm())">"""),_display_(/*25.112*/Messages(constants.Form.SELLER_EXECUTE_ORDER)),format.raw/*25.157*/("""</button>
        <button onclick="getForm(jsRoutes.controllers.SetACLController.setACLForm())">"""),_display_(/*26.88*/Messages(constants.Form.SET_ACL)),format.raw/*26.120*/("""</button>
        <button onclick="getForm(jsRoutes.controllers.FileController.updateZoneKycForm('"""),_display_(/*27.90*/constants/*27.99*/.File.BANK_DETAILS),format.raw/*27.117*/("""'))">"""),_display_(/*27.123*/Messages(constants.Form.UPDATE_ZONE_BANK_DETAILS)),format.raw/*27.172*/("""</button>
        <button onclick="getForm(jsRoutes.controllers.FileController.updateZoneKycForm('"""),_display_(/*28.90*/constants/*28.99*/.File.IDENTIFICATION),format.raw/*28.119*/("""'))">"""),_display_(/*28.125*/Messages(constants.Form.UPDATE_ZONE_IDENTIFICATION)),format.raw/*28.176*/("""</button>
    </div>
    <script>
            componentResource('commonHome', jsRoutes.controllers.ComponentViewController.commonHome('"""),_display_(/*31.103*/loginState/*31.113*/.username),format.raw/*31.122*/("""'));
            componentResource('zone', jsRoutes.controllers.ComponentViewController.zoneDetails('"""),_display_(/*32.98*/loginState/*32.108*/.username),format.raw/*32.117*/("""'));
    </script>
""")))};
Seq[Any](format.raw/*4.1*/("""
"""),_display_(/*5.2*/common(Messages(constants.User.HOME))/*5.39*/(Html(""))/*5.49*/(center)/*5.57*/(Html(""))),format.raw/*5.67*/("""

"""))
      }
    }
  }

  def render(zone:Zone,requestHeader:RequestHeader,messagesProvider:MessagesProvider,flash:Flash,configuration:play.api.Configuration,loginState:LoginState): play.twirl.api.HtmlFormat.Appendable = apply(zone)(requestHeader,messagesProvider,flash,configuration,loginState)

  def f:((Zone) => (RequestHeader,MessagesProvider,Flash,play.api.Configuration,LoginState) => play.twirl.api.HtmlFormat.Appendable) = (zone) => (requestHeader,messagesProvider,flash,configuration,loginState) => apply(zone)(requestHeader,messagesProvider,flash,configuration,loginState)

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  DATE: Sat Jul 06 18:18:48 IST 2019
                  SOURCE: /root/IdeaProjects/comdex/app/views/zoneIndex.scala.html
                  HASH: e3efd0d77f3f14f310e0891169cc70485bc4e5a7
                  MATRIX: 432->1|465->28|880->67|1129->309|1142->315|1222->319|1253->324|1513->557|1563->586|1843->838|1926->899|2073->1018|2139->1062|2287->1182|2362->1235|2508->1353|2582->1405|2719->1514|2779->1552|2928->1673|2995->1718|3119->1815|3173->1847|3299->1946|3317->1955|3357->1973|3391->1979|3462->2028|3588->2127|3606->2136|3648->2156|3682->2162|3755->2213|3919->2349|3939->2359|3970->2368|4099->2470|4119->2480|4150->2489|4208->239|4235->241|4280->278|4298->288|4314->296|4344->306
                  LINES: 17->1|18->2|23->3|27->7|27->7|29->7|30->8|34->12|34->12|42->20|42->20|43->21|43->21|44->22|44->22|45->23|45->23|46->24|46->24|47->25|47->25|48->26|48->26|49->27|49->27|49->27|49->27|49->27|50->28|50->28|50->28|50->28|50->28|53->31|53->31|53->31|54->32|54->32|54->32|57->4|58->5|58->5|58->5|58->5|58->5
                  -- GENERATED --
              */
          