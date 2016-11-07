package stats

import akka.actor.Actor

class EventCollector() extends Actor {

  def receive = {
    case text: String =>
      print(text)
  }

}
