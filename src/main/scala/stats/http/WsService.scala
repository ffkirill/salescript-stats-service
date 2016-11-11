package stats.http

import akka.NotUsed
import akka.actor.ActorSystem
import akka.actor.{PoisonPill, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
import spray.json._
import spray.json.DefaultJsonProtocol
import stats.{NoSuchEvent, ScriptPlayerEvent, ScriptPlayerEventsSource}

import scala.concurrent.ExecutionContext

trait Protocols extends DefaultJsonProtocol {
  implicit val nodeReachedFormat = jsonFormat2(ScriptPlayerEventsSource.NodeReached.apply)
}


class WsService()(implicit val system: ActorSystem,
                  fm: ActorMaterializer,
                  ec: ExecutionContext) extends Protocols {

  def createProcessor(sessionId: String, scriptId: Int): Flow[Message, Message, NotUsed] = {
    val collectingActor = system.actorOf(Props(new ScriptPlayerEventsSource))

    External.fetchUserCredential(sessionId).foreach({
      case Right(user) => collectingActor ! ScriptPlayerEventsSource.Authorized(scriptId, user.id)
      case Left(s) => println(s"Error: $s")
    })

    val incomingMessages: Sink[Message, NotUsed] =
      Flow[Message].map {
        // transform websocket message to domain message
        case TextMessage.Strict(text) => text.parseJson.convertTo[ScriptPlayerEventsSource.NodeReached]
        case _ => NoSuchEvent
      }.to(Sink.actorRef[ScriptPlayerEvent](collectingActor, PoisonPill))

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