package models.blockchain

import java.sql.Timestamp

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries._
import queries.responses.CommunityPoolResponse.{Response => CommunityPoolResponse}
import queries.responses.MintingInflationResponse.{Response => MintingInflationResponse}
import queries.responses.StakingPoolResponse.{Response => StakingPoolResponse}
import queries.responses.TotalSupplyResponse.{Response => TotalSupplyResponse}
import slick.jdbc.JdbcProfile
import utilities.MicroNumber

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Token(symbol: String, totalSupply: MicroNumber, bondedAmount: MicroNumber, notBondedAmount: MicroNumber, communityPool: MicroNumber, inflation: BigDecimal, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Tokens @Inject()(
                        actorSystem: ActorSystem,
                        protected val databaseConfigProvider: DatabaseConfigProvider,
                        configuration: Configuration,
                        getTotalSupply: GetTotalSupply,
                        getStakingPool: GetStakingPool,
                        getMintingInflation: GetMintingInflation,
                        getCommunityPool: GetCommunityPool,
                      )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val stakingTokenSymbol = configuration.get[String]("blockchain.token.stakingSymbol")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_TOKEN

  import databaseConfig.profile.api._

  private[models] val tokenTable = TableQuery[TokenTable]

  case class TokenSerialized(symbol: String, totalSupply: String, bondedAmount: String, notBondedAmount: String, communityPool: String, inflation: BigDecimal, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Token = Token(symbol = symbol, totalSupply = new MicroNumber(totalSupply), bondedAmount = new MicroNumber(bondedAmount), notBondedAmount = new MicroNumber(notBondedAmount), communityPool = new MicroNumber(communityPool), inflation = inflation, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(token: Token): TokenSerialized = TokenSerialized(symbol = token.symbol, totalSupply = token.totalSupply.toString, bondedAmount = token.bondedAmount.toString, notBondedAmount = token.notBondedAmount.toString, communityPool = token.communityPool.toString, inflation = token.inflation, createdBy = token.createdBy, createdOn = token.createdOn, createdOnTimeZone = token.createdOnTimeZone, updatedBy = token.updatedBy, updatedOn = token.updatedOn, updatedOnTimeZone = token.updatedOnTimeZone)

  private def add(token: Token): Future[String] = db.run((tokenTable returning tokenTable.map(_.symbol) += serialize(token)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.CRYPTO_TOKEN_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(tokens: Seq[Token]): Future[Seq[String]] = db.run((tokenTable returning tokenTable.map(_.symbol) ++= tokens.map(x => serialize(x))).asTry).map {
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

  private def findBySymbol(symbol: String): Future[TokenSerialized] = db.run(tokenTable.filter(_.symbol === symbol).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.CRYPTO_TOKEN_NOT_FOUND, noSuchElementException)
    }
  }

  private def getAllTokens: Future[Seq[TokenSerialized]] = db.run(tokenTable.result)

  private def getAllTokenSymbols: Future[Seq[String]] = db.run(tokenTable.map(_.symbol).result)

  private def getTotalBondedTokenAmount: Future[String] = db.run(tokenTable.filter(_.symbol === stakingTokenSymbol).map(_.bondedAmount).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.CRYPTO_TOKEN_NOT_FOUND, noSuchElementException)
    }
  }

  private def updateBySymbol(token: Token): Future[Int] = db.run(tokenTable.filter(_.symbol === token.symbol).update(serialize(token)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.CRYPTO_TOKEN_UPDATE_FAILED, psqlException)
    }
  }

  private def updateBondingTokenBySymbol(symbol: String, bondedAmount: MicroNumber, notBondedAmount: MicroNumber): Future[Int] = db.run(tokenTable.filter(_.symbol === symbol).map(x => (x.bondedAmount, x.notBondedAmount)).update((bondedAmount.toString, notBondedAmount.toString)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.CRYPTO_TOKEN_UPDATE_FAILED, noSuchElementException)
    }
  }

  private def updateTotalSupplyAndInflationBySymbol(symbol: String, totalSupply: MicroNumber, inflation: BigDecimal): Future[Int] = db.run(tokenTable.filter(_.symbol === symbol).map(x => (x.totalSupply, x.inflation)).update((totalSupply.toString, inflation)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.CRYPTO_TOKEN_UPDATE_FAILED, noSuchElementException)
    }
  }

  private[models] class TokenTable(tag: Tag) extends Table[TokenSerialized](tag, "Token") {

    def * = (symbol, totalSupply, bondedAmount, notBondedAmount, communityPool, inflation, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (TokenSerialized.tupled, TokenSerialized.unapply)

    def symbol = column[String]("symbol", O.PrimaryKey)

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

    def create(token: Token): Future[String] = add(token)

    def get(symbol: String): Future[Token] = findBySymbol(symbol).map(_.deserialize)

    def getAll: Future[Seq[Token]] = getAllTokens.map(_.map(_.deserialize))

    def getAllSymbols: Future[Seq[String]] = getAllTokenSymbols

    def insertMultiple(tokens: Seq[Token]): Future[Seq[String]] = addMultiple(tokens)

    def insertOrUpdate(token: Token): Future[Int] = upsert(token)

    def updateStakingAmounts(symbol: String, bondedAmount: MicroNumber, notBondedAmount: MicroNumber): Future[Int] = updateBondingTokenBySymbol(symbol = symbol, bondedAmount = bondedAmount, notBondedAmount = notBondedAmount)

    def updateTotalSupplyAndInflation(symbol: String, totalSupply: MicroNumber, inflation: BigDecimal): Future[Int] = updateTotalSupplyAndInflationBySymbol(symbol = symbol, totalSupply = totalSupply, inflation = inflation)

    def getTotalBondedAmount: Future[MicroNumber] = getTotalBondedTokenAmount.map(x => new MicroNumber(x))
  }

  object Utility {

    def onSlashing: Future[Unit] = {
      val stakingPoolResponse = getStakingPool.Service.get

      def updateStakingToken(stakingPoolResponse: StakingPoolResponse) = Service.updateStakingAmounts(symbol = stakingTokenSymbol, bondedAmount = stakingPoolResponse.result.bonded_tokens, notBondedAmount = stakingPoolResponse.result.not_bonded_tokens)

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

      def update(totalSupplyResponse: TotalSupplyResponse, mintingInflationResponse: MintingInflationResponse, stakingPoolResponse: StakingPoolResponse, communityPoolResponse: CommunityPoolResponse) = Future.traverse(totalSupplyResponse.result) { token =>
        Service.insertOrUpdate(Token(symbol = token.denom, totalSupply = token.amount,
          bondedAmount = if (token.denom == stakingTokenSymbol) stakingPoolResponse.result.bonded_tokens else MicroNumber.zero,
          notBondedAmount = if (token.denom == stakingTokenSymbol) stakingPoolResponse.result.not_bonded_tokens else MicroNumber.zero,
          communityPool = communityPoolResponse.result.find(_.denom == token.denom).fold(MicroNumber.zero)(_.amount),
          inflation = if (token.denom == stakingTokenSymbol) mintingInflationResponse.result else BigDecimal(0.0)
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