package stats.http

import akka.NotUsed
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.model.ws.Message

import scala.concurrent.ExecutionContext

class HttpService(wsMessagesProcessorFactory: (String => Flow[Message, Message, NotUsed]))
                 (implicit executionContext: ExecutionContext) {
  val route =
    path("collect") {
      get {
        cookie("sessionid") { pair => handleWebSocketMessages(wsMessagesProcessorFactory(pair.value)) }
      }
    }
}

object HttpService {
  def apply(wsMessagesProcessorFactory: (String => Flow[Message, Message, NotUsed]))
           (implicit executionContext: ExecutionContext): HttpService =
    new HttpService(wsMessagesProcessorFactory)(executionContext)
}
