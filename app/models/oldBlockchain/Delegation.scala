package models.oldBlockchain

import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class Delegation(delegatorAddress: String, validatorAddress: String, shares: BigDecimal)

@Singleton
class Delegations @Inject()(
                             @NamedDatabase("oldExplorer")
                             protected val databaseConfigProvider: DatabaseConfigProvider,
                           )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = "oldEXPLORER_DELEGATION"

  import databaseConfig.profile.api._

  private[models] val delegationTable = TableQuery[DelegationTable]

  private def getByDelegatorAndValidatorAddress(delegatorAddress: String, validatorAddress: String): Future[Option[Delegation]] = db.run(delegationTable.filter(x => x.delegatorAddress === delegatorAddress && x.validatorAddress === validatorAddress).result.headOption)

  private[models] class DelegationTable(tag: Tag) extends Table[Delegation](tag, "Delegation") {

    def * = (delegatorAddress, validatorAddress, shares) <> (Delegation.tupled, Delegation.unapply)

    def delegatorAddress = column[String]("delegatorAddress", O.PrimaryKey)

    def validatorAddress = column[String]("validatorAddress", O.PrimaryKey)

    def shares = column[BigDecimal]("shares")
  }

  object Service {

    def get(delegatorAddress: String, operatorAddress: String): Future[Option[Delegation]] = getByDelegatorAndValidatorAddress(delegatorAddress = delegatorAddress, validatorAddress = operatorAddress)

  }

}