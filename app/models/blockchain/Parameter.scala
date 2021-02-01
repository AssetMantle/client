package models.blockchain

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.{Parameter => abstractParameter}
import models.Trait.Logged
import models.common.Parameters._
import models.common.TransactionMessages._
import models.masterTransaction
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Parameter(parameterType: String, value: abstractParameter, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Parameters @Inject()(
                            protected val databaseConfigProvider: DatabaseConfigProvider,
                            configuration: Configuration,
                            masterTransactionNotifications: masterTransaction.Notifications,
                          )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_PARAMETER

  import databaseConfig.profile.api._

  private[models] val parameterTable = TableQuery[ParameterTable]

  case class ParameterSerialized(parameterType: String, value: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Parameter = Parameter(parameterType = parameterType, value = utilities.JSON.convertJsonStringToObject[abstractParameter](value), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(parameter: Parameter): ParameterSerialized = ParameterSerialized(parameterType = parameter.parameterType, value = Json.toJson(parameter.value).toString, createdBy = parameter.createdBy, createdOn = parameter.createdOn, createdOnTimeZone = parameter.createdOnTimeZone, updatedBy = parameter.updatedBy, updatedOn = parameter.updatedOn, updatedOnTimeZone = parameter.updatedOnTimeZone)

  private def add(parameter: Parameter): Future[String] = db.run((parameterTable returning parameterTable.map(_.parameterType) += serialize(parameter)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(parameter: Parameter): Future[Int] = db.run(parameterTable.insertOrUpdate(serialize(parameter)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def tryGetByType(parameterType: String): Future[ParameterSerialized] = db.run(parameterTable.filter(_.parameterType === parameterType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getAllParameters: Future[Seq[ParameterSerialized]] = db.run(parameterTable.result)

  private[models] class ParameterTable(tag: Tag) extends Table[ParameterSerialized](tag, "Parameter") {

    def * = (parameterType, value, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (ParameterSerialized.tupled, ParameterSerialized.unapply)

    def parameterType = column[String]("parameterType", O.PrimaryKey)

    def value = column[String]("value")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(parameter: Parameter): Future[String] = add(parameter)

    def insertOrUpdate(parameter: Parameter): Future[Int] = upsert(parameter)

    def tryGet(parameterType: String): Future[Parameter] = tryGetByType(parameterType).map(_.deserialize)

    def tryGetSlashingParameter: Future[SlashingParameter] = tryGetByType(constants.Blockchain.ParameterType.SLASHING).map(_.deserialize).map(_.value.asSlashingParameter)

    def getAll: Future[Seq[Parameter]] = getAllParameters.map(_.map(_.deserialize))

  }
}