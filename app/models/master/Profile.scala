package models.master

import exceptions.BaseException
import models.Trait.Logged
import models.common.Serializable.SocialProfile
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Profile(accountID: String, name: String, description: String, socialProfiles: Seq[SocialProfile], verified: Boolean = false, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Profiles @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_PROFILE

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private implicit val logger: Logger = Logger(this.getClass)

  private[models] val profileTable = TableQuery[ProfileTable]

  case class ProfileSerialized(accountID: String, name: String, description: String, socialProfiles: String, verified: Boolean, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Profile = Profile(accountID = accountID, name = name, description = description, socialProfiles = utilities.JSON.convertJsonStringToObject[Seq[SocialProfile]](socialProfiles), verified = verified, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(profile: Profile): ProfileSerialized = ProfileSerialized(accountID = profile.accountID, name = profile.name, description = profile.description, socialProfiles = Json.toJson(profile.socialProfiles).toString, verified = profile.verified, createdBy = profile.createdBy, createdOn = profile.createdOn, createdOnTimeZone = profile.createdOnTimeZone, updatedBy = profile.updatedBy, updatedOn = profile.updatedOn, updatedOnTimeZone = profile.updatedOnTimeZone)

  private def add(profile: Profile): Future[String] = db.run((profileTable returning profileTable.map(_.name) += serialize(profile)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(profile: Profile): Future[Int] = db.run(profileTable.insertOrUpdate(serialize(profile)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.WALLET_UPSERT_FAILED, psqlException)
    }
  }

  private def getByID(id: String): Future[Option[ProfileSerialized]] = db.run(profileTable.filter(_.accountID === id).result.headOption)

  private def tryGetByID(id: String): Future[ProfileSerialized] = db.run(profileTable.filter(_.accountID === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class ProfileTable(tag: Tag) extends Table[ProfileSerialized](tag, "Profile") {

    def * = (accountID, name, description, socialProfiles, verified, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (ProfileSerialized.tupled, ProfileSerialized.unapply)

    def accountID = column[String]("accountID", O.PrimaryKey)

    def name = column[String]("name")

    def description = column[String]("description")

    def socialProfiles = column[String]("socialProfiles")

    def verified = column[Boolean]("verified")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def get(id: String): Future[Option[Profile]] = getByID(id).map(_.map(_.deserialize))

    def create(id: String, name: String, description: String, socialProfiles: Seq[SocialProfile]): Future[String] = add(Profile(accountID = id, name = name, description = description, socialProfiles = socialProfiles))

    def insertOrUpdate(profile: Profile): Future[Int] = upsert(profile)

  }

}

