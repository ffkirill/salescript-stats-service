package stats

import java.sql.Timestamp
import java.util.UUID

import akka.actor.{Actor, Props}

import stats.models.{EventEntity, User}
import stats.services.EventsDBService

import scala.concurrent.ExecutionContext

sealed trait ScriptPlayerEvent
case object NoSuchEvent extends ScriptPlayerEvent

object ScriptPlayerEventsRecorder {
  final case class NodeReached(from: String, to: String,
                               textFrom: String, textTo: String) extends ScriptPlayerEvent

  val success = "__success__"
  val fail = "__fail__"

  def props(user: User, scriptId: Long)
           (implicit ec:ExecutionContext, eventsService: EventsDBService) =
    Props(new ScriptPlayerEventsRecorder(user, scriptId)(ec, eventsService))
}

class ScriptPlayerEventsRecorder(user: User, scriptId: Long)
                                (implicit ec: ExecutionContext, eventsService: EventsDBService) extends Actor {

  def receive: PartialFunction[Any, Unit] = {
    case ScriptPlayerEventsRecorder.NodeReached(from, to, textFrom, textTo) =>
      to match {
        case ScriptPlayerEventsRecorder.success =>
          eventsService.createEvent(EventEntity(
            userId = user.id,
            scriptId = scriptId,
            fromNodeId = UUID.fromString(from),
            reachedGoalId = Some(0), // Success !TODO: refactor
            textFrom = Some(textFrom),
            timestamp = new Timestamp(System.currentTimeMillis())
          ))
        case ScriptPlayerEventsRecorder.fail =>
          eventsService.createEvent(EventEntity(
            userId = user.id,
            scriptId = scriptId,
            fromNodeId = UUID.fromString(from),
            reachedGoalId = Some(1), // Success !TODO: refactor
            textFrom = Some(textFrom),
            timestamp = new Timestamp(System.currentTimeMillis())
          ))
        case _ =>
          eventsService.createEvent(EventEntity(
            userId = user.id,
            scriptId = scriptId,
            fromNodeId = UUID.fromString(from),
            toNodeId = Some(UUID.fromString(to)),
            textFrom = Some(textFrom),
            textTo = Some(textTo),
            timestamp = new Timestamp(System.currentTimeMillis())
        ))
      }
  }
}
