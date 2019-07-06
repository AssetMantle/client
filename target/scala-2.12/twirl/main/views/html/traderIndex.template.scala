
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
/*1.2*/import models.blockchain.{ACLHash, Asset, Fiat, Negotiation, Order}
/*2.2*/import models.master.{Organization, Zone}
/*3.2*/import controllers.actions.LoginState

object traderIndex extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template9[Int,Zone,Organization,ACLHash,RequestHeader,MessagesProvider,Flash,play.api.Configuration,LoginState,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*4.2*/(totalFiat: Int, zone: Zone, organization: Organization, aclHash: ACLHash)(implicit requestHeader: RequestHeader, messagesProvider: MessagesProvider, flash: Flash, configuration: play.api.Configuration, loginState: LoginState = null):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {

def /*8.2*/right/*8.7*/:play.twirl.api.HtmlFormat.Appendable = {_display_(

Seq[Any](format.raw/*8.11*/("""

    """),_display_(/*10.6*/if(aclHash.buyerExecuteOrder)/*10.35*/ {_display_(Seq[Any](format.raw/*10.37*/("""
        """),format.raw/*11.9*/("""<a onclick="getForm(jsRoutes.controllers.BuyerExecuteOrderController.unmoderatedBuyerExecuteOrderForm())">"""),_display_(/*11.116*/Messages(constants.Form.UNMODERATED_BUYER_EXECUTE_ORDER)),format.raw/*11.172*/("""</a>
    """)))}),format.raw/*12.6*/(""" """),format.raw/*12.7*/("""<br>
    """),format.raw/*26.7*/("""
    """),_display_(/*27.6*/if(aclHash.issueAsset)/*27.28*/ {_display_(Seq[Any](format.raw/*27.30*/("""
        """),format.raw/*28.9*/("""<a onclick="getForm(jsRoutes.controllers.IssueAssetController.issueAssetRequestForm())">"""),_display_(/*28.98*/Messages(constants.Form.ISSUE_ASSET_REQUEST)),format.raw/*28.142*/("""</a>
    """)))}),format.raw/*29.6*/(""" """),format.raw/*29.7*/("""<br>
    """),_display_(/*30.6*/if(aclHash.issueFiat)/*30.27*/ {_display_(Seq[Any](format.raw/*30.29*/("""
        """),format.raw/*31.9*/("""<a onclick="getForm(jsRoutes.controllers.IssueFiatController.issueFiatRequestForm())">"""),_display_(/*31.96*/Messages(constants.Form.ISSUE_FIAT_REQUEST)),format.raw/*31.139*/("""</a>
    """)))}),format.raw/*32.6*/(""" """),format.raw/*32.7*/("""<br>
    """),_display_(/*33.6*/if(aclHash.redeemAsset)/*33.29*/ {_display_(Seq[Any](format.raw/*33.31*/("""
        """),format.raw/*34.9*/("""<a onclick="getForm(jsRoutes.controllers.RedeemAssetController.redeemAssetForm())">"""),_display_(/*34.93*/Messages(constants.Form.REDEEM_ASSET)),format.raw/*34.130*/("""</a>
    """)))}),format.raw/*35.6*/(""" """),format.raw/*35.7*/("""<br>
    """),_display_(/*36.6*/if(aclHash.redeemFiat)/*36.28*/ {_display_(Seq[Any](format.raw/*36.30*/("""
        """),format.raw/*37.9*/("""<a onclick="getForm(jsRoutes.controllers.RedeemFiatController.redeemFiatForm())">"""),_display_(/*37.91*/Messages(constants.Form.REDEEM_FIAT)),format.raw/*37.127*/("""</a>
    """)))}),format.raw/*38.6*/(""" """),format.raw/*38.7*/("""<br>
    """),_display_(/*39.6*/if(aclHash.releaseAsset)/*39.30*/ {_display_(Seq[Any](format.raw/*39.32*/("""
        """),format.raw/*40.9*/("""<a onclick="getForm(jsRoutes.controllers.ReleaseAssetController.releaseAssetForm())">"""),_display_(/*40.95*/Messages(constants.Form.RELEASE_ASSET)),format.raw/*40.133*/("""</a>
    """)))}),format.raw/*41.6*/(""" """),format.raw/*41.7*/("""<br>
    """),_display_(/*42.6*/if(aclHash.sellerExecuteOrder)/*42.36*/ {_display_(Seq[Any](format.raw/*42.38*/("""
        """),format.raw/*43.9*/("""<a onclick="getForm(jsRoutes.controllers.SellerExecuteOrderController.unmoderatedSellerExecuteOrderForm())">"""),_display_(/*43.118*/Messages(constants.Form.UNMODERATED_SELLER_EXECUTE_ORDER)),format.raw/*43.175*/("""</a>
    """)))}),format.raw/*44.6*/(""" """),format.raw/*44.7*/("""<br>
  """),format.raw/*52.7*/("""
    """),format.raw/*53.5*/("""<a onclick="getForm(jsRoutes.controllers.SetBuyerFeedbackController.setBuyerFeedbackForm())">"""),_display_(/*53.99*/Messages(constants.Form.SET_BUYER_FEEDBACK)),format.raw/*53.142*/("""</a>
    <br>
    <a onclick="getForm(jsRoutes.controllers.SetSellerFeedbackController.setSellerFeedbackForm())">"""),_display_(/*55.101*/Messages(constants.Form.SET_SELLER_FEEDBACK)),format.raw/*55.145*/("""</a>
    <br>

    """)))};def /*60.2*/center/*60.8*/:play.twirl.api.HtmlFormat.Appendable = {_display_(

Seq[Any](format.raw/*60.12*/("""
    """),format.raw/*61.5*/("""<div class="flexContainer">
        <div id="commonHome" class="box flexItem flexGrowth"></div>
        <div class="flexGrowth flexItem box leftText">
            <div>
                <h2 class="showHide toggleElement centerText">"""),_display_(/*65.64*/Messages(constants.Form.ZONE)),format.raw/*65.93*/(""":</h2>
            </div>
            <div id="zone">

            </div>
        </div>
        <div class="flexGrowth flexItem box leftText">
            <div>
                <h2 class="showHide toggleElement centerText">"""),_display_(/*73.64*/Messages(constants.Form.ORGANIZATION)),format.raw/*73.101*/(""":</h2>
            </div>
            <div id="organization">

            </div>
        </div>
    </div>
    <div class="flexContainer">
        <div class="flexGrowth flexItem box leftText">
            <div>
                <h2 class="showHide toggleElement centerText">"""),_display_(/*83.64*/Messages(constants.User.ASSETS_OWNED)),format.raw/*83.101*/(""" """),format.raw/*83.102*/("""</h2>
                <div class="centerText">
                """),_display_(/*85.18*/if(aclHash.issueAsset)/*85.40*/ {_display_(Seq[Any](format.raw/*85.42*/("""
                    """),format.raw/*86.21*/("""<button onclick="getForm(jsRoutes.controllers.IssueAssetController.issueAssetRequestForm())">"""),_display_(/*86.115*/Messages(constants.Form.ISSUE_ASSET_REQUEST)),format.raw/*86.159*/("""</button>
                """)))}),format.raw/*87.18*/("""
                """),format.raw/*88.17*/("""</div>
            </div>
            <div id="assetList" class="flexContainer">
            </div>
        </div>

        <div class="flexGrowth flexItem box leftText">
            <div>
                <h2 class="showHide toggleElement centerText">"""),_display_(/*96.64*/Messages(constants.User.FIATS_OWNED)),format.raw/*96.100*/(""": """),_display_(/*96.103*/totalFiat),format.raw/*96.112*/("""</h2>
                <div class="centerText">
                    """),_display_(/*98.22*/if(aclHash.issueFiat)/*98.43*/ {_display_(Seq[Any](format.raw/*98.45*/("""
                        """),format.raw/*99.25*/("""<button onclick="getForm(jsRoutes.controllers.IssueFiatController.issueFiatRequestForm())">"""),_display_(/*99.117*/Messages(constants.Form.ISSUE_FIAT_REQUEST)),format.raw/*99.160*/("""</button>
                    """)))}),format.raw/*100.22*/("""
                    """),_display_(/*101.22*/if(aclHash.redeemFiat)/*101.44*/ {_display_(Seq[Any](format.raw/*101.46*/("""
                        """),format.raw/*102.25*/("""<button onclick="getForm(jsRoutes.controllers.RedeemFiatController.redeemFiatForm())">"""),_display_(/*102.112*/Messages(constants.Form.REDEEM_FIAT)),format.raw/*102.148*/("""</button>
                    """)))}),format.raw/*103.22*/("""
                """),format.raw/*104.17*/("""</div>
            </div>
            <div id="fiatList" class="flexContainer">
            </div>
        </div>

        <div class="flexGrowth flexItem box leftText">
            <div>
                <h2 class="showHide toggleElement centerText">"""),_display_(/*112.64*/Messages(constants.Form.ON_GOING_NEGOTIATIONS)),format.raw/*112.110*/("""</h2>
            </div>
            <div id="negotiationList" class ="flexContainer"></div>

        </div>
        <div class="flexGrowth flexItem box leftText">
            <div>
                <h2 class="showHide toggleElement centerText">"""),_display_(/*119.64*/Messages(constants.Form.ON_GOING_ORDERS)),format.raw/*119.104*/("""</h2>
            </div>
            <div id="orderList" class="flexContainer"></div>
        </div>
    </div>
    <script>
            componentResource('commonHome', jsRoutes.controllers.ComponentViewController.commonHome('"""),_display_(/*125.103*/loginState/*125.113*/.username),format.raw/*125.122*/("""'));
            componentResource('zone', jsRoutes.controllers.ComponentViewController.zoneDetails('"""),_display_(/*126.98*/loginState/*126.108*/.username),format.raw/*126.117*/("""'));
            componentResource('organization', jsRoutes.controllers.ComponentViewController.organizationDetails('"""),_display_(/*127.114*/loginState/*127.124*/.username),format.raw/*127.133*/("""'));
            componentResource('assetList', jsRoutes.controllers.ComponentViewController.assetList('"""),_display_(/*128.101*/loginState/*128.111*/.username),format.raw/*128.120*/("""'));
            componentResource('fiatList', jsRoutes.controllers.ComponentViewController.fiatList('"""),_display_(/*129.99*/loginState/*129.109*/.username),format.raw/*129.118*/("""'));
            componentResource('negotiationList', jsRoutes.controllers.ComponentViewController.negotiationList('"""),_display_(/*130.113*/loginState/*130.123*/.username),format.raw/*130.132*/("""'));
            componentResource('orderList', jsRoutes.controllers.ComponentViewController.orderList('"""),_display_(/*131.101*/loginState/*131.111*/.username),format.raw/*131.120*/("""'))
    </script>
""")))};
Seq[Any](format.raw/*5.1*/("""
"""),_display_(/*6.2*/common(Messages(constants.User.HOME))/*6.39*/(Html(""))/*6.49*/(center)/*6.57*/(right)),format.raw/*6.64*/("""

"""),format.raw/*58.6*/("""

"""))
      }
    }
  }

  def render(totalFiat:Int,zone:Zone,organization:Organization,aclHash:ACLHash,requestHeader:RequestHeader,messagesProvider:MessagesProvider,flash:Flash,configuration:play.api.Configuration,loginState:LoginState): play.twirl.api.HtmlFormat.Appendable = apply(totalFiat,zone,organization,aclHash)(requestHeader,messagesProvider,flash,configuration,loginState)

  def f:((Int,Zone,Organization,ACLHash) => (RequestHeader,MessagesProvider,Flash,play.api.Configuration,LoginState) => play.twirl.api.HtmlFormat.Appendable) = (totalFiat,zone,organization,aclHash) => (requestHeader,messagesProvider,flash,configuration,loginState) => apply(totalFiat,zone,organization,aclHash)(requestHeader,messagesProvider,flash,configuration,loginState)

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  DATE: Sat Jul 06 20:10:51 IST 2019
                  SOURCE: /root/IdeaProjects/comdex/app/views/traderIndex.scala.html
                  HASH: 8318d730c5eda1ce0d15a5c059b329803a800137
                  MATRIX: 432->1|507->70|556->113|998->152|1309->453|1321->458|1401->462|1434->469|1472->498|1512->500|1548->509|1683->616|1761->672|1801->682|1829->683|1865->1473|1897->1479|1928->1501|1968->1503|2004->1512|2120->1601|2186->1645|2226->1655|2254->1656|2290->1666|2320->1687|2360->1689|2396->1698|2510->1785|2575->1828|2615->1838|2643->1839|2679->1849|2711->1872|2751->1874|2787->1883|2898->1967|2957->2004|2997->2014|3025->2015|3061->2025|3092->2047|3132->2049|3168->2058|3277->2140|3335->2176|3375->2186|3403->2187|3439->2197|3472->2221|3512->2223|3548->2232|3661->2318|3721->2356|3761->2366|3789->2367|3825->2377|3864->2407|3904->2409|3940->2418|4077->2527|4156->2584|4196->2594|4224->2595|4258->2941|4290->2946|4411->3040|4476->3083|4618->3197|4684->3241|4727->3264|4741->3270|4822->3274|4854->3279|5113->3511|5163->3540|5415->3765|5474->3802|5777->4078|5836->4115|5866->4116|5957->4180|5988->4202|6028->4204|6077->4225|6199->4319|6265->4363|6323->4390|6368->4407|6647->4659|6705->4695|6736->4698|6767->4707|6862->4775|6892->4796|6932->4798|6985->4823|7105->4915|7170->4958|7233->4989|7283->5011|7315->5033|7356->5035|7410->5060|7526->5147|7585->5183|7648->5214|7694->5231|7973->5482|8042->5528|8315->5773|8378->5813|8634->6040|8655->6050|8687->6059|8817->6161|8838->6171|8870->6180|9017->6298|9038->6308|9070->6317|9204->6422|9225->6432|9257->6441|9388->6544|9409->6554|9441->6563|9587->6680|9608->6690|9640->6699|9774->6804|9795->6814|9827->6823|9884->386|9911->388|9956->425|9974->435|9990->443|10017->450|10046->3261
                  LINES: 17->1|18->2|19->3|24->4|28->8|28->8|30->8|32->10|32->10|32->10|33->11|33->11|33->11|34->12|34->12|35->26|36->27|36->27|36->27|37->28|37->28|37->28|38->29|38->29|39->30|39->30|39->30|40->31|40->31|40->31|41->32|41->32|42->33|42->33|42->33|43->34|43->34|43->34|44->35|44->35|45->36|45->36|45->36|46->37|46->37|46->37|47->38|47->38|48->39|48->39|48->39|49->40|49->40|49->40|50->41|50->41|51->42|51->42|51->42|52->43|52->43|52->43|53->44|53->44|54->52|55->53|55->53|55->53|57->55|57->55|60->60|60->60|62->60|63->61|67->65|67->65|75->73|75->73|85->83|85->83|85->83|87->85|87->85|87->85|88->86|88->86|88->86|89->87|90->88|98->96|98->96|98->96|98->96|100->98|100->98|100->98|101->99|101->99|101->99|102->100|103->101|103->101|103->101|104->102|104->102|104->102|105->103|106->104|114->112|114->112|121->119|121->119|127->125|127->125|127->125|128->126|128->126|128->126|129->127|129->127|129->127|130->128|130->128|130->128|131->129|131->129|131->129|132->130|132->130|132->130|133->131|133->131|133->131|136->5|137->6|137->6|137->6|137->6|137->6|139->58
                  -- GENERATED --
              */
          