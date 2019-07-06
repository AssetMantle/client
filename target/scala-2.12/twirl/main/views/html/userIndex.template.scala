
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

object userIndex extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template5[RequestHeader,MessagesProvider,Flash,play.api.Configuration,LoginState,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*2.2*/()(implicit requestHeader: RequestHeader, messagesProvider: MessagesProvider, flash: Flash, configuration: play.api.Configuration, loginState: LoginState = null):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {

def /*6.6*/center/*6.12*/:play.twirl.api.HtmlFormat.Appendable = {_display_(

Seq[Any](format.raw/*6.16*/("""
    """),format.raw/*7.5*/("""<div class="flexContainer">
        <div id="commonHome" class="box flexItem flexGrowth"></div>
    </div>
    <script src=""""),_display_(/*10.19*/routes/*10.25*/.Assets.versioned("javascripts/showHideUploadUpdateButton.js")),format.raw/*10.87*/("""" type="text/javascript"></script>
    <div class="centerText">
        <button id="userButtonUploadBankDetailsUser" class="hidden" onclick="getForm(jsRoutes.controllers.FileController.uploadUserKycForm('"""),_display_(/*12.142*/constants/*12.151*/.File.BANK_DETAILS),format.raw/*12.169*/("""'))">"""),_display_(/*12.175*/Messages(constants.Form.UPLOAD_BANK_DETAILS)),format.raw/*12.219*/("""</button>
        <button id="userButtonUpdateBankDetailsUser" class="hidden" onclick="getForm(jsRoutes.controllers.FileController.updateUserKycForm('"""),_display_(/*13.142*/constants/*13.151*/.File.BANK_DETAILS),format.raw/*13.169*/("""'))">"""),_display_(/*13.175*/Messages(constants.Form.UPDATE_BANK_DETAILS)),format.raw/*13.219*/("""</button>
        <script>checkAccountKycFileExists('"""),_display_(/*14.45*/loginState/*14.55*/.username),format.raw/*14.64*/("""', '"""),_display_(/*14.69*/constants/*14.78*/.File.BANK_DETAILS),format.raw/*14.96*/("""', "userButtonUploadBankDetailsUser", "userButtonUpdateBankDetailsUser")</script>
        <button id="userButtonUploadIdentificationUser" class="hidden" onclick="getForm(jsRoutes.controllers.FileController.uploadUserKycForm('"""),_display_(/*15.145*/constants/*15.154*/.File.IDENTIFICATION),format.raw/*15.174*/("""'))">"""),_display_(/*15.180*/Messages(constants.Form.UPLOAD_IDENTIFICATION)),format.raw/*15.226*/("""</button>
        <button id="userButtonUpdateIdentificationUser" class="hidden" onclick="getForm(jsRoutes.controllers.FileController.updateUserKycForm('"""),_display_(/*16.145*/constants/*16.154*/.File.IDENTIFICATION),format.raw/*16.174*/("""'))">"""),_display_(/*16.180*/Messages(constants.Form.UPDATE_IDENTIFICATION)),format.raw/*16.226*/("""</button>
        <script>checkAccountKycFileExists('"""),_display_(/*17.45*/loginState/*17.55*/.username),format.raw/*17.64*/("""', '"""),_display_(/*17.69*/constants/*17.78*/.File.IDENTIFICATION),format.raw/*17.98*/("""', "userButtonUploadIdentificationUser", "userButtonUpdateIdentificationUser")</script>
        <button id="userButtonUploadBankDetailsZone" class="hidden" onclick="getForm(jsRoutes.controllers.FileController.uploadUserZoneKycForm('"""),_display_(/*18.146*/constants/*18.155*/.File.BANK_DETAILS),format.raw/*18.173*/("""'))">"""),_display_(/*18.179*/Messages(constants.Form.UPLOAD_ZONE_BANK_DETAILS)),format.raw/*18.228*/("""</button>
        <button id="userButtonUpdateBankDetailsZone" class="hidden" onclick="getForm(jsRoutes.controllers.FileController.updateUserZoneKycForm('"""),_display_(/*19.146*/constants/*19.155*/.File.BANK_DETAILS),format.raw/*19.173*/("""'))">"""),_display_(/*19.179*/Messages(constants.Form.UPDATE_ZONE_BANK_DETAILS)),format.raw/*19.228*/("""</button>
        <script>checkZoneKycFileExists('"""),_display_(/*20.42*/loginState/*20.52*/.username),format.raw/*20.61*/("""', '"""),_display_(/*20.66*/constants/*20.75*/.File.BANK_DETAILS),format.raw/*20.93*/("""', "userButtonUploadBankDetailsZone", "userButtonUpdateBankDetailsZone")</script>
        <button id="userButtonUploadIdentificationZone" class="hidden" onclick="getForm(jsRoutes.controllers.FileController.uploadUserZoneKycForm('"""),_display_(/*21.149*/constants/*21.158*/.File.IDENTIFICATION),format.raw/*21.178*/("""'))">"""),_display_(/*21.184*/Messages(constants.Form.UPLOAD_ZONE_IDENTIFICATION)),format.raw/*21.235*/("""</button>
        <button id="userButtonUpdateIdentificationZone" class="hidden" onclick="getForm(jsRoutes.controllers.FileController.updateUserZoneKycForm('"""),_display_(/*22.149*/constants/*22.158*/.File.IDENTIFICATION),format.raw/*22.178*/("""'))">"""),_display_(/*22.184*/Messages(constants.Form.UPDATE_ZONE_IDENTIFICATION)),format.raw/*22.235*/("""</button>
        <script>checkZoneKycFileExists('"""),_display_(/*23.42*/loginState/*23.52*/.username),format.raw/*23.61*/("""', '"""),_display_(/*23.66*/constants/*23.75*/.File.IDENTIFICATION),format.raw/*23.95*/("""', "userButtonUploadIdentificationZone", "userButtonUpdateIdentificationZone")</script>
        <button id="userButtonUploadBankDetailsOrganization" class="hidden" onclick="getForm(jsRoutes.controllers.FileController.uploadUserOrganizationKycForm('"""),_display_(/*24.162*/constants/*24.171*/.File.BANK_DETAILS),format.raw/*24.189*/("""'))">"""),_display_(/*24.195*/Messages(constants.Form.UPLOAD_ORGANIZATION_BANK_DETAILS)),format.raw/*24.252*/("""</button>
        <button id="userButtonUpdateBankDetailsOrganization" class="hidden" onclick="getForm(jsRoutes.controllers.FileController.updateUserOrganizationKycForm('"""),_display_(/*25.162*/constants/*25.171*/.File.BANK_DETAILS),format.raw/*25.189*/("""'))">"""),_display_(/*25.195*/Messages(constants.Form.UPDATE_ORGANIZATION_BANK_DETAILS)),format.raw/*25.252*/("""</button>
        <script>checkOrganizationKycFileExists('"""),_display_(/*26.50*/loginState/*26.60*/.username),format.raw/*26.69*/("""', '"""),_display_(/*26.74*/constants/*26.83*/.File.BANK_DETAILS),format.raw/*26.101*/("""', "userButtonUploadBankDetailsOrganization", "userButtonUpdateBankDetailsOrganization")</script>
        <button id="userButtonUploadIdentificationOrganization" class="hidden" onclick="getForm(jsRoutes.controllers.FileController.uploadUserOrganizationKycForm('"""),_display_(/*27.165*/constants/*27.174*/.File.IDENTIFICATION),format.raw/*27.194*/("""'))">"""),_display_(/*27.200*/Messages(constants.Form.UPLOAD_ORGANIZATION_IDENTIFICATION)),format.raw/*27.259*/("""</button>
        <button id="userButtonUpdateIdentificationOrganization" class="hidden" onclick="getForm(jsRoutes.controllers.FileController.updateUserOrganizationKycForm('"""),_display_(/*28.165*/constants/*28.174*/.File.IDENTIFICATION),format.raw/*28.194*/("""'))">"""),_display_(/*28.200*/Messages(constants.Form.UPDATE_ORGANIZATION_IDENTIFICATION)),format.raw/*28.259*/("""</button>
        <script>checkOrganizationKycFileExists('"""),_display_(/*29.50*/loginState/*29.60*/.username),format.raw/*29.69*/("""', '"""),_display_(/*29.74*/constants/*29.83*/.File.IDENTIFICATION),format.raw/*29.103*/("""', "userButtonUploadIdentificationOrganization", "userButtonUpdateIdentificationOrganization")</script>
        <button onclick="getForm(jsRoutes.controllers.AddOrganizationController.addOrganizationForm())">"""),_display_(/*30.106*/Messages(constants.Form.ADD_ORGANIZATION)),format.raw/*30.147*/("""</button>
        <button onclick="getForm(jsRoutes.controllers.AddZoneController.addZoneForm())">"""),_display_(/*31.90*/Messages(constants.Form.ADD_ZONE)),format.raw/*31.123*/("""</button>
    </div>
        <script>
                componentResource('commonHome', jsRoutes.controllers.ComponentViewController.commonHome('"""),_display_(/*34.107*/loginState/*34.117*/.username),format.raw/*34.126*/("""'))
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
                  DATE: Sat Jul 06 18:18:48 IST 2019
                  SOURCE: /root/IdeaProjects/comdex/app/views/userIndex.scala.html
                  HASH: 3f4d301275dca262e9a9fc3139b9b5500b6329f8
                  MATRIX: 432->1|842->40|1081->276|1095->282|1175->286|1206->291|1358->416|1373->422|1456->484|1689->689|1708->698|1748->716|1782->722|1848->766|2027->917|2046->926|2086->944|2120->950|2186->994|2267->1048|2286->1058|2316->1067|2348->1072|2366->1081|2405->1099|2659->1325|2678->1334|2720->1354|2754->1360|2822->1406|3004->1560|3023->1569|3065->1589|3099->1595|3167->1641|3248->1695|3267->1705|3297->1714|3329->1719|3347->1728|3388->1748|3649->1981|3668->1990|3708->2008|3742->2014|3813->2063|3996->2218|4015->2227|4055->2245|4089->2251|4160->2300|4238->2351|4257->2361|4287->2370|4319->2375|4337->2384|4376->2402|4634->2632|4653->2641|4695->2661|4729->2667|4802->2718|4988->2876|5007->2885|5049->2905|5083->2911|5156->2962|5234->3013|5253->3023|5283->3032|5315->3037|5333->3046|5374->3066|5651->3315|5670->3324|5710->3342|5744->3348|5823->3405|6022->3576|6041->3585|6081->3603|6115->3609|6194->3666|6280->3725|6299->3735|6329->3744|6361->3749|6379->3758|6419->3776|6709->4038|6728->4047|6770->4067|6804->4073|6885->4132|7087->4306|7106->4315|7148->4335|7182->4341|7263->4400|7349->4459|7368->4469|7398->4478|7430->4483|7448->4492|7490->4512|7727->4721|7790->4762|7916->4861|7971->4894|8143->5038|8163->5048|8194->5057|8255->202|8282->204|8327->241|8345->251|8361->259|8391->269
                  LINES: 17->1|22->2|26->6|26->6|28->6|29->7|32->10|32->10|32->10|34->12|34->12|34->12|34->12|34->12|35->13|35->13|35->13|35->13|35->13|36->14|36->14|36->14|36->14|36->14|36->14|37->15|37->15|37->15|37->15|37->15|38->16|38->16|38->16|38->16|38->16|39->17|39->17|39->17|39->17|39->17|39->17|40->18|40->18|40->18|40->18|40->18|41->19|41->19|41->19|41->19|41->19|42->20|42->20|42->20|42->20|42->20|42->20|43->21|43->21|43->21|43->21|43->21|44->22|44->22|44->22|44->22|44->22|45->23|45->23|45->23|45->23|45->23|45->23|46->24|46->24|46->24|46->24|46->24|47->25|47->25|47->25|47->25|47->25|48->26|48->26|48->26|48->26|48->26|48->26|49->27|49->27|49->27|49->27|49->27|50->28|50->28|50->28|50->28|50->28|51->29|51->29|51->29|51->29|51->29|51->29|52->30|52->30|53->31|53->31|56->34|56->34|56->34|59->3|60->4|60->4|60->4|60->4|60->4
                  -- GENERATED --
              */
          