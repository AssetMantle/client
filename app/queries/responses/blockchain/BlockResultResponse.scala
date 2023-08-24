package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Event
import transactions.Abstract.BaseResponse

object BlockResultResponse {

  case class TxResult(code: Int, events: Seq[Event]) {

    lazy val decodedEvents: Seq[Event] = this.events.map(_.decode)

    def getSubmitProposalIDs: Seq[Int] = this.decodedEvents
      .filter(x => x.`type` == schema.constants.Event.SubmitProposal && x.attributes.exists(_.key == schema.constants.Event.Attribute.ProposalID))
      .flatMap(_.attributes)
      .filter(_.key == schema.constants.Event.Attribute.ProposalID)
      .flatMap(_.value.map(_.toInt))

    def getProposalSubmitters: Seq[String] = this.decodedEvents
      .filter(x => x.`type` == schema.constants.Event.Message && x.attributes.exists(y => y.key == schema.constants.Event.Attribute.Module && y.value.getOrElse("") == "governance") && x.attributes.exists(y => y.key == schema.constants.Event.Attribute.Sender))
      .flatMap(_.attributes)
      .filter(_.key == schema.constants.Event.Attribute.Sender)
      .flatMap(_.value)

    def status: Boolean = code == 0

  }

  implicit val txResultReads: Reads[TxResult] = Json.reads[TxResult]

  case class Result(height: String, txs_results: Option[Seq[TxResult]], begin_block_events: Seq[Event], end_block_events: Option[Seq[Event]]) {

    lazy val decodedSuccessfulTxEvents: Seq[Event] = this.txs_results.fold[Seq[Event]](Seq())(_.filter(_.status).flatMap(_.decodedEvents))

    lazy val decodedBeginBlockEvents: Seq[Event] = this.begin_block_events.map(_.decode)

    lazy val decodedEndBlockEvents: Seq[Event] = this.end_block_events.getOrElse(Seq()).map(_.decode)

    def getSlashingEvents: Seq[Event] = this.decodedBeginBlockEvents.filter(_.`type` == schema.constants.Event.Slash)

    def getLivenessEvents: Seq[Event] = this.decodedBeginBlockEvents.filter(_.`type` == schema.constants.Event.Liveness)

    def getAllEvents: Seq[Event] = this.decodedBeginBlockEvents ++ this.decodedSuccessfulTxEvents ++ this.decodedEndBlockEvents

    def getActiveInactiveProposalEvents: Seq[Event] = this.decodedEndBlockEvents.filter(x => x.`type` == schema.constants.Event.ActiveProposal || x.`type` == schema.constants.Event.InactiveProposal)

  }

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
