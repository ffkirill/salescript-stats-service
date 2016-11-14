package stats

import java.sql.Timestamp
import java.util.UUID


import akka.actor.{Actor, Props}
import stats.ScriptPlayerEventsSource.Authorized
import stats.models.EventEntity
import stats.services.EventsService

import scala.concurrent.ExecutionContext

sealed trait ScriptPlayerEvent
case object NoSuchEvent extends ScriptPlayerEvent

object ScriptPlayerEventsSource {
  case class Authorized(scriptId: Int, userId: Int)
  final case class NodeReached(from: String, to: String) extends ScriptPlayerEvent
  def props(eventsService: EventsService)(implicit ec:ExecutionContext) =
    Props(new ScriptPlayerEventsSource(eventsService)(ec))
}

class ScriptPlayerEventsSource(eventsService: EventsService)
                              (implicit ec:ExecutionContext) extends Actor {

  def receive = {
    case Authorized(scriptId, userId) =>
      context.become(authorized(scriptId, userId), discardOld = true)
  }

  def authorized(scriptId: Int, userId: Int): Receive = {
    case ScriptPlayerEventsSource.NodeReached(from, to) =>
      println(from)
      println(to)
      eventsService.createEvent(EventEntity(
        Option.empty,
        userId,
        scriptId,
        UUID.fromString(from),
        UUID.fromString(to),
        Option.empty,
        new Timestamp(System.currentTimeMillis())
      ))

  }

}
