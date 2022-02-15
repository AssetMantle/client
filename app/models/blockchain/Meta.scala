package models.blockchain

import exceptions.BaseException
import models.Trait.Logged
import models.common.ID.MetaID
import models.common.Serializable._
import models.common.TransactionMessages.MetaReveal
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.responses.common.Header
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Meta(id: MetaID, dataValue: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Metas @Inject()(
                       protected val databaseConfigProvider: DatabaseConfigProvider,
                       configuration: Configuration,
                       utilitiesOperations: utilities.Operations,
                     )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_META

  import databaseConfig.profile.api._

  private[models] val metaTable = TableQuery[MetaTable]

  case class MetaSerialized(typeID: String, hashID: String, dataValue: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Meta = Meta(id = MetaID(typeID = typeID, hashID = hashID), dataValue = dataValue, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(meta: Meta): MetaSerialized = MetaSerialized(typeID = meta.id.typeID, hashID = meta.id.hashID, dataValue = meta.dataValue, createdBy = meta.createdBy, createdOn = meta.createdOn, createdOnTimeZone = meta.createdOnTimeZone, updatedBy = meta.updatedBy, updatedOn = meta.updatedOn, updatedOnTimeZone = meta.updatedOnTimeZone)

  private def add(meta: Meta): Future[String] = db.run((metaTable returning metaTable.map(_.hashID) += serialize(meta)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.META_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(metas: Seq[Meta]): Future[Seq[String]] = db.run((metaTable returning metaTable.map(_.hashID) ++= metas.map(x => serialize(x))).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.META_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(meta: Meta): Future[Int] = db.run(metaTable.insertOrUpdate(serialize(meta)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.META_UPSERT_FAILED, psqlException)
    }
  }

  private def tryGetByTypeIDAndHashType(typeID: String, hashID: String) = db.run(metaTable.filter(x => x.typeID === typeID && x.hashID === hashID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.META_NOT_FOUND, noSuchElementException)
    }
  }

  private def getByTypeIDAndHashID(typeID: String, hashID: String) = db.run(metaTable.filter(x => x.typeID === typeID && x.hashID === hashID).result.headOption)

  private def checkIfExistsByIDAndDataType(typeID: String, hashID: String) = db.run(metaTable.filter(x => x.typeID === typeID && x.hashID === hashID).exists.result)

  private[models] class MetaTable(tag: Tag) extends Table[MetaSerialized](tag, "Meta_BC") {

    def * = (typeID, hashID, dataValue, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (MetaSerialized.tupled, MetaSerialized.unapply)

    def typeID = column[String]("typeID", O.PrimaryKey)

    def hashID = column[String]("hashID", O.PrimaryKey)

    def dataValue = column[String]("dataValue")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(data: Data): Future[String] = add(Meta(MetaID(typeID = data.dataType, hashID = data.value.generateHash), dataValue = data.value.asString))

    def create(meta: Meta): Future[String] = add(meta)

    def tryGet(id: MetaID): Future[Meta] = tryGetByTypeIDAndHashType(typeID = id.typeID, hashID = id.hashID).map(_.deserialize)

    def tryGetData(id: MetaID): Future[Data] = tryGetByTypeIDAndHashType(typeID = id.typeID, hashID = id.hashID).map(x => Data(dataType = x.typeID, dataValue = Option(x.dataValue)))

    def get(id: MetaID): Future[Option[Meta]] = getByTypeIDAndHashID(typeID = id.typeID, hashID = id.hashID).map(_.map(_.deserialize))

    def getData(id: MetaID): Future[Option[Data]] = getByTypeIDAndHashID(typeID = id.typeID, hashID = id.hashID).map(metaOption => metaOption.fold[Option[Data]](None)(x => Option(Data(dataType = x.typeID, dataValue = Option(x.dataValue)))))

    def insertMultiple(metaList: Seq[Meta]): Future[Seq[String]] = addMultiple(metaList)

    def insertMultipleData(dataList: Seq[Data]): Future[Seq[String]] = addMultiple(dataList.map(x => Meta(id = MetaID(typeID = x.dataType, hashID = x.value.generateHash), dataValue = x.value.asString)))

    def insertOrUpdate(meta: Meta): Future[Int] = upsert(meta)

    def insertOrUpdate(data: Data): Future[Int] = upsert(Meta(id = MetaID(typeID = data.dataType, hashID = data.value.generateHash), dataValue = data.value.asString))

    def checkIfExists(id: MetaID): Future[Boolean] = checkIfExistsByIDAndDataType(typeID = id.typeID, hashID = id.hashID)

  }

  object Utility {

    def onReveal(metaReveal: MetaReveal)(implicit header: Header): Future[Unit] = {
      val upsertMeta = Service.insertOrUpdate(metaReveal.metaFact.data)
      (for {
        _ <- upsertMeta
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.META_REVEAL + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def auxiliaryScrub(metaPropertyList: Seq[MetaProperty]): Future[Seq[Property]] = {
      val upsertMetas = utilitiesOperations.traverse(metaPropertyList) { metaProperty =>
        if (metaProperty.metaFact.getHash != "") {
          val upsertMeta = Service.insertOrUpdate(metaProperty.metaFact.data)

          (for {
            _ <- upsertMeta
          } yield ()
            ).recover {
            case _: BaseException =>
          }
        } else Future()
      }

      (for {
        _ <- upsertMetas
      } yield metaPropertyList.map(_.removeData())
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

  }

}