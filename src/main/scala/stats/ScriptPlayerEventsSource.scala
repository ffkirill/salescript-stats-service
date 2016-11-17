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
  val success = "__success__"
  val fail = "__fail__"
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
      to match {
        case ScriptPlayerEventsSource.success =>
          eventsService.createEvent(EventEntity(
            userId=userId,
            scriptId=scriptId,
            fromNodeId=UUID.fromString(from),
            reachedGoalId=Some(0), // Success !TODO: refactor
            timestamp = new Timestamp(System.currentTimeMillis())
          ))
        case ScriptPlayerEventsSource.fail =>
          eventsService.createEvent(EventEntity(
            userId=userId,
            scriptId=scriptId,
            fromNodeId=UUID.fromString(from),
            reachedGoalId=Some(1), // Success !TODO: refactor
            timestamp = new Timestamp(System.currentTimeMillis())
          ))
        case _ =>
          eventsService.createEvent(EventEntity(
            userId=userId,
            scriptId=scriptId,
            fromNodeId=UUID.fromString(from),
            toNodeId=Some(UUID.fromString(to)),
            timestamp=new Timestamp(System.currentTimeMillis())
        ))
      }
      println(from)
      println(to)
  }
}
