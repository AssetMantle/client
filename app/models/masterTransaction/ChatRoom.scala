package models.masterTransaction

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ChatRoom(id: String, fromAccountID: String, tradeRoomID: String, chatContent: String, time: Timestamp, sellerRead: Boolean, buyerRead: Boolean, financierVisibility: Boolean, financierRead: Boolean, deleteStatus: Boolean)

@Singleton
class ChatRooms @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_CHAT_ROOM

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val chatRoomTable = TableQuery[ChatRoomTable]

  private def add(chatRoom: ChatRoom): Future[String] = db.run((chatRoomTable returning chatRoomTable.map(_.id) += chatRoom).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findChatsByTraderRoomID(tradeRoomID: String, offset: Int, limit: Int): Future[Seq[ChatRoom]] = db.run(chatRoomTable.filter(_.tradeRoomID === tradeRoomID).sortBy(_.time.desc).drop(offset).take(limit).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  //  private def findNumberOfReadOnStatusByAccountId(accountID: String, status: Boolean): Future[Int] = db.run(chatRoomTable.filter(_.accountID === accountID).filter(_.read === status).length.result.asTry).map {
  //    case Success(result) => result
  //    case Failure(exception) => exception match {
  //      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
  //        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
  //    }
  //  }
  //
  //  private def updateReadById(id: String, status: Boolean): Future[Int] = db.run(chatRoomTable.filter(_.id === id).map(_.read).update(status).asTry).map {
  //    case Success(result) => result
  //    case Failure(exception) => exception match {
  //      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
  //        throw new BaseException(constants.Response.PSQL_EXCEPTION)
  //      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
  //        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
  //    }
  //  }
  //
  //  private def deleteById(accountID: String) = db.run(chatRoomTable.filter(_.accountID === accountID).delete.asTry).map {
  //    case Success(result) => result
  //    case Failure(exception) => exception match {
  //      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
  //        throw new BaseException(constants.Response.PSQL_EXCEPTION)
  //      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
  //        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
  //    }
  //  }

  private[models] class ChatRoomTable(tag: Tag) extends Table[ChatRoom](tag, "ChatRoom") {

    def * = (id, fromAccountID, tradeRoomID, chatContent, time, buyerRead, sellerRead, financierVisibility, financierRead, deleteStatus) <> (ChatRoom.tupled, ChatRoom.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def fromAccountID = column[String]("fromAccountID")

    def tradeRoomID = column[String]("tradeRoomID")

    def chatContent = column[String]("chatContent")

    def time = column[Timestamp]("time")

    def buyerRead = column[Boolean]("buyerRead")

    def sellerRead = column[Boolean]("sellerRead")

    def financierVisibility = column[Boolean]("financierVisibility")

    def financierRead = column[Boolean]("financierRead")

    def deleteStatus = column[Boolean]("deleteStatus")

  }

  object Service {

    def create(fromAccountID: String, tradeRoomID: String, chatContent: String, buyerRead: Boolean, sellerRead: Boolean, financierVisibility: Boolean, financierRead: Boolean): Future[ChatRoom] = {
      val chatRoom = ChatRoom(id = utilities.IDGenerator.hexadecimal, fromAccountID = fromAccountID, tradeRoomID = tradeRoomID, chatContent = chatContent, time = new Timestamp(System.currentTimeMillis), buyerRead = buyerRead, sellerRead= sellerRead, financierVisibility = financierVisibility, financierRead = financierRead, deleteStatus = false)
      add(chatRoom)
      Future(chatRoom)
    }

    def get(tradeRoomID: String, offset: Int, limit: Int): Future[Seq[ChatRoom]] = findChatsByTraderRoomID(tradeRoomID = tradeRoomID, offset = offset, limit = limit)

    //    def markAsRead(id: String): Future[Int] = updateReadById(id = id, status = true)
    //
    //    def getNumberOfUnread(accountID: String): Future[Int] = findNumberOfReadOnStatusByAccountId(accountID = accountID, status = false)

  }

}