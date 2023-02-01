package models.blockchain

//import models.traits.{Entity, GenericDaoImpl, Logging, ModelTable}
//import play.api.Logger
//import play.api.db.slick.DatabaseConfigProvider
//import schema.id.base.ClassificationID
//import schema.list.PropertyList
//import schema.qualified.{Immutables, Mutables}
//import slick.jdbc.H2Profile.api._
//
//import javax.inject.{Inject, Singleton}
//import scala.concurrent.{ExecutionContext, Future}
//
//case class Asset(id: Array[Byte], classificationID: Array[Byte], immutables: Array[Byte], mutables: Array[Byte], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity[Array[Byte]] {
//
//  def getID: String = commonUtilities.Secrets.base64URLEncoder(this.id)
//
////  def getClassificationID = ClassificationID(this.classificationID)
//
//  def getImmutables: Immutables = Immutables(this.immutables)
//
//  def getMutables: Mutables = Mutables(this.immutables)
//
//}
//
//object Assets {
//
//  implicit val module: String = constants.Module.BLOCKCHAIN_ASSET
//
//  implicit val logger: Logger = Logger(this.getClass)
//
//  class DataTable(tag: Tag) extends Table[Asset](tag, "Asset") with ModelTable[Array[Byte]] {
//
//    def * = (id, immutables, mutables, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (Asset.tupled, Asset.unapply)
//
//    def id = column[Array[Byte]]("id", O.PrimaryKey)
//
//    def classificationID = column[Array[Byte]]("classificationID")
//
//    def immutables = column[Array[Byte]]("immutableMetas")
//
//    def mutables = column[Array[Byte]]("mutables")
//
//    def createdBy = column[String]("createdBy")
//
//    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")
//
//    def updatedBy = column[String]("updatedBy")
//
//    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")
//
//  }
//
//  val TableQuery = new TableQuery(tag => new DataTable(tag))
//
//}
//
//@Singleton
//class Assets @Inject()(
//                                 protected val databaseConfigProvider: DatabaseConfigProvider
//                               )(implicit override val executionContext: ExecutionContext)
//  extends GenericDaoImpl[Assets.DataTable, Asset, Array[Byte]](
//    databaseConfigProvider,
//    Assets.TableQuery,
//    executionContext,
//    Assets.module,
//    Assets.logger
//  ) {
//
//  object Service {
//
//    def add(asset: Asset): Future[String] = create(asset).map(x => commonUtilities.Secrets.base64URLEncoder(x))
//
//    def get(id: String): Future[Option[Asset]] = getById(commonUtilities.Secrets.base64URLDecode(id))
//
//    def get(id: Array[Byte]): Future[Option[Asset]] = getById(id)
//
//    def tryGet(id: String): Future[Asset] = tryGetById(commonUtilities.Secrets.base64URLDecode(id))
//
//    def tryGet(id: Array[Byte]): Future[Asset] = tryGetById(id)
//
//    def fetchAll: Future[Seq[Asset]] = getAll
//
//
//  }
//
//  object Utility {
//
//    def onMintAsset(msg: com.assets.transactions.define.Message): Future[String] = {
//      val immutables = Immutables(PropertyList(PropertyList(msg.getImmutableMetaProperties).propertyList ++ PropertyList(msg.getImmutableProperties).propertyList))
//      val mutables = Mutables(PropertyList(PropertyList(msg.getMutableMetaProperties).propertyList ++ PropertyList(msg.getMutableProperties).propertyList))
//      val assetID = commonUtilities.ID.getAssetID(immutables = immutables, mutables = mutables)
//      val asset = Asset(assetID.getBytes, immutables.asProtoImmutables.toByteString.toByteArray, mutables.asProtoMutables.toByteString.toByteArray)
//      val add = Service.add(asset)
//
//      for {
//        _ <- add
//      } yield msg.getFrom
//    }
//
//  }
//}