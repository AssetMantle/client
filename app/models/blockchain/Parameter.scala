package models.blockchain

import exceptions.BaseException
import models.Abstract.{Parameter => abstractParameter}
import models.common.Parameters._
import models.common.ProposalContents.ParameterChange
import models.common.Serializable.Coin
import models.traits.Logging
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import queries.blockchain.params._
import slick.jdbc.JdbcProfile
import utilities.MicroNumber

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Parameter(parameterType: String, value: abstractParameter, createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging

@Singleton
class Parameters @Inject()(
                            protected val databaseConfigProvider: DatabaseConfigProvider,
                            utilitiesOperations: utilities.Operations,
                            getAuthParams: GetAuth,
                            getBankParams: GetBank,
                            getGovParams: GetGov,
                            getDistributionParams: GetDistribution,
                            getMintParams: GetMint,
                            getSlashingParams: GetSlashing,
                            getStakingParams: GetStaking
                          )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_PARAMETER

  import databaseConfig.profile.api._

  private[models] val parameterTable = TableQuery[ParameterTable]

  case class ParameterSerialized(parameterType: String, value: String, createdBy: Option[String], createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) {
    def deserialize: Parameter = Parameter(parameterType = parameterType, value = utilities.JSON.convertJsonStringToObject[abstractParameter](value), createdBy = createdBy, createdOnMillisEpoch = createdOnMillisEpoch, updatedBy = updatedBy, updatedOnMillisEpoch = updatedOnMillisEpoch)
  }

  def serialize(parameter: Parameter): ParameterSerialized = ParameterSerialized(parameterType = parameter.parameterType, value = Json.toJson(parameter.value).toString, createdBy = parameter.createdBy, createdOnMillisEpoch = parameter.createdOnMillisEpoch, updatedBy = parameter.updatedBy, updatedOnMillisEpoch = parameter.updatedOnMillisEpoch)

  private def add(parameter: Parameter): Future[String] = db.run((parameterTable returning parameterTable.map(_.parameterType) += serialize(parameter)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PARAMETER_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(parameter: Parameter): Future[Int] = db.run(parameterTable.insertOrUpdate(serialize(parameter)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PARAMETER_UPSERT_FAILED, psqlException)
    }
  }

  private def tryGetByType(parameterType: String): Future[ParameterSerialized] = db.run(parameterTable.filter(_.parameterType === parameterType).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.PARAMETER_NOT_FOUND, noSuchElementException)
    }
  }

  private def getAllParameters: Future[Seq[ParameterSerialized]] = db.run(parameterTable.result)

  private[models] class ParameterTable(tag: Tag) extends Table[ParameterSerialized](tag, "Parameter") {

    def * = (parameterType, value, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (ParameterSerialized.tupled, ParameterSerialized.unapply)

    def parameterType = column[String]("parameterType", O.PrimaryKey)

    def value = column[String]("value")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")
  }

  object Service {

    def create(parameter: Parameter): Future[String] = add(parameter)

    def insertOrUpdate(parameter: Parameter): Future[Int] = upsert(parameter)

    def tryGet(parameterType: String): Future[Parameter] = tryGetByType(parameterType).map(_.deserialize)

    def tryGetAuthParameter: Future[AuthParameter] = tryGetByType(constants.Blockchain.ParameterType.AUTH).map(_.deserialize).map(_.value.asAuthParameter)

    def tryGetBankParameter: Future[BankParameter] = tryGetByType(constants.Blockchain.ParameterType.BANK).map(_.deserialize).map(_.value.asBankParameter)

    def tryGetDistributionParameter: Future[DistributionParameter] = tryGetByType(constants.Blockchain.ParameterType.DISTRIBUTION).map(_.deserialize).map(_.value.asDistributionParameter)

    def tryGetGovernanceParameter: Future[GovernanceParameter] = tryGetByType(constants.Blockchain.ParameterType.GOVERNANCE).map(_.deserialize).map(_.value.asGovernanceParameter)

    def tryGetMintingParameter: Future[MintingParameter] = tryGetByType(constants.Blockchain.ParameterType.MINT).map(_.deserialize).map(_.value.asMintingParameter)

    def tryGetSlashingParameter: Future[SlashingParameter] = tryGetByType(constants.Blockchain.ParameterType.SLASHING).map(_.deserialize).map(_.value.asSlashingParameter)

    def tryGetStakingParameter: Future[StakingParameter] = tryGetByType(constants.Blockchain.ParameterType.STAKING).map(_.deserialize).map(_.value.asStakingParameter)

    def tryGetClassificationParameter: Future[ClassificationParameter] = tryGetByType(constants.Blockchain.ParameterType.CLASSIFICATIONS).map(_.deserialize).map(_.value.asClassificationParameter)

    def tryGetIdentityParameter: Future[IdentityParameter] = tryGetByType(constants.Blockchain.ParameterType.IDENTITIES).map(_.deserialize).map(_.value.asIdentityParameter)

    def tryGetOrderParameter: Future[OrderParameter] = tryGetByType(constants.Blockchain.ParameterType.ORDERS).map(_.deserialize).map(_.value.asOrderParameter)

    def tryGetSplitParameter: Future[SplitParameter] = tryGetByType(constants.Blockchain.ParameterType.SPLITS).map(_.deserialize).map(_.value.asSplitParameter)

    def getAll: Future[Seq[Parameter]] = getAllParameters.map(_.map(_.deserialize))

  }

  object Utility {

    def onParameterChange(parameterChange: ParameterChange): Future[Unit] = {
      val update = utilitiesOperations.traverse(parameterChange.changes)(change => {
        val parameter: Future[abstractParameter] = change.subspace match {
          case constants.Blockchain.ParameterType.AUTH =>
            val authResponse = getAuthParams.Service.get()
            for {
              authResponse <- authResponse
            } yield authResponse.params.toParameter
          case constants.Blockchain.ParameterType.BANK =>
            val bankResponse = getBankParams.Service.get()
            for {
              bankResponse <- bankResponse
            } yield bankResponse.params.toParameter
          case constants.Blockchain.ParameterType.DISTRIBUTION =>
            val distributionResponse = getDistributionParams.Service.get()
            for {
              distributionResponse <- distributionResponse
            } yield distributionResponse.params.toParameter
          case constants.Blockchain.ParameterType.GOVERNANCE =>
            val govResponse = getGovParams.Service.get()
            for {
              govResponse <- govResponse
            } yield govResponse.toParameter
          case constants.Blockchain.ParameterType.MINT =>
            val mintResponse = getMintParams.Service.get()
            for {
              mintResponse <- mintResponse
            } yield mintResponse.params.toParameter
          case constants.Blockchain.ParameterType.SLASHING =>
            val slashingResponse = getSlashingParams.Service.get()
            for {
              slashingResponse <- slashingResponse
            } yield slashingResponse.params.toParameter
          case constants.Blockchain.ParameterType.STAKING =>
            val stakingResponse = getStakingParams.Service.get()
            for {
              stakingResponse <- stakingResponse
            } yield stakingResponse.params.toParameter
          case constants.Blockchain.ParameterType.CRISIS => Future(CrisisParameter(Coin(denom = "", amount = MicroNumber.zero)))
          case constants.Blockchain.ParameterType.IBC => Future(IBCParameter(allowedClients = Seq.empty))
          case constants.Blockchain.ParameterType.TRANSFER => Future(TransferParameter(receiveEnabled = true, sendEnabled = true))
        }

        def upsertParameter(parameterValue: abstractParameter) = if (parameterValue.parameterType != constants.Blockchain.ParameterType.CRISIS || parameterValue.parameterType != constants.Blockchain.ParameterType.IBC || parameterValue.parameterType != constants.Blockchain.ParameterType.TRANSFER)
          Service.insertOrUpdate(Parameter(parameterType = parameterValue.parameterType, value = parameterValue)) else Future(0)

        for {
          parameter <- parameter
          _ <- upsertParameter(parameter)
        } yield ()
      })

      (for {
        _ <- update
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }
  }

}