package queries.responses

import play.api.libs.json.{JsPath, JsValue, Json, Reads}
import transactions.Abstract.BaseResponse

object MemberCheckMemberScanResultResponse {

  case class AssociateCorp(name: String, categories: String, subcategories: Option[String], description: Option[String])

  implicit val associateCorpReads: Reads[AssociateCorp] = Json.reads[AssociateCorp]

  case class AssociatePerson(firstName: String, middleName: Option[String], lastName: String, otherCategories: String, subcategories: Option[String], description: String)

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

  case class Date(dateType: String, dateValue: String)

  implicit val dateReads: Reads[Date] = Json.reads[Date]

  case class Role(title: String, `type`: Option[String], status: Option[String], country: String, from: Option[String], to: Option[String])

  implicit val roleReads: Reads[Role] = Json.reads[Role]

  case class NameDetail(nameType: String, firstName: String, middleName: Option[String], lastName: String)

  implicit val nameDetailReads: Reads[NameDetail] = Json.reads[NameDetail]

  case class Description(description1: String, description2: String, description3: String)

  implicit val descriptionReads: Reads[Description] = Json.reads[Description]

  case class Descriptions(descriptions: Seq[Description])

  implicit val descriptionsReads: Reads[Descriptions] = Json.reads[Descriptions]

  case class EntityPart1(uniqueId: Int, categories: String, subcategory: Option[String], gender: Option[String], deceased: String,
                         primaryFirstName: String, primaryMiddleName: Option[String], primaryLastName: String,
                         title: Option[String], position: Option[String], dateOfBirth: Option[String], deceasedDate: Option[String],
                         placeOfBirth: String, primaryLocation: Option[String], image: Option[String])

  implicit val entityPart2Reads: Reads[EntityPart2] = Json.reads[EntityPart2]

  case class EntityPart2(generalInfo: JsValue, furtherInformation: Option[String], xmlFurtherInformation: Option[JsValue], enterDate: Option[String],
                         lastReviewed: Option[String], descriptions: Seq[Description], nameDetails: Seq[NameDetail],
                         originalScriptNames: JsValue, roles: Option[Seq[Role]], importantDates: Seq[Date], locations: Seq[Location],
                         countries: Seq[Country], officialLists: Seq[OfficialList], idNumbers: Seq[IDNumber], sources: Seq[Source],
                         linkedIndividuals: Seq[AssociatePerson], linkedCompanies: Seq[AssociateCorp])

  implicit val entityPart1Reads: Reads[EntityPart1] = Json.reads[EntityPart1]

  case class Entity(uniqueId: Int, categories: String, subcategory: Option[String], gender: Option[String], deceased: String,
                    primaryFirstName: String, primaryMiddleName: Option[String], primaryLastName: String,
                    title: Option[String], position: Option[String], dateOfBirth: Option[String], deceasedDate: Option[String],
                    placeOfBirth: String, primaryLocation: Option[String], image: Option[String], generalInfo: JsValue,
                    furtherInformation: Option[String], xmlFurtherInformation: Option[JsValue], enterDate: Option[String], lastReviewed: Option[String],
                    descriptions: Seq[Description], nameDetails: Seq[NameDetail], originalScriptNames: JsValue,
                    roles: Option[Seq[Role]], importantDates: Seq[Date], locations: Seq[Location], countries: Seq[Country],
                    officialLists: Seq[OfficialList], idNumbers: Seq[IDNumber], sources: Seq[Source], linkedIndividuals: Seq[AssociatePerson],
                    linkedCompanies: Seq[AssociateCorp])

  def entity(entityPart1: EntityPart1, entityPart2: EntityPart2): Entity = Entity(
    entityPart1.uniqueId, entityPart1.categories, entityPart1.subcategory, entityPart1.gender,
    entityPart1.deceased, entityPart1.primaryFirstName, entityPart1.primaryMiddleName, entityPart1.primaryLastName,
    entityPart1.title, entityPart1.position, entityPart1.dateOfBirth, entityPart1.deceasedDate, entityPart1.placeOfBirth,
    entityPart1.primaryLocation, entityPart1.image, entityPart2.generalInfo, entityPart2.furtherInformation,
    entityPart2.xmlFurtherInformation, entityPart2.enterDate, entityPart2.lastReviewed, entityPart2.descriptions,
    entityPart2.nameDetails, entityPart2.originalScriptNames, entityPart2.roles, entityPart2.importantDates,
    entityPart2.locations, entityPart2.countries, entityPart2.officialLists, entityPart2.idNumbers,
    entityPart2.sources, entityPart2.linkedIndividuals, entityPart2.linkedCompanies)

  case class ResponsePart1(id: Int, person: EntityPart1) extends BaseResponse

  implicit val responsePart1Reads: Reads[ResponsePart1] = Json.reads[ResponsePart1]

  case class ResponsePart2(id: Int, person: EntityPart2) extends BaseResponse

  implicit val responsePart2Reads: Reads[ResponsePart2] = Json.reads[ResponsePart2]

  case class Response(id: Int, person: Entity)

}
