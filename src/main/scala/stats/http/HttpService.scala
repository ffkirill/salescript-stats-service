package stats.http

import akka.NotUsed
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.model.ws.Message

import scala.concurrent.ExecutionContext


object HttpServiceTypes {
  type WsMessagesProcessorFactoryT = (String, Int) => Flow[Message, Message, NotUsed]
}


class HttpService(wsMessagesProcessorFactory: HttpServiceTypes.WsMessagesProcessorFactoryT)
                 (implicit executionContext: ExecutionContext) {
  val route =
    path("collect") {
      get {
        parameter("scriptId".as[Int]) {
          scriptId => cookie("sessionid") {
            pair => handleWebSocketMessages(wsMessagesProcessorFactory(pair.value, scriptId)) }
        }
      }
    }
}

object HttpService {
  def apply(wsMessagesProcessorFactory: HttpServiceTypes.WsMessagesProcessorFactoryT)
           (implicit executionContext: ExecutionContext): HttpService =
    new HttpService(wsMessagesProcessorFactory)(executionContext)
}
