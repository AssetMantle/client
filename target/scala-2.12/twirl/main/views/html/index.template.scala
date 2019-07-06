
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

object index extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template9[Seq[constants.Response.Failure],Seq[constants.Response.Warning],Seq[constants.Response.Success],Seq[constants.Response.Info],RequestHeader,MessagesProvider,Flash,play.api.Configuration,LoginState,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*3.2*/(failures: Seq[constants.Response.Failure] = Seq(), warnings: Seq[constants.Response.Warning] = Seq(), successes: Seq[constants.Response.Success] = Seq(), infos: Seq[constants.Response.Info] = Seq())(implicit requestHeader: RequestHeader, messagesProvider: MessagesProvider, flash: Flash, configuration: play.api.Configuration, loginState: LoginState = null):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {

def /*8.2*/center/*8.8*/:play.twirl.api.HtmlFormat.Appendable = {_display_(

Seq[Any](format.raw/*8.12*/("""
   """),format.raw/*9.4*/("""<div class="flexContainer">
       <div class="flexItem">
       """),_display_(/*11.9*/search()),format.raw/*11.17*/("""
        """),format.raw/*12.9*/("""</div>
    </div>
    <div class="flexContainer">
        <div class="flexItem box flexGrowth">"""),_display_(/*15.47*/lastBlockHeight()),format.raw/*15.64*/("""</div>
        <div class="flexItem box flexGrowth">"""),_display_(/*16.47*/validators()),format.raw/*16.59*/("""</div>
        <div class="flexItem box flexGrowth"> """),_display_(/*17.48*/Messages(constants.Form.TOKENS)),format.raw/*17.79*/("""
            """),format.raw/*18.13*/("""<p id="tokensValue"></p></div>
        <div class="flexItem box flexGrowth">"""),_display_(/*19.47*/averageBlockTime()),format.raw/*19.65*/("""</div>
    </div>
    <div id="indexBottomDivision" class="flexContainer">
        <div class="box flexGraph flexGrowth"> """),_display_(/*22.49*/blockTimeGraph()),format.raw/*22.65*/("""</div>
        <div class="box flexGrowth centerText" id="blockExplorer">"""),_display_(/*23.68*/blockExplorer()),format.raw/*23.83*/("""</div>
        <div class="box flexGrowth centerText" id="transactionExplorer"> """),_display_(/*24.75*/transactionExplorer()),format.raw/*24.96*/("""</div>
        <div class="flexGrowth box leftText">
            <div>
                <h2 class="showHide toggleElement centerText">"""),_display_(/*27.64*/Messages(constants.Form.ASSETS)),format.raw/*27.95*/(""" """),format.raw/*27.96*/("""</h2>
            </div>
            <div id="availableAssetList" class="flexContainer">
            </div>
        </div>
    </div>

    """),_display_(/*34.6*/transactionHash()),format.raw/*34.23*/("""

    """),_display_(/*36.6*/blockHeight()),format.raw/*36.19*/("""

    """),_display_(/*38.6*/validatorsTable()),format.raw/*38.23*/("""

    """),_display_(/*40.6*/allBlocksTable()),format.raw/*40.22*/("""

    """),format.raw/*42.5*/("""<script src=""""),_display_(/*42.19*/routes/*42.25*/.Assets.versioned("javascripts/back.js")),format.raw/*42.65*/("""" type="text/javascript"></script>
    <script>
        back();
        componentResource('availableAssetList', jsRoutes.controllers.ComponentViewController.availableAssetList());
    </script>
""")))};def /*49.2*/right/*49.7*/:play.twirl.api.HtmlFormat.Appendable = {_display_(

Seq[Any](format.raw/*49.11*/("""

    """),format.raw/*51.5*/("""<br>
    <a onclick="getForm(jsRoutes.controllers.AddKeyController.blockchainAddKeyForm())">"""),_display_(/*52.89*/Messages(constants.Form.ADD_KEY)),format.raw/*52.121*/("""</a> <br>
    <a onclick="getForm(jsRoutes.controllers.AddOrganizationController.blockchainAddOrganizationForm())">"""),_display_(/*53.107*/Messages(constants.Form.ADD_ORGANIZATION)),format.raw/*53.148*/("""</a>  <br>
    <a onclick="getForm(jsRoutes.controllers.AddZoneController.blockchainAddZoneForm())">"""),_display_(/*54.91*/Messages(constants.Form.ADD_ZONE)),format.raw/*54.124*/("""</a>  <br>
    <a onclick="getForm(jsRoutes.controllers.BuyerExecuteOrderController.blockchainBuyerExecuteOrderForm())">"""),_display_(/*55.111*/Messages(constants.Form.BUYER_EXECUTE_ORDER)),format.raw/*55.155*/("""</a> <br>
    <a onclick="getForm(jsRoutes.controllers.ChangeBuyerBidController.blockchainChangeBuyerBidForm())">"""),_display_(/*56.105*/Messages(constants.Form.CHANGE_BUYER_BID)),format.raw/*56.146*/("""</a> <br>
    <a onclick="getForm(jsRoutes.controllers.ChangeSellerBidController.blockchainChangeSellerBidForm())">"""),_display_(/*57.107*/Messages(constants.Form.CHANGE_SELLER_BID)),format.raw/*57.149*/("""</a> <br>
    <a onclick="getForm(jsRoutes.controllers.ConfirmBuyerBidController.blockchainConfirmBuyerBidForm())">"""),_display_(/*58.107*/Messages(constants.Form.CONFIRM_BUYER_BID)),format.raw/*58.149*/("""</a> <br>
    <a onclick="getForm(jsRoutes.controllers.ConfirmSellerBidController.blockchainConfirmSellerBidForm())">"""),_display_(/*59.109*/Messages(constants.Form.CONFIRM_SELLER_BID)),format.raw/*59.152*/("""</a> <br>
    <a onclick="getForm(jsRoutes.controllers.IssueAssetController.blockchainIssueAssetForm())">"""),_display_(/*60.97*/Messages(constants.Form.ISSUE_ASSET)),format.raw/*60.133*/("""</a> <br>
    <a onclick="getForm(jsRoutes.controllers.IssueFiatController.blockchainIssueFiatForm())">"""),_display_(/*61.95*/Messages(constants.Form.ISSUE_FIAT)),format.raw/*61.130*/("""</a> <br>
    <a onclick="getForm(jsRoutes.controllers.RedeemAssetController.blockchainRedeemAssetForm())">"""),_display_(/*62.99*/Messages(constants.Form.REDEEM_ASSET)),format.raw/*62.136*/("""</a> <br>
    <a onclick="getForm(jsRoutes.controllers.RedeemFiatController.blockchainRedeemFiatForm())">"""),_display_(/*63.97*/Messages(constants.Form.REDEEM_FIAT)),format.raw/*63.133*/("""</a> <br>
    <a onclick="getForm(jsRoutes.controllers.ReleaseAssetController.blockchainReleaseAssetForm())">"""),_display_(/*64.101*/Messages(constants.Form.RELEASE_ASSET)),format.raw/*64.139*/("""</a> <br>
    <a onclick="getForm(jsRoutes.controllers.SellerExecuteOrderController.blockchainSellerExecuteOrderForm())">"""),_display_(/*65.113*/Messages(constants.Form.SELLER_EXECUTE_ORDER)),format.raw/*65.158*/("""</a> <br>
    <a onclick="getForm(jsRoutes.controllers.SendAssetController.blockchainSendAssetForm())">"""),_display_(/*66.95*/Messages(constants.Form.SEND_ASSET)),format.raw/*66.130*/("""</a> <br>
    <a onclick="getForm(jsRoutes.controllers.SendCoinController.blockchainSendCoinForm())">"""),_display_(/*67.93*/Messages(constants.Form.SEND_COIN)),format.raw/*67.127*/("""</a> <br>
    <a onclick="getForm(jsRoutes.controllers.SendFiatController.blockchainSendFiatForm())">"""),_display_(/*68.93*/Messages(constants.Form.SEND_FIAT)),format.raw/*68.127*/("""</a> <br>
    <a onclick="getForm(jsRoutes.controllers.SetACLController.blockchainSetACLForm())">"""),_display_(/*69.89*/Messages(constants.Form.SET_ACL)),format.raw/*69.121*/("""</a> <br>
    <a onclick="getForm(jsRoutes.controllers.SetBuyerFeedbackController.blockchainSetBuyerFeedbackForm())">"""),_display_(/*70.109*/Messages(constants.Form.SET_BUYER_FEEDBACK)),format.raw/*70.152*/("""</a> <br>
    <a onclick="getForm(jsRoutes.controllers.SetSellerFeedbackController.blockchainSetSellerFeedbackForm())">"""),_display_(/*71.111*/Messages(constants.Form.SET_SELLER_FEEDBACK)),format.raw/*71.155*/("""</a> <br>

""")))};
Seq[Any](format.raw/*4.1*/("""
"""),_display_(/*5.2*/common(Messages(constants.Form.INDEX), failures, warnings, successes, infos)/*5.78*/(Html(""))/*5.88*/(center)/*5.96*/(right)),format.raw/*5.103*/("""


"""),format.raw/*47.2*/("""

"""))
      }
    }
  }

  def render(failures:Seq[constants.Response.Failure],warnings:Seq[constants.Response.Warning],successes:Seq[constants.Response.Success],infos:Seq[constants.Response.Info],requestHeader:RequestHeader,messagesProvider:MessagesProvider,flash:Flash,configuration:play.api.Configuration,loginState:LoginState): play.twirl.api.HtmlFormat.Appendable = apply(failures,warnings,successes,infos)(requestHeader,messagesProvider,flash,configuration,loginState)

  def f:((Seq[constants.Response.Failure],Seq[constants.Response.Warning],Seq[constants.Response.Success],Seq[constants.Response.Info]) => (RequestHeader,MessagesProvider,Flash,play.api.Configuration,LoginState) => play.twirl.api.HtmlFormat.Appendable) = (failures,warnings,successes,infos) => (requestHeader,messagesProvider,flash,configuration,loginState) => apply(failures,warnings,successes,infos)(requestHeader,messagesProvider,flash,configuration,loginState)

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  DATE: Sat Jul 06 14:53:10 IST 2019
                  SOURCE: /root/IdeaProjects/comdex/app/views/index.scala.html
                  HASH: 0b7335917e586f657cc37b3e75b53dbb21dabced
                  MATRIX: 432->1|649->212|1180->251|1616->717|1629->723|1709->727|1739->731|1831->797|1860->805|1896->814|2019->910|2057->927|2137->980|2170->992|2251->1046|2303->1077|2344->1090|2448->1167|2487->1185|2637->1308|2674->1324|2775->1398|2811->1413|2919->1494|2961->1515|3122->1649|3174->1680|3203->1681|3369->1821|3407->1838|3440->1845|3474->1858|3507->1865|3545->1882|3578->1889|3615->1905|3648->1911|3689->1925|3704->1931|3765->1971|3983->2169|3996->2174|4077->2178|4110->2184|4230->2277|4284->2309|4428->2425|4491->2466|4619->2567|4674->2600|4823->2721|4889->2765|5031->2879|5094->2920|5238->3036|5302->3078|5446->3194|5510->3236|5656->3354|5721->3397|5854->3503|5912->3539|6043->3643|6100->3678|6235->3786|6294->3823|6427->3929|6485->3965|6623->4075|6683->4113|6833->4235|6900->4280|7031->4384|7088->4419|7217->4521|7273->4555|7402->4657|7458->4691|7583->4789|7637->4821|7783->4939|7848->4982|7996->5102|8062->5146|8112->610|8139->612|8223->688|8241->698|8257->706|8285->713|8315->2166
                  LINES: 17->1|18->2|23->3|27->8|27->8|29->8|30->9|32->11|32->11|33->12|36->15|36->15|37->16|37->16|38->17|38->17|39->18|40->19|40->19|43->22|43->22|44->23|44->23|45->24|45->24|48->27|48->27|48->27|55->34|55->34|57->36|57->36|59->38|59->38|61->40|61->40|63->42|63->42|63->42|63->42|68->49|68->49|70->49|72->51|73->52|73->52|74->53|74->53|75->54|75->54|76->55|76->55|77->56|77->56|78->57|78->57|79->58|79->58|80->59|80->59|81->60|81->60|82->61|82->61|83->62|83->62|84->63|84->63|85->64|85->64|86->65|86->65|87->66|87->66|88->67|88->67|89->68|89->68|90->69|90->69|91->70|91->70|92->71|92->71|95->4|96->5|96->5|96->5|96->5|96->5|99->47
                  -- GENERATED --
              */
          