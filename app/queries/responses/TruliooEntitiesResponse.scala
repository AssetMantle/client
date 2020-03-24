package queries.responses

import play.api.libs.json.{JsValue, Json, Reads}
import transactions.Abstract.BaseResponse

object TruliooEntitiesResponse {

  case class PersonalInfo(FirstGivenName: String, MiddleName: String, FirstSurName: String, DayOfBirth: String, MonthOfBirth: String, YearOfBirth: String, Gender: String)

  implicit val personalInfoReads: Reads[PersonalInfo] = Json.reads[PersonalInfo]

  case class Location(BuildingNumber: String, UnitNumber: String, StreetName: String, StreetType: String, Suburb: String, StateProvinceCode: String, PostalCode: String)

  implicit val locationReads: Reads[Location] = Json.reads[Location]

  case class Communication(Telephone: String, EmailAddress: String)

  implicit val communicationReads: Reads[Communication] = Json.reads[Communication]

  case class DriverLicence(Number: String, State: String, DayOfExpiry: String, MonthOfExpiry: String, YearOfExpiry: String)

  implicit val driverLicenceReads: Reads[DriverLicence] = Json.reads[DriverLicence]

  //  case class NationalIds(Number: String, Type: String)
//  implicit val nationalIdsReads: Reads[NationalIds] = Json.reads[NationalIds]

  case class Passport(Mrz1: String, Mrz2: String, Number: String, DayOfExpiry: String, MonthOfExpiry: String, YearOfExpiry: String)

  implicit val passportReads: Reads[Passport] = Json.reads[Passport]

//  case class CountrySpecific(AU: JsValue)
//
//  implicit val countrySpecificReads: Reads[CountrySpecific] = Json.reads[CountrySpecific]


//  case class Response(PersonInfo: PersonalInfo, Location: Location, Communication: Communication, DriverLicence:DriverLicence, NationalIds: JsValue, Passport:Passport, CountrySpecific: JsValue) extends BaseResponse
  case class Response(PersonInfo: JsValue, Location: JsValue, Communication: JsValue, DriverLicence:Option[JsValue], NationalIds: Option[JsValue], Passport:JsValue, CountrySpecific: JsValue) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
  implicit val seqResponseReads: Reads[Seq[Response]] = Reads.seq[Response]

}
