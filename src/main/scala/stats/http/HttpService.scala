package stats.http

import akka.NotUsed
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext
import stats.models.User
import stats.services.EventsDBService
import de.heikoseeberger.akkahttpcirce.CirceSupport
import io.circe.generic.auto._
import io.circe.syntax._
import stats.utils.Protocols


private object HttpServiceTypes {
  type EventsRecorderFlowFactoryT = ((User, Long) => Flow[Message, Message, NotUsed])
}

class HttpService(eventsRecorderFlowFactory: HttpServiceTypes.EventsRecorderFlowFactoryT)
                 (implicit ec: ExecutionContext,
                  val externalService: ExternalService,
                  eventsService: EventsDBService)
  extends CirceSupport with Protocols with SecurityDirectives {

  val route: Route =
    authenticate { user =>
      get {
        path("collect") {
          parameter("scriptId".as[Long]) {
            scriptId => handleWebSocketMessages(eventsRecorderFlowFactory(user, scriptId)) }
        } ~
        pathPrefix("stats-v1-query" / IntNumber) { id =>
          path("summary") {
            complete(eventsService.getByScriptId(id).map(_.asJson))
          }
        }
      }
    }
}

object HttpService {
  def apply(wsMessagesProcessorFactory: HttpServiceTypes.EventsRecorderFlowFactoryT)
           (implicit ec: ExecutionContext,
            externalService: ExternalService,
            eventsService: EventsDBService): HttpService =
    new HttpService(wsMessagesProcessorFactory)(ec, externalService, eventsService)
}
