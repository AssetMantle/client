
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

object common extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template12[String,Seq[constants.Response.Failure],Seq[constants.Response.Warning],Seq[constants.Response.Success],Seq[constants.Response.Info],Html,Html,Html,RequestHeader,MessagesProvider,Flash,LoginState,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*2.2*/(title: String, failures: Seq[constants.Response.Failure] = Seq(), warnings: Seq[constants.Response.Warning] = Seq(), successes: Seq[constants.Response.Success] = Seq(), infos: Seq[constants.Response.Info] = Seq())(left: Html)(center:Html = Html(""))(right:Html = Html(""))(implicit requestHeader: RequestHeader, messagesProvider: MessagesProvider, flash: Flash,loginState: LoginState = null):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*3.1*/("""<!DOCTYPE html>
<html lang="en">
    <head>
        <title>"""),_display_(/*6.17*/title),format.raw/*6.22*/("""</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" media="screen" href=""""),_display_(/*8.54*/routes/*8.60*/.Assets.versioned("stylesheets/class.css")),format.raw/*8.102*/("""">
        <link rel="stylesheet" media="screen" href=""""),_display_(/*9.54*/routes/*9.60*/.Assets.versioned("stylesheets/tag.css")),format.raw/*9.100*/("""">
        <link rel="shortcut icon" type="image/png" href=""""),_display_(/*10.59*/routes/*10.65*/.Assets.versioned("images/favicon.png")),format.raw/*10.104*/("""">
        <script src=""""),_display_(/*11.23*/routes/*11.29*/.JavaScriptRoutesController.javascriptRoutes),format.raw/*11.73*/("""" type="text/javascript" ></script>
        <script src=""""),_display_(/*12.23*/routes/*12.29*/.Assets.versioned("javascripts/jQuery.min.js")),format.raw/*12.75*/("""" type="text/javascript"></script>
        <script src=""""),_display_(/*13.23*/routes/*13.29*/.Assets.versioned("javascripts/firebase.js")),format.raw/*13.73*/("""" type="text/javascript"></script>
        <script src=""""),_display_(/*14.23*/routes/*14.29*/.Assets.versioned("javascripts/cookie.js")),format.raw/*14.71*/("""" type="text/javascript"></script>
        <script src=""""),_display_(/*15.23*/routes/*15.29*/.Assets.versioned("javascripts/configuration.js")),format.raw/*15.78*/("""" type="text/javascript"></script>
        <script src=""""),_display_(/*16.23*/routes/*16.29*/.Assets.versioned("javascripts/pushNotification.js")),format.raw/*16.81*/("""" type="text/javascript"></script>
        <script src=""""),_display_(/*17.23*/routes/*17.29*/.Assets.versioned("javascripts/notification.js")),format.raw/*17.77*/("""" type="text/javascript"></script>
        <script src=""""),_display_(/*18.23*/routes/*18.29*/.Assets.versioned("javascripts/componentResource.js")),format.raw/*18.82*/("""" type="text/javascript"></script>

    </head>
    <body>
        <div class="pageContent">
            <div class="stickyBar">
                <div class = "navbar">
                    <a class="active" href=""""),_display_(/*25.46*/controllers/*25.57*/.routes.IndexController.index()),format.raw/*25.88*/("""" >Home</a>

                    """),_display_(/*27.22*/loginState/*27.32*/ match/*27.38*/ {/*28.25*/case null =>/*28.37*/ {_display_(Seq[Any](format.raw/*28.39*/("""
                            """),format.raw/*29.29*/("""<a class="right" onclick="getForm(jsRoutes.controllers.SignUpController.signUpForm())">"""),_display_(/*29.117*/Messages(constants.Form.SIGNUP)),format.raw/*29.148*/("""</a>
                            <a class="right" onclick="getForm(jsRoutes.controllers.LoginController.loginForm())">"""),_display_(/*30.115*/Messages(constants.Form.LOGIN)),format.raw/*30.145*/("""</a>
                        """)))}/*32.25*/case default =>/*32.40*/ {_display_(Seq[Any](format.raw/*32.42*/("""
                            """),format.raw/*33.29*/("""<a id="market" href=""""),_display_(/*33.51*/controllers/*33.62*/.routes.ViewController.market()),format.raw/*33.93*/("""">Market</a>

                            <a class="right" onclick="getForm(jsRoutes.controllers.LogoutController.logoutForm())">"""),_display_(/*35.117*/loginState/*35.127*/.username),format.raw/*35.136*/("""</a>
                            <a class="right" onclick="getForm(jsRoutes.controllers.LogoutController.logoutForm())">"""),_display_(/*36.117*/Messages(constants.Form.LOGOUT)),format.raw/*36.148*/("""</a>
                            <a class="right notification" onclick="getForm(jsRoutes.controllers.NotificationController.notificationPage(0))">"""),_display_(/*37.143*/Messages(constants.Form.INBOX)),format.raw/*37.173*/("""
                            """),format.raw/*38.29*/("""<span class="badge" id="notificationBadge">0</span>
                            </a>
                        """)))}}),format.raw/*41.22*/("""

                """),format.raw/*43.17*/("""</div>

                """),_display_(/*45.18*/for(failure <- failures) yield /*45.42*/ {_display_(Seq[Any](format.raw/*45.44*/("""
                    """),format.raw/*46.21*/("""<div class="alert failure" onclick="getForm("""),_display_(/*46.66*/failure/*46.73*/.action),format.raw/*46.80*/(""")">
                        <span class="closeButton">x</span>
                        <strong>"""),_display_(/*48.34*/Messages(constants.Form.FAILURE)),format.raw/*48.66*/("""</strong>
                        """),_display_(/*49.26*/Messages(failure.message)),format.raw/*49.51*/("""
                    """),format.raw/*50.21*/("""</div>
                """)))}),format.raw/*51.18*/("""
                """),_display_(/*52.18*/for(warning <- warnings) yield /*52.42*/ {_display_(Seq[Any](format.raw/*52.44*/("""
                    """),format.raw/*53.21*/("""<div class="alert warning" onclick="getForm("""),_display_(/*53.66*/warning/*53.73*/.action),format.raw/*53.80*/(""")">
                        <span class="closeButton">x</span>
                        <strong>"""),_display_(/*55.34*/Messages(constants.Form.WARNING)),format.raw/*55.66*/("""</strong>
                        """),_display_(/*56.26*/Messages(warning.message)),format.raw/*56.51*/("""
                    """),format.raw/*57.21*/("""</div>
                """)))}),format.raw/*58.18*/("""

                """),_display_(/*60.18*/for(success <- successes) yield /*60.43*/ {_display_(Seq[Any](format.raw/*60.45*/("""
                    """),format.raw/*61.21*/("""<div class="alert success" onclick="getForm("""),_display_(/*61.66*/success/*61.73*/.action),format.raw/*61.80*/(""")">
                        <span class="closeButton">x</span>
                        <strong>"""),_display_(/*63.34*/Messages(constants.Form.SUCCESS)),format.raw/*63.66*/("""</strong>
                        """),_display_(/*64.26*/Messages(success.message)),format.raw/*64.51*/("""
                    """),format.raw/*65.21*/("""</div>
                """)))}),format.raw/*66.18*/("""
                """),_display_(/*67.18*/for(info <- infos) yield /*67.36*/ {_display_(Seq[Any](format.raw/*67.38*/("""
                    """),format.raw/*68.21*/("""<div class="alert information" onclick="getForm("""),_display_(/*68.70*/info/*68.74*/.action),format.raw/*68.81*/(""")">
                        <span class="closeButton">x</span>
                        <strong>"""),_display_(/*70.34*/Messages(constants.Form.INFORMATION)),format.raw/*70.70*/("""</strong>
                        """),_display_(/*71.26*/Messages(info.message)),format.raw/*71.48*/("""
                    """),format.raw/*72.21*/("""</div>
                """)))}),format.raw/*73.18*/("""
            """),format.raw/*74.13*/("""</div>

            <div class="pageFlexContainer">
                <div class="first">
                    """),_display_(/*78.22*/left),format.raw/*78.26*/("""
                """),format.raw/*79.17*/("""</div>
                <div class="second">
                    """),_display_(/*81.22*/center),format.raw/*81.28*/("""
                """),format.raw/*82.17*/("""</div>
                <div class="third">
                    """),_display_(/*84.22*/right),format.raw/*84.27*/("""
                """),format.raw/*85.17*/("""</div>
            </div>
        </div>

        <div class="modal" id="commonModal">
            <div class="modalContent " id="commonModalContent"></div>
        </div>
        <div class = "modal centerScreen" id="loading">
            <img src=""""),_display_(/*93.24*/routes/*93.30*/.Assets.versioned("images/loading.gif")),format.raw/*93.69*/("""" alt = """"),_display_(/*93.79*/Messages(constants.Form.LOADING)),format.raw/*93.111*/(""""/>
        </div>
        <script type="text/javascript" src=""""),_display_(/*95.46*/routes/*95.52*/.JavaScriptRoutesController.javascriptRoutes),format.raw/*95.96*/(""""></script>
        <script src=""""),_display_(/*96.23*/routes/*96.29*/.Assets.versioned("javascripts/getForm.js")),format.raw/*96.72*/("""" type="text/javascript"></script>
        <script src=""""),_display_(/*97.23*/routes/*97.29*/.Assets.versioned("javascripts/submitForm.js")),format.raw/*97.75*/("""" type="text/javascript"></script>
        <script src=""""),_display_(/*98.23*/routes/*98.29*/.Assets.versioned("javascripts/validateForm.js")),format.raw/*98.77*/("""" type="text/javascript"></script>

        <script src=""""),_display_(/*100.23*/routes/*100.29*/.Assets.versioned("javascripts/style/alert.js")),format.raw/*100.76*/("""" type="text/javascript"></script>
        <script src=""""),_display_(/*101.23*/routes/*101.29*/.Assets.versioned("javascripts/style/ajaxLoading.js")),format.raw/*101.82*/("""" type="text/javascript"></script>
        <script src=""""),_display_(/*102.23*/routes/*102.29*/.Assets.versioned("javascripts/style/modal.js")),format.raw/*102.76*/("""" type="text/javascript"></script>
        <script src=""""),_display_(/*103.23*/routes/*103.29*/.Assets.versioned("javascripts/style/hideElement.js")),format.raw/*103.82*/("""" type="text/javascript"></script>
        <script>
                """),format.raw/*105.117*/("""
        """),format.raw/*106.9*/("""</script>
    </body>
</html>
"""))
      }
    }
  }

  def render(title:String,failures:Seq[constants.Response.Failure],warnings:Seq[constants.Response.Warning],successes:Seq[constants.Response.Success],infos:Seq[constants.Response.Info],left:Html,center:Html,right:Html,requestHeader:RequestHeader,messagesProvider:MessagesProvider,flash:Flash,loginState:LoginState): play.twirl.api.HtmlFormat.Appendable = apply(title,failures,warnings,successes,infos)(left)(center)(right)(requestHeader,messagesProvider,flash,loginState)

  def f:((String,Seq[constants.Response.Failure],Seq[constants.Response.Warning],Seq[constants.Response.Success],Seq[constants.Response.Info]) => (Html) => (Html) => (Html) => (RequestHeader,MessagesProvider,Flash,LoginState) => play.twirl.api.HtmlFormat.Appendable) = (title,failures,warnings,successes,infos) => (left) => (center) => (right) => (requestHeader,messagesProvider,flash,loginState) => apply(title,failures,warnings,successes,infos)(left)(center)(right)(requestHeader,messagesProvider,flash,loginState)

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  DATE: Sat Jul 06 14:51:28 IST 2019
                  SOURCE: /root/IdeaProjects/comdex/app/views/common.scala.html
                  HASH: e233c19164ebf6e37eb849076b0f5d311dcda289
                  MATRIX: 432->1|964->40|1450->433|1536->493|1561->498|1728->639|1742->645|1805->687|1887->743|1901->749|1962->789|2050->850|2065->856|2126->895|2178->920|2193->926|2258->970|2343->1028|2358->1034|2425->1080|2509->1137|2524->1143|2589->1187|2673->1244|2688->1250|2751->1292|2835->1349|2850->1355|2920->1404|3004->1461|3019->1467|3092->1519|3176->1576|3191->1582|3260->1630|3344->1687|3359->1693|3433->1746|3673->1959|3693->1970|3745->2001|3806->2035|3825->2045|3840->2051|3851->2078|3872->2090|3912->2092|3969->2121|4085->2209|4138->2240|4285->2359|4337->2389|4386->2444|4410->2459|4450->2461|4507->2490|4556->2512|4576->2523|4628->2554|4786->2684|4806->2694|4837->2703|4986->2824|5039->2855|5214->3002|5266->3032|5323->3061|5465->3193|5511->3211|5563->3236|5603->3260|5643->3262|5692->3283|5764->3328|5780->3335|5808->3342|5931->3438|5984->3470|6046->3505|6092->3530|6141->3551|6196->3575|6241->3593|6281->3617|6321->3619|6370->3640|6442->3685|6458->3692|6486->3699|6609->3795|6662->3827|6724->3862|6770->3887|6819->3908|6874->3932|6920->3951|6961->3976|7001->3978|7050->3999|7122->4044|7138->4051|7166->4058|7289->4154|7342->4186|7404->4221|7450->4246|7499->4267|7554->4291|7599->4309|7633->4327|7673->4329|7722->4350|7798->4399|7811->4403|7839->4410|7962->4506|8019->4542|8081->4577|8124->4599|8173->4620|8228->4644|8269->4657|8405->4766|8430->4770|8475->4787|8567->4852|8594->4858|8639->4875|8730->4939|8756->4944|8801->4961|9079->5212|9094->5218|9154->5257|9191->5267|9245->5299|9336->5363|9351->5369|9416->5413|9477->5447|9492->5453|9556->5496|9640->5553|9655->5559|9722->5605|9806->5662|9821->5668|9890->5716|9976->5774|9992->5780|10061->5827|10146->5884|10162->5890|10237->5943|10322->6000|10338->6006|10407->6053|10492->6110|10508->6116|10583->6169|10681->6337|10718->6346
                  LINES: 17->1|22->2|27->3|30->6|30->6|32->8|32->8|32->8|33->9|33->9|33->9|34->10|34->10|34->10|35->11|35->11|35->11|36->12|36->12|36->12|37->13|37->13|37->13|38->14|38->14|38->14|39->15|39->15|39->15|40->16|40->16|40->16|41->17|41->17|41->17|42->18|42->18|42->18|49->25|49->25|49->25|51->27|51->27|51->27|51->28|51->28|51->28|52->29|52->29|52->29|53->30|53->30|54->32|54->32|54->32|55->33|55->33|55->33|55->33|57->35|57->35|57->35|58->36|58->36|59->37|59->37|60->38|62->41|64->43|66->45|66->45|66->45|67->46|67->46|67->46|67->46|69->48|69->48|70->49|70->49|71->50|72->51|73->52|73->52|73->52|74->53|74->53|74->53|74->53|76->55|76->55|77->56|77->56|78->57|79->58|81->60|81->60|81->60|82->61|82->61|82->61|82->61|84->63|84->63|85->64|85->64|86->65|87->66|88->67|88->67|88->67|89->68|89->68|89->68|89->68|91->70|91->70|92->71|92->71|93->72|94->73|95->74|99->78|99->78|100->79|102->81|102->81|103->82|105->84|105->84|106->85|114->93|114->93|114->93|114->93|114->93|116->95|116->95|116->95|117->96|117->96|117->96|118->97|118->97|118->97|119->98|119->98|119->98|121->100|121->100|121->100|122->101|122->101|122->101|123->102|123->102|123->102|124->103|124->103|124->103|126->105|127->106
                  -- GENERATED --
              */
          