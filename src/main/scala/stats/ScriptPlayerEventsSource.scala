package stats

import akka.actor.Actor
import stats.ScriptPlayerEventsSource.Authorized

sealed trait ScriptPlayerEvent
case object NoSuchEvent extends ScriptPlayerEvent

object ScriptPlayerEventsSource {
  case class Authorized(scriptId: Int, userId: Int)
  final case class NodeReached(from: String, to: String) extends ScriptPlayerEvent
}

class ScriptPlayerEventsSource() extends Actor {

  def receive = {
    case Authorized(scriptId, userId) =>
      context.become(authorized(scriptId, userId), discardOld = true)
  }

  def authorized(scriptId: Int, userId: Int): Receive = {
    case ScriptPlayerEventsSource.NodeReached(from, to) =>
      print(from)
      print(to)
  }

}
