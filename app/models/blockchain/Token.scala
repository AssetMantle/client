package models.blockchain

import exceptions.BaseException
import models.traits.Logging
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

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Token(denom: String, totalSupply: MicroNumber, bondedAmount: MicroNumber, notBondedAmount: MicroNumber, communityPool: MicroNumber, inflation: BigDecimal, totalLocked: MicroNumber, totalBurnt: MicroNumber, createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging

@Singleton
class Tokens @Inject()(
                        protected val databaseConfigProvider: DatabaseConfigProvider,
                        configuration: Configuration,
                        getTotalSupply: GetTotalSupply,
                        getStakingPool: GetStakingPool,
                        getMintingInflation: GetMintingInflation,
                        getCommunityPool: GetCommunityPool,
                      )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_TOKEN

  import databaseConfig.profile.api._

  private[models] val tokenTable = TableQuery[TokenTable]

  case class TokenSerialized(denom: String, totalSupply: BigDecimal, bondedAmount: BigDecimal, notBondedAmount: BigDecimal, communityPool: BigDecimal, inflation: BigDecimal, totalLocked: BigDecimal, totalBurnt: BigDecimal, createdBy: Option[String], createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) {
    def deserialize: Token = Token(denom = denom, totalSupply = new MicroNumber(totalSupply), bondedAmount = new MicroNumber(bondedAmount), notBondedAmount = new MicroNumber(notBondedAmount), communityPool = new MicroNumber(communityPool), inflation = inflation, totalLocked = new MicroNumber(totalLocked), totalBurnt = new MicroNumber(totalBurnt), createdBy = createdBy, createdOnMillisEpoch = createdOnMillisEpoch, updatedBy = updatedBy, updatedOnMillisEpoch = updatedOnMillisEpoch)
  }

  def serialize(token: Token): TokenSerialized = TokenSerialized(denom = token.denom, totalSupply = token.totalSupply.toBigDecimal, bondedAmount = token.bondedAmount.toBigDecimal, notBondedAmount = token.notBondedAmount.toBigDecimal, communityPool = token.communityPool.toBigDecimal, inflation = token.inflation, totalLocked = token.totalLocked.toBigDecimal, totalBurnt = token.totalBurnt.toBigDecimal, createdBy = token.createdBy, createdOnMillisEpoch = token.createdOnMillisEpoch, updatedBy = token.updatedBy, updatedOnMillisEpoch = token.updatedOnMillisEpoch)

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

  private def getTotalBondedTokenAmount: Future[BigDecimal] = db.run(tokenTable.filter(_.denom === constants.Blockchain.StakingDenom).map(_.bondedAmount).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.CRYPTO_TOKEN_NOT_FOUND, noSuchElementException)
    }
  }

  private def updateExceptBurnAndLock(denom: String, totalSupply: BigDecimal, bondedAmount: BigDecimal, notBondedAmount: BigDecimal, communityPool: BigDecimal, inflation: BigDecimal): Future[Int] = db.run(tokenTable.filter(_.denom === denom).map(x => (x.totalSupply, x.bondedAmount, x.notBondedAmount, x.communityPool, x.inflation)).update((totalSupply, bondedAmount, notBondedAmount, communityPool, inflation)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.CRYPTO_TOKEN_UPDATE_FAILED, psqlException)
    }
  }

  private def existsByDenom(denom: String): Future[Boolean] = db.run(tokenTable.filter(_.denom === denom).exists.result)

  private def updateBondingTokenByDenom(denom: String, bondedAmount: MicroNumber, notBondedAmount: MicroNumber): Future[Int] = db.run(tokenTable.filter(_.denom === denom).map(x => (x.bondedAmount, x.notBondedAmount)).update((bondedAmount.toBigDecimal, notBondedAmount.toBigDecimal)).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.CRYPTO_TOKEN_UPDATE_FAILED, noSuchElementException)
    }
  }

  private def updateTotalLockedByDenom(denom: String, totalLocked: BigDecimal): Future[Int] = db.run(tokenTable.filter(_.denom === denom).map(_.totalLocked).update(totalLocked).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.CRYPTO_TOKEN_UPDATE_FAILED, noSuchElementException)
    }
  }

  private def updateTotalBurntByDenom(denom: String, totalBurnt: BigDecimal): Future[Int] = db.run(tokenTable.filter(_.denom === denom).map(_.totalBurnt).update(totalBurnt).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.CRYPTO_TOKEN_UPDATE_FAILED, noSuchElementException)
    }
  }


  private[models] class TokenTable(tag: Tag) extends Table[TokenSerialized](tag, "Token") {

    def * = (denom, totalSupply, bondedAmount, notBondedAmount, communityPool, inflation, totalLocked, totalBurnt, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (TokenSerialized.tupled, TokenSerialized.unapply)

    def denom = column[String]("denom", O.PrimaryKey)

    def totalSupply = column[BigDecimal]("totalSupply")

    def bondedAmount = column[BigDecimal]("bondedAmount")

    def notBondedAmount = column[BigDecimal]("notBondedAmount")

    def communityPool = column[BigDecimal]("communityPool")

    def inflation = column[BigDecimal]("inflation")

    def totalLocked = column[BigDecimal]("totalLocked")

    def totalBurnt = column[BigDecimal]("totalBurnt")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")
  }

  object Service {

    def get(denom: String): Future[Token] = findByDenom(denom).map(_.deserialize)

    def getAll: Future[Seq[Token]] = getAllTokens.map(_.map(_.deserialize))

    def getAllDenoms: Future[Seq[String]] = getAllTokendenoms

    def getStakingToken: Future[Token] = findByDenom(constants.Blockchain.StakingDenom).map(_.deserialize)

    def insertOrUpdate(token: Token): Future[Int] = upsert(token)

    def insertMultiple(tokens: Seq[Token]): Future[Seq[String]] = addMultiple(tokens)

    def updateOnNewBlock(denom: String, totalSupply: MicroNumber, bondedAmount: MicroNumber, notBondedAmount: MicroNumber, communityPool: MicroNumber, inflation: BigDecimal): Future[Unit] = {
      val exists = existsByDenom(denom)

      def checkAndProcess(exists: Boolean) = if (!exists) insertOrUpdate(Token(denom = denom, totalSupply = totalSupply, bondedAmount = bondedAmount, notBondedAmount = notBondedAmount, communityPool = communityPool, inflation = inflation, totalLocked = MicroNumber.zero, totalBurnt = MicroNumber.zero))
      else updateExceptBurnAndLock(denom = denom, totalSupply = totalSupply.toBigDecimal, bondedAmount = bondedAmount.toBigDecimal, notBondedAmount = notBondedAmount.toBigDecimal, communityPool = communityPool.toBigDecimal, inflation = inflation)

      for {
        exists <- exists
        _ <- checkAndProcess(exists)
      } yield ()
    }

    def updateStakingAmounts(denom: String, bondedAmount: MicroNumber, notBondedAmount: MicroNumber): Future[Int] = updateBondingTokenByDenom(denom = denom, bondedAmount = bondedAmount, notBondedAmount = notBondedAmount)

    def addTotalLocked(denom: String, locked: MicroNumber): Future[Int] = {
      val token = get(denom)

      def update(token: Token) = updateTotalLockedByDenom(denom = denom, totalLocked = (token.totalLocked + locked).toBigDecimal)

      for {
        token <- token
        result <- update(token)
      } yield result
    }

    def subtractTotalLocked(denom: String, locked: MicroNumber): Future[Int] = {
      val token = get(denom)

      def update(token: Token) = updateTotalLockedByDenom(denom = denom, totalLocked = (token.totalLocked - locked).toBigDecimal)

      for {
        token <- token
        result <- update(token)
      } yield result
    }

    def addTotalBurnt(denom: String, burnt: MicroNumber): Future[Int] = {
      val token = get(denom)

      def update(token: Token) = updateTotalBurntByDenom(denom = denom, totalBurnt = (token.totalBurnt + burnt).toBigDecimal)

      for {
        token <- token
        result <- update(token)
      } yield result
    }

    def getTotalBondedAmount: Future[MicroNumber] = getTotalBondedTokenAmount.map(x => new MicroNumber(x))
  }

  object Utility {

    def onSlashing: Future[Unit] = {
      val stakingPoolResponse = getStakingPool.Service.get

      def updateStakingToken(stakingPoolResponse: StakingPoolResponse) = Service.updateStakingAmounts(denom = constants.Blockchain.StakingDenom, bondedAmount = stakingPoolResponse.pool.bonded_tokens, notBondedAmount = stakingPoolResponse.pool.not_bonded_tokens)

      for {
        stakingPoolResponse <- stakingPoolResponse
        _ <- updateStakingToken(stakingPoolResponse)
      } yield ()

    }

    def updateAll(): Future[Unit] = {
      val totalSupplyResponse = getTotalSupply.Service.get
      val mintingInflationResponse = getMintingInflation.Service.get
      val stakingPoolResponse = getStakingPool.Service.get
      val communityPoolResponse = getCommunityPool.Service.get

      def update(totalSupplyResponse: TotalSupplyResponse, mintingInflationResponse: MintingInflationResponse, stakingPoolResponse: StakingPoolResponse, communityPoolResponse: CommunityPoolResponse) = Future.traverse(totalSupplyResponse.supply) { token =>
        Service.updateOnNewBlock(
          denom = token.denom,
          totalSupply = token.amount,
          bondedAmount = if (token.denom == constants.Blockchain.StakingDenom) stakingPoolResponse.pool.bonded_tokens else MicroNumber.zero,
          notBondedAmount = if (token.denom == constants.Blockchain.StakingDenom) stakingPoolResponse.pool.not_bonded_tokens else MicroNumber.zero,
          communityPool = communityPoolResponse.pool.find(_.denom == token.denom).fold(MicroNumber.zero)(_.amount),
          inflation = if (token.denom == constants.Blockchain.StakingDenom) BigDecimal(mintingInflationResponse.inflation) else BigDecimal(0.0)
        )
      }

      for {
        totalSupplyResponse <- totalSupplyResponse
        mintingInflationResponse <- mintingInflationResponse
        stakingPoolResponse <- stakingPoolResponse
        communityPoolResponse <- communityPoolResponse
        _ <- update(totalSupplyResponse, mintingInflationResponse, stakingPoolResponse, communityPoolResponse)
      } yield ()
    }
  }

}