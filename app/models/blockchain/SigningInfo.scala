package models.blockchain

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries._
import queries.responses.AllSigningInfosResponse.{Response => AllSigningInfosResponse}
import queries.responses.SigningInfoResponse.{Response => SigningInfoResponse}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SigningInfo(consensusAddress: String, startHeight: Int, indexOffset: Int, jailedUntil: String, tombstoned: Boolean = false, missedBlocksCounter: Int = 0, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {
  val hexAddress: String = utilities.Bech32.convertConsensusAddressToHexAddress(consensusAddress)
}

@Singleton
class SigningInfos @Inject()(
                              protected val databaseConfigProvider: DatabaseConfigProvider,
                              configuration: Configuration,
                              getSigningInfo: GetSigningInfo,
                              getAllSigningInfos: GetAllSigningInfos,
                            )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_SIGNING_INFO

  import databaseConfig.profile.api._

  private[models] val signingInfoTable = TableQuery[SigningInfoTable]

  private def add(signingInfo: SigningInfo): Future[String] = db.run((signingInfoTable returning signingInfoTable.map(_.consensusAddress) += signingInfo).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.SIGNING_INFO_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(signingInfos: Seq[SigningInfo]): Future[Seq[String]] = db.run((signingInfoTable returning signingInfoTable.map(_.consensusAddress) ++= signingInfos).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.SIGNING_INFO_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(signingInfo: SigningInfo): Future[Int] = db.run(signingInfoTable.insertOrUpdate(signingInfo).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.SIGNING_INFO_UPSERT_FAILED, psqlException)
    }
  }

  private def findAll: Future[Seq[SigningInfo]] = db.run(signingInfoTable.result)

  private def findByConsensusAddress(consensusAddress: String): Future[SigningInfo] = db.run(signingInfoTable.filter(_.consensusAddress === consensusAddress).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.SIGNING_INFO_NOT_FOUND, noSuchElementException)
    }
  }

  private[models] class SigningInfoTable(tag: Tag) extends Table[SigningInfo](tag, "SigningInfo") {

    def * = (consensusAddress, startHeight, indexOffset, jailedUntil, tombstoned, missedBlocksCounter, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (SigningInfo.tupled, SigningInfo.unapply)

    def consensusAddress = column[String]("consensusAddress", O.PrimaryKey)

    def startHeight = column[Int]("startHeight")

    def indexOffset = column[Int]("indexOffset")

    def jailedUntil = column[String]("jailedUntil")

    def tombstoned = column[Boolean]("tombstoned")

    def missedBlocksCounter = column[Int]("missedBlocksCounter")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(signingInfo: SigningInfo): Future[String] = add(signingInfo)

    def insertMultiple(signingInfos: Seq[SigningInfo]): Future[Seq[String]] = addMultiple(signingInfos)

    def insertOrUpdate(signingInfo: SigningInfo): Future[Int] = upsert(signingInfo)

    def getAll: Future[Seq[SigningInfo]] = findAll

    def get(consensusAddress: String): Future[SigningInfo] = findByConsensusAddress(consensusAddress)

    //TODO Optimize using Bech32
    def getByHexAddress(hexAddress: String): Future[SigningInfo] = getAll.map(_.find(_.hexAddress == hexAddress).getOrElse(throw new BaseException(constants.Response.SIGNING_INFO_NOT_FOUND)))

  }

  object Utility {

    def insertOrUpdate(consensusPublicKey: String): Future[Unit] = {
      val signingInfosResponse = getSigningInfo.Service.get(consensusPublicKey)

      def update(signingInfosResponse: SigningInfoResponse) = Service.insertOrUpdate(signingInfosResponse.result.toSigningInfo)

      (for {
        signingInfosResponse <- signingInfosResponse
        _ <- update(signingInfosResponse)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def insertAll(): Future[Unit] = {
      val allSigningInfosResponse = getAllSigningInfos.Service.get

      def update(signingInfosResponse: AllSigningInfosResponse) = Service.insertMultiple(signingInfosResponse.result.map(_.toSigningInfo))

      (for {
        allSigningInfosResponse <- allSigningInfosResponse
        _ <- update(allSigningInfosResponse)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }
  }

}