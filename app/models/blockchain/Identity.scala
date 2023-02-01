package models.blockchain

import models.traits.{Entity, GenericDaoImpl, Logging, ModelTable}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import schema.data.base.{AccAddressData, IDData, ListData}
import schema.id.base.StringID
import schema.list.PropertyList
import schema.qualified.{Immutables, Mutables}
import slick.jdbc.H2Profile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class Identity(id: Array[Byte], classificationID: Array[Byte], immutables: Array[Byte], mutables: Array[Byte], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity[Array[Byte]] {

  def getID: String = utilities.Secrets.base64URLEncoder(this.id)

  def getClassificationID: String = utilities.Secrets.base64URLEncoder(this.classificationID)

  def getImmutables: Immutables = Immutables(this.immutables)

  def getMutables: Mutables = Mutables(this.immutables)

}

object Identities {

  implicit val module: String = constants.Module.BLOCKCHAIN_IDENTITY

  implicit val logger: Logger = Logger(this.getClass)

  class DataTable(tag: Tag) extends Table[Identity](tag, "Identity") with ModelTable[Array[Byte]] {

    def * = (id, classificationID, immutables, mutables, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (Identity.tupled, Identity.unapply)

    def id = column[Array[Byte]]("id", O.PrimaryKey)

    def classificationID = column[Array[Byte]]("classificationID")

    def immutables = column[Array[Byte]]("immutables")

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
  extends GenericDaoImpl[Identities.DataTable, Identity, Array[Byte]](
    databaseConfigProvider,
    Identities.TableQuery,
    executionContext,
    Identities.module,
    Identities.logger
  ) {

  object Service {

    def add(identity: Identity): Future[String] = create(identity).map(x => utilities.Secrets.base64URLEncoder(x))

    def get(id: String): Future[Option[Identity]] = getById(utilities.Secrets.base64URLDecode(id))

    def get(id: Array[Byte]): Future[Option[Identity]] = getById(id)

    def tryGet(id: String): Future[Identity] = tryGetById(utilities.Secrets.base64URLDecode(id))

    def tryGet(id: Array[Byte]): Future[Identity] = tryGetById(id)

    def fetchAll: Future[Seq[Identity]] = getAll


  }

  object Utility {

    def onNub(msg: com.identities.transactions.nub.Message): Future[String] = try {
      val immutables = Immutables(PropertyList(Seq(constants.Blockchain.NubProperty.copy(data = IDData(StringID(msg.getNubID).toAnyID).toAnyData))))
      val mutables = Mutables(PropertyList(Seq(constants.Blockchain.AuthenticationProperty.copy(data = ListData(Seq(AccAddressData(utilities.Crypto.convertAddressToAccAddressBytes(msg.getFrom)).toAnyData)).toAnyData))))
      val identityID = utilities.ID.getIdentityID(classificationID = constants.Blockchain.NubClassificationID, immutables = immutables)
      val identity = Identity(id = identityID.getBytes, classificationID = constants.Blockchain.NubClassificationID.getBytes, immutables = immutables.getProtoBytes, mutables = mutables.getProtoBytes)
      val add = Service.add(identity)

      for {
        _ <- add
      } yield msg.getFrom
    }

  }
}