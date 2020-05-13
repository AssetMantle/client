package queries.responses

import play.api.libs.json.{JsValue, Json, Reads}
import transactions.Abstract.BaseResponse

object MemberCheckCorporateScanResultResponse {

  case class AssociateCorp(name: String, categories: String, subcategories: Option[String], description: Option[String])

  implicit val associateCorpReads: Reads[AssociateCorp] = Json.reads[AssociateCorp]

  case class AssociatePerson(firstName: String, middleName: Option[String], lastName: String, otherCategories: String, subcategories: Option[String], description: Option[String])

  implicit val associatePersonReads: Reads[AssociatePerson] = Json.reads[AssociatePerson]

  case class Source(url: String, categories: String, dates: String)

  implicit val sourceReads: Reads[Source] = Json.reads[Source]

  case class IDNumber(`type`: String, idNotes: String, number: String)

  implicit val idNUmberReads: Reads[IDNumber] = Json.reads[IDNumber]

  case class OfficialList(keyword: String, category: String, description: String, country: String, isCurrent: Boolean)

  implicit val officialListReads: Reads[OfficialList] = Json.reads[OfficialList]

  case class Country(countryType: String, countryValue: String)

  implicit val countryReads: Reads[Country] = Json.reads[Country]

  case class Location(country: String, city: Option[String], address: String)

  implicit val locationReads: Reads[Location] = Json.reads[Location]

  case class NameDetail(nameType: String, entityName: String)

  implicit val nameDetailReads: Reads[NameDetail] = Json.reads[NameDetail]

  case class Description(description1: String, description2: String, description3: String)

  implicit val descriptionReads: Reads[Description] = Json.reads[Description]

  case class Descriptions(descriptions: Seq[Description])

  implicit val descriptionsReads: Reads[Descriptions] = Json.reads[Descriptions]

  case class Entity(uniqueId: Int, categories: String, subcategory: Option[String], primaryName: String,
                    primaryLocation: Option[String], generalInfo: JsValue, furtherInformation: Option[String],
                    xmlFurtherInformation: Option[JsValue], enterDate: Option[String], lastReviewed: Option[String],
                    descriptions: Seq[Description], nameDetails: Seq[NameDetail], originalScriptNames: JsValue,
                    locations: Seq[Location], countries: Seq[Country], officialLists: Seq[OfficialList],
                    idNumbers: Seq[IDNumber], sources: Seq[Source], linkedIndividuals: Seq[AssociatePerson],
                    linkedCompanies: Seq[AssociateCorp])

  implicit val entityReads: Reads[Entity] = Json.reads[Entity]

  case class Response(id: Int, entity: Entity) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
