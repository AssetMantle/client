package utilities

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import queries.{GetResponse, GetTransactionHashResponse}
import transactions.Abstract.BaseRequest
import transactions.responses.TransactionResponse
import transactions.responses.TransactionResponse.{AsyncResponse, BlockResponse, KafkaResponse, SyncResponse}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Transaction @Inject()(getTxHashResponse: GetTransactionHashResponse, getResponse: GetResponse)(implicit executionContext: ExecutionContext, configuration: Configuration, wsClient: WSClient) {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private val responseErrorTransactionHashNotFound: String = constants.Response.PREFIX + constants.Response.FAILURE_PREFIX + configuration.get[String]("blockchain.response.error.transactionHashNotFound")

  def process[T1 <: BaseTransaction[T1], T2 <: BaseRequest](entity: T1, blockchainTransactionCreate: T1 => Future[String], request: T2, action: T2 => Future[WSResponse], onSuccess: (String, BlockResponse) => Future[Unit], onFailure: (String, String) => Future[Unit], updateTransactionHash: (String, String) => Future[Int])(implicit module: String, logger: Logger): Future[String] = {


    val ticketID: Future[String] = if (kafkaEnabled) utilities.JSON.getResponseFromJson[KafkaResponse](action(request)).map(res=> res.ticketID) else Future{utilities.IDGenerator.ticketID}

    ticketID.flatMap{ticketID =>
      blockchainTransactionCreate(entity.mutateTicketID(ticketID)).map{ _ =>
     if(!kafkaEnabled){
        transactionMode match {
          case constants.Transactions.BLOCK_MODE => utilities.JSON.getResponseFromJson[BlockResponse](action(request)).flatMap{blockResponse=>
            onSuccess(ticketID,blockResponse)
          }
          case constants.Transactions.ASYNC_MODE => utilities.JSON.getResponseFromJson[AsyncResponse](action(request)).flatMap{asyncResponse=>
            updateTransactionHash(ticketID,asyncResponse.txhash)
          }
          case constants.Transactions.SYNC_MODE =>utilities.JSON.getResponseFromJson[SyncResponse](action(request)).flatMap{asyncResponse=>
            updateTransactionHash(ticketID,asyncResponse.txhash)
          }
        }
      }
     }

    }.recover{
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        ticketID.map{ticketID=>
          onFailure(ticketID, baseException.failure.message)
        }
    }
    ticketID

  }





  def ticketUpdater(getTickets: () => Future[Seq[String]], getTransactionHash: String => Future[Option[String]], getMode: String => Future[String], onSuccess: (String, BlockResponse) => Future[Unit], onFailure: (String, String) => Future[Unit])(implicit module: String, logger: Logger) {
   // val ticketIDsSeq: Seq[String] = getTickets()
    /*for (ticketID <- ticketIDsSeq) {
      try {
        val blockResponse: BlockResponse = if (kafkaEnabled) {
          getMode(ticketID) match {
            case constants.Transactions.BLOCK_MODE => utilities.JSON.getResponseFromJson[BlockResponse](getResponse.Service.get(ticketID))
            case constants.Transactions.ASYNC_MODE => utilities.JSON.getResponseFromJson[BlockResponse](getTxHashResponse.Service.get(utilities.JSON.getResponseFromJson[AsyncResponse](getResponse.Service.get(ticketID)).txhash))
            case constants.Transactions.SYNC_MODE => utilities.JSON.getResponseFromJson[BlockResponse](getTxHashResponse.Service.get(utilities.JSON.getResponseFromJson[SyncResponse](getResponse.Service.get(ticketID)).txhash))
          }
        } else {
          utilities.JSON.getResponseFromJson[BlockResponse](getTxHashResponse.Service.get(getTransactionHash(ticketID).getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION))))
        }
        if (blockResponse.code.isEmpty) onSuccess(ticketID, blockResponse) else onFailure(ticketID, blockResponse.code.get.toString)
      } catch {
        case baseException: BaseException => if (!baseException.failure.message.matches(responseErrorTransactionHashNotFound)) onFailure(ticketID, baseException.failure.message) else logger.info(baseException.failure.message, baseException)
      }
    }*/

    val ticketIDsSeq: Future[Seq[String]] = getTickets()
    def getBlockResponse(mode:String,ticketID:String)={
      mode match {
        case constants.Transactions.BLOCK_MODE =>
          utilities.JSON.getResponseFromJson[BlockResponse](getResponse.Service.get(ticketID))
        case constants.Transactions.ASYNC_MODE =>
          val transactionResponse=utilities.JSON.getResponseFromJson[AsyncResponse](getResponse.Service.get(ticketID))
          def jsonResponse(transactionResponse:TransactionResponse.AsyncResponse)=utilities.JSON.getResponseFromJson[BlockResponse](getTxHashResponse.Service.get(transactionResponse.txhash))
          for{
            transactionResponse<-transactionResponse
            jsonResponse<-jsonResponse(transactionResponse)
        }yield  jsonResponse
        case constants.Transactions.SYNC_MODE =>
          val transactionResponse=utilities.JSON.getResponseFromJson[SyncResponse](getResponse.Service.get(ticketID))
          def jsonResponse(transactionResponse:TransactionResponse.SyncResponse)=utilities.JSON.getResponseFromJson[BlockResponse](getTxHashResponse.Service.get(transactionResponse.txhash))
          for{
            transactionResponse<-transactionResponse
            jsonResponse<-jsonResponse(transactionResponse)
          }yield  jsonResponse
       }
    }
    def successOrFailure(blockResponse:BlockResponse,ticketID:String)=if (blockResponse.code.isEmpty) onSuccess(ticketID, blockResponse) else onFailure(ticketID, blockResponse.code.get.toString)
    def responseSucessFaliure(ticketIDsSeq:Seq[String])=Future.sequence{

      ticketIDsSeq.map{ticketID=>
        val blockResponse = if (kafkaEnabled) {
          val mode=getMode(ticketID)
          for{
            mode<-mode
            blockResponseVal<-getBlockResponse(mode,ticketID)
          }yield blockResponseVal
        } else {
          val transactionHash=getTransactionHash(ticketID)
          def jsonResponse(transactionHash:Option[String])=utilities.JSON.getResponseFromJson[BlockResponse](getTxHashResponse.Service.get(transactionHash.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION))))
          for{
            transactionHash<-transactionHash
            jsonResponse<-jsonResponse(transactionHash)
          }yield jsonResponse
        }

        for{
          blockResponse<-blockResponse
          _<-successOrFailure(blockResponse,ticketID)
        }yield {}
      }
    }
    for{
      ticketIDsSeq<-ticketIDsSeq
      _<-responseSucessFaliure(ticketIDsSeq)
    }yield {}
  }

}

