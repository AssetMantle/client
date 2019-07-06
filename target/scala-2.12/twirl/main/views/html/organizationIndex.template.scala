
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
/*1.2*/import models.master.Organization
/*2.2*/import controllers.actions.LoginState

object organizationIndex extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template6[Organization,RequestHeader,MessagesProvider,Flash,play.api.Configuration,LoginState,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*3.2*/(organization: Organization)(implicit requestHeader: RequestHeader, messagesProvider: MessagesProvider, flash: Flash, configuration: play.api.Configuration, loginState: LoginState = null):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {

def /*6.2*/center/*6.8*/:play.twirl.api.HtmlFormat.Appendable = {_display_(

Seq[Any](format.raw/*6.12*/("""
    """),format.raw/*7.5*/("""<div class="flexContainer">
        <div id="commonHome" class="box flexItem flexGrowth"></div>
        <div class="flexItem box leftText">
            <div>
                <h2 class="showHide toggleElement centerText">"""),_display_(/*11.64*/Messages(constants.Form.ORGANIZATION)),format.raw/*11.101*/("""</h2>
            </div>
            <div id="organization">

            </div>
        </div>
    </div>
    <div class="centerText">
        <button onclick="getForm(jsRoutes.controllers.FileController.updateOrganizationKycForm('"""),_display_(/*19.98*/constants/*19.107*/.File.BANK_DETAILS),format.raw/*19.125*/("""'))">"""),_display_(/*19.131*/Messages(constants.Form.UPDATE_ORGANIZATION_BANK_DETAILS)),format.raw/*19.188*/("""</button>
        <button onclick="getForm(jsRoutes.controllers.FileController.updateOrganizationKycForm('"""),_display_(/*20.98*/constants/*20.107*/.File.IDENTIFICATION),format.raw/*20.127*/("""'))">"""),_display_(/*20.133*/Messages(constants.Form.UPDATE_ORGANIZATION_IDENTIFICATION)),format.raw/*20.192*/("""</button>
    </div>
    <script>
            componentResource('commonHome', jsRoutes.controllers.ComponentViewController.commonHome('"""),_display_(/*23.103*/loginState/*23.113*/.username),format.raw/*23.122*/("""'));
            componentResource('organization', jsRoutes.controllers.ComponentViewController.organizationDetails('"""),_display_(/*24.114*/loginState/*24.124*/.username),format.raw/*24.133*/("""'));
    </script>
""")))};
Seq[Any](format.raw/*4.1*/("""
"""),_display_(/*5.2*/common(Messages(constants.User.HOME))/*5.39*/(Html(""))/*5.49*/(center)/*5.57*/(Html(""))),format.raw/*5.67*/("""
"""))
      }
    }
  }

  def render(organization:Organization,requestHeader:RequestHeader,messagesProvider:MessagesProvider,flash:Flash,configuration:play.api.Configuration,loginState:LoginState): play.twirl.api.HtmlFormat.Appendable = apply(organization)(requestHeader,messagesProvider,flash,configuration,loginState)

  def f:((Organization) => (RequestHeader,MessagesProvider,Flash,play.api.Configuration,LoginState) => play.twirl.api.HtmlFormat.Appendable) = (organization) => (requestHeader,messagesProvider,flash,configuration,loginState) => apply(organization)(requestHeader,messagesProvider,flash,configuration,loginState)

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  DATE: Sat Jul 06 18:17:19 IST 2019
                  SOURCE: /root/IdeaProjects/comdex/app/views/organizationIndex.scala.html
                  HASH: f4f7d02d1c08e39ba08d5dc0250f11464f7aa48c
                  MATRIX: 432->1|473->36|904->75|1169->332|1182->338|1262->342|1293->347|1541->568|1600->605|1860->838|1879->847|1919->865|1953->871|2032->928|2166->1035|2185->1044|2227->1064|2261->1070|2342->1129|2506->1265|2526->1275|2557->1284|2703->1402|2723->1412|2754->1421|2812->263|2839->265|2884->302|2902->312|2918->320|2948->330
                  LINES: 17->1|18->2|23->3|27->6|27->6|29->6|30->7|34->11|34->11|42->19|42->19|42->19|42->19|42->19|43->20|43->20|43->20|43->20|43->20|46->23|46->23|46->23|47->24|47->24|47->24|50->4|51->5|51->5|51->5|51->5|51->5
                  -- GENERATED --
              */
          