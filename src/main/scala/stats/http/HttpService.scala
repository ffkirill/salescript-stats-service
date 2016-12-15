package stats.http

import akka.NotUsed
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext
import stats.models.User

object HttpServiceTypes {
  type EventsRecorderFlowFactoryT = ((User, Long) => Flow[Message, Message, NotUsed])
}

class HttpService(eventsRecorderFlowFactory: HttpServiceTypes.EventsRecorderFlowFactoryT)
                 (implicit ec: ExecutionContext,
                  val externalService: ExternalService) extends SecurityDirectives {

  val route: Route =
    authenticate { user =>
      get {
        path("collect") {
          parameter("scriptId".as[Long]) {
            scriptId => handleWebSocketMessages(eventsRecorderFlowFactory(user, scriptId)) }
        }
      }
    }
}

object HttpService {
  def apply(wsMessagesProcessorFactory: HttpServiceTypes.EventsRecorderFlowFactoryT)
           (implicit ec: ExecutionContext, externalService: ExternalService): HttpService =
    new HttpService(wsMessagesProcessorFactory)(ec, externalService)
}
