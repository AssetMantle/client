package models.kycCheck.worldcheck

import models.Trait.Logged
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

case class WorldCheckScreeningChecks (id: String,
                                      checkType: String,
                                      status: Option[Boolean] = None,
                                      createdBy: Option[String] = None,
                                      createdOn: Option[Timestamp] = None,
                                      createdOnTimeZone: Option[String] = None,
                                      updatedBy: Option[String] = None,
                                      updatedOn: Option[Timestamp] = None,
                                      updatedOnTimeZone: Option[String] = None)extends Logged

@Singleton
class WorldCheckKycFiles @Inject() (
                                     protected val databaseConfigProvider: DatabaseConfigProvider,
                                     configuration: Configuration
                                   )(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.WORLD_CHECK_KYC_FILE

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val worldCheckKycFileTable = TableQuery[WorldCheckKycFileTable]
