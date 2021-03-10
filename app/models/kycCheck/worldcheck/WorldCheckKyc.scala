package models.kycCheck.worldcheck

import exceptions.BaseException
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class WorldCheckKyc(
    id: String,
    email: String,
    counterParty: String,
    requester: String,
    status: Option[Boolean] = None,
    createdBy: Option[String] = None,
    createdOn: Option[Timestamp] = None,
    createdOnTimeZone: Option[String] = None,
    updatedBy: Option[String] = None,
    updatedOn: Option[Timestamp] = None,
    updatedOnTimeZone: Option[String] = None
) extends Logged

@Singleton
class WorldCheckKYCs @Inject() (
    protected val databaseConfigProvider: DatabaseConfigProvider,
    configuration: Configuration
)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.WORLD_CHECK_KYC

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val worldCheckKYCTable = TableQuery[WorldCheckTable]

  private def add(worldCheck: WorldCheckKyc): Future[String] =
    db.run(
        (worldCheckKYCTable returning worldCheckKYCTable
          .map(_.id) += worldCheck).asTry
      )
      .map {
        case Success(result) => result
        case Failure(exception) =>
          exception match {
            case psqlException: PSQLException =>
              throw new BaseException(
                constants.Response.PSQL_EXCEPTION,
                psqlException
              )
          }
      }

  private def getByRequesterId(
      requester: String
  ): Future[Seq[WorldCheckKyc]] =
    db.run(
      worldCheckKYCTable.filter(_.requester === requester).result
    )

  private def getByIdRequesterId(
      id: String,
      requester: String
  ): Future[WorldCheckKyc] =
    db.run(
      worldCheckKYCTable
        .filter(_.id === id)
        .filter(_.requester === requester)
        .result
        .head
    )

  private[models] class WorldCheckTable(tag: Tag)
      extends Table[WorldCheckKyc](tag, "WorldCheckKyc") {

    def id = column[String]("id", O.PrimaryKey)

    def email = column[String]("email")

    def counterParty = column[String]("counterParty")

    def requester = column[String]("requester")

    def status = column[Boolean]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

    override def * =
      (
        id,
        email,
        counterParty,
        requester,
        status.?,
        createdBy.?,
        createdOn.?,
        createdOnTimeZone.?,
        updatedBy.?,
        updatedOn.?,
        updatedOnTimeZone.?
      ) <> (WorldCheckKyc.tupled, WorldCheckKyc.unapply)
  }

  object Service {

    def create(
        email: String,
        counterParty: String,
        requester: String
    ): Future[String] =
      add(
        WorldCheckKyc(
          utilities.IDGenerator.requestID(),
          email = email,
          counterParty = counterParty,
          requester = requester
        )
      )

    def get(requester: String): Future[Seq[WorldCheckKyc]] =
      getByRequesterId(requester)

    def getByIdAndRequester(
        id: String,
        requester: String
    ): Future[WorldCheckKyc] =
      getByIdRequesterId(id, requester)
  }

}
