package models.blockchain

import akka.pattern.ask
import akka.util.Timeout
import actors.models.{CreateParameter, GetAllParameter, InsertOrUpdateParameter, OrderActor, ParameterActor, TryGetAuthParameter, TryGetBankParameter, TryGetDistributionParameter, TryGetGovernanceParameter, TryGetHalvingParameter, TryGetMintingParameter, TryGetParameter, TryGetSlashingParameter, TryGetStakingParameter}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}

import java.sql.Timestamp
import exceptions.BaseException

import javax.inject.{Inject, Singleton}
import models.Abstract.{Parameter => abstractParameter}
import models.Trait.Logged
import models.common.Parameters._
import models.common.ProposalContents.ParameterChange
import models.common.Serializable.Coin
import models.common.TransactionMessages._
import models.masterTransaction
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.blockchain.params._
import queries.responses.blockchain.params._
import queries.responses.common.Header
import slick.jdbc.JdbcProfile
import utilities.MicroNumber

import java.util.UUID
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Parameter(parameterType: String, value: abstractParameter, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Parameters @Inject()(
                            protected val databaseConfigProvider: DatabaseConfigProvider,
                            configuration: Configuration,
                            masterTransactionNotifications: masterTransaction.Notifications,
                            utilitiesOperations: utilities.Operations,
                            getAuthParams: GetAuth,
                            getBankParams: GetBank,
                            getGovParams: GetGov,
                            getDistributionParams: GetDistribution,
                            getHalvingParams: GetHalving,
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

  private val uniqueId: String = UUID.randomUUID().toString

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

    implicit val timeout = Timeout(10 seconds) // needed for `?` below

    private val parameterActorRegion = {
      ClusterSharding(actors.models.Service.actorSystem).start(
        typeName = "parameterRegion",
        entityProps = ParameterActor.props(Parameters.this),
        settings = ClusterShardingSettings(actors.models.Service.actorSystem),
        extractEntityId = ParameterActor.idExtractor,
        extractShardId = ParameterActor.shardResolver
      )
    }

    def createParameterWithActor(parameter: Parameter): Future[String] = (parameterActorRegion ? CreateParameter(uniqueId, parameter)).mapTo[String]

    def create(parameter: Parameter): Future[String] = add(parameter)

    def insertOrUpdateParameterWithActor(parameter: Parameter): Future[Int] = (parameterActorRegion ? InsertOrUpdateParameter(uniqueId, parameter)).mapTo[Int]

    def insertOrUpdate(parameter: Parameter): Future[Int] = upsert(parameter)

    def tryGetParameterWithActor(parameterType: String): Future[Parameter] = (parameterActorRegion ? TryGetParameter(uniqueId, parameterType)).mapTo[Parameter]

    def tryGet(parameterType: String): Future[Parameter] = tryGetByType(parameterType).map(_.deserialize)

    def tryGetAuthParameterWithActor: Future[AuthParameter] = (parameterActorRegion ? TryGetAuthParameter(uniqueId)).mapTo[AuthParameter]

    def tryGetAuthParameter: Future[AuthParameter] = tryGetByType(constants.Blockchain.ParameterType.AUTH).map(_.deserialize).map(_.value.asAuthParameter)

    def tryGetBankParameterWithActor: Future[BankParameter] = (parameterActorRegion ? TryGetBankParameter(uniqueId)).mapTo[BankParameter]

    def tryGetBankParameter: Future[BankParameter] = tryGetByType(constants.Blockchain.ParameterType.BANK).map(_.deserialize).map(_.value.asBankParameter)

    def tryGetDistributionParameterWithActor: Future[DistributionParameter] = (parameterActorRegion ? TryGetDistributionParameter(uniqueId)).mapTo[DistributionParameter]

    def tryGetDistributionParameter: Future[DistributionParameter] = tryGetByType(constants.Blockchain.ParameterType.DISTRIBUTION).map(_.deserialize).map(_.value.asDistributionParameter)

    def tryGetGovernanceParameterWithActor: Future[GovernanceParameter] = (parameterActorRegion ? TryGetGovernanceParameter(uniqueId)).mapTo[GovernanceParameter]

    def tryGetGovernanceParameter: Future[GovernanceParameter] = tryGetByType(constants.Blockchain.ParameterType.GOVERNANCE).map(_.deserialize).map(_.value.asGovernanceParameter)

    def tryGetHalvingParameterWithActor: Future[HalvingParameter] = (parameterActorRegion ? TryGetHalvingParameter(uniqueId)).mapTo[HalvingParameter]

    def tryGetHalvingParameter: Future[HalvingParameter] = tryGetByType(constants.Blockchain.ParameterType.HALVING).map(_.deserialize).map(_.value.asHalvingParameter)

    def tryGetMintingParameterWithActor: Future[MintingParameter] = (parameterActorRegion ? TryGetMintingParameter(uniqueId)).mapTo[MintingParameter]

    def tryGetMintingParameter: Future[MintingParameter] = tryGetByType(constants.Blockchain.ParameterType.MINT).map(_.deserialize).map(_.value.asMintingParameter)

    def tryGetSlashingParameterWithActor: Future[SlashingParameter] = (parameterActorRegion ? TryGetSlashingParameter(uniqueId)).mapTo[SlashingParameter]

    def tryGetSlashingParameter: Future[SlashingParameter] = tryGetByType(constants.Blockchain.ParameterType.SLASHING).map(_.deserialize).map(_.value.asSlashingParameter)

    def tryGetStakingParameterWithActor: Future[StakingParameter] = (parameterActorRegion ? TryGetStakingParameter(uniqueId)).mapTo[StakingParameter]

    def tryGetStakingParameter: Future[StakingParameter] = tryGetByType(constants.Blockchain.ParameterType.STAKING).map(_.deserialize).map(_.value.asStakingParameter)

    def getAllParameterWithActor: Future[Seq[Parameter]] = (parameterActorRegion ? GetAllParameter).mapTo[Seq[Parameter]]

    def getAll: Future[Seq[Parameter]] = getAllParameters.map(_.map(_.deserialize))

  }

  object Utility {

    def onNewBlock(header: Header): Future[Unit] = {
      val halvingParameter = Service.tryGetHalvingParameter

      def checkAndUpdate(halvingParameter: HalvingParameter) = if ((header.height % halvingParameter.blockHeight) == 0) {
        val mintingParameter = Service.tryGetMintingParameterWithActor

        def updateMintingParameter(mintingParameter: MintingParameter) = Service.insertOrUpdate(Parameter(parameterType = mintingParameter.`type`, value = mintingParameter.copy(inflationMax = mintingParameter.inflationMax / 2, inflationMin = mintingParameter.inflationMin / 2, inflationRateChange = (mintingParameter.inflationMax / 2) - (mintingParameter.inflationMin / 2))))

        for {
          mintingParameter <- mintingParameter
          _ <- updateMintingParameter(mintingParameter)
        } yield ()
      } else Future()

      (for {
        halvingParameter <- halvingParameter
        _ <- checkAndUpdate(halvingParameter)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }

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
          case constants.Blockchain.ParameterType.HALVING =>
            val halvingResponse = getHalvingParams.Service.get()
            for {
              halvingResponse <- halvingResponse
            } yield halvingResponse.params.toParameter
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

        def upsertParameter(parameterValue: abstractParameter) = if (parameterValue.`type` != constants.Blockchain.ParameterType.CRISIS || parameterValue.`type` != constants.Blockchain.ParameterType.IBC || parameterValue.`type` != constants.Blockchain.ParameterType.TRANSFER)
          Service.insertOrUpdate(Parameter(parameterType = parameterValue.`type`, value = parameterValue)) else Future(0)

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