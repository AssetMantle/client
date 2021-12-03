package models.blockchain

import akka.pattern.{ask, pipe}
import akka.util.Timeout
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import constants.Actor.{NUMBER_OF_ENTITIES, NUMBER_OF_SHARDS}

import java.sql.Timestamp
import exceptions.BaseException

import javax.inject.{Inject, Singleton}
import models.Abstract.{ShardedActorRegion, Parameter => abstractParameter}
import models.Trait.Logged
import models.blockchain.Parameter.{CreateParameter, GetAllParameter, InsertOrUpdateParameter, TryGetAuthParameter, TryGetBankParameter, TryGetDistributionParameter, TryGetGovernanceParameter, TryGetHalvingParameter, TryGetMintingParameter, TryGetParameter, TryGetSlashingParameter, TryGetStakingParameter}
import models.common.Parameters._
import models.common.ProposalContents.ParameterChange
import models.common.Serializable.Coin
import models.masterTransaction
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.blockchain.params._
import queries.responses.common.Header
import slick.jdbc.JdbcProfile
import utilities.MicroNumber

import java.util.UUID
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
                          )(implicit executionContext: ExecutionContext) extends ShardedActorRegion {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_PARAMETER

  import databaseConfig.profile.api._

  private[models] val parameterTable = TableQuery[ParameterTable]

  private val uniqueId: String = UUID.randomUUID().toString

  private implicit val timeout = Timeout(constants.Actor.ACTOR_ASK_TIMEOUT)

  override def idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateParameter(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateParameter(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetParameter(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetAuthParameter(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetBankParameter(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetDistributionParameter(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetGovernanceParameter(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetHalvingParameter(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetMintingParameter(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetSlashingParameter(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetStakingParameter(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllParameter(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  override def shardResolver: ShardRegion.ExtractShardId = {
    case CreateParameter(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateParameter(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetParameter(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetAuthParameter(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetBankParameter(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetDistributionParameter(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetGovernanceParameter(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetHalvingParameter(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetMintingParameter(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetSlashingParameter(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetStakingParameter(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllParameter(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }
  override def regionName: String = "parameterRegion"

  override def props: Props = Parameter.props(Parameters.this)

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

    def createParameterWithActor(parameter: Parameter): Future[String] = (actorRegion ? CreateParameter(uniqueId, parameter)).mapTo[String]

    def create(parameter: Parameter): Future[String] = add(parameter)

    def insertOrUpdateParameterWithActor(parameter: Parameter): Future[Int] = (actorRegion ? InsertOrUpdateParameter(uniqueId, parameter)).mapTo[Int]

    def insertOrUpdate(parameter: Parameter): Future[Int] = upsert(parameter)

    def tryGetParameterWithActor(parameterType: String): Future[Parameter] = (actorRegion ? TryGetParameter(uniqueId, parameterType)).mapTo[Parameter]

    def tryGet(parameterType: String): Future[Parameter] = tryGetByType(parameterType).map(_.deserialize)

    def tryGetAuthParameterWithActor: Future[AuthParameter] = (actorRegion ? TryGetAuthParameter(uniqueId)).mapTo[AuthParameter]

    def tryGetAuthParameter: Future[AuthParameter] = tryGetByType(constants.Blockchain.ParameterType.AUTH).map(_.deserialize).map(_.value.asAuthParameter)

    def tryGetBankParameterWithActor: Future[BankParameter] = (actorRegion ? TryGetBankParameter(uniqueId)).mapTo[BankParameter]

    def tryGetBankParameter: Future[BankParameter] = tryGetByType(constants.Blockchain.ParameterType.BANK).map(_.deserialize).map(_.value.asBankParameter)

    def tryGetDistributionParameterWithActor: Future[DistributionParameter] = (actorRegion ? TryGetDistributionParameter(uniqueId)).mapTo[DistributionParameter]

    def tryGetDistributionParameter: Future[DistributionParameter] = tryGetByType(constants.Blockchain.ParameterType.DISTRIBUTION).map(_.deserialize).map(_.value.asDistributionParameter)

    def tryGetGovernanceParameterWithActor: Future[GovernanceParameter] = (actorRegion ? TryGetGovernanceParameter(uniqueId)).mapTo[GovernanceParameter]

    def tryGetGovernanceParameter: Future[GovernanceParameter] = tryGetByType(constants.Blockchain.ParameterType.GOVERNANCE).map(_.deserialize).map(_.value.asGovernanceParameter)

    def tryGetHalvingParameterWithActor: Future[HalvingParameter] = (actorRegion ? TryGetHalvingParameter(uniqueId)).mapTo[HalvingParameter]

    def tryGetHalvingParameter: Future[HalvingParameter] = tryGetByType(constants.Blockchain.ParameterType.HALVING).map(_.deserialize).map(_.value.asHalvingParameter)

    def tryGetMintingParameterWithActor: Future[MintingParameter] = (actorRegion ? TryGetMintingParameter(uniqueId)).mapTo[MintingParameter]

    def tryGetMintingParameter: Future[MintingParameter] = tryGetByType(constants.Blockchain.ParameterType.MINT).map(_.deserialize).map(_.value.asMintingParameter)

    def tryGetSlashingParameterWithActor: Future[SlashingParameter] = (actorRegion ? TryGetSlashingParameter(uniqueId)).mapTo[SlashingParameter]

    def tryGetSlashingParameter: Future[SlashingParameter] = tryGetByType(constants.Blockchain.ParameterType.SLASHING).map(_.deserialize).map(_.value.asSlashingParameter)

    def tryGetStakingParameterWithActor: Future[StakingParameter] = (actorRegion ? TryGetStakingParameter(uniqueId)).mapTo[StakingParameter]

    def tryGetStakingParameter: Future[StakingParameter] = tryGetByType(constants.Blockchain.ParameterType.STAKING).map(_.deserialize).map(_.value.asStakingParameter)

    def getAllParameterWithActor: Future[Seq[Parameter]] = (actorRegion ? GetAllParameter).mapTo[Seq[Parameter]]

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

object Parameter {
  def props(blockchainParameters: models.blockchain.Parameters) (implicit executionContext: ExecutionContext) = Props(new ParameterActor(blockchainParameters))

  @Singleton
  class ParameterActor @Inject()(
                                  blockchainParameters: models.blockchain.Parameters
                                ) (implicit executionContext: ExecutionContext) extends Actor with ActorLogging {
    private implicit val logger: Logger = Logger(this.getClass)

    override def receive: Receive = {
      case CreateParameter(_, parameter) => {
        blockchainParameters.Service.create(parameter) pipeTo sender()
      }
      case TryGetParameter(_, parameterType) => {
        blockchainParameters.Service.tryGet(parameterType) pipeTo sender()
      }
      case InsertOrUpdateParameter(_, parameter) => {
        blockchainParameters.Service.insertOrUpdate(parameter) pipeTo sender()
      }
      case TryGetAuthParameter(_) => {
        blockchainParameters.Service.tryGetAuthParameter pipeTo sender()
      }
      case TryGetBankParameter(_) => {
        blockchainParameters.Service.tryGetBankParameter pipeTo sender()
      }
      case TryGetDistributionParameter(_) => {
        blockchainParameters.Service.tryGetDistributionParameter pipeTo sender()
      }
      case TryGetGovernanceParameter(_) => {
        blockchainParameters.Service.tryGetGovernanceParameter pipeTo sender()
      }
      case TryGetHalvingParameter(_) => {
        blockchainParameters.Service.tryGetHalvingParameter pipeTo sender()
      }
      case TryGetMintingParameter(_) => {
        blockchainParameters.Service.tryGetMintingParameter pipeTo sender()
      }
      case TryGetSlashingParameter(_) => {
        blockchainParameters.Service.tryGetSlashingParameter pipeTo sender()
      }
      case TryGetStakingParameter(_) => {
        blockchainParameters.Service.tryGetStakingParameter pipeTo sender()
      }
      case GetAllParameter(_) => {
        blockchainParameters.Service.getAll pipeTo sender()
      }
    }
  }

  case class CreateParameter(id: String, parameter: Parameter)
  case class InsertOrUpdateParameter(id: String, parameter: Parameter)
  case class TryGetParameter(id: String, parameterType: String)
  case class TryGetAuthParameter(id: String)
  case class TryGetBankParameter(id: String)
  case class TryGetDistributionParameter(id: String)
  case class TryGetGovernanceParameter(id: String)
  case class TryGetHalvingParameter(id: String)
  case class TryGetMintingParameter(id: String)
  case class TryGetSlashingParameter(id: String)
  case class TryGetStakingParameter(id: String)
  case class GetAllParameter(id: String)

}