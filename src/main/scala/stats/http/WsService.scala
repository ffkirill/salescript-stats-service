package stats.http

import akka.NotUsed
import akka.actor.ActorSystem
import akka.actor.{PoisonPill, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
import stats.EventCollector

import scala.concurrent.ExecutionContext

class WsService()(implicit val system: ActorSystem,
                  fm: ActorMaterializer,
                  ec: ExecutionContext) {

  def createProcessor(sessionId: String): Flow[Message, Message, NotUsed] = {
    val collectingActor = system.actorOf(Props(new EventCollector))

    External.fetchUserCredential(sessionId).foreach({
      case Right(s) => println(s)
      case Left(s) => println(s"Error: $s")
    })

    val incomingMessages: Sink[Message, NotUsed] =
      Flow[Message].map {
        // transform websocket message to domain message
        case TextMessage.Strict(text) => text
      }.to(Sink.actorRef[String](collectingActor, PoisonPill))

    val outgoingMessages: Source[Message, NotUsed] =
      Source.actorRef[String](10, OverflowStrategy.fail).mapMaterializedValue(
        outActor => NotUsed
      ).map(TextMessage(_))
    // then combine both to a flow
    Flow.fromSinkAndSource(incomingMessages, outgoingMessages)
  }

}

object WsService {
  def apply()(implicit system: ActorSystem,
              fm: ActorMaterializer,
              ec: ExecutionContext) = new WsService()(system, fm, ec)
}