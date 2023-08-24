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
                            getStakingParams: GetStaking,
                            getClassificationParams: GetClassification,
                            getAssetParams: GetAsset,
                            getIdentityParams: GetIdentity,
                            getMaintainerParams: GetMaintainer,
                            getMetaParams: GetMeta,
                            getOrderParams: GetOrder,
                            getSplitParams: GetSplit,
                          )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_PARAMETER

  import databaseConfig.profile.api._

  val parameterTable = TableQuery[ParameterTable]

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

  class ParameterTable(tag: Tag) extends Table[ParameterSerialized](tag, "Parameter") {

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

    def tryGetAuthParameter: Future[AuthParameter] = tryGetByType(schema.constants.Parameter.Type.AUTH).map(_.deserialize).map(_.value.asAuthParameter)

    def tryGetBankParameter: Future[BankParameter] = tryGetByType(schema.constants.Parameter.Type.BANK).map(_.deserialize).map(_.value.asBankParameter)

    def tryGetDistributionParameter: Future[DistributionParameter] = tryGetByType(schema.constants.Parameter.Type.DISTRIBUTION).map(_.deserialize).map(_.value.asDistributionParameter)

    def tryGetGovernanceParameter: Future[GovernanceParameter] = tryGetByType(schema.constants.Parameter.Type.GOVERNANCE).map(_.deserialize).map(_.value.asGovernanceParameter)

    def tryGetMintingParameter: Future[MintingParameter] = tryGetByType(schema.constants.Parameter.Type.MINT).map(_.deserialize).map(_.value.asMintingParameter)

    def tryGetSlashingParameter: Future[SlashingParameter] = tryGetByType(schema.constants.Parameter.Type.SLASHING).map(_.deserialize).map(_.value.asSlashingParameter)

    def tryGetStakingParameter: Future[StakingParameter] = tryGetByType(schema.constants.Parameter.Type.STAKING).map(_.deserialize).map(_.value.asStakingParameter)

    def tryGetClassificationParameter: Future[ClassificationParameter] = tryGetByType(schema.constants.Parameter.Type.CLASSIFICATIONS).map(_.deserialize).map(_.value.asClassificationParameter)

    def tryGetIdentityParameter: Future[IdentityParameter] = tryGetByType(schema.constants.Parameter.Type.IDENTITIES).map(_.deserialize).map(_.value.asIdentityParameter)

    def tryGetOrderParameter: Future[OrderParameter] = tryGetByType(schema.constants.Parameter.Type.ORDERS).map(_.deserialize).map(_.value.asOrderParameter)

    def tryGetSplitParameter: Future[SplitParameter] = tryGetByType(schema.constants.Parameter.Type.SPLITS).map(_.deserialize).map(_.value.asSplitParameter)

    def getAll: Future[Seq[Parameter]] = getAllParameters.map(_.map(_.deserialize))

  }

  object Utility {

    def onParameterChange(parameterChange: ParameterChange): Future[Unit] = {
      val update = utilitiesOperations.traverse(parameterChange.changes)(change => {
        parameterUpdate(change.subspace)
      })

      (for {
        _ <- update
      } yield ()
        ).recover {
        case _: BaseException => logger.error("FAILED_TO_UPDATE_BLOCKCHAIN_PARAMETER")
      }
    }

    private def parameterUpdate(parameterType: String) = {
      val parameter: Future[abstractParameter] = parameterType match {
        case schema.constants.Parameter.Type.AUTH =>
          val authResponse = getAuthParams.Service.get()
          for {
            authResponse <- authResponse
          } yield authResponse.params.toParameter
        case schema.constants.Parameter.Type.BANK =>
          val bankResponse = getBankParams.Service.get()
          for {
            bankResponse <- bankResponse
          } yield bankResponse.params.toParameter
        case schema.constants.Parameter.Type.DISTRIBUTION =>
          val distributionResponse = getDistributionParams.Service.get()
          for {
            distributionResponse <- distributionResponse
          } yield distributionResponse.params.toParameter
        case schema.constants.Parameter.Type.GOVERNANCE =>
          val govResponse = getGovParams.Service.get()
          for {
            govResponse <- govResponse
          } yield govResponse.toParameter
        case schema.constants.Parameter.Type.MINT =>
          val mintResponse = getMintParams.Service.get()
          for {
            mintResponse <- mintResponse
          } yield mintResponse.params.toParameter
        case schema.constants.Parameter.Type.SLASHING =>
          val slashingResponse = getSlashingParams.Service.get()
          for {
            slashingResponse <- slashingResponse
          } yield slashingResponse.params.toParameter
        case schema.constants.Parameter.Type.STAKING =>
          val stakingResponse = getStakingParams.Service.get()
          for {
            stakingResponse <- stakingResponse
          } yield stakingResponse.params.toParameter
        case schema.constants.Parameter.Type.CRISIS => Future(CrisisParameter(Coin(denom = "", amount = MicroNumber.zero)))
        case schema.constants.Parameter.Type.IBC => Future(IBCParameter(allowedClients = Seq.empty))
        case schema.constants.Parameter.Type.TRANSFER => Future(TransferParameter(receiveEnabled = true, sendEnabled = true))
        case schema.constants.Parameter.Type.ASSETS =>
          val response = getAssetParams.Service.get()
          for {
            response <- response
          } yield response.toParameter
        case schema.constants.Parameter.Type.CLASSIFICATIONS =>
          val response = getClassificationParams.Service.get()
          for {
            response <- response
          } yield response.toParameter
        case schema.constants.Parameter.Type.IDENTITIES =>
          val response = getIdentityParams.Service.get()
          for {
            response <- response
          } yield response.toParameter
        case schema.constants.Parameter.Type.MAINTAINERS =>
          val response = getMaintainerParams.Service.get()
          for {
            response <- response
          } yield response.toParameter
        case schema.constants.Parameter.Type.METAS =>
          val response = getMetaParams.Service.get()
          for {
            response <- response
          } yield response.toParameter
        case schema.constants.Parameter.Type.ORDERS =>
          val response = getOrderParams.Service.get()
          for {
            response <- response
          } yield response.toParameter
        case schema.constants.Parameter.Type.SPLITS =>
          val response = getSplitParams.Service.get()
          for {
            response <- response
          } yield response.toParameter
        case _ => constants.Response.PARAMETER_TYPE_NOT_FOUND.throwBaseException()
      }

      def upsertParameter(parameterValue: abstractParameter) = if (parameterValue.parameterType != schema.constants.Parameter.Type.CRISIS || parameterValue.parameterType != schema.constants.Parameter.Type.IBC || parameterValue.parameterType != schema.constants.Parameter.Type.TRANSFER)
        Service.insertOrUpdate(Parameter(parameterType = parameterValue.parameterType, value = parameterValue)) else Future(0)

      for {
        parameter <- parameter
        _ <- upsertParameter(parameter)
      } yield ()
    }

    def updateParameters(parameterTypes: Seq[String]): Future[Seq[Unit]] = utilitiesOperations.traverse(parameterTypes) { parameterType => parameterUpdate(parameterType)
    }
  }
}