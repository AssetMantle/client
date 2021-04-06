package queries.responses.blockchain

import blockchainTx.common.{ID, Immutables, Mutables}
import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object OrderResponse {

  case class OrderIDValue(chainID: ID, maintainersID: ID, hashID: ID)

  implicit val orderIDValueReads: Reads[OrderIDValue] = Json.reads[OrderIDValue]

  case class OrderID(value: OrderIDValue)

  implicit val orderIDReads: Reads[OrderID] = Json.reads[OrderID]

  case class Order(id: OrderID, immutables: Immutables, mutables: Mutables)

  implicit val orderReads: Reads[Order] = Json.reads[Order]

  case class OrderValue(value: Order)

  implicit val orderValueReads: Reads[OrderValue] = Json.reads[OrderValue]

  case class OrdersValue(id: ID, list: Seq[OrderValue])

  implicit val ordersValueReads: Reads[OrdersValue] = Json.reads[OrdersValue]

  case class Orders(value: OrdersValue)

  implicit val ordersReads: Reads[Orders] = Json.reads[Orders]

  case class Value(Orders: Orders)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Result(value: Value)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(height: String, result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
