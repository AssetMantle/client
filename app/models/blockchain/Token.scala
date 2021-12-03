package models.blockchain

import java.sql.Timestamp
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import exceptions.BaseException

import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.blockchain.Tokens.{CreateToken, GetAllDenoms, GetAllToken, GetStakingToken, GetToken, GetTotalBondedAmount, InsertMultipleToken, InsertOrUpdateToken, TokenActor, UpdateStakingAmounts, UpdateTotalSupplyAndInflation}
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import constants.Actor.{NUMBER_OF_ENTITIES, NUMBER_OF_SHARDS}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.blockchain.{GetCommunityPool, GetMintingInflation, GetStakingPool, GetTotalSupply}
import queries.responses.blockchain.CommunityPoolResponse.{Response => CommunityPoolResponse}
import queries.responses.blockchain.MintingInflationResponse.{Response => MintingInflationResponse}
import queries.responses.blockchain.StakingPoolResponse.{Response => StakingPoolResponse}
import queries.responses.blockchain.TotalSupplyResponse.{Response => TotalSupplyResponse}
import slick.jdbc.JdbcProfile
import utilities.MicroNumber
import models.Abstract.ShardedActorRegion

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Token(denom: String, totalSupply: MicroNumber, bondedAmount: MicroNumber, notBondedAmount: MicroNumber, communityPool: MicroNumber, inflation: BigDecimal, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Tokens @Inject()(
                        protected val databaseConfigProvider: DatabaseConfigProvider,
                        configuration: Configuration,
                        getTotalSupply: GetTotalSupply,
                        getStakingPool: GetStakingPool,
                        getMintingInflation: GetMintingInflation,
                        getCommunityPool: GetCommunityPool,
                      )(implicit executionContext: ExecutionContext) extends ShardedActorRegion {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val stakingDenom = configuration.get[String]("blockchain.stakingDenom")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_TOKEN

  private val uniqueId: String = UUID.randomUUID().toString

  import databaseConfig.profile.api._

  private[models] val tokenTable = TableQuery[TokenTable]

  private implicit val timeout = Timeout(constants.Actor.ACTOR_ASK_TIMEOUT)

  override def idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateToken(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetToken(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllToken(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllDenoms(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetStakingToken(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertMultipleToken(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateToken(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@UpdateStakingAmounts(id, _, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@UpdateTotalSupplyAndInflation(id, _, _, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetTotalBondedAmount(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  override def shardResolver: ShardRegion.ExtractShardId = {
    case CreateToken(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetToken(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllToken(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllDenoms(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetStakingToken(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertMultipleToken(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateToken(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case UpdateStakingAmounts(id, _, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case UpdateTotalSupplyAndInflation(id, _, _, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetTotalBondedAmount(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }

  override def regionName: String = "tokenRegion"

  override def props: Props = Tokens.props(Tokens.this)

  case class TokenSerialized(denom: String, totalSupply: String, bondedAmount: String, notBondedAmount: String, communityPool: String, inflation: BigDecimal, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Token = Token(denom = denom, totalSupply = new MicroNumber(totalSupply), bondedAmount = new MicroNumber(bondedAmount), notBondedAmount = new MicroNumber(notBondedAmount), communityPool = new MicroNumber(communityPool), inflation = inflation, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(token: Token): TokenSerialized = TokenSerialized(denom = token.denom, totalSupply = token.totalSupply.toString, bondedAmount = token.bondedAmount.toString, notBondedAmount = token.notBondedAmount.toString, communityPool = token.communityPool.toString, inflation = token.inflation, createdBy = token.createdBy, createdOn = token.createdOn, createdOnTimeZone = token.createdOnTimeZone, updatedBy = token.updatedBy, updatedOn = token.updatedOn, updatedOnTimeZone = token.updatedOnTimeZone)

  private def add(token: Token): Future[String] = db.run((tokenTable returning tokenTable.map(_.denom) += serialize(token)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.CRYPTO_TOKEN_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(tokens: Seq[Token]): Future[Seq[String]] = db.run((tokenTable returning tokenTable.map(_.denom) ++= tokens.map(x => serialize(x))).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.CRYPTO_TOKEN_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(token: Token): Future[Int] = db.run(tokenTable.insertOrUpdate(serialize(token)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.CRYPTO_TOKEN_UPSERT_FAILED, psqlException)
    }
  }

  private def findByDenom(denom: String): Future[TokenSerialized] = db.run(tokenTable.filter(_.denom === denom).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.CRYPTO_TOKEN_NOT_FOUND, noSuchElementException)
    }
  }

  private def getAllTokens: Future[Seq[TokenSerialized]] = db.run(tokenTable.result)

  private def getAllTokendenoms: Future[Seq[String]] = db.run(tokenTable.map(_.denom).result)

  private def getTotalBondedTokenAmount: Future[String] = db.run(tokenTable.filter(_.denom === stakingDenom).map(_.bondedAmount).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.CRYPTO_TOKEN_NOT_FOUND, noSuchElementException)
    }
  }

  private def updateBydenom(token: Token): Future[Int] = db.run(tokenTable.filter(_.denom === token.denom).update(serialize(token)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.CRYPTO_TOKEN_UPDATE_FAILED, psqlException)
    }
  }

  private def updateBondingTokenByDenom(denom: String, bondedAmount: MicroNumber, notBondedAmount: MicroNumber): Future[Int] = db.run(tokenTable.filter(_.denom === denom).map(x => (x.bondedAmount, x.notBondedAmount)).update((bondedAmount.toString, notBondedAmount.toString)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.CRYPTO_TOKEN_UPDATE_FAILED, noSuchElementException)
    }
  }

  private def updateTotalSupplyAndInflationByDenom(denom: String, totalSupply: MicroNumber, inflation: BigDecimal): Future[Int] = db.run(tokenTable.filter(_.denom === denom).map(x => (x.totalSupply, x.inflation)).update((totalSupply.toString, inflation)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.CRYPTO_TOKEN_UPDATE_FAILED, noSuchElementException)
    }
  }

  private[models] class TokenTable(tag: Tag) extends Table[TokenSerialized](tag, "Token") {

    def * = (denom, totalSupply, bondedAmount, notBondedAmount, communityPool, inflation, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (TokenSerialized.tupled, TokenSerialized.unapply)

    def denom = column[String]("denom", O.PrimaryKey)

    def totalSupply = column[String]("totalSupply")

    def bondedAmount = column[String]("bondedAmount")

    def notBondedAmount = column[String]("notBondedAmount")

    def communityPool = column[String]("communityPool")

    def inflation = column[BigDecimal]("inflation")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def createTokenWithActor(token: Token): Future[String] = (actorRegion ? CreateToken(uniqueId, token)).mapTo[String]

    def create(token: Token): Future[String] = add(token)

    def getTokenWithActor(denom: String): Future[Token] = (actorRegion ? GetToken(uniqueId, denom)).mapTo[Token]

    def get(denom: String): Future[Token] = findByDenom(denom).map(_.deserialize)

    def getAllTokenWithActor: Future[Seq[Token]] = (actorRegion ? GetAllToken(uniqueId)).mapTo[Seq[Token]]

    def getAll: Future[Seq[Token]] = getAllTokens.map(_.map(_.deserialize))

    def getAllDenomsWithActor: Future[Seq[String]] = (actorRegion ? GetAllDenoms(uniqueId)).mapTo[Seq[String]]

    def getAllDenoms: Future[Seq[String]] = getAllTokendenoms

    def getStakingTokenWithActor: Future[Token] = (actorRegion ? GetStakingToken(uniqueId)).mapTo[Token]

    def getStakingToken: Future[Token] = findByDenom(stakingDenom).map(_.deserialize)

    def insertMultipleTokenWithActor(tokens: Seq[Token]): Future[Seq[String]] = (actorRegion ? InsertMultipleToken(uniqueId, tokens)).mapTo[Seq[String]]

    def insertMultiple(tokens: Seq[Token]): Future[Seq[String]] = addMultiple(tokens)

    def insertOrUpdateTokenWithActor(token: Token): Future[Int] = (actorRegion ? InsertOrUpdateToken(uniqueId, token)).mapTo[Int]

    def insertOrUpdate(token: Token): Future[Int] = upsert(token)

    def updateStakingAmountsWithActor(denom: String, bondedAmount: MicroNumber, notBondedAmount: MicroNumber): Future[Int] = (actorRegion ? UpdateStakingAmounts(uniqueId, denom, bondedAmount, notBondedAmount)).mapTo[Int]

    def updateStakingAmounts(denom: String, bondedAmount: MicroNumber, notBondedAmount: MicroNumber): Future[Int] = updateBondingTokenByDenom(denom = denom, bondedAmount = bondedAmount, notBondedAmount = notBondedAmount)

    def updateTotalSupplyAndInflationWithActor(denom: String, totalSupply: MicroNumber, inflation: BigDecimal): Future[Int] = (actorRegion ? UpdateTotalSupplyAndInflation(uniqueId, denom, totalSupply, inflation)).mapTo[Int]

    def updateTotalSupplyAndInflation(denom: String, totalSupply: MicroNumber, inflation: BigDecimal): Future[Int] = updateTotalSupplyAndInflationByDenom(denom = denom, totalSupply = totalSupply, inflation = inflation)

    def getTotalBondedAmountWithActor: Future[MicroNumber] = (actorRegion ? GetTotalBondedAmount(uniqueId)).mapTo[MicroNumber]

    def getTotalBondedAmount: Future[MicroNumber] = getTotalBondedTokenAmount.map(x => new MicroNumber(x))
  }

  object Utility {

    def onSlashing: Future[Unit] = {
      val stakingPoolResponse = getStakingPool.Service.get

      def updateStakingToken(stakingPoolResponse: StakingPoolResponse) = Service.updateStakingAmounts(denom = stakingDenom, bondedAmount = stakingPoolResponse.pool.bonded_tokens, notBondedAmount = stakingPoolResponse.pool.not_bonded_tokens)

      (for {
        stakingPoolResponse <- stakingPoolResponse
        _ <- updateStakingToken(stakingPoolResponse)
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def updateAll(): Future[Unit] = {
      val totalSupplyResponse = getTotalSupply.Service.get
      val mintingInflationResponse = getMintingInflation.Service.get
      val stakingPoolResponse = getStakingPool.Service.get
      val communityPoolResponse = getCommunityPool.Service.get

      def update(totalSupplyResponse: TotalSupplyResponse, mintingInflationResponse: MintingInflationResponse, stakingPoolResponse: StakingPoolResponse, communityPoolResponse: CommunityPoolResponse) = Future.traverse(totalSupplyResponse.supply) { token =>
        Service.insertOrUpdate(Token(denom = token.denom, totalSupply = token.amount,
          bondedAmount = if (token.denom == stakingDenom) stakingPoolResponse.pool.bonded_tokens else MicroNumber.zero,
          notBondedAmount = if (token.denom == stakingDenom) stakingPoolResponse.pool.not_bonded_tokens else MicroNumber.zero,
          communityPool = communityPoolResponse.pool.find(_.denom == token.denom).fold(MicroNumber.zero)(_.amount),
          inflation = if (token.denom == stakingDenom) BigDecimal(mintingInflationResponse.inflation) else BigDecimal(0.0)
        ))
      }

      (for {
        totalSupplyResponse <- totalSupplyResponse
        mintingInflationResponse <- mintingInflationResponse
        stakingPoolResponse <- stakingPoolResponse
        communityPoolResponse <- communityPoolResponse
        _ <- update(totalSupplyResponse, mintingInflationResponse, stakingPoolResponse, communityPoolResponse)
      } yield ()
        ).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw baseException
      }
    }
  }

}

object Tokens {
  def props(blockchainTokens: models.blockchain.Tokens) (implicit executionContext: ExecutionContext) = Props(new TokenActor(blockchainTokens))

  @Singleton
  class TokenActor @Inject()(
                              blockchainTokens: models.blockchain.Tokens
                            ) (implicit executionContext: ExecutionContext) extends Actor with ActorLogging {
    private implicit val logger: Logger = Logger(this.getClass)

    override def receive: Receive = {
      case CreateToken(_, token) => {
        blockchainTokens.Service.create(token) pipeTo sender()
      }
      case GetToken(_, denom) => {
        blockchainTokens.Service.get(denom) pipeTo sender()
      }
      case GetAllToken(_) => {
        blockchainTokens.Service.getAll pipeTo sender()
      }
      case GetAllDenoms(_) => {
        blockchainTokens.Service.getAllDenoms pipeTo sender()
      }
      case GetStakingToken(_) => {
        blockchainTokens.Service.getStakingToken pipeTo sender()
      }
      case InsertMultipleToken(_, tokens) => {
        blockchainTokens.Service.insertMultiple(tokens) pipeTo sender()
      }
      case InsertOrUpdateToken(_, token) => {
        blockchainTokens.Service.insertOrUpdate(token) pipeTo sender()
      }
      case UpdateStakingAmounts(_, denom, bondedAmount, notBondedAmount) => {
        blockchainTokens.Service.updateStakingAmounts(denom, bondedAmount, notBondedAmount) pipeTo sender()
      }
      case UpdateTotalSupplyAndInflation(_, denom, totalSupply, inflation) => {
        blockchainTokens.Service.updateTotalSupplyAndInflation(denom, totalSupply, inflation) pipeTo sender()
      }
      case GetTotalBondedAmount(_) => {
        blockchainTokens.Service.getTotalBondedAmount pipeTo sender()
      }
    }
  }

  case class CreateToken(id: String, Token: Token)
  case class GetToken(id: String, denom: String)
  case class GetAllToken(id: String)
  case class GetAllDenoms(id: String)
  case class GetStakingToken(id: String)
  case class InsertMultipleToken(id: String, Tokens: Seq[Token])
  case class InsertOrUpdateToken(id: String, Token: Token)
  case class UpdateStakingAmounts(id: String, denom: String, bondedAmount: MicroNumber, notBondedAmount: MicroNumber)
  case class UpdateTotalSupplyAndInflation(id: String, denom: String, totalSupply: MicroNumber, inflation: BigDecimal)
  case class GetTotalBondedAmount(id: String)
}