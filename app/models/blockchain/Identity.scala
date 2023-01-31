package models.blockchain

import models.traits.{Entity, GenericDaoImpl, Logging, ModelTable}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import schema.list.PropertyList
import schema.qualified.{Immutables, Mutables}
import slick.jdbc.H2Profile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class Identity(id: Array[Byte], classificationID: Array[Byte], immutables: Array[Byte], mutables: Array[Byte], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity[Array[Byte]] {

  def getID: String = commonUtilities.Secrets.base64URLEncoder(this.id)

  //  def getClassificationID = ClassificationID(this.classificationID)

  def getImmutables: Immutables = Immutables(this.immutables)

  def getMutables: Mutables = Mutables(this.immutables)

}

object Identitys {

  implicit val module: String = constants.Module.BLOCKCHAIN_IDENTITY

  implicit val logger: Logger = Logger(this.getClass)

  class DataTable(tag: Tag) extends Table[Identity](tag, "Identity") with ModelTable[Array[Byte]] {

    def * = (id, classificationID, immutables, mutables, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (Identity.tupled, Identity.unapply)

    def id = column[Array[Byte]]("id", O.PrimaryKey)

    def classificationID = column[Array[Byte]]("classificationID")

    def immutables = column[Array[Byte]]("immutableMetas")

    def mutables = column[Array[Byte]]("mutables")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")

  }

  val TableQuery = new TableQuery(tag => new DataTable(tag))

}

@Singleton
class Identities @Inject()(
                            protected val databaseConfigProvider: DatabaseConfigProvider
                          )(implicit override val executionContext: ExecutionContext)
  extends GenericDaoImpl[Identitys.DataTable, Identity, Array[Byte]](
    databaseConfigProvider,
    Identitys.TableQuery,
    executionContext,
    Identitys.module,
    Identitys.logger
  ) {

  object Service {

    def add(identity: Identity): Future[String] = create(identity).map(x => commonUtilities.Secrets.base64URLEncoder(x))

    def get(id: String): Future[Option[Identity]] = getById(commonUtilities.Secrets.base64URLDecode(id))

    def get(id: Array[Byte]): Future[Option[Identity]] = getById(id)

    def tryGet(id: String): Future[Identity] = tryGetById(commonUtilities.Secrets.base64URLDecode(id))

    def tryGet(id: Array[Byte]): Future[Identity] = tryGetById(id)

    def fetchAll: Future[Seq[Identity]] = getAll


  }

  object Utility {

//    def onNub(msg: com.identities.transactions.nub.Message): Future[String] = {
//      val immutables = Immutables(PropertyList(PropertyList(msg.getImmutableMetaProperties).propertyList ++ PropertyList(msg.getImmutableProperties).propertyList))
//      val mutables = Mutables(PropertyList(PropertyList(msg.getMutableMetaProperties).propertyList ++ PropertyList(msg.getMutableProperties).propertyList))
//      val identityID = commonUtilities.ID.getIdentityID(immutables = immutables, mutables = mutables)
//      val identity = Identity(identityID.getBytes, immutables.asProtoImmutables.toByteString.toByteArray, mutables.asProtoMutables.toByteString.toByteArray)
//      val add = Service.add(identity)
//
//      for {
//        _ <- add
//      } yield msg.getFrom
//    }

  }
}