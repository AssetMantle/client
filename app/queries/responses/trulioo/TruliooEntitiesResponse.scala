package queries.responses.trulioo

import play.api.libs.json.{JsValue, Json, Reads}
import transactions.Abstract.BaseResponse

object TruliooEntitiesResponse {

  case class PersonalInfoAdditionalFields(FullName: String)

  implicit val personalInfoAdditionalFieldsReads: Reads[PersonalInfoAdditionalFields] = Json.reads[PersonalInfoAdditionalFields]

  case class PersonalInfo(FirstGivenName: String, MiddleName: String, FirstSurName: String, SecondSurname: Option[String], ISOLatin1Name: Option[String], DayOfBirth: Int, MonthOfBirth: Int, YearOfBirth: Int, MinimumAge: Option[Int], Gender: Option[String], AdditionalFields: Option[PersonalInfoAdditionalFields])

  implicit val personalInfoReads: Reads[PersonalInfo] = Json.reads[PersonalInfo]

  case class LocationAdditionalFields(Address1: String)

  implicit val locationAdditionalFieldsReads: Reads[LocationAdditionalFields] = Json.reads[LocationAdditionalFields]

  case class Location(BuildingNumber: String, BuildingName: Option[String], UnitNumber: String, StreetName: String, StreetType: String, City: Option[String], Suburb: String, County: Option[String], StateProvinceCode: String, PostalCode: String, POBox: Option[String], AdditionalFields: Option[LocationAdditionalFields])

  implicit val locationReads: Reads[Location] = Json.reads[Location]

  case class Communication(MobileNumber: Option[String], Telephone: String, Telephone2: Option[String], EmailAddress: Option[String])

  implicit val communicationReads: Reads[Communication] = Json.reads[Communication]

  case class DriverLicence(Number: String, State: String, DayOfExpiry: String, MonthOfExpiry: String, YearOfExpiry: String)

  implicit val driverLicenceReads: Reads[DriverLicence] = Json.reads[DriverLicence]

  case class NationalIds(Number: String, Type: String, DistrictOfIssue: Option[String], CityOfIssue: Option[String], ProvinceOfIssue: Option[String], CountyOfIssue: Option[String])

  implicit val nationalIdsReads: Reads[NationalIds] = Json.reads[NationalIds]

  case class Passport(Mrz1: Option[String], Mrz2: Option[String], Number: Option[String], DayOfExpiry: Option[Int], MonthOfExpiry: Option[Int], YearOfExpiry: Option[Int])

  implicit val passportReads: Reads[Passport] = Json.reads[Passport]

  case class Response(PersonInfo: PersonalInfo, Location: Location, Communication: Communication, DriverLicence: Option[DriverLicence], NationalIds: Option[Seq[NationalIds]], Passport: Option[Passport], CountrySpecific: JsValue) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
  implicit val seqResponseReads: Reads[Seq[Response]] = Reads.seq[Response]

}
