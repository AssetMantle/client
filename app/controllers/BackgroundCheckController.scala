package controllers

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import queries.{GetTruliooAuthentication, GetTruliooConsents, GetTruliooCountryCodes, GetTruliooCountrySubdivisions, GetTruliooDataSources, GetTruliooDetailedConsents, GetTruliooEntities, GetTruliooFields, GetTruliooRecommendedFields, GetTruliooTransactionRecords}
import transactions.TruliooVerify

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BackgroundCheckController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                          getTruliooAuthentication: GetTruliooAuthentication,
                                          getTruliooCountryCodes: GetTruliooCountryCodes,
                                          getTruliooEntities: GetTruliooEntities,
                                          getTruliooConsents: GetTruliooConsents,
                                          getTruliooDetailedConsents: GetTruliooDetailedConsents,
                                          getTruliooDataSources: GetTruliooDataSources,
                                          getTruliooFields: GetTruliooFields,
                                          getTruliooRecommendedFields: GetTruliooRecommendedFields,
                                          getTruliooTransactionRecords: GetTruliooTransactionRecords,
                                          getTruliooCountrySubdivisions: GetTruliooCountrySubdivisions,
                                          truliooVerify: TruliooVerify
                                         )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def testTruliooAPI: Action[AnyContent] = Action.async { implicit request =>
//    authTest
//    countryCodes
//    entities
//    fields
//    recommdendedFields
//    consents
//    detailedConsents
//    countrySubdivisions
//    dataSources
//    transactionRecord
    verify
  }

  //auth test
  def authTest = {
      val response = getTruliooAuthentication.Service.get()
      (for {
        response <- response
      } yield {
        val printValue = response.body.mkString
        println(printValue)
        Ok(views.html.test(printValue))
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.test(baseException.failure.message))
      }
  }

  //country Codes
  def countryCodes = {
    val response = getTruliooCountryCodes.Service.get()
    (for {
      response <- response
    } yield {
      val printValue = response.body
      println(printValue, printValue.foreach(x => println(x)))
      Ok(views.html.test(printValue.mkString))
    }).recover {
      case baseException: BaseException => InternalServerError(views.html.test(baseException.failure.message))
    }
  }

  //entities
  def entities = {
    val response = getTruliooEntities.Service.get(countryCode = "AU")
    (for {
      response <- response
    } yield {
      val printValue = response
      println(printValue, printValue.foreach(x => println(x)))
      Ok(views.html.test(printValue.mkString))
    }).recover {
      case baseException: BaseException => InternalServerError(views.html.test(baseException.failure.message))
    }
  }

  //_fields
  def fields = {
    val response = getTruliooFields.Service.get(countryCode = "AU")
    (for {
      response <- response
    } yield {
      val printValue = response
      println(printValue)
      Ok(views.html.test(printValue.toString))
    }).recover {
      case baseException: BaseException => InternalServerError(views.html.test(baseException.failure.message))
    }
  }
  // recommendedFields
  def  recommdendedFields= {
    val response = getTruliooRecommendedFields.Service.get(countryCode = "AU")
    (for {
      response <- response
    } yield {
      val printValue = response
      println(printValue)
      Ok(views.html.test(printValue.toString))
    }).recover {
      case baseException: BaseException => InternalServerError(views.html.test(baseException.failure.message))
    }
  }

  //consents
  def consents = {
    val response = getTruliooConsents.Service.get(countryCode = "AU")
    (for {
      response <- response
    } yield {
      val printValue = response
      println(printValue)
      Ok(views.html.test(printValue.body.mkString))
    }).recover {
      case baseException: BaseException => InternalServerError(views.html.test(baseException.failure.message))
    }
  }

  // detailedConsents
  def detailedConsents = {
    val response = getTruliooDetailedConsents.Service.get(countryCode = "AU")
    (for {
      response <- response
    } yield {
      val printValue = response
      println(printValue)
      Ok(views.html.test(printValue.toString))
    }).recover {
      case baseException: BaseException => InternalServerError(views.html.test(baseException.failure.message))
    }
  }

  // countrySubdivisions
  def countrySubdivisions = {
    val response = getTruliooCountrySubdivisions.Service.get(countryCode = "AU")
    (for {
      response <- response
    } yield {
      val printValue = response
      println(printValue)
      Ok(views.html.test(printValue.toString))
    }).recover {
      case baseException: BaseException => InternalServerError(views.html.test(baseException.failure.message))
    }
  }

  // dataSources
  def dataSources = {
    val response = getTruliooDataSources.Service.get(countryCode = "AU")
    (for {
      response <- response
    } yield {
      val printValue = response
      println(printValue)
      Ok(views.html.test(printValue.toString))
    }).recover {
      case baseException: BaseException => InternalServerError(views.html.test(baseException.failure.message))
    }
  }

  // transactionRecord
  def transactionRecord = {
    val response = getTruliooTransactionRecords.Service.get(id = "259888b7-71e4-1e8a-6a59-ac52bd9c8386")
    (for {
      response <- response
    } yield {
      val printValue = response
      println(printValue)
      Ok(views.html.test(printValue.toString))
    }).recover {
      case baseException: BaseException => InternalServerError(views.html.test(baseException.failure.message))
    }
  }

  // POST verify
  def verify = {
    val response = truliooVerify.Service.post(request =  truliooVerify.Request(true,false,"Identity Verification" ,Seq("Visa Verification"),"AU", truliooVerify.DataFields(truliooVerify.PersonInfo("J","Smith", 5, 3, 1983),truliooVerify.Location("Lawford","3108"),None,None)))
    (for {
      response <- response
    } yield {
      val printValue = response.body
      println(printValue)
      Ok(views.html.test(printValue.toString))
    }).recover {
      case baseException: BaseException => InternalServerError(views.html.test(baseException.failure.message))
    }
  }

  //  def blockchainAddKey: Action[AnyContent] = Action.async { implicit request =>
  //    views.companion.blockchain.AddKey.form.bindFromRequest().fold(
  //      formWithErrors => {
  //        Future(BadRequest(views.html.component.blockchain.addKey(formWithErrors)))
  //      },
  //      addKeyData => {
  //        val postRequest = transactionsAddKey.Service.post(transactionsAddKey.Request(addKeyData.name, addKeyData.password, addKeyData.mnemonic))
  //        (for {
  //          _ <- postRequest
  //        } yield Ok(views.html.index(successes = Seq(constants.Response.KEY_ADDED)))
  //          ).recover {
  //          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
  //        }
  //      }
  //    )
  //  }
}
